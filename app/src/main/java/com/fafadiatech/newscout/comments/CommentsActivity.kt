package com.fafadiatech.newscout.comments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.BaseActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.appconstants.URL
import com.fafadiatech.newscout.appconstants.getImageURL
import com.fafadiatech.newscout.application.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class CommentsActivity : BaseActivity(){

    lateinit var nApi: ApiInterface
    lateinit var ivCapcha : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)


        nApi = ApiClient.getClient().create(ApiInterface::class.java)
//Check user is login or not
        val token = themePreference.getString("token value", "")
        val checkInternet = MyApplication.checkInternet

        ivCapcha = findViewById(R.id.iv_capcha)

        if(checkInternet) {
            getCapchaImage(token)
        }

    }

    fun getCapchaImage(token:String){
        val cCall: Call<CapchaResponseData> = nApi.getCaptchaText(token)
        cCall.enqueue(object: Callback<CapchaResponseData> {
            override fun onFailure(call: Call<CapchaResponseData>, t: Throwable) {
                Log.d("CapchaActivity", "Error : "+t.message)
            }

            override fun onResponse(call: Call<CapchaResponseData>, response: Response<CapchaResponseData>) {
                //update mutable object
                //update imageview
                Log.d("CapchaActivity", "Data Success : ")
                if (response.body() != null) {

                    var data = response.body()!!.body.result

                    val status = data.status
                    val new_captch_key = data.new_captch_key
                    val new_captch_image = data.new_captch_image

                    Log.d("CapchaActivity", "Data : "+new_captch_key)

                    var imageUrl = URL + new_captch_image
                    Glide.with(this@CommentsActivity).load(imageUrl).apply(requestOptions)
                            .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.image_not_found)
                            .error(R.drawable.image_not_found)
                            .into(ivCapcha)
                }

            }
        })
    }
}