package com.fafadiatech.newscout.activity

import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fafadiatech.newscout.broadcast.ConnectivityReceiver

open class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    lateinit var connectivityReceiver: ConnectivityReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityReceiver = ConnectivityReceiver()
        connectivityReceiver.setListener(this)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(connectivityReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(connectivityReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }

    override fun onStop() {

        if (connectivityReceiver != null)
            unregisterReceiver(connectivityReceiver)
        super.onStop()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
    }
}