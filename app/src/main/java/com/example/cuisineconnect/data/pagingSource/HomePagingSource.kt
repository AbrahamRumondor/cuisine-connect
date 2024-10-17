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

class HomePagingSource(
  private val user: User,
  private val postsRef: CollectionReference,
  private val recipesRef: CollectionReference,
  private val usersRef: CollectionReference
) : PagingSource<QuerySnapshot, FeedItem>() {

  // Store snapshots separately for each user
  private val lastPostSnapshots = mutableMapOf<String, DocumentSnapshot?>()
  private val lastRecipeSnapshots = mutableMapOf<String, DocumentSnapshot?>()

  override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, FeedItem> {
    return try {
      val followingUsers = listOf(user.id).plus(user.following)
      val pageSize = params.loadSize / followingUsers.size // Divide page size across users
      val feedItems = mutableListOf<FeedItem>()

      // For each following user, fetch posts and recipes
      for (userId in followingUsers) {
        // Fetch posts for the user
        val postsQuery = getQueryForPosts(userId, pageSize)
        val postsResult = postsQuery.get().await()
        val posts = postsResult.toObjects(PostResponse::class.java)

        // Update last snapshot for posts
        lastPostSnapshots[userId] = postsResult.documents.lastOrNull()

        for (post in posts) {
          val postUser = usersRef.document(post.id.substringBefore("_")).get().await()
            .toObject(UserResponse::class.java)
          postUser?.let {
            Log.d("borborbor", "gas")
            feedItems.add(
              FeedItem.PostItem(
                UserResponse.transform(it),
                PostResponse.transform(post)
              )
            )
          }
        }

        // Fetch recipes for the user
        val recipesQuery = getQueryForRecipes(userId, pageSize)
        val recipesResult = recipesQuery.get().await()
        val recipes = recipesResult.toObjects(RecipeResponse::class.java)

        // Update last snapshot for recipes
        lastRecipeSnapshots[userId] = recipesResult.documents.lastOrNull()

        for (recipe in recipes) {
          val recipeUser = usersRef.document(recipe.id.substringBefore("_")).get().await()
            .toObject(UserResponse::class.java)
          recipeUser?.let {
            feedItems.add(
              FeedItem.RecipeItem(
                UserResponse.transform(it),
                RecipeResponse.transform(recipe)
              )
            )
          }
        }
      }

      // Sort feed items by date
      feedItems.sortByDescending {
        when (it) {
          is FeedItem.PostItem -> it.post.date
          is FeedItem.RecipeItem -> it.recipe.date
        }
      }

      // Determine next page
      val lastVisibleSnapshot =
        lastPostSnapshots.values.lastOrNull() ?: lastRecipeSnapshots.values.lastOrNull()
      val nextPage = lastVisibleSnapshot?.let {
        postsRef.startAfter(it).limit(pageSize.toLong()).get().await()
      }

      LoadResult.Page(
        data = feedItems,
        prevKey = null, // No backward pagination in this example
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