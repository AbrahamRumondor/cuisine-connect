package com.example.cuisineconnect.data.di

import android.content.Context
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver
import com.example.cuisineconnect.data.network.networkStatusObserver.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

  @Provides
  @Singleton
  fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
    return NetworkConnectivityObserver(context)
  }
}