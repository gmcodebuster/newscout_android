package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.adapter.BookmarkedNewsAdapter
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.broadcast.ConnectivityReceiver
import com.fafadiatech.newscout.customcomponent.MyItemDecoration
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel


class BookmarkActivity : BaseActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    lateinit var rvBookmark: RecyclerView
    var bookmarkedList = ArrayList<DetailNewsData>()
    lateinit var themePreference: SharedPreferences
    lateinit var emptyListTextView: TextView
    var category: String = ""
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    lateinit var bookmarkAdapter: BookmarkedNewsAdapter
    lateinit var token: String
    var deviceWidthDp: Float = 0f
    lateinit var layoutManager: LinearLayoutManager
    lateinit var fabReturnTop: com.github.clans.fab.FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayMetrics = DisplayMetrics()
        val windowmanager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        themePreference = getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        token = themePreference.getString("token value", "")
        var themes: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        this.setTheme(themes)
        setContentView(R.layout.activity_bookmark)
        fetchDataViewModel = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)
        emptyListTextView = findViewById(R.id.empty_message)
        rvBookmark = findViewById(R.id.rv_bookmark)
        bookmarkAdapter = BookmarkedNewsAdapter(this, category)
        fabReturnTop = findViewById(R.id.fab_return_top)
        if (token == "") {
            emptyListTextView.text = resources.getString(R.string.see_bookmark_msg)
        } else {

            fetchDataViewModel.getBookmarkNewsFromDb()
                    .observe(this, object : androidx.lifecycle.Observer<List<DetailNewsData>> {
                        override fun onChanged(list: List<DetailNewsData>?) {
                            bookmarkedList = list as ArrayList<DetailNewsData>
                            bookmarkAdapter.setData(bookmarkedList)

                            if (bookmarkedList.size == 0 || bookmarkedList == null) {
                                emptyListTextView.text = resources.getString(R.string.no_bookmark)
                                emptyListTextView.visibility = View.VISIBLE
                            } else {
                                emptyListTextView.visibility = View.GONE
                            }
                        }
                    })
        }

        var isNightModeEnable = themePreference.getBoolean("night mode enable", false)
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        if (deviceWidthDp < 600) {

            layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

            if (!isNightModeEnable) {
                itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.news_item_divider)!!)
            } else {
                itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.news_item_divider_night)!!)
            }
        } else {

            layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
            if (!isNightModeEnable) {
                var divider = MyItemDecoration(ContextCompat.getDrawable(this, R.drawable.item_decorator_divider)!!)
            } else {
                var divider = MyItemDecoration(ContextCompat.getDrawable(this, R.drawable.item_decorator_divider_night)!!)
            }
        }

        rvBookmark.layoutManager = layoutManager
        rvBookmark.adapter = bookmarkAdapter
        fabReturnTop.setOnClickListener {
            rvBookmark.smoothScrollToPosition(0)
        }
    }

    override fun onResume() {
        super.onResume()
        if (token == "") {
            emptyListTextView.text = resources.getString(R.string.see_bookmark_msg)
        } else {
            bookmarkAdapter = BookmarkedNewsAdapter(this@BookmarkActivity, "")
            var result = fetchDataViewModel.getBookmarkListFromDb()
            bookmarkedList = result as ArrayList<DetailNewsData>
            rvBookmark.adapter = bookmarkAdapter
            bookmarkAdapter.setData(bookmarkedList)
        }
    }


    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        super.onNetworkConnectionChanged(isConnected)
    }

    override fun onStop() {
        super.onStop()
    }
}