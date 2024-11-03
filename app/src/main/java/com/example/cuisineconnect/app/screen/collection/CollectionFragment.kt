package com.example.cuisineconnect.app.screen.collection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentCollectionBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CollectionFragment : Fragment() {

  private lateinit var binding: FragmentCollectionBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentCollectionBinding.inflate(inflater, container, false)

    val adapter = CollectionViewPagerAdapter(this)
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

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    return binding.root
  }

  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
      binding.inclInternet.root.visibility = View.VISIBLE
      binding.appbarLayout.visibility = View.GONE
      binding.vp2Collection.visibility = View.GONE
      binding.inclFab.root.visibility = View.GONE
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
      binding.inclInternet.root.visibility = View.GONE
      binding.appbarLayout.visibility = View.VISIBLE
      binding.vp2Collection.visibility = View.VISIBLE
      binding.inclFab.root.visibility = View.VISIBLE
    }
  }
}