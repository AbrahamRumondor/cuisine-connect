package com.example.cuisineconnect.app.screen.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.screen.authentication.LoginActivity

class SplashScreenActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Enable edge-to-edge layout
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContentView(R.layout.activity_splash_screen)

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    // Delay and then navigate to LoginActivity
    Handler(Looper.getMainLooper()).postDelayed({
      startActivity(Intent(this, LoginActivity::class.java))
      finish() // Close SplashScreenActivity so it’s not accessible on back press
    }, 1000) // 2-second delay
  }
}