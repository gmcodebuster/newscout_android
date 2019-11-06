package com.fafadiatech.newscout.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.model.ForgotPasswordData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : BaseActivity() {

    lateinit var interfaceObj: ApiInterface
    lateinit var emailText: String
    var status: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        var edTextEmail = findViewById<EditText>(R.id.et_email)

        var btnSubmit = findViewById<Button>(R.id.btn_enter_forgot_pass)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)

        btnSubmit.setOnClickListener {
            emailText = edTextEmail.text.toString()
            forgotPassword()
        }
    }

    fun forgotPassword() {

        var call: Call<ForgotPasswordData> = interfaceObj.forgotPassword(emailText)
        try {
            call.enqueue(object : Callback<ForgotPasswordData> {
                override fun onFailure(call: Call<ForgotPasswordData>, t: Throwable) {
                }

                override fun onResponse(call: Call<ForgotPasswordData>, response: Response<ForgotPasswordData>) {

                    status = response.body()?.header?.status
                    if (status == 1) {
                        var resultSuccess = response.body()?.body?.Msg
                    } else if (status == 0) {

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