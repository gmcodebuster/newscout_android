package com.fafadiatech.newscout.searchcomponent

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations.switchMap
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.fafadiatech.newscout.model.ArticlesData
import java.util.concurrent.Executor

class InMemoryByPageKeyRepository(
        private val gnewsApi: GNewsApiService,
        private val networkExecutor: Executor
) : GNewsRepository {

    @MainThread
    override fun searchNews(searchQuery: String, pageSize: Int): Listing<ArticlesData> {
        val factory = gNewsDataSourceFactory(searchQuery)

        val config = pagedListConfig(pageSize)

        val livePagedList = LivePagedListBuilder(factory, config)
                .setFetchExecutor(networkExecutor)
                .build()
        return Listing(
                pagedList = livePagedList,
                networkState = switchMap(factory.source) { it.network },
                retry = { factory.source.value?.retryAllFailed() },
                refresh = { factory.source.value?.invalidate() },
                refreshState = switchMap(factory.source) { it.initial })
    }

    private fun gNewsDataSourceFactory(searchQuery: String): GNewsDataSourceFactory {
        return GNewsDataSourceFactory(searchQuery, gnewsApi, networkExecutor)
    }

    private fun pagedListConfig(pageSize: Int): PagedList.Config {
        return PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                //.setInitialLoadSizeHint(pageSize * 2)
                .setPageSize(20)
                .build()
    }
}