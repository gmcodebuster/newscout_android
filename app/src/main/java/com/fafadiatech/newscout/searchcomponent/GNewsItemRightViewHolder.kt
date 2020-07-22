package com.fafadiatech.newscout.searchcomponent

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.application.GlideRequests
import com.fafadiatech.newscout.model.ArticlesData
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.text.SimpleDateFormat
import java.util.*

class GNewsItemRightViewHolder(private val view: View,
                                 private val glide: GlideRequests) : RecyclerView.ViewHolder(view) {

    private var item: ArticlesData? = null

    private val itemRootView: ConstraintLayout = view.findViewById<ConstraintLayout>(R.id.root_layout_news_item_alternate)
    private val newsSource: TextView = view.findViewById<TextView>(R.id.news_source_alternate)
    private val newsTitle: TextView = view.findViewById<TextView>(R.id.news_title_alternate)
    private val newsImage: ImageView = view.findViewById<ImageView>(R.id.news_image_alternate)
    private val newsTime: TextView = view.findViewById<TextView>(R.id.news_time_alternate)

    init {
        view.setOnClickListener {

        }
    }

    fun bind(item: ArticlesData?) {
        this.item = item
        newsSource.text =  spannableSource( view.context, item?.source)
        newsTitle.text = item?.let { HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY) }
        newsTime.text = publishDate(item?.published_on)
        newsCoverImage(view.context, glide, newsImage, item?.cover_image)
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): GNewsItemRightViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.news_item_alternate, parent, false)
            return GNewsItemRightViewHolder(view, glide)
        }
    }


}