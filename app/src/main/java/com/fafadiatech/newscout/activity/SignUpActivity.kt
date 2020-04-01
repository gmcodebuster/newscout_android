package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.customcomponent.BaseAlertDialog
import com.fafadiatech.newscout.model.SignUpErrorData
import com.fafadiatech.newscout.model.SignUpMessageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : BaseActivity() {

    lateinit var etFirstName: AppCompatEditText
    lateinit var etLastName: AppCompatEditText
    lateinit var etEmail: AppCompatEditText
    lateinit var etPassWord: AppCompatEditText
    lateinit var etRePassWord: AppCompatEditText
    lateinit var btnRegister: Button
    lateinit var strFirstName: String
    lateinit var strLastName: String
    lateinit var strEmail: String
    lateinit var strPassword: String
    lateinit var nApi: ApiInterface
    var status: Int? = null
    var isNetwork: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        val login = findViewById<TextView>(R.id.txt_already_member)
        login.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        etFirstName = findViewById(R.id.ed_first_name)
        etLastName = findViewById(R.id.ed_last_name)
        etEmail = findViewById(R.id.ed_enter_email)
        etPassWord = findViewById(R.id.ed_password)
        etRePassWord = findViewById(R.id.retype_ed_password)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            strFirstName = etFirstName.text.toString()
            strLastName = etLastName.text.toString()
            strEmail = etEmail.text.toString()
            strPassword = etPassWord.text.toString()
            if (isNetwork) {
                signupThroughApi()
            } else {
                BaseAlertDialog.showAlertDialog(this, resources.getString(R.string.no_signup_msg))
            }

        }
    }

    fun signupThroughApi() {
        try {
            var call: Call<SignUpMessageData> = nApi.signUpByApi(strFirstName, strLastName, strEmail, strPassword)
            call.enqueue(object : Callback<SignUpMessageData> {

                override fun onResponse(call: Call<SignUpMessageData>, response: Response<SignUpMessageData>) {
                    var responseCode = response.code()
                    if (responseCode >= 200 && responseCode < 400) {
                        var deviceId = themePreference.getString("device_token", "")
                        val sessionId = getUniqueCode(this@SignUpActivity, themePreference)
                        signupTrackingCallback(nApi, themePreference, ActionType.SIGNUP.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, strFirstName?:"", strLastName?:"", "", strEmail)
                        status = response.body()?.header?.status
                        var result: String = response.body()?.body!!.Msg
                        Toast.makeText(this@SignUpActivity, result, Toast.LENGTH_SHORT).show()
                        var intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val converter = ApiClient.getClient().responseBodyConverter<SignUpErrorData>(
                                SignUpErrorData::class.java, arrayOfNulls<Annotation>(0))

                        var errorResponse: SignUpErrorData?
                        errorResponse = converter?.convert(response.errorBody())
                        status = errorResponse?.header?.status
                        var resultFailed = errorResponse?.errors?.errorList!![0].field_error

                        Toast.makeText(this@SignUpActivity, "Signup Error, Please try again", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SignUpMessageData>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, "Signup Error, Please try again", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Throwable) {
            Toast.makeText(this@SignUpActivity, "Signup Error, Please try again", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        super.onNetworkConnectionChanged(isConnected)
        isNetwork = isConnected
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }
}

