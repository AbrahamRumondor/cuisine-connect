package com.example.cuisineconnect.data.pagingSource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.data.response.UserResponse
import com.example.cuisineconnect.domain.model.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * HomePagingSource handles loading and paginating posts and recipes for the home feed.
 *
 * Logic:
 * 1. **No Following Users**:
 *    - Fetches fallback posts and recipes sorted by upvotes.
 *    - Ensures users see popular content even without following anyone.
 *
 * 2. **Following Users Exist**:
 *    - Fetches posts and recipes for each followed user, sorted by date (most recent first).
 *    - Tracks the last visible document (`lastPostSnapshots`, `lastRecipeSnapshots`) for efficient pagination.
 *    - Combines posts and recipes into a single feed.
 *
 * 3. **Duplicate Handling**:
 *    - Uses a `Set` (`itemIdsSet`) to ensure no duplicate items (by ID) appear in the feed.
 *
 * 4. **Feed Sorting**:
 *    - Content from followed users is sorted by date (newest first).
 *    - Fallback posts and recipes (sorted by upvotes) are appended to fill the feed.
 *
 * 5. **Pagination**:
 *    - Continues fetching posts and recipes from `startAfter()` the last visible document.
 *
 * Helper Methods:
 * - `fetchPostsAndRecipes()`: Fetches fallback posts and recipes when needed.
 * - `getQueryForPosts(userId, limit)`: Fetches posts for a specific user, sorted by date, with pagination.
 * - `getQueryForRecipes(userId, limit)`: Fetches recipes for a specific user, sorted by date, with pagination.
 */
