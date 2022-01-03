package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.format.RowStyleTableFormatter
import com.seanshubin.factor.analysis.ratio.Ratio
import com.seanshubin.factor.analysis.ratio.Ratio.Companion.ONE
import com.seanshubin.factor.analysis.ratio.Ratio.Companion.ZERO
import com.seanshubin.factor.analysis.ratio.Ratio.Companion.div
import com.seanshubin.factor.analysis.ratio.Ratio.Companion.sum
import com.seanshubin.factor.analysis.ratio.Ratio.Companion.toRatio
import com.seanshubin.factor.analysis.ratio.Ratio.Companion.toRatioArray

interface Matrix {
    val rowCount: Int
    val columnCount: Int
    operator fun get(i: Int, j: Int): Ratio
    fun toEmpty(): Matrix
    fun addRow(vararg cells: Ratio): Matrix
    fun addColumn(vararg cells: Ratio): Matrix
    fun fromRows(rows: List<List<Ratio>>): Matrix
    fun replaceRow(rowIndex: Int, cells: List<Ratio>): Matrix
    fun swapRows(rowIndexA: Int, rowIndexB: Int): Matrix
    fun unaryOperation(operation: (Ratio) -> Ratio): Matrix
    fun binaryOperation(that: Matrix, operation: (Ratio, Ratio) -> Ratio): Matrix
    fun crossOperation(
        that: Matrix,
        operation1: (Ratio, Ratio) -> Ratio,
        operation2: (Ratio, Ratio) -> Ratio
    ): Matrix

    fun toList(): List<Ratio> {
        val list = (0 until rowCount).flatMap { rowIndex ->
            (0 until columnCount).map { columnIndex ->
                this[rowIndex, columnIndex]
            }
        }
        return list
    }

    fun toRows(): List<List<Ratio>> {
        val rows = (0 until rowCount).map { rowIndex ->
            (0 until columnCount).map { columnIndex ->
                this[rowIndex, columnIndex]
            }
        }
        return rows
    }

    fun toLines(): List<String> = RowStyleTableFormatter.minimal.format(toRows())
    fun addRow(vararg cells: Int): Matrix = addRow(*cells.map { it.toRatio() }.toRatioArray())
    fun addColumn(vararg cells: Int): Matrix = addColumn(*cells.map { it.toRatio() }.toRatioArray())
    operator fun plus(that: Matrix): Matrix = binaryOperation(that) { a, b -> a + b }
    operator fun times(that: Matrix): Matrix = crossOperation(that, { a, b -> a + b }, { a, b -> a * b })
    operator fun times(scalar: Ratio): Matrix = unaryOperation { it * scalar }
    operator fun times(scalar: Int): Matrix = times(scalar.toRatio())
    operator fun div(scalar: Ratio): Matrix = times(1 / scalar)
    operator fun div(scalar: Int): Matrix = div(scalar.toRatio())
    fun getRow(rowIndex: Int): List<Ratio> = (0 until columnCount).map { this[rowIndex, it] }
    fun getColumn(columnIndex: Int): List<Ratio> = (0 until rowCount).map { this[it, columnIndex] }
    fun multiplyRowBy(rowIndex: Int, x: Ratio): Matrix {
        val newRow = (0 until columnCount).map { this[rowIndex, it] * x }
        return replaceRow(rowIndex, newRow)
    }

    fun multiplyRowBy(rowIndex: Int, x: Int): Matrix = multiplyRowBy(rowIndex, x.toRatio())
    fun divideRowBy(rowIndex: Int, x: Ratio): Matrix = multiplyRowBy(rowIndex, 1 / x)
    fun divideRowBy(rowIndex: Int, x: Int): Matrix = divideRowBy(rowIndex, x.toRatio())
    fun addMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Ratio): Matrix {
        val newRow = (0 until columnCount).map { this[targetRowIndex, it] + multiple * this[sourceRowIndex, it] }
        return replaceRow(targetRowIndex, newRow)
    }

    fun addMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Int): Matrix =
        addMultipleOfRow(targetRowIndex, sourceRowIndex, multiple.toRatio())

    fun subtractMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Ratio): Matrix =
        addMultipleOfRow(targetRowIndex, sourceRowIndex, -multiple)

    fun subtractMultipleOfRow(targetRowIndex: Int, sourceRowIndex: Int, multiple: Int): Matrix =
        subtractMultipleOfRow(targetRowIndex, sourceRowIndex, multiple.toRatio())

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
                        rowIndex != columnIndex && this[rowIndex, columnIndex] == ZERO
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
            current.addColumn(*getColumn(columnIndex).toRatioArray())
        }
        return result
    }

    fun addColumns(that: Matrix): Matrix {
        val result = (0 until that.columnCount).fold(this) { current: Matrix, columnIndex: Int ->
            val column = that.getColumn(columnIndex).toRatioArray()
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

    fun determinant(): Ratio {
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
        return if (determinant == ZERO) null else adjugate() / determinant
    }

    private fun reducedRowEchelonForm(rowIndex: Int, columnIndex: Int): Matrix =
        if (columnIndex < columnCount && rowIndex < rowCount) {
            if (columnAllZeroes(columnIndex)) {
                reducedRowEchelonForm(rowIndex, columnIndex + 1)
            } else {
                val a = moveRowsWithZeroToBottom(rowIndex, columnIndex)
                if (a[rowIndex, columnIndex] == ZERO) {
                    a.reducedRowEchelonForm(rowIndex, columnIndex + 1)
                } else {
                    val b = a.makeLeadingCoefficientOne(rowIndex, columnIndex)
                    val c = b.zeroOutOtherRows(rowIndex, columnIndex)
                    val d = c.reducedRowEchelonForm(rowIndex + 1, columnIndex + 1)
                    d
                }
            }
        } else {
            this
        }

    private fun columnAllZeroes(columnIndex: Int): Boolean = (0 until rowCount).all { this[it, columnIndex] == ZERO }
    private fun moveRowsWithZeroToBottom(rowIndex: Int, columnIndex: Int): Matrix {
        val result = if (rowIndex >= rowCount) {
            this
        } else if (this[rowIndex, columnIndex] == ZERO) {
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
        val resultIndex = (rowIndex until rowCount).indexOfFirst { this[it, columnIndex] != ZERO }
        return if (resultIndex == -1) null else resultIndex + rowIndex
    }
}
