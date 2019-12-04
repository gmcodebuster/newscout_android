package com.fafadiatech.newscout.paging

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.model.INews

class NewsDataSourceFactory(context: Context, nodeId: Int) : DataSource.Factory<Int, INews>() {

    val itemLiveDataSource = MutableLiveData<PageKeyedDataSource<Int, INews>>()
    var query: Int
    var mContext: Context

    init {
        query = nodeId
        mContext = context
    }

    override fun create(): DataSource<Int, INews> {
        var newsDataSource = NewsItemDataSource(mContext, query)
        itemLiveDataSource.postValue(newsDataSource)
        return newsDataSource
    }

    fun getNewsSourceData(): MutableLiveData<PageKeyedDataSource<Int, INews>> {
        return itemLiveDataSource
    }
}

