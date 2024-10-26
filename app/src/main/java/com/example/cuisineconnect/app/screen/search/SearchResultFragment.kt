package com.example.cuisineconnect.app.screen.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.map
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.home.HomeAdapter
import com.example.cuisineconnect.databinding.FragmentSearchResultBinding
import dagger.hilt.android.AndroidEntryPoint
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

    val query = args.query

    setupRecyclerView()
    observeData(query)

    binding.clSearchBar.setOnClickListener {
      findNavController().popBackStack()
    }

    binding.tvSearch.text = query

    return binding.root
  }

  private fun setupRecyclerView() {
    binding.list.adapter = adapter  // Assuming `list` is the RecyclerView ID in `FragmentSearchResultBinding`

    adapter.setItemListener(object : RecipeListListener {
      override fun onRecipeClicked(recipeId: String) {
//        val action = SearchFragmentDirections.actionSearchResultFragmentToRecipeDetailFragment(recipeId)
//        findNavController().navigate(action)
      }

      override fun onRecipeLongClicked(recipeId: String) {
        // Implement any specific behavior for long click, if required
      }

      override fun onItemDeleteClicked(itemId: String, type: String) {

      }
    })
  }

  private fun observeData(query: String) {
    val formatQuery = formatSearchQuery(query)
    searchResultViewModel.updateSearchQuery(formatQuery)

    lifecycleScope.launch {
      searchResultViewModel.postsNRecipesList.collectLatest {
        it.map { item ->

        Log.d("searchResultFragment", item.toString())

        }
        adapter.submitData(it)
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
}