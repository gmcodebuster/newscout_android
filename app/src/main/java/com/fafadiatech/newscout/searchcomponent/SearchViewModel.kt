package com.fafadiatech.newscout.searchcomponent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel

class SearchViewModel(
        private val repository: GNewsRepository
) : ViewModel() {

    private val searchQuery = MutableLiveData<String>()
    private val itemResult = map(searchQuery) {
        repository.searchNews(it, PAGE_SIZE)
    }

    val items = switchMap(itemResult) { it.pagedList }!!
    val networkState = switchMap(itemResult) { it.networkState }!!
    val refreshState = switchMap(itemResult) { it.refreshState }!!

    fun refresh() {
        itemResult.value?.refresh?.invoke()
    }

    fun showSearchResults(searchQuery: String): Boolean {
        if (this.searchQuery.value == searchQuery) {
            return false
        }
        this.searchQuery.value = searchQuery
        return true
    }

    fun retry() {
        val listing = itemResult?.value
        listing?.retry?.invoke()
    }

    fun currentSearchQuery(): String? = searchQuery.value

    override fun onCleared() {
        super.onCleared()
    }

    companion object {
        const val PAGE_SIZE: Int = 1
    }
}