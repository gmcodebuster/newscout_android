package com.fafadiatech.newscout.paging

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.db.NewsEntity

class NewsDataSourceFactory(context: Context, nodeId: Int) : DataSource.Factory<Int, NewsEntity>() {

    val itemLiveDataSource = MutableLiveData<PageKeyedDataSource<Int, NewsEntity>>()
    var query: Int
    var mContext: Context

    init {
        query = nodeId
        mContext = context
    }

    override fun create(): DataSource<Int, NewsEntity> {
        var newsDataSource = NewsItemDataSource(mContext, query)
        itemLiveDataSource.postValue(newsDataSource)

        return newsDataSource
    }

    fun getNewsSorceData(): MutableLiveData<PageKeyedDataSource<Int, NewsEntity>> {
        return itemLiveDataSource
    }
}

