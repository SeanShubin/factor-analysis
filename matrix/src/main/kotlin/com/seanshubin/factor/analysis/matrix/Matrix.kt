package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.format.RowStyleTableFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.toDuration

interface Matrix {
    val rowCount: Int
    val columnCount: Int
    operator fun get(i: Int, j: Int): Double
    fun toEmpty(): Matrix
    fun addRow(vararg cells: Double): Matrix
    fun addColumn(vararg cells: Double): Matrix
    fun fromRows(rows: List<List<Double>>): Matrix
    fun replaceRow(rowIndex: Int, cells: List<Double>): Matrix
    fun swapRows(rowIndexA: Int, rowIndexB: Int): Matrix
    fun unaryOperation(operation: (Double) -> Double): Matrix
    fun binaryOperation(that: Matrix, operation: (Double, Double) -> Double): Matrix
    fun crossOperation(
        that: Matrix,
        operation1: (Double, Double) -> Double,
        operation2: (Double, Double) -> Double
    ): Matrix

    fun toList(): List<Double> {
        val list = (0 until rowCount).flatMap { rowIndex ->
            (0 until columnCount).map { columnIndex ->
                this[rowIndex, columnIndex]
            }
        }
        return list
    }

    fun toRows(): List<List<Double>> {
        val rows = (0 until rowCount).map { rowIndex ->
            (0 until columnCount).map { columnIndex ->
                this[rowIndex, columnIndex]
            }
        }
        return rows
    }

    fun toLines(): List<String> = RowStyleTableFormatter.minimal.format(toRows())
    fun addRow(vararg cells: Int): Matrix = addRow(*cells.map { it.toDouble() }.toDoubleArray())
    fun addColumn(vararg cells: Int): Matrix = addColumn(*cells.map { it.toDouble() }.toDoubleArray())
    operator fun plus(that: Matrix): Matrix = binaryOperation(that) { a, b -> a + b }
    operator fun times(that: Matrix): Matrix = crossOperation(that, { a, b -> a + b }, { a, b -> a * b })
    operator fun times(scalar:Double):Matrix = unaryOperation { it * scalar }
    operator fun times(scalar:Int):Matrix = times(scalar.toDouble())
    operator fun div(scalar:Double):Matrix = times(1/scalar)
    operator fun div(scalar:Int):Matrix = div(scalar.toDouble())
    fun getRow(rowIndex: Int): List<Double> = (0 until columnCount).map { this[rowIndex, it] }
    fun getColumn(columnIndex: Int): List<Double> = (0 until rowCount).map { this[it, columnIndex] }
    fun multiplyRowBy(rowIndex: Int, x: Double): Matrix {
        val newRow = (0 until columnCount).map { this[rowIndex, it] * x }.map(::noNegativeZero)
        return replaceRow(rowIndex, newRow)
    }

