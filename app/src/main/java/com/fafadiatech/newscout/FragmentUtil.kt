package com.fafadiatech.newscout

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentUtil {

    companion object {
        val TAG_NAME_FRAGMENT = "ACTIVITY_FRAGMENT"
        fun getFragmentByTagName(fragmentManager: FragmentManager, fragmentTagName: String): Fragment? {
            var ret: Fragment? = null
            val fragmentList = fragmentManager.getFragments()

            if (fragmentList != null) {
                val size = fragmentList!!.size
                for (i in 0 until size) {
                    val fragment = fragmentList!!.get(i)

                    if (fragment != null) {
                        val fragmentTag = fragment!!.getTag()
                        if (fragmentTag == fragmentTagName) {
                            ret = fragment
                        }
                    }
                }
            }
            return ret
        }

        fun printActivityFragmentList(fragmentManager: FragmentManager) {
            val fragmentList = fragmentManager.fragments

            if (fragmentList != null) {
                val size = fragmentList.size
                for (i in 0 until size) {
                    val fragment = fragmentList[i]
                    if (fragment != null) {
                        val fragmentTag = fragment.tag
                    }
                }
            }
        }
    }
}