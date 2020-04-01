package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.fragment.SettingFragment

class SettingActivity : AppCompatActivity() {

    lateinit var themePreference: SharedPreferences
    lateinit var emailText: String
    lateinit var nApi: ApiInterface
    lateinit var token: String
    var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        var themes: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        isNightMode = themePreference.getBoolean("night mode enable", false)
        val defaultNightMode = themePreference.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        getDelegate().setLocalNightMode(defaultNightMode)
        this.setTheme(themes)
        setContentView(R.layout.activity_setting)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        token = themePreference.getString("token value", "")
        emailText = themePreference.getString("login success", "")

        var supportFragManager = getSupportFragmentManager()
        var frag = supportFragManager.findFragmentByTag("setting_fragment")
        if (frag == null) {
            var fragment = SettingFragment()
            supportFragManager.beginTransaction().add(R.id.frame_setting_sc, fragment, "setting_fragment").commit()
        }
    }

    override fun onBackPressed() {
        finish()
        var intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}