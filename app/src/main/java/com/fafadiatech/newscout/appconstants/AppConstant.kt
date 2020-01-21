package com.fafadiatech.newscout.appconstants

import android.app.Activity
import android.content.Context
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fafadiatech.newscout.api.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.hardware.usb.UsbDevice.getDeviceId
import android.content.Context.TELEPHONY_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat.getSystemService
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemServiceName

val URL = "http://www.newscout.in/"
val BASE_URL = URL + "api/v1/"
const val TRACKING_URL = "http://www.newscout.in/event/track/"//"http://t.fafadiatech.com/api/v1/track/"
val THUMBOR_IMG_URL = "http://d3fgx8oqv3lbgx.cloudfront.net/unsafe/"
val KEY_NAME = "menu_name"
val TRENDING_ID = -1
const val TRENDING_NAME = "Trending"
val NONEWS_ID = -2
val DAILYDIGEST_ID = -3
const val DAILYDIGEST_NAME = "Daily Digest"
val LATESTNEWS_ID = -4
const val LATESTNEWS_FIELDNAME = "Uncategorised"
const val LATESTNEWS_FIELDNAME2 = "Uncategorized"
const val LATESTNEWS_NAME = "Latest News"
const val UNIQUE_CODE = "uniquecode"
val NEWSPAGESIZE = 20
val TRENDINGPAGESIZE = 30
const val PLATFORM = "android"
val ADFACTOR = 10

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

@RequiresApi(Build.VERSION_CODES.O)
fun getIMEINo(con:Context, activity: Activity){
    if (ContextCompat.checkSelfPermission(con, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.READ_PHONE_STATE)) {

        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 2)
        }
    }

    try{
        val tm = con.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val IMEI = tm.imei
        if (IMEI != null)
            Toast.makeText(con, "IMEI number: " + IMEI,
                    Toast.LENGTH_LONG).show()

    }catch (ex:Exception){
        Log.e("",ex.message)
    }
}


