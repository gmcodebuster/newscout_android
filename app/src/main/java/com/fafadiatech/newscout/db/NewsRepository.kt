package com.fafadiatech.newscout.db

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.fafadiatech.newscout.appconstants.NEWSPAGESIZE
import com.fafadiatech.newscout.appconstants.TRENDING_ID
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.dailydigest.DailyDigestEntity
import com.fafadiatech.newscout.db.trending.TrendingData
import com.fafadiatech.newscout.db.trending.TrendingNewsEntity
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.model.MenuHeading
import com.fafadiatech.newscout.model.SubMenuResultData
import com.fafadiatech.newscout.model.TrendingNewsData
import com.fafadiatech.newscout.paging.DBNewsDataSource
import com.fafadiatech.newscout.paging.DDNewsDataSourceFactory
import com.fafadiatech.newscout.paging.NewsDataSourceFactory

class NewsRepository(application: Application) {
    var rNewsDao: NewsDao
    private var newsDatabase: NewsDatabase? = null
    lateinit var newsList: LiveData<List<NewsEntity>>
    lateinit var categoryNewsList: LiveData<List<String>>
    lateinit var categoryArticlesList: LiveData<List<NewsEntity>>
    lateinit var dataSourceFactory: NewsDataSourceFactory
    lateinit var dbData: DBNewsDataSource
    lateinit var itemPagedList: LiveData<PagedList<NewsEntity>>
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, NewsEntity>>
    lateinit var ddLiveDataSource: LiveData<PageKeyedDataSource<Int, DailyDigestEntity>>
    lateinit var ddItemPagedList: LiveData<PagedList<DailyDigestEntity>>
    lateinit var application: Application
    var newsPagedList: LiveData<PagedList<NewsEntity>>? = null
    var ddNewsPagedList: LiveData<PagedList<DailyDigestEntity>>? = null
    lateinit var factory: DataSource.Factory<Int, NewsEntity>
    lateinit var ddfactory: DataSource.Factory<Int, DailyDigestEntity>

    init {
        this.application = application
        newsDatabase = NewsDatabase.getInstance(application)
        rNewsDao = newsDatabase!!.newsDao()
        dataSourceFactory = NewsDataSourceFactory(application, 0)
        dbData = DBNewsDataSource(application.baseContext, 0, 1)
    }

    companion object {
        private const val DATABASE_PAGE_SIZE = 10
    }
    
    fun getRepoNews(): LiveData<List<NewsEntity>> {
        newsList = rNewsDao.getNewsFromDb()
        return newsList
    }

    fun getCategoryList(): LiveData<List<String>> {
        categoryNewsList = rNewsDao.getCategoryListFromDb()
        return categoryNewsList
    }

    fun getTitleBySearch(query: String): List<String> {
        var list = rNewsDao.getTitleBySearch(query)
        return list
    }

    fun getRepoDetailNewsFromDb(): LiveData<List<DetailNewsData>> {
        var result: LiveData<List<DetailNewsData>> = rNewsDao.getDetailNewsFromDb()
        return result
    }

    fun getRepoDetalSearchNewsFromDb(): List<DetailNewsData> {
        var result: List<DetailNewsData> = rNewsDao.getDetailSearchNewsFromDb()
        return result
    }


    fun getRepoRecommendNewsFromDb(): LiveData<List<DetailNewsData>> {
        var result: LiveData<List<DetailNewsData>> = rNewsDao.getRecommendedNewsFromDb()
        return result
    }

    fun getTopFiveFromDb(): List<DetailNewsData> {
        var result: List<DetailNewsData> = rNewsDao.getTopFiveArticles()
        return result
    }

    fun getShuffledNewsFromDb(): List<DetailNewsData> {
        var result: List<DetailNewsData> = rNewsDao.getShuffledNewsFromDb()
        return result
    }

    fun getRepoLikeDataFromDb(): LiveData<List<LikeEntity>> {
        var result: LiveData<List<LikeEntity>> = rNewsDao.getLikeDataFromDb()
        return result
    }

    fun getRepoBookmarkNewsFromDb(): LiveData<List<DetailNewsData>> {
        var result: LiveData<List<DetailNewsData>> = rNewsDao.getbookmarkedNewsFromDb()
        return result
    }


