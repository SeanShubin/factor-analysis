package com.seanshubin.factor.analysis.matrix

interface Matrix {
    val rowCount: Int
    val columnCount: Int
    operator fun get(i: Int, j: Int): Double
    fun binaryOperation(that: Matrix, operation: (Double, Double) -> Double): Matrix
    fun addRow(vararg cells: Double): Matrix
    fun addColumn(vararg cells: Double): Matrix
    fun toList(): List<Double>
    fun toRows(): List<List<Double>>

    fun addRow(vararg cells: Int): Matrix = addRow(*cells.map { it.toDouble() }.toDoubleArray())
    fun addColumn(vararg cells: Int): Matrix = addColumn(*cells.map { it.toDouble() }.toDoubleArray())

    operator fun plus(that: Matrix): Matrix
    operator fun times(that: Matrix): Matrix

    val order: Order get() = Order(rowCount, columnCount)

    fun toLines(): List<String>
}
