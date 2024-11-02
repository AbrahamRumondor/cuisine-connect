package com.example.cuisineconnect.app.screen.collection

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cuisineconnect.app.screen.collection.bookmark.BookmarkRecipeFragment
import com.example.cuisineconnect.app.screen.collection.myRecipe.MyRecipeFragment

class CollectionViewPagerAdapter(activity: CollectionFragment) : FragmentStateAdapter(activity) {
  override fun getItemCount(): Int = 2  // Number of tabs

  override fun createFragment(position: Int): Fragment {
    return when (position) {
      0 -> MyRecipeFragment()
      1 -> BookmarkRecipeFragment()
      else -> throw IllegalStateException("Unexpected position $position")
    }
  }
}