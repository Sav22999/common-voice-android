package org.commonvoice.saverio_lib.api.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.Exception

class ConnectionManager(appContext: Context) {

    private val _liveInternetAvailability = MutableLiveData(true)
    val liveInternetAvailability: LiveData<Boolean> get() = _liveInternetAvailability

    val isInternetAvailable: Boolean
        get() = _liveInternetAvailability.value ?: false

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            _liveInternetAvailability.postValue(true)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)

            _liveInternetAvailability.postValue(false)
        }

        override fun onLost(network: Network) {
            super.onLost(network)

            _liveInternetAvailability.postValue(false)
        }

        override fun onUnavailable() {
            super.onUnavailable()

            _liveInternetAvailability.postValue(false)
        }

    }

    private val networkRequest = NetworkRequest.Builder().apply {
        addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    }.build()

    init {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= 24) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            connectivityManager.registerNetworkCallback(networkRequest, callback)
        }
        if (!connectivityManager.isDefaultNetworkActive) {
            _liveInternetAvailability.postValue(false)
        }
    }

}