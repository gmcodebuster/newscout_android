package com.fafadiatech.newscout.appconstants

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.db.NewsDatabase.Companion.getInstance
import com.fafadiatech.newscout.model.AdsData
import com.fafadiatech.newscout.model.DetailNewsData
import com.fafadiatech.newscout.model.INews
import com.fafadiatech.newscout.model.SubMenuResultData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    var newsCategoryId: Int = 0
    try {
        var ids = newsDao.getLatestNewsId()
        if (ids.size > 0) {
            newsCategoryId = ids.get(0)
        }
    } catch (e: Exception) {
        newsCategoryId = 0
    }
    return newsCategoryId
}

fun addAdsData(newsList : ArrayList<INews>?): ArrayList<INews>?{

    val newsIterator = newsList?.listIterator()
    for ((index, value) in newsIterator?.withIndex()!!) {
        if(index > 0 && (index + 1) % ADFACTOR == 0){
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
    BOOKMARKARTICLEDETAIL("bookmark_article_detail"),
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
    RATETHISAPP("rate_this_app"),
    ABOUTUS("about_us"),
    DEVELOPEDBY("devloped_by"),
    PARENTCATEGORYCLICK("parent_category_change"),
    TRENDINGMENUCLICK("trending_menu_click"),
    TRENDINGGROUPCLICK("trending_group_click"),
    TRENDINGLISTCLICK("trending_list_click"),
    DAILYDIGESTMENUCLICK("daily_digest_click"),
    DAILYDIGESTLISTCLICK("daily_digest_list_click"),
    OPTIONMENU("options_menu_open"),
    SETTINGSMENUCLICK("settings_open"),
    SEARCHMENUCLICK("search_menu_click"),
    MODECHANGE("mode_change"),
    BOOKMARKMENUCLICK("view_book_marks"),
    SCROLLTOTOP("scroll_to_top"),
    ADCLICK("ad_click")

}

enum class ViewType(val type: String){
    EDITORIALVIEW("EDITORIAL_VIEW"),
    ENGAGEVIEW("ENGAGE_VIEW"),
    MONETIZATIONVIEW("MONETIZATION_VIEW")
}

fun trackingCallback(nApi: ApiInterface, themePreference: SharedPreferences, itemId:Int, itemName:String, categoryId: Int, categoryName: String, authorName:String, action:String, deviceId:String, plateform:String, type:String, sessionId:String, sourceName:String, sourceId:Int = 0, clusterId: Int = 0){

    var tCall: Call<Void> = nApi.trackApp(itemId, itemName, categoryId, categoryName, authorName, action, deviceId, plateform, type, sessionId, sourceName, sourceId, clusterId) //getUniqueCode(con, themePreference)
    tCall.enqueue(object: Callback<Void> {
        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("Utils: ", "Error")
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("Utils: ", "Success")
        }
    })
}

fun adsTrackingCallback(nApi: ApiInterface, themePreference: SharedPreferences, itemId:Int, itemName:String, categoryId: Int, categoryName: String, authorName:String, action:String, deviceId:String, plateform:String, type:String, sessionId:String, sourceName:String, sourceId:Int = 0, clusterId: Int = 0){

    var tCall: Call<Void> = nApi.trackApp(itemId, itemName, categoryId, categoryName, authorName, action, deviceId, plateform, type, sessionId, sourceName, sourceId, clusterId) //getUniqueCode(con, themePreference)
    tCall.enqueue(object: Callback<Void> {
        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("Utils: ", "Error")
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("Utils: ", "Success")
        }
    })
}

fun signinTrackingCallback(nApi: ApiInterface, themePreference: SharedPreferences, action:String, deviceId:String, plateform:String, type:String, sessionId:String, firstname: String, lastname: String, token: String, email:String){

    var tCall: Call<Void> = nApi.signInTrackApp( action, deviceId, plateform, type, sessionId, firstname, lastname, token, email) //getUniqueCode(con, themePreference)
    tCall.enqueue(object: Callback<Void> {
        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("Utils: ", "Error")
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("Utils: ", "Success")
        }
    })
}

fun signupTrackingCallback(nApi: ApiInterface, themePreference: SharedPreferences, action:String, deviceId:String, plateform:String, type:String, sessionId:String, firstname: String, lastname: String, token: String, email:String){

    var tCall: Call<Void> = nApi.signInTrackApp( action, deviceId, plateform, type, sessionId, firstname, lastname, token, email) //getUniqueCode(con, themePreference)
    tCall.enqueue(object: Callback<Void> {
        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("Utils: ", "Error")
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("Utils: ", "Success")
        }
    })
}


fun getLatestNewsName(newsDao: NewsDao): String {
    var LatestNewsName: String = ""
    try {
        var ids = newsDao.getLatestNewsName()
        if (ids.size > 0) {
            LatestNewsName = ids.get(0).name
        }
    } catch (e: Exception) {
        LatestNewsName = ""
    }
    return LatestNewsName
}


fun setColorForPath(spannable: Spannable, paths: Array<String>, color: Int) {
    for (i in paths.indices) {
        val indexOfPath = spannable.toString().indexOf(paths[i])
        if (indexOfPath == -1) {
            continue
        }
        spannable.setSpan(ForegroundColorSpan(color), indexOfPath,
                indexOfPath + paths[i].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun searchTrackingCallback(nApi: ApiInterface, themePreference: SharedPreferences, action:String, deviceId:String, plateform:String, type:String, sessionId:String, searchText:String){

    /*var tCall: Call<Void> = nApi.signInTrackApp( action, deviceId, plateform, type, sessionId, searchText) //getUniqueCode(con, themePreference)
    tCall.enqueue(object: Callback<Void> {
        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("Utils: ", "Error")
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("Utils: ", "Success")
        }
    })*/


}

/*
fun resetPointer(context:Context, tableName:String) {
    val roomDb = getInstance(context)

    roomDb.writableDatabase.execSQL("DELETE FROM sqlite_sequence WHERE name='$tableName';")
    roomDb.close()
}*/