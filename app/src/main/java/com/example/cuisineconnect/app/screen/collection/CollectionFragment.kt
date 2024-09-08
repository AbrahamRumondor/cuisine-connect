package com.example.cuisineconnect.app.screen.collection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.cuisineconnect.databinding.FragmentCollectionBinding
import com.google.android.material.tabs.TabLayoutMediator

class CollectionFragment : Fragment() {

  private lateinit var binding: FragmentCollectionBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentCollectionBinding.inflate(inflater, container, false)

    val adapter = ViewPagerAdapter(this)
    binding.vp2Collection.adapter = adapter

    // Link the TabLayout with the ViewPager2
    TabLayoutMediator(binding.tlCollection, binding.vp2Collection) { tab, position ->
      tab.text = when (position) {
        0 -> "My Recipes"
        1 -> "Saved Recipes"
        else -> null
      }
    }.attach()

//    (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

    return binding.root
  }
}