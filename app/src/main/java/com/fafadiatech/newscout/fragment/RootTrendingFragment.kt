package com.fafadiatech.newscout.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fafadiatech.newscout.FragmentUtil
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.interfaces.TrendingFragListener

class RootTrendingFragment() : Fragment(), TrendingFragListener {

    private val TAG = "RootTrendingFragment"
    var position: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.root_fragment, container, false)

        var fm: FragmentManager = childFragmentManager
        var trendingFrag = FragmentUtil.getFragmentByTagName(fm, "TrendingFragment")
        if (trendingFrag == null) {
            trendingFrag = TrendingFragment()
        }

        var category = arguments!!.getString("category_name")
        var categoryId = arguments!!.getInt("category_id")
        var bundle: Bundle = Bundle()
        bundle.putString("category_name", category)
        bundle.putInt("category_id", categoryId)
        trendingFrag.arguments = bundle
        var transaction = fm.beginTransaction()
        transaction.replace(R.id.root_frame, trendingFrag, "TrendingFragment")
        transaction.addToBackStack(null)
        transaction.commit()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun loadFragment(clusterId: Int, position: Int) {
        this.position = position
        var fm = childFragmentManager
        var transaction = fm.beginTransaction()
        var newsFragment = TrendingNewsFragment()
        var fragment = FragmentUtil.getFragmentByTagName(fm, "TrendingFragment")
        if (fragment != null) {
            transaction.hide(fragment)
        }
        var bundle = Bundle()
        var categoryId: Int = -1
        var categoryName = "Trending"
        bundle.putInt("category_id", categoryId)
        bundle.putString("category_name", categoryName)
        bundle.putInt("cluster_id", clusterId)
        bundle.putInt("last_position", position)
        newsFragment.arguments = bundle
        transaction.replace(R.id.root_frame, newsFragment!!, "TrendingNewsFragment")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
    }
}