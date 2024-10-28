package com.example.cuisineconnect.data.pagingSource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.data.response.UserResponse
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class SearchResultPagingSource(
  private val postsRef: CollectionReference,
  private val recipesRef: CollectionReference,
  private val usersRef: CollectionReference,
  private val hashtagRef: CollectionReference,
  private val searchQuery: Pair<List<String>?, String?> // Pair of hashtags and title
) : PagingSource<QuerySnapshot, FeedItem>() {

  private var lastPostSnapshot: DocumentSnapshot? = null
  private var lastRecipeSnapshot: DocumentSnapshot? = null

  override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, FeedItem> {
    return try {
      val pageSize = params.loadSize
      val feedItems = mutableListOf<FeedItem>()

      // Unpack the hashtags and title from the searchQuery
      val (hashtags, title) = searchQuery

      // Fetch common hashtag IDs
      val commonHashtagIds = hashtags?.let { fetchCommonHashtagIds(it) } ?: emptyList()
      Log.d("searchResultPagingSource", "commonHashtags:: $commonHashtagIds")

      // Create a filtered list of hashtags that start with 'p' or 'r'
      val filteredPostHashtags = commonHashtagIds.filter { it.startsWith("p_") }
      val filteredRecipeHashtags = commonHashtagIds.filter { it.startsWith("r_") }

      // Fetch posts based on filtered hashtag IDs (no title filtering for posts)
      if (title.isNullOrEmpty()) {
        for (postId in filteredPostHashtags) {
          Log.d("searchResultPagingSource", "postId:: $postId")
          val postSnapshot = postsRef.document(postId).get().await()
          val post = postSnapshot.toObject(PostResponse::class.java)

          post?.let {
            val postUser = usersRef.document(it.id.substringAfter("_").substringBefore("_")).get().await()
              .toObject(UserResponse::class.java)
            postUser?.let { user ->
              feedItems.add(
                FeedItem.PostItem(
                  UserResponse.transform(user),
                  PostResponse.transform(it)
                )
              )
            }
          }
        }
      }

      val uniqueRecipes = fetchUniqueRecipes(title, filteredRecipeHashtags, pageSize)

      // Add unique recipes to feedItems
      for (recipe in uniqueRecipes) {
        val recipeUser = usersRef.document(recipe.id.substringAfter("_").substringBefore("_")).get().await()
          .toObject(UserResponse::class.java)
        recipeUser?.let { user ->
          feedItems.add(
            FeedItem.RecipeItem(
              UserResponse.transform(user),
              RecipeResponse.transform(recipe)
            )
          )
        }
      }

      // Sort feed items by date
      feedItems.sortByDescending {
        when (it) {
          is FeedItem.PostItem -> it.post.date
          is FeedItem.RecipeItem -> it.recipe.date
        }
      }

      // Limit to 10 results after all operations
      val limitedFeedItems = feedItems.take(10)

      LoadResult.Page(
        data = limitedFeedItems,
        prevKey = null, // No backward pagination in this example
        nextKey = null // Ensure nextKey is null if no nextPage
      )

    } catch (e: Exception) {
      Log.d("searchResultPagingSource", "error:: $e")
      LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<QuerySnapshot, FeedItem>): QuerySnapshot? {
    return state.anchorPosition?.let { anchorPosition ->
      state.closestPageToPosition(anchorPosition)?.nextKey
    }
  }

  override val keyReuseSupported: Boolean = true // Enable key reuse

  private suspend fun fetchCommonHashtagIds(hashtags: List<String>): List<String> {
    // Return an empty list if no hashtags are provided
    if (hashtags.isEmpty()) return emptyList()

    // Fetch IDs for the first hashtag
    val firstHashtagIds = fetchHashtagIds(hashtags[0]).toSet()

    // Intersect with IDs for the rest of the hashtags
    var commonIds = firstHashtagIds

    for (hashtag in hashtags.drop(1)) {
      val currentIds = fetchHashtagIds(hashtag).toSet()
      commonIds = commonIds.intersect(currentIds) // Keep only common IDs
    }

    return commonIds.toList()
  }

  private suspend fun fetchHashtagIds(hashtag: String): List<String> {
    // Fetch the document with the specified hashtag
    val querySnapshot = hashtagRef.whereEqualTo("hashtag_body", hashtag).get().await()

    // Check if any document is returned and retrieve the list of IDs
    val document = querySnapshot.documents.firstOrNull()
    val ids = document?.get("hashtag_list_id") as? List<String> ?: emptyList()

    Log.d("searchResultPagingSource", "ids: $ids")
    return ids
  }

  // Function to fetch and deduplicate recipes
  private suspend fun fetchUniqueRecipes(
    title: String?,
    filteredRecipeHashtags: List<String>,
    pageSize: Int
  ): List<RecipeResponse> {
    // Create sets to hold recipes fetched by title and hashtags
    val recipesByTitle = mutableSetOf<RecipeResponse>()
    val recipesByHashtags = mutableSetOf<RecipeResponse>()

    // Fetch recipes by title if provided
    if (!title.isNullOrEmpty()) {
      val titleKeywords = title.split(" ").map { it.lowercase() }
      val titleRecipesQuery = recipesRef
        .whereArrayContainsAny("recipe_title_split", titleKeywords)
        .get()
        .await()

      recipesByTitle.addAll(titleRecipesQuery.toObjects(RecipeResponse::class.java))
    }

    // Fetch recipes by hashtags if any hashtags are provided
    if (filteredRecipeHashtags.isNotEmpty()) {
      for (recipeId in filteredRecipeHashtags) {
        val recipeSnapshot = recipesRef.document(recipeId).get().await()
        recipeSnapshot.toObject(RecipeResponse::class.java)?.let {
          recipesByHashtags.add(it)
        }
      }
    }

    // Determine the common recipes
    val commonRecipes = when {
      title.isNullOrEmpty() && filteredRecipeHashtags.isEmpty() -> emptySet() // No search parameters
      title.isNullOrEmpty() -> recipesByHashtags // Only hashtags provided
      filteredRecipeHashtags.isEmpty() -> recipesByTitle // Only title provided
      else -> recipesByTitle.intersect(recipesByHashtags) // Both provided
    }

    // Apply pagination limit after deduplication
    return commonRecipes.take(pageSize).toList()
  }
}

//class SearchResultPagingSource(
//  private val postsRef: CollectionReference,
//  private val recipesRef: CollectionReference,
//  private val usersRef: CollectionReference,
//  private val searchQuery: Pair<List<String>, String> // Pair of hashtags and title
//) : PagingSource<QuerySnapshot, FeedItem>() {
//
//  private var lastPostSnapshot: DocumentSnapshot? = null
//  private var lastRecipeSnapshot: DocumentSnapshot? = null
//
//  override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, FeedItem> {
//    return try {
//      val pageSize = params.loadSize
//      val feedItems = mutableListOf<FeedItem>()
//
//      // Unpack the hashtags and title from the searchQuery
//      val (hashtags, title) = searchQuery
//
//      // Fetch posts if hashtags are present
//      if (title.isBlank()) {
//        val postsQuery = getQueryForPosts(pageSize)
//        val postsResult = postsQuery.get().await()
//        val posts = postsResult.toObjects(PostResponse::class.java)
//        lastPostSnapshot = postsResult.documents.lastOrNull()
//
//        for (post in posts) {
//          val postUser = usersRef.document(post.id.substringBefore("_")).get().await()
//            .toObject(UserResponse::class.java)
//          postUser?.let {
//            feedItems.add(
//              FeedItem.PostItem(
//                UserResponse.transform(it),
//                PostResponse.transform(post)
//              )
//            )
//          }
//        }
//      }
//
//      val recipesQuery = getQueryForRecipes(pageSize, title)
//      val recipesResult = recipesQuery.get().await()
//      val recipes = recipesResult.toObjects(RecipeResponse::class.java)
//      lastRecipeSnapshot = recipesResult.documents.lastOrNull()
//
//      for (recipe in recipes) {
//        val recipeUser = usersRef.document(recipe.id.substringBefore("_")).get().await()
//          .toObject(UserResponse::class.java)
//        recipeUser?.let {
//          feedItems.add(
//            FeedItem.RecipeItem(
//              UserResponse.transform(it),
//              RecipeResponse.transform(recipe)
//            )
//          )
//        }
//      }
//
//      // Sort feed items by date
//      feedItems.sortByDescending {
//        when (it) {
//          is FeedItem.PostItem -> it.post.date
//          is FeedItem.RecipeItem -> it.recipe.date
//        }
//      }
//
//      // Determine the next page key
//      val nextPage = when {
//        lastPostSnapshot != null -> {
//          postsRef.orderBy("post_date", Query.Direction.DESCENDING)
//            .startAfter(lastPostSnapshot)
//            .limit(pageSize.toLong())
//            .get().await()
//        }
//
//        lastRecipeSnapshot != null -> {
//          recipesRef.orderBy("recipe_date", Query.Direction.DESCENDING)
//            .startAfter(lastRecipeSnapshot)
//            .limit(pageSize.toLong())
//            .get().await()
//        }
//
//        else -> null
//      }
//
//      Log.d("searchResultPagingSource", "feeds: $feedItems")
//
//      LoadResult.Page(
//        data = feedItems,
//        prevKey = null, // No backward pagination in this example
//        nextKey = nextPage // Ensure nextKey is null if no nextPage
//      )
//
//    } catch (e: Exception) {
//      Log.d("searchResultPagingSource", "error:: $e")
//      LoadResult.Error(e)
//    }
//  }
//
//  override fun getRefreshKey(state: PagingState<QuerySnapshot, FeedItem>): QuerySnapshot? {
//    return state.anchorPosition?.let { anchorPosition ->
//      state.closestPageToPosition(anchorPosition)?.nextKey
//    }
//  }
//
//  override val keyReuseSupported: Boolean = true // Enable key reuse
//
//  private fun getQueryForPosts(limit: Int): Query {
//    Log.d("searchResultPagingSource", "1")
//
//    var postsQuery = postsRef.orderBy("post_date", Query.Direction.DESCENDING).limit(limit.toLong())
//
//    Log.d("searchResultPagingSource", "2")
//
//    lastPostSnapshot?.let { lastSnapshot ->
//      postsQuery = postsQuery.startAfter(lastSnapshot)
//    }
//
//    return postsQuery
//  }
//
//  private fun getQueryForRecipes(limit: Int, title: String): Query {
//    Log.d("searchResultPagingSource", "4")
//    var recipesQuery =
//      recipesRef.orderBy("recipe_date", Query.Direction.DESCENDING).limit(limit.toLong())
//
//    // Split title string into keywords
//    val titleKeywords = title.split(" ").map { it.lowercase() }
//
//    Log.d("searchResultPagingSource", "5")
//
//    // If title keywords are not empty, use them for querying recipes
//    if (titleKeywords.isNotEmpty()) {
//      recipesQuery = recipesQuery.whereArrayContainsAny("recipe_title_split", titleKeywords)
//    }
//
//    Log.d("searchResultPagingSource", "6")
//
//    lastRecipeSnapshot?.let { lastSnapshot ->
//      recipesQuery = recipesQuery.startAfter(lastSnapshot)
//    }
//
//    return recipesQuery
//  }
//}