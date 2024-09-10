package com.example.cuisineconnect.app.screen.collection

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.MainActivityViewModel
import com.example.cuisineconnect.databinding.FragmentMyRecipeBinding
import com.example.cuisineconnect.databinding.FragmentMyRecipeListBinding
import com.example.cuisineconnect.domain.model.Recipe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
@AndroidEntryPoint
class MyRecipeFragment : Fragment() {

  private var columnCount = 1

  private lateinit var binding: FragmentMyRecipeListBinding
  private val collectionViewModel: CollectionViewModel by viewModels()

  private val recipeAdapter by lazy { MyRecipeRecyclerViewAdapter(binding) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      columnCount = it.getInt(ARG_COLUMN_COUNT)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentMyRecipeListBinding.inflate(inflater, container, false)

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

    binding.root.setOnRefreshListener {
      refreshContent()
    }

    return binding.root
  }

  private fun refreshContent() {
    lifecycleScope.launch {
      collectionViewModel.recipes.collectLatest {
        if (it != null) {
          recipeAdapter.updateData(it)
        }
      }
    }

    binding.root.isRefreshing = false
  }

  private fun setupAdapter() {
    binding.list.adapter = recipeAdapter

    lifecycleScope.launch {
      collectionViewModel.recipes.collectLatest {
        if (it != null) {
          recipeAdapter.updateData(it)
        }
      }
    }

  }

  companion object {

    // TODO: Customize parameter argument names
    const val ARG_COLUMN_COUNT = "column-count"

    // TODO: Customize parameter initialization
    @JvmStatic
    fun newInstance(columnCount: Int) =
      MyRecipeFragment().apply {
        arguments = Bundle().apply {
          putInt(ARG_COLUMN_COUNT, columnCount)
        }
      }
  }
}