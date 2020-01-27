package com.fafadiatech.newscout.paging

import android.content.Context
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.TRENDING_NAME
import com.fafadiatech.newscout.appconstants.addAdsData
import com.fafadiatech.newscout.db.*
import com.fafadiatech.newscout.model.AdsData
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.model.NewsDataApi
import com.fafadiatech.newscout.model.TrendingDataApi
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class NewsItemDataSource(context: Context, queryTag: String) : PageKeyedDataSource<Int, INews>() {

    lateinit var interfaceObj: ApiInterface
    var tagName: String = ""
    var articleNewsDao: NewsDao
    var newsDatabase: NewsDatabase? = null
    val TAG: String = "NewsItemDataSource"
    lateinit var mContext: Context
    var gson = Gson()

    lateinit var file: File

    companion object {
        private val FIRST_PAGE = 1
    }

    init {
        mContext = context
        newsDatabase = NewsDatabase.getInstance(context)
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        articleNewsDao = newsDatabase!!.newsDao()
        tagName = queryTag
    }

    var adjacentKey: Int? = null
    var key: Int? = null


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, INews>) {

        if (tagName.equals(TRENDING_NAME, true)) {
            lateinit var articleList: ArrayList<INews>
            var call: Call<TrendingDataApi> = interfaceObj.getNewsByTrending()

            call.enqueue(object : Callback<TrendingDataApi> {
                override fun onFailure(call: Call<TrendingDataApi>, t: Throwable) {
                }

                override fun onResponse(call: Call<TrendingDataApi>, response: Response<TrendingDataApi>) {
                    var trendingResultList = response.body()?.body?.results
                    articleList = ArrayList<INews>()
                    var trendingNewsList = ArrayList<TrendingEntity>()
                    var newsId: Int = 0
                    trendingResultList?.let {
                        articleNewsDao.deleteTrendingData()
                        for (i in 0 until trendingResultList.size) {
                            var trendingList = trendingResultList.get(i).articles
                            var trendingListCount = trendingList.size
                            var clusterId = trendingResultList.get(i).id
                            for (j in 0 until trendingList.size) {
                                var obj = trendingList.get(j)
                                newsId = obj.id
                                val categoryId = obj.category_id
                                val title: String = obj.title
                                val source: String = obj.source
                                val category: String = obj.category.let { it }
                                val url: String = obj.source_url
                                val urlToImage: String = obj.cover_image
                                val description: String = obj.blurb
                                val publishedOn: String = obj.published_on
                                val hashTags = obj.hash_tags
                                var articleScore = obj.article_score.toString()

                                var entityObj =
                                        NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore)
                                articleList.add(entityObj)
                                var trendingObJ = TrendingEntity(0, clusterId, newsId, trendingListCount)
                                trendingNewsList.add(trendingObJ)
                            }
                        }
                        articleNewsDao.insertNews(articleList as ArrayList<NewsEntity>)
                        articleNewsDao.insertTrendingData(trendingNewsList)

                        articleList = addAdsData(articleList)!!
                    }
                }
            })


            callback.onResult(articleList, null, null)
            return
        }

        var call: Call<NewsDataApi> = interfaceObj.getNewsFromNodeIdByPage(FIRST_PAGE, tagName)
        call.enqueue(object : Callback<NewsDataApi> {
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                if (response.body() != null) {
                    var newsList = ArrayList<INews>()

                    if (response.body()!!.body.next != null) {
                        key = FIRST_PAGE + 1
                    } else {
                        key = null
                    }

                    file = File(mContext.cacheDir, "${tagName}.txt")
                    var list = response.body()!!.body.results
                    if (list != null && list.size > 0) {
                        var hashTagArrayList: ArrayList<HashTagEntity> = ArrayList<HashTagEntity>()

                        var articleMediaArrayList = ArrayList<ArticleMediaEntity>()
                        newsList.clear()
                        for (i in 0 until list.size) {
                            var obj = list.get(i)
                            var jsonString = gson.toJson(obj)
                            file.appendText(jsonString + ",", Charsets.UTF_8)
                            val newsId: Int = obj.id
                            val categoryId = obj.category_id
                            val title: String = obj.title
                            val source: String = obj.source
                            val category: String = obj.category.let { it }
                            val url: String = obj.source_url
                            val urlToImage: String = obj.cover_image
                            val description: String = obj.blurb
                            val publishedOn: String = obj.published_on
                            val hashTags = obj.hash_tags
                            var articleScore = obj.article_score.toString()

                            var entityObj =
                                    NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore)
                            newsList.add(entityObj)

                            var hashTagList = list.get(i).hash_tags
                            for (j in 0 until hashTagList!!.size) {
                                var name = hashTagList.get(j)
                                var entityObj = HashTagEntity(newsId, name)
                                hashTagArrayList.add(entityObj)
                            }

                            var articleMediaList = list.get(i).article_media
                            for (k in 0 until articleMediaList!!.size) {
                                var articleMedia = articleMediaList.get(k)
                                var id = articleMedia.id
                                var createdAt = articleMedia.created_at
                                var modifiedAt = articleMedia.modified_at
                                var category = articleMedia.category
                                var url = articleMedia.url
                                var videoUrl: String? = articleMedia.video_url
                                var article = articleMedia.article
                                var articleMediaEntity = ArticleMediaEntity(id, createdAt, modifiedAt, category, url, videoUrl, article)
                                articleMediaArrayList.add(articleMediaEntity)
                            }
                        }

                        try {
                            articleNewsDao.insertNews(newsList as ArrayList<NewsEntity>)
                            articleNewsDao.insertHashTagList(hashTagArrayList)
                            articleNewsDao.insertArticleMediaList(articleMediaArrayList)
                        } catch (e: Exception) {

                        }
                        newsList = addAdsData(newsList)!!
                    }

                    try {
                        callback.onResult(newsList, null, key)
                    } catch (e: Exception) {

                    }
                }
            }
        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, INews>) {
        var call: Call<NewsDataApi> = interfaceObj.getNewsFromNodeIdByPage(params.key, tagName)
        call.enqueue(object : Callback<NewsDataApi> {
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {

                if (response.body() != null) {
                    var newsList = ArrayList<INews>()
                    var list = response.body()!!.body.results
                    if (response.body()!!.body.next != null) {
                        key = params.key + 1
                    } else {
                        key = null
                    }

                    if (list != null && list.size > 0) {
                        var hashTagArrayList: ArrayList<HashTagEntity> = ArrayList<HashTagEntity>()
                        var articleMediaArrayList = ArrayList<ArticleMediaEntity>()
                        newsList.clear()
                        for (i in 0 until list.size) {
                            var obj = list.get(i)
                            var jsonString = gson.toJson(obj)
                            file.appendText(jsonString + ",", Charsets.UTF_8)
                            val newsId: Int = obj.id
                            var categoryId: Int = obj.category_id
                            val title: String = obj.title
                            val source: String = obj.source
                            val category: String = obj.category.let { it }
                            val url: String = obj.source_url
                            val urlToImage: String = obj.cover_image
                            val description: String = obj.blurb
                            val publishedOn: String = obj.published_on
                            val hashTags = obj.hash_tags
                            var articleScore = obj.article_score.toString()

                            var entityObj =
                                    NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!,articleScore)
                            newsList.add(entityObj)

                            var hashTagList = list.get(i).hash_tags
                            for (j in 0 until hashTagList!!.size) {
                                var name = hashTagList.get(j)
                                var entityObj = HashTagEntity(newsId, name)
                                hashTagArrayList.add(entityObj)
                            }

                            var articleMediaList = list.get(i).article_media
                            for (k in 0 until articleMediaList!!.size) {
                                var articleMedia = articleMediaList.get(k)
                                var id = articleMedia.id
                                var createdAt = articleMedia.created_at
                                var modifiedAt = articleMedia.modified_at
                                var category = articleMedia.category
                                var url = articleMedia.url
                                var videoUrl: String? = articleMedia.video_url
                                var article = articleMedia.article
                                var articleMediaEntity = ArticleMediaEntity(id, createdAt, modifiedAt, category, url, videoUrl, article)
                                articleMediaArrayList.add(articleMediaEntity)
                            }
                        }
                        try {
                            articleNewsDao.insertNews(newsList as ArrayList<NewsEntity>)
                            articleNewsDao.insertHashTagList(hashTagArrayList)
                            articleNewsDao.insertArticleMediaList(articleMediaArrayList)
                        } catch (e: Exception) {

                        }

                        newsList = addAdsData(newsList)!!
                    }

                    try {
                        callback.onResult(newsList, key)
                    } catch (e: Exception) {

                    }
                }
            }
        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, INews>) {
        var call: Call<NewsDataApi> = interfaceObj.getNewsFromNodeIdByPage(params.key, tagName)
        call.enqueue(object : Callback<NewsDataApi> {
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                var newsList = ArrayList<INews>()
                var result = response.body()!!.body.results

                result.let {
                    for (i in 0 until result.size) {
                        var obj = result.get(i)
                        val newsId: Int = obj.id
                        var categoryId: Int = obj.category_id
                        val title: String = obj.title
                        val source: String = obj.source
                        val category: String = obj.category.let { it }
                        val url: String = obj.source_url
                        val urlToImage: String = obj.cover_image
                        val description: String = obj.blurb
                        val publishedOn: String = obj.published_on
                        val hashTags = obj.hash_tags
                        var articleScore = obj.article_score.toString()

                        var entityObj =
                                NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore)
                        newsList.add(entityObj)
                    }

                    if (params.key > 1) {
                        adjacentKey = params.key - 1
                    } else {
                        adjacentKey = null
                    }
                }

                if (response.body() != null) {

                }
            }
        })
    }
}