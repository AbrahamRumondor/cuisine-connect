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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.app.screen.collection.CollectionFragmentDirections
import com.example.cuisineconnect.databinding.FragmentSearchPromptBinding
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchPromptFragment : Fragment() {

  private lateinit var binding: FragmentSearchPromptBinding
  private val searchPromptViewModel: SearchPromptViewModel by viewModels()
  private val searchPromptAdapter by lazy { SearchPromptAdapter() }

  private val args: SearchPromptFragmentArgs by navArgs()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSearchPromptBinding.inflate(inflater, container, false)

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    val query = args.query

    if (!query.isNullOrEmpty()) binding.etSearch.setText(query)

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

        // Check if the current text matches any of the specified phrases
        if (prompt in listOf(
            getString(R.string.no_hashtags_found),
            getString(R.string.no_users_found),
            getString(R.string.type_something_to_search)
          )
        ) {
          return
        }

        // Split the current text into words
        val words = currentText.split(" ").toMutableList()

        // Get the last word from the current text
        val lastWord = words.lastOrNull() ?: ""

        // Check if the last word matches the prompt exactly (case-insensitively)
        if (lastWord.equals(prompt, ignoreCase = true)) {
          // Clear the adapter items if the last word is the same as the prompt
          binding.etSearch.setText(currentText + " ") // Append a space
          binding.etSearch.setSelection(currentText.length + 1) // Move cursor to the end of the text
          searchPromptAdapter.submitItems(mutableListOf(""))
          return // Do nothing if the last word is the same as the prompt
        }

        // If the last word is a prefix of the prompt, replace it
        if (prompt.startsWith(lastWord, ignoreCase = true)) {
          // Replace the last word with the prompt
          words[words.size - 1] = prompt
        } else {
          // Append the prompt if the last word does not match
          words.add(prompt)
        }

        // Join the words back into a single string
        var updatedText = words.joinToString(" ")

        // Set the updated text to the EditText and move the cursor to the end
        updatedText += " "
        binding.etSearch.setText(updatedText) // Append a space
        binding.etSearch.setSelection(updatedText.length) // Move cursor to the end

        // Update the view model (if necessary)
        searchPromptAdapter.submitItems(mutableListOf(""))
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

          // Split the current text into words and get the last word
          val lastWord = query.toString().split(" ").lastOrNull() ?: ""

          // Update the ViewModel with the last word only
          searchPromptViewModel.updatePrompt(lastWord)
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