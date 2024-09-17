package com.example.cuisineconnect.data.network.networkStatusObserver

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver.Status
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver.Status.Available
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver.Status.Losing
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver.Status.Lost
import com.example.alfaresto_customersapp.data.network.networkStatusObserver.ConnectivityObserver.Status.Unavailable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityObserver @Inject constructor(
    context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(Available) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { send(Losing) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(Lost) }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(Unavailable) }
                }
            }

            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }
}