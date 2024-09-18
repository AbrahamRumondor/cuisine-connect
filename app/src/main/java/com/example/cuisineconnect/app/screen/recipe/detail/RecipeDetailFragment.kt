package com.example.cuisineconnect.app.screen.recipe.detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentRecipeDetailBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeDetailFragment : Fragment() {

  val args: RecipeDetailFragmentArgs by navArgs()

  private lateinit var binding: FragmentRecipeDetailBinding
  private val recipeDetailViewModel: RecipeDetailViewModel by viewModels()
  private val recipeAdapter by lazy { RecipeDetailAdapter() }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)

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
          tvLikeCount.text = recipe.upvotes.size.toString()

          setupUpvoteButton(recipe, user, recipeId)

          if (recipeId.contains(user.id)) {
            ivIcBookmark.visibility = View.INVISIBLE
            tvBookmarkCount.visibility = View.INVISIBLE
          } else {
            ivIcBookmark.visibility = View.VISIBLE
            tvBookmarkCount.visibility = View.VISIBLE
            ivIcBookmark.setOnClickListener {
              // Handle bookmark action here
            }
          }

          ivIcReply.setOnClickListener {
            // Handle reply action here
          }
        }
      }
    }
  }

  private fun setupUpvoteButton(recipe: Recipe, user: User, recipeId: String) {
    binding.ivIcThumbs.apply {
      Log.d("brobruhbruh", recipe.toString())
      val isUpvoted = recipe.upvotes[user.id] == true
      setImageResource(
        if (isUpvoted) R.drawable.ic_thumbs_up_solid else R.drawable.ic_thumbs_up_regular
      )

      setOnClickListener {
        if (isUpvoted) {
          recipeDetailViewModel.downVoteRecipe(recipeId, user.id)
          Toast.makeText(this.context, "DOWNVOTED", Toast.LENGTH_SHORT).show()
        } else {
          recipeDetailViewModel.upvoteRecipe(recipeId, user.id)
          Toast.makeText(this.context, "UPVOTED", Toast.LENGTH_SHORT).show()
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
        // Ensure all necessary data is available before populating the adapter
        if (recipe != null && user != null && steps != null) {
          // Populate the adapter only if it hasn't been set already
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

    val userId = recipeId.substringBefore("_")
    recipeDetailViewModel.fetchUser(userId)
    recipeDetailViewModel.fetchRecipe(recipeId)
    recipeDetailViewModel.fetchSteps(recipeId)
  }
}