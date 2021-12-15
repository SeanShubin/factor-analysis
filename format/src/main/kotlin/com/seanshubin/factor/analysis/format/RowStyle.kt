package com.seanshubin.factor.analysis.format

data class RowStyle(
    val left: String,
    val middle: String,
    val right: String,
    val separator: String
) {
    fun format(columnWidths: List<Int>): String {
        val columns: List<String> = columnWidths.map(middle::repeat)
        val expandedMiddle: String = columns.joinToString(separator)
        return left + expandedMiddle + right
    }

    fun format(columnWidths: List<Int>, data: List<Any?>, formatCell: (Any?, Int, String) -> String): String {
        val cells = (columnWidths zip data).map { (width, data) -> formatCell(data, width, middle) }
        val expandedCells = cells.joinToString(separator)
        return left + expandedCells + right
    }
}
