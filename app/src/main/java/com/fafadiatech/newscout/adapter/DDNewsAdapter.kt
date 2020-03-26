package com.fafadiatech.newscout.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.DetailNewsActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.db.dailydigest.DailyDigestEntity
import com.fafadiatech.newscout.interfaces.PlaceHolderImageListener
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DDNewsAdapter(context: Context) : PagedListAdapter<DailyDigestEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var con: Context = context
    var itemIndex: Int? = null
    var list = ArrayList<DailyDigestEntity>()
    var detailArticleList = ArrayList<DetailNewsData>()
    var fetchDataViewModel: FetchDataApiViewModel
    var dateString: String? = null
    var categoryId: Int = 0
    lateinit var apiInterfaceObj: ApiInterface
    lateinit var themePreference: SharedPreferences
    var placeHolderListener: PlaceHolderImageListener? = null

    companion object {
        var TAG: String = "NewsAdapter"
        val DIFF_CALLBACK: DiffUtil.ItemCallback<DailyDigestEntity> = object : DiffUtil.ItemCallback<DailyDigestEntity>() {

            override fun areItemsTheSame(oldData: DailyDigestEntity, newData: DailyDigestEntity): Boolean {
                return oldData.id == newData.id
            }

            override fun areContentsTheSame(oldData: DailyDigestEntity, newData: DailyDigestEntity): Boolean {
                return oldData.equals(newData)
            }
        }
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    init {
        fetchDataViewModel = ViewModelProviders.of(context as FragmentActivity).get(FetchDataApiViewModel::class.java)
        apiInterfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        themePreference = context.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        if (context is PlaceHolderImageListener) {
            placeHolderListener = context as PlaceHolderImageListener
        } else {
            placeHolderListener = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var inflater = LayoutInflater.from(parent.context)
        return when (viewType % 2) {
            0 -> NewsAdapter.LeftItemViewHolder(inflater.inflate(R.layout.news_item_alternate, parent, false))
            else -> NewsAdapter.RightItemViewHolder(inflater.inflate(R.layout.news_item_main, parent, false))

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var pagedList = currentList
        var listSize = pagedList!!.size
        val currentItem = getItem(position)
        var newsList: MutableList<DailyDigestEntity> = pagedList!!.snapshot()
        var sourceList = ArrayList<DailyDigestEntity>()
        sourceList.addAll(newsList)
        placeHolderListener?.showPlaceHolder(listSize)

        when (holder.itemViewType) {
            1 -> {
                var rightItemViewholder = holder as NewsAdapter.RightItemViewHolder
                getItem(position)?.let {

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
                        var id = getItem(position)!!.id
                        fetchDataViewModel.startRecommendNewsWorkManager(id)
                        categoryId = getItem(position)!!.category_id
                        var itemTitle = getItem(position)!!.title
                        var deviceId = themePreference.getString("device_token", "")
                        trackClick("News Click", getItem(position)!!.category, itemTitle)

                        val sessionId = getUniqueCode(con, themePreference)
                        val cName = getItem(position)!!.category
                        val source = getItem(position)!!.source
                        trackingCallback(apiInterfaceObj, themePreference, id, itemTitle, categoryId, cName, "", ActionType.DAILYDIGESTLISTCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, source, 0)

                        var detailIntent = Intent(con, DetailNewsActivity::class.java)
                        detailIntent.putExtra("indexPosition", itemIndex!!)
//                        detailIntent.putParcelableArrayListExtra("source_list", sourceList)
                        detailIntent.putExtra("category_of_newslist", "DailyDigest")
                        detailIntent.putExtra("category_id", categoryId)

                        con.startActivity(detailIntent)
                    }
                }
            }

            0 -> {
                var leftItemViewholder = holder as NewsAdapter.LeftItemViewHolder
                getItem(position)?.let {

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

                    itemIndex = position
                    var id = getItem(position)!!.id
                    fetchDataViewModel.startRecommendNewsWorkManager(id)
                    categoryId = getItem(position)!!.category_id
                    var itemTitle = getItem(position)!!.title
                    var deviceId = themePreference.getString("device_token", "")
                    trackClick("News Click", getItem(position)!!.category, itemTitle)

                    val sessionId = getUniqueCode(con, themePreference)
                    val cName = getItem(position)!!.category
                    val source = getItem(position)!!.source
                    trackingCallback(apiInterfaceObj, themePreference, id, itemTitle, categoryId, cName, "", ActionType.DAILYDIGESTLISTCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, source, 0)

                    var detailIntent = Intent(con, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", itemIndex!!)
//                    detailIntent.putParcelableArrayListExtra("source_list", sourceList)
                    detailIntent.putExtra("category_of_newslist", "DailyDigest")
                    detailIntent.putExtra("category_id", categoryId)

                    con.startActivity(detailIntent)
                }
            }
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

    override fun getItemViewType(position: Int): Int {
        return when (position % 2) {
            0 -> 0
            else -> 1
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
}