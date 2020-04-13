package com.fafadiatech.newscout.comments

import android.content.Context
import android.content.SharedPreferences
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.appconstants.showMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentsDataSource(context: Context, articleId: Int) : PageKeyedDataSource<Int, CommentList>() {

    lateinit var nApi: ApiInterface
    var comList = ArrayList<CommentList>()
    var articleId: Int = 0
    lateinit var mContext: Context

    companion object {
        private val FIRST_PAGE = 1
    }

    init {
        mContext = context
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        this.articleId = articleId
    }

    var adjacentKey: Int? = null
    var key: Int? = null


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentList>) {

        var call: Call<CommentResponseData> = nApi.getAllComments(articleId, FIRST_PAGE)
        try {
            call.enqueue(object : Callback<CommentResponseData> {
                override fun onFailure(call: Call<CommentResponseData>, t: Throwable) {

                }

                override fun onResponse(call: Call<CommentResponseData>, response: Response<CommentResponseData>) {
                    var resCode = response.code()

                    if (resCode == 200) {
                        val status = response.body()!!.header.status
                        if (status == 1) {
                            if (response.body() != null) {
                                var list = response.body()!!.body.results

                                if (list != null && list.size > 0) {
                                    val oList = list as List<CommentList>
                                    callback.onResult(oList.asReversed(), null, FIRST_PAGE + 1)
                                } else {
                                    val list = ArrayList<CommentList>()
                                    callback.onResult(list, null, null)
                                }
                            }
                        } else {
                            val converter = ApiClient.getClient().responseBodyConverter<CommentErrorData>(
                                    CommentErrorData::class.java, arrayOfNulls<Annotation>(0))

                            var errorResponse: CommentErrorData?
                            errorResponse = converter?.convert(response.errorBody())
                            val status = errorResponse?.header?.status
                            var errMsg = errorResponse?.errors?.error

                            showMessage(mContext, errMsg ?: "Article does not exist", 900)

                        }

                    } else if (resCode == 401) {
                        showMessage(mContext, "No data found.", 900)

                    } else if (resCode == 404) {
                        showMessage(mContext, "No data found.", 900)
                    } else if (resCode == 500) {
                        showMessage(mContext, "No data found.", 900)
                    } else {
                        showMessage(mContext, "No data found.", 900)
                    }


                }
            })
        } catch (e: Exception) {
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, CommentList>) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, CommentList>) {

    }
}