package com.fafadiatech.newscout.searchcomponent

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.application.GlideApp
import com.fafadiatech.newscout.model.ArticlesData
import kotlinx.android.synthetic.main.test_activity_search_git.*
import java.util.concurrent.Executors

class NewsSearchActivity: AppCompatActivity() {

    companion object {
        const val KEY_NEWS_SEARCH = "github_user"
        const val DEFAULT_USER = ""
    }

    private lateinit var list: RecyclerView
    private lateinit var model: SearchViewModel
    private val glideRequests by lazy { GlideApp.with(this) }
    lateinit var toolbar: Toolbar
    private val NETWORK_IO = Executors.newFixedThreadPool(5)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_search_git)
        toolbar = findViewById(R.id.toolbar_home_sc)
        setSupportActionBar(toolbar)
        list = findViewById(R.id.list)

        model = viewModel()
        initAdapter()
        initSwipeToRefresh()
        val searchQuery = savedInstanceState?.getString(KEY_NEWS_SEARCH) ?: DEFAULT_USER
//        model.showSearchResults(searchQuery)
    }

    private fun provideGNewsApiService(): GNewsApiService {
        return GNewsApiService(GNewsApi.create())
    }

    private fun provideGithubRepository(): GNewsRepository {
        return InMemoryByPageKeyRepository(provideGNewsApiService(), NETWORK_IO)
    }

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return SearchViewModelFactory(provideGithubRepository())
    }

    private fun viewModel(): SearchViewModel {
        val viewModelFactory = provideViewModelFactory()
        return ViewModelProviders.of(this, viewModelFactory)[SearchViewModel::class.java]
    }

    private fun initAdapter() {
        val adapter = NewsSearchAdapter(glideRequests) {
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
            // do nothing
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        searchView = searchView(menu)
        searchView?.queryHint = getString(R.string.search)
        searchView?.setOnQueryTextListener(onQueryTextListener)
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
}