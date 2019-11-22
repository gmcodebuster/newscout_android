package com.fafadiatech.newscout.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.model.MenuModel
import com.fafadiatech.newscout.model.TopMenuModel

class ExpandListAdapter(
        var context: Context,
        var listDataHeader: List<TopMenuModel>,
        var listDataChild: HashMap<TopMenuModel, List<MenuModel>?>
) : BaseExpandableListAdapter() {

    var selectedItem = 0

    override fun getGroup(groupPosition: Int): TopMenuModel {
        return this.listDataHeader.get(groupPosition)
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val headerTitle = getGroup(groupPosition).menuName

        var view: View
        if (convertView == null) {
            val infalInflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = infalInflater.inflate(R.layout.list_group_header, null)
        } else {
            view = convertView
        }

        val lblListHeader = view.findViewById<TextView>(R.id.lblListHeader)
        var imgViewMenu = view.findViewById<ImageView>(R.id.img_menu_item)
        var selectedColor = ContextCompat.getColor(context, R.color.primaryTextColor)

        if (groupPosition == 0) {
            imgViewMenu.setImageResource(R.drawable.trending_news)
        } else if (groupPosition == 1) {
            imgViewMenu.setImageResource(R.drawable.latest_news)
        } else if (groupPosition == 2) {
            imgViewMenu.setImageResource(R.drawable.sector_update)
        } else if (groupPosition == 3) {
            imgViewMenu.setImageResource(R.drawable.internet)
        } else if (groupPosition == 4) {
            imgViewMenu.setImageResource(R.drawable.finance)
        } else if (groupPosition == 5) {
            imgViewMenu.setImageResource(R.drawable.economy)
        } else if (groupPosition == 6) {
            imgViewMenu.setImageResource(R.drawable.news)
        } else {
            imgViewMenu.setImageResource(R.drawable.trending_news)
        }

        lblListHeader.text = headerTitle
        imgViewMenu.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)

        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        if (this.listDataChild.get(this.listDataHeader.get(groupPosition)) == null) {
            return 0
        } else {
            val data = this.listDataChild.get(this.listDataHeader.get(groupPosition))
            if (data != null) {
                return data.size
            } else {
                return 0
            }
        }
    }

    override fun getChild(groupPosition: Int, childPosition: Int): MenuModel {
        return listDataChild.get(listDataHeader.get(groupPosition))!!.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childText: String? = getChild(groupPosition, childPosition).subMenuData?.name
        var view: View
        if (convertView == null) {
            var infalInflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = infalInflater.inflate(R.layout.list_group_child, null)
        } else {
            view = convertView
        }

        val txtListChild = view.findViewById<TextView>(R.id.lblListItem)
        if (!childText.isNullOrBlank())
            txtListChild.text = childText

        return view
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return this.listDataHeader.size
    }

    fun setItemSelection(position: Int) {
        selectedItem = position
    }
}