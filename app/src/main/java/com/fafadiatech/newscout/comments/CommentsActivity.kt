package com.fafadiatech.newscout.comments

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.BaseActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.URL
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.fafadiatech.newscout.viewmodel.ViewModelProviderFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentsActivity : BaseActivity(){

    lateinit var nApi: ApiInterface
    lateinit var ivCaptcha : ImageView
    lateinit var dataVM: FetchDataApiViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        dataVM = ViewModelProviders.of(this@CommentsActivity, ViewModelProviderFactory(application, "")).get(FetchDataApiViewModel::class.java)

        nApi = ApiClient.getClient().create(ApiInterface::class.java)
//Check user is login or not
        val token = themePreference.getString("token value", "")
        val checkInternet = MyApplication.checkInternet

        val data:DetailNewsData = intent.getParcelableExtra("data")

        ivCaptcha = findViewById(R.id.iv_captcha)

        if(checkInternet) {
            getCaptchaImage(token)
        }

        getAllComments(token, data.article_id)


    }

    fun getCaptchaImage(token:String){
        val cCall: Call<CaptchaResponseData> = nApi.getCaptchaText(token)
        cCall.enqueue(object: Callback<CaptchaResponseData> {
            override fun onFailure(call: Call<CaptchaResponseData>, t: Throwable) {
                Log.d("CaptchaActivity", "Error : "+t.message)
            }

            override fun onResponse(call: Call<CaptchaResponseData>, response: Response<CaptchaResponseData>) {
                //update mutable object
                //update imageview
                Log.d("CaptchaActivity", "Data Success : ")
                if (response.body() != null) {

                    var data = response.body()!!.body.result

                    val status = data.status
                    val new_captch_key = data.new_captch_key
                    val new_captch_image = data.new_captch_image

                    Log.d("CaptchaActivity", "Data : "+new_captch_key)

                    var imageUrl = URL + new_captch_image
                    Glide.with(this@CommentsActivity).load(imageUrl).apply(requestOptions)
                            .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.image_not_found)
                            .error(R.drawable.image_not_found)
                            .into(ivCaptcha)
                }

            }
        })
    }

    fun getAllComments(token:String, articleId:Int){
        dataVM.getAllComment("",1).observe(this, Observer{ commentList ->

            Log.d("CommentActivity", "Size : "+commentList.size)

            //set adapter here

        })
    }
}