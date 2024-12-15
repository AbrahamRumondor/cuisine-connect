package com.example.cuisineconnect.app.screen.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.airbnb.lottie.LottieDrawable
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.MainActivityViewModel
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.screen.authentication.LoginActivity
import com.example.cuisineconnect.app.screen.collection.CollectionFragmentDirections
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.app.util.UserUtil.isSelectingRecipe
import com.example.cuisineconnect.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
    adapter.isFromHome()
    adapter.addViewModel(createPostViewModel)
    loadingAnimation()


    binding.run {

      inclFab.fabOpenOptions.setOnClickListener {
        toggleFabOptions()
      }

      srlHome.setOnRefreshListener {
        refreshContent()
      }

      lifecycleScope.launch {
        delay(2000)
        setConnectionBehaviour()
      }
      inclInternet.btnInetTryAgain.setOnClickListener {
        setConnectionBehaviour()
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


  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
      showNoInternetLayout()
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
      hideNoInternetLayout()
    }
  }

  private fun refreshContent() {
    binding.srlHome.isRefreshing = true

    lifecycleScope.launch {
      mainActivityViewModel.getUser()
      mainActivityViewModel.refreshItems()

      mainActivityViewModel.postsNRecipesList.collectLatest { pagingData ->
        binding.srlHome.isRefreshing = false
        mainActivityViewModel.refreshToFalse()
        adapter.submitData(pagingData)
      }
    }
  }

  private fun loadData() {
    binding.srlHome.isRefreshing = true
    lifecycleScope.launch {
      mainActivityViewModel.postsNRecipesList.collectLatest { pagingData ->
        Log.d("homeFragment", "data loaded...")
        binding.srlHome.isRefreshing = false
        binding.ivEmptyState.visibility = View.GONE // Hide empty state initially
        adapter.submitData(pagingData)
      }
    }

    // Listen to the adapter's load states
    adapter.addLoadStateListener { loadStates ->
      // Show empty state if no items loaded after refresh is complete
      if (loadStates.refresh is LoadState.NotLoading && adapter.itemCount == 0) {
        binding.list.visibility = View.VISIBLE
        binding.ivEmptyState.visibility = View.VISIBLE
      } else if (loadStates.refresh is LoadState.Loading) {
        // Optionally show loading animation while loading
        binding.list.visibility = View.INVISIBLE
        showLoadingAnimation()
      } else {
        hideLoadingAnimation()
        binding.list.visibility = View.VISIBLE
        binding.ivEmptyState.visibility = View.GONE
      }
    }
  }

  private fun setupView() {
    binding.list.adapter = adapter

    adapter.setItemListener(object : ItemListListener {
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

      override fun onPostClicked(postId: String) {
        val action =
          HomeFragmentDirections.actionHomeFragmentToPostDetailFragment2(postId)
        findNavController().navigate(action)
      }

      override fun onUserProfileClicked(userId: String) {
        if (userId == currentUser?.id) {
          val action =
            HomeFragmentDirections.actionHomeFragmentToProfileFragment()
          findNavController().navigate(action)
          return
        } else {
          val action =
            HomeFragmentDirections.actionHomeFragmentToOtherProfileFragment(
              userId
            )
          findNavController().navigate(action)
        }
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

  private fun showNoInternetLayout() {
    binding.inclInternet.root.visibility = View.VISIBLE
    binding.appbarLayout.visibility = View.GONE
    binding.srlHome.visibility = View.GONE
    binding.inclFab.root.visibility = View.GONE
  }

  private fun hideNoInternetLayout() {
    binding.inclInternet.root.visibility = View.GONE
    binding.appbarLayout.visibility = View.VISIBLE
    binding.srlHome.visibility = View.VISIBLE
    binding.inclFab.root.visibility = View.VISIBLE
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

  override fun onResume() {
    super.onResume()
    if (isSelectingRecipe) {
      binding.inclFab.fabOpenOptions.visibility = View.GONE
    } else {
      binding.inclFab.fabOpenOptions.visibility = View.VISIBLE
    }
    showLoadingAnimation()
  }
}