package com.fafadiatech.newscout.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.crashlytics.android.answers.Answers
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.adapter.DetailNewsAdapter
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.customcomponent.VerticalViewPager
import com.fafadiatech.newscout.db.NewsEntity
import com.fafadiatech.newscout.interfaces.ITopNews
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import io.fabric.sdk.android.Fabric

class DetailNewsActivity : BaseActivity(), VerticalViewPager.SwiperListener, ITopNews {

    var daList = ArrayList<DetailNewsData>()
    var currentItem: Int? = null
    var strCategory: String? = null
    lateinit var vPagerDetail: VerticalViewPager
    lateinit var detailNewsAdpt: DetailNewsAdapter
    var index: Int = 0
    lateinit var dataVM: FetchDataApiViewModel
    var categoryId: Int = 0
    var token: String = ""
    lateinit var topLayout : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Answers())
        dataVM = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)
        token = themePreference.getString("token value", "")
        setContentView(R.layout.activity_news_detail)

        if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            val displayMetrics = DisplayMetrics()
            val windowmanager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowmanager.defaultDisplay.getMetrics(displayMetrics)
            val deviceWidth = displayMetrics.widthPixels
            var deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
            if (deviceWidthDp < 600) {
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
        }
        detailNewsAdpt = DetailNewsAdapter(this)
        vPagerDetail = findViewById(R.id.vPager_detail_screen)
        vPagerDetail.adapter = detailNewsAdpt
        index = intent.getIntExtra("indexPosition", 0)
        strCategory = intent.getStringExtra("category_of_newslist")
        categoryId = intent.getIntExtra("category_id", 0)
        //fabReturnTop = findViewById(R.id.fab_return_top)
        topLayout = findViewById(R.id.top_layout_detailSc)

        if (strCategory == "") {
            dataVM.getDetailNewsFromDb().observe(this, object : androidx.lifecycle.Observer<List<DetailNewsData>> {
                override fun onChanged(list: List<DetailNewsData>?) {
                    daList = list as ArrayList<DetailNewsData>
                    detailNewsAdpt.setData(daList)
                    detailNewsAdpt.notifyDataSetChanged()
                    vPagerDetail.currentItem = index
                }
            })
        } else if (strCategory == "Bookmark") {
            dataVM.getBookmarkNewsFromDb().observe(this, object : androidx.lifecycle.Observer<List<DetailNewsData>> {
                override fun onChanged(list: List<DetailNewsData>?) {
                    daList = list as ArrayList<DetailNewsData>
                    detailNewsAdpt.setData(daList)
                    detailNewsAdpt.notifyDataSetChanged()
                    if (index == daList.size) {
                        index = index - 1
                        vPagerDetail.currentItem = index
                    } else {
                        vPagerDetail.currentItem = index
                    }
                }
            })
        } else if (strCategory == "Search") {

            var list = dataVM.getDetailSearchNewsFromDb() as ArrayList<DetailNewsData>
            detailNewsAdpt.setData(list)
            detailNewsAdpt.notifyDataSetChanged()
            vPagerDetail.currentItem = index
        } else if (strCategory == "Source") {
            var list = intent.getParcelableArrayListExtra<NewsEntity>("source_list") as ArrayList<NewsEntity>
            var detailList = ArrayList<DetailNewsData>()

            for (i in 0 until list.size) {
                var sourceItem = list.get(i)
                var detailListItem = DetailNewsData(sourceItem.id, sourceItem.title, sourceItem.source, sourceItem.category, sourceItem.source_url, sourceItem.cover_image, sourceItem.blurb!!, sourceItem.published_on, 2, 0, sourceItem.article_score)
                detailList.add(detailListItem)
            }


            detailNewsAdpt.setData(detailList)
            detailNewsAdpt.notifyDataSetChanged()
            vPagerDetail.currentItem = index
        } else if (strCategory == "Trending") {
            var clusterId = intent.getIntExtra("cluster_id", 0)

            dataVM.getTrendingDetailByClusterIdFromDb(clusterId).observe(this, object : androidx.lifecycle.Observer<List<DetailNewsData>> {
                override fun onChanged(list: List<DetailNewsData>?) {
                    var trendingList = list as ArrayList<DetailNewsData>
                    detailNewsAdpt.setData(trendingList)
                    detailNewsAdpt.notifyDataSetChanged()
                    vPagerDetail.currentItem = index
                }
            })
        } else if (strCategory == "DailyDigest") {
            dataVM.dailyDigestDetailNews().observe(this, object : androidx.lifecycle.Observer<List<DetailNewsData>> {
                override fun onChanged(list: List<DetailNewsData>?) {
                    daList = list as ArrayList<DetailNewsData>
                    detailNewsAdpt.setData(daList)
                    detailNewsAdpt.notifyDataSetChanged()
                    vPagerDetail.currentItem = index
                }
            })
        } else {

            if (token != "") {
                var list = dataVM.getDetailNewsFromDb(categoryId)

                daList = list as ArrayList<DetailNewsData>
            } else {
                var list = dataVM.getDefaultDetailNewsFromDb(categoryId)

                daList = list as ArrayList<DetailNewsData>
            }
            detailNewsAdpt.setData(daList)
            detailNewsAdpt.notifyDataSetChanged()
            vPagerDetail.currentItem = index
        }

        detailNewsAdpt.setCategory(strCategory!!)

        vPagerDetail.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {

                if (position == daList.size - 1) {
                    Toast.makeText(this@DetailNewsActivity, "You have reached at last news", Toast.LENGTH_SHORT).show()
                }

                if (position == 0) {
                    Toast.makeText(this@DetailNewsActivity, "You have reached at top news", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        currentItem = vPagerDetail.currentItem
        index = currentItem!!

        var diff = vPagerDetail.upX1 - vPagerDetail.upX2
        if (diff > 0) {
            onLeftSwipe()
        }


        return super.onTouchEvent(event)
    }

    override fun onLeftSwipe() {
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        index = intent.getIntExtra("indexPosition", 0)
        daList = intent.getParcelableArrayListExtra<DetailNewsData>("arrayList") ?: arrayListOf()
        if (daList != null) {
            detailNewsAdpt.setData(daList)
            detailNewsAdpt.notifyDataSetChanged()
            detailNewsAdpt.setCategory(strCategory!!)
        } else {
            daList = ArrayList<DetailNewsData>()
            detailNewsAdpt.setData(daList)
            detailNewsAdpt.notifyDataSetChanged()
            detailNewsAdpt.setCategory(strCategory!!)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        vPagerDetail.currentItem = index
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        super.onNetworkConnectionChanged(isConnected)
        MyApplication.checkInternet = isConnected
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstant.REQUEST_FOR_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                index = data!!.getIntExtra("news_item_position", 0)
                vPagerDetail.currentItem = index
            }
        }
    }

    override fun gotoFirstNews() {
        vPagerDetail.setCurrentItem(0, false)
        detailNewsAdpt.notifyDataSetChanged()
    }
}