package com.example.cuisineconnect.app.screen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.app.screen.recipe.detail.RecipeDetailFragmentDirections
import com.example.cuisineconnect.app.screen.search.trending.TrendingHashtagAdapter
import com.example.cuisineconnect.app.screen.search.trending.TrendingViewModel
import com.example.cuisineconnect.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

  private lateinit var binding: FragmentSearchBinding
  private val trendingViewModel: TrendingViewModel by viewModels()
  private val hashtagAdapter by lazy { TrendingHashtagAdapter() }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSearchBinding.inflate(inflater, container, false)

    // Initialize and set up the HashtagAdapter
    binding.list.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = hashtagAdapter
    }

    setupAdapterListener()

    // Set up the click listener for the search bar
    binding.clSearchBar.setOnClickListener {
      val action = SearchFragmentDirections.actionSearchFragmentToSearchPromptFragment(null)
      findNavController().navigate(action)
    }

    binding.srlHome.setOnRefreshListener {
      refreshHashtagData()
    }

    loadHashtagData()

    return binding.root
  }

  private fun loadHashtagData() {
    trendingViewModel.fetchTrendingHashtags { hashtags, error ->
      if (error == null) {
        hashtagAdapter.submitData(hashtags)
      } else {
        Toast.makeText(
          context,
          "Error fetching trending hashtags: ${error.message}",
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  private fun refreshHashtagData() {
    trendingViewModel.fetchTrendingHashtags { hashtags, error ->
      if (error == null) {
        hashtagAdapter.submitData(hashtags)
      } else {
        Toast.makeText(
          context,
          "Error fetching trending hashtags: ${error.message}",
          Toast.LENGTH_SHORT
        ).show()
      }
      // Hide the refresh indicator once the data is loaded
      binding.srlHome.isRefreshing = false
    }
  }

  fun setupAdapterListener() {
    hashtagAdapter.setItemListener(object : OnClickItemListener {
      override fun onPromptClicked(prompt: String) {
        val action =
          SearchFragmentDirections.actionSearchFragmentToSearchResultFragment(
            prompt
          )
        findNavController().navigate(action)
      }
    })
  }
}