package com.example.cuisineconnect.app.screen.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.screen.profile.ProfileFragmentDirections
import com.example.cuisineconnect.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

  private lateinit var binding: FragmentSearchBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSearchBinding.inflate(inflater, container, false)

    binding.clSearchBar.setOnClickListener {
      val action = SearchFragmentDirections.actionSearchFragmentToSearchPromptFragment()
      findNavController().navigate(action)
    }

    return binding.root
  }

}