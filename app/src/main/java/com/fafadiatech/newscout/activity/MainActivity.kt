package com.fafadiatech.newscout.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.viewpager.widget.ViewPager
import com.fafadiatech.newscout.BuildConfig
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
import kotlinx.android.synthetic.main.app_bar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity(), MenuHeaderClickListener, NavigationView.OnNavigationItemSelectedListener {
    lateinit var vPager: ViewPager
    lateinit var vpAdpt: MainAdapter
    lateinit var dataNotFoundReceiver: BroadcastReceiver
    lateinit var gson: Gson
    lateinit var dataVM: FetchDataApiViewModel
    lateinit var nApi: ApiInterface
    var token: String = ""
    lateinit var rvMenu: RecyclerView
    lateinit var menuAdpt: TopHeadingMenuAdapter
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
    lateinit var mDrawer: DrawerLayout
    lateinit var navigationView: NavigationView
    var headerList = ArrayList<TopMenuModel>();
    var childList = HashMap<TopMenuModel, List<MenuModel>?>()
    var topMenuResult = ArrayList<MenuHeading>()
    lateinit var expandableListAdapter: ExpandListAdapter
    lateinit var menuHeadinglayoutManager: LinearLayoutManager
    var arrFragment = ArrayList<Fragment>()
    var arrDataBundle = ArrayList<Bundle>()
    var pagePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isNightMode = themePreference.getBoolean("night mode enable", false)
        var isThemeChange = themePreference.getBoolean("theme changed", false)
        token = themePreference.getString("token value", "")
        dataVM = ViewModelProviders.of(this@MainActivity).get(FetchDataApiViewModel::class.java)
        nApi = ApiClient.getClient().create(ApiInterface::class.java)

        setContentView(R.layout.activity_main_page)
        newsDatabase = NewsDatabase.getInstance(this)
        articleNewsDao = newsDatabase!!.newsDao()
        val displayMetrics = DisplayMetrics()
        val windowmanager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        deviceWidthDp = deviceWidth / Resources.getSystem().getDisplayMetrics().density
        clickListener = this as MenuHeaderClickListener
        rvMenu = findViewById(R.id.rv_top_heading)
        toolbar = findViewById(R.id.toolbar_home_sc)
        mDrawer = findViewById(R.id.drawer_layout)
        gson = Gson()
        vpAdpt = MainAdapter(this, supportFragmentManager, arrFragment, arrDataBundle)
        val query = SimpleSQLiteQuery("DELETE from sqlite_sequence")
        val value = articleNewsDao.deleteSequenceTable(query);
        if (token != null && token != "") {

            var deviceId = themePreference.getString("device_token", "")
            var call: Call<DeviceServerResponseData> = nApi.sendDeviceIdToServerWithToken(token, deviceId, "android")
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
            var call: Call<DeviceServerResponseData> = nApi.sendDeviceIdToServerWithoutToken(deviceId, "android")
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
        val sessionId = getUniqueCode(this@MainActivity, themePreference)
        trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.APPOPEN.type, deviceId
                ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        menuHeadinglayoutManager =
                LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
        menuAdpt = TopHeadingMenuAdapter(this@MainActivity, clickListener)
        rvMenu.layoutManager = menuHeadinglayoutManager
        rvMenu.adapter = menuAdpt

        if (savedInstanceState != null) {
            rvMenu.scrollToPosition(savedInstanceState.getInt("item_position"))
        } else {
            menuAdpt.selectedItem = 0
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
        vPager.adapter = vpAdpt

        tabLayout.setupWithViewPager(vPager)

        expandableListView = findViewById<ExpandableListView>(R.id.expandableListView)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        /*
        vPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var lastPageChange = false
            override fun onPageScrollStateChanged(state: Int) {
                val lastIdx = vpAdpt.getCount() - 1;
                val curItem = vPager.getCurrentItem();
                val firstIdx = 0
                if (curItem == lastIdx && state == 1) {
                    lastPageChange = true;
                    if (menuAdpt.selectedItem < menuAdpt.itemCount) {

                        /*menuAdpt.setPosition(menuAdpt.selectedItem+1)
                        val newData = menuAdpt.getObject(menuAdpt.selectedItem+1)
                        var headingData = TopHeadingData(newData.id, menuAdpt.selectedItem, newData.name)

                        swipeNewsData(headingData)*/

                        //val pos = menuAdpt.selectedItem+1
                        //menuAdpt.changeMenu(pos)
                        //onClick(headingData)
                        //selectedItem = position

                        /*val newData = menuAdpt.getObject(4)
                        headingIdInitial = newData.id
                        val headingName = newData.name
                        val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                        onClick(headingData)*/
                    }
                } else if (curItem == firstIdx && state == 1) {
                    lastPageChange = false

                    if (menuAdpt.selectedItem > 0) {

                        val newData = menuAdpt.getObject(3)
                        headingIdInitial = newData.id
                        val headingName = newData.name
                        val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                        onClick(headingData)
                    }
                } else {
                    lastPageChange = false
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val lastIdx = vpAdpt.getCount() - 1
                if (lastPageChange && position == lastIdx) {
                }
            }

            override fun onPageSelected(position: Int) {
                pagePosition = position
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

                    var deviceId = themePreference.getString("device_token", "")

                    val sessionId = getUniqueCode(this@MainActivity, themePreference)
                    trackingCallback(nApi, themePreference, 0, "", itemId, subHeadName, "", ActionType.MENUCHANGE.type, deviceId
                            ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                }
            }
        })
        */
        dataNotFoundReceiver = DataNotFoundReceiver()
    }

    fun setIconsTab(tabLayout: TabLayout) {

        for (i in 0 until tabLayout.getTabCount()) {
            val cTab = tabLayout.getTabAt(i)
            val tabView = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tabView.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(8, 8, 8, 8)
            cTab?.setCustomView(vpAdpt.getTabView(i))
            tabView.requestLayout()
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
            trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.BURGERICON.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

            mDrawer.openDrawer(Gravity.LEFT);
            return true
        } else {
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.OPTIONMENU.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, ", 0")
        }

        var defaultTheme = MODE_NIGHT_NO

        when (item!!.itemId) {
            R.id.ic_bookmark -> {

                val sessionId = getUniqueCode(this@MainActivity, themePreference)
                trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.BOOKMARKMENUCLICK.type, deviceId
                        ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                val intent = Intent(this, BookmarkActivity::class.java)
                startActivity(intent)
            }

            R.id.ic_setting -> {

                val sessionId = getUniqueCode(this@MainActivity, themePreference)
                trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.SETTINGSMENUCLICK.type, deviceId
                        ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, ", 0")
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }

            R.id.ic_search -> {

                val sessionId = getUniqueCode(this@MainActivity, themePreference)
                trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.SEARCHMENUCLICK.type, deviceId
                        ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, ", 0")
                val intent = Intent(this, TestSearchActivity::class.java)
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
                    trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.MODECHANGE.type, deviceId
                            ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                } else {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_moon))
                    chooseDefaultTheme()
                    isNightMode = false
                    isNighModeEnabled = true
                    isThemeChanged = true
                    defaultTheme = MODE_NIGHT_NO
                    val sessionId = getUniqueCode(this@MainActivity, themePreference)
                    trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.MODECHANGE.type, deviceId
                            ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
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

        mDrawer = findViewById(R.id.drawer_layout)
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START)
            return
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

        if (topMenuResult.size > 1) {
            var trendingMenu = MenuHeading(TRENDING_ID, TRENDING_NAME)
            topMenuResult.add(0, trendingMenu)
            val latestId = getLatestNewsID(articleNewsDao)
            trendingMenu = MenuHeading(latestId, LATESTNEWS_NAME)
            topMenuResult.add(1, trendingMenu)
            trendingMenu = MenuHeading(DAILYDIGEST_ID, DAILYDIGEST_NAME)
            topMenuResult.add(2, trendingMenu)
        }

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
            if (topMenuResult.size <= 1) {

                if (BuildConfig.showTrendingNews) {
                    val trendingMenu = SubMenuResultData(TRENDING_ID, TRENDING_ID, TRENDING_NAME)
                    result.add(0, trendingMenu)
                }
                if (BuildConfig.showLatestNews) {
                    val latestId = getLatestNewsID(articleNewsDao)
                    val trendingMenu = SubMenuResultData(latestId, latestId, LATESTNEWS_NAME)
                    if (BuildConfig.showTrendingNews) {
                        result.add(1, trendingMenu)
                    } else {
                        result.add(0, trendingMenu)
                    }
                }

                if (BuildConfig.showDailyDigest) {
                    val trendingMenu = SubMenuResultData(DAILYDIGEST_ID, DAILYDIGEST_ID, DAILYDIGEST_NAME)

                    if (BuildConfig.showTrendingNews && BuildConfig.showLatestNews) {
                        result.add(2, trendingMenu)
                    } else if (BuildConfig.showTrendingNews && !BuildConfig.showLatestNews) {
                        result.add(1, trendingMenu)
                    } else if (!BuildConfig.showTrendingNews && BuildConfig.showLatestNews) {
                        result.add(1, trendingMenu)
                    } else {
                        result.add(0, trendingMenu)
                    }
                }
            } else {

            }
            for (i in 0 until result.size) {
                subHeadId = result.get(i).id
                var subHead = result.get(i).name
                if (!subHead.equals(LATESTNEWS_FIELDNAME) && !subHead.equals(LATESTNEWS_FIELDNAME2)) {
                    subHeadList.add(subHead)
                    menuModel = MenuModel(subHead, false, false, result.get(i))
                    childModelsList.add(menuModel)
                }
            }
            if (item.name.equals(LATESTNEWS_NAME)) {
                subHeadId = getLatestNewsID(articleNewsDao)
                var subHead = LATESTNEWS_NAME
                subHeadList.add(subHead)
                menuModel = MenuModel(subHead, false, false, null)
                childModelsList.add(menuModel)
            }

            if (topMenuModel.hasChildren) {

                if (topMenuResult.size <= 1) {
                    headerList.clear()
                    for (child in childModelsList) {
                        var trendingMenu = MenuHeading(child.subMenuData!!.id, child.subMenuData!!.name)
                        topMenuResult.add(0, trendingMenu)
                        val topMenuModel = TopMenuModel(child.menuName, false, false, trendingMenu)
                        headerList.add(topMenuModel)
                    }
                    childList.put(topMenuModel, childModelsList);
                } else {
                    childList.put(topMenuModel, childModelsList);
                }
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
                trackingCallback(nApi, themePreference, 0, "", data.id, data.name, "", ActionType.BURGERMENUCLICK.type, deviceId
                        ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                if (!(rv_top_heading.isVisible)) {

                    vPager.setCurrentItem(groupPosition)
                    mDrawer = findViewById(R.id.drawer_layout)
                    if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                        mDrawer.closeDrawer(GravityCompat.START)
                    }
                    return true
                } else {
                    rvMenu.scrollToPosition(groupPosition)
                    menuAdpt.selectedItem = groupPosition
                    menuAdpt.setPosition(groupPosition)
                    menuAdpt.notifyDataSetChanged()

                    loadNewsData(headingData)
                    return true
                }
                return false
            }
        })
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {

        return false
    }

    fun scrollToCenter(position: Int) {
        val smoothScroller = CenterSmoothScroller(rvMenu.getContext());
        smoothScroller.setTargetPosition(position);
        menuHeadinglayoutManager.startSmoothScroll(smoothScroller);
    }

    fun loadDataInMenu(savedInstanceState: Bundle?) {

        dataVM.getMenuHeadingFromDb().observe(this, object : androidx.lifecycle.Observer<List<MenuHeading>> {
            override fun onChanged(list: List<MenuHeading>?) {
                var result = list as ArrayList<MenuHeading>
                if (result.size > 1) {
                    var trendingMenu = MenuHeading(TRENDING_ID, TRENDING_NAME)
                    result.add(0, trendingMenu)
                    val latestId = getLatestNewsID(articleNewsDao)
                    trendingMenu = MenuHeading(latestId, LATESTNEWS_NAME)
                    result.add(1, trendingMenu)
                    trendingMenu = MenuHeading(DAILYDIGEST_ID, DAILYDIGEST_NAME)
                    result.add(2, trendingMenu)
                }
                menuAdpt.notifyChanges(result)


                if (isFirstLoad) {
                    if (menuAdpt.getItemCount() > 0) {
                        isFirstLoad = false
                        val newData = menuAdpt.getObject(menuAdpt.selectedItem)
                        headingIdInitial = newData.id
                        val headingName = newData.name
                        val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                        loadNewsData(headingData)
                    }
                }
                prepareMenuData()
            }
        })

        if (menuAdpt.selectedItem > 0) {

            if (menuAdpt.getItemCount() > 0) {
                val newData = menuAdpt.getObject(menuAdpt.selectedItem)
                headingIdInitial = newData.id
                val headingName = newData.name
                val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                loadNewsData(headingData)
            }
        } else {
            if (menuAdpt.getItemCount() > 0) {
                val newData = menuAdpt.getObject(menuAdpt.selectedItem)
                headingIdInitial = newData.id
                val headingName = newData.name
                val headingData = TopHeadingData(headingIdInitial, 0, headingName)
                loadNewsData(headingData)
            }
        }
    }

    fun loadNewsData(headingData: TopHeadingData) {
        var headingPos: Int = 0
        val pos = menuAdpt.getPosition()
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
            vpAdpt.removeFragment()

            arrFragment.add(rootFrag)
            arrDataBundle.add(bundle)
            vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)
            vPager.adapter = vpAdpt
            vpAdpt.notifyDataSetChanged()
            setIconsTab(tabLayout)
            vPager.setCurrentItem(0)

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", TRENDING_ID, TRENDING_NAME, "", ActionType.TRENDINGMENUCLICK.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        } else if (headingData.category.equals(LATESTNEWS_NAME)) {
            tabLayout.visibility = View.GONE
            var newsCategoryId: Int = getLatestNewsID(articleNewsDao)
            var newsCategoryName: String = getLatestNewsName(articleNewsDao)
            var bundle = Bundle()
            bundle.putString("category_name", newsCategoryName)
            bundle.putInt("position", 0)
            bundle.putInt("category_id", newsCategoryId)

            val newsFrag = NewsFragment()
            vpAdpt.removeFragment()
            arrFragment.add(newsFrag)
            arrDataBundle.add(bundle)

            vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)

            vPager.adapter = vpAdpt
            vpAdpt.notifyDataSetChanged()
            setIconsTab(tabLayout)
            vPager.setCurrentItem(0)

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", newsCategoryId, LATESTNEWS_NAME, "", ActionType.MENUCHANGE.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        } else if (headingData.category.equals(DAILYDIGEST_NAME)) {
            tabLayout.visibility = View.GONE
            var bundle = Bundle()
            bundle.putString("category_name", DAILYDIGEST_NAME)
            bundle.putInt("position", 0)
            bundle.putInt("category_id", DAILYDIGEST_ID)

            val newsFrag = DailyDigestFragment()
            vpAdpt.removeFragment()

            arrFragment.add(newsFrag)
            arrDataBundle.add(bundle)

            vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)

            vPager.adapter = vpAdpt
            vpAdpt.notifyDataSetChanged()
            setIconsTab(tabLayout)
            vPager.setCurrentItem(0)

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", DAILYDIGEST_ID, DAILYDIGEST_NAME, "", ActionType.DAILYDIGESTMENUCLICK.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        } else {
            if (menuAdpt.itemCount <= 1) {
                rvMenu.visibility = View.GONE
            } else {
                rvMenu.visibility = View.VISIBLE
            }

            tabLayout.visibility = View.VISIBLE
            vpAdpt.removeFragment()
            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", headingData.id, headingData.category, "", ActionType.PARENTCATEGORYCLICK.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

            dataVM.getSubMenuDataFromDb(headingData.id).observe(this, object : androidx.lifecycle.Observer<List<SubMenuResultData>> {
                override fun onChanged(list: List<SubMenuResultData>?) {
                    var result = list as ArrayList<SubMenuResultData>
                    if (menuAdpt.itemCount <= 1) {

                        if (BuildConfig.showTrendingNews) {
                            val trendingSubMenu = SubMenuResultData(TRENDING_ID, TRENDING_ID, TRENDING_NAME)
                            result.add(0, trendingSubMenu)
                        }
                        if (BuildConfig.showLatestNews) {
                            val latestSubMenu = SubMenuResultData(LATESTNEWS_ID, LATESTNEWS_ID, LATESTNEWS_NAME)
                            if (BuildConfig.showTrendingNews) {

                                result.add(1, latestSubMenu)
                            } else {
                                result.add(0, latestSubMenu)
                            }
                        }

                        if (BuildConfig.showDailyDigest) {
                            val dailyDigestSubMenu = SubMenuResultData(DAILYDIGEST_ID, DAILYDIGEST_ID, DAILYDIGEST_NAME)

                            if (BuildConfig.showTrendingNews && BuildConfig.showLatestNews) {
                                result.add(2, dailyDigestSubMenu)
                            } else if (BuildConfig.showTrendingNews && !BuildConfig.showLatestNews) {
                                result.add(1, dailyDigestSubMenu)
                            } else if (!BuildConfig.showTrendingNews && BuildConfig.showLatestNews) {
                                result.add(1, dailyDigestSubMenu)
                            } else {
                                result.add(0, dailyDigestSubMenu)
                            }
                        }
                    }
                    var subMenuId: Int = 0
                    var subMenuName: String = ""
                    if (result.size > 0) {
                        vpAdpt.removeFragment()

                        if (!subMenuName.equals(LATESTNEWS_FIELDNAME) && !subMenuName.equals(LATESTNEWS_FIELDNAME2)) {
                            subMenuId = result.get(0).id
                            subMenuName = result.get(0).name
                        } else {
                            //subMenuId =  result.get(1).id
                            //subMenuName =  result.get(1).name
                        }
                    }
                    vpAdpt.removeFragment()
                    for (i in 0 until result.size) {
                        val name = result.get(i).name
                        if (name.equals(TRENDING_NAME)) {
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
                            bundle.putInt("position", i)
                            bundle.putInt("category_id", TRENDING_ID)

                            val rootFrag = RootTrendingFragment()

                            arrFragment.add(rootFrag)
                            arrDataBundle.add(bundle)

                            var deviceId = themePreference.getString("device_token", "")
                            val sessionId = getUniqueCode(this@MainActivity, themePreference)
                            //trackingCallback(nApi, themePreference, 0, "", TRENDING_ID, TRENDING_NAME, "", ActionType.TRENDINGMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                        } else if (name.equals(LATESTNEWS_NAME)) {
                            var newsCategoryId: Int = getLatestNewsID(articleNewsDao)
                            var bundle = Bundle()
                            bundle.putString("category_name", LATESTNEWS_NAME)
                            bundle.putInt("position", i)
                            bundle.putInt("category_id", newsCategoryId)

                            val newsFrag = NewsFragment()

                            arrFragment.add(newsFrag)
                            arrDataBundle.add(bundle)
                        } else if (name.equals(DAILYDIGEST_NAME)) {
                            var bundle = Bundle()
                            bundle.putString("category_name", DAILYDIGEST_NAME)
                            bundle.putInt("position", i)
                            bundle.putInt("category_id", DAILYDIGEST_ID)

                            val newsFrag = DailyDigestFragment()

                            arrFragment.add(newsFrag)
                            arrDataBundle.add(bundle)
                        } else {
                            if (!name.equals(LATESTNEWS_FIELDNAME) && !name.equals(LATESTNEWS_FIELDNAME2)) {
                                var bundle = Bundle()
                                bundle.putString("category_name", result.get(i).name)
                                bundle.putInt("position", i)
                                bundle.putInt("category_id", result.get(i).id)

                                val newsFrag = NewsFragment()

                                arrFragment.add(newsFrag)
                                arrDataBundle.add(bundle)
                            }
                        }
                    }
                    vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)
                    vPager.adapter = vpAdpt
                    vpAdpt.notifyDataSetChanged()
                    setIconsTab(tabLayout)
                    vPager.setCurrentItem(0)
                    trackingCallback(nApi, themePreference, 0, "", subMenuId, subMenuName, "", ActionType.MENUCHANGE.type, deviceId
                            ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                }
            })
        }
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START)
        }
        scrollToCenter(pos)
    }

    override fun onDestroy() {
        var deviceId = themePreference.getString("device_token", "")
        val sessionId = getUniqueCode(this@MainActivity, themePreference)
        trackingCallback(nApi, themePreference, 0, "", 0, "", "", ActionType.APPCLOSE.type, deviceId
                ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        super.onDestroy()
    }

    fun swipeTopHeading(position: Int) {
        var data = topMenuResult.get(position)
        var headingData = TopHeadingData(data.id, position, data.name)

        var deviceId = themePreference.getString("device_token", "")
        val sessionId = getUniqueCode(this@MainActivity, themePreference)
        trackingCallback(nApi, themePreference, 0, "", data.id, data.name, "", ActionType.BURGERMENUCLICK.type, deviceId
                ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        if (!(rv_top_heading.isVisible)) {

            vPager.setCurrentItem(position)
        } else {
            rvMenu.scrollToPosition(position)
            menuAdpt.selectedItem = position
            menuAdpt.setPosition(position)
            menuAdpt.notifyDataSetChanged()

            loadNewsData(headingData)
        }
    }


    fun swipeNewsData(headingData: TopHeadingData) {
        var headingPos: Int = 0
        val pos = menuAdpt.getPosition()
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
            vpAdpt.removeFragment()

            arrFragment.add(rootFrag)
            arrDataBundle.add(bundle)
            vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)
            vPager.adapter = vpAdpt
            //vpAdpt.notifyDataSetChanged()
            setIconsTab(tabLayout)
            //vPager.setCurrentItem(0)

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", TRENDING_ID, TRENDING_NAME, "", ActionType.TRENDINGMENUCLICK.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        } else if (headingData.category.equals(LATESTNEWS_NAME)) {
            tabLayout.visibility = View.GONE
            var newsCategoryId: Int = getLatestNewsID(articleNewsDao)
            var newsCategoryName: String = getLatestNewsName(articleNewsDao)
            var bundle = Bundle()
            bundle.putString("category_name", newsCategoryName)
            bundle.putInt("position", 0)
            bundle.putInt("category_id", newsCategoryId)

            val newsFrag = NewsFragment()
            vpAdpt.removeFragment()
            vPager.adapter = null
            arrFragment.add(newsFrag)
            arrDataBundle.add(bundle)

            vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)

            vPager.adapter = vpAdpt
            //vpAdpt.notifyDataSetChanged()
            setIconsTab(tabLayout)
            //vPager.setCurrentItem(0)

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", newsCategoryId, LATESTNEWS_NAME, "", ActionType.MENUCHANGE.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        } else if (headingData.category.equals(DAILYDIGEST_NAME)) {
            tabLayout.visibility = View.GONE
            var bundle = Bundle()
            bundle.putString("category_name", DAILYDIGEST_NAME)
            bundle.putInt("position", 0)
            bundle.putInt("category_id", DAILYDIGEST_ID)

            val newsFrag = DailyDigestFragment()
            vpAdpt.removeFragment()

            arrFragment.add(newsFrag)
            arrDataBundle.add(bundle)

            vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)

            vPager.adapter = vpAdpt
            //vpAdpt.notifyDataSetChanged()
            setIconsTab(tabLayout)
            //vPager.setCurrentItem(0)

            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", DAILYDIGEST_ID, DAILYDIGEST_NAME, "", ActionType.DAILYDIGESTMENUCLICK.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
        } else {
            if (menuAdpt.itemCount <= 1) {
                rvMenu.visibility = View.GONE
            } else {
                rvMenu.visibility = View.VISIBLE
            }

            tabLayout.visibility = View.VISIBLE
            vpAdpt.removeFragment()
            var deviceId = themePreference.getString("device_token", "")
            val sessionId = getUniqueCode(this@MainActivity, themePreference)
            trackingCallback(nApi, themePreference, 0, "", headingData.id, headingData.category, "", ActionType.PARENTCATEGORYCLICK.type, deviceId
                    ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)

            dataVM.getSubMenuDataFromDb(headingData.id).observe(this, object : androidx.lifecycle.Observer<List<SubMenuResultData>> {
                override fun onChanged(list: List<SubMenuResultData>?) {
                    var result = list as ArrayList<SubMenuResultData>
                    if (menuAdpt.itemCount <= 1) {

                        if (BuildConfig.showTrendingNews) {
                            val trendingSubMenu = SubMenuResultData(TRENDING_ID, TRENDING_ID, TRENDING_NAME)
                            result.add(0, trendingSubMenu)
                        }
                        if (BuildConfig.showLatestNews) {
                            val latestSubMenu = SubMenuResultData(LATESTNEWS_ID, LATESTNEWS_ID, LATESTNEWS_NAME)
                            if (BuildConfig.showTrendingNews) {

                                result.add(1, latestSubMenu)
                            } else {
                                result.add(0, latestSubMenu)
                            }
                        }

                        if (BuildConfig.showDailyDigest) {
                            val dailyDigestSubMenu = SubMenuResultData(DAILYDIGEST_ID, DAILYDIGEST_ID, DAILYDIGEST_NAME)

                            if (BuildConfig.showTrendingNews && BuildConfig.showLatestNews) {
                                result.add(2, dailyDigestSubMenu)
                            } else if (BuildConfig.showTrendingNews && !BuildConfig.showLatestNews) {
                                result.add(1, dailyDigestSubMenu)
                            } else if (!BuildConfig.showTrendingNews && BuildConfig.showLatestNews) {
                                result.add(1, dailyDigestSubMenu)
                            } else {
                                result.add(0, dailyDigestSubMenu)
                            }
                        }
                    }
                    var subMenuId: Int = 0
                    var subMenuName: String = ""
                    if (result.size > 0) {
                        vpAdpt.removeFragment()

                        if (!subMenuName.equals(LATESTNEWS_FIELDNAME) && !subMenuName.equals(LATESTNEWS_FIELDNAME2)) {
                            subMenuId = result.get(0).id
                            subMenuName = result.get(0).name
                        } else {
                            //subMenuId =  result.get(1).id
                            //subMenuName =  result.get(1).name
                        }
                    }
                    vpAdpt.removeFragment()
                    for (i in 0 until result.size) {
                        val name = result.get(i).name
                        if (name.equals(TRENDING_NAME)) {
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
                            bundle.putInt("position", i)
                            bundle.putInt("category_id", TRENDING_ID)

                            val rootFrag = RootTrendingFragment()

                            arrFragment.add(rootFrag)
                            arrDataBundle.add(bundle)

                            var deviceId = themePreference.getString("device_token", "")
                            val sessionId = getUniqueCode(this@MainActivity, themePreference)
                            //trackingCallback(nApi, themePreference, 0, "", TRENDING_ID, TRENDING_NAME, "", ActionType.TRENDINGMENUCLICK.type, deviceId?:"", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                        } else if (name.equals(LATESTNEWS_NAME)) {
                            var newsCategoryId: Int = getLatestNewsID(articleNewsDao)
                            var bundle = Bundle()
                            bundle.putString("category_name", LATESTNEWS_NAME)
                            bundle.putInt("position", i)
                            bundle.putInt("category_id", newsCategoryId)

                            val newsFrag = NewsFragment()

                            arrFragment.add(newsFrag)
                            arrDataBundle.add(bundle)
                        } else if (name.equals(DAILYDIGEST_NAME)) {
                            var bundle = Bundle()
                            bundle.putString("category_name", DAILYDIGEST_NAME)
                            bundle.putInt("position", i)
                            bundle.putInt("category_id", DAILYDIGEST_ID)

                            val newsFrag = DailyDigestFragment()

                            arrFragment.add(newsFrag)
                            arrDataBundle.add(bundle)
                        } else {
                            if (!name.equals(LATESTNEWS_FIELDNAME) && !name.equals(LATESTNEWS_FIELDNAME2)) {
                                var bundle = Bundle()
                                bundle.putString("category_name", result.get(i).name)
                                bundle.putInt("position", i)
                                bundle.putInt("category_id", result.get(i).id)

                                val newsFrag = NewsFragment()

                                arrFragment.add(newsFrag)
                                arrDataBundle.add(bundle)
                            }
                        }
                    }
                    vpAdpt = MainAdapter(this@MainActivity, supportFragmentManager, arrFragment, arrDataBundle)
                    vPager.adapter = vpAdpt
                    //vpAdpt.notifyDataSetChanged()
                    setIconsTab(tabLayout)
                    //vPager.setCurrentItem(0)
                    trackingCallback(nApi, themePreference, 0, "", subMenuId, subMenuName, "", ActionType.MENUCHANGE.type, deviceId
                            ?: "", PLATFORM, ViewType.ENGAGEVIEW.type, sessionId, "", 0)
                }
            })
        }
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START)
        }
        scrollToCenter(pos)
    }
}