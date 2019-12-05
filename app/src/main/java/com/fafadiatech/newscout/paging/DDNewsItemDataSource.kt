package com.fafadiatech.newscout.paging

import android.content.Context
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.dailydigest.DDArticleMediaEntity
import com.fafadiatech.newscout.db.dailydigest.DDHashTagEntity
import com.fafadiatech.newscout.db.dailydigest.DailyDigestEntity
import com.fafadiatech.newscout.model.NewsDataApi
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DDNewsItemDataSource(context: Context, deviceId: String) : PageKeyedDataSource<Int, DailyDigestEntity>() {

    lateinit var interfaceObj: ApiInterface
    lateinit var deviceId: String

    var articleNewsDao: NewsDao
    var newsDatabase: NewsDatabase? = null
    val TAG: String = "DDItemDataSource"
    lateinit var mContext: Context
    var gson = Gson()

    companion object {
        private val FIRST_PAGE = 1
    }

    init {
        mContext = context
        newsDatabase = NewsDatabase.getInstance(context)
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        articleNewsDao = newsDatabase!!.newsDao()
        this.deviceId = deviceId
    }

    var adjacentKey: Int? = null
    var key: Int? = null

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, DailyDigestEntity>) {

        var call: Call<NewsDataApi> = interfaceObj.getDDNewsFromNodeIdByPage(FIRST_PAGE, deviceId)
        call.enqueue(object : Callback<NewsDataApi> {
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {
                print(t.message)
            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                if (response.body() != null) {
                    val newsList = ArrayList<DailyDigestEntity>()

                    if (response.body()!!.body.next != null) {
                        key = FIRST_PAGE + 1
                    } else {
                        key = null
                    }
                    var list = response.body()!!.body.results
                    list.let {

                        var hashTagArrayList: ArrayList<DDHashTagEntity> = ArrayList<DDHashTagEntity>()

                        var articleMediaArrayList = ArrayList<DDArticleMediaEntity>()
                        newsList.clear()
                        for (i in 0 until list.size) {
                            var obj = list.get(i)
                            var jsonString = gson.toJson(obj)

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
                            var articleScore = obj.article_score

                            var entityObj =
                                    DailyDigestEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!, articleScore)

                            newsList.add(entityObj)

                            var hashTagList = list.get(i).hash_tags
                            for (j in 0 until hashTagList!!.size) {
                                var name = hashTagList.get(j)
                                var entityObj = DDHashTagEntity(newsId, name)

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
                                var articleMediaEntity = DDArticleMediaEntity(id, createdAt, modifiedAt, category, url, videoUrl, article)
                                articleMediaArrayList.add(articleMediaEntity)
                            }
                        }
                        try {
                            articleNewsDao.removeDDNews(newsList, hashTagArrayList, articleMediaArrayList)
                            callback.onResult(newsList, null, key)
                        } catch (e: Exception) {
                            print(e.stackTrace)
                        }
                    }
                }
            }
        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, DailyDigestEntity>) {
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, DailyDigestEntity>) {
    }
}