package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.BookmarkEntity
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.model.BookmarkArticleDataServer
import com.fafadiatech.newscout.model.BookmarkDetailData
import retrofit2.Call
import retrofit2.Response

class DbInsertBookmarkServerData(context: Context, params: WorkerParameters) : Worker(context, params) {

    private var newsDatabase: NewsDatabase? = null
    var nApi: ApiInterface
    var newsDao: NewsDao

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        newsDao = newsDatabase!!.newsDao()
    }

    override fun doWork(): Result {
        var token = inputData.getString("token_value_from_sign_in")
        var result = ArrayList<BookmarkEntity>()
        var call: Call<BookmarkArticleDataServer> = nApi.getBookmarkListFromServer(token!!)
        try {
            var response: Response<BookmarkArticleDataServer> = call.execute()
            if (response.isSuccessful) {
                var list: ArrayList<BookmarkDetailData> = response.body()?.body!!.results
                for (i in 0 until list.size) {
                    var entity = list.get(i)
                    var bookmarkEntity = BookmarkEntity(0, entity.article, entity.status)
                    result.add(bookmarkEntity)
                }
                newsDao.insertBookmarkServerData(result)
            }

            return Result.success()
        } catch (e: Throwable) {
            return Result.failure()
        }
    }
}