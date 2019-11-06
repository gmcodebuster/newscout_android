package com.fafadiatech.newscout.adapter

import android.content.Context
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.getImageURL
import com.fafadiatech.newscout.interfaces.AddTrendingFragmentListener
import com.fafadiatech.newscout.model.TrendingNewsData
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TrendingAdapter(var context: Context, var addTrendingFragmentListener: AddTrendingFragmentListener) : RecyclerView.Adapter<TrendingAdapter.TrendingViewHolder>() {

    var trendingList = ArrayList<TrendingNewsData>()
    var dateString: String? = null
    var categoryId: Int = 0
    var categoryName: String = ""
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.layout_trending_item, parent, false)
        return TrendingViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trendingList.size
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        val vto = holder.newsImage.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                holder.newsImage.viewTreeObserver.removeOnPreDrawListener(this)
                if (trendingList.get(position).cover_image != null && trendingList.get(position).cover_image.length > 0) {
                    var imageUrl = getImageURL(holder.newsImage, trendingList.get(position).cover_image)
                    Glide.with(context).load(imageUrl).apply(requestOptions)
                            .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                            .placeholder(R.drawable.image_not_found)
                            .error(R.drawable.image_not_found)
                            .into(holder.newsImage)
                } else {
                    Glide.with(context).load(R.drawable.image_not_found).apply(requestOptions)
                            .into(holder.newsImage)
                }
                return true
            }
        })

        holder.newsTitle.text = trendingList.get(position)?.title
        holder.trendingCount.text = trendingList.get(position).count.toString()

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            holder.newsImage.clipToOutline = true
        } else {
            holder.newsImage.background = ContextCompat.getDrawable(context, R.drawable.round_outline)
        }

        if (trendingList.get(position)!!.source != null) {
            var spannable = SpannableString(" via " + trendingList.get(position).source)
            setColorForPath(spannable, arrayOf(trendingList.get(position).source), ContextCompat.getColor(context, R.color.primaryColorNs))
            holder.newsSource.text = spannable
        }

        if (trendingList.get(position)?.published_on != null) {
            var timeAgo: String = ""
            try {
                dateString = trendingList.get(position).published_on
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
                holder.newsTime.text = timeAgo
            } catch (e: Exception) {
                try {
                    var timeZone = Calendar.getInstance().timeZone.id
                    var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                    dateformat.timeZone = TimeZone.getTimeZone("UTC")
                    var date = dateformat.parse(dateString)
                    dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                    dateformat.format(date)
                    timeAgo = TimeAgo.using(date.time, TimeAgoMessages.Builder().defaultLocale().build())
                    holder.newsTime.text = timeAgo
                } catch (exception: Exception) {
                    holder.newsTime.text = ""
                }
            }
        } else {

        }

        holder.rootLayout.setOnClickListener {
            var clusterId = trendingList.get(position).cluster_id
            addTrendingFragmentListener.addFragmentOnClick(clusterId, position)
        }
    }

    class TrendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout = itemView.findViewById<ConstraintLayout>(R.id.trending_root_layout)
        var newsImage = itemView.findViewById<ImageView>(R.id.imgView_news_item)
        var newsTitle = itemView.findViewById<TextView>(R.id.tv_news_title)
        var newsTime = itemView.findViewById<TextView>(R.id.news_time)
        var newsSource = itemView.findViewById<TextView>(R.id.news_source)
        var trendingCount = itemView.findViewById<TextView>(R.id.trending_count)
    }

    fun setTrendingData(list: ArrayList<TrendingNewsData>) {
        this.trendingList.clear()
        this.trendingList.addAll(list)
        notifyDataSetChanged()
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

    fun setCategory(category: String, id: Int) {
        this.categoryName = category
        this.categoryId = id
        notifyDataSetChanged()
    }
}