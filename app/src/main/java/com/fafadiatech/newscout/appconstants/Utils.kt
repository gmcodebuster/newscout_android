package com.fafadiatech.newscout.appconstants

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.widget.ImageView
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.model.SubMenuResultData

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