    fun multiplyRowBy(rowIndex: Int, x: Int): Matrix = multiplyRowBy(rowIndex, x.toDouble())
    fun divideRowBy(rowIndex: Int, x: Double): Matrix = multiplyRowBy(rowIndex, 1 / x)
    fun divideRowBy(rowIndex: Int, x: Int): Matrix = divideRowBy(rowIndex, x.toDouble())
    fun addMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Double): Matrix {
        val newRow = (0 until columnCount).map { this[targetRowIndex, it] + multiple * this[sourceRowIndex, it] }
            .map(::noNegativeZero)
        return replaceRow(targetRowIndex, newRow)
    }

    fun addMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Int): Matrix =
        addMultipleOfRow(targetRowIndex, sourceRowIndex, multiple.toDouble())

    fun subtractMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Double): Matrix =
        addMultipleOfRow(targetRowIndex, sourceRowIndex, -multiple)

    fun subtractMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Int): Matrix =
        subtractMultipleOfRow(targetRowIndex, sourceRowIndex, multiple.toDouble())

    fun reducedRowEchelonForm(): Matrix = reducedRowEchelonForm(0, 0)
    fun inverse(): Matrix? {
        val sizedIdentity = identity(columnCount)
        val augmented = addColumns(sizedIdentity)
        val solved = augmented.reducedRowEchelonForm()
        val (maybeIdentity, solution) = solved.splitAtColumn(columnCount)
        return if (maybeIdentity.isIdentity()) {
            solution
        } else {
            null
        }
    }

    fun identity(size: Int): Matrix {
        val rows = (0 until size).map { rowIndex ->
            (0 until size).map { columnIndex ->
                if (rowIndex == columnIndex) 1.0 else 0.0
            }
        }
        return fromRows(rows)
    }

    fun isIdentity(): Boolean {
        if (rowCount != columnCount) return false
        return (0 until rowCount).all { rowIndex ->
            (0 until columnCount).all { columnIndex ->
                rowIndex == columnIndex && this[rowIndex, columnIndex] == 1.0 ||
                        rowIndex != columnIndex && this[rowIndex, columnIndex] == 0.0
            }
        }
    }

    fun splitAtColumn(columnIndex: Int): Pair<Matrix, Matrix> {
        val first = columnRange(0, columnIndex)
        val second = columnRange(columnIndex, columnCount)
        return Pair(first, second)
    }

    fun columnRange(begin: Int, end: Int): Matrix {
        val result = (begin until end).fold(toEmpty()) { current: Matrix, columnIndex: Int ->
            current.addColumn(*getColumn(columnIndex).toDoubleArray())
        }
        return result
    }

    fun addColumns(that: Matrix): Matrix {
        val result = (0 until that.columnCount).fold(this) { current: Matrix, columnIndex: Int ->
            val column: DoubleArray = that.getColumn(columnIndex).toDoubleArray()
            current.addColumn(*column)
        }
        return result
    }

    fun round(scale: Int): Matrix {
        val roundFunction = createRoundFunction(scale)
        val result = unaryOperation(roundFunction)
        return result
    }

    fun transpose(): Matrix {
        val result = (0 until columnCount).map { columnIndex ->
            (0 until rowCount).map { rowIndex ->
                this[rowIndex, columnIndex]
            }
        }
        return fromRows(result)
    }

    fun covariance():Matrix {
        val result = this * this.transpose() / columnCount
        return result
    }

    companion object {
        fun createRoundFunction(scale: Int): (Double) -> Double {
            fun roundFunction(x: Double): Double {
                return BigDecimal.valueOf(x).setScale(scale, RoundingMode.HALF_UP).toDouble()
            }
            return ::roundFunction
        }
    }

    private fun reducedRowEchelonForm(rowIndex: Int, columnIndex: Int): Matrix {
        val result = if (columnIndex < columnCount && rowIndex < rowCount) {
            if (columnAllZeroes(columnIndex)) {
                reducedRowEchelonForm(rowIndex, columnIndex + 1)
            } else {
                val a = moveRowsWithZeroToBottom(rowIndex, columnIndex)
                val b = a.makeLeadingCoefficientOne(rowIndex, columnIndex)
                val c = b.zeroOutOtherRows(rowIndex, columnIndex)
                val d = c.reducedRowEchelonForm(rowIndex + 1, columnIndex + 1)
                return d
            }
        } else {
            this
        }
        return result
    }

    private fun columnAllZeroes(columnIndex: Int): Boolean = (0 until rowCount).all { this[it, columnIndex] == 0.0 }
    private fun moveRowsWithZeroToBottom(rowIndex: Int, columnIndex: Int): Matrix {
        val result = if (rowIndex >= rowCount) {
            this
        } else if (this[rowIndex, columnIndex] == 0.0) {
            val nonZeroRowIndex = findNonZeroRowIndex(rowIndex + 1, columnIndex)
            if (nonZeroRowIndex == null) {
                this
            } else {
                val a = swapRows(rowIndex, nonZeroRowIndex)
                val b = a.moveRowsWithZeroToBottom(rowIndex + 1, columnIndex)
                b
            }
        } else {
            moveRowsWithZeroToBottom(rowIndex + 1, columnIndex)
        }
        return result
    }

    private fun makeLeadingCoefficientOne(rowIndex: Int, columnIndex: Int): Matrix {
        val result = divideRowBy(rowIndex, this[rowIndex, columnIndex])
        return result
    }

    private fun zeroOutOtherRows(rowIndex: Int, columnIndex: Int): Matrix {
        var result = this
        (0 until rowCount).forEach {
            if (it != rowIndex) {
                val multiple = this[it, columnIndex] / this[rowIndex, columnIndex]
                result = result.subtractMultipleOfRow(it, rowIndex, multiple)
            }
        }
        return result
    }

    private fun findNonZeroRowIndex(rowIndex: Int, columnIndex: Int): Int? {
        val resultIndex = (rowIndex until rowCount).indexOfFirst { this[it, columnIndex] != 0.0 }
        return if (resultIndex == -1) null else resultIndex + rowIndex
    }

    private fun noNegativeZero(x: Double): Double = if (x == -0.0) 0.0 else x
}
