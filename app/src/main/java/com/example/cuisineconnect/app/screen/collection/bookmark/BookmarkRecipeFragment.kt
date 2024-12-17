package com.example.cuisineconnect.app.screen.collection.bookmark

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.collection.CollectionFragmentDirections
import com.example.cuisineconnect.app.screen.collection.CollectionViewModel
import com.example.cuisineconnect.app.util.Sort
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.databinding.FragmentMyRecipeListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
@AndroidEntryPoint
class BookmarkRecipeFragment : Fragment() {

  private var columnCount = 1

  private lateinit var binding: FragmentMyRecipeListBinding
  private val collectionViewModel: CollectionViewModel by viewModels()

  private val recipeAdapter by lazy { BookmarkRecipeRecyclerViewAdapter(binding) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    collectionViewModel.getBookmarkedRecipes()

    arguments?.let {
      columnCount = it.getInt(ARG_COLUMN_COUNT)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentMyRecipeListBinding.inflate(inflater, container, false)

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

//    binding.root.isNestedScrollingEnabled = true

    binding.srlMyRecipe.setOnRefreshListener {
      refreshContent()
    }

    setupSpinner()

    return binding.root
  }

  private fun setupSpinner() {
    val spinner: Spinner = binding.spinnerSortBy // Assuming you have the Spinner in your binding
    val sortingOptions = arrayOf("Latest", "Most Liked", "Oldest", "Least Liked")

    val adapter = ArrayAdapter(requireContext(), R.layout.spinner_collection_item, sortingOptions)
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_collection_item)
    spinner.adapter = adapter

    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        if (view != null) { // Check if view is not null
          val selectedOption = sortingOptions[position]
          val sortBy = when (selectedOption) {
            "Latest" -> Sort.Latest
            "Most Liked" -> Sort.MostLiked
            "Oldest" -> Sort.Oldest
            "Least Liked" -> Sort.LeastLiked

            else -> Sort.Latest // Default case
          }
          collectionViewModel.getBookmarkedRecipes(sortBy) // Call the function with the selected sorting option
        }
      }

      override fun onNothingSelected(parent: AdapterView<*>) {
        // Handle case when nothing is selected if needed
      }
    }
  }

  private fun refreshContent() {
    lifecycleScope.launch {
      collectionViewModel.getBookmarkedRecipes()
      binding.srlMyRecipe.isRefreshing = false
    }
  }

  private fun setupAdapter() {
    binding.list.adapter = recipeAdapter

    // Observe bookmarkedRecipes once in setupAdapter
    lifecycleScope.launch {
      collectionViewModel.bookmarkedRecipes.collectLatest { recipes ->
        if (recipes.isEmpty()) {
          recipeAdapter.updateData(recipes)
          hideLoadingAnimation()
          binding.ivEmptyState.visibility = View.VISIBLE
          binding.tvEmptyState.visibility = View.VISIBLE
          return@collectLatest
        }
        binding.ivEmptyState.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        Log.d("collectionViewModel", "INI ADAPTER: $recipes")
        recipeAdapter.updateData(recipes)
      }
    }

    binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      private var isSpinnerVisible = true

      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (dy > 0 && isSpinnerVisible) { // Scrolling up
          binding.spinnerSortBy.visibility = View.GONE
          isSpinnerVisible = false
        } else if (dy < 0 && !isSpinnerVisible) { // Scrolling down
          binding.spinnerSortBy.visibility = View.VISIBLE
          isSpinnerVisible = true
        }
      }
    })

    recipeAdapter.setItemListener(object : RecipeListListener {
      override fun onRecipeClicked(recipeId: String) {
        val action =
          CollectionFragmentDirections.actionCollectionFragmentToRecipeDetailFragment(recipeId)
        findNavController().navigate(action)
      }

      override fun onRecipeLongClicked(recipeId: String) {
        onRecipeSelected(recipeId)
      }

      override fun onItemDeleteClicked(itemId: String, type: String) {
        collectionViewModel.deleteRecipe(itemId)
        recipeAdapter.removeData(itemId)
      }

      override fun onUserProfileClicked(userId: String) {
        if (userId == currentUser?.id) {
          val action =
            CollectionFragmentDirections.actionCollectionFragmentToProfileFragment()
          findNavController().navigate(action)
          return
        } else {
          val action =
            CollectionFragmentDirections.actionCollectionFragmentToOtherProfileFragment(
              userId
            )
          findNavController().navigate(action)
        }
      }
    })
  }

  fun onRecipeSelected(recipeId: String) {
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
      recipeAdapter.isPopulated.collectLatest {
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
    collectionViewModel.getBookmarkedRecipes()
  }

  companion object {

    // TODO: Customize parameter argument names
    const val ARG_COLUMN_COUNT = "column-count"

    // TODO: Customize parameter initialization
    @JvmStatic
    fun newInstance(columnCount: Int) =
      BookmarkRecipeFragment().apply {
        arguments = Bundle().apply {
          putInt(ARG_COLUMN_COUNT, columnCount)
        }
      }
  }
}