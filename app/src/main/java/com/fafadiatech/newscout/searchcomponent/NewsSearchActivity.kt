package com.fafadiatech.newscout.searchcomponent

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.DetailNewsActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.application.GlideApp
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.model.ArticlesData
import com.fafadiatech.newscout.model.GenericDataModel
import com.fafadiatech.newscout.model.SuggestResponse
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import kotlinx.android.synthetic.main.test_activity_search_git.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class NewsSearchActivity: AppCompatActivity(), OnNewsItemClickListener {

    companion object {
        const val KEY_NEWS_SEARCH = "github_user"
        const val DEFAULT_USER = ""
    }

    private lateinit var list: RecyclerView
    private lateinit var model: SearchViewModel
    private lateinit var autoSearch: SearchView
    private val glideRequests by lazy { GlideApp.with(this) }
    lateinit var toolbar: Toolbar
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    lateinit var nApi: ApiInterface
    lateinit var themePreference: SharedPreferences
    lateinit var dataVM: FetchDataApiViewModel
    lateinit var suggestionAdapter : ArrayAdapter<String>
    private var apiJob: Job? = null
    var suggestionList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_search)
        dataVM = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar_home_sc)

        list = findViewById(R.id.list)
        autoSearch = findViewById(R.id.search_repo)
        model = viewModel()
        initAutoCompleteView()
        initAdapter()
        initSwipeToRefresh()
        val searchQuery = savedInstanceState?.getString(KEY_NEWS_SEARCH) ?: DEFAULT_USER
//        model.showSearchResults(searchQuery)
    }

    private fun initAutoCompleteView(){
        var btnCross: ImageView = autoSearch.findViewById(androidx.appcompat.R.id.search_close_btn)

        var searchEditText: EditText = autoSearch.findViewById(androidx.appcompat.R.id.search_src_text)
        searchEditText.setHintTextColor(ContextCompat.getColor(this@NewsSearchActivity, R.color.search_hint_color))

        val searchAutoComplete = autoSearch.findViewById(androidx.appcompat.R.id.search_src_text)
                as SearchView.SearchAutoComplete
        searchAutoComplete.threshold = 0

        suggestionAdapter = ArrayAdapter<String>(this@NewsSearchActivity, android.R.layout.simple_dropdown_item_1line)
        searchAutoComplete.setAdapter(suggestionAdapter)
        autoSearch.setOnQueryTextListener(onQueryTextListener)

        dataVM.getSearchSuggestedData().observe(this, object : androidx.lifecycle.Observer<List<String>> {
            override fun onChanged(list: List<String>?) {
                suggestionList = list as ArrayList<String>
            }
        })

        searchAutoComplete.setOnItemClickListener { parent, view, position, id ->
            var queryString = parent.getItemAtPosition(position) as String
            searchAutoComplete.setText(queryString)
            searchEditText.setSelection(queryString.length)
        }

        btnCross.setOnClickListener {
            searchEditText.text.clear()
            dataVM.deleteSearchTableWork()
            val suggestionAdapter = ArrayAdapter(this@NewsSearchActivity, android.R.layout.simple_dropdown_item_1line, suggestionList)
            searchAutoComplete.setAdapter(suggestionAdapter)

        }
    }

    private fun provideGNewsApiService(): GNewsApiService {
        return GNewsApiService(GNewsApi.create())
    }

    private fun provideGithubRepository(): GNewsRepository {
        return InMemoryByPageKeyRepository(provideGNewsApiService(), NETWORK_IO, getDatabase())
    }

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return SearchViewModelFactory(provideGithubRepository())
    }

    private fun viewModel(): SearchViewModel {
        val viewModelFactory = provideViewModelFactory()
        return ViewModelProviders.of(this, viewModelFactory)[SearchViewModel::class.java]
    }

    private fun initAdapter() {
        val adapter = NewsSearchAdapter(glideRequests, this) {
            model.retry()
        }
        list.adapter = adapter
        model.items.observe(this, Observer<PagedList<ArticlesData>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            model.refresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_NEWS_SEARCH, model.currentSearchQuery())
    }

    /**
     * Search configuration
     */
    private var searchView: SearchView? = null

    private var onQueryTextListener: SearchView.OnQueryTextListener? = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchGithub(query)
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            // do nothing call suggestion api and display list
            apiJob?.cancel()
            startSearching(newText)

            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_search, menu)
//        searchView = searchView(menu)
//        searchView?.queryHint = getString(R.string.search)
//        searchView?.setOnQueryTextListener(onQueryTextListener)
        return true
    }

    private fun searchView(menu: Menu?): SearchView? {
        val searchItem = menu?.findItem(R.id.action_search)
        return searchItem?.actionView as? SearchView
    }
    private fun hideKeyboard() {
        if (searchView?.hasFocus() == true) searchView?.clearFocus()
    }

    private fun searchGithub(searchQuery: String) {
        searchQuery.trim().let {
            if (it.isNotEmpty()) {
                if (model.showSearchResults(it)) {
                    list.scrollToPosition(0)
                    (list.adapter as? NewsSearchAdapter)?.submitList(null)
                    hideKeyboard()
                }
            }
        }
    }

    private fun getDatabase(): NewsDatabase? {
        return NewsDatabase.getInstance(this@NewsSearchActivity)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        onQueryTextListener = null
        super.onDestroy()
    }

    override fun onItemClick(item: ArticlesData?, position: Int) {
        item?.let {

            var id = item.id
//            dataVM.startRecommendNewsWorkManager(id)

            var itemTitle = item.title
            var deviceId = themePreference.getString("device_token", "")

            val sessionId = getUniqueCode(this, themePreference)
            val title = item.title
            val cName = item.category
            val categoryId = item.category_id
            val source = item.source
            trackingCallback(nApi, themePreference, id, title, categoryId, cName, "", ActionType.ARTICLESEARCHDETAIL.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, source, 0)

            var detailIntent = Intent(this, DetailNewsActivity::class.java)
            detailIntent.putExtra("indexPosition", position)
            detailIntent.putExtra("category_of_newslist", "Search")
            detailIntent.putExtra("category_id", categoryId)
            startActivity(detailIntent)
        }
    }

    private fun startSearching(searchQuery: String?){
        apiJob = CoroutineScope(Dispatchers.IO).launch{
            dataVM.getSearchResult(searchQuery)
            withContext(Dispatchers.Main){
                dataVM.searchResultLiveData.observe(
                        this@NewsSearchActivity,
                        Observer{
                            genericDataModel: GenericDataModel<SuggestResponse>? ->
                            run{
                                if(genericDataModel?.isSucess == true){
                                    val data = genericDataModel.data
                                    if(data?.header?.status == 1){
                                        suggestionList.clear()
                                        val result = data?.body?.result
                                        suggestionList.clear()
                                        for(r in result){
                                            suggestionList.add(r.value)
                                        }
                                        suggestionAdapter.addAll(suggestionList)
//                                        resultTextView?.text = data.toString()
//                                        resultTextview?.visibility = View.VISIBLE
//                                        progressLoading?.visibility = View.GONE
                                        Log.d("DATA Result: ", data.toString())
                                    } else {

                                    }
                                } else{
//                                    resultTextView?.text = "No Data"
//                                    resultTextView?.visibility = View.VISIBLE
//                                    progressLoading?.visibility = View.GONE
                                    Log.d("DATA Result: ", "No Data")
                                }
                            }
                        }
                )
            }
        }
    }
}