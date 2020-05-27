package org.commonvoice.saverio_lib.api.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ConnectionManager(appContext: Context) {

    private val _isInternetAvailable = MutableLiveData(true)
    val isInternetAvailable: LiveData<Boolean> get() = _isInternetAvailable

    val isAvailable: Boolean
        get() = _isInternetAvailable.value ?: true

    private val callback = object: ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            _isInternetAvailable.postValue(true)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)

            _isInternetAvailable.postValue(false)
        }

        override fun onLost(network: Network) {
            super.onLost(network)

            _isInternetAvailable.postValue(false)
        }

        override fun onUnavailable() {
            super.onUnavailable()

            _isInternetAvailable.postValue(false)
        }

    }

    private val networkRequest = NetworkRequest.Builder().apply {
        addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    }.build()

    init {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= 24) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            connectivityManager.registerNetworkCallback(networkRequest, callback)
        }
    }

}