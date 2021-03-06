package com.fafadiatech.newscout.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.DetailNewsActivity
import com.fafadiatech.newscout.appconstants.getImageURL
import com.fafadiatech.newscout.model.DetailNewsData

class SuggestedNewsAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TAG: String = "SuggestedNewsAdapter"
    var itemIndex: Int = 0
    var detailList = ArrayList<DetailNewsData>()
    var widthInPixel: Int = 0
    var heightInPixel: Int = 0
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    init {
        widthInPixel = convertDpToPx(context, 160)
        heightInPixel = convertDpToPx(context, 80)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SuggestedNewsHeaderViewHolder(inflater.inflate(R.layout.layout_recommended_news_title, null))
            else -> SuggestedNewsViewHolder(inflater.inflate(R.layout.suggested_news_item_bottom, null))
        }
    }

    override fun getItemCount(): Int {
        return detailList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> 0
            else -> 1
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                var viewHolderTitle = holder as SuggestedNewsHeaderViewHolder
                viewHolderTitle.recommendedNewsTitle.text = context.resources.getString(R.string.recommended_news_title)
            }

            1 -> {
                itemIndex = position - 1
                var viewHolderItem = holder as SuggestedNewsViewHolder
                viewHolderItem.suggestedNewsTitle.text = detailList.get(itemIndex).title
                val vto = viewHolderItem.suggestedNewsImage.viewTreeObserver
                vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        viewHolderItem.suggestedNewsImage.viewTreeObserver.removeOnPreDrawListener(this)
                        if (detailList.get(position - 1).cover_image != null && detailList.get(position - 1).cover_image.length > 0) {
                            var imageUrl = getImageURL(viewHolderItem.suggestedNewsImage, detailList.get(position - 1).cover_image)
                            Glide.with(context).load(imageUrl).apply(requestOptions)
                                    .apply(RequestOptions.timeoutOf(5 * 60 * 1000))
                                    .placeholder(R.drawable.image_not_found)
                                    .error(R.drawable.image_not_found)
                                    .into(viewHolderItem.suggestedNewsImage)
                        } else {
                            Glide.with(context).load(R.drawable.image_not_found)
                                    .into(viewHolderItem.suggestedNewsImage)
                        }
                        return true
                    }
                })

                viewHolderItem.parentLayout.setOnClickListener {
                    itemIndex = position - 1
                    var detailIntent = Intent(context, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", itemIndex)
                    if (detailList.isNotEmpty()) {
                        detailIntent.putParcelableArrayListExtra("arrayList", detailList)
                    }
                    context.startActivity(detailIntent)
                }
            }
        }
    }

    inner class SuggestedNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var parentLayout = itemView.findViewById<ConstraintLayout>(R.id.root_layout)
        var suggestedNewsImage = itemView.findViewById<ImageView>(R.id.imgView_suggested)
        var suggestedNewsTitle = itemView.findViewById<TextView>(R.id.tv_title_suggested)
    }

    inner class SuggestedNewsHeaderViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        var recommendedNewsTitle = itemView.findViewById<TextView>(R.id.tv_recommended_title)
    }

    fun setData(list: ArrayList<DetailNewsData>) {
        this.detailList.clear()
        this.detailList = list
        notifyDataSetChanged()
    }

    fun convertDpToPx(context: Context, dp: Int): Int {
        return dp * context.resources.displayMetrics.density.toInt()
    }
}