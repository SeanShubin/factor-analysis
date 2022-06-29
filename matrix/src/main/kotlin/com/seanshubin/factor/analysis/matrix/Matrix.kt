package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.format.RowStyleTableFormatter
import kotlin.math.abs

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
    val size:List<Int> get() = listOf(rowCount, columnCount)

    fun fromRow(row: List<Double>): Matrix = fromRows(listOf(row))
    fun fromColumn(column: List<Double>): Matrix = fromRow(column).transpose()

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
    operator fun minus(that: Matrix): Matrix = binaryOperation(that) { a, b -> a - b }
    operator fun times(that: Matrix): Matrix = crossOperation(that, { a, b -> a + b }, { a, b -> a * b })
    operator fun times(scalar: Double): Matrix = unaryOperation { it * scalar }
    operator fun times(scalar: Int): Matrix = times(scalar.toDouble())
    operator fun div(scalar: Double): Matrix = times(1 / scalar)
    operator fun div(scalar: Int): Matrix = div(scalar.toDouble())
    fun getRowAsList(rowIndex: Int): List<Double> = (0 until columnCount).map { this[rowIndex, it] }
    fun getColumnAsList(columnIndex: Int): List<Double> = (0 until rowCount).map { this[it, columnIndex] }
    fun getRowAsMatrix(rowIndex: Int): Matrix = fromRow(getRowAsList(rowIndex))
    fun getColumnAsMatrix(columnIndex: Int): Matrix = fromColumn(getColumnAsList(columnIndex))
    fun multiplyRowBy(rowIndex: Int, x: Double): Matrix {
        val newRow = (0 until columnCount).map { multiply(this[rowIndex, it], x) }
        return replaceRow(rowIndex, newRow)
    }

    fun multiplyRowBy(rowIndex: Int, x: Int): Matrix = multiplyRowBy(rowIndex, x.toDouble())
    fun divideRowBy(rowIndex: Int, x: Double): Matrix = multiplyRowBy(rowIndex, 1 / x)
    fun divideRowBy(rowIndex: Int, x: Int): Matrix = divideRowBy(rowIndex, x.toDouble())
    fun addMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Double): Matrix {
        val newRow = (0 until columnCount).map { this[targetRowIndex, it] + multiple * this[sourceRowIndex, it] }
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
                if (rowIndex == columnIndex) ONE else ZERO
            }
        }
        return fromRows(rows)
    }

    fun isIdentity(): Boolean {
        if (rowCount != columnCount) return false
        return (0 until rowCount).all { rowIndex ->
            (0 until columnCount).all { columnIndex ->
                rowIndex == columnIndex && this[rowIndex, columnIndex] == ONE ||
                        rowIndex != columnIndex && closeToZero(this[rowIndex, columnIndex])
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
            current.addColumn(*getColumnAsList(columnIndex).toDoubleArray())
        }
        return result
    }

    fun addColumns(that: Matrix): Matrix {
        val result = (0 until that.columnCount).fold(this) { current: Matrix, columnIndex: Int ->
            val column = that.getColumnAsList(columnIndex).toDoubleArray()
            current.addColumn(*column)
        }
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

    fun covariance(): Matrix {
        val result = this * this.transpose() / columnCount
        return result
    }

    fun resizeToColumns(columnCount: Int): Matrix {
        return ListMatrix(toList().chunked(columnCount))
    }

    fun isSquare(): Boolean = rowCount == columnCount

    fun determinant(): Double {
        require(isSquare()) {
            "Determinant only applies to a square matrix"
        }
        if (rowCount == 1) return this[0, 0]
        return (0 until rowCount).map {
            val sign = if (it % 2 == 0) 1 else -1
            this[0, it] * minor(0, it).determinant() * sign
        }.sum()
    }

    fun minor(rowIndex: Int, columnIndex: Int): Matrix {
        val rows = (0 until rowCount).filter { rowIndex != it }.map { r ->
            (0 until columnCount).filter { columnIndex != it }.map { c ->
                this[r, c]
            }
        }
        return fromRows(rows)
    }

    fun cofactor(): Matrix {
        val rows = (0 until rowCount).map { rowIndex ->
            (0 until columnCount).map { columnIndex ->
                val sign = if ((rowIndex * rowCount + columnIndex) % 2 == 0) 1 else -1
                this.minor(rowIndex, columnIndex).determinant() * sign
            }
        }
        return fromRows(rows)
    }

    fun adjugate(): Matrix = cofactor().transpose()

    fun inverseCramersRule(): Matrix? {
        val determinant = determinant()
        return if (closeToZero(determinant)) null else adjugate() / determinant
    }

    fun correlationCoefficients():Matrix{
        val rows = (0 until columnCount).map { xIndex ->
            val xs = getColumnAsList(xIndex)
            val sumX = xs.sum()
            val sumXSquared = xs.sumSquares()
            (0 until columnCount).map { yIndex ->
                val ys = getColumnAsList(yIndex)
                val sumY = ys.sum()
                val sumYSquared = ys.sumSquares()
                val xy = xs * ys
                val sumXy = xy.sum()
                val numerator = rowCount * sumXy - sumX * sumY
                val denominator = Math.sqrt(
                    (rowCount * sumXSquared- sumX * sumX) *
                    (rowCount * sumYSquared- sumY * sumY)
                )
                val correlationCoefficient = numerator / denominator
                correlationCoefficient
            }
        }
        return fromRows(rows)
    }

    private fun List<Double>.sumSquares() = sumOf { it * it }
    private operator fun List<Double>.times(that:List<Double>):List<Double> {
        require(this.size == that.size) {
            "lists must be same size, got ${this.size} and ${that.size}"
        }
        return indices.map { this[it] * that[it] }
    }


    private fun reducedRowEchelonForm(rowIndex: Int, columnIndex: Int): Matrix =
        if (columnIndex < columnCount && rowIndex < rowCount) {
            val a = moveRowsWithZeroToBottom(rowIndex, columnIndex)
            if (closeToZero(a[rowIndex, columnIndex])) {
                a.reducedRowEchelonForm(rowIndex, columnIndex + 1)
            } else {
                val b = a.makeLeadingCoefficientOne(rowIndex, columnIndex)
                val c = b.zeroOutOtherRows(rowIndex, columnIndex)
                val d = c.reducedRowEchelonForm(rowIndex + 1, columnIndex + 1)
                d
            }
        } else {
            this
        }

    private fun columnAllZeroes(columnIndex: Int): Boolean = (0 until rowCount).all { closeToZero(this[it, columnIndex]) }
    private fun moveRowsWithZeroToBottom(rowIndex: Int, columnIndex: Int): Matrix {
        val result = if (rowIndex >= rowCount) {
            this
        } else if (closeToZero(this[rowIndex, columnIndex])) {
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
        val denominator = this[rowIndex, columnIndex]
        return divideRowBy(rowIndex, denominator)
    }

    private fun zeroOutOtherRows(rowIndex: Int, columnIndex: Int): Matrix {
        var result = this
        (0 until rowCount).forEach {
            if (it != rowIndex) {
                val numerator = this[it, columnIndex]
                val denominator = this[rowIndex, columnIndex]
                val multiple = numerator / denominator
                result = result.subtractMultipleOfRow(it, rowIndex, multiple)
            }
        }
        return result
    }

    private fun findNonZeroRowIndex(rowIndex: Int, columnIndex: Int): Int? {
        val resultIndex = (rowIndex until rowCount).indexOfFirst { !closeToZero(this[it, columnIndex]) }
        return if (resultIndex == -1) null else resultIndex + rowIndex
    }
    companion object{
        val tolerance = 1.0e-13
        val ZERO = 0.0
        val ONE = 1.0
        fun Double.noNegativeZero():Double = if(this == -0.0)  0.0 else  this
        fun multiply(x:Double, y:Double):Double = (x * y).noNegativeZero()
        fun closeToZero(x:Double):Boolean = abs(x) <= tolerance
    }
}
