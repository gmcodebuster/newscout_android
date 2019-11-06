package com.fafadiatech.newscout.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.NEWSPAGESIZE
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.*
import com.fafadiatech.newscout.db.dailydigest.DailyDigestEntity
import com.fafadiatech.newscout.db.trending.TrendingData
import com.fafadiatech.newscout.db.trending.TrendingNewsEntity
import com.fafadiatech.newscout.model.*
import com.fafadiatech.newscout.paging.NewsDataSourceFactory
import com.fafadiatech.newscout.paging.NewsItemDataSource
import com.fafadiatech.newscout.workmanager.*


class FetchDataApiViewModel(application: Application, mParams: Int) : AndroidViewModel(application) {
    var apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
    var articleList = MutableLiveData<ArrayList<ArticlesData>>()
    var cursorNext: String? = null
    var nextPageUrl: String? = null
    var newsList = ArrayList<NewsEntity>()
    var prevPageUrl: String? = null
    var cursorPrev: String? = null
    var list = ArrayList<NewsEntity>()
    var articleDataList = ArrayList<ArticlesData>()
    var categoryMutableList = MutableLiveData<ArrayList<CategoryData>>()
    var categoryList = ArrayList<String>()
    var workManager: WorkManager = WorkManager.getInstance()
    private val repository = NewsRepository(application)
    var queryTag = 1
    var itemPagedList: LiveData<PagedList<NewsEntity>>
    var liveDataSource: LiveData<PageKeyedDataSource<Int, NewsEntity>>
    var detailList = ArrayList<DetailNewsData>()
    lateinit var articleNewsDao: NewsDao
    var newsDatabase: NewsDatabase? = null
    lateinit var newsItemPagedList: LiveData<PagedList<NewsEntity>>
    lateinit var ddnewsItemPagedList: LiveData<PagedList<DailyDigestEntity>>

    init {
        newsDatabase = NewsDatabase.getInstance(application.baseContext)
        articleNewsDao = newsDatabase!!.newsDao()
        queryTag = mParams
        var itemDataSourceFactory = NewsDataSourceFactory(application, queryTag)
        liveDataSource = itemDataSourceFactory.getNewsSorceData()
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(NEWSPAGESIZE).build()

        itemPagedList = LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                .build()
    }

    constructor(application: Application) : this(application, 1) {
    }

    fun getCursorNext(category: String) {
        cursorNext = MyApplication.hashMapNextUrl.get(category)
    }

    fun getCursorPrev(category: String) {
        cursorPrev = MyApplication.hashMapPrevUrl.get(category)
    }

    fun getNextNews(token: String, category: String) {
        getCursorNext(category)
        getCursorPrev(category)

        if (!MyApplication.hashMapNextUrl.containsKey(category)) {
            cursorNext = ""
        }

        if (cursorNext != null) {

            startArticleWorkManager(category, cursorNext)
        } else {
            Toast.makeText(getApplication(), "Reached at the end of news", Toast.LENGTH_SHORT).show()
        }
    }

    fun startArticleWorkManager(tag: String, cursor: String?) {
        var dbArticleInsertWork = OneTimeWorkRequest.Builder(DbInsertArticleWork::class.java)
        var data = Data.Builder()
        data.putString("tag_value", tag)
        data.putString("cursor_string_value", cursor)
        dbArticleInsertWork.setInputData(data.build())
        workManager.enqueue(dbArticleInsertWork.build())
    }

    fun deleteSearchTableWork() {
        var deleteSearchDataWork = OneTimeWorkRequest.Builder(DeleteSearchTableWork::class.java)
        workManager.enqueue(deleteSearchDataWork.build())
    }

    fun startRecommendNewsWorkManager(newsId: Int) {
        var dbArticleInsertWork = OneTimeWorkRequest.Builder(DbInsertRecommendedNewsWork::class.java)
        var data = Data.Builder()
        data.putInt("recommended_news_id", newsId)
        dbArticleInsertWork.setInputData(data.build())
        workManager.enqueue(dbArticleInsertWork.build())
    }

    fun startMenuWorkManager() {
        var menuWork = OneTimeWorkRequest.Builder(DbInsertMenuWork::class.java)
        workManager.enqueue(menuWork.build())
    }

    fun startVoteServerDataWorkManager(token: String) {
        var data = Data.Builder()
        data.putString("token_value_from_sign_in", token)
        var r1: OneTimeWorkRequest = OneTimeWorkRequest.Builder(DbInsertLikeServerDataWork::class.java)
                .setInputData(data.build()).build()
        var r2: OneTimeWorkRequest = OneTimeWorkRequest.Builder(DbInsertBookmarkServerData::class.java)
                .setInputData(data.build()).build()
        var continuation: WorkContinuation = WorkManager.getInstance().beginWith(r1)
        continuation.then(r2).enqueue()
    }

    fun startBookmarkWorkManager(token: String, isBookmark: Int, newsId: Int) {
        var bookmarkInsertWork = OneTimeWorkRequest.Builder(DbInsertBookmarkWork::class.java)
        var data = Data.Builder()
        data.putString("token_value_from_detail", token)
        data.putInt("isbookmark_value", isBookmark)
        data.putInt("news_id_detail", newsId)
        bookmarkInsertWork.setInputData(data.build())
        workManager.enqueue(bookmarkInsertWork.build())
    }

    fun getNewsFromDb(): LiveData<List<NewsEntity>> {

        var newsList: LiveData<List<NewsEntity>> = repository.getRepoNews()
        return newsList
    }

