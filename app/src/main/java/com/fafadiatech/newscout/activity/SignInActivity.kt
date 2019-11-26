package com.fafadiatech.newscout.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProviders
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.customcomponent.BaseAlertDialog
import com.fafadiatech.newscout.model.*
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class SignInActivity : BaseActivity() {

    lateinit var interfaceObj: ApiInterface
    lateinit var emailText: String
    lateinit var passwordText: String
    var status: Int? = null
    lateinit var txtViewcreateAccount: TextView
    lateinit var email: AppCompatEditText
    private var mCallbackManager: CallbackManager? = null
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 9001
    var success: Int = 0
    lateinit var btnSignIn: Button
    lateinit var password: AppCompatEditText
    var categoryListServer = ArrayList<String>()
    var isNetwork: Boolean = false
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    var position: Int = 0
    var token: String? = ""
    lateinit var btnLoginGoogle: SignInButton
    val EMAIL = "email"
    val USER_POSTS = "user_posts"
    val AUTH_TYPE = "rerequest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchDataViewModel = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)

        position = intent.getIntExtra("detail_news_item_position", 0)
        setContentView(R.layout.activity_sign_in)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)
        var fontTypeface = Typeface.createFromAsset(assets, "HelveticaNeueMed.ttf")

        mCallbackManager = CallbackManager.Factory.create()
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        email = findViewById<AppCompatEditText>(R.id.ed_enter_email_signIn)
        password = findViewById<AppCompatEditText>(R.id.ed_password_signIn)
        btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        var btnForgotPassword = findViewById<Button>(R.id.btn_forgot_password)
        val btnLoginFacebook = findViewById<LoginButton>(R.id.btn_login_facebook)
        btnLoginGoogle = findViewById<SignInButton>(R.id.btn_login_google)
        txtViewcreateAccount = findViewById(R.id.create_Account)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestIdToken(getString(R.string.web_client_id)).build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        btnSignIn.setOnClickListener {
            emailText = email.text.toString()
            passwordText = password.text.toString()

            if (isNetwork == true) {
                signIn()
            } else {
                BaseAlertDialog.showAlertDialog(this, resources.getString(R.string.no_signin_msg))
            }
        }

        btnForgotPassword.setOnClickListener {
            var intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        txtViewcreateAccount.setOnClickListener {
            val startIntent = Intent(this, SignUpActivity::class.java)
            startActivity(startIntent)
        }

        btnLoginFacebook.setReadPermissions(Arrays.asList(EMAIL, USER_POSTS))
        btnLoginFacebook.authType = AUTH_TYPE

        btnLoginFacebook.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {

                var request = GraphRequest.newMeRequest(loginResult.accessToken, object : GraphRequest.GraphJSONObjectCallback {
                    override fun onCompleted(jsonObject: JSONObject, response: GraphResponse?) {
                        if (response!!.error != null) {

                        } else {
                            var email = jsonObject.optString("email")
                            val editor = themePreference.edit()
                            editor.putString("login success", email)
                            editor.commit()
                        }
                    }
                })

                var param = Bundle()
                param.putString("fields", "id,email,name")
                request.parameters = param
                request.executeAsync()

                var facebookToken = loginResult.accessToken.token
                var deviceId: String = themePreference.getString("device_token", "")
                var call: Call<MessageLoginData> = interfaceObj.loginBySocial("facebook", deviceId, "android", facebookToken)
                call.enqueue(object : Callback<MessageLoginData> {
                    override fun onFailure(call: Call<MessageLoginData>, t: Throwable) {

                    }

                    override fun onResponse(call: Call<MessageLoginData>, response: Response<MessageLoginData>) {
                        var responseCode = response.code()
                        if (responseCode >= 200 && responseCode <= 399) {

                            var token = response.body()!!.body.user.token
                            token = "Token " + token
                            fetchDataViewModel.startVoteServerDataWorkManager(token)
                            var firstName = response.body()?.body?.user?.first_name
                            var lastName = response.body()?.body?.user?.last_name
                            var userName = firstName + " " + lastName

                            var editor = themePreference.edit()
                            editor.putString("token value", token)
                            editor.putString("user name", userName)
                            editor.commit()

                            var intent = Intent()
                            getPositionFromIntent(intent)
                        } else {

                        }
                    }
                })
            }

            override fun onCancel() {

            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(this@SignInActivity, "Facebook Signin Error, Please try again", Toast.LENGTH_SHORT).show()
            }
        })

        btnLoginGoogle.setOnClickListener {
            if (success == 0) {
                signInWithGoogle()
                success = 1
            } else {
                signOutGoogle()
                success = 0
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun signOutGoogle() {

        mGoogleSignInClient.signOut()
    }

    fun signIn() {
        try {
            var call: Call<MessageLoginData> = interfaceObj.loginByApi(emailText, passwordText, AppConstant.DEVICE_ID, AppConstant.DEVICE_NAME)
            call.enqueue(object : Callback<MessageLoginData> {
                override fun onFailure(call: Call<MessageLoginData>, t: Throwable) {

                }

                override fun onResponse(call: Call<MessageLoginData>, response: Response<MessageLoginData>) {
                    var responseCode = response.code()
                    if (responseCode >= 200 && responseCode < 400) {
                        status = response.body()?.header?.status
                        Toast.makeText(this@SignInActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        token = response.body()?.body?.user?.token
                        val editor = themePreference.edit()
                        token = "Token " + token
                        fetchDataViewModel.startVoteServerDataWorkManager(token!!)
                        editor.putString("token value", token)
                        editor.putString("login success", emailText)
                        var firstName = response.body()?.body?.user?.first_name
                        var lastName = response.body()?.body?.user?.last_name
                        var savedCategoryList: ArrayList<UserPassionData> = response.body()?.body?.user?.passion!!
                        for (i in 0 until savedCategoryList.size) {
                            var entity = savedCategoryList.get(i)
                            categoryListServer.add(entity.name)
                        }

                        var gson = Gson()
                        var json: String = gson.toJson(categoryListServer)
                        var userName = firstName + " " + lastName
                        editor.putString("user name", userName)
                        editor.putString("saved_category_list", json)
                        editor.commit()
                        var intent = Intent()
                        getPositionFromIntent(intent)
                    } else {
                        if (email.text.toString() == "" || password.text.toString() == "") {
                            Toast.makeText(this@SignInActivity, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                        } else {
                            val converter = ApiClient.getClient().responseBodyConverter<LoginErrorData>(
                                    LoginErrorData::class.java, arrayOfNulls<Annotation>(0))

                            var errorResponse: LoginErrorData?
                            errorResponse = converter?.convert(response.errorBody())
                            status = errorResponse?.header?.status
                            var resultFailed = errorResponse?.errors?.invalid_credentials

                            Toast.makeText(this@SignInActivity, "Signin Error, Please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Throwable) {
            Toast.makeText(this@SignInActivity, "Signin Error, Please try again", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            var webToken = account!!.idToken
            var email = account.email
            var editor = themePreference.edit()
            editor.putString("login success", email)
            editor.commit()
            var deviceId = themePreference.getString("device_token", "")

            Toast.makeText(this, "Login Successful using Google", Toast.LENGTH_SHORT).show()
            var call: Call<MessageLoginData> = interfaceObj.loginBySocial("google", deviceId, "android", webToken!!)
            call.enqueue(object : Callback<MessageLoginData> {
                override fun onFailure(call: Call<MessageLoginData>, t: Throwable) {

                }

                override fun onResponse(call: Call<MessageLoginData>, response: Response<MessageLoginData>) {

                    var token = response.body()!!.body.user.token
                    token = "Token " + token
                    fetchDataViewModel.startVoteServerDataWorkManager(token)
                    var firstName = response.body()?.body?.user?.first_name
                    var lastName = response.body()?.body?.user?.last_name
                    var userName = firstName + " " + lastName

                    var editor = themePreference.edit()
                    editor.putString("token value", token)
                    editor.putString("user name", userName)
                    editor.commit()
                    var intent = Intent()
                    getPositionFromIntent(intent)
                }
            })
        } catch (e: ApiException) {
            Toast.makeText(this, "Login failed using Google", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        super.onNetworkConnectionChanged(isConnected)
        isNetwork = isConnected

        if (isConnected) {

        } else {

        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    fun getLikedListFromServer(token: String) {
        var call: Call<VoteArticleDataServer> = interfaceObj.getLikedListFromServer(token)
        call.enqueue(object : Callback<VoteArticleDataServer> {
            override fun onFailure(call: Call<VoteArticleDataServer>, t: Throwable) {
            }

            override fun onResponse(call: Call<VoteArticleDataServer>, response: Response<VoteArticleDataServer>) {

                var responseCode = response.code()
                if (responseCode == 200) {
                    var list: ArrayList<VoteDetailData> = response.body()?.body?.results!!
                }
            }
        })
    }

    override fun onBackPressed() {
        var intent = Intent()
        getPositionFromIntent(intent)
    }

    fun getPositionFromIntent(intent: Intent) {

        if (intent != null) {
            intent.putExtra("news_item_position", position)
            setResult(Activity.RESULT_OK, intent)
        }

        finish()
    }
}