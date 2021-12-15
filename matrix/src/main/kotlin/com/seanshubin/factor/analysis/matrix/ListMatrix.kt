package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.format.RowStyleTableFormatter

class ListMatrix(private val rows: List<List<Double>>) : Matrix {
    init {
        requireAllRowsSameSize()
    }

    private fun constructor() = ListMatrix(emptyList())
    private fun constructor(rows: List<List<Double>>) = ListMatrix(rows)
    private fun constructor(cells: List<Double>, columnCount: Int) =
        if (cells.isEmpty() && columnCount == 0) constructor()
        else ListMatrix(cells.chunked(columnCount))

    override fun get(i: Int, j: Int): Double = rows[i][j]

    override fun addRow(vararg cells: Double): Matrix =
        ListMatrix(rows + listOf(cells.toList()))

    override fun addColumn(vararg cells: Double): Matrix {
        val baseRows = rows.ifEmpty {
            (1..cells.size).map { emptyList() }
        }
        return ListMatrix(baseRows.zip(cells.toList()).map(::appendCell))
    }

    private fun appendCell(rowAndCell: Pair<List<Double>, Double>): List<Double> =
        appendCell(rowAndCell.first, rowAndCell.second)

    private fun appendCell(row: List<Double>, cell: Double): List<Double> = row + cell

    override fun plus(that: Matrix): Matrix = binaryOperation(that) { a, b -> a + b }

    override fun times(that: Matrix): Matrix {
        TODO("not implemented")
    }

    override fun binaryOperation(that: Matrix, operation: (Double, Double) -> Double): Matrix {
        val cells: List<Double> = toList().zip(that.toList()).map { (a, b) -> operation(a, b) }
        val result = constructor(cells, columnCount)
        return result
    }

    override val order: Order get() = Order(rowCount, columnCount)

    override val rowCount: Int get() = rows.size

    override val columnCount: Int get() = if (rowCount == 0) 0 else rows[0].size

    override fun toList(): List<Double> = rows.flatten()
    override fun toRows(): List<List<Double>> {
        return rows
    }

    private fun requireAllRowsSameSize() {
        if (rows.isEmpty()) return
        val rowSizes = rows.map { it.size }
        val firstRowSize = rowSizes[0]
        val allRowsSameSize = rowSizes.all { it == firstRowSize }
        if (!allRowsSameSize) throw RuntimeException("All rows are required to be the same size:\n$this")
    }

    companion object {
        val empty: Matrix = ListMatrix(emptyList())
    }

    override fun toLines(): List<String> = RowStyleTableFormatter.minimal.format(rows)
    override fun toString(): String = toLines().joinToString("\n")
    override fun equals(other: Any?): Boolean =
        when (other) {
            is Matrix -> this.toRows() == other.toRows()
            else -> false
        }

    override fun hashCode(): Int = rows.hashCode()
}
