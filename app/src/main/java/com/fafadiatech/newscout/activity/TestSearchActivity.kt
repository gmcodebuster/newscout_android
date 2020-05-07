package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
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
import com.fafadiatech.newscout.model.GenericDataModel
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.model.SuggestResponse
import com.fafadiatech.newscout.paging.NewsDataSourceFactory
import com.fafadiatech.newscout.paging.SearchDataSourceFactory
import com.fafadiatech.newscout.paging.SourceDataSourceFactory
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import kotlinx.coroutines.*

class TestSearchActivity : AppCompatActivity(), ProgressBarListener {

    lateinit var rvNews: RecyclerView
    lateinit var query: String
    lateinit var nApi: ApiInterface
    lateinit var pBar: ProgressBar
    lateinit var themePreference: SharedPreferences
    lateinit var dataVM: FetchDataApiViewModel
    lateinit var emptyText: LinearLayout
    var emptyTextFlag: Boolean = false
    var deviceWidthDp: Float = 0f
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, NewsEntity>>
    lateinit var itemPagedList: LiveData<PagedList<NewsEntity>>
    lateinit var progressBarListener: ProgressBarListener
    //var suggestionList = ArrayList<String>()
    lateinit var fabReturnTop: com.github.clans.fab.FloatingActionButton
    lateinit var animFadein: Animation
    lateinit var animFadeout : Animation
    var lessThenTen = false
    var moreThenTen = true
    //private var apiJob: Job? = null
    var suggestionAdapter : ArrayAdapter<String>? = null
    lateinit var edtSearch : AutoCompleteTextView

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
        dataVM = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)
        this.setTheme(themes)
        setContentView(R.layout.test_activity_search)
        var toolbarText = findViewById<TextView>(R.id.toolbar_title)

        pBar = findViewById(R.id.progressBar_searchScreen)
        emptyText = findViewById(R.id.empty_message)
        edtSearch = findViewById(R.id.search_repo)
        edtSearch.setThreshold(1)


        rvNews = findViewById(R.id.news_item_search)
        fabReturnTop = findViewById(R.id.fab_return_top)
        fabReturnTop.visibility = View.GONE
        fabReturnTop.isClickable = false
        animFadein = AnimationUtils.loadAnimation(this@TestSearchActivity, R.anim.fade_in)
        animFadeout = AnimationUtils.loadAnimation(this@TestSearchActivity, R.anim.fade_out)

        if (deviceWidthDp < 600) {
            layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        } else {
            layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
            var divider = MyItemDecoration(ContextCompat.getDrawable(this, R.drawable.item_decorator_divider)!!)
        }
        rvNews.layoutManager = layoutManager


        var searchAdapter = SearchAdapter(this@TestSearchActivity, "Search", progressBarListener)
        rvNews.adapter = searchAdapter
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


        rvNews.visibility = View.VISIBLE
        emptyText.visibility = View.INVISIBLE
        //edtSearch.showDropDown()
        edtSearch.addTextChangedListener(object: TextWatcher{
            private var apiJob: Job? = null
            override fun afterTextChanged(p0: Editable?) {
                apiJob?.cancel()
                apiJob = startSearching(p0.toString())
                Log.d("TestSearchActivity", "Aft TxtChg : "+p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        dataVM.searchResultLiveData.observe(
                this@TestSearchActivity,
                Observer{
                    genericDataModel: GenericDataModel<SuggestResponse>? ->
                    run{
                        var suggestionList = ArrayList<String>()
                        //Log.d("Test SearchActivity", "Key : $searchQuery")
                        if(genericDataModel?.isSucess == true){
                            suggestionList.clear()
                            suggestionAdapter?.clear()
                            val data = genericDataModel.data
                            if(data?.header?.status == 1){

                                val result = data?.body?.result
                                for(r in result){
                                    suggestionList.add(r.value)
                                }
                                suggestionAdapter = ArrayAdapter<String>(this@TestSearchActivity, android.R.layout.simple_dropdown_item_1line, suggestionList)
                                edtSearch.setAdapter(suggestionAdapter)
                                edtSearch.threshold = 1
                                //suggestionAdapter.addAll(suggestionList)
                                //suggestionAdapter.notifyDataSetChanged()
//                                        resultTextView?.text = data.toString()
//                                        resultTextview?.visibility = View.VISIBLE
//                                        progressLoading?.visibility = View.GONE
                                Log.d("DATA Success : ", data.toString())
                            } else {
                                suggestionList.clear()
                                suggestionAdapter?.clear()
                            }
                        } else{
                            //suggestionList.clear()
                            suggestionAdapter?.clear()
//                                    resultTextView?.text = "No Data"
//                                    resultTextView?.visibility = View.VISIBLE
//                                    progressLoading?.visibility = View.GONE
                            Log.d("DATA Result: ", "No Data")
                        }
                    }
                }
        )

        /*btnCross.setOnClickListener {
            searchEditText.text.clear()
            emptyText.visibility = View.INVISIBLE
            dataVM.deleteSearchTableWork()
            emptyTextFlag = false
            val suggestionAdapter = ArrayAdapter(this@TestSearchActivity, android.R.layout.simple_dropdown_item_1line, suggestionList)
            searchAutoComplete.setAdapter(suggestionAdapter)

        }*/

        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)



//        query = searchView.query.toString()

        fabReturnTop.setOnClickListener {
            rvNews.smoothScrollToPosition(0)
        }

        edtSearch.setOnEditorActionListener { p0, p1, p2 ->
            var handle = false
            if(p1 == EditorInfo.IME_ACTION_SEARCH){
                Toast.makeText(this@TestSearchActivity, "Search ${p0?.text}", Toast.LENGTH_SHORT).show()
            }
            true
        }

        val obvSearch = Observer<PagedList<NewsEntity>>{
            Log.d("Search Activity", "Paged List :"+ it.size)
            Log.d("Search Activity", "Paged List snapshot :"+ it.snapshot().size)

            searchAdapter.submitList(it)
        }

        dataVM.initSearchNews("China",1)?.observe(this@TestSearchActivity, Observer<PagedList<NewsEntity>>{
            Log.d("Search Activity", "Paged List :"+ it.size)
            Log.d("Search Activity", "Paged List snapshot :"+ it.snapshot().size)

            searchAdapter.submitList(it)
        })

        /*dataVM.initializeNews("Banking", 1)
        dataVM.newsItemPagedList.observe(this, Observer<PagedList<INews>> {

            val newsList = it
            Log.d("Search Activity", "Paged List :"+ newsList.size)
            Log.d("Search Activity", "Paged List snapshot :"+ newsList.snapshot().size)
        })*/


        edtSearch.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                var handle = false
                if(p1 == EditorInfo.IME_ACTION_SEARCH){
                    Toast.makeText(this@TestSearchActivity, "Search ${p0?.text}", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@TestSearchActivity, "Search ${p0?.text}", Toast.LENGTH_SHORT).show()

                    //Call search API
                    /*dataVM.initSearchNews(p0!!.text.toString(),1)?.observe(this@TestSearchActivity, Observer<PagedList<NewsEntity>>{
                        Log.d("Search Activity", "Paged List :"+ it.size)
                        Log.d("Search Activity", "Paged List snapshot :"+ it.snapshot().size)

                        searchAdapter.submitList(it)
                    })*/

                }
                return true
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        dataVM.deleteSearchTableWork()
    }

    override fun showProgress() {
        pBar.visibility = View.GONE
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
        pBar.visibility = View.GONE
        if (show) {
            emptyText.visibility = View.VISIBLE
            rvNews.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            rvNews.visibility = View.VISIBLE
        }
    }

    private fun startSearching(searchQuery: String?) : Job{
        Log.d("TestSearchActivity", "Arg : $searchQuery ")
        //Log.d("Coroutine Job : ","Job Active: "+apiJob?.isActive)
        val job = CoroutineScope(Dispatchers.IO).launch{
            dataVM.getSearchResult(searchQuery)
            withContext(Dispatchers.Main){
                Log.d("TestSearchActivity", "WithContext() : $searchQuery")

            }
        }
        return job
    }

    fun getSerachResult(){

    }
}