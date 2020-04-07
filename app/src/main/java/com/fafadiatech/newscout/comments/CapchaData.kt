package com.fafadiatech.newscout.comments

import com.fafadiatech.newscout.model.NewsStatus

data class CapchaData(val status:Int, val new_captch_key:String, val new_captch_image:String)

data class CapchaBodyResponseData(var result: CapchaData)

data class CapchaResponseData(var header: NewsStatus, var body: CapchaBodyResponseData)

/*

Direct API call from VM
{"header":{"status":"1"},
"body":{"result":"{"status": 1, "new_captch_key": "7db0d3af47c4f29370a8dba3ea5f006667bcfcab",
 "new_captch_image": "/captcha/image/7db0d3af47c4f29370a8dba3ea5f006667bcfcab/"}"}}

 */