package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.adapter.NewsAdapter
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.appconstants.NEWSPAGESIZE
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.interfaces.PlaceHolderImageListener
import com.fafadiatech.newscout.model.ArticlesData
import com.fafadiatech.newscout.paging.SourceDataSourceFactory
import com.fafadiatech.newscout.paging.SourceItemDataSource

class SourceActivity : AppCompatActivity(), PlaceHolderImageListener {

    lateinit var interfaceObj: ApiInterface
    var list = ArrayList<ArticlesData>()
    var newsList = ArrayList<NewsEntity>()
    lateinit var adapter: NewsAdapter
    lateinit var itemPagedList: LiveData<PagedList<NewsEntity>>
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, NewsEntity>>
    var source: String = ""
    var deviceWidthDp: Float = 0f
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var placeHolderListener: PlaceHolderImageListener
    lateinit var themePreference: SharedPreferences
    lateinit var fabReturnTop: com.github.clans.fab.FloatingActionButton
    var lessTen = false
    var moreTen = true
    lateinit var animFadein: Animation
    lateinit var animFadeout : Animation
    lateinit var rvSource: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placeHolderListener = this
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val displayMetrics = DisplayMetrics()
            val windowmanager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowmanager.defaultDisplay.getMetrics(displayMetrics)
            val deviceWidth = displayMetrics.widthPixels
            deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
            if (deviceWidthDp < 600) {
            }
        }

        themePreference = this.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        var themes: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        var isNightModeEnable = themePreference.getBoolean("night mode enable", false)
        val defaultNightMode = themePreference.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        getDelegate().setLocalNightMode(defaultNightMode)
        this.setTheme(themes)
        setContentView(R.layout.activity_source)
        rvSource = findViewById<RecyclerView>(R.id.rv_source)
        var emptyView = findViewById<TextView>(R.id.empty_view)
        var toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        emptyView.visibility = View.GONE
        interfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        var adapterObj = NewsAdapter(this, "Source")
        fabReturnTop = findViewById(R.id.fab_return_top)
        fabReturnTop.visibility = View.INVISIBLE
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        if (deviceWidthDp < 600) {
            layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        } else {
            itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.news_item_divider_tab_horizontal)!!)
            layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
            val hDivider = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
            hDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.news_item_divider_verticle)!!)
        }

        rvSource.layoutManager = layoutManager
        rvSource.adapter = adapterObj
        var intent = intent
        var source = intent.getStringExtra("source_from_detail")
        val itemDataSourceFactory = SourceDataSourceFactory(this.application, source)
        toolbarTitle.text = source
        liveDataSource = itemDataSourceFactory.itemLiveDataSource
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(NEWSPAGESIZE).build()

        itemPagedList = LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                .build()
        itemPagedList.observe(this, Observer<PagedList<NewsEntity>> {
            adapterObj.submitList(it)
        })

        animFadein = AnimationUtils.loadAnimation(this@SourceActivity, R.anim.fade_in)
        animFadeout = AnimationUtils.loadAnimation(this@SourceActivity, R.anim.fade_out)

        rvSource?.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if(lastVisibleItemPosition > 10){
                    if(moreTen) {
                        fabReturnTop.startAnimation(animFadein)
                        fabReturnTop.visibility = View.VISIBLE
                        moreTen = false
                        lessTen = true
                    }
                } else{
                    if(lessTen) {
                        fabReturnTop.visibility = View.INVISIBLE
                        fabReturnTop.startAnimation(animFadeout)
                        moreTen = true
                        lessTen = false
                    }
                }
            }
        })

        fabReturnTop.setOnClickListener {
            rvSource.smoothScrollToPosition(0)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    private val lastVisibleItemPosition: Int
        get() = (rvSource!!.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()
}