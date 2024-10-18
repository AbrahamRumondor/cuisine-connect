package com.example.cuisineconnect.app.screen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cuisineconnect.databinding.FragmentSearchPromptBinding

class SearchPromptFragment : Fragment() {

  private lateinit var binding: FragmentSearchPromptBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSearchPromptBinding.inflate(inflater, container, false)
    return binding.root
  }
}