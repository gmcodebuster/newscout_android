package com.fafadiatech.newscout.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelProviderFactory():ViewModelProvider.Factory {

    lateinit var mApplication:Application
    var mParams:Int=0

    constructor(application: Application,param:Int):this(){
        mApplication = application
        mParams = param
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  FetchDataApiViewModel(mApplication,mParams)  as T
    }
}