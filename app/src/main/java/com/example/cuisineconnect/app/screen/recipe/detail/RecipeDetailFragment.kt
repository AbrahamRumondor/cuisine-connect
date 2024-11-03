package com.example.cuisineconnect.app.screen.recipe.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.databinding.FragmentRecipeDetailBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeDetailFragment : Fragment() {

  private val args: RecipeDetailFragmentArgs by navArgs()

  private lateinit var binding: FragmentRecipeDetailBinding
  private val recipeDetailViewModel: RecipeDetailViewModel by viewModels()
  private val recipeAdapter by lazy { RecipeDetailAdapter() }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    binding.btnBack.setOnClickListener {
      activity?.supportFragmentManager?.popBackStack()
    }

    val recipeId = args.recipeId

    Log.d("brobruh", recipeId ?: "NONE")
//    Toast.makeText(this.context, recipeId ?: "NONE", Toast.LENGTH_SHORT).show()

    fetchUserNRecipe(recipeId)

    populateDetailAdapter()

    setupBottomButtons(recipeId)

    return binding.root
  }

  private fun setupBottomButtons(recipeId: String?) {
    if (recipeId == null) return // Return early if recipeId is null

    lifecycleScope.launch {
      // Combine recipe and currentUser into a single flow to avoid nesting
      combine(
        recipeDetailViewModel.recipe,
        recipeDetailViewModel.currentUser
      ) { recipe, user ->
        recipe to user
      }.collectLatest { (recipe, user) ->
        if (recipe == null || user == null) return@collectLatest

        binding.run {
          tvUpvoteCount.text = recipe.upvotes.size.toString()
          tvReplyCount.text = recipe.replyCount.toString()

          currentUser?.let {
            setupUpvoteButton(recipe, it, recipeId)

            if (recipeId.contains(it.id)) {
              ivIcBookmark.visibility = View.INVISIBLE
              tvBookmarkCount.visibility = View.INVISIBLE
            } else {
              ivIcBookmark.visibility = View.VISIBLE
              tvBookmarkCount.visibility = View.VISIBLE
              tvBookmarkCount.text = recipe.bookmarks.size.toString()
            }
          }

          llReply.setOnClickListener {
            val action =
              RecipeDetailFragmentDirections.actionRecipeDetailFragmentToReplyRecipeFragment(
                recipeId
              )
            findNavController().navigate(action)
          }

          recipeDetailViewModel.updateTrendingCounter(
            recipe.hashtags,
            recipe.id
          )
        }
      }
    }
  }

  private fun populateDetailAdapter() {
    lifecycleScope.launch {
      // Combine the recipe, user, and steps into a single flow
      combine(
        recipeDetailViewModel.recipe,
        recipeDetailViewModel.user,
        recipeDetailViewModel.steps
      ) { recipe, user, steps ->
        Triple(recipe, user, steps)
      }.collectLatest { (recipe, user, steps) ->
        if (recipe != null && user != null && steps != null) {
          if (binding.rvRecipeDetail.adapter == null) {
            binding.rvRecipeDetail.adapter = recipeAdapter

            recipeAdapter.submitRecipeParts(
              recipeDetailViewModel.makeDetailRecipe(
                recipe = recipe,
                user = user,
                ingredients = recipe.ingredients,
                steps = steps
              ) ?: mutableListOf()
            )
          }
        }
      }
    }
  }

  private fun fetchUserNRecipe(recipeId: String?) {
    if (recipeId.isNullOrEmpty()) return

    val userId = recipeId.substringAfter("_").substringBefore("_")
    recipeDetailViewModel.fetchUser(userId)
    recipeDetailViewModel.fetchRecipe(recipeId)
    recipeDetailViewModel.fetchSteps(recipeId)
  }

  private fun setupUpvoteButton(recipe: Recipe, user: User, recipeId: String) {
    val isUpvoted = recipe.upvotes[user.id] == true
    val isBookmarked = recipe.bookmarks[user.id] == true

    updateUpvoteIcon(isUpvoted)
    updateBookmarkIcon(isBookmarked)


    binding.llUpvote.setOnClickListener {
      if (isUpvoted) {
        handleDownvote(recipeId, user.id)
      } else {
        handleUpvote(recipeId, user.id)
      }
    }

    binding.llBookmark.setOnClickListener {
      if (isBookmarked) {
        handleRemoveBookmark(recipe.id, user.id)
      } else {
        handleAddBookmark(recipe.id, user.id)
      }
    }
  }

  private fun updateUpvoteIcon(isUpvoted: Boolean) {
    binding.ivIcThumbs.setImageResource(
      if (isUpvoted) R.drawable.ic_thumbs_up_solid else R.drawable.ic_thumbs_up_regular
    )
  }

  private fun handleUpvote(recipeId: String, userId: String) {
    recipeDetailViewModel.upvoteRecipe(recipeId, userId)
    showToast("UPVOTED")
  }

  private fun handleDownvote(recipeId: String, userId: String) {
    recipeDetailViewModel.downVoteRecipe(recipeId, userId)
    showToast("DOWNVOTED")
  }

  private fun showToast(message: String) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
  }

  private fun updateBookmarkIcon(isBookmarked: Boolean) {
    binding.ivIcBookmark.setImageResource(
      if (isBookmarked) R.drawable.ic_bookmark_solid else R.drawable.ic_bookmark_regular
    )
  }

  private fun handleAddBookmark(postId: String, userId: String) {
    recipeDetailViewModel.addToBookmark(postId, userId)
    showToast("Added to bookmarks")
  }

  private fun handleRemoveBookmark(postId: String, userId: String) {
    recipeDetailViewModel.removeFromBookmark(postId, userId)
    showToast("Removed from bookmarks")
  }

  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
      binding.inclInternet.root.visibility = View.VISIBLE
      binding.ablToolbar.visibility = View.GONE
      binding.clRvRecipeDetail.visibility = View.GONE
      binding.cvBottomDetail.visibility = View.GONE
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
      binding.inclInternet.root.visibility = View.GONE
      binding.ablToolbar.visibility = View.VISIBLE
      binding.clRvRecipeDetail.visibility = View.VISIBLE
      binding.cvBottomDetail.visibility = View.VISIBLE
    }
  }

}