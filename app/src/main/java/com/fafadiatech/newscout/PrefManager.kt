package com.fafadiatech.newscout

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    lateinit var pref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var context: Context
    var PRIVATE_MODE = 0
    private val PREF_NAME = "androidhive-welcome"
    private val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    private val IS_INTERNET_CONNECTED = "isInternetConnected"
    private val IS_MENU_FETCHED = "isMenuFetched"

    init {
        this.context = context
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
        editor.commit()
    }

    fun isFirstTimeLaunch(): Boolean {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
    }

    fun setInternetConnected(isConnected: Boolean) {
        editor.putBoolean(IS_INTERNET_CONNECTED, isConnected)
        editor.commit()
    }

    fun isInternetConnected(): Boolean {
        return pref.getBoolean(IS_INTERNET_CONNECTED, false)
    }

    fun setMenuFetched(fetched: Boolean) {
        editor.putBoolean(IS_MENU_FETCHED, fetched)
        editor.commit()
    }

    fun isMenuFetched(): Boolean {
        return pref.getBoolean(IS_MENU_FETCHED, false)
    }
}