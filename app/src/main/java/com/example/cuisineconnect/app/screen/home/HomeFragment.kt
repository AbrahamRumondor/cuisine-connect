package com.example.cuisineconnect.app.screen.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.MainActivityViewModel
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.authentication.LoginActivity
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.profile.ProfileFragmentDirections
import com.example.cuisineconnect.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

  private lateinit var binding: FragmentHomeBinding
  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
  private val createPostViewModel: CreatePostViewModel by viewModels()
  private val adapter by lazy { HomeAdapter() }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentHomeBinding.inflate(inflater, container, false)

    setupView()
    loadData()

    binding.run {
      inclFab.fabOpenOptions.setOnClickListener {
        if (inclFab.fabCreatePost.visibility == View.GONE && inclFab.fabCreateRecipe.visibility == View.GONE) {
          inclFab.fabCreatePost.visibility = View.VISIBLE
          inclFab.fabCreateRecipe.visibility = View.VISIBLE
          inclFab.fabOpenOptions.setImageResource(
            R.drawable.ic_close
          )

          inclFab.fabCreateRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createRecipeFragment)
          }
          inclFab.fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createPostFragment)
          }
        } else {
          inclFab.fabCreatePost.visibility = View.GONE
          inclFab.fabCreateRecipe.visibility = View.GONE
          inclFab.fabOpenOptions.setImageResource(
            R.drawable.ic_create_recipe
          )
        }
      }

      binding.srlHome.setOnRefreshListener {
        refreshContent()
      }
    }

    lifecycleScope.launch {
      mainActivityViewModel.user.collect { user ->
        if (user == null) {
          goToLogin()
        }
      }
    }

    return binding.root
  }

  private fun refreshContent() {
    lifecycleScope.launch {
      mainActivityViewModel.getUser()
      mainActivityViewModel.postsNRecipesList.collectLatest {
        adapter.submitData(it)
        binding.srlHome.isRefreshing = false
      }
    }
  }

  private fun setupView() {
    binding.list.adapter = adapter

    adapter.setItemListener(object : RecipeListListener {
      override fun onRecipeClicked(recipeId: String) {
        Log.d("aahdfkfj", "masuk")
        val action =
          HomeFragmentDirections.actionHomeFragmentToRecipeDetailFragment(recipeId)
        findNavController().navigate(action)
      }

      override fun onRecipeLongClicked(recipeId: String) {
        onRecipeLongClickSelected(recipeId)
      }

      override fun onItemDeleteClicked(itemId: String, type: String) {
        when (type) {
          "post" -> mainActivityViewModel.deletePost(itemId)
          "recipe" -> mainActivityViewModel.deleteRecipe(itemId)
        }
//        adapter.removeData(itemId, type)
      }
    })

  }

  private fun loadData() {
    lifecycleScope.launch {
      mainActivityViewModel.postsNRecipesList.collectLatest {
        adapter.submitData(it)
        adapter.addViewModel(createPostViewModel)
      }
    }
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

  private fun goToLogin() {
    val intent = Intent(context, LoginActivity::class.java)
    startActivity(intent)
    activity?.finish()
  }
}