package com.fafadiatech.newscout.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val isConnected = checkConnection(context)
        if (connRecvListener != null) connRecvListener?.onNetworkConnectionChanged(isConnected)
    }

    fun checkConnection(context: Context?): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return (activeNetwork != null && activeNetwork.isConnected)
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connRecvListener: ConnectivityReceiverListener? = null
    }

    fun setListener(listener: ConnectivityReceiverListener) {
        connRecvListener = listener as ConnectivityReceiverListener
    }
}