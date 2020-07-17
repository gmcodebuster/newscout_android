package com.fafadiatech.newscout.searchcomponent

import com.fafadiatech.newscout.model.NewsDataApi

class GNewsApiService(
        private val gnewsApi: GNewsApi
) {

    fun searchUsersSync(query: String, page: Int, perPage: Int,
                        onPrepared: () -> Unit,
                        onSuccess: (NewsDataApi?) -> Unit,
                        onError: (String) -> Unit) {

        val request = gnewsApi.searchUsers(query, page)
        onPrepared()
        ApiRequestHelper.syncRequest(request, onSuccess, onError)
    }

    fun searchUsersAsync(query: String, page: Int, perPage: Int,
                         onPrepared: () -> Unit,
                         onSuccess: (NewsDataApi?) -> Unit,
                         onError: (String) -> Unit) {

        val request = gnewsApi.searchUsers(query, page)
        onPrepared()
        ApiRequestHelper.asyncRequest(request, onSuccess, onError)
    }
}