package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
import com.fafadiatech.newscout.adapter.SearchAdapter
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.customcomponent.BaseAlertDialog
import com.fafadiatech.newscout.customcomponent.MyItemDecoration
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.interfaces.ProgressBarListener
import com.fafadiatech.newscout.paging.SearchDataSourceFactory
import com.fafadiatech.newscout.paging.SearchItemDataSource
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel

class SearchActivity : AppCompatActivity(), ProgressBarListener {

    lateinit var searchView: SearchView
    lateinit var rvNews: RecyclerView
    lateinit var query: String
    lateinit var apiInterface: ApiInterface
    lateinit var progressBar: ProgressBar
    lateinit var themePreference: SharedPreferences
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    lateinit var emptyText: TextView
    var emptyTextFlag: Boolean = false
    var deviceWidthDp: Float = 0f
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, NewsEntity>>
    lateinit var itemPagedList: LiveData<PagedList<NewsEntity>>
    lateinit var progressBarListener: ProgressBarListener
    var suggestionList = ArrayList<String>()
    lateinit var fabReturnTop: com.github.clans.fab.FloatingActionButton
    lateinit var animFadein: Animation
    lateinit var animFadeout : Animation
    var lessThenTen = false
    var moreThenTen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayMetrics = DisplayMetrics()
        val windowmanager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        themePreference = this.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        progressBarListener = this as ProgressBarListener
        val defaultNightMode = themePreference.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        getDelegate().setLocalNightMode(defaultNightMode)
        var themes: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        var isNightModeEnable = themePreference.getBoolean("night mode enable", false)
        fetchDataViewModel = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)
        this.setTheme(themes)
        setContentView(R.layout.activity_search)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)
        searchView = findViewById(R.id.search_view)
        progressBar = findViewById(R.id.progressBar_searchScreen)
        emptyText = findViewById(R.id.empty_message)
        var btnCross: ImageView = this.searchView.findViewById(androidx.appcompat.R.id.search_close_btn)

        var searchEditText: EditText = this.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        rvNews = findViewById(R.id.news_item_search)
        fabReturnTop = findViewById(R.id.fab_return_top)
        fabReturnTop.visibility = View.GONE
        fabReturnTop.isClickable = false
        animFadein = AnimationUtils.loadAnimation(this@SearchActivity, R.anim.fade_in)
        animFadeout = AnimationUtils.loadAnimation(this@SearchActivity, R.anim.fade_out)
        val searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
                as SearchView.SearchAutoComplete
        searchAutoComplete.threshold = 0

        rvNews?.addOnScrollListener(object: RecyclerView.OnScrollListener(){
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
                        fabReturnTop.visibility = View.GONE
                        fabReturnTop.startAnimation(animFadeout)
                        moreThenTen = true
                        lessThenTen = false
                    }
                }
            }
        })

        fetchDataViewModel.getSearchSuggestedData().observe(this, object : androidx.lifecycle.Observer<List<String>> {
            override fun onChanged(list: List<String>?) {
                suggestionList = list as ArrayList<String>
                val suggestionAdapter = ArrayAdapter(this@SearchActivity, android.R.layout.simple_dropdown_item_1line, suggestionList)
                searchAutoComplete.setAdapter(suggestionAdapter)

            }
        })

        searchAutoComplete.setOnItemClickListener { parent, view, position, id ->
            var queryString = parent.getItemAtPosition(position) as String
            searchAutoComplete.setText(queryString)
            searchEditText.setSelection(queryString.length)


        }
        rvNews.visibility = View.GONE
        emptyText.visibility = View.VISIBLE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryText: String?): Boolean {

                query = queryText!!
                query = queryText.trim()
                if (query.length == 0) {
                    BaseAlertDialog.showAlertDialog(this@SearchActivity, "Please enter some words")
                } else {

                    var deviceId = themePreference.getString("device_token", "")
                    fetchDataViewModel.startSearchSuggestionWorkManager(query)
                    //trackUserSearch(apiInterface, "search", deviceId, "android", query)

                    progressBar.visibility = View.VISIBLE
                    fetchDataViewModel.deleteSearchTableWork()

                    var searchAdapter = SearchAdapter(this@SearchActivity, "Search", progressBarListener)
                    val itemDataSourceFactory = SearchDataSourceFactory(this@SearchActivity, query)

                    rvNews.adapter = searchAdapter
                    liveDataSource = itemDataSourceFactory.itemLiveDataSource

                    val pagedListConfig = PagedList.Config.Builder()
                            .setEnablePlaceholders(false)
                            .setPageSize(NEWSPAGESIZE)
                            .build()
                    itemPagedList = LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                            .build()


                    itemPagedList.observe(this@SearchActivity, Observer<PagedList<NewsEntity>> {
                        progressBar.visibility = View.VISIBLE
                        Log.d("Search Activity", "Paged List :"+ it.size)

                        Log.d("Search Activity", "Paged List snapshot :"+ it.snapshot().size)
                        showEmptyList(it?.size == 0)
                        searchAdapter.submitList(it)

                        it.addWeakCallback(null, object:PagedList.Callback(){
                            override fun onChanged(position: Int, count: Int) {
                                Log.d("SearchActivity", "onChanged Size : "+count)
                                if(count == 0){
                                    Log.d("","")
                                    progressBar.visibility = View.GONE
                                    emptyText.visibility = View.VISIBLE
                                    emptyText.text = "No data found"
                                }else{
                                    progressBar.visibility = View.VISIBLE
                                    emptyText.visibility = View.GONE
                                }
                            }

                            override fun onInserted(position: Int, count: Int) {
                                Log.d("SearchActivity", "onInserted Size : "+count)
                                if(count == 0){
                                    progressBar.visibility = View.GONE
                                    emptyText.visibility = View.VISIBLE
                                    emptyText.text = "No data found"
                                }else{
                                    progressBar.visibility = View.GONE
                                    rvNews.visibility = View.VISIBLE
                                    emptyText.visibility = View.GONE
                                }
                            }

                            override fun onRemoved(position: Int, count: Int) {

                            }
                        })

                    })
                    val sessionId = getUniqueCode(this@SearchActivity, themePreference)
                    trackingCallback(apiInterface, themePreference, 0, queryText, 0, "", "", ActionType.SEARCHQUERY.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId,"",0)

                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.length >= 2) {
                    var query = newText + "%"
                    var titleList = fetchDataViewModel.getTitleBySearch(query)
                    val suggestionAdapter = ArrayAdapter(this@SearchActivity, android.R.layout.simple_dropdown_item_1line, titleList)
                    searchAutoComplete.setAdapter(suggestionAdapter)

                }
                return false
            }
        })

        btnCross.setOnClickListener {
            searchEditText.text.clear()
            emptyText.visibility = View.INVISIBLE
            fetchDataViewModel.deleteSearchTableWork()
            emptyTextFlag = false
            val suggestionAdapter = ArrayAdapter(this@SearchActivity, android.R.layout.simple_dropdown_item_1line, suggestionList)
            searchAutoComplete.setAdapter(suggestionAdapter)

        }

        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        if (deviceWidthDp < 600) {
            layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        } else {
            layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
            var divider = MyItemDecoration(ContextCompat.getDrawable(this, R.drawable.item_decorator_divider)!!)
        }

        rvNews.layoutManager = layoutManager
        query = searchView.query.toString()

        fabReturnTop.setOnClickListener {
            rvNews.smoothScrollToPosition(0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        fetchDataViewModel.deleteSearchTableWork()
    }

    override fun showProgress() {
        progressBar.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    private val lastVisibleItemPosition: Int
        get() = (rvNews!!.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()

    private fun showEmptyList(show: Boolean) {
        progressBar.visibility = View.GONE
        if (show) {
            emptyText.visibility = View.VISIBLE
            rvNews.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            rvNews.visibility = View.VISIBLE
        }
    }
}