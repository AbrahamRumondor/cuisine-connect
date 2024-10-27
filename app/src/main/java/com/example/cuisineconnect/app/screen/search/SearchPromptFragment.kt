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
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.app.screen.collection.CollectionFragmentDirections
import com.example.cuisineconnect.databinding.FragmentSearchPromptBinding
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
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
    setAdapterButtons()

    return binding.root
  }

  private fun setAdapterButtons() {
    searchPromptAdapter.setItemListener(object : OnClickItemListener {

      override fun onUserClicked(user: User) {
        val action =
          SearchPromptFragmentDirections.actionSearchPromptFragmentToOtherProfileFragment(user.id)
        findNavController().navigate(action)
      }

      override fun onPromptClicked(prompt: String) {
        val currentText = binding.etSearch.text.toString().trim()

        val updatedText = if (currentText.isEmpty()) {
          prompt // If no text, just add the prompt
        } else {
          "$currentText $prompt" // Append prompt to the existing text with a space
        }

        binding.etSearch.setText(updatedText)
        binding.etSearch.setSelection(updatedText.length) // Move cursor to the end of the text

        searchPromptViewModel.updatePrompt(updatedText)
      }
    })
  }


  private fun searchBarWatcher() {
    binding.ivSearchIcon.setImageResource(R.drawable.ic_bnv_search)
    binding.ivSearchIcon.setOnClickListener {
      binding.etSearch.setText("") // Clears the text
    }

    // Watch for changes in the search bar text
    binding.etSearch.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No action needed here
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // No action needed here
      }

      override fun afterTextChanged(s: Editable?) {
        s?.let { query ->
          // Toggle icon based on text presence
          if (query.isEmpty()) {
            binding.ivSearchIcon.setImageResource(R.drawable.ic_bnv_search)
          } else {
            binding.ivSearchIcon.setImageResource(R.drawable.ic_close)
          }
          searchPromptViewModel.updatePrompt(query.toString()) // Update ViewModel with query
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
    binding.etSearch.requestFocus()

    binding.etSearch.post {
      val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
      imm?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    // Set an OnEditorActionListener to detect when the "Enter" key is pressed
    binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
      // Check if the action is equivalent to pressing the "Enter" key
      if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
        actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
        actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO
      ) {

        // Get the current text in the search bar
        val query = binding.etSearch.text.toString()

        if (query.isNotEmpty()) {
          // Navigate to the SearchResultFragment with the search query
          val action =
            SearchPromptFragmentDirections.actionSearchPromptFragmentToSearchResultFragment(query)
          findNavController().navigate(action)
        }
        true // Return true to consume the action
      } else {
        false // Return false to let other actions process
      }
    }
  }
}