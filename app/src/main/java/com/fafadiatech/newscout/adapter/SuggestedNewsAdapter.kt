package com.fafadiatech.newscout.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.DetailNewsActivity
import com.fafadiatech.newscout.adapter.NewsAdapter.Companion.DIFF_CALLBACK
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.model.INews

class SuggestedNewsAdapter(val context: Context) : PagedListAdapter<DetailNewsData, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    val TAG: String = "SuggestedNewsAdapter"
    var itemIndex: Int = 0
    var detailList = ArrayList<DetailNewsData>()
    var widthInPixel: Int = 0
    var heightInPixel: Int = 0
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    lateinit var themePreference: SharedPreferences
    var interfaceObj: ApiInterface

    init {
        widthInPixel = convertDpToPx(context, 160)
        heightInPixel = convertDpToPx(context, 80)
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        themePreference = context.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
    }

    companion object {
        var TAG: String = "SuggestedNewsAdapter"

        val DIFF_CALLBACK: DiffUtil.ItemCallback<DetailNewsData> = object : DiffUtil.ItemCallback<DetailNewsData>() {
            override fun areItemsTheSame(oldData: DetailNewsData, newData: DetailNewsData): Boolean {
                if(oldData is DetailNewsData && newData is DetailNewsData) {
                    return oldData.article_id == newData.article_id
                }
                return false
            }

            override fun areContentsTheSame(oldData: DetailNewsData, newData: DetailNewsData): Boolean {
                if(oldData is DetailNewsData && newData is DetailNewsData) {
                    return oldData.equals(newData)
                }
                return false
            }
        }
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SuggestedNewsHeaderViewHolder(inflater.inflate(R.layout.layout_recommended_news_title, null))
            else -> SuggestedNewsViewHolder(inflater.inflate(R.layout.suggested_news_item_bottom, null))
        }
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

                val count:Int  = itemCount
                itemIndex = position - 1
                var viewHolderItem = holder as SuggestedNewsViewHolder
                val preNews = getItem(position -1) as DetailNewsData
                viewHolderItem.suggestedNewsTitle.text = preNews.title
                val vto = viewHolderItem.suggestedNewsImage.viewTreeObserver
                vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        viewHolderItem.suggestedNewsImage.viewTreeObserver.removeOnPreDrawListener(this)
                        if (preNews.cover_image != null && preNews.cover_image.length > 0) {
                            var imageUrl = getImageURL(viewHolderItem.suggestedNewsImage, preNews.cover_image)
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

                    val data = currentList?.snapshot()
                    data?.let {
                        detailList = data?.toList() as ArrayList<DetailNewsData>
                    }
                    var deviceId = themePreference.getString("device_token", "")
                    val newsId = preNews.article_id
                    val itemName = preNews.title
                    val cName = preNews.category
                    val cId = MyApplication.categoryIdHashMap.get(cName) ?: 0
                    val sourceName = preNews.source

                    var detailIntent = Intent(context, DetailNewsActivity::class.java)
                    detailIntent.putExtra("indexPosition", itemIndex)
                    if (currentList?.size ?: 0 != 0) {
                        detailIntent.putParcelableArrayListExtra("arrayList", detailList)
                    }
                    val sessionId = getUniqueCode(context, themePreference)
                    trackingCallback(interfaceObj, themePreference, newsId, itemName, cId,cName,"", ActionType.RECOMMENDATION.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, sourceName, 0)

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