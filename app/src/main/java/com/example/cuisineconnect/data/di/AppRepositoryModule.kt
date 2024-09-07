package com.example.cuisineconnect.data.di

import com.example.cuisineconnect.data.repository.AuthRepositoryImpl
import com.example.cuisineconnect.data.repository.CategoryRepositoryImpl
import com.example.cuisineconnect.data.repository.IngredientRepositoryImpl
import com.example.cuisineconnect.data.repository.RecipeRepositoryImpl
import com.example.cuisineconnect.data.repository.StepRepositoryImpl
import com.example.cuisineconnect.data.repository.UserRepositoryImpl
import com.example.cuisineconnect.domain.repository.AuthRepository
import com.example.cuisineconnect.domain.repository.CategoryRepository
import com.example.cuisineconnect.domain.repository.IngredientRepository
import com.example.cuisineconnect.domain.repository.RecipeRepository
import com.example.cuisineconnect.domain.repository.StepRepository
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

  @Binds
  abstract fun provideRecipeRepository(
    recipeRepositoryImpl: RecipeRepositoryImpl
  ): RecipeRepository

  @Binds
  abstract fun provideStepRepository(
    stepRepositoryImpl: StepRepositoryImpl
  ): StepRepository

  @Binds
  abstract fun provideIngredientRepository(
    ingredientRepositoryImpl: IngredientRepositoryImpl
  ): IngredientRepository

  @Binds
  abstract fun provideCategoryRepository(
    categoryRepositoryImpl: CategoryRepositoryImpl
  ): CategoryRepository
}