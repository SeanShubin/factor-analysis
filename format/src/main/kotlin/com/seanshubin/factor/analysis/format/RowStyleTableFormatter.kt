package com.seanshubin.factor.analysis.format

import com.seanshubin.factor.analysis.format.ListUtil.transpose
import com.seanshubin.factor.analysis.format.TableFormatter.Companion.escapeString
import com.seanshubin.factor.analysis.format.TableFormatter.Justify.Left
import com.seanshubin.factor.analysis.format.TableFormatter.Justify.Right
import java.io.Reader

data class RowStyleTableFormatter(
    private val cellToString: (Any?) -> String,
    private val content: RowStyle,
    private val top: RowStyle? = null,
    private val bottom: RowStyle? = null,
    private val separator: RowStyle? = null
) : TableFormatter {
    override fun format(originalRows: List<List<Any?>>): List<String> {
        val paddedRows = makeAllRowsTheSameSize(originalRows, "")
        val columns = paddedRows.transpose()
        val columnWidths = columns.map { a: List<Any?> -> maxWidthForColumn(a) }
        val formattedRows = formatRows(columnWidths, paddedRows)
        val content = if (separator == null) {
            formattedRows
        } else {
            val content = separator.format(columnWidths)
            interleave(formattedRows, content)
        }
        val top = if (top == null) listOf() else listOf(top.format(columnWidths))
        val bottom = if (bottom == null) listOf() else listOf(bottom.format(columnWidths))
        return top + content + bottom
    }

    override fun <T> parse(reader: Reader, mapToElement: (Map<String, String>) -> T): Iterable<T> {
        val lineIterator = LineIterator(reader)
        return TableIterable(lineIterator, mapToElement)
    }

    private class LineIterator(reader: Reader) : Iterator<String> {
        private val bufferedReader = reader.toBufferedReader()
        private var currentLine: String? = bufferedReader.readLine()
        override fun hasNext(): Boolean = currentLine != null
        override fun next(): String {
            val result = currentLine!!
            currentLine = bufferedReader.readLine()
            return result
        }
    }

    private class EmptyIterator<T> : Iterator<T> {
        override fun hasNext(): Boolean = false
        override fun next(): T {
            throw UnsupportedOperationException("next() on an empty iterator")
        }
    }

    private class TableIteratorWithColumnIndices<T>(
        val lineIterator: LineIterator,
        val mapToElement: (Map<String, String>) -> T,
        val columnIndexNamePairs: List<Pair<Int, String>>
    ) : Iterator<T> {
        val columnIndexToNameMap = columnIndexNamePairs.toMap()
        val columnIndices = columnIndexNamePairs.map { it.first }
        val windowed = columnIndices.windowed(2)
        override fun hasNext(): Boolean = lineIterator.hasNext()
        override fun next(): T {
            val line = lineIterator.next()
            val cells = parseCells(line)
            val map = cells.mapIndexed { index, cell -> Pair(columnIndexNamePairs[index].second, cell) }.toMap()
            return mapToElement(map)
        }

        private fun parseCells(line: String): List<String> {
            val exceptLast = windowed.map { (startIndex, endIndex) ->
                line.substring(startIndex, endIndex)
            }
            val last = line.substring(columnIndices.last())
            return exceptLast + last
        }
    }

    private class TableIterable<T>(val lineIterator: LineIterator, val mapToElement: (Map<String, String>) -> T) :
        Iterable<T> {
        override operator fun iterator(): Iterator<T> {
            if (!lineIterator.hasNext()) return EmptyIterator<T>()
            val columnIndices = parseColumnIndices(lineIterator.next())
            return TableIteratorWithColumnIndices(lineIterator, mapToElement, columnIndices)
        }

        fun parseColumnIndices(header: String): List<Pair<Int, String>> {
            val word = Regex("""[^\s]+""")
            val matches = word.findAll(header)
            return matches.map { matchResult ->
                val index = matchResult.range.first
                val name = matchResult.value
                Pair(index, name)
            }.toList()
        }
    }

    private fun makeAllRowsTheSameSize(rows: List<List<Any?>>, value: Any): List<List<Any?>> {
        val rowSizes: List<Int> = rows.map { row -> row.size }
        val targetSize = rowSizes.maxOrNull() ?: 0

        fun makeRowSameSize(row: List<Any?>): List<Any?> {
            val extraCells = makeExtraCells(targetSize - row.size, value)
            return row + extraCells
        }

        return rows.map { makeRowSameSize(it) }
    }

    private fun makeExtraCells(howMany: Int, contents: Any): List<Any> {
        return (1..howMany).map { contents }
    }

    private fun formatRows(columnWidths: List<Int>, rows: List<List<Any?>>): List<String> =
        rows.map { row ->
            content.format(columnWidths, row, ::formatCell)
        }

    private fun <T> interleave(data: List<T>, separator: T): List<T> {
        fun combine(soFar: List<T>, next: T): List<T> {
            return listOf(next) + listOf(separator) + soFar
        }

        val combineLambda = { a: List<T>, b: T -> combine(a, b) }
        return if (data.isEmpty()) {
            emptyList()
        } else {
            data.drop(1).fold(listOf(data.first()), combineLambda).asReversed()
        }
    }

    private fun maxWidthForColumn(column: List<Any?>): Int {
        return column.map { cell -> cellWidth(cell) }.maxOrNull() ?: 0
    }

    private fun cellWidth(cell: Any?): Int = justifiedCellToString(cell).length

    private fun formatCell(cell: Any?, width: Int, padding: String): String =
        when (cell) {
            is Left -> leftJustify(justifiedCellToString(cell.x), width, padding)
            is Right -> rightJustify(justifiedCellToString(cell.x), width, padding)
            null -> rightJustify(justifiedCellToString(cell), width, padding)
            is String -> leftJustify(justifiedCellToString(cell), width, padding)
            is Enum<*> -> leftJustify(justifiedCellToString(cell), width, padding)
            else -> rightJustify(justifiedCellToString(cell), width, padding)
        }

    private fun rightJustify(s: String, width: Int, padding: String = " "): String {
        return paddingFor(s, width, padding) + s
    }

    private fun leftJustify(s: String, width: Int, padding: String = " "): String {
        return s + paddingFor(s, width, padding)
    }

    private fun paddingFor(s: String, width: Int, padding: String): String {
        val quantity = width - s.length
        return padding.repeat(quantity)
    }

    private fun justifiedCellToString(cell: Any?): String =
        when (cell) {
            is Left -> justifiedCellToString(cell.x)
            is Right -> justifiedCellToString(cell.x)
            else -> cellToString(cell)
        }

    companion object {
        val boxDrawing = RowStyleTableFormatter(
            cellToString = escapeString,
            content = RowStyle(
                left = "║",
                middle = " ",
                right = "║",
                separator = "│"
            ),
            top = RowStyle(
                left = "╔",
                middle = "═",
                right = "╗",
                separator = "╤"
            ),
            bottom = RowStyle(
                left = "╚",
                middle = "═",
                right = "╝",
                separator = "╧"
            ),
            separator = RowStyle(
                left = "╟",
                middle = "─",
                right = "╢",
                separator = "┼"
            )
        )
        val plainText = RowStyleTableFormatter(
            cellToString = escapeString,
            content = RowStyle(
                left = "|",
                middle = " ",
                right = "|",
                separator = "|"
            ),
            top = RowStyle(
                left = "/",
                middle = "-",
                right = "\\",
                separator = "+"
            ),
            bottom = RowStyle(
                left = "\\",
                middle = "-",
                right = "/",
                separator = "+"
            ),
            separator = RowStyle(
                left = "+",
                middle = "-",
                right = "+",
                separator = "+"
            )
        )
        val minimal = RowStyleTableFormatter(
            cellToString = escapeString,
            content = RowStyle(
                left = "",
                middle = " ",
                right = "",
                separator = " "
            ),
            top = null,
            bottom = null,
            separator = null
        )
    }
}
