package com.fafadiatech.newscout.paging

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.model.INews

class SgstDataSourceFactory(context: Context, newsId: Int) : DataSource.Factory<Int, INews>() {
    val itemLiveDataSource = MutableLiveData<PageKeyedDataSource<Int, INews>>()
    var newsId: Int
    var mContext: Context

    init {
        this.newsId = newsId
        mContext = context
    }

    override fun create(): DataSource<Int, INews> {
        var newsDataSource = SgstItemDataSource(mContext, newsId)
        itemLiveDataSource.postValue(newsDataSource)
        return newsDataSource
    }

    fun getNewsSourceData(): MutableLiveData<PageKeyedDataSource<Int, INews>> {
        return itemLiveDataSource
    }
}