package com.example.cuisineconnect.app.screen.profile.post

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
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.collection.SavedRecipeFragment.Companion.ARG_COLUMN_COUNT
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.profile.OtherProfileFragmentDirections
import com.example.cuisineconnect.app.screen.profile.ProfileFragmentDirections
import com.example.cuisineconnect.databinding.FragmentProfilePostBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtherProfilePostFragment : Fragment() {

  private var columnCount = 1
  private var userId: String? = null

  private lateinit var binding: FragmentProfilePostBinding
  private val otherProfilePostViewModel: OtherProfilePostViewModel by viewModels()
  private val createPostViewModel: CreatePostViewModel by viewModels()

  private val profilePostAdapter by lazy { ProfilePostAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    userId = arguments?.getString("userId")

    userId?.let {
      otherProfilePostViewModel.getUser(it)
    }

    arguments?.let {
      columnCount = it.getInt(ARG_COLUMN_COUNT)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentProfilePostBinding.inflate(inflater, container, false)

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
      otherProfilePostViewModel.getPostNRecipeOfUser()
      otherProfilePostViewModel.list.collectLatest {
        if (it != null) {
          profilePostAdapter.submitPostNRecipeParts(it.toMutableList())
          binding.root.isRefreshing = false
        }
      }
    }
  }

  private fun setupAdapter() {
    binding.list.adapter = profilePostAdapter

    lifecycleScope.launch {
      otherProfilePostViewModel.list.collectLatest { list ->
        if (list != null) {
          profilePostAdapter.submitPostNRecipeParts(list.toMutableList())
          profilePostAdapter.addViewModel(createPostViewModel)
        }
      }
    }

    profilePostAdapter.setItemListener(object : RecipeListListener {
      override fun onRecipeClicked(recipeId: String) {
        Log.d("aahdfkfj", "masuk")
        val action =
          OtherProfileFragmentDirections.actionOtherProfileFragmentToRecipeDetailFragment(recipeId)
        findNavController().navigate(action)
      }

      override fun onRecipeLongClicked(recipeId: String) {
        onRecipeLongClickSelected(recipeId)
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

  override fun onResume() {
    super.onResume()
    userId?.let {
      otherProfilePostViewModel.getUser(it)
    }
  }
}