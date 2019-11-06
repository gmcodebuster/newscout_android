package com.fafadiatech.newscout.callback

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.fafadiatech.newscout.appconstants.KEY_NAME
import com.fafadiatech.newscout.model.MenuHeading

class MenuDiffCallback(oldList: List<MenuHeading>, newList: List<MenuHeading>) : DiffUtil.Callback() {
    var mOldList = listOf<MenuHeading>()
    var mNewList = listOf<MenuHeading>()

    init {
        mOldList = oldList
        mNewList = newList
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mNewList?.get(newItemPosition)?.id == mOldList?.get(oldItemPosition
        )?.id
    }

    override fun getOldListSize(): Int {
        if (mOldList.isNullOrEmpty()) {
            return 0
        } else {
            return mOldList.size
        }
    }

    override fun getNewListSize(): Int {
        if (mNewList.isNullOrEmpty()) {
            return 0
        } else {
            return mNewList.size
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newItem = mNewList.get(newItemPosition)
        val oldItem = mOldList.get(oldItemPosition)
        return oldItem.name.equals(newItem.name)
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldMenu = mOldList.get(oldItemPosition)
        val newMenu = mNewList.get(newItemPosition)
        var diffBundle = Bundle()
        if (!newMenu.name.equals(oldMenu.name)) {
            diffBundle.putString(KEY_NAME, newMenu.name)
        }

        if (diffBundle.size() == 0) return null
        return diffBundle
    }
}