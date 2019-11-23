package com.fafadiatech.newscout.appconstants

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.api.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val BASE_URL = "http://192.168.2.100:8000/api/v1/" //www.newscout.in
const val TRACKING_URL = "http://t.fafadiatech.com/api/v1/track"
val THUMBOR_IMG_URL = "http://d3fgx8oqv3lbgx.cloudfront.net/unsafe/"
val KEY_NAME = "menu_name"
val TRENDING_ID = -1
const val TRENDING_NAME = "Trending"
val NONEWS_ID = -2
val DAILYDIGEST_ID = -3
const val DAILYDIGEST_NAME = "Daily Digest"
val LATESTNEWS_ID = -4
const val LATESTNEWS_FIELDNAME = "Uncategorised"
const val LATESTNEWS_NAME = "Latest News"
val NEWSPAGESIZE = 20
val TRENDINGPAGESIZE = 30

class AppConstant {

    companion object {
        var APPPREF = "newsPref"
        var REQUEST_FOR_ACTIVITY_CODE = 1
        var DEVICE_NAME = "android"
        var DEVICE_ID = "device"
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}

fun trackUserSelection(apiInterfaceObj: ApiInterface, menuClick: String, deviceId: String, deviceType: String, itemId: Int, itemName: String) {
    var call: Call<Void> = apiInterfaceObj.trackUserSelection(menuClick, deviceId, deviceType, itemId, itemName)
    call.enqueue(object : Callback<Void> {
        override fun onFailure(call: Call<Void>, t: Throwable) {
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            var responseCode = response.code()
            if (responseCode == 200) {

            } else {

            }
        }
    })
}

fun trackUserSearch(apiInterfaceObj: ApiInterface, search: String, deviceId: String, deviceType: String, query: String) {
    var call: Call<Void> = apiInterfaceObj.trackUserSearchQuery(search, deviceId, deviceType, query)
    call.enqueue(object : Callback<Void> {
        override fun onFailure(call: Call<Void>, t: Throwable) {
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            var responseCode = response.code()
            if (responseCode == 200) {

            } else {

            }
        }
    })
}