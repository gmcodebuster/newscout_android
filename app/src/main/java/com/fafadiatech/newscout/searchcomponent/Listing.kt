package com.fafadiatech.newscout.searchcomponent

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class Listing<T>(
        val pagedList: LiveData<PagedList<T>>,
        val networkState: LiveData<NetworkState>,
        val refreshState: LiveData<NetworkState>,
        val refresh: () -> Unit,
        val retry: () -> Unit
)

enum class Status{
    RUNNING,
    SUCCESS,
    FAILED
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
        val status: Status,
        val msg: String? = null
){
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)

        fun error(msg: String?) = NetworkState(
                Status.FAILED,
                msg
        )
    }
}