package com.seanshubin.factor.analysis.matrix

class ListMatrix(private val rows: List<List<Double>>) : Matrix {
    companion object {
        val empty: Matrix = ListMatrix(emptyList())
    }

    init {
        requireAllRowsSameSize()
    }

    private fun constructor() = ListMatrix(emptyList())
    private fun constructor(cells: List<Double>, columnCount: Int) =
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
    override fun get(i: Int, j: Int): Double = rows[i][j]
    override fun addRow(vararg cells: Double): Matrix = ListMatrix(rows + listOf(cells.toList()))

    override fun addColumn(vararg cells: Double): Matrix {
        fun appendCell(row: List<Double>, cell: Double): List<Double> = row + cell
        fun appendCell(rowAndCell: Pair<List<Double>, Double>): List<Double> =
            appendCell(rowAndCell.first, rowAndCell.second)

        val baseRows = rows.ifEmpty {
            (1..cells.size).map { emptyList() }
        }
        return ListMatrix(baseRows.zip(cells.toList()).map(::appendCell))
    }

    override fun binaryOperation(that: Matrix, operation: (Double, Double) -> Double): Matrix {
        val cells: List<Double> = toList().zip(that.toList()).map { (a, b) -> operation(a, b) }
        val result = constructor(cells, columnCount)
        return result
    }

    override fun crossOperation(that: Matrix, operation1: (Double, Double) -> Double, operation2: (Double, Double) -> Double): Matrix {
        if(columnCount != that.rowCount) {
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

    private fun dotProduct(listA:List<Double>,
                           listB:List<Double>,
                           operation1: (Double, Double) -> Double,
                           operation2: (Double, Double) -> Double):Double {
        return listA.zip(listB).map { (a,b )-> operation2(a,b)}.fold(0.0, operation1)
    }

    override fun toString(): String = toLines().joinToString("\n")
    override fun equals(other: Any?): Boolean =
        when (other) {
            is Matrix -> this.toRows() == other.toRows()
            else -> false
        }

    override fun hashCode(): Int = rows.hashCode()

}