class HomePagingSource(
  private val user: User,
  private val postsRef: CollectionReference,
  private val recipesRef: CollectionReference,
  private val usersRef: CollectionReference,
  private var isRefreshing: Boolean = false
) : PagingSource<QuerySnapshot, FeedItem>() {

  // Store snapshots separately for each user
  private val lastPostSnapshots = mutableMapOf<String, DocumentSnapshot?>()
  private val lastRecipeSnapshots = mutableMapOf<String, DocumentSnapshot?>()

  private suspend fun fetchPostsAndRecipes(fallbackPageSize: Int): List<FeedItem> {
    return try {
      // Fetch posts sorted by upvotes
      val postsQuery = postsRef
        .orderBy("post_upvotes", Query.Direction.DESCENDING)
        .limit(fallbackPageSize.toLong())
        .get()
        .await()

      val posts = postsQuery.documents.mapNotNull { doc ->
        val post = doc.toObject(PostResponse::class.java)
        val userId = post?.id?.substringAfter("_")?.substringBefore("_")
        userId?.let {
          val user = usersRef.document(it).get().await().toObject(UserResponse::class.java)
          if (post != null && user != null) {
            FeedItem.PostItem(UserResponse.transform(user), PostResponse.transform(post))
          } else null
        }
      }

      // Fetch recipes sorted by upvotes
      val recipesQuery = recipesRef
        .orderBy("recipe_upvotes", Query.Direction.DESCENDING)
        .limit(fallbackPageSize.toLong())
        .get()
        .await()

      val recipes = recipesQuery.documents.mapNotNull { doc ->
        val recipe = doc.toObject(RecipeResponse::class.java)
        val userId = recipe?.id?.substringAfter("_")?.substringBefore("_")
        userId?.let {
          val user = usersRef.document(it).get().await().toObject(UserResponse::class.java)
          if (recipe != null && user != null) {
            FeedItem.RecipeItem(UserResponse.transform(user), RecipeResponse.transform(recipe))
          } else null
        }
      }

      // Combine and sort by upvotes for a unified feed
      (posts + recipes).sortedByDescending { feedItem ->
        when (feedItem) {
          is FeedItem.PostItem -> feedItem.post.upvotes.size
          is FeedItem.RecipeItem -> feedItem.recipe.upvotes.size
        }
      }
    } catch (e: Exception) {
      Timber.tag("HomePagingSource").e(e, "Error fetching fallback posts and recipes")
      emptyList()
    }
  }

  override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, FeedItem> {
    return try {
      if (isRefreshing) invalidate()

      val followingUsers = listOf(user.id).plus(user.following)
      val pageSize = params.loadSize / (if (followingUsers.isEmpty()) 1 else followingUsers.size)
      val feedItems = mutableListOf<FeedItem>()
      val itemIdsSet = mutableSetOf<String>() // To track unique IDs

      // Case 1: No following users, fetch fallback posts and recipes
      if (followingUsers.isEmpty()) {
        feedItems.addAll(fetchPostsAndRecipes(params.loadSize))
      } else {
        // Fetch posts and recipes for following users
        for (userId in followingUsers) {
          val postsQuery = getQueryForPosts(userId, pageSize)
          val postsResult = postsQuery.get().await()
          val posts = postsResult.toObjects(PostResponse::class.java)

          lastPostSnapshots[userId] = postsResult.documents.lastOrNull()

          for (post in posts) {
            val postUser = usersRef.document(post.id.substringAfter("_").substringBefore("_"))
              .get().await().toObject(UserResponse::class.java)
            postUser?.let {
              val feedItem = FeedItem.PostItem(UserResponse.transform(it), PostResponse.transform(post))
              if (itemIdsSet.add(post.id)) { // Add only if the ID is unique
                feedItems.add(feedItem)
              }
            }
          }

          val recipesQuery = getQueryForRecipes(userId, pageSize)
          val recipesResult = recipesQuery.get().await()
          val recipes = recipesResult.toObjects(RecipeResponse::class.java)

          lastRecipeSnapshots[userId] = recipesResult.documents.lastOrNull()

          for (recipe in recipes) {
            val recipeUser = usersRef.document(recipe.id.substringAfter("_").substringBefore("_"))
              .get().await().toObject(UserResponse::class.java)
            recipeUser?.let {
              val feedItem = FeedItem.RecipeItem(UserResponse.transform(it), RecipeResponse.transform(recipe))
              if (itemIdsSet.add(recipe.id)) { // Add only if the ID is unique
                feedItems.add(feedItem)
              }
            }
          }
        }

        // Sort following users' posts and recipes by date
        feedItems.sortByDescending {
          when (it) {
            is FeedItem.PostItem -> it.post.date
            is FeedItem.RecipeItem -> it.recipe.date
          }
        }
      }

      val lastVisibleSnapshot =
        lastPostSnapshots.values.lastOrNull() ?: lastRecipeSnapshots.values.lastOrNull()
      val nextPage = lastVisibleSnapshot?.let {
        postsRef.startAfter(it).limit(params.loadSize.toLong()).get().await()
      }

      if (nextPage == null) {
        val fallbackItems = fetchPostsAndRecipes(params.loadSize)
        for (fallbackItem in fallbackItems) {
          val id = when (fallbackItem) {
            is FeedItem.PostItem -> fallbackItem.post.id
            is FeedItem.RecipeItem -> fallbackItem.recipe.id
          }
          if (itemIdsSet.add(id)) { // Add only if the ID is unique
            feedItems.add(fallbackItem)
          }
        }
      }

      LoadResult.Page(
        data = feedItems,
        prevKey = null, // No backward pagination
        nextKey = nextPage
      )
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }
  override fun getRefreshKey(state: PagingState<QuerySnapshot, FeedItem>): QuerySnapshot? {
    return state.anchorPosition?.let { anchorPosition ->
      state.closestPageToPosition(anchorPosition)?.nextKey
    }
  }

  private fun getQueryForPosts(userId: String, limit: Int): Query {
    var postsQuery = postsRef.whereEqualTo("post_user_id", userId)

    postsQuery
      .orderBy("post_date", Query.Direction.DESCENDING)
      .limit(limit.toLong())

    lastPostSnapshots[userId]?.let { lastSnapshot ->
      postsQuery = postsQuery.startAfter(lastSnapshot)
    }

    return postsQuery
  }

  private fun getQueryForRecipes(userId: String, limit: Int): Query {
    var recipesQuery = recipesRef.whereEqualTo("recipe_user_id", userId)

    recipesQuery
      .orderBy("recipe_date", Query.Direction.DESCENDING)
      .limit(limit.toLong())

    lastRecipeSnapshots[userId]?.let { lastSnapshot ->
      recipesQuery = recipesQuery.startAfter(lastSnapshot)
    }

    return recipesQuery
  }
}