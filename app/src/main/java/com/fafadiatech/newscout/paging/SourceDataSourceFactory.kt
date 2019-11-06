package com.fafadiatech.newscout.paging

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.db.NewsEntity

class SourceDataSourceFactory(context: Context, query: String) : DataSource.Factory<Int, NewsEntity>() {

    val itemLiveDataSource = MutableLiveData<PageKeyedDataSource<Int, NewsEntity>>()
    var queryString: String
    var mContext: Context

    init {
        queryString = query
        mContext = context
    }

    override fun create(): DataSource<Int, NewsEntity> {
        var newsDataSource = SourceItemDataSource(mContext, queryString)
        itemLiveDataSource.postValue(newsDataSource)
        return newsDataSource
    }
}