package com.example.cuisineconnect.app.screen.profile

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
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
          0 -> getString(R.string.posts_caps)
          1 -> getString(R.string.recipes_caps)
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
          tvUsername.text = user.displayName
          val username = "@${user.username}"
          tvUniqueUsername.text = username

          Glide.with(root)
            .load(user.image)
            .placeholder(R.drawable.ic_bnv_profile)
            .into(ivUserProfile)

          Glide.with(root)
            .load(user.background)
            .into(ivBackground)

          if (user.bio.isNotEmpty()) {
            tvBio.visibility = View.VISIBLE
            tvBio.text = user.bio
          } else {
            tvBio.visibility = View.GONE
          }

          if (user.id == currentUser?.id) {
            binding.btnFollow.visibility = View.GONE
          } else if (user.follower.contains(currentUser?.id)) {
            binding.btnFollow.run {
              text = getString(R.string.following_caps)
              setOnClickListener {
                // Show confirmation dialog for unfollow
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.unfollow_user_title))
                builder.setMessage("${getString(R.string.unfollow_user_message)} ${user.displayName}?")

                builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                  // Call unfollow logic
                  profileViewModel.unFollowUser(object : TwoWayCallback {
                    override fun onSuccess() {
                      profileViewModel.getUser() // Refresh the current user's profile
                      profileViewModel.getUser(user.id) // Refresh the target user's profile
                      Toast.makeText(context, getString(R.string.successfully_unfollowed), Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(errorMessage: String) {
                      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                  })
                  dialog.dismiss() // Dismiss the dialog
                }

                builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                  dialog.dismiss() // Dismiss the dialog if the user cancels
                }

                builder.show() // Display the dialog
              }
            }
          } else {
            binding.btnFollow.run {
              text = getString(R.string.follow)
              setOnClickListener {
                profileViewModel.followUser(object : TwoWayCallback {
                  override fun onSuccess() {
                    profileViewModel.getUser()
                    profileViewModel.getUser(user.id)
                    Toast.makeText(context, getString(R.string.successfully_followed), Toast.LENGTH_SHORT).show()
                  }

                  override fun onFailure(errorMessage: String) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                  }
                })
              }
            }
          }

          val posts = Html.fromHtml("<b>${user.recipes.size}</b> ${getString(R.string.recipes)}")
          tvPosts.text = posts
          val followers = Html.fromHtml("<b>${user.follower.size}</b> ${getString(R.string.followers)}")
          tvFollowers.text = followers
          val following = Html.fromHtml("<b>${user.following.size}</b> ${getString(R.string.following)}")
          tvFollowing.text = following

          tvFollowers.setOnClickListener {
            val action =
              OtherProfileFragmentDirections.actionOtherProfileFragmentToFollowListFragment(
                userId = user.id,
                listType = "follower",
                username = user.displayName
              )
            findNavController().navigate(action)
          }

          tvFollowing.setOnClickListener {
            val action =
              OtherProfileFragmentDirections.actionOtherProfileFragmentToFollowListFragment(
                userId = user.id,
                listType = "following",
                username = user.displayName
              )
            findNavController().navigate(action)
          }
        }
      }
    }
  }
}