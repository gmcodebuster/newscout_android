package com.fafadiatech.newscout.paging

import android.content.Context
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.db.SearchDataEntity
import com.fafadiatech.newscout.model.NewsDataApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchItemDataSource(context: Context, queryTag: String) : PageKeyedDataSource<Int, NewsEntity>() {

    lateinit var nApi: ApiInterface
    var query: String
    var newsList = ArrayList<SearchDataEntity>()
    var articleList = ArrayList<NewsEntity>()
    var newsDatabase: NewsDatabase? = null
    var newsDao: NewsDao

    companion object {
        private var FIRST_PAGE = 1
    }

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        newsDao = newsDatabase!!.newsDao()
        query = queryTag
    }

    var adjacentKey: Int? = null
    var key: Int? = null

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, NewsEntity>) {

        var call: Call<NewsDataApi> = nApi.searchPaginatedNewsFromApi(query, SearchItemDataSource.FIRST_PAGE)
        call.enqueue(object : Callback<NewsDataApi> {
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                Log.d("SearchDataSource", "URL : "+call.request().url.toString())
                articleList.clear()
                if (response.body() != null) {
                    var list = response.body()!!.body.results
                    if (response.body()!!.body.next != null) {
                        key = FIRST_PAGE + 1
                    } else {
                        key = null
                    }
                    for (i in 0 until list.size) {
                        var obj = list.get(i)
                        val newsId: Int = obj.id
                        val categoryId = obj.category_id
                        val title: String = obj.title
                        Log.d("SearchDataSource", "ID : $newsId - Title : $title")
                        val source: String = obj.source
                        val category: String = obj.category.let { it }
                        val url: String = obj.source_url
                        val urlToImage: String = obj.cover_image
                        val description: String = obj.blurb
                        val publishedOn: String = obj.published_on
                        val hashTags = obj.hash_tags
                        var articleScore = obj.article_score.toString()

                        var entityObj =
                                SearchDataEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!)
                        var entityObj1 =
                                      NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore)

                        newsList.add(entityObj)
                        articleList.add(entityObj1)
                        Log.d("SearchItemDataSource", "Size : ${articleList.size}")
                    }
                    try {
                        //newsDao.insertSearchNews(newsList)
                        //var list = newsDao.getSearchNewsFromDb()
                        callback.onResult(articleList, null, key)
                    } catch (e: Throwable) {
                        Log.d("SearchItemDataSource", e.message)
                    }
                }
            }
        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, NewsEntity>) {
        var call: Call<NewsDataApi> = nApi.searchPaginatedNewsFromApi(query, params.key)
        call.enqueue(object : Callback<NewsDataApi> {
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                Log.d("SearchDataSource", "URL : "+call.request().url.toString())
                articleList.clear()
                if (response.body() != null) {
                    var list = response.body()!!.body.results
                    if (response.body()!!.body.next != null) {
                        Log.d("SearchDataSource", "Next Key : "+response.body()!!.body.next)
                        Log.d("SearchDataSource", "Params Key : "+params.key)
                        key = params.key + 1
                    } else {
                        key = null
                        return
                    }

                    for (i in 0 until list.size) {
                        var obj = list.get(i)
                        val newsId: Int = obj.id
                        val categoryId = obj.category_id
                        val title: String = obj.title
                        Log.d("SearchDataSource", "ID : $newsId -- Title : $title")
                        val source: String = obj.source
                        val category: String = obj.category.let { it }
                        val url: String = obj.source_url
                        val urlToImage: String = obj.cover_image
                        val description: String = obj.blurb
                        val publishedOn: String = obj.published_on
                        val hashTags = obj.hash_tags
                        var articleScore = obj.article_score.toString()

                        var entityObj =
                                SearchDataEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!)
                        var entityObj1 =
                                NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore)
                        newsList.add(entityObj)
                        articleList.add(entityObj1)
                    }
                    try {
                        //newsDao.insertSearchNews(newsList)
                        //var list = newsDao.getSearchNewsFromDb()
                        Log.d("SearchDataSource", "Next Params Key : "+key)
                        callback.onResult(articleList, key)
                    } catch (e: Throwable) {
                        Log.d("SearchItemDataSource", e.message)
                    }
                }
            }
        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, NewsEntity>) {
    }
}