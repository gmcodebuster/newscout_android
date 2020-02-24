package com.fafadiatech.newscout.adapter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.appconstants.convertDpToPx
import com.fafadiatech.newscout.fragment.NewsFragment

class MainAdapter(context: Context, fragManager: FragmentManager, data : ArrayList<Fragment>, dataBundle : ArrayList<Bundle>) : FragmentPagerAdapter(fragManager) {

    var con: Context = context
    private var baseId: Long = 0
    var subHeading = ArrayList<String>()
    var tagDataArrayList = ArrayList<ArrayList<String>>()
    var categoryListFromApi = ArrayList<String>()
    var tagList = ArrayList<String>()
    lateinit var themePreference: SharedPreferences
    val imgWd = convertDpToPx(context, 153)
    val imgHt = convertDpToPx(context, 100)
    var dataBundleList = ArrayList<Bundle>()
    var dataList = ArrayList<Fragment>()
    lateinit var fManager: FragmentManager

    init {
        this.fManager = fragManager
        this.dataList = data
        this.dataBundleList = dataBundle
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Fragment {
        if (dataList.size > 0) {
            var bData = dataBundleList.get(position)
            var frag = dataList.get(position)
            frag.arguments = bData
            return frag
        } else {
            return NewsFragment()
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        fManager.beginTransaction().remove(`object` as Fragment).commitNowAllowingStateLoss()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position)
    }

    fun addFragment(position: Int, f: Fragment, data: Bundle) {
        dataList.add(f)
        dataBundleList.add(data)
        notifyDataSetChanged()
    }

    fun removeFragment() {
        dataBundleList.clear()
        dataList.removeAll(dataList)
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return null
    }

    fun getTabView(position: Int): View {
        val bData = dataBundleList.get(position)
        var name = bData.getString("category_name")
        val view = LayoutInflater.from(con).inflate(R.layout.custom_tab, null);
        val tvTitle = view.findViewById(R.id.tab_title) as TextView;
        tvTitle.setText(name);

        return view;
    }
}