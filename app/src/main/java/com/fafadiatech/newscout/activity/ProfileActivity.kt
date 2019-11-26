package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.AppConstant

class ProfileActivity : BaseActivity() {

    lateinit var token: String
    lateinit var tvUserEmail: TextView
    lateinit var userEmail: String
    lateinit var tvUserName: TextView
    lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)
        var btnChangePassword = findViewById<Button>(R.id.btn_change_password)
        tvUserEmail = findViewById<TextView>(R.id.txt_profile_email)
        tvUserName = findViewById(R.id.txt_profile_name)
        token = themePreference.getString("token value", "")
        userEmail = themePreference.getString("login success", "")
        userName = themePreference.getString("user name", "")

        if (token == "") {
            btnChangePassword.visibility = View.GONE
            tvUserEmail.text = resources.getString(R.string.not_login_msg)
        } else {
            btnChangePassword.visibility = View.VISIBLE
            tvUserEmail.text = userEmail
            tvUserName.text = userName
        }

        btnChangePassword.setOnClickListener {
            var startIntent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(startIntent)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        super.onNetworkConnectionChanged(isConnected)
    }
}