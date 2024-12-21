package com.example.alfaresto_customersapp.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NetworkUtils {

    private val _isConnectedToNetwork = MutableLiveData(true)
    val isConnectedToNetwork: LiveData<Boolean> = _isConnectedToNetwork

    var warningAppear = false

    fun setConnectionToTrue() {
        Log.d("netutils", "true")
        _isConnectedToNetwork.postValue(true)
    }

    fun setConnectionToFalse() {
        Log.d("netutils", "false")
        _isConnectedToNetwork.postValue(false)
    }

}