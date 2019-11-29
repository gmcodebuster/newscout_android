package com.fafadiatech.newscout.application

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.multidex.MultiDex
import com.crashlytics.android.BuildConfig
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.fafadiatech.newscout.broadcast.ConnectivityReceiver
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.model.DetailNewsData
import io.fabric.sdk.android.Fabric
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory

class MyApplication : Application() {

    val SENTRY_KEY = "http://7d16d86d407d4c54971c17e1bb7fbd13:8745c229359b420f8107526ae14f206f@sentry.fafadiatech.com/34"

    companion object {

        val hashMapNextUrl: HashMap<String, String?> = HashMap<String, String?>()
        val hashMapPrevUrl: HashMap<String, String?> = HashMap<String, String?>()
        val toastBroadcastAction = "com.ft.newscout.DataNotFoundAction"
        var apiCallArticles: Boolean = false
        var articleHashMap = HashMap<String, ArrayList<NewsEntity>>()
        var categoryHashMap = HashMap<String, ArrayList<String>>()
        var bookmarkListHashMap = HashMap<String, ArrayList<DetailNewsData>>()
        var checkInternet: Boolean = false

        var tagDataHashMap = HashMap<String, ArrayList<String>>()
        var categoryIdHashMap = HashMap<String, Int>()
        var zeroItemFlag: Boolean = true
        var resultSize: Int = 0


        fun putHashMapNext(key: String, value: String?) {
            hashMapNextUrl.put(key, value)
        }

        fun putHashMapPrev(key: String, value: String?) {
            hashMapPrevUrl.put(key, value)
        }


        fun setConnectionListener(listener: ConnectivityReceiver.ConnectivityReceiverListener) {
            ConnectivityReceiver.connectivityReceiverListener = listener
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        val crashlyticsCore = CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build()
        Fabric.with(this, Crashlytics())
        Sentry.init(SENTRY_KEY, AndroidSentryClientFactory(getApplicationContext()));

        try{
            var ai : ApplicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            var bundle : Bundle = ai.metaData
        }catch (e:Exception){

        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}