package com.example.cuisineconnect.domain.callbacks

import com.example.cuisineconnect.domain.model.User

interface FirestoreCallback {
    fun onSuccess(user: User?)
    fun onFailure(exception: Exception)
}