package com.fafadiatech.newscout.searchcomponent

import com.fafadiatech.newscout.model.ArticlesData

interface OnNewsItemClickListener {
    fun onItemClick(item: ArticlesData?, position: Int)
}