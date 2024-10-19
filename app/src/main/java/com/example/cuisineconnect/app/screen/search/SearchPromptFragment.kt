package com.example.cuisineconnect.app.screen.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cuisineconnect.databinding.FragmentSearchPromptBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchPromptFragment : Fragment() {

  private lateinit var binding: FragmentSearchPromptBinding
  private val searchPromptViewModel: SearchPromptViewModel by viewModels()
  private val searchPromptAdapter by lazy { SearchPromptAdapter() }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSearchPromptBinding.inflate(inflater, container, false)

    setupToolbar()
    focusSearchBar()
    searchBarWatcher()

    binding.list.adapter = searchPromptAdapter
    observeSearchResults() // Observe combined search results

    return binding.root
  }

  private fun searchBarWatcher() {
    binding.etSearch.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No action needed
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // No action needed
      }

      override fun afterTextChanged(s: Editable?) {
        s?.let { query ->
          searchPromptViewModel.updatePrompt(query.toString()) // Update prompt in ViewModel
        }
      }
    })
  }

  private fun observeSearchResults() {
    lifecycleScope.launch {
      searchPromptViewModel.searchResults.collectLatest { results ->
        searchPromptAdapter.submitItems(results.toMutableList())
      }
    }
  }

  private fun setupToolbar() {
    (activity as? AppCompatActivity)?.apply {
      setSupportActionBar(binding.toolbar)
      supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setDisplayShowHomeEnabled(true)
      }
    }

    // Handle back button click
    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }
  }

  private fun focusSearchBar() {
    // Request focus on the EditText
    binding.etSearch.requestFocus()

    // Show the keyboard programmatically
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
  }
}