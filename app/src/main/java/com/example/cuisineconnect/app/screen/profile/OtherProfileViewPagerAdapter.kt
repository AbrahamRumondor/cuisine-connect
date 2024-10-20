package com.example.cuisineconnect.app.screen.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cuisineconnect.app.screen.collection.SavedRecipeFragment
import com.example.cuisineconnect.app.screen.collection.myRecipe.MyRecipeFragment
import com.example.cuisineconnect.app.screen.profile.post.OtherProfilePostFragment
import com.example.cuisineconnect.app.screen.profile.post.ProfilePostFragment

class OtherProfileViewPagerAdapter(
  activity: OtherProfileFragment,
  private val userId: String // Add userId as a constructor parameter
) : FragmentStateAdapter(activity) {

  override fun getItemCount(): Int = 2  // Number of tabs

  override fun createFragment(position: Int): Fragment {
    return when (position) {
      0 -> {
        val fragment = OtherProfilePostFragment()
        fragment.arguments = Bundle().apply {
          putString("userId", userId)
        }
        fragment
      }
      1 -> {
        val fragment = SavedRecipeFragment()
        fragment.arguments = Bundle().apply {
          putString("userId", userId)
        }
        fragment
      }
      else -> throw IllegalStateException("Unexpected position $position")
    }
  }
}