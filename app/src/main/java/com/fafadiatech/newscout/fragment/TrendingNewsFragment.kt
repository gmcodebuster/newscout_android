package com.fafadiatech.newscout.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.adapter.TrendingNewsAdapter
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.customcomponent.MyItemDecoration
import com.fafadiatech.newscout.db.NewsEntity

import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout

class TrendingNewsFragment : Fragment() {

    lateinit var themePreference: SharedPreferences
    lateinit var tokenId: String
    lateinit var mContext: Context
    lateinit var rvNews: RecyclerView
    var deviceWidthDp: Float = 0f
    lateinit var tagName: String
    var tagId: Int = 0
    lateinit var adapter: TrendingNewsAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    lateinit var fabReturnTop: com.github.clans.fab.FloatingActionButton
    var checkInternet: Boolean = false
    var lessThenTen = false
    var moreThenTen = true
    lateinit var animFadein: Animation
    lateinit var animFadeout : Animation
    var progressBar : ProgressBar? = null
    lateinit var imgViewNoDataFound: ImageView
    override fun onAttach(context: Context) {
        super.onAttach(context)
        themePreference = context!!.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        tokenId = themePreference.getString("token value", "")
        if (tokenId == null) {
            tokenId = ""
        }
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val displayMetrics = DisplayMetrics()
            val windowmanager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowmanager.defaultDisplay.getMetrics(displayMetrics)
            val deviceWidth = displayMetrics.widthPixels
            deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
            if (deviceWidthDp < 600) {

            }
        }

        var theme: Int = themePreference.getInt("theme", R.style.DefaultMedium)
        var isNightModeEnable = themePreference.getBoolean("night mode enable", false)
        activity?.setTheme(theme)
        NewsFragment.categoryName = ""

        if (NewsFragment.categoryName == "For You") {
            NewsFragment.categoryName = ""
        }

        var view = LayoutInflater.from(context).inflate(R.layout.fragment_main, container, false)
        fabReturnTop = view.findViewById(R.id.fab_return_top)
        var rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout_main_fragment)
        if (isNightModeEnable) {
            rootLayout.setBackgroundColor(context?.let { ContextCompat.getColor(it, R.color.night_mode_background) }!!)
        } else {
            rootLayout.setBackgroundColor(context?.let { ContextCompat.getColor(it, R.color.top_back_color) }!!)
        }

        var layoutSwipeRefresh = view.findViewById<SwipyRefreshLayout>(R.id.layout_swipe_refresh)


        rvNews = view.findViewById<RecyclerView>(R.id.rv_frag_main)

        tagName = arguments!!.getString("category_name", "")
        tagId = arguments!!.getInt("category_id", 0)
        var clusterId = arguments!!.getInt("cluster_id", 0)

        adapter = TrendingNewsAdapter(context!!, tagName)
        adapter.setClustedId(clusterId)

        val itemDecorator = DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL)

        if (deviceWidthDp < 600) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        } else {
            layoutManager = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)

            if (!isNightModeEnable) {
                var divider = MyItemDecoration(ContextCompat.getDrawable(context!!, R.drawable.item_decorator_divider)!!)
            } else {
                var divider = MyItemDecoration(ContextCompat.getDrawable(context!!, R.drawable.item_decorator_divider_night)!!)
            }
        }

        rvNews.layoutManager = layoutManager

        animFadein = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
        animFadeout = AnimationUtils.loadAnimation(activity, R.anim.fade_out)
        progressBar = view.findViewById<ProgressBar>(R.id.pbar_loading)
        imgViewNoDataFound = view.findViewById<ImageView>(R.id.img_view_data_not_found)
        imgViewNoDataFound.visibility = View.GONE
        fabReturnTop.visibility = View.GONE
        fabReturnTop.isClickable = false
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

        if (tagName.equals("Trending")) {
            fetchDataViewModel = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)


            fetchDataViewModel.getTrendingByClusterIdFromDb(clusterId).observe(this, object : androidx.lifecycle.Observer<List<NewsEntity>> {
                override fun onChanged(list: List<NewsEntity>?) {
                    progressBar?.visibility = View.GONE

                   list?.let {
                       if(list.size == 0){
                           imgViewNoDataFound.visibility = View.VISIBLE
                       }else{
                           imgViewNoDataFound.visibility = View.GONE
                       }
                   }
                    var trendingList = list as ArrayList<NewsEntity>

                    adapter.setTrendingData(trendingList)
                }
            })
        }
        checkInternet = MyApplication.checkInternet



        rvNews.adapter = adapter

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
    }

    private val lastVisibleItemPosition: Int
        get() = (rvNews!!.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()
}