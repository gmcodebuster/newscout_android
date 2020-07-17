package com.fafadiatech.newscout.searchcomponent

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.fafadiatech.newscout.model.ArticlesData
import java.util.concurrent.Executor

class GNewsDataSourceFactory(
        private val searchQuery: String,
        private val gnewsApi: GNewsApiService,
        private val retryExecutor: Executor
) : DataSource.Factory<Int, ArticlesData>() {

    val source = MutableLiveData<GNewsPageKeyedDataSource>()

    override fun create(): DataSource<Int, ArticlesData> {
        val source = GNewsPageKeyedDataSource(searchQuery, gnewsApi, retryExecutor)
        this.source.postValue(source)
        return source
    }
}