    fun getRepoBookmarkSearchNewsFromDb(): List<DetailNewsData> {
        var result: List<DetailNewsData> = rNewsDao.getbookmarkedNewsSearchFromDb()
        return result
    }


    fun getRepoBookmarkListFromDb(): List<DetailNewsData> {
        var result: List<DetailNewsData> = rNewsDao.getbookmarkNewsFromDb()
        return result
    }

    fun getRepoDetailNewsByCategoryFromDb(category: Array<String?>): LiveData<List<DetailNewsData>> {
        var result: LiveData<List<DetailNewsData>> = rNewsDao.getDetailNewsByCategory(category)
        return result
    }

    fun deleteSearchTableData() {
        rNewsDao.deleteSearchTableData()
    }

    fun getRepoMenuHeadingFromDb(): LiveData<List<MenuHeading>> {
        var result: LiveData<List<MenuHeading>> = rNewsDao.getMenuHeadingFromDb()
        return result
    }

    fun getRepoMenuHeadingListFromDb(): List<MenuHeading> {
        var result: List<MenuHeading> = rNewsDao.getMenuHeadingListFromDb()
        return result
    }

    fun getRepoSubMenuDataFromDb(headingId: Int): LiveData<List<SubMenuResultData>> {
        var result: LiveData<List<SubMenuResultData>> = rNewsDao.getSubMenuDataFromDb(headingId)
        return result
    }

    fun getRepoSubTagDataFromDb(subMenuId: Int): LiveData<List<String>> {
        var result: LiveData<List<String>> = rNewsDao.getSubMenuTagsFromDb(subMenuId)
        return result
    }

    fun getNewsByRawQuery(query: SupportSQLiteQuery): List<DetailNewsData> {
        var result = rNewsDao.getDetailNewsByRawQuery(query)
        return result
    }

    fun getDetailNewsByNodeId(categoryId: Int): List<DetailNewsData> {
        var result = rNewsDao.getDetailNewsByNodeId(categoryId)
        return result
    }

    fun getDefaultDetailNewsByNodeId(categoryId: Int): List<DetailNewsData> {
        var result = rNewsDao.getDefaultDetailNewsByNodeId(categoryId)
        return result
    }

    fun getRepoTrendingDataFromDb(): LiveData<List<TrendingNewsData>> {
        var result = rNewsDao.getTrendingDataFromDb()
        return result
    }

    fun getRepoTrendingByClusterIdFromDb(clusterId: Int): LiveData<List<NewsEntity>> {
        var result = rNewsDao.getTrendingByClusterId(clusterId)
        return result
    }

    fun getRepoTrendingDetailByClusterIdFromDb(clusterId: Int): LiveData<List<DetailNewsData>> {
        var result = rNewsDao.getTrendingDetailByClusterId(clusterId)
        return result
    }

    fun getSearchSuggestionFromDb(): LiveData<List<String>> {
        var result = rNewsDao.getSearchSuggestionFromDb()
        return result
    }

    fun initializeDBDataSourceFactory(application: Application): LiveData<PagedList<NewsEntity>> {
        val dbData = DBNewsDataSource(application.baseContext, 0, 1)
        val articleDataSource: DataSource.Factory<Int, NewsEntity> = rNewsDao.getPaggedDetailNewsFromDb()
        val articleList = articleDataSource.toLiveData(pageSize = NEWSPAGESIZE)
        return articleList
    }

    fun initializeNetworkCall(application: Application, queryTag: Int): LiveData<PagedList<NewsEntity>> {
        var itemDataSourceFactory = NewsDataSourceFactory(application, queryTag)
        var PAGESIZE = 20
        if (queryTag == TRENDING_ID) {
            PAGESIZE = 30
        }

        liveDataSource = itemDataSourceFactory.getNewsSorceData()
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(NEWSPAGESIZE).build()

        itemPagedList = LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                .build()
        return itemPagedList
    }

