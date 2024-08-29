package com.example.cuisineconnect.app.screen.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

  private lateinit var binding: FragmentHomeBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentHomeBinding.inflate(inflater, container, false)

    binding.inclFab.fabCreateRecipe.setOnClickListener {
      findNavController().navigate(R.id.action_homeFragment_to_createRecipeFragment)
    }

    return binding.root
  }
}