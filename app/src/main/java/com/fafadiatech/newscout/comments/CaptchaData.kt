package com.fafadiatech.newscout.comments

import com.fafadiatech.newscout.model.NewsStatus

data class CaptchaData(val status:Int, val new_captch_key:String, val new_captch_image:String)

data class CaptchaBodyResponseData(var result: CaptchaData)

data class CaptchaResponseData(var header: NewsStatus, var body: CaptchaBodyResponseData)