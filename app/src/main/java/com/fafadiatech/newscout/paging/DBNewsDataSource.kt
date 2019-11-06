package com.fafadiatech.newscout.paging

import android.content.Context
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.NewsEntity

class DBNewsDataSource(context: Context, id: Int, pageno: Int) : PageKeyedDataSource<Int, NewsEntity>() {
    lateinit var mContext: Context
    var categoty_id: Int = 0
    var rNewsDao: NewsDao
    var newsDatabase: NewsDatabase? = null
    var pageno: Int = 0

    init {
        newsDatabase = NewsDatabase.getInstance(context)
        rNewsDao = newsDatabase!!.newsDao()
        mContext = context
        categoty_id = id
        this.pageno = pageno
    }

    companion object {
        private val FIRST_PAGE = 1
    }

    var key: Int? = null

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, NewsEntity>) {

        var list = ArrayList<NewsEntity>()
        list = rNewsDao.getPagedNewsByNodeIdFromDb(categoty_id) as ArrayList<NewsEntity>
        if (list.size > 0) {
            key = FIRST_PAGE + 1
            callback.onResult(list, null, key)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, NewsEntity>) {
        var list = ArrayList<NewsEntity>()
        list = rNewsDao.getPagedNewsByNodeIdFromDb(categoty_id) as ArrayList<NewsEntity>
        if (list.size > 0) {
            key = params.key + 1
            callback.onResult(list, key)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, NewsEntity>) {
    }
}