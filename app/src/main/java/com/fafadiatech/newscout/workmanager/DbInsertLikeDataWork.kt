package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.LikeEntity
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.model.VoteArticleData
import retrofit2.Call
import retrofit2.Response

class DbInsertLikeDataWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    private var newsDatabase: NewsDatabase? = null
    var nApi: ApiInterface
    var newsDao: NewsDao

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        newsDao = newsDatabase!!.newsDao()
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
    }

    override fun doWork(): Result {
        var tokenValue: String? = inputData.getString("token_value_from_detail")
        var id: Int = 0
        var newsId: Int = inputData.getInt("news_id_detail", 0)
        var isLike: Int = inputData.getInt("is_like_value_detail", 2)
        var call: Call<VoteArticleData> = nApi.voteArticlesByApi(tokenValue!!, isLike, newsId)
        try {
            var response: Response<VoteArticleData> = call.execute()
            var message = response.body()?.body?.Msg
            var likeEntity = LikeEntity(id, newsId, isLike)
            newsDao.insertLike(likeEntity)
            return Result.success()
        } catch (e: Throwable) {
            return Result.failure()
        }
    }
}