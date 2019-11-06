package com.fafadiatech.newscout.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.adapter.TrendingAdapter
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.customcomponent.MyItemDecoration
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.db.TrendingEntity
import com.fafadiatech.newscout.db.trending.TrendingData
import com.fafadiatech.newscout.db.trending.TrendingNewsEntity
import com.fafadiatech.newscout.interfaces.AddTrendingFragmentListener
import com.fafadiatech.newscout.interfaces.TrendingFragListener
import com.fafadiatech.newscout.model.TrendingDataApi
import com.fafadiatech.newscout.model.TrendingNewsData
import com.fafadiatech.newscout.model.trending.TrendingDataHeaderApi
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.github.clans.fab.FloatingActionButton
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrendingFragment : Fragment(), AddTrendingFragmentListener {

    lateinit var rvTrending: RecyclerView
    var trendingList = ArrayList<TrendingNewsData>()
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    lateinit var addTrendingFragmentListener: AddTrendingFragmentListener
    lateinit var loadTrendFragment: TrendingFragListener
    lateinit var refreshTrendingNews: SwipyRefreshLayout
    lateinit var articleNewsDao: NewsDao
    var newsDatabase: NewsDatabase? = null
    lateinit var apiInterfaceObj: ApiInterface
    lateinit var fabReturnTop: FloatingActionButton
    lateinit var trendingAdapter: TrendingAdapter
    lateinit var themePreference: SharedPreferences
    var deviceWidthDp: Float = 0f
    lateinit var mContext: Context
    var pos: Int = 0
    lateinit var imagePlaceHolder: ImageView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        pos = 0

        addTrendingFragmentListener = this@TrendingFragment as AddTrendingFragmentListener

        if (parentFragment is RootTrendingFragment) {
            loadTrendFragment = parentFragment as RootTrendingFragment
        }
        newsDatabase = NewsDatabase.getInstance(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val displayMetrics = DisplayMetrics()
        val windowmanager = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        var view = LayoutInflater.from(context).inflate(R.layout.fragment_trending, container, false)

        apiInterfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        articleNewsDao = newsDatabase!!.newsDao()
        themePreference = this.activity!!.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        var isNightModeEnable = themePreference.getBoolean("night mode enable", false)
        rvTrending = view.findViewById(R.id.rv_trending)
        fabReturnTop = view.findViewById(R.id.fab_return_top)
        imagePlaceHolder = view.findViewById<ImageView>(R.id.img_view_placeholder)
        var tagId = arguments!!.getInt("category_id", 0)
        var categoryName = arguments!!.getString("category_name")

        var layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val itemDecorator = DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL)

        if (deviceWidthDp < 600) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            if (!isNightModeEnable) {
                itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.news_item_divider)!!)
            } else {
                itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.news_item_divider_night)!!)
            }
        } else {
            layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)

            if (!isNightModeEnable) {
                var divider = MyItemDecoration(ContextCompat.getDrawable(context!!, R.drawable.item_decorator_divider)!!)
            } else {
                var divider = MyItemDecoration(ContextCompat.getDrawable(context!!, R.drawable.item_decorator_divider_night)!!)
            }
        }

        trendingAdapter = TrendingAdapter(context!!, addTrendingFragmentListener)
        refreshTrendingNews = view.findViewById(R.id.refresh_trending)
        rvTrending.layoutManager = layoutManager
        rvTrending.adapter = trendingAdapter
        trendingAdapter.setCategory(categoryName, tagId)
        imagePlaceHolder.visibility = View.VISIBLE

        fabReturnTop.setOnClickListener {
            rvTrending.smoothScrollToPosition(0)
        }

        refreshTrendingNews.setOnRefreshListener(object : SwipyRefreshLayout.OnRefreshListener {

            override fun onRefresh(direction: SwipyRefreshLayoutDirection?) {
                fetchData()
            }
        })

        trendingAdapter.setTrendingData(trendingList)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fetchDataViewModel = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)
        fetchDataViewModel.getTrendingData().observe(viewLifecycleOwner, Observer {
            var tNews = it as ArrayList<TrendingNewsData>
            if (tNews.size > 0) {
                imagePlaceHolder.visibility = View.GONE
            } else {
                imagePlaceHolder.visibility = View.VISIBLE
            }
            trendingAdapter.setTrendingData(tNews)

            if (tNews.size > 0) {
                val pos =
                        rvTrending.scrollToPosition(pos)
            } else {
                rvTrending.scrollToPosition(0)
            }
        })
        fetchData()
    }

    override fun addFragmentOnClick(clusterId: Int, pos: Int) {
        this.pos = pos
        loadTrendFragment.loadFragment(clusterId, pos)
    }

    fun fetchData() {
        var call: Call<TrendingDataApi> = apiInterfaceObj.getNewsByTrending()
        call.enqueue(object : Callback<TrendingDataApi> {
            override fun onFailure(call: Call<TrendingDataApi>, t: Throwable) {

            }

            override fun onResponse(call: Call<TrendingDataApi>, response: Response<TrendingDataApi>) {

                var trendingResultList = response.body()?.body?.results
                var articleList = ArrayList<NewsEntity>()
                var trendingArticleList = ArrayList<TrendingNewsEntity>()
                var trendingNewsList = ArrayList<TrendingEntity>()
                var newsId: Int = 0

                trendingResultList?.let {
                    for (i in 0 until trendingResultList.size) {
                        var trendingList = trendingResultList.get(i).articles
                        var trendingListCount = trendingList.size
                        var clusterId = trendingResultList.get(i).id
                        for (j in 0 until trendingList.size) {
                            var obj = trendingList.get(j)
                            newsId = obj.id
                            val categoryId = obj.category_id
                            val title: String = obj.title
                            val source: String = obj.source
                            val category: String = obj.category?.let { it } ?: ""
                            val url: String = obj.source_url
                            val urlToImage: String = obj.cover_image
                            val description: String = obj.blurb
                            val publishedOn: String = obj.published_on
                            val hashTags = obj.hash_tags

                            var entityObj =
                                    NewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!)

                            articleList.add(entityObj)

                            var entityObjTNE =
                                    TrendingNewsEntity(newsId, categoryId, title, source, category, url, urlToImage, description, publishedOn, hashTags!!)

                            trendingArticleList.add(entityObjTNE)

                            var trendingObJ = TrendingEntity(0, clusterId, newsId, trendingListCount)

                            trendingNewsList.add(trendingObJ)
                        }
                    }
                    articleNewsDao.removeTrending(trendingArticleList, trendingNewsList)
                }
                refreshTrendingNews.isRefreshing = false
            }
        })
    }

    fun fetchAPIData() {
        var call: Call<TrendingDataHeaderApi> = apiInterfaceObj.getNewsByTrendingAPI()
        call.enqueue(object : Callback<TrendingDataHeaderApi> {
            override fun onFailure(call: Call<TrendingDataHeaderApi>, t: Throwable) {

            }

            override fun onResponse(call: Call<TrendingDataHeaderApi>, response: Response<TrendingDataHeaderApi>) {
                var trendingResultList = response.body()?.body?.results
                var trendingArticleList = ArrayList<TrendingData>()
                var newsId: Int = 0
                trendingResultList?.let {
                    articleNewsDao.deleteTrendingData()
                    for (i in 0 until trendingResultList.size) {
                        var id = trendingResultList.get(i).id
                        var trendingList = trendingResultList.get(i).articles ?: arrayListOf()
                        var domain = trendingResultList.get(i).domain ?: ""
                        var created_at = trendingResultList.get(i).created_at ?: ""
                        var modified_at = trendingResultList.get(i).modified_at ?: ""
                        var active = trendingResultList.get(i).active ?: false
                        var score = trendingResultList.get(i).score ?: 0.0f
                        var data = TrendingData(id, trendingList, domain, created_at, modified_at, active, score)
                        trendingArticleList.add(data)
                    }
                    articleNewsDao.insertTrendingAPIData(trendingArticleList)
                }
                refreshTrendingNews.isRefreshing = false
            }
        })
    }

    override fun onDestroyView() {
        fetchDataViewModel.getTrendingDataFromDb().removeObservers(this)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }
}