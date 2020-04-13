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
import com.fafadiatech.newscout.appconstants.showMessage
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.fafadiatech.newscout.viewmodel.ViewModelProviderFactory
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.HTTP
import java.text.SimpleDateFormat
import java.util.*

class CommentsActivity : BaseActivity() {

    lateinit var nApi: ApiInterface
    lateinit var ivCaptcha: ImageView
    lateinit var dataVM: FetchDataApiViewModel
    lateinit var comAdapter: CommentAdapter
    lateinit var rvComments: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var emptyText: LinearLayout
    lateinit var btnSend: Button
    lateinit var etCaptcha: EditText
    lateinit var etComment: EditText
    lateinit var ibtnRefresh: ImageButton
    lateinit var tvTitle: TextView
    lateinit var tvSource: TextView
    lateinit var tvTime: TextView
    lateinit var ivNews: ImageView
    var strDate: String? = null
    var capData = MutableLiveData<CaptchaData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        dataVM = ViewModelProviders.of(this@CommentsActivity, ViewModelProviderFactory(application, "")).get(FetchDataApiViewModel::class.java)

        nApi = ApiClient.getClient().create(ApiInterface::class.java)

        val token = themePreference.getString("token value", "")

        val data: DetailNewsData = intent.getParcelableExtra("data")

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

        getCaptchaImage(token)

        getAllComments(data.article_id)

        btnSend.setOnClickListener(View.OnClickListener gotoTop@{
            val comment = etComment.text.toString()
            if(etCaptcha.text.toString().isNullOrBlank()){
                showMessage(this@CommentsActivity, "Please enter captcha.", 900)
                return@gotoTop
            }

            if(comment.isNullOrBlank()){
                showMessage(this@CommentsActivity, "Please enter comment.", 900)
                return@gotoTop
            }

            if (comment.isNotBlank() && capData.value != null) {
                val checkInternet = MyApplication.checkInternet
                if (!checkInternet) {
                    showMessage(this@CommentsActivity, "No Internet Connection.", 900)
                    return@gotoTop
                }
                //call API
                val cData = capData.value
                dataVM.sendComment(token, data.article_id, comment, etCaptcha.text.toString(), cData!!.new_captch_key)

                // display progressbar

            }
        })

        dataVM.postComment.observe(this, Observer {
            it?.let {
                try {
                    var resCode = it.code()
                    if (resCode >= 200 && resCode < 400) {
                        val status = it.body()?.header?.status
                        if (status == 1) {
                            if (it.body() != null) {
                                Log.d("Comment Fetch", "Code : " + resCode)
                                getAllComments(data.article_id)
                                Toast.makeText(this@CommentsActivity, "Comment posted successfully.", Toast.LENGTH_LONG).show()
                            }
                        } else {

                            it.body()?.errors.let {
                                val errMsg = it?.error
                                showMessage(this@CommentsActivity, errMsg?:"Something went wrong, Please try again.", 200)
                            }
                            showMessage(this@CommentsActivity,
                                     "Something went wrong, Please try again", 900)

                        }
                    } else if(resCode == 401){
                        val converter = ApiClient.getClient().responseBodyConverter<CommentPostErrorData>(
                                CommentPostErrorData::class.java, arrayOfNulls<Annotation>(0))
                        var errorResponse: CommentPostErrorData?
                        errorResponse = converter?.convert(it.errorBody())
                        val status = errorResponse?.header?.status
                        var errMsg = errorResponse?.errors?.Msg?.get(0)?:" Article Id not entered."

                        showMessage(this@CommentsActivity, errMsg, 900)
                    } else{
                        showMessage(this@CommentsActivity, "Something went wrong, Please try again", 900)
                    }
                } catch (e: java.lang.Exception) {
                    showMessage(this@CommentsActivity, "Something went wrong, Please try again", 900)
                }


                // hide progressbar

                etComment.setText("")

                etCaptcha.setText("")

                getCaptchaImage(token)
            }
        })

        ibtnRefresh.setOnClickListener {
            getCaptchaImage(token)
        }
    }

    fun getCaptchaImage(token: String) {

        val checkInternet = MyApplication.checkInternet
        if (!checkInternet) {
            showMessage(this@CommentsActivity, "No Internet Connection", 900)
            return
        }
        val cCall: Call<CaptchaResponseData> = nApi.getCaptchaText(token)
        cCall.enqueue(object : Callback<CaptchaResponseData> {
            override fun onFailure(call: Call<CaptchaResponseData>, t: Throwable) {
                showMessage(this@CommentsActivity, "No Internet Connection", 900)
            }

            override fun onResponse(call: Call<CaptchaResponseData>, response: Response<CaptchaResponseData>) {
                try {
                    var resCode = response.code()
                    if (response.body() != null) {

                        if (resCode == 200) {

                            var statusCode = response.body()!!.header.status
                            var data = response.body()!!.body.result
                            capData.postValue(data)
                            val status = data.status
                            val new_captch_key = data.new_captch_key
                            val new_captch_image = data.new_captch_image

                            var imageUrl = CURL + new_captch_image
                            Glide.with(this@CommentsActivity).load(imageUrl).apply(requestOptions)
                                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                    .thumbnail(0.1f)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.image_not_found)
                                    .error(R.drawable.image_not_found)
                                    .into(ivCaptcha)

                        } else {
                            showMessage(this@CommentsActivity, "No data found", 900)
                        }

                    } else {

                        if (resCode == 401) {
                            showMessage(this@CommentsActivity, "Please login to send comment", 401)

                        } else if (resCode == 404) {
                            showMessage(this@CommentsActivity, "No data found, please try again.", 900)

                        } else if (resCode == 500) {
                            showMessage(this@CommentsActivity, "No data found, please try again.", 900)

                        } else {
                            showMessage(this@CommentsActivity, "No data found", 900)
                        }
                        showMessage(this@CommentsActivity, "No data found", 900)
                    }
                } catch (e: Exception) {
                    showMessage(this@CommentsActivity, "No data found", 900)
                }

            }
        })
    }

    fun getAllComments(articleId: Int) {
        val checkInternet = MyApplication.checkInternet
        if (!checkInternet) {
            showMessage(this@CommentsActivity, "No Internet Connection", 900)
            return
        }
        dataVM.getAllComment(articleId).observe(this, Observer { commentList ->

            Log.d("CommentActivity", "Size : " + commentList?.size)

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