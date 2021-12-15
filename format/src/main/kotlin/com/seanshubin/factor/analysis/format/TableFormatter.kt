package com.seanshubin.factor.analysis.format

import com.seanshubin.factor.analysis.format.StringUtil.escape
import com.seanshubin.factor.analysis.format.StringUtil.truncate
import java.io.Reader

interface TableFormatter {
    interface Justify {
        data class Left(val x: Any?) : Justify

        data class Right(val x: Any?) : Justify
    }

    fun format(originalRows: List<List<Any?>>): List<String>
    fun <T> parse(reader: Reader, mapToElement: (Map<String, String>) -> T): Iterable<T>

    companion object {
        val escapeString: (Any?) -> String = { cell ->
            when (cell) {
                null -> "null"
                else -> cell.toString().escape()
            }
        }

        fun escapeAndTruncateString(max: Int): (Any?) -> String = { cell ->
            escapeString(cell).truncate(max)
        }
    }
}
