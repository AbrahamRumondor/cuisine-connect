package com.example.cuisineconnect.data.di

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideApplicationContext(application: Application): Context {
    return application.applicationContext
  }

  @Provides
  @Singleton
  @Named("db")
  fun provideDb(): FirebaseFirestore {
    return Firebase.firestore
  }

  @Provides
  @Singleton
  fun provideAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
  }

  @Provides
  @Singleton
  @Named("usersRef")
  fun provideUserRef(): CollectionReference {
    return Firebase.firestore.collection("users")
  }

  @Provides
  @Singleton
  @Named("recipesRef")
  fun provideRecipesRef(): CollectionReference {
    return Firebase.firestore.collection("recipes")
  }

  @Provides
  @Singleton
  @Named("stepsRef")
  fun provideStepsRef(): CollectionReference {
    return Firebase.firestore.collection("steps")
  }

  @Provides
  @Singleton
  @Named("ingredientsRef")
  fun provideIngredientsRef(): CollectionReference {
    return Firebase.firestore.collection("ingredients")
  }

  @Provides
  @Singleton
  @Named("categoriesRef")
  fun provideCategoriesRef(): CollectionReference {
    return Firebase.firestore.collection("categories")
  }

  @Provides
  @Singleton
  @Named("postsRef")
  fun providePostsRef(): CollectionReference {
    return Firebase.firestore.collection("posts")
  }
}