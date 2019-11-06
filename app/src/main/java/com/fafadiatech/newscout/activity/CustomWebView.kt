package com.fafadiatech.newscout.activity

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView

class CustomWebView : WebView {

    private var gestureDetector: GestureDetector? = null


    constructor(context: Context) : super(context) {}


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return gestureDetector!!.onTouchEvent(ev) || super.onTouchEvent(ev)
    }

    fun setGestureDetector(gestureDetector: GestureDetector) {
        this.gestureDetector = gestureDetector
    }
}