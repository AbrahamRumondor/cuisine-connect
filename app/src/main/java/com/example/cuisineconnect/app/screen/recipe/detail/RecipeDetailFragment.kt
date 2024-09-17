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
import com.example.cuisineconnect.databinding.FragmentRecipeDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
    Toast.makeText(this.context, recipeId ?: "NONE", Toast.LENGTH_SHORT).show()

    fetchUserNRecipe(recipeId)

    populateDetailAdapter()

    return binding.root
  }

  private fun populateDetailAdapter() {
    lifecycleScope.launch {
      recipeDetailViewModel.recipe.collectLatest { recipe ->
        recipeDetailViewModel.user.collectLatest { user ->
          recipeDetailViewModel.steps.collectLatest { steps ->
            binding.rvRecipeDetail.adapter = recipeAdapter

            recipeAdapter.submitRecipeParts(
              recipeDetailViewModel.makeDetailRecipe(
                recipe = recipe,
                user = user,
                ingredients = recipe?.ingredients,
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