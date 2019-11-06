package com.fafadiatech.newscout.adapter

import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.DetailNewsActivity
import com.fafadiatech.newscout.appconstants.getImageURL
import com.fafadiatech.newscout.db.NewsEntity
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TrendingNewsAdapter(context: Context, category: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var trendingList = ArrayList<NewsEntity>()
    var con: Context = context
    var itemIndex: Int? = null
    var dateString: String? = null
    var categoryId: Int = 0
    var categoryType = category
    var clusterId: Int = 0
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        return when (viewType % 2) {

            0 -> TrendingNewsAdapter.LeftItemViewHolder(inflater.inflate(R.layout.news_item_alternate, parent, false))
            else -> TrendingNewsAdapter.RightItemViewHolder(inflater.inflate(R.layout.news_item_main, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return trendingList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var list = ArrayList<NewsEntity>()
        if (categoryType.equals("Source")) {

        }
        when (holder.itemViewType) {
            1 -> {
                var rightItemViewholder = holder as TrendingNewsAdapter.RightItemViewHolder

                if (trendingList.get(position)!!.source != null) {
                    var spannable = SpannableString(" via " + trendingList.get(position).source)
                    setColorForPath(spannable, arrayOf(trendingList.get(position).source), ContextCompat.getColor(con, R.color.primaryColorNs))
                    rightItemViewholder.newsSource.text = spannable
                }

                rightItemViewholder.newsTitle.text = trendingList.get(position)?.title

                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    rightItemViewholder.newsImage.clipToOutline = true
                } else {
                    rightItemViewholder.newsImage.background = ContextCompat.getDrawable(con, R.drawable.round_outline)
                }

                val vto = rightItemViewholder.newsImage.viewTreeObserver
                vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        rightItemViewholder.newsImage.viewTreeObserver.removeOnPreDrawListener(this)
                        if (trendingList.get(position)?.cover_image != null && trendingList.get(position).cover_image.length > 0) {
                            var imageUrl = getImageURL(rightItemViewholder.newsImage, trendingList.get(position).cover_image)
                            Glide.with(con).load(imageUrl).apply(requestOptions)
                                    .placeholder(R.drawable.image_not_found)
                                    .error(R.drawable.image_not_found)
                                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                    .into(rightItemViewholder.newsImage)
                        } else {

                            Glide.with(con).load(R.drawable.image_not_found)
                                    .into(rightItemViewholder.newsImage)
                        }
                        return true
                    }
                })

                if (trendingList.get(position)?.published_on != null) {
                    var timeAgo: String = ""
                    try {
                        dateString = trendingList.get(position)!!.published_on
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
                    var id = trendingList.get(position)!!.id
                    categoryId = trendingList.get(position)!!.category_id
                    var itemTitle = trendingList.get(position)!!.title
                    var detailIntent = Intent(con, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", itemIndex!!)
                    detailIntent.putExtra("cluster_id", clusterId)
                    detailIntent.putParcelableArrayListExtra("source_list", list)
                    detailIntent.putExtra("category_of_newslist", categoryType)
                    detailIntent.putExtra("category_id", categoryId)
                    con.startActivity(detailIntent)
                }
            }

            0 -> {
                var leftItemViewholder = holder as TrendingNewsAdapter.LeftItemViewHolder
                if (trendingList.get(position)!!.source != null) {
                    var spannable = SpannableString(" via " + trendingList.get(position).source)
                    setColorForPath(spannable, arrayOf(trendingList.get(position).source), ContextCompat.getColor(con, R.color.primaryColorNs))
                    leftItemViewholder.newsSourceLeft.text = spannable
                }

                leftItemViewholder.newsTitleLeft.text = trendingList.get(position)?.title
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    leftItemViewholder.newsImageLeft.clipToOutline = true
                } else {
                    leftItemViewholder.newsImageLeft.background = ContextCompat.getDrawable(con, R.drawable.round_outline)
                }

                val vto = leftItemViewholder.newsImageLeft.viewTreeObserver
                vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        leftItemViewholder.newsImageLeft.viewTreeObserver.removeOnPreDrawListener(this)
                        if (trendingList.get(position)?.cover_image != null && trendingList.get(position).cover_image.length > 0) {
                            var imageUrl = getImageURL(leftItemViewholder.newsImageLeft, trendingList.get(position).cover_image)

                            Glide.with(con).load(imageUrl).apply(requestOptions)
                                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                    .placeholder(R.drawable.image_not_found)
                                    .error(R.drawable.image_not_found)
                                    .into(leftItemViewholder.newsImageLeft)
                        } else {
                            Glide.with(con).load(R.drawable.image_not_found)
                                    .into(leftItemViewholder.newsImageLeft)
                        }
                        return true
                    }
                })

                if (trendingList.get(position)?.published_on != null) {
                    var timeAgo: String = ""
                    try {
                        dateString = trendingList.get(position)!!.published_on
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

                leftItemViewholder.itemRootView.setOnClickListener {
                    itemIndex = position
                    var id = trendingList.get(position)!!.id
                    categoryId = trendingList.get(position).category_id
                    var itemTitle = trendingList.get(position).title
                    var detailIntent = Intent(con, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", itemIndex!!)
                    detailIntent.putExtra("cluster_id", clusterId)
                    detailIntent.putParcelableArrayListExtra("source_list", list)
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

    fun setTrendingData(list: ArrayList<NewsEntity>) {
        this.trendingList = list
        notifyDataSetChanged()
    }

    fun setClustedId(clusterId: Int) {
        this.clusterId = clusterId
    }
}