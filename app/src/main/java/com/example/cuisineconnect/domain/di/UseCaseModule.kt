package com.example.cuisineconnect.domain.di

import com.example.alfaresto_customersapp.domain.usecase.user.UserUseCaseImpl
import com.example.cuisineconnect.domain.usecase.auth.AuthUseCase
import com.example.cuisineconnect.domain.usecase.auth.AuthUseCaseImpl
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {

  @Binds
  @ViewModelScoped
  abstract fun provideAuthUseCase(
    authUseCaseImpl: AuthUseCaseImpl
  ): AuthUseCase

  @Binds
  @ViewModelScoped
  abstract fun provideUserUseCase(
    userUseCaseImpl: UserUseCaseImpl
  ): UserUseCase

}