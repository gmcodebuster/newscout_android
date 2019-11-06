package com.fafadiatech.newscout.customcomponent

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager

class VerticalViewPager(context: Context, attributeSet: AttributeSet) : ViewPager(context, attributeSet) {

    init {
        initialization()
    }

    private var downX: Float = 0.toFloat()
    private var downY: Float = 0.toFloat()
    private var isTouchCaptured: Boolean = false
    var upX1: Float = 0.toFloat()
    var upY1: Float = 0.toFloat()
    var upX2: Float = 0.toFloat()
    var upY2: Float = 0.toFloat()
    val min_distance = 10
    var eventSent = false

    internal var mSwiperListener: SwiperListener? = null


    fun initialization() {
        setPageTransformer(true, VerticalViewPagerTransform())
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    interface SwiperListener {

        fun onLeftSwipe()
    }

    companion object {

        private val Min_Scale = 0.65f
    }

    private fun swapXY(event: MotionEvent): MotionEvent {
        val x = width.toFloat()
        val y = height.toFloat()

        val newX = event.y / y * y
        val newY = event.x / x * x
        event.setLocation(newX, newY)
        return event
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.getAction()) {
            MotionEvent.ACTION_MOVE -> {
                downX = ev.getX()
                downY = ev.getY()

                if (!isTouchCaptured) {
                    upX1 = ev.getX()
                    upY1 = ev.getY()
                    isTouchCaptured = true
                } else {
                    upX2 = ev.getX()
                    upY2 = ev.getY()
                    val deltaX = upX1 - upX2
                    val deltaY = upY1 - upY2

                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (Math.abs(deltaX) > min_distance) {

                            if (deltaX > 0) {
                                return false
                            } else if (deltaX < 0) {
                                return true
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouchCaptured = false
                eventSent = false
            }

        }

        return super.onTouchEvent(swapXY(ev))
    }

    private inner class VerticalViewPagerTransform : ViewPager.PageTransformer {
        override fun transformPage(page: View, position: Float) {

            inshortAnimation(page, position)
        }

        fun verticalFlipTranformation(page: View, position: Float) {
            page.setTranslationX(-position * page.getWidth())
            page.setCameraDistance(12000f);

            if (position < 0.5 && position > -0.5) {
                page.setVisibility(View.VISIBLE);
            } else {
                page.setVisibility(View.INVISIBLE);
            }
            if (position < -1) {
                page.setAlpha(0f);
            } else if (position <= 0) {
                page.setAlpha(1f)
                page.setRotationY(180 * (1 - Math.abs(position) + 1))
            } else if (position <= 1) {
                page.setAlpha(1f)
                page.setRotationY(-180 * (1 - Math.abs(position) + 1));
            } else {

                page.setAlpha(0f);
            }
        }

        fun inshortAnimation(page: View, position: Float) {

            if (position < -1) {
                page.alpha = 0f
            } else if (position <= 0) {
                page.alpha = 1f
                page.translationX = page.width * -position
                page.translationY = page.height * position
                page.scaleX = 1f
                page.scaleY = 1f
            } else if (position <= 1) {
                page.alpha = 1 - position
                page.translationX = page.width * -position
                page.translationY = 0f
                val scaleFactor = Min_Scale + (1 - Min_Scale) * (1 - Math.abs(position))
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
            } else if (position > 1) {
                page.alpha = 0f
            }
        }

        fun rotateAnimation(page: View, position: Float) {
            val ROT_MOD = -15f
            val width = page.getWidth();
            val rotation: Float = ROT_MOD * position;
            page.setPivotX(width * 0.5f)
            page.setPivotY(0f)
            page.setTranslationX(0f)
            page.setRotation(rotation)
        }
    }
}


