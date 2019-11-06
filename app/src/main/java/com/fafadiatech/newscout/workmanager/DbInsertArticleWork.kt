package com.fafadiatech.newscout.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.*
import com.fafadiatech.newscout.model.ArticlesData
import com.fafadiatech.newscout.model.NewsDataApi
import retrofit2.Call
import retrofit2.Response

class DbInsertArticleWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    var articleNewsDao: NewsDao
    private var newsDatabase: NewsDatabase? = null
    var newsList = ArrayList<NewsEntity>()
    var apiInterface: ApiInterface
    var nextPageUrl: String? = null
    var prevPageUrl: String? = null
    var cursorNext: String? = null
    var cursorPrev: String? = null

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        articleNewsDao = newsDatabase!!.newsDao()
        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
    }

    override fun doWork(): Result {
        var categoryValue = inputData.getString("category_value_worker")
        var tag = inputData.getString("tag_value")
        var call: Call<NewsDataApi> = apiInterface.getNewsFromTag(tag!!)
        try {
            var response: Response<NewsDataApi> = call.execute()
            var responseCode = response.code()
            if (responseCode == 200) {
                var list = ArrayList<ArticlesData>()
                list = response.body()?.body!!.results
                if (list != null && list.size > 0) {
                    nextPageUrl = response.body()?.body?.next
                    prevPageUrl = response.body()?.body?.previous
                    var hashTagArrayList: ArrayList<HashTagEntity> = ArrayList<HashTagEntity>()
                    var articleMediaArrayList = ArrayList<ArticleMediaEntity>()

                    for (i in 0 until list.size) {
                        var obj = list.get(i)
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

                        var entityObj =
                                NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!)
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
                    articleNewsDao.insertNews(newsList)
                    articleNewsDao.insertHashTagList(hashTagArrayList)
                    articleNewsDao.insertArticleMediaList(articleMediaArrayList)
                }
                return Result.success()
            }
        } catch (e: Throwable) {
            return Result.failure()
        }
        return Result.success()
    }
}