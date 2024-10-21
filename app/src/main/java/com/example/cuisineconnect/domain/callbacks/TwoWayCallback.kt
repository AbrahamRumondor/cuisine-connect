package com.example.cuisineconnect.domain.callbacks

interface TwoWayCallback {
    fun onSuccess()
    fun onFailure(errorMessage: String)
}