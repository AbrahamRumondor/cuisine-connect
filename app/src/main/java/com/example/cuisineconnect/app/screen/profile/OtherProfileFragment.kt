package com.example.cuisineconnect.app.screen.profile

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.databinding.FragmentOtherProfileBinding
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtherProfileFragment : Fragment() {

  private lateinit var binding: FragmentOtherProfileBinding

  private val profileViewModel: ProfileViewModel by activityViewModels()
  private val args: OtherProfileFragmentArgs by navArgs()


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentOtherProfileBinding.inflate(inflater, container, false)

    val userId = args.userId
    userId?.let { uId ->
      profileViewModel.getUser(uId)

      val adapter = OtherProfileViewPagerAdapter(this, uId)
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

      binding.btnBack.setOnClickListener {
        findNavController().navigateUp()
      }
    }

    return binding.root
  }

  private fun populateProfile() {
    lifecycleScope.launch {
      profileViewModel.otherUser.collectLatest { user ->
        binding.run {
          tvUsername.text = user.name
          val unique = "@${user.email}"
          tvUniqueUsername.text = unique

          Glide.with(root)
            .load(user.image)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(ivUserProfile)

          if (user.follower.contains(currentUser?.id)) {
            binding.btnFollow.run {
              text = "Following"
              setOnClickListener {
                profileViewModel.unFollowUser(object : TwoWayCallback {
                  override fun onSuccess() {
                    profileViewModel.getUser()
                    profileViewModel.getUser(user.id)
                    Toast.makeText(context, "Successfully unfollowed", Toast.LENGTH_SHORT).show()
                  }

                  override fun onFailure(errorMessage: String) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                  }
                })
              }
            }
          } else {
            binding.btnFollow.run {
              text = "Follow"
              setOnClickListener {
                profileViewModel.followUser(object : TwoWayCallback {
                  override fun onSuccess() {
                    profileViewModel.getUser()
                    profileViewModel.getUser(user.id)
                    Toast.makeText(context, "Successfully followed", Toast.LENGTH_SHORT).show()
                  }

                  override fun onFailure(errorMessage: String) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                  }
                })
              }
            }
          }

          val posts = Html.fromHtml("<b>${user.recipes.size}</b> recipes")
          tvPosts.text = posts
          val followers = Html.fromHtml("<b>${user.follower.size}</b> followers")
          tvFollowers.text = followers
          val following = Html.fromHtml("<b>${user.following.size}</b> following")
          tvFollowing.text = following
        }
      }
    }
  }
}