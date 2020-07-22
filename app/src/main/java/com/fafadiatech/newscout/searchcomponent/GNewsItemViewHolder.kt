package com.fafadiatech.newscout.searchcomponent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.newsCoverImage
import com.fafadiatech.newscout.appconstants.publishDate
import com.fafadiatech.newscout.appconstants.spannableSource
import com.fafadiatech.newscout.application.GlideRequests
import com.fafadiatech.newscout.model.ArticlesData

class GNewsItemViewHolder(private val view: View,
                          private val glide: GlideRequests) : RecyclerView.ViewHolder(view) {

    private var item: ArticlesData? = null

    private val itemRootView:ConstraintLayout  = view.findViewById<ConstraintLayout>(R.id.root_layout_news_item)
    private val newsSource: TextView = view.findViewById<TextView>(R.id.news_source_main)
    private val newsTitle: TextView = view.findViewById<TextView>(R.id.news_title_main)
    private val newsImage: ImageView = view.findViewById<ImageView>(R.id.news_image_main)
    private val newsTime: TextView = view.findViewById<TextView>(R.id.news_time_main)

    init {
        view.setOnClickListener {
            item?.id?.let { url ->
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                view.context.startActivity(intent)
            }
        }


    }

    fun bind(item: ArticlesData?) {
        this.item = item
        newsSource.text = spannableSource( view.context, item?.source)
        newsTitle.text = item?.let { HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY) }
        newsTime.text = publishDate(item?.published_on)
        newsCoverImage(view.context, glide, newsImage, item?.cover_image)
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): GNewsItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.news_item_main, parent, false)
            return GNewsItemViewHolder(view, glide)
        }
    }
}