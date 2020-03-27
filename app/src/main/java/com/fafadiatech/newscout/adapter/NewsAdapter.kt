package com.fafadiatech.newscout.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.fafadiatech.newscout.BuildConfig
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.DetailNewsActivity
import com.fafadiatech.newscout.activity.NewsWebActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.db.SourceNewsEntity
import com.fafadiatech.newscout.interfaces.PlaceHolderImageListener
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.model.NewsAdsApi
import com.fafadiatech.newscout.model.NewsAdsBodyData
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class NewsAdapter(context: Context, category: String) : PagedListAdapter<INews, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var con: Context = context
    var itemIndex: Int? = null
    var list = ArrayList<NewsEntity>()
    var detailArticleList = ArrayList<DetailNewsData>()
    var categoryType = category
    var fetchDataViewModel: FetchDataApiViewModel
    var dateString: String? = null
    var categoryId: Int = 0
    lateinit var apiInterfaceObj: ApiInterface
    lateinit var apiAdsInterfaceObj : ApiInterface
    lateinit var themePreference: SharedPreferences
    var placeHolderListener: PlaceHolderImageListener? = null
    val liveDataAds = MutableLiveData<NewsAdsApi>()
    lateinit var nameObserver : Observer<NewsAdsBodyData>

    companion object {
        var TAG: String = "NewsAdapter"

        val DIFF_CALLBACK: DiffUtil.ItemCallback<INews> = object : DiffUtil.ItemCallback<INews>() {
            override fun areItemsTheSame(oldData: INews, newData: INews): Boolean {
                if(oldData is NewsEntity && newData is NewsEntity) {
                    return oldData.id == newData.id
                }
                return false
            }

            override fun areContentsTheSame(oldData: INews, newData: INews): Boolean {
                if(oldData is NewsEntity && newData is NewsEntity) {
                    return oldData.equals(newData)
                }
                return false
            }
        }
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    init {
        fetchDataViewModel = ViewModelProviders.of(context as FragmentActivity).get(FetchDataApiViewModel::class.java)
        apiInterfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        apiAdsInterfaceObj= ApiClient.getADSClient().create(ApiInterface::class.java)
        themePreference = context.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        if (context is PlaceHolderImageListener) {
            placeHolderListener = context as PlaceHolderImageListener
        } else {
            placeHolderListener = null
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var inflater = LayoutInflater.from(parent.context)

        return when (viewType){
            0 -> {
                NewsAdapter.LeftItemViewHolder(inflater.inflate(R.layout.news_item_alternate, parent, false))
            }

            1 -> NewsAdapter.RightItemViewHolder(inflater.inflate(R.layout.news_item_main, parent, false))
            2 -> {
                AdsItemViewHolder(inflater.inflate(R.layout.item_promotion_ads_big, parent, false), con)
            }
            else -> NewsAdapter.RightItemViewHolder(inflater.inflate(R.layout.news_item_main, parent, false))
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var pagedList = currentList
        var listSize = pagedList!!.size
        val currentItem = getItem(position)
        var newsList: MutableList<INews> = pagedList!!.snapshot()
        var sourceList = ArrayList<INews>()
        if (categoryType.equals("Source") || categoryType.equals("Search")) {
            sourceList.addAll(newsList)
        }

        placeHolderListener?.showPlaceHolder(listSize)

        when (holder.itemViewType) {
            1 -> {
                var rightItemViewholder = holder as NewsAdapter.RightItemViewHolder
                val news = getItem(position) as NewsEntity
                news?.let{
                    if (it!!.source != null) {
                        var spannable = SpannableString(" via " + it.source)
                        setColorForPath(spannable, arrayOf(it.source), ContextCompat.getColor(con, R.color.colorPrimary))
                        rightItemViewholder.newsSource.text = spannable
                    }
                    rightItemViewholder.newsTitle.text = it?.title

                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        rightItemViewholder.newsImage.clipToOutline = true
                    } else {
                        rightItemViewholder.newsImage.background = ContextCompat.getDrawable(con, R.drawable.round_outline)
                    }

                    val vto = rightItemViewholder.newsImage.viewTreeObserver
                    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            rightItemViewholder.newsImage.viewTreeObserver.removeOnPreDrawListener(this)

                            if (it?.cover_image != null && it.cover_image.length > 0) {
                                var imageUrl = getImageURL(rightItemViewholder.newsImage, it.cover_image)
                                Glide.with(con).load(imageUrl).apply(requestOptions)
                                        .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                        .thumbnail(0.1f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.image_not_found)
                                        .error(R.drawable.image_not_found)
                                        .into(rightItemViewholder.newsImage)
                            } else {

                                Glide.with(con).load(R.drawable.image_not_found).apply(requestOptions)
                                        .into(rightItemViewholder.newsImage)
                            }

                            return true
                        }
                    })

                    if (it?.published_on != null) {
                        var timeAgo: String = ""
                        try {
                            dateString = it!!.published_on

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
                            rightItemViewholder.newsTime.text = timeAgo
                        } catch (e: Exception) {
                            try {
                                var timeZone = Calendar.getInstance().timeZone.id
                                var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                                dateformat.timeZone = TimeZone.getTimeZone("UTC")
                                var date = dateformat.parse(dateString)
                                dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                                dateformat.format(date)
                                timeAgo = TimeAgo.using(date.time, TimeAgoMessages.Builder().defaultLocale().build())
                                rightItemViewholder.newsTime.text = timeAgo
                            } catch (exception: Exception) {
                                rightItemViewholder.newsTime.text = ""
                            }
                        }
                    } else {

                    }

                    rightItemViewholder.itemRootView.setOnClickListener {
                        itemIndex = position
                        fetchDataViewModel.setCurrentListNews(newsList)
                        var id = news!!.id
                        categoryId = news!!.category_id
                        val categoryName = MyApplication.categoryNameHashMap.get(categoryId) ?: ""
                        var itemTitle = news!!.title
                        var deviceId = themePreference.getString("device_token", "")
                        trackClick("News Click", news!!.category, itemTitle)

                        val sessionId = getUniqueCode(con, themePreference)
                        val title = news!!.title
                        val cName = news!!.category
                        val source = news!!.source
                        trackingCallback(apiInterfaceObj, themePreference, id, title, categoryId, cName, "", ActionType.ARTICLEDETAIL.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, source, 0)

                        if(BuildConfig.showAds) {
                            var difference: Int = 0
                            if (position > (ADFACTOR - 1)) {
                                difference = floor(position.toFloat() / (ADFACTOR + 1)).toInt()
                                itemIndex = itemIndex!! - difference
                            }
                        }
                        Log.d("News Adapter","Right Image News Id :"+id)
                        var detailIntent = Intent(con, DetailNewsActivity::class.java)
                        detailIntent.putExtra("indexPosition", itemIndex!!)
                        if (categoryType.equals("Source") || categoryType.equals("Search")) {
                            detailIntent.putParcelableArrayListExtra("source_list", sourceList)
                        }
                        detailIntent.putExtra("category_of_newslist", categoryType)
                        detailIntent.putExtra("category_id", categoryId)
                        con.startActivity(detailIntent)
                    }

                }
            }

            0 -> {
                var leftItemViewholder = holder as NewsAdapter.LeftItemViewHolder
                val news = getItem(position) as NewsEntity
                news?.let {
                    if (it!!.source != null) {
                        var spannable = SpannableString(" via " + it.source)
                        setColorForPath(spannable, arrayOf(it.source), ContextCompat.getColor(con, R.color.colorPrimary))
                        leftItemViewholder.newsSourceLeft.text = spannable
                    }

                    leftItemViewholder.newsTitleLeft.text = it?.title

                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        leftItemViewholder.newsImageLeft.clipToOutline = true
                    } else {
                        leftItemViewholder.newsImageLeft.background = ContextCompat.getDrawable(con, R.drawable.round_outline)
                    }

                    val vto = leftItemViewholder.newsImageLeft.viewTreeObserver
                    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            leftItemViewholder.newsImageLeft.viewTreeObserver.removeOnPreDrawListener(this)

                            if (it?.cover_image != null && it.cover_image.length > 0) {
                                var imageUrl = getImageURL(leftItemViewholder.newsImageLeft, it.cover_image)
                                Glide.with(con).load(imageUrl).apply(requestOptions)
                                        .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                        .thumbnail(0.1f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.image_not_found)
                                        .error(R.drawable.image_not_found)
                                        .into(leftItemViewholder.newsImageLeft)
                            } else {
                                Glide.with(con).load(R.drawable.image_not_found).apply(requestOptions)
                                        .into(leftItemViewholder.newsImageLeft)
                            }
                            return true
                        }
                    })

                    if (it?.published_on != null) {
                        var timeAgo: String = ""
                        try {
                            dateString = it!!.published_on
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
                            leftItemViewholder.newsTimeLeft.text = timeAgo

                        } catch (e: Exception) {

                            try {
                                var timeZone = Calendar.getInstance().timeZone.id
                                var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                                dateformat.timeZone = TimeZone.getTimeZone("UTC")
                                var date = dateformat.parse(dateString)
                                dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                                dateformat.format(date)
                                timeAgo = TimeAgo.using(date.time, TimeAgoMessages.Builder().defaultLocale().build())
                                leftItemViewholder.newsTimeLeft.text = timeAgo

                            } catch (exception: Exception) {
                                leftItemViewholder.newsTimeLeft.text = ""
                            }
                        }
                    } else {

                    }
                }

                leftItemViewholder.itemRootView.setOnClickListener {
                    fetchDataViewModel.setCurrentListNews(newsList)
                    itemIndex = position
                    var id = news!!.id
                    fetchDataViewModel.startRecommendNewsWorkManager(id)
                    categoryId = news!!.category_id
                    var itemTitle = news!!.title
                    var deviceId = themePreference.getString("device_token", "")
                    val categoryName = MyApplication.categoryNameHashMap.get(categoryId) ?: ""
                    trackClick("News Click", news!!.category, itemTitle)

                    val sessionId = getUniqueCode(con, themePreference)
                    val title = news!!.title
                    val cName = news!!.category
                    val source = news!!.source
                    trackingCallback(apiInterfaceObj, themePreference, id, title, categoryId, cName, "", ActionType.ARTICLEDETAIL.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, source, 0)
                    if(BuildConfig.showAds) {
                        var difference: Int = 0
                        if (position > (ADFACTOR - 1)) {
                            difference = floor(position.toFloat() / (ADFACTOR + 1)).toInt()
                            itemIndex = itemIndex!! - difference
                        }
                    }
                    Log.d("News Adapter","Left Image News Id :"+id)
                    var detailIntent = Intent(con, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", itemIndex!!)
                    if (categoryType.equals("Source") || categoryType.equals("Search")) {
                        detailIntent.putParcelableArrayListExtra("source_list", sourceList)
                    }
                    detailIntent.putExtra("category_of_newslist", categoryType)
                    detailIntent.putExtra("category_id", categoryId)

                    con.startActivity(detailIntent)
                }
            }

            2 -> {
                var adsViewholder = holder as AdsItemViewHolder
                fetchDataViewModel.getAdsTitle().observeOnce(con as LifecycleOwner, Observer<NewsAdsBodyData> {
                    if (it != null) {
                        holder?.adsTitle?.text = it?.ad_text
                        val adsurl = it?.ad_url
                        if (it?.media != null && it.media.length > 0) {

                            Glide.with(con).load(it.media).apply(requestOptions)
                                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                    .thumbnail(0.1f)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.image_not_found)
                                    .error(R.drawable.image_not_found)
                                    .into(holder?.adsImage)
                        } else {
                            Glide.with(con).load(R.drawable.image_not_found).apply(requestOptions)
                                    .into(holder?.adsImage)
                        }


                        holder?.adsCardview.setOnClickListener {
                            var deviceId = themePreference.getString("device_token", "")
                            val sessionId = getUniqueCode(con, themePreference)
                            trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.ADCLICK.type, deviceId?:"", PLATFORM, ViewType.MONETIZATIONVIEW.type, sessionId, "", 0)

                            var url = adsurl
                            if (url.isNullOrBlank()) {
                                Toast.makeText(con, "Page not found", Toast.LENGTH_SHORT).show()
                            } else {
                                val i = Intent(con, NewsWebActivity::class.java)
                                i.putExtra("url_link", url)
                                con.startActivity(i)
                            }
                        }

                    }
                })
                getAdsDetail()
            }
        }
    }

    fun setArticleDetailData(result: ArrayList<DetailNewsData>?) {
        if (result == null) {

        } else {
            this.detailArticleList.clear()
            this.detailArticleList.addAll(result)
            notifyDataSetChanged()
        }
    }

    class RightItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemRootView = view.findViewById<ConstraintLayout>(R.id.root_layout_news_item)
        var newsSource = view.findViewById<TextView>(R.id.news_source_main)
        var newsTitle = view.findViewById<TextView>(R.id.news_title_main)
        var newsImage = view.findViewById<ImageView>(R.id.news_image_main)
        var newsTime = view.findViewById<TextView>(R.id.news_time_main)
    }

    class LeftItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemRootView = view.findViewById<ConstraintLayout>(R.id.root_layout_news_item_alternate)
        var newsSourceLeft = view.findViewById<TextView>(R.id.news_source_alternate)
        var newsTitleLeft = view.findViewById<TextView>(R.id.news_title_alternate)
        var newsImageLeft = view.findViewById<ImageView>(R.id.news_image_alternate)
        var newsTimeLeft = view.findViewById<TextView>(R.id.news_time_alternate)
    }

    class AdsItemViewHolder(view:View, conn:Context) : RecyclerView.ViewHolder(view), LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)

        init {
            lifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
        }
        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

        fun markAttach() {
            lifecycleRegistry.markState(Lifecycle.State.STARTED)
        }

        fun markDetach() {
            lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
        }
        var adsCardview = view.findViewById<CardView>(R.id.card_view)
        var adsTitle = view.findViewById<TextView>(R.id.ads_title)
        var adsImage = view.findViewById<ImageView>(R.id.ads_image)


    }

    override fun getItemViewType(position: Int): Int {

        if(getItem(position) is NewsEntity){
            if(position % 2 == 0){
                return 0
            }else{
                return 1
            }
        } else{
            return 2
        }
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

    fun setPlaceHolderImage(placeHolderListener: PlaceHolderImageListener) {
        this.placeHolderListener = placeHolderListener
    }

    fun trackClick(eventName: String, category: String, title: String) {
        Answers.getInstance().logCustom(CustomEvent(eventName)
                .putCustomAttribute("Category", category)
                .putCustomAttribute("Title", title));
    }

    fun getAdsDetail(){

        var call: Call<NewsAdsApi> = apiAdsInterfaceObj.getAds(categoryType)
        call.enqueue(object : Callback<NewsAdsApi> {
            override fun onFailure(call: Call<NewsAdsApi>, t: Throwable) {
                Log.d("TestMainActivity", "Inside Failure")
            }

            override fun onResponse(call: Call<NewsAdsApi>, response: Response<NewsAdsApi>) {

                Log.d("TestMainActivity", "Inside Success")
                val code = response.code()
                if(code == 200) {
                    val bodyData = response.body()
                    bodyData?.body?.let{
                        fetchDataViewModel.setAdsTitle(bodyData?.body)
                    }
                }
            }
        })
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
}

