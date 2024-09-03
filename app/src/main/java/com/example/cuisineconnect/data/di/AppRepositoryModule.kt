package com.example.cuisineconnect.data.di

import com.example.cuisineconnect.data.repository.AuthRepositoryImpl
import com.example.cuisineconnect.data.repository.UserRepositoryImpl
import com.example.cuisineconnect.domain.repository.AuthRepository
import com.example.cuisineconnect.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class AppRepositoryModule {

  @Binds
  abstract fun provideAuthRepository(
    authRepositoryImpl: AuthRepositoryImpl
  ): AuthRepository

  @Binds
  abstract fun provideUserRepository(
    userRepositoryImpl: UserRepositoryImpl
  ): UserRepository

}