package com.fafadiatech.newscout.searchcomponent

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.model.ArticlesData
import com.fafadiatech.newscout.model.NewsDataApi
import java.util.concurrent.Executor

class GNewsPageKeyedDataSource(
        private val searchQuery: String,
        private val apiService: GNewsApiService,
        private val retryExecutor: Executor
) : PageKeyedDataSource<Int, ArticlesData>() {

    var retry: (() -> Any)? = null
    val network = MutableLiveData<NetworkState>()
    val initial = MutableLiveData<NetworkState>()

    override fun loadBefore(params: LoadParams<Int>,
                            callback: LoadCallback<Int, ArticlesData>) {
        // ignored, since we only ever append to our initial load
    }

    override fun loadInitial(params: LoadInitialParams<Int>,
                             callback: LoadInitialCallback<Int, ArticlesData>) {

        val currentPage = 1
        val nextPage = currentPage + 1

        makeLoadInitialRequest(params, callback, currentPage, nextPage)
    }

    private fun makeLoadInitialRequest(params: LoadInitialParams<Int>,
                                       callback: LoadInitialCallback<Int, ArticlesData>,
                                       currentPage: Int,
                                       nextPage: Int) {

        // triggered by a refresh, we better execute sync
        apiService.searchUsersSync(
                query = searchQuery,
                page = currentPage,
                perPage = params.requestedLoadSize,
                onPrepared = {
                    postInitialState(NetworkState.LOADING)
                },
                onSuccess = { responseBody ->
                    val items = (responseBody as NewsDataApi)?.body?.results ?: ArrayList<ArticlesData>()//emptyList()
                    retry = null
                    postInitialState(NetworkState.LOADED)
                    callback.onResult(items, null, nextPage)
                },
                onError = { errorMessage ->
                    retry = { loadInitial(params, callback) }
                    postInitialState(NetworkState.error(errorMessage))
                })
    }


    override fun loadAfter(params: LoadParams<Int>,
                           callback: LoadCallback<Int, ArticlesData>) {

        val currentPage = params.key
        val nextPage = currentPage + 1

        makeLoadAfterRequest(params, callback, currentPage, nextPage)
    }


    private fun makeLoadAfterRequest(params: LoadParams<Int>,
                                     callback: LoadCallback<Int, ArticlesData>,
                                     currentPage: Int,
                                     nextPage: Int) {

        apiService.searchUsersAsync(
                query = searchQuery,
                page = currentPage,
                perPage = params.requestedLoadSize,
                onPrepared = {
                    postAfterState(NetworkState.LOADING)
                },
                onSuccess = { responseBody ->
                    val items = (responseBody as NewsDataApi)?.body?.results ?: ArrayList<ArticlesData>()//emptyList()
                    retry = null
                    callback.onResult(items, nextPage)
                    postAfterState(NetworkState.LOADED)
                },
                onError = { errorMessage ->
                    retry = { loadAfter(params, callback) }
                    postAfterState(NetworkState.error(errorMessage))
                })
    }

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let { retry ->
            retryExecutor.execute { retry() }
        }
    }

    private fun postInitialState(state: NetworkState) {
        network.postValue(state)
        initial.postValue(state)
    }

    private fun postAfterState(state: NetworkState) {
        network.postValue(state)
    }
}