package com.fafadiatech.newscout.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fafadiatech.newscout.PrefManager
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.viewmodel.FetchDataApiViewModel

class IntroActivity : AppCompatActivity() {

    private var viewPager: ViewPager? = null
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    var dots: Array<TextView?>? = null
    private var layouts: IntArray? = null
    lateinit var btnGetStarted: Button

    private var prefManager: PrefManager? = null
    lateinit var fetchDataViewModel: FetchDataApiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchDataViewModel = ViewModelProviders.of(this).get(FetchDataApiViewModel::class.java)

        prefManager = PrefManager(this)
        if (!prefManager!!.isFirstTimeLaunch()) {
            launchHomeScreen()
        }

        setContentView(R.layout.activity_intro_screen)
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        dotsLayout = findViewById(R.id.layoutDots)
        btnGetStarted = findViewById(R.id.btn_get_started)
        layouts = intArrayOf(R.layout.welcome_slide_one, R.layout.welcome_slide_two, R.layout.welcome_slide_three,
                R.layout.welcome_slide_four, R.layout.welcome_slide_five, R.layout.welcome_slide_six)
        addBottomDots(0)
        myViewPagerAdapter = MyViewPagerAdapter()
        viewPager!!.setAdapter(myViewPagerAdapter)
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)

        btnGetStarted.setOnClickListener {
            launchHomeScreen()
        }

        fetchDataViewModel.startMenuWorkManager()
    }

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls<TextView>(layouts!!.size)
        dotsLayout!!.removeAllViews()
        for (i in dots!!.indices) {
            dots!![i] = TextView(this)
            dots!![i]?.text = HtmlCompat.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
            dots!![i]?.textSize = 35f
            dots!![i]?.setTextColor(ContextCompat.getColor(this, R.color.grey_light))
            dotsLayout!!.addView(dots!![i])
        }

        if (dots!!.size > 0)
            dots!![currentPage]?.setTextColor(ContextCompat.getColor(this, R.color.black))
    }

    private fun getItem(i: Int): Int {
        return viewPager!!.getCurrentItem() + i
    }

    private fun launchHomeScreen() {
        prefManager!!.setFirstTimeLaunch(false)
        startActivity(Intent(this@IntroActivity, MainActivity::class.java))
        this@IntroActivity.finish()
    }


    var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            addBottomDots(position)
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
        }

        override fun onPageScrollStateChanged(arg0: Int) {
        }
    }


    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(layouts!![position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts!!.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}