package com.fafadiatech.newscout.activity

import android.content.*
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.adapter.ExpandListAdapter
import com.fafadiatech.newscout.adapter.MainAdapter
import com.fafadiatech.newscout.adapter.TopHeadingMenuAdapter
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.*
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.broadcast.DataNotFoundReceiver
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.fragment.DailyDigestFragment
import com.fafadiatech.newscout.fragment.NewsFragment
import com.fafadiatech.newscout.fragment.RootTrendingFragment
import com.fafadiatech.newscout.interfaces.MenuHeaderClickListener
import com.fafadiatech.newscout.model.*
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity(), MenuHeaderClickListener, NavigationView.OnNavigationItemSelectedListener {
    lateinit var vPager: ViewPager
    lateinit var adapterObj: MainAdapter
    lateinit var dataNotFoundReceiver: BroadcastReceiver
    lateinit var gson: Gson
    lateinit var fetchDataViewModel: FetchDataApiViewModel
    lateinit var apiInterfaceObj: ApiInterface
    var token: String = ""
    lateinit var recyclerViewTopHeading: RecyclerView
    lateinit var recyclerTopHeadingAdapter: TopHeadingMenuAdapter
    lateinit var clickListener: MenuHeaderClickListener
    var headingIdInitial: Int = 0
    var subHeadingId: Int = 0
    var subHeadList = ArrayList<String>()
    lateinit var toolbar: Toolbar
    var isNightMode: Boolean = false
    lateinit var fontSize: String
    var deviceWidthDp: Float = 0f
    lateinit var articleNewsDao: NewsDao
    var newsDatabase: NewsDatabase? = null
    lateinit var tabLayout: TabLayout
    var headingPosition: Int = 0
    val TAG: String = "TestMainActivity"
    var isFirstLoad = true
    lateinit var expandableListView: ExpandableListView
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationView: NavigationView
    var headerList = ArrayList<TopMenuModel>();
    var childList = HashMap<TopMenuModel, List<MenuModel>?>()
    var topMenuResult = ArrayList<MenuHeading>()
    lateinit var expandableListAdapter: ExpandListAdapter
    lateinit var menuHeadinglayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isNightMode = themePreference.getBoolean("night mode enable", false)
        var isThemeChange = themePreference.getBoolean("theme changed", false)
        token = themePreference.getString("token value", "")
        fetchDataViewModel = ViewModelProviders.of(this@MainActivity).get(FetchDataApiViewModel::class.java)
        apiInterfaceObj = ApiClient.getClient().create(ApiInterface::class.java)

        setContentView(R.layout.activity_main_page)
        newsDatabase = NewsDatabase.getInstance(this)
        articleNewsDao = newsDatabase!!.newsDao()
        val displayMetrics = DisplayMetrics()
        val windowmanager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        clickListener = this as MenuHeaderClickListener
        recyclerViewTopHeading = findViewById(R.id.rv_top_heading)
        toolbar = findViewById(R.id.toolbar_home_sc)
        drawer_layout = findViewById(R.id.drawer_layout)
        gson = Gson()
        adapterObj = MainAdapter(this, supportFragmentManager)

        if (token != null && token != "") {

            var deviceId = themePreference.getString("device_token", "")
            var call: Call<DeviceServerResponseData> = apiInterfaceObj.sendDeviceIdToServerWithToken(token, deviceId, "android")
            call.enqueue(object : Callback<DeviceServerResponseData> {
                override fun onFailure(call: Call<DeviceServerResponseData>, t: Throwable) {
                }

                override fun onResponse(call: Call<DeviceServerResponseData>,
                                        response: Response<DeviceServerResponseData>) {

                    if (response.code() == 200) {
                        var message = response.body()!!.body.Msg
                    }
                }
            })
        } else {
            var deviceId = themePreference.getString("device_token", "")
            var call: Call<DeviceServerResponseData> = apiInterfaceObj.sendDeviceIdToServerWithoutToken(deviceId, "android")
            call.enqueue(object : Callback<DeviceServerResponseData> {
                override fun onFailure(call: Call<DeviceServerResponseData>, t: Throwable) {
                }

                override fun onResponse(call: Call<DeviceServerResponseData>,
                                        response: Response<DeviceServerResponseData>) {

                    if (response.code() == 200) {

                    }
                }
            })
        }

        val uniqueVal = getUniqueCode(this@MainActivity, themePreference)
        var deviceId = themePreference.getString("device_token", "")
        //var tCall: Call<Void> = apiInterfaceObj.trackApp(0, "", 0, "", "", ActionType.APPOPEN.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type,getUniqueCode(this@MainActivity, themePreference))
        //trackingCallback(tCall)
        val sessionId = getUniqueCode(this@MainActivity, themePreference)
        trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.APPOPEN.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        menuHeadinglayoutManager =
                LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
        recyclerTopHeadingAdapter = TopHeadingMenuAdapter(this@MainActivity, clickListener)
        recyclerViewTopHeading.layoutManager = menuHeadinglayoutManager
        recyclerViewTopHeading.adapter = recyclerTopHeadingAdapter

        if (savedInstanceState != null) {
            recyclerViewTopHeading.scrollToPosition(savedInstanceState.getInt("item_position"))
        } else {
            recyclerTopHeadingAdapter.selectedItem = 0
        }

        loadDataInMenu(savedInstanceState)

        var toolbarText = findViewById<TextView>(R.id.toolbar_title_main)
        if (isThemeChange) {
            isThemeChange = false
            var editor = themePreference.edit()
            editor.putBoolean("theme changed", isThemeChange)
            editor.apply()
        } else {
            if (!MyApplication.apiCallArticles) {
                MyApplication.apiCallArticles = true
            }
        }

        tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.visibility = View.GONE

        setSupportActionBar(toolbar)

        val actionBar = supportActionBar

        actionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        actionBar?.setDisplayHomeAsUpEnabled(true);

        vPager = findViewById(R.id.vPager_main)
        vPager.adapter = adapterObj

        tabLayout.setupWithViewPager(vPager)

        expandableListView = findViewById(R.id.expandableListView)

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        vPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {

                if (tabLayout.getTabAt(position) != null) {
                    val tabview = (tabLayout.getTabAt(position)?.customView) as ViewGroup

                    var subHeadName = (tabview.getChildAt(0) as TextView).text.toString()
                    if (subHeadName.isNullOrBlank()) {
                        subHeadName = "Banking"
                    }
                    var itemId: Int = 0
                    try {
                        itemId = MyApplication.categoryIdHashMap.get(subHeadName)!!
                    } catch (e: Exception) {

                    }

                    //var deviceId = themePreference.getString("device_token", "")
                    //trackUserSelection(apiInterfaceObj, "sub_menu_click", deviceId, "android", itemId, subHeadName)
                    var deviceId = themePreference.getString("device_token", "")
                    //var tCall: Call<Void> = apiInterfaceObj.trackApp(0, "", itemId, subHeadName, "", ActionType.MENUCHANGE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type,getUniqueCode(this@MainActivity, themePreference))
                    //trackingCallback(tCall)
                    val sessionId = getUniqueCode(this@MainActivity, themePreference)
                    trackingCallback(apiInterfaceObj, themePreference, 0, "", itemId, subHeadName, "", ActionType.MENUCHANGE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId,"",0)
                }
            }
        })

        dataNotFoundReceiver = DataNotFoundReceiver()
    }

    fun setIconsTab(tabLayout: TabLayout) {

        for (i in 0 until tabLayout.getTabCount()) {
            val tab1 = tabLayout.getTabAt(i)
            val tabView = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tabView.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(16, 8, 16, 8)
            tabView.requestLayout()
            tab1?.setCustomView(adapterObj.getTabView(i))
        }
    }

    override fun onPause() {

        super.onPause()
        unregisterReceiver(dataNotFoundReceiver)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(dataNotFoundReceiver, IntentFilter("com.ft.newscout15.DataNotFoundAction"))
    }


    override fun onStop() {

        super.onStop()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        super.onNetworkConnectionChanged(isConnected)
        MyApplication.checkInternet = isConnected
        if (isConnected) {

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity, object : OnSuccessListener<InstanceIdResult> {
                override fun onSuccess(instanceIdResult: InstanceIdResult?) {
                    val deviceId = instanceIdResult?.getToken()
                    var editor = themePreference.edit()
                    editor.putString("device_token", deviceId)
                    editor.commit()
                }
            }).addOnFailureListener(this@MainActivity, object : OnFailureListener {

                override fun onFailure(e: Exception) {
                }
            })
        }
    }

    override fun onRestart() {
        super.onRestart()
    }


    override fun onClick(headingData: TopHeadingData) {

        loadNewsData(headingData)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        var menuNight = menu!!.findItem(R.id.switch_night_mode)

        var isNightMode = themePreference.getBoolean("night mode enable", false)
        if (isNightMode) {
            menuNight.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_moon_night))
            menuNight.title = "Day Mode"
        } else {
            menuNight.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_moon))
            menuNight.title = "Night Mode"
        }

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var deviceId = themePreference.getString("device_token", "")
        if (item?.getItemId() == android.R.id.home) {
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.BURGERICON.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId,"", 0)
            //burger menu click
            drawer_layout.openDrawer(Gravity.LEFT);
            return true
        }
        var defaultTheme = MODE_NIGHT_NO

        val sessionId = getUniqueCode(this@MainActivity, themePreference)
        trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.OPTIONMENU.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, ", 0")
        when (item!!.itemId) {
            R.id.ic_bookmark -> {

                val sessionId = getUniqueCode(this@MainActivity, themePreference)
                trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.BOOKMARKMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId,"", 0)
                val intent = Intent(this, BookmarkActivity::class.java)
                startActivity(intent)
            }

            R.id.ic_setting -> {

                val sessionId = getUniqueCode(this@MainActivity, themePreference)
                trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.SETTINGSMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, ", 0")
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }

            R.id.ic_search -> {

                val sessionId = getUniqueCode(this@MainActivity, themePreference)
                trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.SEARCHMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, ", 0")
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }

            R.id.switch_night_mode -> {

                var isNighModeEnabled: Boolean
                var isThemeChanged: Boolean
                var drawable = item.getIcon().constantState


                if (drawable == ContextCompat.getDrawable(this, R.drawable.ic_moon)?.constantState) {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_moon_night))
                    chooseNightTheme()
                    isNightMode = true
                    isNighModeEnabled = false
                    isThemeChanged = true
                    defaultTheme = MODE_NIGHT_YES
                    val sessionId = getUniqueCode(this@MainActivity, themePreference)
                    trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.MODECHANGE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                } else {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_moon))
                    chooseDefaultTheme()
                    isNightMode = false
                    isNighModeEnabled = true
                    isThemeChanged = true
                    defaultTheme =  MODE_NIGHT_NO
                    val sessionId = getUniqueCode(this@MainActivity, themePreference)
                    trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.MODECHANGE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                }
                var editor = themePreference.edit()
                editor.putInt("theme", themes)
                editor.putBoolean("night mode enable", isNighModeEnabled)
                editor.putBoolean("theme changed", isThemeChanged)
                editor.putBoolean("night mode enable", isNightMode)
                editor.putInt("night_mode", defaultTheme)
                editor.apply()

                recreate()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun chooseNightTheme() {
        fontSize = themePreference.getString("text_font_size", "medium")
        if (fontSize.equals("small")) {
            themes = R.style.DefaultSmall
        } else if (fontSize.equals("medium")) {
            themes = R.style.DefaultMedium
        } else if (fontSize.equals("large")) {
            themes = R.style.DefaultLarge
        }
    }

    fun chooseDefaultTheme() {
        fontSize = themePreference.getString("text_font_size", "medium")
        if (fontSize.equals("small")) {
            themes = R.style.DefaultSmall
        } else if (fontSize.equals("medium")) {
            themes = R.style.DefaultMedium
        } else if (fontSize.equals("large")) {
            themes = R.style.DefaultLarge
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putInt("item_position", headingPosition)
    }

    override fun onBackPressed() {

        drawer_layout = findViewById(R.id.drawer_layout)
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        val fm = supportFragmentManager
        if (onBackPress(fm)) {
            return;
        }

        super.onBackPressed();
    }

    fun onBackPress(fm: FragmentManager): Boolean {

        if (fm != null) {
            if (fm.getBackStackEntryCount() > 1) {
                fm.popBackStack();
                return true;
            }

            var fragList: List<Fragment> = fm.getFragments();
            if (fragList != null && fragList.size > 0) {
                for (frag in fragList) {

                    if (frag == null) {
                        continue
                    }
                    if (frag.isVisible) {

                        if (onBackPress(frag.childFragmentManager)) {
                            return true
                        }
                    }
                }
            }
        }
        return false;
    }

    fun prepareMenuData() {
        var subHeadList = ArrayList<String>()

        subHeadList.clear()
        headerList.clear()
        childList.clear()
        var headList = ArrayList<MenuHeading>()
        headList.clear()
        headList = articleNewsDao.getMenuHeadingListFromDb() as ArrayList<MenuHeading>
        topMenuResult = headList as ArrayList<MenuHeading>
        var trendingMenu = MenuHeading(TRENDING_ID, TRENDING_NAME)
        topMenuResult.add(0, trendingMenu)
        val latestId = getLatestNewsID(articleNewsDao)
        trendingMenu = MenuHeading(latestId, LATESTNEWS_NAME)
        topMenuResult.add(1, trendingMenu)
        trendingMenu = MenuHeading(DAILYDIGEST_ID, DAILYDIGEST_NAME)
        topMenuResult.add(2, trendingMenu)

        for (item: MenuHeading in topMenuResult) {
            var list: List<SubMenuResultData>
            val id = item.id
            var subHeadId: Int = 0
            lateinit var topMenuModel: TopMenuModel
            lateinit var menuModel: MenuModel

            if (item.name.equals(TRENDING_NAME)) {
                topMenuModel = TopMenuModel(item.name, false, false, item)
            } else if (item.name.equals(DAILYDIGEST_NAME)) {
                topMenuModel = TopMenuModel(item.name, false, false, item)
            } else {
                topMenuModel = TopMenuModel(item.name, true, true, item)
            }
            headerList.add(topMenuModel);
            var childModelsList = ArrayList<MenuModel>()
            try {
                list = articleNewsDao.getSubMenuDataListFromDb(id)
            } catch (e: Exception) {
                list = ArrayList<SubMenuResultData>()
            }

            var result = list as ArrayList<SubMenuResultData>
            menuModel = MenuModel("", false, false, null)
            for (i in 0 until result.size) {
                subHeadId = result.get(i).id
                var subHead = result.get(i).name
                if (!subHead.equals(LATESTNEWS_FIELDNAME)) {
                    subHeadList.add(subHead)
                }
                menuModel = MenuModel(subHead, false, false, result.get(i))
                childModelsList.add(menuModel)
            }
            if (item.name.equals(LATESTNEWS_NAME)) {
                subHeadId = getLatestNewsID(articleNewsDao)
                var subHead = LATESTNEWS_NAME
                subHeadList.add(subHead)
                menuModel = MenuModel(subHead, false, false, null)
                childModelsList.add(menuModel)
            }

            if (topMenuModel.hasChildren) {

                childList.put(topMenuModel, childModelsList);
            } else {
                childList.put(topMenuModel, null);
            }
        }

        populateExpandableList()
    }

    fun populateExpandableList() {
        expandableListAdapter = ExpandListAdapter(this, headerList, childList)
        expandableListView.setAdapter(expandableListAdapter)
        expandableListView.setOnGroupClickListener(object : ExpandableListView.OnGroupClickListener {
            override fun onGroupClick(parent: ExpandableListView?, view: View?, groupPosition: Int, childPosition: Long): Boolean {


                var data = topMenuResult.get(groupPosition)
                var headingData = TopHeadingData(data.id, groupPosition, data.name)

                var deviceId = themePreference.getString("device_token", "")
                val sessionId = getUniqueCode(this@MainActivity, themePreference)
                trackingCallback(apiInterfaceObj, themePreference, 0, "", data.id, data.name, "", ActionType.BURGERMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

                recyclerViewTopHeading.scrollToPosition(groupPosition)
                recyclerTopHeadingAdapter.selectedItem = groupPosition
                recyclerTopHeadingAdapter.setPosition(groupPosition)
                recyclerTopHeadingAdapter.notifyDataSetChanged()

                loadNewsData(headingData)
                return true
            }
        })
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {

        return false
    }

    fun scrollToCenter(position: Int) {
        val smoothScroller = CenterSmoothScroller(recyclerViewTopHeading.getContext());
        smoothScroller.setTargetPosition(position);
        menuHeadinglayoutManager.startSmoothScroll(smoothScroller);
    }

    fun loadDataInMenu(savedInstanceState: Bundle?) {

        fetchDataViewModel.getMenuHeadingFromDb().observe(this, object : androidx.lifecycle.Observer<List<MenuHeading>> {
            override fun onChanged(list: List<MenuHeading>?) {
                var result = list as ArrayList<MenuHeading>
                var trendingMenu = MenuHeading(TRENDING_ID, TRENDING_NAME)
                result.add(0, trendingMenu)
                val latestId = getLatestNewsID(articleNewsDao)
                trendingMenu = MenuHeading(latestId, LATESTNEWS_NAME)
                result.add(1, trendingMenu)
                trendingMenu = MenuHeading(DAILYDIGEST_ID, DAILYDIGEST_NAME)
                result.add(2, trendingMenu)

                recyclerTopHeadingAdapter.notifyChanges(result)


                if (isFirstLoad) {
                    if (recyclerTopHeadingAdapter.getItemCount() > 0) {
                        isFirstLoad = false
                        val newData = recyclerTopHeadingAdapter.getObject(recyclerTopHeadingAdapter.selectedItem)
                        headingIdInitial = newData.id
                        val headingName = newData.name
                        val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                        loadNewsData(headingData)
                    }
                }
                prepareMenuData()
            }
        })

        if (recyclerTopHeadingAdapter.selectedItem > 0) {

            if (recyclerTopHeadingAdapter.getItemCount() > 0) {
                val newData = recyclerTopHeadingAdapter.getObject(recyclerTopHeadingAdapter.selectedItem)
                headingIdInitial = newData.id
                val headingName = newData.name
                val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                loadNewsData(headingData)
            }
        } else {
            if (recyclerTopHeadingAdapter.getItemCount() > 0) {
                val newData = recyclerTopHeadingAdapter.getObject(recyclerTopHeadingAdapter.selectedItem)
                headingIdInitial = newData.id
                val headingName = newData.name
                val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                loadNewsData(headingData)
            }
        }
    }

    fun loadNewsData(headingData: TopHeadingData) {
        var headingPos: Int = 0
        val pos = recyclerTopHeadingAdapter.getPosition()
        if (headingData.category.equals(TRENDING_NAME)) {
            tabLayout.visibility = View.GONE
            headingData.id = TRENDING_ID
            var fm = supportFragmentManager
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            val list = fm.fragments
            for (i in 0 until list.size) {
                var frag = list.get(i)
                val cm = frag.childFragmentManager
            }
            var bundle = Bundle()
            bundle.putString("category_name", TRENDING_NAME)
            bundle.putInt("position", 0)
            bundle.putInt("category_id", TRENDING_ID)

            val rootFrag = RootTrendingFragment()
            adapterObj.removeFragment()
            adapterObj.addFragment(0, rootFrag, bundle)
            adapterObj.notifyDataSetChanged()

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(apiInterfaceObj, themePreference, 0, "", TRENDING_ID, TRENDING_NAME, "", ActionType.TRENDINGMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

        } else if (headingData.category.equals(LATESTNEWS_NAME)) {
            tabLayout.visibility = View.GONE
            var newsCategoryId: Int = getLatestNewsID(articleNewsDao)
            var bundle = Bundle()
            bundle.putString("category_name", LATESTNEWS_NAME)
            bundle.putInt("position", 0)
            bundle.putInt("category_id", newsCategoryId)

            val newsFrag = NewsFragment()
            adapterObj.removeFragment()
            adapterObj.addFragment(0, newsFrag, bundle)
            adapterObj.notifyDataSetChanged()

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(apiInterfaceObj, themePreference, 0, "", newsCategoryId, LATESTNEWS_NAME, "", ActionType.MENUCHANGE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId,"", 0)

        } else if (headingData.category.equals(DAILYDIGEST_NAME)) {
            tabLayout.visibility = View.GONE
            var bundle = Bundle()
            bundle.putString("category_name", DAILYDIGEST_NAME)
            bundle.putInt("position", 0)
            bundle.putInt("category_id", DAILYDIGEST_ID)

            val newsFrag = DailyDigestFragment()
            adapterObj.removeFragment()
            adapterObj.addFragment(0, newsFrag, bundle)
            adapterObj.notifyDataSetChanged()

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(apiInterfaceObj, themePreference, 0, "", DAILYDIGEST_ID, DAILYDIGEST_NAME, "", ActionType.DAILYDIGESTMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

        } else {
            tabLayout.visibility = View.VISIBLE
            adapterObj.removeFragment()
            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(apiInterfaceObj, themePreference, 0, "", headingData.id,headingData.category, "", ActionType.PARENTCATEGORYCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

            fetchDataViewModel.getSubMenuDataFromDb(headingData.id).observe(this, object : androidx.lifecycle.Observer<List<SubMenuResultData>> {
                override fun onChanged(list: List<SubMenuResultData>?) {
                    var result = list as ArrayList<SubMenuResultData>
                    var subMenuId:Int = 0
                    var subMenuName:String = ""
                    if (result.size > 0) {
                        adapterObj.removeFragment()

                        if (!subMenuName.equals(LATESTNEWS_FIELDNAME)) {
                            subMenuId =  result.get(0).id
                            subMenuName =  result.get(0).name
                        }else{
                            subMenuId =  result.get(1).id
                            subMenuName =  result.get(1).name
                        }
                    }

                    for (i in 0 until result.size) {
                        val name = result.get(i).name
                        if (!name.equals(LATESTNEWS_FIELDNAME)) {

                            var bundle = Bundle()
                            bundle.putString("category_name", result.get(i).name)
                            bundle.putInt("position", i)
                            bundle.putInt("category_id", result.get(i).id)

                            val newsFrag = NewsFragment()

                            adapterObj.addFragment(i, newsFrag, bundle)
                        }
                    }
                    adapterObj.notifyDataSetChanged()
                    setIconsTab(tabLayout)
                    vPager.setCurrentItem(0)
                    trackingCallback(apiInterfaceObj, themePreference, 0, "", subMenuId, subMenuName, "", ActionType.MENUCHANGE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                }
            })
        }
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        scrollToCenter(pos)
    }



    override fun onDestroy() {
        var deviceId = themePreference.getString("device_token", "")
        val sessionId = getUniqueCode(this@MainActivity, themePreference)
        trackingCallback(apiInterfaceObj, themePreference, 0, "", 0, "", "", ActionType.APPCLOSE.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        super.onDestroy()
    }
}