package com.fafadiatech.newscout.appconstants

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.model.AdsData
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.model.SubMenuResultData
import java.util.*
import kotlin.collections.ArrayList

fun convertDpToPx(context: Context, dp: Int): Int {
    return dp * context.resources.displayMetrics.density.toInt()
}

var networkStatus: Boolean = false

fun getImageURL(view: ImageView, originalUrl: String): String {
    var url: String = ""
    url = THUMBOR_IMG_URL + view.width.toString() + "x" + view.height.toString() + "/smart/" + originalUrl
    return url
}

fun getImageURL(view: ImageView, originalUrl: String, width: Int, height: Int): String {
    var url: String = ""
    url = THUMBOR_IMG_URL + width.toString() + "x" + height.toString() + "/smart/" + originalUrl
    return url
}

fun checkInternet(context: Context) {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    val isConnected: Boolean = activeNetwork?.isConnected == true
    val isWiFi: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_WIFI
}

fun getWifiLevel(context: Context): Int {
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val linkSpeed = wifiManager.connectionInfo.rssi
    return WifiManager.calculateSignalLevel(linkSpeed, 5)
}

fun getMenuFromDB(context: Context) {
    val database = NewsDatabase.getInstance(context)
    val newsDao = database?.newsDao()
    val listOfSubMenu = newsDao?.getAllSubMenuFromDb()
    for (sm: SubMenuResultData in listOfSubMenu.orEmpty()) {
        val id = sm.id
        val name = sm.name
        var tagData = ArrayList<String>()
        tagData = newsDao?.getMenuTagsFromDb(id) as ArrayList<String>
        MyApplication.tagDataHashMap.put(name, tagData)
        MyApplication.categoryIdHashMap.put(name, id)
    }
}

fun getLatestNewsID(newsDao: NewsDao): Int {
    var newsCategoryId: Int = 123
    try {
        var ids = newsDao.getLatestNewsId()
        if (ids.size > 0) {
            newsCategoryId = ids.get(0)
        }
    } catch (e: Exception) {
        newsCategoryId = 123
    }
    return newsCategoryId
}

fun addAdsData(newsList : ArrayList<INews>?): ArrayList<INews>?{

    val newsIterator = newsList?.listIterator()
    for ((index, value) in newsIterator?.withIndex()!!) {
        if(index > 0 && (index + 1) % 10 == 0){
            newsIterator.add(AdsData("",""))
        }
    }

    return newsList
}

fun getUniqueCode(con:Context, sharedPreference: SharedPreferences) : String{
    var uniqueCode: String = ""
    try{
        //check shared preferance value for uniqueCode
        sharedPreference?.let{
            uniqueCode = sharedPreference.getString(UNIQUE_CODE, "")
            if(uniqueCode.isNullOrEmpty()){
                val androidId = Settings.Secure.getString(con.contentResolver, Settings.Secure.ANDROID_ID)
                val currentTime = System.currentTimeMillis()/ 1000L
                val uniqueCode = androidId + "" + currentTime
                var editor = it.edit()
                editor.putString(UNIQUE_CODE, uniqueCode)
                editor.commit()
                return uniqueCode
            }else{
                return uniqueCode
            }
        }
    }catch (ex: Exception){
        Log.d("NewScout", ex.message)
    }
    return uniqueCode
}

fun getUUID(): String{
    val uniqueID: String = UUID.randomUUID().toString()
    return uniqueID
}

enum class ActionType(val type:String){
    PUBLISHED("published"),
    APPOPEN("app_open"),
    APPCLOSE("app_close"),
    MENUCHANGE("menu_change"),
    PAGESCROLL("page_scroll"),
    ARTICLEDETAIL("article_detail"),
    READMORE("original_article"),
    RECOMMENDATION("recommendations"),
    BOOKMARK("bookmark_article"),
    SHAREARTICLE("share_article"),
    SOURCECLICK("source_click"),
    SEARCHQUERY("search_query"),
    ARTICLESEARCHDETAIL("article_search_details"),
    BURGERICON("burger_icon_click"),
    BURGERMENUCLICK("burger_menu_click"),
    LOGIN("login"),
    LOGOUT("logout"),
    PROFILEVIEW("profile_view"),
    FORGOTPASSWORD("forgot_password"),
    SIGNUP("signup"),
    CHANGEPASSWORD("change_password"),
    SHARETHISAPP("share_this_app"),
    ABOUTUS("about_us"),
    DEVELOPEDBY("devloped_by"),
    TRENDINGMENUCLICK("trending_menu_click"),
    TRENDINGLISTCLICK("trending_list_click"),
    DAILYDIGESTMENUCLICK("daily_digest_click"),
    FAILYDIGESTLISTCLICK("daily_digest_list_click"),
    OPTIONMENU("options_menu_open"),
    SETTINGSMENUCLICK("settings_open"),
    MODECHANGE("mode_change"),
    BOOKMARKMENUCLICK("view_book_marks"),
    SCROLLTOTOP("scroll_to_top"),
    ADCLICK("ad_click")

    //val actionType = ActionType.PUBLISHED.type
}

enum class ViewType(val type: String){
    EDITORIALVIEW("EDITORIAL_VIEW"),
    ENGAGEVIEW("ENGAGE_VIEW"),
    MONETIZATIONVIEW("MONETIZATION_VIEW")
}