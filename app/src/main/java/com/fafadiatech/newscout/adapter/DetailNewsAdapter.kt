package com.fafadiatech.newscout.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.net.Uri
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.NewsWebActivity
import com.fafadiatech.newscout.activity.SignInActivity
import com.fafadiatech.newscout.activity.SourceActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.customcomponent.BaseAlertDialog
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.interfaces.ITopNews
import com.fafadiatech.newscout.model.BookmarkArticleData
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_intro_screen.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.floor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetailNewsAdapter(val context: Context) : PagerAdapter() {

    var mLayoutInflater: LayoutInflater
    var interfaceObj: ApiInterface
    var isBookmark: Int = 0
    var isLike: Int = 1
    lateinit var rootLayout: ConstraintLayout
    var detailList = ArrayList<DetailNewsData>()
    lateinit var imgBtnShare: ImageButton
    lateinit var token: String
    var newsId: Int = 1
    lateinit var themePreference: SharedPreferences
    lateinit var gson: Gson
    var categoryName: String = ""
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    var newHeadingHeightInDp: Int = 0
    var deviceHeightInDp: Float = 0f
    var imageHeightInDp: Int = 0
    lateinit var mResources: Resources
    var isMoreStoriesUp: Boolean = false
    lateinit var rvSuggestedNews: RecyclerView
    lateinit var imgBtnBookmark: ImageView
    lateinit var imgBtnShuffle: ImageButton
    lateinit var newsDao: NewsDao
    private var newsDatabase: NewsDatabase? = null
    var height: Int = 0
    var requestOptions: RequestOptions? = null
    lateinit var itopNews: ITopNews

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        themePreference = context.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        fetchDataViewModel = ViewModelProviders.of(context as FragmentActivity).get(FetchDataApiViewModel::class.java)
        newsDatabase = NewsDatabase.getInstance(context)
        newsDao = newsDatabase!!.newsDao()
        requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).timeout(5000)
        itopNews = context as ITopNews
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun getCount(): Int {
        if (detailList == null) {
            return 0
        }
        return detailList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val displayMetrics = DisplayMetrics()
        val windowmanager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        val deviceHeight = displayMetrics.heightPixels
        deviceHeightInDp = deviceHeight / Resources.getSystem().getDisplayMetrics().density
        var deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        mResources = context.resources  
        val imageHeightInPixel = deviceHeight / (16 / 9)
        imageHeightInDp = convertPxToDp(context, imageHeightInPixel)
        var themes: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        context.setTheme(themes)
        var isNightMode = themePreference.getBoolean("night mode enable", false)
        var view = mLayoutInflater.inflate(R.layout.news_detail_top_layout, container, false)
        var newsTopImage = view.findViewById<ImageView>(R.id.news_top_img_detail)
        var newsHeading = view.findViewById<TextView>(R.id.news_heading_detail)
        var newsDesc = view.findViewById<TextView>(R.id.news_desc_detail)
        var newsSource = view.findViewById<TextView>(R.id.news_source_detail)
        var newsTime = view.findViewById<TextView>(R.id.news_time_detail)
        imgBtnBookmark = view.findViewById<ImageButton>(R.id.img_btn_bookmark_detail)
        var btnReadMore = view.findViewById<Button>(R.id.btn_read_more)
        var tvMoreStories = view.findViewById<TextView>(R.id.tv_more_stories)
        var btnTopNews = view.findViewById<ImageButton>(R.id.btn_top_news)
        var parentLayoutRvSuggested: ConstraintLayout = view.findViewById(R.id.parent_rv_suggested)
        var moreStoriesParent: ConstraintLayout = view.findViewById(R.id.bottom_layout_like_menu_bar)
        height = moreStoriesParent.layoutParams.height
        imgBtnShare = view.findViewById(R.id.btn_bottom_share_detail)
        rvSuggestedNews = view.findViewById<RecyclerView>(R.id.rv_suggested_news)
        rootLayout = view.findViewById(R.id.root_layout_detail_screen)
        imgBtnShuffle = view.findViewById(R.id.img_btn_shuffle)

        val layoutManagerHz = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        parentLayoutRvSuggested.visibility = View.INVISIBLE
        rvSuggestedNews.invalidate()
        var rvSuggestedAdapter = SuggestedNewsAdapter(context)
        rvSuggestedNews.layoutManager = layoutManagerHz
        rvSuggestedNews.adapter = rvSuggestedAdapter

        fetchDataViewModel = ViewModelProviders.of(context as FragmentActivity).get(FetchDataApiViewModel::class.java)

        newsId = detailList.get(position).article_id
        fetchDataViewModel.suggestedNews(newsId, 1).observe(context as LifecycleOwner, object: Observer<List<INews>> {
            override fun onChanged(list: List<INews>?) {
                val newsList = list as PagedList<DetailNewsData>
                rvSuggestedAdapter.submitList(newsList)

            }
        })
        if (isMoreStoriesUp) {
            isMoreStoriesUp = false
            slideTopStoriesDown(view, btnReadMore, tvMoreStories)

        }

        var suggestedNewsHeight = rvSuggestedNews.height
        gson = Gson()
        var imageObserver: ViewTreeObserver = newsHeading.getViewTreeObserver()

        imageObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = newsDesc.layoutParams as ConstraintLayout.LayoutParams

                var newsDescHeight = newsDesc.height - params.topMargin - params.bottomMargin
                var lineHeight = newsDesc.getPaint().getFontMetrics().bottom - newsDesc.getPaint().getFontMetrics().top

                if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val displayMetrics = DisplayMetrics()
                    val windowmanager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    windowmanager.defaultDisplay.getMetrics(displayMetrics)
                    val deviceWidth = displayMetrics.widthPixels
                    var deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
                    if (deviceWidthDp > 580 && deviceWidthDp < 620) {
                        var lineSpacingExtraInPx = convertDpToPx(context, 4)
                        lineHeight += lineSpacingExtraInPx
                    }
                }
                var lineCount = newsDescHeight / lineHeight
                newsDesc.maxLines = floor(lineCount.toDouble()).toInt()
                newsDesc.text = HtmlCompat.fromHtml(detailList.get(position).description, FROM_HTML_MODE_LEGACY, MImageGetter(newsDesc, context), null)

                if (detailList.get(position).cover_image.length > 0) {
                    var imageUrl = getImageURL(newsTopImage, detailList.get(position).cover_image)
                    Glide.with(context).load(imageUrl).apply(requestOptions!!)
                            .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                            .placeholder(R.drawable.image_not_found)
                            .error(R.drawable.image_not_found)
                            .into(newsTopImage)
                } else {
                    Glide.with(context).load(R.drawable.image_not_found).apply(requestOptions!!)
                            .into(newsTopImage);
                }

                newsHeading.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        newsHeading.text = detailList.get(position).title
        Log.d("DetailNewsAdapter", "Title : "+detailList.get(position).title + " id : "+detailList.get(position).article_id)
        newsDesc.text = detailList.get(position).description.replace("\n", "")
        if (detailList.get(position).source != null) {

            var spannable = SpannableString(" via " + detailList.get(position).source) as Spannable
            setColorForPath(spannable, arrayOf(detailList.get(position).source), ContextCompat.getColor(context, R.color.colorPrimary))
            newsSource.text = spannable
        }

        var dateString: String = detailList.get(position).published_on
        if (dateString != null) {
            var timeAgo: String = ""
            try {
                if (dateString?.endsWith("Z", false) == false) {
                    dateString += "Z"
                }

                var timeZone = Calendar.getInstance().timeZone.id
                var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                dateformat.timeZone = TimeZone.getTimeZone("UTC")
                var date = dateformat.parse(dateString)
                dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                dateformat.format(date)
                timeAgo = TimeAgo.using(date.time, TimeAgoMessages.Builder().defaultLocale().build())
                newsTime.text = timeAgo
            } catch (e: Exception) {

                try {
                    var timeZone = Calendar.getInstance().timeZone.id
                    var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                    dateformat.timeZone = TimeZone.getTimeZone("UTC")
                    var date = dateformat.parse(dateString)
                    dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                    dateformat.format(date)
                    timeAgo = TimeAgo.using(date.time, TimeAgoMessages.Builder().defaultLocale().build())
                    newsTime.text = timeAgo
                } catch (exception: Exception) {
                    newsTime.text = ""
                }
            }
        } else {
            newsTime.text = ""
        }

        isBookmark = detailList.get(position).bookmark_status
        if (isBookmark == 1) {
            imgBtnBookmark.setBackgroundResource(R.drawable.ic_bookmark_fill)
        } else if (isBookmark == 0) {
            imgBtnBookmark.setBackgroundResource(R.drawable.ic_bookmark_empty)
        }

        token = themePreference.getString("token value", "")

        imgBtnShare.setOnClickListener {
            var newsTitle = detailList.get(position).title
            var sentViaMessage = "Sent via NewsCout"
            var newsUrl = detailList.get(position).source_url
            var image = Uri.parse(detailList.get(position).cover_image)
            val cName = detailList.get(position).category
            val cId = MyApplication.categoryIdHashMap.get(cName) ?: 0
            var deviceId = themePreference.getString("device_token", "")
            var sourceName = detailList.get(position).source
            val sessionId = getUniqueCode(context, themePreference)
            trackingCallback(interfaceObj, themePreference, newsId, newsTitle, cId, cName, "", ActionType.SHAREARTICLE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId,sourceName,0)

            var sendIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE

                putExtra(Intent.EXTRA_TEXT, "$newsTitle\n$sentViaMessage\n$newsUrl")
                putExtra(Intent.EXTRA_SUBJECT, newsTitle)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share newss to.."))

        }

        imgBtnBookmark.setOnClickListener {
            token = themePreference.getString("token value", "")
            if (token == "") {
                var intent = Intent(context, SignInActivity::class.java)
                intent.putExtra("detail_news_item_position", position)
                (context as Activity).startActivityForResult(intent, AppConstant.REQUEST_FOR_ACTIVITY_CODE)
            }

            if (MyApplication.checkInternet == true && token != "") {
                isBookmark = detailList.get(position).bookmark_status
                var newsId = detailList.get(position).article_id
                var call: Call<BookmarkArticleData> = interfaceObj.bookmarkArticlesByApi(token, newsId)
                try {
                    call.enqueue(object : Callback<BookmarkArticleData> {
                        override fun onFailure(call: Call<BookmarkArticleData>, t: Throwable) {
                        }

                        override fun onResponse(call: Call<BookmarkArticleData>, response: Response<BookmarkArticleData>) {
                            if (isBookmark == 0) {
                                it as ImageButton
                                it.setBackgroundResource(R.drawable.ic_bookmark_fill)
                                Toast.makeText(context, "Article bookmarked", Toast.LENGTH_SHORT).show()
                                isBookmark = 1
                                detailList.get(position).bookmark_status = 1
                                var obj = detailList.get(position)
                                var id = obj.article_id
                                var category = obj.category?.let { it } ?: ""
                                var title = obj.title
                                var source = obj.source



                                if (categoryName.equals("Search")) {

                                    var sourceUrl = obj.source_url
                                    var desc = obj.description
                                    var publishedOn = obj.published_on
                                    var coverImage = obj.cover_image
                                    var hasTags = ArrayList<String>()
                                    var articleScore = obj.article_score
                                    var newsEntity = NewsEntity(id, 0, title, source, category, sourceUrl, coverImage, desc, publishedOn, hasTags, articleScore.toString())
                                    newsDao.insertNewsEntity(newsEntity)
                                }


                                var deviceId = themePreference.getString("device_token", "")
                                val sessionId = getUniqueCode(context, themePreference)
                                val cId = MyApplication.categoryIdHashMap.get(category) ?: 0
                                trackingCallback(interfaceObj, themePreference, id, title, cId, category, "", ActionType.BOOKMARK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, source, 0)

                                fetchDataViewModel.startBookmarkWorkManager(token, isBookmark, newsId)
                                notifyDataSetChanged()
                            } else if (isBookmark == 1) {
                                it.setBackgroundResource(R.drawable.ic_bookmark_empty)
                                Toast.makeText(context, "Article removed from bookmark", Toast.LENGTH_SHORT).show()
                                isBookmark = 0
                                detailList.get(position).bookmark_status = 0
                                var newsId = detailList.get(position).article_id
                                fetchDataViewModel.startBookmarkWorkManager(token, isBookmark, newsId)
                                notifyDataSetChanged()
                            }
                        }
                    })
                } catch (e: Throwable) {

                }
            } else if (MyApplication.checkInternet == false) {
                BaseAlertDialog.showAlertDialog(context, "No Internet Connection")
            }

        }

        newsSource.setOnClickListener {
            var deviceId = themePreference.getString("device_token", "")
            var sourceName = detailList.get(position).source
            val category = detailList.get(position).category
            val itemName = detailList.get(position).title
            val cName = detailList.get(position).category
            val cId = MyApplication.categoryIdHashMap.get(cName) ?: 0
            val sessionId = getUniqueCode(context, themePreference)
            trackingCallback(interfaceObj, themePreference, newsId, itemName, cId,cName,"", ActionType.SOURCECLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, sourceName, 0)
            var intent = Intent(context, SourceActivity::class.java)
            var source = detailList.get(position).source
            intent.putExtra("source_from_detail", source)
            context.startActivity(intent)
        }

        btnReadMore.setOnClickListener {
            val itemName = detailList.get(position).title
            var deviceId = themePreference.getString("device_token", "")
            val cName = detailList.get(position).category
            val cId = MyApplication.categoryIdHashMap.get(cName) ?: 0
            val sessionId = getUniqueCode(context, themePreference)
            trackingCallback(interfaceObj, themePreference, newsId, itemName, cId, cName, "", ActionType.READMORE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId,"",0)
            var url = detailList.get(position).source_url
            if (url.isNullOrBlank()) {
                Toast.makeText(context, "Page not found", Toast.LENGTH_SHORT).show()
            } else {
                val i = Intent(context, NewsWebActivity::class.java)
                i.putExtra("url_link", url)
                context.startActivity(i)
            }
        }

        tvMoreStories.setOnClickListener {
            onSlideMoreStoriesClick(parentLayoutRvSuggested, btnReadMore, tvMoreStories)
        }

        imgBtnShuffle.setOnClickListener {
            var shuffleList = fetchDataViewModel.getSuffledNewsFromDb()
            shuffleList as ArrayList<DetailNewsData>
            setData(shuffleList)

        }

        btnTopNews.setOnClickListener {
            itopNews.gotofirstNews()

        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }


    fun setData(list: ArrayList<DetailNewsData>) {
        detailList.clear()
        this.detailList = list
        notifyDataSetChanged()
    }

    fun setCategory(category: String) {
        categoryName = category
    }

    fun convertPxToDp(context: Context, px: Int): Int {
        try{
            val den = context.resources.displayMetrics.density.toInt()

            return px / context.resources.displayMetrics.density.toInt()
        }catch(e:Exception){
            return px
        }
    }

    fun convertDpToPx(context: Context, dp: Int): Int {
        return dp * context.resources.displayMetrics.density.toInt()
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    fun setColorForPath(spannable: Spannable, paths: Array<String>, color: Int) {
        for (i in paths.indices) {
            val indexOfPath = spannable.toString().indexOf(paths[i])
            if (indexOfPath == -1) {
                continue
            }
            spannable.setSpan(ForegroundColorSpan(color), indexOfPath,
                    indexOfPath + paths[i].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun slideTopStoriesUp(view: View, btnReadMore: Button, tvStories: TextView) {
        tvStories.setTypeface(null, Typeface.NORMAL)
        btnReadMore.visibility = View.INVISIBLE
        view.visibility = View.VISIBLE
        var animate = TranslateAnimation(0f, 0f, view.height.toFloat(), 0f)
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun slideTopStoriesDown(view: View, btnReadMore: Button, tvStories: TextView) {

        tvStories.setTypeface(null, Typeface.BOLD)
        view.visibility = View.INVISIBLE
        var animate = TranslateAnimation(0f, 0f, 0f, view.height.toFloat())
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)

        val animset = AnimationSet(false)
        val animFadein = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        animset.addAnimation(animFadein)
        animset.startOffset = 500
        btnReadMore.startAnimation(animset)

        btnReadMore.visibility = View.VISIBLE

    }

    fun onSlideMoreStoriesClick(view: View, btnReadMore: Button, tvStories: TextView) {
        if (isMoreStoriesUp) {
            slideTopStoriesDown(view, btnReadMore, tvStories)
        } else {
            slideTopStoriesUp(view, btnReadMore, tvStories)
        }
        isMoreStoriesUp = !isMoreStoriesUp
    }
}


open class MImageGetter(internal var mTv: TextView, internal var mContext: Context) : Html.ImageGetter {
    override fun getDrawable(source: String): Drawable {
        val drawable = LevelListDrawable()
        Glide.with(mContext)
                .asBitmap()
                .load(source)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        if (resource != null) {
                            val bitmapDrawable = BitmapDrawable(mContext.resources, resource)
                            drawable.addLevel(1, 1, bitmapDrawable)
                            drawable.setBounds(0, 0, resource.width, resource.height)
                            drawable.level = 1
                            mTv.invalidate()
                            mTv.text = mTv.text
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        return drawable
    }

}