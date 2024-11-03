package com.example.cuisineconnect.app

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private var navController: NavController? = null

  @Inject
  lateinit var connectivityObserver: ConnectivityObserver

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    checkConnectivityStatus()

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
          R.id.recipeDetailFragment -> hideBottomNav()
          R.id.replyRecipeFragment -> hideBottomNav()
          R.id.createPostFragment -> hideBottomNav()
          R.id.searchPromptFragment -> hideBottomNav()
          R.id.postDetailFragment2 -> hideBottomNav()
          R.id.searchResultFragment -> hideBottomNav()
          R.id.otherProfileFragment -> hideBottomNav()
          else -> showBottomNav()
        }
      }

    }
  }

  private fun checkConnectivityStatus() {
    lifecycleScope.launch {
      connectivityObserver.observe().collectLatest {
        if (it.toString() == getString(R.string.available) &&
          NetworkUtils.isConnectedToNetwork.value != true
        ) {
//                    binding.vBlockActions.visibility = View.GONE
          NetworkUtils.setConnectionToTrue()
        } else if (it.toString() != getString(R.string.available) &&
          NetworkUtils.isConnectedToNetwork.value != false
        ) {
//                    binding.vBlockActions.visibility = View.VISIBLE
          NetworkUtils.setConnectionToFalse()
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