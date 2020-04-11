package com.fafadiatech.newscout.comments

import android.content.Context
import android.content.SharedPreferences
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentsDataSource(context: Context, articleId:Int): PageKeyedDataSource<Int, CommentList>() {

    lateinit var nApi : ApiInterface
    var comList = ArrayList<CommentList>()
    var articleId :Int = 0
    companion object {
        private val FIRST_PAGE = 1
    }

    init{
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        this.articleId = articleId
    }

    var adjacentKey: Int? = null
    var key: Int? = null



    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentList>){

        var call : Call<CommentResponseData> = nApi.getAllComments(articleId, FIRST_PAGE)
        try{
            call.enqueue(object: Callback<CommentResponseData> {
                override fun onFailure(call: Call<CommentResponseData>, t: Throwable) {

                }

                override fun onResponse(call: Call<CommentResponseData>, response: Response<CommentResponseData>) {
                    if(response.body() != null){
                        var list = response.body()!!.body.results

                        if(list != null && list.size > 0){
                            callback.onResult(list, null, FIRST_PAGE + 1)
                        }else{
                            val list = ArrayList<CommentList>()
                            callback.onResult(list, null, null)
                        }
                    }
                }
            })
        }catch(e:Exception){}
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, CommentList>) {
        /*var call:Call<CommentResponseData> = nApi.getAllComments(articleId, params.key)
        try{
            call.enqueue(object: Callback<CommentResponseData> {
                override fun onFailure(call: Call<CommentResponseData>, t: Throwable) {

                }

                override fun onResponse(call: Call<CommentResponseData>, response: Response<CommentResponseData>) {
                    if(response.body() != null){
                        var list = response.body()!!.body.results

                        if(list != null && list.size > 0){
                            callback.onResult(list, FIRST_PAGE + 1)
                        }
                    }
                }
            })
        }catch(e:Exception){}*/
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, CommentList>) {

    }


}