package com.example.cuisineconnect.app.screen.profile.recipe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.screen.collection.SavedRecipeFragment.Companion.ARG_COLUMN_COUNT
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.home.HomeFragmentDirections
import com.example.cuisineconnect.app.screen.profile.ProfileFragmentDirections
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.databinding.FragmentProfileRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileRecipeFragment : Fragment() {

  private var columnCount = 1

  private lateinit var binding: FragmentProfileRecipeBinding
  private val profileRecipeViewModel: ProfileRecipeViewModel by viewModels()
  private val createPostViewModel: CreatePostViewModel by viewModels()

  private val profileRecipeAdapter by lazy { ProfileRecipeAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    profileRecipeViewModel.getRecipeOfUser()

    arguments?.let {
      columnCount = it.getInt(ARG_COLUMN_COUNT)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentProfileRecipeBinding.inflate(inflater, container, false)

    loadingAnimation()

    if (binding.list is RecyclerView) {
      with(binding.list) {
        layoutManager = when {
          columnCount <= 1 -> LinearLayoutManager(context)
          else -> GridLayoutManager(context, columnCount)
        }

        setupAdapter()
      }
    }

    binding.root.isNestedScrollingEnabled = true

    binding.srlMyRecipe.setOnRefreshListener {
      refreshContent()
    }

    return binding.root
  }

  private fun refreshContent() {
    lifecycleScope.launch {
      profileRecipeViewModel.getRecipeOfUser()
      profileRecipeViewModel.list.collectLatest {
        if (it != null) {
          profileRecipeAdapter.submitRecipeList(it.toMutableList())
          binding.srlMyRecipe.isRefreshing = false
        }
      }
    }
  }

  private fun setupAdapter() {
    binding.list.adapter = profileRecipeAdapter

    lifecycleScope.launch {
      profileRecipeViewModel.list.collectLatest { list ->
        if (list == null) {
          return@collectLatest
        }
        if (list.isEmpty()) {
          hideLoadingAnimation()
          binding.ivEmptyState.visibility = View.VISIBLE
          binding.tvEmptyState.visibility = View.VISIBLE
          return@collectLatest
        }
        binding.ivEmptyState.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        profileRecipeAdapter.submitRecipeList(list.toMutableList())
        profileRecipeAdapter.addViewModel(createPostViewModel)
      }
    }

    profileRecipeAdapter.setItemListener(object : ItemListListener {
      override fun onRecipeClicked(recipeId: String) {
        Log.d("aahdfkfj", "masuk")
        val action =
          ProfileFragmentDirections.actionProfileFragmentToRecipeDetailFragment(recipeId)
        findNavController().navigate(action)
      }

      override fun onRecipeLongClicked(recipeId: String) {
        onRecipeLongClickSelected(recipeId)
      }

      override fun onItemDeleteClicked(itemId: String, type: String) {
        when (type) {
          "recipe" -> profileRecipeViewModel.deleteRecipe(itemId)
        }
        profileRecipeAdapter.removeRecipe(itemId)
      }
    })
  }

  fun onRecipeLongClickSelected(recipeId: String) {
    // Pass the selected recipeId back to CreatePostFragment
    val bundle = Bundle().apply {
      putString("recipeId", recipeId)
    }
    requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundle)

    // Navigate back to CreatePostFragment
    navigateBackToCreatePostFragment()
  }

  private fun navigateBackToCreatePostFragment() {
    val navController = findNavController()

    val success = navController.popBackStack(R.id.createPostFragment, false)

    if (!success) {
      // If CreatePostFragment is not in the back stack, handle it appropriately
      // You may navigate back to a specific fragment or show an error message
    }
  }

  private fun loadingAnimation() {
    lifecycleScope.launch {
      profileRecipeAdapter.isPopulated.collectLatest {
        if (it) {
          hideLoadingAnimation()
        } else {
          showLoadingAnimation()
        }
      }
    }
  }

  private fun showLoadingAnimation() {
    binding.progressBar.setAnimation(R.raw.cc_loading) // Set the animation from res/raw
    binding.progressBar.repeatCount = LottieDrawable.INFINITE // Loop the animation infinitely
    binding.progressBar.playAnimation() // Start the animation
    binding.progressBar.visibility = View.VISIBLE
  }

  private fun hideLoadingAnimation() {
    binding.progressBar.cancelAnimation() // Stop the Lottie animation
    binding.progressBar.visibility = View.GONE
  }

  override fun onResume() {
    super.onResume()
    profileRecipeViewModel.getUser()
  }
}