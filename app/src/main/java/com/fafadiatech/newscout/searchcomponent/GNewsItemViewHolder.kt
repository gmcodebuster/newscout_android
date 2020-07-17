package com.fafadiatech.newscout.searchcomponent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.application.GlideRequests
import com.fafadiatech.newscout.model.ArticlesData

class GNewsItemViewHolder(view: View,
                          private val glide: GlideRequests) : RecyclerView.ViewHolder(view) {

    private var item: ArticlesData? = null

    private val itemRootView:ConstraintLayout  = view.findViewById<ConstraintLayout>(R.id.root_layout_news_item)
    private val newsSourceLeft: TextView = view.findViewById<TextView>(R.id.news_source_main)
    private val newsTitleLeft: TextView = view.findViewById<TextView>(R.id.news_title_main)
    private val newsImageLeft: ImageView = view.findViewById<ImageView>(R.id.news_image_main)
    private val newsTimeLeft: TextView = view.findViewById<TextView>(R.id.news_time_main)

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
        newsSourceLeft.text = item?.source
        newsTitleLeft.text = item?.title
        newsTimeLeft.text = item?.published_on
        /*if (item?.cover_image?.startsWith("http") == true) {

            glide.load(item.avatarUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_placeholder)
                    .into(thumbnail)
        } else {

            glide.clear(thumbnail)
        }*/
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): GNewsItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.news_item_main, parent, false)
            return GNewsItemViewHolder(view, glide)
        }
    }
}