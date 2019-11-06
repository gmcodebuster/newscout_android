package com.fafadiatech.newscout.model

data class MenuHeading(var id: Int, var name: String) : Comparable<Any> {
    override fun compareTo(other: Any): Int {
        val compare = other as MenuHeading
        if (compare.id == this.id && compare.name.equals(this.name)) {
            return 0
        }
        return 1
    }
}