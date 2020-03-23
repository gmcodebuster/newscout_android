package com.fafadiatech.newscout.paging

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.*
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.model.NewsDataApi
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.fafadiatech.newscout.viewmodel.ViewModelProviderFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SgstItemDataSource(context: Context, newsId: Int) : PageKeyedDataSource<Int, INews>() {
    lateinit var apiClient: ApiInterface
    lateinit var mContext: Context
    var newsId:Int = 0
    init {
        mContext = context
        this.newsId = newsId
        apiClient = ApiClient.getClient().create(ApiInterface::class.java)
    }
    companion object {
        private val FIRST_PAGE = 1
    }
    var adjacentKey: Int? = null
    var key: Int? = null

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, INews>) {
        lateinit var articleList: ArrayList<DetailNewsData>
        var call: Call<NewsDataApi> = apiClient.getSuggestedArticles(newsId)
        call.enqueue(object: Callback<NewsDataApi>{
            override fun onFailure(call: Call<NewsDataApi>, t: Throwable) {

            }

            override fun onResponse(call: Call<NewsDataApi>, response: Response<NewsDataApi>) {
                var newsList : ArrayList<INews>? = null
                if (response.body() != null) {
                    val resHeader = response.body()?.header
                    val resBody = response.body()?.body
                    val status = resHeader?.status

                    if(status == 0){

                        Log.d("SugDataSource Exception","Error :")
                        val newsDatabase = NewsDatabase.getInstance(mContext.applicationContext)
                        val rNewsDao = newsDatabase!!.newsDao()
                        val news = rNewsDao.getTopFiveArticles()
                        callback.onResult(news, null, key)

                    }else{

                        resBody?.next?.let {
                            key = FIRST_PAGE + 1
                        }

                        var list = response.body()?.body?.results ?: arrayListOf()
                        if (list != null && list.size > 0) {
                            var hashTagArrayList: ArrayList<HashTagEntity> = ArrayList<HashTagEntity>()
                            newsList = ArrayList<INews>()
                            var articleMediaArrayList = ArrayList<ArticleMediaEntity>()
                            newsList?.clear()
                            for (i in 0 until list.size) {
                                var obj = list.get(i)
                                val newsId: Int = obj.id
                                val categoryId = obj.category_id
                                val title: String = obj.title
                                val source: String = obj.source
                                val category: String = obj.category.let { it }
                                val url: String = obj.source_url
                                val urlToImage: String = obj.cover_image
                                val description: String = obj.blurb
                                val publishedOn: String = obj.published_on
                                val hashTags = obj.hash_tags
                                val likeStatus:Int  = 0
                                val bookmarkStatus:Int = 0
                                var articleScore = obj.article_score.toString()

                                /*Log.d("SgstItemData","News Id : "+newsId +" CategoryId : "+categoryId+ " Title" +
                                "Source : " +source+" category : " +category+ " url : "+url + " urlToImage : "+ urlToImage+ " description : "+ description+ "publishedOn : "+publishedOn + "hashTags : "+hashTags?.size + "likeStatus : "+likeStatus+ "bookmarkStatus : "+bookmarkStatus+ "articleScore :"+articleScore
                                )*/
                                var entityObj =
                                        DetailNewsData(newsId?:0, categoryId?:0, title?:"", source?:"", category?:"", url?:"", urlToImage?:"", description?:"", publishedOn?:"", hashTags!!, likeStatus?:0, bookmarkStatus?:0, articleScore?:"0.0")
                                newsList?.add(entityObj)

                                var hashTagList = list.get(i).hash_tags
                                for (j in 0 until hashTagList!!.size) {
                                    var name = hashTagList.get(j)
                                    var entityObj = HashTagEntity(newsId, name)
                                    hashTagArrayList.add(entityObj)
                                }

                                var articleMediaList = list.get(i).article_media
                                for (k in 0 until articleMediaList!!.size) {
                                    var articleMedia = articleMediaList.get(k)
                                    var id = articleMedia.id
                                    var createdAt = articleMedia.created_at
                                    var modifiedAt = articleMedia.modified_at
                                    var category = articleMedia.category
                                    var url = articleMedia.url
                                    var videoUrl: String? = articleMedia.video_url
                                    var article = articleMedia.article
                                    var articleMediaEntity = ArticleMediaEntity(id, createdAt, modifiedAt, category, url, videoUrl, article)
                                    articleMediaArrayList.add(articleMediaEntity)
                                }
                            }

                        }else{
                            if(newsList.isNullOrEmpty() || newsList.size == 0){
                                val newsDatabase = NewsDatabase.getInstance(mContext.applicationContext)
                                val rNewsDao = newsDatabase!!.newsDao()
                                newsList = rNewsDao.getTopFiveArticles() as ArrayList<INews>
                            }
                        }

                        try {
                            if(newsList == null){
                                newsList = arrayListOf<INews>()
                            }
                            callback.onResult(newsList!!, null, key)
                        } catch (e: Exception) {
                            Log.d("SuggestedDataSource","Error : "+e.message)
                        }

                    }
                }else{
                    try {
                        val newsDatabase = NewsDatabase.getInstance(mContext.applicationContext)
                        val rNewsDao = newsDatabase!!.newsDao()
                        //newsList = rNewsDao.getTopFiveSgstArticles() as ArrayList<INews>
                        newsList = rNewsDao.getTopFiveArticles() as ArrayList<INews>
                        callback.onResult(newsList, null, key)
                    }catch (e:Exception){
                        Log.d("SugDataSource Exception","Error : "+e.message)
                        val newsDatabase = NewsDatabase.getInstance(mContext.applicationContext)
                        val rNewsDao = newsDatabase!!.newsDao()
                        val news = rNewsDao.getTopFiveArticles()
                        callback.onResult(news, null, key)
                    }
                }
            }
        })

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, INews>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, INews>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}