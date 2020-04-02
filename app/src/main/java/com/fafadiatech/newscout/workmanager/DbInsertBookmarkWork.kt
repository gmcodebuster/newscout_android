package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.db.BookmarkEntity
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase

class DbInsertBookmarkWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    private var newsDatabase: NewsDatabase? = null
    var nApi: ApiInterface
    var newsDao: NewsDao

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        newsDao = newsDatabase!!.newsDao()
    }

    override fun doWork(): Result {
        var id: Int = 0
        var newsId: Int = inputData.getInt("news_id_detail", 0)
        var isBookmark: Int = inputData.getInt("isbookmark_value", 0)
        var bookmarkEntity = BookmarkEntity(id, newsId, isBookmark)
        newsDao.insertBookmark(bookmarkEntity)
        return Result.success()
    }
}