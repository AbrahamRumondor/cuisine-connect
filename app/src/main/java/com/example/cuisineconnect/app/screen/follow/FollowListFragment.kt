package com.example.cuisineconnect.app.screen.follow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.databinding.FragmentFollowListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FollowListFragment : Fragment() {

  private lateinit var binding: FragmentFollowListBinding
  private val followListViewModel: FollowListViewModel by viewModels()
  private val followListAdapter by lazy { FollowListAdapter() }

  private var userId: String? = null
  private var listType: String? = null
  private var isMe = false

  private val args: FollowListFragmentArgs by navArgs()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    userId = args.userId
    listType = args.listType
    isMe = userId == currentUser?.id

    if (!isMe) {
      userId?.let {
        followListViewModel.getOtherUser(it)
      }
      if (listType == "follower") {
        followListViewModel.otherUserFollower()
      } else {
        followListViewModel.otherUserFollowing()
      }
    } else {
      if (listType == "follower") {
        followListViewModel.getMyFollowers()
      } else {
        followListViewModel.getMyFollowing()
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentFollowListBinding.inflate(inflater, container, false)

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    setupToolbar()
    setupRecyclerView()
    setupSwipeRefresh() // Set up swipe to refresh
    observeFollowList()
    return binding.root
  }

  private fun setupToolbar() {
    (activity as? AppCompatActivity)?.apply {
      setSupportActionBar(binding.toolbar)
      supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setDisplayShowHomeEnabled(true)
        title = if (args.listType == "follower") {
          args.listType.replaceFirstChar { 'F' } + "s" // Append 's' if listType is "follower"
        } else {
          args.listType.replaceFirstChar { 'F' } // Just replace the first character if not
        }
      }
    }

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }
  }

  private fun setupRecyclerView() {
    binding.list.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = followListAdapter
    }

    followListAdapter.setItemListener(object : FollowListListener {
      override fun onUserClicked(userId: String) {
        val action = FollowListFragmentDirections.actionFollowListFragmentToOtherProfileFragment(userId)
        findNavController().navigate(action)
      }
    })
  }

  private fun setupSwipeRefresh() {
    binding.srlHome.setOnRefreshListener {
      refreshFollowList()
    }
  }

  private fun refreshFollowList() {
    if (!isMe) {
      userId?.let {
        followListViewModel.getOtherUser(it)
      }
      if (listType == "follower") {
        followListViewModel.otherUserFollower()
      } else {
        followListViewModel.otherUserFollowing()
      }
    } else {
      if (listType == "follower") {
        followListViewModel.getMyFollowers()
      } else {
        followListViewModel.getMyFollowing()
      }
    }
    binding.srlHome.isRefreshing = false // Stop refreshing after data is fetched
  }

  private fun observeFollowList() {
    lifecycleScope.launch {
      val flow = when {
        isMe && listType == "follower" -> followListViewModel.myFollowers
        isMe && listType == "following" -> followListViewModel.myFollowing
        !isMe && listType == "follower" -> followListViewModel.otherUserFollowers
        !isMe && listType == "following" -> followListViewModel.otherUserFollowing
        else -> emptyFlow()
      }
      flow.collectLatest { users ->
        followListAdapter.submitUsers(users)
      }
    }
  }

  override fun onResume() {
    super.onResume()
//    refreshFollowList() // Refresh on resume
  }

  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
      binding.inclInternet.root.visibility = View.VISIBLE
      binding.cvToolbar.visibility = View.GONE
      binding.srlHome.visibility = View.GONE
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
      binding.inclInternet.root.visibility = View.GONE
      binding.cvToolbar.visibility = View.VISIBLE
      binding.srlHome.visibility = View.VISIBLE
    }
  }
}