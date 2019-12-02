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
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.DetailNewsActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.appconstants.getImageURL
import com.fafadiatech.newscout.appconstants.trackUserSelection
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.interfaces.ProgressBarListener
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SearchAdapter(context: Context, category: String, var progressBarListener: ProgressBarListener) : PagedListAdapter<NewsEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var con: Context = context
    var itemIndex: Int? = null
    var list = ArrayList<NewsEntity>()
    var categoryType = category
    var fetchDataViewModel: FetchDataApiViewModel
    var dateString: String? = null
    var categoryId: Int = 0
    lateinit var apiInterfaceObj: ApiInterface
    lateinit var themePreference: SharedPreferences

    companion object {
        var TAG: String = "SearchAdapter"

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        val DIFF_CALLBACK: DiffUtil.ItemCallback<NewsEntity> = object : DiffUtil.ItemCallback<NewsEntity>() {
            override fun areItemsTheSame(oldData: NewsEntity, newData: NewsEntity): Boolean {
                return oldData.id == newData.id
            }

            override fun areContentsTheSame(oldData: NewsEntity, newData: NewsEntity): Boolean {
                return oldData.equals(newData)
            }
        }
    }

    init {
        fetchDataViewModel = ViewModelProviders.of(context as FragmentActivity).get(FetchDataApiViewModel::class.java)
        apiInterfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        themePreference = context.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        return when (viewType % 2) {
            0 -> SearchAdapter.LeftItemViewHolder(inflater.inflate(R.layout.news_item_alternate, parent, false))
            else -> SearchAdapter.RightItemViewHolder(inflater.inflate(R.layout.news_item_main, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var pagedList = currentList
        var newsList: MutableList<NewsEntity> = pagedList!!.snapshot()
        var sourceList = ArrayList<NewsEntity>()
        if (categoryType.equals("Source")) {
            sourceList.addAll(newsList)
        }

        if (pagedList.size != 0 && pagedList.size != null) {
            progressBarListener.showProgress()
        }

        when (holder.itemViewType) {
            1 -> {
                var rightItemViewholder = holder as SearchAdapter.RightItemViewHolder
                getItem(position).let {
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
                        trackUserSelection(apiInterfaceObj, "item_detail", deviceId, "android", id, itemTitle)
                        var detailIntent = Intent(con, DetailNewsActivity::class.java)
                        detailIntent.putExtra("indexPosition", itemIndex!!)
                        detailIntent.putParcelableArrayListExtra("source_list", sourceList)
                        detailIntent.putExtra("category_of_newslist", categoryType)
                        detailIntent.putExtra("category_id", categoryId)
                        con.startActivity(detailIntent)
                    }
                }
            }

            0 -> {
                var leftItemViewholder = holder as SearchAdapter.LeftItemViewHolder
                getItem(position).let {
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
                    trackUserSelection(apiInterfaceObj, "item_detail", deviceId, "android", id, itemTitle)
                    var detailIntent = Intent(con, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", itemIndex!!)
                    detailIntent.putParcelableArrayListExtra("source_list", sourceList)
                    detailIntent.putExtra("category_of_newslist", categoryType)
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
}