package com.fafadiatech.newscout.searchcomponent

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.model.INews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsDataSource @Inject constructor(val nApi: ApiInterface) : PageKeyedDataSource<Int, INews>() {

    private val completableJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + completableJob)
    private var retry: (() -> Any)? = null
    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed(){
        val prevRetry = retry
        retry = null
        prevRetry?.invoke()
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, INews>) {
        coroutineScope.launch(Dispatchers.IO) {
            try{
                initialLoad.postValue(NetworkState.LOADING)
//                val primaryNewsResponse = callLatestNewsAsync(1)
            }catch (e:Exception){

            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, INews>) {
        TODO("Not yet implemented")
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, INews>) {
        TODO("Not yet implemented")
    }

//    private suspend fun callLatestNewsAsync(pageNumber: Int): Response<Any> = nApi.getNewsFromSource(pageNumber.toString())
}