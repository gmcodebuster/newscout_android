package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.model.ChangePasswordData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : BaseActivity() {

    lateinit var oldPassword: AppCompatEditText
    lateinit var newPassword: AppCompatEditText
    lateinit var confirmPassword: AppCompatEditText
    lateinit var btnSubmit: Button
    lateinit var oldPasswordText: String
    lateinit var newPasswordText: String
    lateinit var confirmPasswordText: String
    lateinit var interfaceObj: ApiInterface
    lateinit var token: String
    lateinit var sharedPref: SharedPreferences
    var status: Int? = null
    lateinit var tvName: TextView
    var name: String = ""
    var themes: Int = R.style.DefaultMedium
    lateinit var themePreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        themes = themePreference.getInt("theme", R.style.DefaultMedium)
        val defaultNightMode = themePreference.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        getDelegate().setLocalNightMode(defaultNightMode)
        this.setTheme(themes)
        setContentView(R.layout.activity_change_password)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)

        tvName = findViewById(R.id.username)
        oldPassword = findViewById(R.id.et_old_password)
        newPassword = findViewById(R.id.et_new_password)
        confirmPassword = findViewById(R.id.et_confirm_pass)
        btnSubmit = findViewById(R.id.btn_enter)
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        sharedPref = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        token = sharedPref.getString("token value", "")
        name = sharedPref.getString("user name", "")
        tvName.setText(name)
        btnSubmit.setOnClickListener {
            oldPasswordText = oldPassword.text.toString()
            newPasswordText = newPassword.text.toString()
            confirmPasswordText = confirmPassword.text.toString()
            changePassword()
        }
    }

    fun changePassword() {
        var call: Call<ChangePasswordData> = interfaceObj.changePassword(token, newPasswordText, confirmPasswordText, oldPasswordText)
        try {
            call.enqueue(object : Callback<ChangePasswordData> {
                override fun onFailure(call: Call<ChangePasswordData>, t: Throwable) {
                }

                override fun onResponse(call: Call<ChangePasswordData>, response: Response<ChangePasswordData>) {

                    status = response.body()?.header?.status
                    if (status == 1) {
                        val resultMessage: String? = response.body()?.body?.Msg
                    } else if (status == 0) {

                        var resultFailed = response.body()?.errors?.Msg
                    }
                }
            })
        } catch (e: Throwable) {

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