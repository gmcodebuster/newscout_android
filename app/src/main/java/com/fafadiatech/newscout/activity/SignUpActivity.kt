package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.customcomponent.BaseAlertDialog
import com.fafadiatech.newscout.model.SignUpErrorData
import com.fafadiatech.newscout.model.SignUpMessageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : BaseActivity() {

    lateinit var firstName: EditText
    lateinit var lastName: EditText
    lateinit var userEmail: EditText
    lateinit var userPassWord: EditText
    lateinit var retypePassword: EditText
    lateinit var btnRegister: Button
    lateinit var firstNameText: String
    lateinit var lastNameText: String
    lateinit var userEmailText: String
    lateinit var userPasswordText: String
    lateinit var apiInterfaceSignUp: ApiInterface
    var status: Int? = null
    lateinit var themePreference: SharedPreferences
    var isNetwork: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        var themes: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        this.setTheme(themes)
        setContentView(R.layout.activity_sign_up)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)
        apiInterfaceSignUp = ApiClient.getClient().create(ApiInterface::class.java)
        val login = findViewById<TextView>(R.id.txt_already_member)
        login.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        firstName = findViewById(R.id.ed_first_name)
        lastName = findViewById(R.id.ed_last_name)
        userEmail = findViewById(R.id.ed_enter_email)
        userPassWord = findViewById(R.id.ed_password)
        retypePassword = findViewById(R.id.retype_ed_password)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            firstNameText = firstName.text.toString()
            lastNameText = lastName.text.toString()
            userEmailText = userEmail.text.toString()
            userPasswordText = userPassWord.text.toString()
            if (isNetwork) {
                signupThroughApi()
            } else {
                BaseAlertDialog.showAlertDialog(this, resources.getString(R.string.no_signup_msg))
            }

        }
    }

    fun signupThroughApi() {
        try {
            var call: Call<SignUpMessageData> = apiInterfaceSignUp.signUpByApi(firstNameText, lastNameText, userEmailText, userPasswordText)
            call.enqueue(object : Callback<SignUpMessageData> {

                override fun onResponse(call: Call<SignUpMessageData>, response: Response<SignUpMessageData>) {
                    var responseCode = response.code()
                    if (responseCode >= 200 && responseCode < 400) {
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

