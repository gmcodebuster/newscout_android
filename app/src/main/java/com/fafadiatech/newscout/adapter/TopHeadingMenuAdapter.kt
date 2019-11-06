package com.fafadiatech.newscout.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.KEY_NAME
import com.fafadiatech.newscout.callback.MenuDiffCallback
import com.fafadiatech.newscout.interfaces.MenuHeaderClickListener
import com.fafadiatech.newscout.model.MenuHeading
import com.fafadiatech.newscout.model.TopHeadingData


class TopHeadingMenuAdapter(var context: Context, var clickListener: MenuHeaderClickListener) :
        RecyclerView.Adapter<TopHeadingMenuAdapter.MyViewHolder>() {

    var headingDataList = ArrayList<MenuHeading>()
    var selectedItem = 0
    var selectedItemPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopHeadingMenuAdapter.MyViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.top_header_recyclerview_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (headingDataList == null) {
            return 0
        }
        return headingDataList.size
    }

    override fun onBindViewHolder(holder: TopHeadingMenuAdapter.MyViewHolder, position: Int) {

        if (position == 0) {
            holder.imgViewMenu.setImageResource(R.drawable.trending_news)
        } else if (position == 1) {
            holder.imgViewMenu.setImageResource(R.drawable.latest_news)
        } else if (position == 2) {
            holder.imgViewMenu.setImageResource(R.drawable.sector_update)
        } else if (position == 3) {
            holder.imgViewMenu.setImageResource(R.drawable.internet)
        } else if (position == 4) {
            holder.imgViewMenu.setImageResource(R.drawable.finance)
        } else if (position == 5) {
            holder.imgViewMenu.setImageResource(R.drawable.economy)
        } else if (position == 6) {
            holder.imgViewMenu.setImageResource(R.drawable.news)
        } else {
            holder.imgViewMenu.setImageResource(R.drawable.trending_news)
        }

        holder.tView.text = headingDataList.get(position).name
        var selectedColor = ContextCompat.getColor(context, R.color.black)
        holder.tView.setTextColor(selectedColor)
        holder.imgViewMenu.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)

        holder.tView.setOnClickListener {
            var headingData = TopHeadingData(headingDataList.get(position).id, position, headingDataList.get(position).name)
            setPosition(position)
            clickListener.onClick(headingData)
            selectedItem = position
            notifyDataSetChanged()
        }

        if (position == selectedItem) {
            var selectedColor = ContextCompat.getColor(context, R.color.primaryColorNs)
            holder.tView.setTextColor(selectedColor)
            holder.imgViewMenu.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            val bundle = payloads.get(0) as Bundle
            for (key in bundle.keySet()) {
                if (key.equals(KEY_NAME)) {
                    holder.tView.text = headingDataList.get(position).name
                }
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tView = itemView.findViewById<TextView>(R.id.tview_item)
        var imgViewMenu = itemView.findViewById<ImageView>(R.id.img_menu_item)
    }

    fun setHeadingData(list: ArrayList<MenuHeading>) {
        headingDataList = list
        notifyDataSetChanged()
    }

    fun getHeadingData(): ArrayList<MenuHeading> {
        return headingDataList
    }

    fun getObject(position: Int): MenuHeading {
        if (headingDataList != null && headingDataList.size > 0) {
            return headingDataList?.get(position)
        }
        return MenuHeading(0, "")
    }

    fun notifyChanges(newList: List<MenuHeading>) {
        val diffResult = DiffUtil.calculateDiff(MenuDiffCallback(this.headingDataList, newList))
        diffResult.dispatchUpdatesTo(this)
        this.headingDataList.clear()
        this.headingDataList.addAll(newList)
    }

    fun getPosition(): Int {
        return this.selectedItemPosition
    }

    fun setPosition(pos: Int) {
        this.selectedItemPosition = pos
    }
}