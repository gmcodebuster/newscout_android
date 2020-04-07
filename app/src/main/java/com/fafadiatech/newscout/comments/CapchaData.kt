package com.fafadiatech.newscout.comments

import com.fafadiatech.newscout.model.NewsStatus

data class CapchaData(val status:Int, val new_captch_key:String, val new_captch_image:String)

data class CapchaBodyResponseData(var result: CapchaData)

data class CapchaResponseData(var header: NewsStatus, var body: CapchaBodyResponseData)