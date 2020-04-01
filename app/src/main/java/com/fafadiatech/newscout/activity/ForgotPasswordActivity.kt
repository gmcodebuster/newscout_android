package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.model.ForgotPasswordData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : BaseActivity() {

    lateinit var nApi: ApiInterface
    lateinit var emailText: String
    var status: Int? = null
    lateinit var pBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forgot_password)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        var edTextEmail = findViewById<AppCompatEditText>(R.id.et_email)

        var btnSubmit = findViewById<Button>(R.id.btn_enter_forgot_pass)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)

        pBar = findViewById(R.id.progressBar)

        btnSubmit.setOnClickListener {
            emailText = edTextEmail.text.toString()
            pBar.visibility = View.VISIBLE
            forgotPassword()
        }
    }

    fun forgotPassword() {

        var call: Call<ForgotPasswordData> = nApi.forgotPassword(emailText)
        try {

            call.enqueue(object : Callback<ForgotPasswordData> {
                override fun onFailure(call: Call<ForgotPasswordData>, t: Throwable) {
                    //Display dialog for internet error
                    //dismiss progress bar
                    pBar.visibility = View.GONE
                    showMessage("Something went wrong, Please try again.")
                }

                override fun onResponse(call: Call<ForgotPasswordData>, response: Response<ForgotPasswordData>) {
                    pBar.visibility=View.GONE
                    val code = response.code()
                    if(code in 200..299){
                        status = response.body()?.header?.status
                        if (status == 1) {
                            var resultSuccess = response.body()?.body?.Msg
                            //Display Dialog
                            showMessage(resultSuccess.toString(), code)
                            //dismiss progress bar
                            var deviceId = themePreference.getString("device_token", "")
                            val sessionId = getUniqueCode(this@ForgotPasswordActivity, themePreference)
                            trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.FORGOTPASSWORD.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

                        } else if (status == 0) {
                            //Display Dialog
                            showMessage("Something went wrong, Please try again.")
                        }
                    }else if(code in 400..499){
                        //Display Dialog
                        showMessage("Something went wrong, Please try again.")
                    } else if(code >= 500){
                        //Display Dialog
                        showMessage("Something went wrong, Please try again.")
                    }

                }
            })
        } catch (e: Throwable) {
            pBar.visibility = View.GONE
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

    fun showMessage(msg:String){
        val msgDialog = AlertDialog.Builder(this@ForgotPasswordActivity)
                .setTitle("Newscout")
                .setMessage(msg)
                .setIcon(R.mipmap.ic_newscout_launcher)
                .setPositiveButton("OK", DialogInterface.OnClickListener{
                    dialog, id -> return@OnClickListener
                })
                .create()

        msgDialog.show()
    }


    fun showMessage(msg:String, code:Int){
        val msgDialog = AlertDialog.Builder(this@ForgotPasswordActivity)
                .setTitle("Newscout")
                .setMessage(msg)
                .setIcon(R.mipmap.ic_newscout_launcher)
                .setPositiveButton("OK", DialogInterface.OnClickListener{
                    dialog, id -> (
                        if(code in 200..299){
                            finish()
                        })
                })
                .create()

        msgDialog.show()
    }
}