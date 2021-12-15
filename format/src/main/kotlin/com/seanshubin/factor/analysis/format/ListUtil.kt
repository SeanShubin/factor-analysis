package com.seanshubin.factor.analysis.format

object ListUtil {
    fun <T> List<List<T>>.transpose(): List<List<T>> {
        return if (this.isEmpty()) {
            emptyList()
        } else {
            val mutableList = mutableListOf<List<T>>()
            for (i in 0..this[0].lastIndex) {
                val newMutableRow = mutableListOf<T>()
                for (j in 0..this.lastIndex) {
                    newMutableRow.add(this[j][i])
                }
                mutableList.add(newMutableRow)
            }
            mutableList
        }
    }
}
