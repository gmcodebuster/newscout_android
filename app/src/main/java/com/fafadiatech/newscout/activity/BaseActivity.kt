package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.appconstants.getUniqueCode
import com.fafadiatech.newscout.broadcast.ConnectivityReceiver

open class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    lateinit var connRecv: ConnectivityReceiver
    lateinit var themePreference: SharedPreferences
    var themes: Int = R.style.DefaultMedium
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        val defaultNightMode = themePreference.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        getDelegate().setLocalNightMode(defaultNightMode)
        themes = themePreference.getInt("theme", R.style.DefaultMedium)

        this.setTheme(themes)
        connRecv = ConnectivityReceiver()
        connRecv.setListener(this)
        getUniqueCode(this, themePreference)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(connRecv, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(connRecv, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }

    override fun onStop() {

        if (connRecv != null)
            unregisterReceiver(connRecv)
        super.onStop()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}