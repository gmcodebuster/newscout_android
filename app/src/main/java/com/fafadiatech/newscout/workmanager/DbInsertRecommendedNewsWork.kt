package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.RecommendedDataEntity
import com.fafadiatech.newscout.model.RecommendedDataApi
import retrofit2.Call
import retrofit2.Response

class DbInsertRecommendedNewsWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    var newsDao: NewsDao
    private var newsDatabase: NewsDatabase? = null
    var newsList = ArrayList<RecommendedDataEntity>()
    var nApi: ApiInterface
    var context: Context

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        newsDao = newsDatabase!!.newsDao()
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        this.context = context
    }

    override fun doWork(): Result {
        var newsId = inputData.getInt("recommended_news_id", 0)
        var call: Call<RecommendedDataApi> = nApi.getRecommendedArticles(newsId)
        try {
            var response: Response<RecommendedDataApi> = call.execute()
            var responseCode = response.code()
            newsDao.deleteRecommendedTableData()
            if (responseCode == 200) {
                var list = response.body()?.body?.results!!
                if (list != null && list.size > 0) {
                    for (i in 0 until list.size) {
                        var obj = list.get(i)
                        val newsId: Int = obj.id
                        val title: String? = obj.title
                        val source: String = obj.source
                        val category: String = obj.category.let { it }
                        val sourceUrl: String = obj.source_url
                        val urlToImage: String? = obj.cover_image
                        val description: String? = obj.blurb
                        val publishedOn: String = obj.published_on
                        var articleScore = obj.article_score
                        var entityObj = RecommendedDataEntity(newsId, title, source, category, sourceUrl, urlToImage, description, publishedOn, articleScore)
                        newsList.add(entityObj)
                    }
                    newsDao.insertRecommendedNews(newsList)
                }
            }
            return Result.success()
        } catch (e: Throwable) {
            return Result.failure()
        }
    }
}