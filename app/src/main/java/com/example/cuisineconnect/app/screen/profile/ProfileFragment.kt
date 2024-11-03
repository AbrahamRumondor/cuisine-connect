package com.example.cuisineconnect.app.screen.profile

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.screen.authentication.LoginActivity
import com.example.cuisineconnect.app.screen.collection.CollectionFragmentDirections
import com.example.cuisineconnect.app.screen.follow.FollowListFragmentDirections
import com.example.cuisineconnect.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

  private lateinit var binding: FragmentProfileBinding

  private val profileViewModel: ProfileViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentProfileBinding.inflate(inflater, container, false)

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    profileViewModel.getUser()

    val adapter = ProfileViewPagerAdapter(this)
    binding.vp2Profile.adapter = adapter

    // Link the TabLayout with the ViewPager2
    TabLayoutMediator(binding.tlProfile, binding.vp2Profile) { tab, position ->
      tab.text = when (position) {
        0 -> "Posts"
        1 -> "Recipes"
        else -> null
      }
    }.attach()

    populateProfile()

    return binding.root
  }

  private fun populateProfile() {
    lifecycleScope.launch {
      profileViewModel.user.collectLatest { user ->
        binding.run {
          tvUsername.text = user.name
          val unique = "@${user.email}"
          tvUniqueUsername.text = unique

          Glide.with(root)
            .load(user.image)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(ivUserProfile)

          val posts = Html.fromHtml("<b>${user.recipes.size}</b> recipes")
          tvPosts.text = posts
          val followers = Html.fromHtml("<b>${user.follower.size}</b> followers")
          tvFollowers.text = followers
          val following = Html.fromHtml("<b>${user.following.size}</b> following")
          tvFollowing.text = following

          btnSettings.setOnClickListener {
            showPopupMenu(it, user.id)
          }

          tvFollowers.setOnClickListener {
            val action =
              ProfileFragmentDirections.actionProfileFragmentToFollowListFragment(
                userId = user.id,
                listType = "follower",
                username = user.name
              )
            findNavController().navigate(action)
          }

          tvFollowing.setOnClickListener {
            val action =
              ProfileFragmentDirections.actionProfileFragmentToFollowListFragment(
                userId = user.id,
                listType = "following",
                username = user.name
              )
            findNavController().navigate(action)
          }
        }
      }
    }
  }

  private fun showPopupMenu(view: View, userId: String) {
    val popupMenuStyle = ContextThemeWrapper(view.context, R.style.CustomPopupMenu)
    val popupMenu = PopupMenu(popupMenuStyle, view)
    popupMenu.menuInflater.inflate(R.menu.settings, popupMenu.menu)

    popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
      when (menuItem.itemId) {
        R.id.action_edit -> {
          val action =
            ProfileFragmentDirections.actionProfileFragmentToProfileEditFragment(userId)
          findNavController().navigate(action)
          true
        }

        R.id.action_settings -> {
          FirebaseAuth.getInstance().signOut()
          goToLogin()
          true
        }

        else -> false
      }
    }

    popupMenu.show()
  }

  private fun goToLogin() {
    val intent = Intent(context, LoginActivity::class.java)
    startActivity(intent)
    activity?.finish()
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