    fun getDetailNewsFromDb(): LiveData<List<DetailNewsData>> {
        var list: LiveData<List<DetailNewsData>> = repository.getRepoDetailNewsFromDb()
        return list
    }

    fun getDetailSearchNewsFromDb(): List<DetailNewsData> {
        var list: List<DetailNewsData> = repository.getRepoDetalSearchNewsFromDb()
        return list
    }

    fun getDetailRecommendNewsFromDb(): LiveData<List<DetailNewsData>> {
        var list: LiveData<List<DetailNewsData>> = repository.getRepoRecommendNewsFromDb()
        return list
    }

    fun getDetailTopFiveFromDb(): List<DetailNewsData> {
        var list: List<DetailNewsData> = repository.getTopFiveFromDb()
        return list
    }

    fun getSuffledNewsFromDb(): List<DetailNewsData> {
        var list: List<DetailNewsData> = repository.getShuffledNewsFromDb()
        return list
    }

    fun getLikeDataFromDb(): LiveData<List<LikeEntity>> {
        var result: LiveData<List<LikeEntity>> = repository.getRepoLikeDataFromDb()
        return result
    }

    fun getBookmarkNewsFromDb(): LiveData<List<DetailNewsData>> {
        var result: LiveData<List<DetailNewsData>> = repository.getRepoBookmarkNewsFromDb()
        return result
    }

    fun getBookmarkListFromDb(): List<DetailNewsData> {
        var result: List<DetailNewsData> = repository.getRepoBookmarkListFromDb()
        return result
    }

    fun getMenuHeadingFromDb(): LiveData<List<MenuHeading>> {
        var list: LiveData<List<MenuHeading>> = repository.getRepoMenuHeadingFromDb()
        return list
    }

    fun getMenuHeadingListFromDb(): List<MenuHeading> {
        var list: List<MenuHeading> = repository.getRepoMenuHeadingListFromDb()
        return list
    }

    fun getSubMenuDataFromDb(headingId: Int): LiveData<List<SubMenuResultData>> {
        var list: LiveData<List<SubMenuResultData>> = repository.getRepoSubMenuDataFromDb(headingId)
        return list
    }

    fun getDetailNewsFromDb(categoryId: Int): List<DetailNewsData> {
        var result = repository.getDetailNewsByNodeId(categoryId)
        return result
    }

    fun getDefaultDetailNewsFromDb(categoryId: Int): List<DetailNewsData> {
        var result = repository.getDefaultDetailNewsByNodeId(categoryId)
        return result
    }

    fun getTrendingDataFromDb(): LiveData<List<TrendingNewsData>> {
        var result = repository.getRepoTrendingDataFromDb()
        var resultTrending = repository.getAllTrendingData()

        return result
    }

    fun getTrendingByClusterIdFromDb(clusterId: Int): LiveData<List<NewsEntity>> {
        var result = repository.getRepoTrendingByClusterIdFromDb(clusterId)
        return result
    }

    fun getTrendingDetailByClusterIdFromDb(clusterId: Int): LiveData<List<DetailNewsData>> {
        var result = repository.getRepoTrendingDetailByClusterIdFromDb(clusterId)
        return result
    }

    fun getSearchSuggestedData(): LiveData<List<String>> {
        var result = repository.getSearchSuggestionFromDb()
        return result
    }

    fun startSearchSuggestionWorkManager(query: String) {
        var dbSearchSuggestInsertWork = OneTimeWorkRequest.Builder(DbInsertSearchSuggestionWork::class.java)
        var data = Data.Builder()
        data.putString("search_query_text", query)
        dbSearchSuggestInsertWork.setInputData(data.build())
        workManager.enqueue(dbSearchSuggestInsertWork.build())
    }

    fun getTitleBySearch(query: String): List<String> {
        var list = repository.getTitleBySearch(query)
        return list
    }

    fun setCurrentList(mList: List<NewsEntity>) {
        this.detailList.clear()
        var listMap: List<DetailNewsData> = mList.map { DetailNewsData(it.id, it.title, it.source, it.category, it.source_url, it.cover_image, it.blurb!!, it.published_on, 0, 0) };
        this.detailList.addAll(listMap)
    }

    fun initializeNews(cateId: Int, pageNo: Int): LiveData<PagedList<NewsEntity>> {
        newsItemPagedList = repository.selectSource(cateId, pageNo)
        return newsItemPagedList
    }

    fun invalidateDataSource() {
        repository.invalidateDataSourceFactory()
        repository
    }

    fun getAllTrendingData(): LiveData<List<TrendingData>> {
        var result = repository.getAllTrendingData3()
        return result
    }

    fun getAllTrendingEntity(): LiveData<List<TrendingNewsEntity>> {
        var result = repository.getAllTrendingEntity()
        return result
    }

    fun initializeDailyDigestNews(deviceId: String): LiveData<PagedList<DailyDigestEntity>> {
        ddnewsItemPagedList = repository.selectDailyDigestSource(deviceId)
        return ddnewsItemPagedList
    }

    fun dailyDigestDetailNews(): LiveData<List<DetailNewsData>> {
        var list: LiveData<List<DetailNewsData>> = repository.getDDDetailNewsFromDb()
        return list
    }

    fun getTrendingData(): LiveData<List<TrendingNewsData>> {
        var result = repository.getRepoTrendingDataFromDb()
        return result
    }
}