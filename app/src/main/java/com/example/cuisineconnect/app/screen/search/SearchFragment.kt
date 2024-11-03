package com.example.cuisineconnect.app.screen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.app.screen.recipe.detail.RecipeDetailFragmentDirections
import com.example.cuisineconnect.app.screen.search.trending.TrendingHashtagAdapter
import com.example.cuisineconnect.app.screen.search.trending.TrendingViewModel
import com.example.cuisineconnect.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

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

}