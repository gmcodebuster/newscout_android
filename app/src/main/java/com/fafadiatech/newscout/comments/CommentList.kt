package com.fafadiatech.newscout.comments

import com.fafadiatech.newscout.model.NewsStatus

data class CommentList(val id:Int, val created_at:String, val comment: String, val article_id:Int, val user:Int, val user_name:String, val reply:Any, val replies:Any)

data class CommentBodyResData(var result: ArrayList<CommentList>, var total_article_likes: Int)

data class CommentResponseData(var header: NewsStatus, var body: CommentBodyResData )