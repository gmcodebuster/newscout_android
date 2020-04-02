package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.LikeEntity
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.model.VoteArticleDataServer
import com.fafadiatech.newscout.model.VoteDetailData
import retrofit2.Call
import retrofit2.Response

class DbInsertLikeServerDataWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    private var newsDatabase: NewsDatabase? = null
    var nApi: ApiInterface
    var newsDao: NewsDao

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        newsDao = newsDatabase!!.newsDao()
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
    }

    override fun doWork(): Result {
        var token = inputData.getString("token_value_from_sign_in")
        var resultList = ArrayList<LikeEntity>()
        var call: Call<VoteArticleDataServer> = nApi.getLikedListFromServer(token!!)
        try {
            var response: Response<VoteArticleDataServer> = call.execute()
            if (response.isSuccessful) {
                var list: ArrayList<VoteDetailData> = response.body()?.body!!.results
                for (i in 0 until list.size) {
                    var obj = list.get(i)
                    var likeEntity = LikeEntity(0, obj.article, obj.is_like)
                    resultList.add(likeEntity)
                }
                newsDao.insertLikeServerData(resultList)
            }
            return Result.success()
        } catch (e: Throwable) {
            return Result.failure()
        }
    }
}