package com.fafadiatech.newscout.comments

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource


class CommentsDataSourceFactory(context: Context, articleId:Int) : DataSource.Factory<Int, CommentList>() {

    val comLiveDataSource = MutableLiveData<PageKeyedDataSource<Int, CommentList>>()
    var mContext: Context
    var articleId:Int

    init{
        mContext = context
        this.articleId = articleId
    }

    override fun create(): DataSource<Int, CommentList> {
        var comDataSource = CommentsDataSource(mContext, articleId)
        comLiveDataSource.postValue(comDataSource)
        return comDataSource
    }

    fun getCommentSourceData(): MutableLiveData<PageKeyedDataSource<Int, CommentList>>{
        return comLiveDataSource
    }
}