package com.example.cuisineconnect.app.screen.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.paging.map
import com.airbnb.lottie.LottieDrawable
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.home.HomeAdapter
import com.example.cuisineconnect.app.screen.recipe.detail.RecipeDetailFragmentDirections
import com.example.cuisineconnect.databinding.FragmentSearchResultBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchResultFragment : Fragment() {

  private lateinit var binding: FragmentSearchResultBinding
  private val searchResultViewModel: SearchResultViewModel by activityViewModels()
  private val adapter by lazy { HomeAdapter() }
  private val args: SearchResultFragmentArgs by navArgs()


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSearchResultBinding.inflate(inflater, container, false)

    loadingAnimation()

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

    binding.inclFab.fabOpenOptions.setOnClickListener {
      toggleFabOptions()
    }

    val query = args.query

    setupRecyclerView()
    observeData(query)

    binding.clSearchBar.setOnClickListener {
      val navController = findNavController()

      // Check if SearchPromptFragment is in the back stack
      val hasSearchPromptFragment = navController.previousBackStackEntry?.destination?.id == R.id.searchPromptFragment

      if (!hasSearchPromptFragment) {
        val action =
          SearchResultFragmentDirections.actionSearchResultFragmentToSearchPromptFragment(
            query
          )
        navController.navigate(action)
      } else {
        navController.popBackStack()
      }
    }

    binding.tvSearch.text = query

    return binding.root
  }

  private fun setupRecyclerView() {
    binding.list.adapter = adapter  // Assuming `list` is the RecyclerView ID in `FragmentSearchResultBinding`

    adapter.setItemListener(object : ItemListListener {
      override fun onRecipeClicked(recipeId: String) {
        val action = SearchResultFragmentDirections.actionSearchResultFragmentToRecipeDetailFragment(recipeId)
        findNavController().navigate(action)
      }

      override fun onRecipeLongClicked(recipeId: String) {
        onRecipeLongClickSelected(recipeId)
      }

      override fun onItemDeleteClicked(itemId: String, type: String) {

      }
    })
  }

  private fun observeData(query: String) {
    val formatQuery = formatSearchQuery(query)
    searchResultViewModel.updateSearchQuery(formatQuery)

    lifecycleScope.launch {
      searchResultViewModel.postsNRecipesList.collectLatest { pagingData ->
        adapter.submitData(pagingData) // Submit data to the adapter
      }
    }

    adapter.addLoadStateListener { loadStates ->
      when {
        loadStates.refresh is LoadState.NotLoading && adapter.itemCount == 0 -> {
          // Show empty state if no items loaded
          hideLoadingAnimation()
          binding.list.visibility = View.VISIBLE
          binding.llEmptyState.visibility = View.VISIBLE
        }
        loadStates.refresh is LoadState.Loading -> {
          // Show loading animation while loading
          binding.list.visibility = View.GONE
          showLoadingAnimation()
        }
        else -> {
          // Hide empty state and loading animation
          hideLoadingAnimation()
          binding.list.visibility = View.VISIBLE
          binding.llEmptyState.visibility = View.GONE
        }
      }
    }
  }
  private fun formatSearchQuery(searchQuery: String): Pair<List<String>, String> {
    val splitQueries = searchQuery.split(" ")
    val hashtags = mutableListOf<String>()
    val titles = mutableListOf<String>()

    // Iterate through the split queries to categorize them
    for (query in splitQueries) {
      if (query.startsWith("#")) {
        hashtags.add(query) // Add the hashtag as is
      } else {
        titles.add(query) // Add title keywords
      }
    }

    // Join the titles into a single string with spaces
    val titleString = titles.joinToString(" ")

    return Pair(hashtags, titleString) // Return the Pair of hashtags and joined title string
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

  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
//      binding.inclInternet.root.visibility = View.VISIBLE
//      binding.appbarLayout.visibility = View.GONE
//      binding.srlHome.visibility = View.GONE
//      binding.inclFab.root.visibility = View.GONE
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
//      binding.inclInternet.root.visibility = View.GONE
//      binding.appbarLayout.visibility = View.VISIBLE
//      binding.srlHome.visibility = View.VISIBLE
//      binding.inclFab.root.visibility = View.VISIBLE
    }
  }

  private fun loadingAnimation() {
    lifecycleScope.launch {
      adapter.isPopulated.collectLatest {
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

  private fun toggleFabOptions() {
    with(binding.inclFab) {
      if (fabCreatePost.visibility == View.GONE && fabCreateRecipe.visibility == View.GONE) {
        fabCreatePost.visibility = View.VISIBLE
        fabCreateRecipe.visibility = View.VISIBLE
        fabOpenOptions.setImageResource(R.drawable.ic_close)

        fabCreateRecipe.setOnClickListener {
          findNavController().navigate(R.id.action_homeFragment_to_createRecipeFragment)
        }
        fabCreatePost.setOnClickListener {
          findNavController().navigate(R.id.action_homeFragment_to_createPostFragment)
        }
      } else {
        fabCreatePost.visibility = View.GONE
        fabCreateRecipe.visibility = View.GONE
        fabOpenOptions.setImageResource(R.drawable.ic_create_recipe)
      }
    }
  }
}