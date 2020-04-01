package com.fafadiatech.newscout.paging

import android.content.Context
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.db.SourceNewsEntity
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.model.NewsDataApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SourceItemDataSource(context: Context, queryTag: String) : PageKeyedDataSource<Int, NewsEntity>() {

    lateinit var nApi: ApiInterface
    var tags: String
    var newsList = ArrayList<NewsEntity>()
    var sourceNewsList = ArrayList<SourceNewsEntity>()
    var newsDatabase: NewsDatabase? = null
    var newsDao: NewsDao

    companion object {
        private val FIRST_PAGE = 1
    }

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        tags = queryTag
        newsDao = newsDatabase!!.newsDao()
    }

    var adjacentKey: Int? = null
    var key: Int? = null

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, NewsEntity>) {
        //lateinit var newsList: ArrayList<INews>
        var call: Call<NewsDataApi> = nApi.getNewsFromSource(tags, SourceItemDataSource.FIRST_PAGE)
        try {
            call.enqueue(object : Callback<NewsDataApi> {
                override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
                }

                override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                    if (response.body() != null) {
                        //newsList = ArrayList<INews>()
                        var list = response.body()!!.body.results

                        if (list != null && list.size > 0) {
                            //delete data from db
                            newsDao.deleteSourceArticle()

                            for (i in 0 until list.size) {
                                var obj = list.get(i)
                                val newsId: Int = obj.id
                                val categoryId: Int = obj.category_id
                                val title: String = obj.title
                                val source: String = obj.source
                                val category: String = obj.category.let { it }
                                val url: String = obj.source_url
                                val urlToImage: String = obj.cover_image
                                val description: String = obj.blurb
                                val publishedOn: String = obj.published_on
                                val hashTags = obj.hash_tags
                                var articleScore = obj.article_score

                                var entityObj =
                                        NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore.toString())
                                newsList.add(entityObj)

                                var sourceEntityObj =
                                        SourceNewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore.toString())
                                sourceNewsList.add(sourceEntityObj)
                                newsDao.insertSourceNews(sourceNewsList)
                            }
                        }
                        callback.onResult(newsList, null, FIRST_PAGE + 1)
                    }
                }
            })
        } catch (e: Throwable) {
            Log.d("SourceItemDataSource","Error : "+e.message)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, NewsEntity>) {
        //lateinit var newsList: ArrayList<INews>
        var call: Call<NewsDataApi> = nApi.getNewsFromSource(tags, params.key)
        try {
            call.enqueue(object : Callback<NewsDataApi> {
                override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
                }

                override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                    if (response.body() != null) {
                        //newsList = ArrayList<INews>()
                        var list = response.body()!!.body.results
                        if (response.body()!!.body.next != null) {
                            key = params.key + 1
                        } else {
                            key = null
                            return
                        }

                        if (list != null && list.size > 0) {

                            for (i in 0 until list.size) {
                                var obj = list.get(i)
                                val newsId: Int = obj.id
                                val categoryId: Int = obj.category_id
                                val title: String = obj.title
                                val source: String = obj.source
                                val category: String = obj.category.let { it }
                                val url: String = obj.source_url
                                val urlToImage: String = obj.cover_image
                                val description: String = obj.blurb
                                val publishedOn: String = obj.published_on
                                val hashTags = obj.hash_tags
                                var articleScore = obj.article_score

                                var entityObj =
                                        NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore.toString())
                                newsList.add(entityObj)
                                var sourceEntityObj =
                                        SourceNewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore.toString())
                                sourceNewsList.add(sourceEntityObj)
                                newsDao.insertSourceNews(sourceNewsList)
                            }
                        }
                        try {
                            callback.onResult(newsList, key)
                        } catch (e: Exception) {

                        }
                    }
                }
            })
        } catch (e: Throwable) {
            Log.d("SourceItemDataSource","Error : "+e.message)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, NewsEntity>) {
        var call: Call<NewsDataApi> = nApi.getNewsFromSource(tags, params.key)
        call.enqueue(object : Callback<NewsDataApi> {
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                if (params.key > 1) {
                    adjacentKey = params.key - 1
                } else {
                    adjacentKey = null
                }
            }
        })
    }
}