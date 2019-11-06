package com.fafadiatech.newscout.customcomponent

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

class MyItemDecoration(mDrawable: Drawable) : RecyclerView.ItemDecoration() {
    internal var drawable: Drawable
    internal var mInsets = 0

    init {
        this.drawable = mDrawable
    }

    override fun onDraw(@NonNull c: Canvas, @NonNull parent: RecyclerView, @NonNull state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    fun drawVertical(c: Canvas, parent: RecyclerView) {
        if (parent.getChildCount() === 0) return
        val childCount = parent.getChildCount()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.getLayoutParams() as RecyclerView.LayoutParams
            val left = child.getLeft() - params.leftMargin - mInsets
            val right = child.getRight() + params.rightMargin + mInsets
            val top = child.getBottom() + params.bottomMargin + mInsets
            val bottom = top + drawable.getIntrinsicHeight()
            drawable.setBounds(left, top, right, bottom)
            drawable.draw(c)
        }
    }

    fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val childCount = parent.getChildCount()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.getLayoutParams() as RecyclerView.LayoutParams
            val left = child.getRight() + params.rightMargin + mInsets
            val right = left + drawable.getIntrinsicWidth()
            val top = child.getTop() - params.topMargin - mInsets
            val bottom = child.getBottom() + params.bottomMargin + mInsets
            drawable.setBounds(left, top, right, bottom)
            drawable.draw(c)
        }
    }
}