package com.example.cuisineconnect.app.screen.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.MainActivity
import com.example.cuisineconnect.app.MainActivityViewModel
import com.example.cuisineconnect.app.screen.authentication.LoginActivity
import com.example.cuisineconnect.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

  private lateinit var binding: FragmentHomeBinding
  private lateinit var auth: FirebaseAuth
  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentHomeBinding.inflate(inflater, container, false)

    binding.inclFab.fabCreateRecipe.setOnClickListener {
      findNavController().navigate(R.id.action_homeFragment_to_createRecipeFragment)
    }

    lifecycleScope.launch {
      mainActivityViewModel.user.collect { user ->
        if (user == null) {
          goToLogin()
        } else {
          binding.tvName.text = user.name
        }
      }
    }

    binding.btnLogout.setOnClickListener {
      FirebaseAuth.getInstance().signOut()
      goToLogin()
    }

    return binding.root
  }

  private fun goToLogin() {
    val intent = Intent(context, LoginActivity::class.java)
    startActivity(intent)
    activity?.finish()
  }
}