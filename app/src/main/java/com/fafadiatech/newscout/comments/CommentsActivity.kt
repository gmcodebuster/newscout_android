package com.fafadiatech.newscout.comments

import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.BaseActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.CURL
import com.fafadiatech.newscout.appconstants.getImageURL
import com.fafadiatech.newscout.appconstants.setColorForPath
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.fafadiatech.newscout.viewmodel.ViewModelProviderFactory
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CommentsActivity : BaseActivity(){

    lateinit var nApi: ApiInterface
    lateinit var ivCaptcha : ImageView
    lateinit var dataVM: FetchDataApiViewModel
    lateinit var comAdapter: CommentAdapter
    lateinit var rvComments : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager
    lateinit var emptyText : LinearLayout
    lateinit var btnSend : Button
    lateinit var etCaptcha: EditText
    lateinit var etComment : EditText
    lateinit var ibtnRefresh : ImageButton
    lateinit var tvTitle : TextView
    lateinit var tvSource : TextView
    lateinit var tvTime : TextView
    lateinit var ivNews : ImageView
    var strDate: String? = null
    var capData = MutableLiveData<CaptchaData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        dataVM = ViewModelProviders.of(this@CommentsActivity, ViewModelProviderFactory(application, "")).get(FetchDataApiViewModel::class.java)

        nApi = ApiClient.getClient().create(ApiInterface::class.java)

        val token = themePreference.getString("token value", "")
        val checkInternet = MyApplication.checkInternet
        val data:DetailNewsData = intent.getParcelableExtra("data")

        ivCaptcha = findViewById(R.id.iv_captcha)
        rvComments = findViewById(R.id.rv_comments)
        emptyText = findViewById(R.id.empty_message)
        btnSend = findViewById(R.id.btn_send)
        etCaptcha = findViewById(R.id.et_captcha)
        etComment = findViewById(R.id.et_comment)
        ibtnRefresh = findViewById(R.id.imgbtn_refresh)
        tvTitle = findViewById(R.id.tv_title)
        tvSource = findViewById(R.id.tv_source)
        tvTime = findViewById(R.id.tv_time)
        ivNews = findViewById(R.id.news_image)

        emptyText.visibility = View.INVISIBLE
        layoutManager = LinearLayoutManager(this@CommentsActivity, VERTICAL, false)

        comAdapter = CommentAdapter(this@CommentsActivity)
        rvComments.layoutManager = layoutManager
        rvComments.adapter = comAdapter

        tvTitle.text = data.title

        if (data.published_on != null) {
            var timeAgo: String = ""
            try {
                  strDate = data.published_on

                if (strDate?.endsWith("Z", false) == false) {
                    strDate += "Z"
                }

                var timeZone = Calendar.getInstance().timeZone.id
                var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                dateformat.timeZone = TimeZone.getTimeZone("UTC")
                var date = dateformat.parse(strDate)
                dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                dateformat.format(date)
                timeAgo = TimeAgo.using(date.time, TimeAgoMessages.Builder().defaultLocale().build())
                tvTime.text = timeAgo
            } catch (e: Exception) {
                try {
                    var timeZone = Calendar.getInstance().timeZone.id
                    var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                    dateformat.timeZone = TimeZone.getTimeZone("UTC")
                    var date = dateformat.parse(strDate)
                    dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                    dateformat.format(date)
                    timeAgo = TimeAgo.using(date.time, TimeAgoMessages.Builder().defaultLocale().build())
                    tvTime.text = timeAgo
                } catch (exception: Exception) {
                    tvTime.text = ""
                }
            }
        } else {

        }


        if (data.cover_image != null && data.cover_image.length > 0) {
            var imageUrl = getImageURL(ivNews, data.cover_image)
            Glide.with(this@CommentsActivity).load(imageUrl).apply(requestOptions)
                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.image_not_found)
                    .error(R.drawable.image_not_found)
                    .into(ivNews)
        } else {

            Glide.with(this@CommentsActivity).load(R.drawable.image_not_found).apply(requestOptions)
                    .into(ivNews)
        }

        if (data.source != null) {
            var spannable = SpannableString(" via " + data.source)
            setColorForPath(spannable, arrayOf(data.source), ContextCompat.getColor(this@CommentsActivity, R.color.colorPrimary))
            tvSource.text = spannable
        }

        if(checkInternet) {
            getCaptchaImage(token)
        }

        getAllComments(data.article_id)


        btnSend.setOnClickListener(View.OnClickListener {
            val comment = etComment.text.toString()
            if(comment.isNotBlank() && capData.value != null){
                //call API
                val cData = capData.value
                 dataVM.sendComment(token, data.article_id, comment, etCaptcha.text.toString(),cData!!.new_captch_key)

                // display progressbar
                //update recyclerview
            }
        })

        dataVM.postComment.observe(this, Observer {
            it?.let {
                // hide progressbar
                // Display Toast message sucess /
                Toast.makeText(this@CommentsActivity, "Comment posted successfully.", Toast.LENGTH_LONG
                ).show()
                //blank etComment
                etComment.setText("")
                //blank etCaptcha
                etCaptcha.setText("")
                //refresh captcha
                getCaptchaImage(token)
                //update recyclerview
                getAllComments(data.article_id)
            }
        })

        ibtnRefresh.setOnClickListener {
            getCaptchaImage(token)
        }
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
                    capData.postValue(data)
                    val status = data.status
                    val new_captch_key = data.new_captch_key
                    val new_captch_image = data.new_captch_image

                    Log.d("CaptchaActivity", "Data : "+new_captch_key)

                    var imageUrl = CURL + new_captch_image
                    Glide.with(this@CommentsActivity).load(imageUrl).apply(requestOptions)
                            .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.image_not_found)
                            .error(R.drawable.image_not_found)
                            .into(ivCaptcha)
                }else{
                    Log.d("CaptchaActivity", "Data Fail : ")
                }

            }
        })
    }

    fun getAllComments(articleId:Int){
        dataVM.getAllComment(articleId).observe(this, Observer{ commentList ->

            Log.d("CommentActivity", "Size : "+commentList?.size)

            showEmptyList(commentList == null)

            comAdapter.submitList(commentList)

        })

    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            emptyText.visibility = View.VISIBLE
            rvComments.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            rvComments.visibility = View.VISIBLE
        }
    }
}