    fun initializedbDataSourceFactory(application: Application, cateId: Int): LiveData<PagedList<NewsEntity>> {
        var PAGESIZE = 20
        if (cateId == TRENDING_ID) {
            PAGESIZE = 30
        }
        val pageListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(PAGESIZE).build()

        if (cateId > 0) {
            factory = rNewsDao.getPagedNewsByNodeIdFromDb(cateId)
        } else {
            factory = rNewsDao.getPagedNewsByNodeIdFromDb(cateId)
        }

        factory.toLiveData(pageListConfig)
        val pagedListBuilder: LivePagedListBuilder<Int, NewsEntity> = LivePagedListBuilder<Int, NewsEntity>(factory,
                pageListConfig)
        itemPagedList = pagedListBuilder.build()
        return itemPagedList
    }

    fun selectSource(cateId: Int, pageNo: Int): LiveData<PagedList<NewsEntity>> {

        if (MyApplication.checkInternet) {
            newsPagedList = initializeNetworkCall(application, cateId)
        } else {
            if (cateId > 0) {
                newsPagedList = initializedbDataSourceFactory(application, cateId)
            } else {
                newsPagedList = initializedbDataSourceFactory(application, cateId)
            }
        }
        return newsPagedList!!
    }

    fun selectTrendingSource(cateId: Int) {
        if (MyApplication.checkInternet) {
            initNwTrendingSF(cateId)
        } else {
            initDBTrendingSF()
        }
    }

    fun initNwTrendingSF(queryTag: Int) {
        var itemDataSourceFactory = NewsDataSourceFactory(application, queryTag)
        liveDataSource = itemDataSourceFactory.getNewsSorceData()
    }

    fun invalidateDataSourceFactory() {
        newsPagedList?.value?.dataSource?.invalidate()
        ddNewsPagedList?.value?.dataSource?.invalidate()
    }

    fun initDBTrendingSF() {
    }

    fun getAllTrendingData(): LiveData<List<TrendingNewsData>> {
        var liveData1 = rNewsDao.getAllTrendingData()
        var liveData2 = rNewsDao.getAllTrendingEntity()

        val result = MediatorLiveData<List<TrendingNewsData>>()
        result.addSource(liveData1) {
            var data = rNewsDao.getTrendingDataListFromDb()
            result.value = data
        }
        result.addSource(liveData2) {
            var data = rNewsDao.getTrendingDataListFromDb()
            result.value = data
        }
        return result
    }

    fun getAllTrendingData2(): LiveData<List<TrendingNewsData>> {
        val result = MediatorLiveData<List<TrendingNewsData>>()
        return result
    }

    fun getAllTrendingData3(): LiveData<List<TrendingData>> {
        var liveData1 = rNewsDao.getAllTrendingData()
        return liveData1
    }

    fun getAllTrendingEntity(): LiveData<List<TrendingNewsEntity>> {
        var liveData2 = rNewsDao.getAllTrendingEntity()
        return liveData2
    }


    fun initializeDDNetworkCall(application: Application, deviceId: String): LiveData<PagedList<DailyDigestEntity>> {
        var itemDataSourceFactory = DDNewsDataSourceFactory(application, deviceId)
        var PAGESIZE = 20
        ddLiveDataSource = itemDataSourceFactory.getNewsSorceData()
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(NEWSPAGESIZE).build()

        ddItemPagedList = LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                .build()
        return ddItemPagedList
    }

    fun initializeDDdbDataSourceFactory(application: Application): LiveData<PagedList<DailyDigestEntity>> {
        var PAGESIZE = 20
        val pageListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(PAGESIZE).build()

        ddfactory = rNewsDao.getPagedNewsByNodeIdFromDb()
        ddfactory.toLiveData(pageListConfig)
        val ddpagedListBuilder: LivePagedListBuilder<Int, DailyDigestEntity> = LivePagedListBuilder<Int, DailyDigestEntity>(ddfactory,
                pageListConfig)
        ddItemPagedList = ddpagedListBuilder.build()
        return ddItemPagedList
    }

    fun selectDailyDigestSource(deviceId: String): LiveData<PagedList<DailyDigestEntity>> {
        if (MyApplication.checkInternet) {
            ddNewsPagedList = initializeDDNetworkCall(application, deviceId)
        } else {
            ddNewsPagedList = initializeDDdbDataSourceFactory(application)
        }
        return ddNewsPagedList!!
    }

    fun getDDDetailNewsFromDb(): LiveData<List<DetailNewsData>> {
        var result: LiveData<List<DetailNewsData>> = rNewsDao.getDDDetailNewsFromDb()
        return result
    }
}