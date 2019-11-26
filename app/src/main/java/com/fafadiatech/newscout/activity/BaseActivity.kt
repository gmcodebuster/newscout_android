package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.broadcast.ConnectivityReceiver

open class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    lateinit var connectivityReceiver: ConnectivityReceiver
    lateinit var themePreference: SharedPreferences
    var themes: Int = R.style.DefaultMedium
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        val defaultNightMode = themePreference.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        getDelegate().setLocalNightMode(defaultNightMode)
        themes = themePreference.getInt("theme", R.style.DefaultMedium)

        this.setTheme(themes)
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