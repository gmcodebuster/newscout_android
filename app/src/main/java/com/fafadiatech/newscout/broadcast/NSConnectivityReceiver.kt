package com.fafadiatech.newscout.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NSConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getAction()
        if (action.equals("com.fafadiatech.AN_INTENT")) {

        }

        if (("android.net.conn.CONNECTIVITY_CHANGE").equals(action)) {

            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
        }
    }
}