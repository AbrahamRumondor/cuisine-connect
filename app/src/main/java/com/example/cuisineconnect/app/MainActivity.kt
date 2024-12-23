package com.example.cuisineconnect.app

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.app.util.UserUtil.isSelectingRecipe
import com.example.cuisineconnect.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private var navController: NavController? = null

  @Inject
  lateinit var connectivityObserver: ConnectivityObserver

  @RequiresApi(VERSION_CODES.R)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    checkConnectivityStatus()

    binding.btnCloseTips.setOnClickListener {
      binding.flChooseRecipe.visibility = View.GONE
    }

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
          R.id.homeFragment -> {
            showBottomNav()
            this.window.statusBarColor = this.getColor(R.color.cc_vanilla_cream)
          }

          else -> {
            this.window.statusBarColor = this.getColor(R.color.white)
            showBottomNav()
          }
        }
      }

    }

    lifecycleScope.launch {
      currentUser?.let {
        setLocale(it.language)
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

//  fun setLocale(lang: String) {
//    val locale = Locale(lang)
//    Locale.setDefault(locale)
//    val resources = resources
//    val config: Configuration = resources.configuration
//    config.setLocale(locale)
//    resources.updateConfiguration(config, resources.displayMetrics)
//
//    // Restart the activity to apply the changes
////    val intent = intent
////    finish()
////    startActivity(intent)
//    recreate()
//  }

  fun setLocale(lang: String) {
    val newLocale = Locale(lang)
    val currentLocale = resources.configuration.locales[0] // Get the current locale

    // Check if the new locale is different from the current locale
    if (newLocale != currentLocale) {
      Locale.setDefault(newLocale)
      val resources = resources
      val config: Configuration = resources.configuration
      config.setLocale(newLocale)
      resources.updateConfiguration(config, resources.displayMetrics)

      val builder = AlertDialog.Builder(application.baseContext)
      val langStr = if (currentUser?.language == "id") "Mohon untuk mengulang aplikasi untuk menerapkan perubahan bahasa." else "Please restart the application to apply the language change."
      builder.setTitle(if (currentUser?.language == "id") "Pemberitahuan" else "Notification")
      builder.setMessage(langStr)
//      // Restart the activity to apply the changes
//      val intent = intent
//      finish()
//      startActivity(intent)
    }
  }

  fun showChooseRecipeView(show: Boolean) {
    isSelectingRecipe = show
    binding.flChooseRecipe.visibility = if (show) View.VISIBLE else View.GONE
  }
}