package com.example.alfaresto_customersapp.data.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NetworkUtils {

    private val _isConnectedToNetwork = MutableLiveData(false)
    val isConnectedToNetwork: LiveData<Boolean> = _isConnectedToNetwork

    var warningAppear = false

    fun setConnectionToTrue() {
        _isConnectedToNetwork.postValue(true)
    }

    fun setConnectionToFalse() {
        _isConnectedToNetwork.postValue(false)
    }

}