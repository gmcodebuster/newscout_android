package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase

class DeleteSearchTableWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    var articleNewsDao: NewsDao
    private var newsDatabase: NewsDatabase? = null

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        articleNewsDao = newsDatabase!!.newsDao()
    }

    override fun doWork(): Result {
        articleNewsDao.deleteSearchTableData()
        return Result.success()
    }
}