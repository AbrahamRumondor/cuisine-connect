package com.example.cuisineconnect.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.ActivityMainBinding

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

//      it.addOnDestinationChangedListener { _, destination, _ ->
//        when (destination.id) {
//          R.id.homeFragment -> {
//            binding.tbTitle.text = "Home"
//            // Customize toolbar for Fragment 1
//          }
//          R.id.searchFragment -> {
//            binding.tbTitle.text = "Explore"
//            // Customize toolbar for Fragment 2
//          }
//          R.id.collectionFragment -> {
//            binding.tbTitle.text = "Collection"
//            // Customize toolbar for Fragment 1
//          }
//          R.id.profileFragment -> {
//            binding.tbTitle.text = "Profile"
//            // Customize toolbar for Fragment 2
//          }
//          // Add more cases for other fragments
//          else -> {
//            binding.tbTitle.text = "Sad"
//            // Reset or set a default toolbar behavior
//          }
//        }
//      }
//      setSupportActionBar(binding.toolbar)

    }
  }
}