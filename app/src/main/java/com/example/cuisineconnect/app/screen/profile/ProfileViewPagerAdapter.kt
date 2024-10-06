package com.example.cuisineconnect.app.screen.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cuisineconnect.app.screen.collection.SavedRecipeFragment
import com.example.cuisineconnect.app.screen.collection.myRecipe.MyRecipeFragment
import com.example.cuisineconnect.app.screen.profile.post.ProfilePostFragment

class ProfileViewPagerAdapter(activity: ProfileFragment) : FragmentStateAdapter(activity) {
  override fun getItemCount(): Int = 2  // Number of tabs

  override fun createFragment(position: Int): Fragment {
    return when (position) {
      0 -> ProfilePostFragment()
      1 -> SavedRecipeFragment()
      else -> throw IllegalStateException("Unexpected position $position")
    }
  }
}