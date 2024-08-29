package com.example.cuisineconnect.app

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private var navController: NavController? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    this.window.statusBarColor = this.getColor(R.color.cc_yellow);

    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
    navController = navHostFragment.navController

    navController?.let {
      binding.bnvMain.setupWithNavController(it)

      it.addOnDestinationChangedListener { _, destination, _ ->
        Timber.tag("abcd").d(destination.id.toString())
        when (destination.id) {
          R.id.createRecipeFragment -> hideBottomNav()
          else -> showBottomNav()
        }
      }

    }
  }

  private fun hideBottomNav() {
    binding.bnvMain.visibility = View.GONE
  }

  private fun showBottomNav() {
    binding.bnvMain.visibility = View.VISIBLE
  }

}