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
import com.fafadiatech.newscout.model.DetailNewsData
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BookmarkedNewsAdapter(var con: Context, var category: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var dateString: String? = null
    var widthInPixel: Int = 0
    var heightInPixel: Int = 0
    var bookmarkedList = ArrayList<DetailNewsData>()
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    init {
        widthInPixel = convertDpToPx(con, 75)
        heightInPixel = convertDpToPx(con, 75)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var inflater = LayoutInflater.from(parent.context)

        return when (viewType % 2) {
            0 -> BookmarkedNewsAdapter.LeftItemViewHolder(inflater.inflate(R.layout.news_item_alternate, parent, false))
            else -> BookmarkedNewsAdapter.RightItemViewHolder(inflater.inflate(R.layout.news_item_main, parent, false))
        }
    }

    override fun getItemCount(): Int {

        if (bookmarkedList == null) {
            return 0
        }

        return bookmarkedList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (position % 2) {
            0 -> 0
            else -> 1
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            1 -> {
                var rightItemViewholder = holder as BookmarkedNewsAdapter.RightItemViewHolder

                if (bookmarkedList.get(position)!!.source != null) {

                    var spannable = SpannableString(" via " + bookmarkedList.get(position).source)
                    setColorForPath(spannable, arrayOf(bookmarkedList.get(position).source), ContextCompat.getColor(con, R.color.colorPrimary))
                    rightItemViewholder.newsSource.text = spannable
                }

                rightItemViewholder.newsTitle.text = bookmarkedList.get(position)?.title

                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    rightItemViewholder.newsImage.clipToOutline = true
                } else {
                    rightItemViewholder.newsImage.background = ContextCompat.getDrawable(con, R.drawable.round_outline)
                }

                if (bookmarkedList.get(position)?.cover_image != null && bookmarkedList.get(position)?.cover_image?.length!! > 0) {
                    var observer: ViewTreeObserver = rightItemViewholder.newsImage.getViewTreeObserver()
                    observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            var imageUrl = getImageURL(rightItemViewholder.newsImage, bookmarkedList.get(position).cover_image)
                            Glide.with(con).load(imageUrl).apply(requestOptions)
                                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                    .placeholder(R.drawable.image_not_found)
                                    .error(R.drawable.image_not_found)
                                    .into(rightItemViewholder.newsImage)
                        }
                    })
                } else {

                    Glide.with(con).load(R.drawable.image_not_found).apply(requestOptions)
                            .into(rightItemViewholder.newsImage)
                }
                if (bookmarkedList.get(position)?.published_on != null) {
                    var timeAgo: String = ""
                    try {

                        dateString = bookmarkedList.get(position)!!.published_on

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

                    var detailIntent = Intent(con, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", position)
                    detailIntent.putExtra("category_of_newslist", "Bookmark")
                    con.startActivity(detailIntent)
                }
            }

            0 -> {
                var leftItemViewholder = holder as BookmarkedNewsAdapter.LeftItemViewHolder
                if (bookmarkedList.get(position)!!.source != null) {
                    var spannable = SpannableString(" via " + bookmarkedList.get(position).source)
                    setColorForPath(spannable, arrayOf(bookmarkedList.get(position).source), ContextCompat.getColor(con, R.color.colorPrimary))
                    leftItemViewholder.newsSourceLeft.text = spannable
                }
                leftItemViewholder.newsTitleLeft.text = bookmarkedList.get(position)?.title
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    leftItemViewholder.newsImageLeft.clipToOutline = true
                } else {
                    leftItemViewholder.newsImageLeft.background = ContextCompat.getDrawable(con, R.drawable.round_outline)
                }
                if (bookmarkedList.get(position)?.cover_image != null && bookmarkedList.get(position)?.cover_image?.length!! > 0) {
                    var observer: ViewTreeObserver = leftItemViewholder.newsImageLeft.getViewTreeObserver()
                    observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            var imageUrl = getImageURL(leftItemViewholder.newsImageLeft, bookmarkedList.get(position).cover_image)
                            Glide.with(con).load(imageUrl).apply(requestOptions)
                                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                    .placeholder(R.drawable.image_not_found)
                                    .error(R.drawable.image_not_found)
                                    .into(leftItemViewholder.newsImageLeft)
                        }
                    })
                } else {
                    Glide.with(con).load(R.drawable.image_not_found).apply(requestOptions)
                            .into(leftItemViewholder.newsImageLeft)
                }
                if (bookmarkedList.get(position)?.published_on != null) {
                    var timeAgo: String = ""
                    try {
                        dateString = bookmarkedList.get(position)!!.published_on

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
                    var detailIntent = Intent(con, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", position)
                    detailIntent.putExtra("category_of_newslist", "Bookmark")
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

    fun convertDpToPx(context: Context, dp: Int): Int {
        return dp * context.resources.displayMetrics.density.toInt()
    }

    fun setData(result: ArrayList<DetailNewsData>) {
        this.bookmarkedList.clear()
        this.bookmarkedList = result

        notifyDataSetChanged()
    }
}
