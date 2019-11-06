package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.appconstants.NEWSPAGESIZE
import com.fafadiatech.newscout.appconstants.trackUserSearch
import com.fafadiatech.newscout.customcomponent.BaseAlertDialog
import com.fafadiatech.newscout.customcomponent.MyItemDecoration
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.interfaces.ProgressBarListener
import com.fafadiatech.newscout.paging.SearchDataSourceFactory
import com.fafadiatech.newscout.paging.SearchItemDataSource
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel

class SearchActivity : AppCompatActivity(), ProgressBarListener {

    lateinit var searchView: SearchView
    lateinit var recyclerViewSearch: RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayMetrics = DisplayMetrics()
        val windowmanager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        themePreference = this.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        progressBarListener = this as ProgressBarListener
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
        btnCross.setImageResource(R.drawable.ic_clear_black_24dp)
        var searchEditText: EditText = this.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        recyclerViewSearch = findViewById(R.id.news_item_search)
        fabReturnTop = findViewById(R.id.fab_return_top)
        val searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
                as SearchView.SearchAutoComplete
        searchAutoComplete.threshold = 0

        fetchDataViewModel.getSearchSuggestedData().observe(this, object : androidx.lifecycle.Observer<List<String>> {
            override fun onChanged(list: List<String>?) {
                suggestionList = list as ArrayList<String>
                val suggestionAdapter = ArrayAdapter(this@SearchActivity, android.R.layout.simple_dropdown_item_1line, suggestionList)
                searchAutoComplete.setAdapter(suggestionAdapter)
                searchAutoComplete.setTextColor(ContextCompat.getColor(this@SearchActivity, R.color.black))
            }
        })

        searchAutoComplete.setOnItemClickListener { parent, view, position, id ->
            var queryString = parent.getItemAtPosition(position) as String
            searchAutoComplete.setText(queryString)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryText: String?): Boolean {

                query = queryText!!
                query = queryText.trim()
                if (query.length == 0) {
                    BaseAlertDialog.showAlertDialog(this@SearchActivity, "Please enter some words")
                } else {

                    var deviceId = themePreference.getString("device_token", "")
                    fetchDataViewModel.startSearchSuggestionWorkManager(query)
                    trackUserSearch(apiInterface, "search", deviceId, "android", query)

                    progressBar.visibility = View.VISIBLE
                    fetchDataViewModel.deleteSearchTableWork()

                    var searchAdapter = SearchAdapter(this@SearchActivity, "Search", progressBarListener)
                    val itemDataSourceFactory = SearchDataSourceFactory(this@SearchActivity, query)

                    recyclerViewSearch.adapter = searchAdapter
                    liveDataSource = itemDataSourceFactory.itemLiveDataSource

                    val pagedListConfig = PagedList.Config.Builder()
                            .setEnablePlaceholders(false)
                            .setPageSize(NEWSPAGESIZE)
                            .build()
                    itemPagedList = LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                            .build()

                    itemPagedList.observe(this@SearchActivity, Observer<PagedList<NewsEntity>> {

                        searchAdapter.submitList(it)

                    })
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.length >= 2) {
                    var query = newText + "%"
                    var titleList = fetchDataViewModel.getTitleBySearch(query)
                    val suggestionAdapter = ArrayAdapter(this@SearchActivity, android.R.layout.simple_dropdown_item_1line, titleList)
                    searchAutoComplete.setAdapter(suggestionAdapter)
                    searchAutoComplete.setTextColor(ContextCompat.getColor(this@SearchActivity, R.color.black))
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
            searchAutoComplete.setTextColor(ContextCompat.getColor(this@SearchActivity, R.color.black))
        }

        if (isNightModeEnable) {
            searchView.background = ContextCompat.getDrawable(this, R.color.top_back_color)
        }

        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        if (deviceWidthDp < 600) {
            layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        } else {
            layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
            var divider = MyItemDecoration(ContextCompat.getDrawable(this, R.drawable.item_decorator_divider)!!)
        }

        recyclerViewSearch.layoutManager = layoutManager
        query = searchView.query.toString()

        fabReturnTop.setOnClickListener {
            recyclerViewSearch.smoothScrollToPosition(0)
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
}