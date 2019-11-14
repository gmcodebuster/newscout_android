package com.fafadiatech.newscout.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.adapter.NewsAdapter
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.appconstants.NEWSPAGESIZE
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.broadcast.ConnectivityReceiver
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.interfaces.PlaceHolderImageListener
import com.fafadiatech.newscout.paging.NewsDataSourceFactory
import com.fafadiatech.newscout.paging.NewsItemDataSource
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.fafadiatech.newscout.viewmodel.ViewModelProviderFactory
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import kotlinx.android.synthetic.main.fragment_main.*


class NewsFragment() : Fragment(), ConnectivityReceiver.ConnectivityReceiverListener, PlaceHolderImageListener {

    lateinit var fragRecyclerview: RecyclerView
    lateinit var themePreference: SharedPreferences
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    lateinit var tokenId: String
    lateinit var mContext: Context
    var checkInternet: Boolean = false
    var firstLoad: Boolean = false
    var isShown: Boolean = false
    lateinit var tagName: String
    var tagId: Int = 0
    lateinit var adapter: NewsAdapter
    lateinit var itemPagedList: LiveData<PagedList<NewsEntity>>
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, NewsEntity>>
    var deviceWidthDp: Float = 0f
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var fabReturnTop: com.github.clans.fab.FloatingActionButton
    var showReturnToTopButton: Boolean = true
    lateinit var placeHolderListener: PlaceHolderImageListener
    lateinit var placeHolderImage: ImageView
    lateinit var imgViewNoDataFound: ImageView
    var lessThenTen = false
    var moreThenTen = true
    lateinit var animFadein: Animation
    lateinit var animFadeout : Animation
    var progressBar :ProgressBar? = null

    companion object {
        var newsList = ArrayList<NewsEntity>()
        lateinit var categoryName: String
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        themePreference = context!!.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        tokenId = themePreference.getString("token value", "")
        if (tokenId == null) {
            tokenId = ""
        }
        this.mContext = context
        MyApplication.zeroItemFlag = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = this.activity!!.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var theme: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        var isNightModeEnable = themePreference.getBoolean("night mode enable", false)
        activity?.setTheme(theme)
        placeHolderListener = this as PlaceHolderImageListener
        categoryName = ""
        val displayMetrics = DisplayMetrics()
        val windowmanager = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        var view = LayoutInflater.from(context).inflate(R.layout.fragment_main, container, false)
        var rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout_main_fragment)
        if (isNightModeEnable) {
            rootLayout.setBackgroundColor(context?.let { ContextCompat.getColor(it, R.color.night_mode_background) }!!)
        } else {
            rootLayout.setBackgroundColor(context?.let { ContextCompat.getColor(it, R.color.top_back_color) }!!)
        }

