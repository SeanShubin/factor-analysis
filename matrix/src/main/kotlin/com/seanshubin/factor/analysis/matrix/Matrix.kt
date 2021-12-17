package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.format.RowStyleTableFormatter

interface Matrix {
    val rowCount: Int
    val columnCount: Int
    operator fun get(i: Int, j: Int): Double
    fun addRow(vararg cells: Double): Matrix
    fun addColumn(vararg cells: Double): Matrix
    fun replaceRow(rowIndex:Int, cells:List<Double>):Matrix
    fun swapRows(rowIndexA:Int, rowIndexB:Int):Matrix
    fun binaryOperation(that: Matrix, operation: (Double, Double) -> Double): Matrix
    fun crossOperation(that: Matrix, operation1: (Double, Double) -> Double, operation2: (Double, Double) -> Double): Matrix

    fun toList(): List<Double> {
        val list = mutableListOf<Double>()
        (0 until rowCount).forEach { rowIndex ->
            (0 until columnCount).forEach{ columnIndex ->
                list.add(this[rowIndex, columnIndex])
            }
        }
        return list
    }
    fun toRows(): List<List<Double>> {
        val list = mutableListOf<List<Double>>()
        (0 until rowCount).forEach { rowIndex ->
            val row = mutableListOf<Double>()
            (0 until columnCount).forEach{ columnIndex ->
                row.add(this[rowIndex, columnIndex])
            }
            list.add(row)
        }
        return list

    }
    fun toLines(): List<String> = RowStyleTableFormatter.minimal.format(toRows())
    fun addRow(vararg cells: Int): Matrix = addRow(*cells.map { it.toDouble() }.toDoubleArray())
    fun addColumn(vararg cells: Int): Matrix = addColumn(*cells.map { it.toDouble() }.toDoubleArray())
    operator fun plus(that: Matrix): Matrix = binaryOperation(that) { a, b -> a + b }
    operator fun times(that:Matrix):Matrix = crossOperation(that, {a, b -> a + b}, {a, b -> a * b})
    fun getRow(rowIndex:Int):List<Double> = (0 until columnCount).map { this[rowIndex, it] }
    fun getColumn(columnIndex:Int):List<Double> = (0 until rowCount).map{this[it,columnIndex]}
    fun multiplyRowBy(rowIndex:Int, x:Double):Matrix {
        val newRow = (0 until columnCount).map{this[rowIndex, it] * x}.map(::noNegativeZero)
        return replaceRow(rowIndex, newRow)
    }
    fun multiplyRowBy(rowIndex:Int, x:Int):Matrix = multiplyRowBy(rowIndex, x.toDouble())
    fun divideRowBy(rowIndex:Int, x:Double):Matrix = multiplyRowBy(rowIndex, 1/x)
    fun divideRowBy(rowIndex:Int, x:Int):Matrix = divideRowBy(rowIndex, x.toDouble())
    fun addMultipleOfRow(targetRowIndex:Int, sourceRowIndex:Int, multiple:Double):Matrix {
        val newRow = (0 until columnCount).map{this[targetRowIndex, it] + multiple * this[sourceRowIndex, it]}.map(::noNegativeZero)
        return replaceRow(targetRowIndex, newRow)
    }
    fun addMultipleOfRow(targetRowIndex:Int, sourceRowIndex:Int, multiple:Int):Matrix =
        addMultipleOfRow(targetRowIndex, sourceRowIndex, multiple.toDouble())
    fun subtractMultipleOfRow(targetRowIndex:Int, sourceRowIndex:Int, multiple:Double):Matrix =
        addMultipleOfRow(targetRowIndex, sourceRowIndex, -multiple)
    fun subtractMultipleOfRow(targetRowIndex:Int, sourceRowIndex:Int, multiple:Int):Matrix =
        subtractMultipleOfRow(targetRowIndex, sourceRowIndex, multiple.toDouble())
    fun reducedRowEchelonForm():Matrix {
        var newMatrix = this
        while(newMatrix.zeroAboveNonZero()){
            newMatrix = newMatrix.fixZeroAboveNonZero()
        }
        return newMatrix
    }
    private fun noNegativeZero(x:Double):Double = if(x == -0.0) 0.0 else x
    private fun zeroAboveNonZero():Boolean {
        val firstColumn = getColumn(0)
        val indexOfFirstZero = firstColumn.indexOfFirst { it == 0.0 }
        val indexOfFirstNonZero = firstColumn.indexOfFirst { it != 0.0 }
        return indexOfFirstZero < indexOfFirstNonZero
    }
    private fun fixZeroAboveNonZero():Matrix {
        val firstColumn = getColumn(0)
        val indexOfFirstZero = firstColumn.indexOfFirst { it == 0.0 }
        val indexOfFirstNonZero = firstColumn.indexOfFirst { it != 0.0 }
        return swapRows(indexOfFirstZero, indexOfFirstNonZero)
    }
}
