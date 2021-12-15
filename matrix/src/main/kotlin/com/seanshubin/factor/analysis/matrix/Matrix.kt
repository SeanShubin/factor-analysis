package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.format.RowStyleTableFormatter

interface Matrix {
    val rowCount: Int
    val columnCount: Int
    operator fun get(i: Int, j: Int): Double
    fun addRow(vararg cells: Double): Matrix
    fun addColumn(vararg cells: Double): Matrix
    fun binaryOperation(that: Matrix, operation: (Double, Double) -> Double): Matrix

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
}
