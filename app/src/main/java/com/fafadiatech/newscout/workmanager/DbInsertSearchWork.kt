package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.SearchDataEntity
import com.fafadiatech.newscout.model.ArticlesData
import com.fafadiatech.newscout.model.NewsDataApi
import retrofit2.Call
import retrofit2.Response

class DbInsertSearchWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    var articleNewsDao: NewsDao
    private var newsDatabase: NewsDatabase? = null
    var newsList = ArrayList<SearchDataEntity>()
    var apiInterface: ApiInterface
    var context: Context

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        articleNewsDao = newsDatabase!!.newsDao()
        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        this.context = context
    }

    override fun doWork(): Result {
        var query: String? = inputData.getString("search_query")
        var call: Call<NewsDataApi> = apiInterface.searchNewsFromApi(query!!)
        try {
            var response: Response<NewsDataApi> = call.execute()
            var responseCode = response.code()
            articleNewsDao.deleteSearchTableData()
            if (responseCode == 200) {
                var list = ArrayList<ArticlesData>()

                list = response.body()?.body!!.results
                if (list != null && list.size > 0) {
                    for (i in 0 until list.size) {
                        var obj = list.get(i)
                        val newsId: Int = obj.id
                        var categoryId = obj.category_id
                        val title: String = obj.title
                        val source: String = obj.source
                        val category: String = obj.category.let { it }
                        val url: String = obj.source_url
                        val urlToImage: String = obj.cover_image
                        val description: String = obj.blurb
                        val publishedOn: String = obj.published_on
                        var hashTags = obj.hash_tags

                        var entityObj = SearchDataEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!)
                        newsList.add(entityObj)
                    }

                    articleNewsDao.insertSearchNews(newsList)
                }
            } else {
                newsList.clear()
                articleNewsDao.insertSearchNews(newsList)
            }
            return Result.success()
        } catch (exeption: Throwable) {
            return Result.failure()
        }
    }
}