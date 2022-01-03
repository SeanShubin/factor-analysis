package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.ratio.Ratio
import com.seanshubin.factor.analysis.ratio.Ratio.Companion.ZERO

class ListMatrix constructor(private val rows: List<List<Ratio>>) : Matrix {
    companion object {
        val empty: Matrix = ListMatrix(emptyList())
    }

    init {
        requireAllRowsSameSize()
    }

    override fun fromRows(rows: List<List<Ratio>>): Matrix = ListMatrix(rows)
    override fun toEmpty(): Matrix = empty

    private fun constructor() = ListMatrix(emptyList())
    private fun constructor(cells: List<Ratio>, columnCount: Int) =
        if (cells.isEmpty() && columnCount == 0) constructor()
        else ListMatrix(cells.chunked(columnCount))

    private fun requireAllRowsSameSize() {
        if (rows.isEmpty()) return
        val rowSizes = rows.map { it.size }
        val firstRowSize = rowSizes[0]
        val allRowsSameSize = rowSizes.all { it == firstRowSize }
        if (!allRowsSameSize) throw RuntimeException("All rows are required to be the same size:\n$this")
    }

    override val rowCount: Int get() = rows.size
    override val columnCount: Int get() = if (rowCount == 0) 0 else rows[0].size
    override fun get(i: Int, j: Int): Ratio = rows[i][j]
    override fun addRow(vararg cells: Ratio): Matrix = ListMatrix(rows + listOf(cells.toList()))

    override fun addColumn(vararg cells: Ratio): Matrix {
        fun appendCell(row: List<Ratio>, cell: Ratio): List<Ratio> = row + cell
        fun appendCell(rowAndCell: Pair<List<Ratio>, Ratio>): List<Ratio> =
            appendCell(rowAndCell.first, rowAndCell.second)

        val baseRows = rows.ifEmpty {
            (1..cells.size).map { emptyList() }
        }
        return ListMatrix(baseRows.zip(cells.toList()).map(::appendCell))
    }

    override fun swapRows(rowIndexA: Int, rowIndexB: Int): Matrix {
        val rowA = rows[rowIndexA]
        val rowB = rows[rowIndexB]
        return replaceRow(rowIndexA, rowB).replaceRow(rowIndexB, rowA)
    }

    override fun unaryOperation(operation: (Ratio) -> Ratio): Matrix {
        val cells: List<Ratio> = toList().map(operation)
        val result = constructor(cells, columnCount)
        return result
    }

    override fun binaryOperation(that: Matrix, operation: (Ratio, Ratio) -> Ratio): Matrix {
        val cells: List<Ratio> = toList().zip(that.toList()).map { (a, b) -> operation(a, b) }
        val result = constructor(cells, columnCount)
        return result
    }

    override fun crossOperation(
        that: Matrix,
        operation1: (Ratio, Ratio) -> Ratio,
        operation2: (Ratio, Ratio) -> Ratio
    ): Matrix {
        if (columnCount != that.rowCount) {
            throw RuntimeException(
                """Column count ($columnCount) of this matrix does not match the row count (${that.rowCount} of that matrix)
                    |this matrix
                    |$this
                    |that matrix
                    |$that
                """.trimMargin()
            )
        }
        val rows = (0 until rowCount).map { rowIndex ->
            (0 until that.columnCount).map { columnIndex ->
                val row = getRow(rowIndex)
                val column = that.getColumn(columnIndex)
                dotProduct(row, column, operation1, operation2)
            }
        }
        return ListMatrix(rows)
    }

    override fun replaceRow(rowIndex: Int, cells: List<Ratio>): Matrix {
        return ListMatrix(rows.take(rowIndex) + listOf(cells) + rows.drop(rowIndex + 1))
    }

    private fun dotProduct(
        listA: List<Ratio>,
        listB: List<Ratio>,
        operation1: (Ratio, Ratio) -> Ratio,
        operation2: (Ratio, Ratio) -> Ratio
    ): Ratio {
        return listA.zip(listB).map { (a, b) -> operation2(a, b) }.fold(ZERO, operation1)
    }

    override fun toString(): String = toLines().joinToString("\n")
    override fun equals(other: Any?): Boolean =
        when (other) {
            is Matrix -> this.toRows() == other.toRows()
            else -> false
        }

    override fun hashCode(): Int = rows.hashCode()

}
