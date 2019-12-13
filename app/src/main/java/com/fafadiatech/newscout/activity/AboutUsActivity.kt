package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fafadiatech.newscout.BuildConfig
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import android.text.util.Linkify.WEB_URLS
import android.text.util.Linkify
import androidx.core.text.util.LinkifyCompat


class AboutUsActivity : AppCompatActivity() {

    lateinit var themePreference: SharedPreferences
    lateinit var apiInterfaceObj: ApiInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        apiInterfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        var themes: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        this.setTheme(themes)
        setContentView(R.layout.activity_about_us)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)
        var tvVersionNo = findViewById<TextView>(R.id.tv_app_version_no)
        var tvLastUpdatedDate = findViewById<TextView>(R.id.tv_last_update_value)
        var versionNo = BuildConfig.VERSION_NAME
        tvVersionNo.text = versionNo
        tvLastUpdatedDate.text = resources.getString(R.string.last_update_value)

        var developedBy = findViewById<TextView>(R.id.tv_developed_by_value)

    }

    fun developedValue(view: View){
        var deviceId = themePreference.getString("device_token", "")
        val sessionId = getUniqueCode(this@AboutUsActivity, themePreference)
        trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.DEVELOPEDBY.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

        val uris = Uri.parse("http://"+resources.getString(R.string.developed_by_value))
        val intents = Intent(Intent.ACTION_VIEW, uris)

        startActivity(intents)

    }
}