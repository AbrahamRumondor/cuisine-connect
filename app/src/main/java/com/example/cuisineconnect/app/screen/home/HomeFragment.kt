package com.example.cuisineconnect.app.screen.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.MainActivityViewModel
import com.example.cuisineconnect.app.screen.authentication.LoginActivity
import com.example.cuisineconnect.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

  private lateinit var binding: FragmentHomeBinding
  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentHomeBinding.inflate(inflater, container, false)

    binding.run {
      inclFab.fabOpenOptions.setOnClickListener {
        if (inclFab.fabCreatePost.visibility == View.GONE && inclFab.fabCreateRecipe.visibility == View.GONE) {
          inclFab.fabCreatePost.visibility = View.VISIBLE
          inclFab.fabCreateRecipe.visibility = View.VISIBLE
          inclFab.fabOpenOptions.setImageResource(
            R.drawable.ic_close
          )

          inclFab.fabCreateRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createRecipeFragment)
          }
          inclFab.fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createPostFragment)
          }
        } else {
          inclFab.fabCreatePost.visibility = View.GONE
          inclFab.fabCreateRecipe.visibility = View.GONE
          inclFab.fabOpenOptions.setImageResource(
            R.drawable.ic_create_recipe
          )
        }
      }
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