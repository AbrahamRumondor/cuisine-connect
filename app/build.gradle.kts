plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.google.gms.google.services)
  id("kotlin-kapt")
  id("com.google.dagger.hilt.android")
  id("androidx.navigation.safeargs.kotlin")
}

android {
  namespace = "com.example.cuisineconnect"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.example.cuisineconnect"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  buildFeatures {
    viewBinding = true
    buildConfig = true
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

kapt {
  correctErrorTypes = true
}

dependencies {
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.legacy.support.v4)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.lifecycle.livedata.ktx)
  val coroutinesAndroid = "1.7.1"
  val coroutinesCore = "1.6.4"

  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
  implementation("androidx.fragment:fragment-ktx:1.8.0")
  implementation("com.google.firebase:firebase-firestore:25.0.0")
  implementation("com.google.firebase:firebase-auth:23.0.0")
  implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
  implementation("com.google.firebase:firebase-storage:21.0.0")
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.firebase.firestore)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  // hilt
  implementation("com.google.dagger:hilt-android:2.48")
  kapt("com.google.dagger:hilt-android-compiler:2.48")

  // glide
  implementation("com.github.bumptech.glide:glide:4.12.0")
  kapt("com.github.bumptech.glide:compiler:4.12.0")

  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesAndroid")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesAndroid")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesCore")

  // timber
  implementation("com.jakewharton.timber:timber:5.0.1")
}