        var layoutSwipeRefresh = view.findViewById<SwipyRefreshLayout>(R.id.layout_swipe_refresh)
        fragRecyclerview = view.findViewById(R.id.rv_frag_main)
        placeHolderImage = view.findViewById<ImageView>(R.id.img_view_placeholder)
        imgViewNoDataFound = view.findViewById<ImageView>(R.id.img_view_data_not_found)
        fabReturnTop = view.findViewById(R.id.fab_return_top)
        tagName = arguments!!.getString("category_name", "")
        tagId = arguments!!.getInt("category_id", 0)
        adapter = NewsAdapter(context!!, tagName)
        imgViewNoDataFound.visibility = View.GONE
        fabReturnTop.visibility = View.GONE
        fabReturnTop.isClickable = false
        animFadein = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
        animFadeout = AnimationUtils.loadAnimation(activity, R.anim.fade_out)
        progressBar = view.findViewById(R.id.pbar_loading)
        fragRecyclerview?.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if(lastVisibleItemPosition > 10){
                    if(moreThenTen) {
                        fabReturnTop.isClickable = true
                        fabReturnTop.startAnimation(animFadein)
                        fabReturnTop.visibility = View.VISIBLE
                        moreThenTen = false
                        lessThenTen = true
                    }
                } else{
                    if(lessThenTen) {
                        fabReturnTop.isClickable = false
                        fabReturnTop.visibility = View.INVISIBLE
                        fabReturnTop.startAnimation(animFadeout)
                        moreThenTen = true
                        lessThenTen = false
                    }
                }
            }
        })
        val itemDecorator = DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL)

        if (deviceWidthDp < 600) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            if (!isNightModeEnable) {
                itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.news_item_divider)!!)
            } else {
                itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.news_item_divider_night)!!)
            }
        } else {
            layoutManager = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            val hDivider = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)

            if (!isNightModeEnable) {
                rootLayout.setBackgroundColor(context?.let { ContextCompat.getColor(it, R.color.tab_rv_bg) }!!)
            } else {
                rootLayout.setBackgroundColor(context?.let { ContextCompat.getColor(it, R.color.grey_light) }!!)
            }
        }

        fragRecyclerview.layoutManager = object : LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                if (showReturnToTopButton && (findLastVisibleItemPosition() - findFirstVisibleItemPosition() + 1 < adapter.itemCount)) {
                    showReturnToTopButton = false
                }
            }
        }

        fabReturnTop.setOnClickListener {
            fragRecyclerview.smoothScrollToPosition(0)
        }

        fragRecyclerview.layoutManager = layoutManager

        if (!tagName.equals("Trending")) {
            fetchDataViewModel = ViewModelProviders.of(this, ViewModelProviderFactory(activity!!.application, tagId)).get(FetchDataApiViewModel::class.java)
            fetchDataViewModel.initializeNews(tagId, 1).observe(this@NewsFragment, Observer<PagedList<NewsEntity>> {

                adapter.setPlaceHolderImage(placeHolderListener)
                adapter.submitList(it)
            })
        }

        checkInternet = MyApplication.checkInternet

        layoutSwipeRefresh.setOnRefreshListener(object : SwipyRefreshLayout.OnRefreshListener {
            override fun onRefresh(direction: SwipyRefreshLayoutDirection?) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    fetchDataViewModel.invalidateDataSource()
                    if (checkInternet == true) {
                        val itemDataSourceFactory = NewsDataSourceFactory(activity!!.application, tagId)
                        liveDataSource = itemDataSourceFactory.itemLiveDataSource

                        val pagedListConfig = PagedList.Config.Builder()
                                .setEnablePlaceholders(false)
                                .setPageSize(NEWSPAGESIZE)
                                .build()


                        itemPagedList = LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                                .build()
                        itemPagedList.observe(this@NewsFragment, Observer<PagedList<NewsEntity>> {
                            adapter.submitList(it)

                        })
                    }

                    var tagItems = ArrayList<String>()
                    if (categoryName != null && categoryName!!.contains("&tag=")) {
                        tagItems = categoryName!!.split("&tag=") as ArrayList<String>
                    }

                    var tagsArray = arrayOfNulls<String>(tagItems.size)
                    tagItems.toArray(tagsArray)
                    fetchDataViewModel.initializeNews(tagId, 1).observe(this@NewsFragment, Observer<PagedList<NewsEntity>> {

                        adapter.submitList(it)
                    })
                }
                layoutSwipeRefresh.isRefreshing = false
            }
        })

        if (checkInternet == true) {

            if (firstLoad == false) {
                firstLoad = true
            }
        }

        fragRecyclerview.adapter = adapter
        return view
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (view != null) {
            isShown = true
        } else {
            isShown = false
        }
    }


    override fun onNetworkConnectionChanged(isConnected: Boolean) {
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
    }

    override fun showPlaceHolder(size: Int?) {
        progressBar?.visibility = View.GONE
        if (size!! > 0) {
            imgViewNoDataFound.visibility = View.GONE

        } else {
            imgViewNoDataFound.visibility = View.VISIBLE
        }
    }

    private val lastVisibleItemPosition: Int
        get() = (fragRecyclerview!!.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()
}