package com.fafadiatech.newscout.searchcomponent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.application.GlideRequests
import com.fafadiatech.newscout.model.ArticlesData

class NewsSearchAdapter(
        private val glideRequests: GlideRequests,
        private val retryCallback: () -> Unit
) : PagedListAdapter<ArticlesData, RecyclerView.ViewHolder>(ITEM_COMPARATOR) {

    private var networkState: NetworkState? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.news_item_main ->{ GNewsItemViewHolder.create(parent, glideRequests)
            }
            R.layout.news_item_alternate -> {
                GNewsItemRightViewHolder.create(parent, glideRequests)
            }
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.news_item_main -> (holder as GNewsItemViewHolder).bind(getItem(position))
            R.layout.news_item_alternate -> (holder as GNewsItemRightViewHolder).bind(getItem(position))
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bind(networkState)
        }
    }


    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {

            R.layout.network_state_item
        } else {
            if(getItem(position) is ArticlesData){
                if(position % 2 == 0){
                    return R.layout.news_item_alternate  //Right
                }else{
                    return R.layout.news_item_main  //Left
                }
            } else{
                return R.layout.news_item_main //ADVERTISE VIEW
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    class LeftItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemRootView = view.findViewById<ConstraintLayout>(R.id.root_layout_news_item_alternate)
        var newsSourceLeft = view.findViewById<TextView>(R.id.news_source_alternate)
        var newsTitleLeft = view.findViewById<TextView>(R.id.news_title_alternate)
        var newsImageLeft = view.findViewById<ImageView>(R.id.news_image_alternate)
        var newsTimeLeft = view.findViewById<TextView>(R.id.news_time_alternate)
    }

    companion object {
        private val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ArticlesData>() {
            override fun areItemsTheSame(oldItem: ArticlesData, newItem: ArticlesData): Boolean =
                    oldItem.id == newItem.id



            override fun areContentsTheSame(oldItem: ArticlesData, newItem: ArticlesData): Boolean =
                    oldItem == newItem



        }
    }

}