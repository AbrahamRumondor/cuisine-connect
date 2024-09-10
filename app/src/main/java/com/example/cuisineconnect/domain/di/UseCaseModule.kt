package com.example.cuisineconnect.domain.di

import com.example.cuisineconnect.domain.usecase.user.UserUseCaseImpl
import com.example.cuisineconnect.domain.usecase.auth.AuthUseCase
import com.example.cuisineconnect.domain.usecase.auth.AuthUseCaseImpl
import com.example.cuisineconnect.domain.usecase.category.CategoryUseCase
import com.example.cuisineconnect.domain.usecase.category.CategoryUseCaseImpl
import com.example.cuisineconnect.domain.usecase.ingredient.IngredientUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCaseImpl
import com.example.cuisineconnect.domain.usecase.step.StepUseCase
import com.example.cuisineconnect.domain.usecase.step.StepUseCaseImpl
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

  @Binds
  @ViewModelScoped
  abstract fun provideRecipeUseCase(
    recipeUseCaseImpl: RecipeUseCaseImpl
  ): RecipeUseCase

  @Binds
  @ViewModelScoped
  abstract fun provideStepUseCase(
    stepUseCaseImpl: StepUseCaseImpl
  ): StepUseCase

  @Binds
  @ViewModelScoped
  abstract fun provideIngredientUseCase(
    ingredientUseCase: IngredientUseCase
  ): IngredientUseCase

  @Binds
  @ViewModelScoped
  abstract fun provideCategoryUseCase(
    categoryUseCaseImpl: CategoryUseCaseImpl
  ): CategoryUseCase
}