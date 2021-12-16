package com.seanshubin.factor.analysis.matrix

import kotlin.test.Test
import kotlin.test.assertEquals

class MatrixTest {
    @Test
    fun add() {
        val builder: Matrix = ListMatrix.empty
        val a: Matrix = builder.addColumn(1, 2, 3).addColumn(4, 5, 6)
        val b: Matrix = builder.addRow(6, 5).addRow(4, 3).addRow(2, 1)
        val actual: Matrix = a + b
        val expected = builder.addRow(7, 9).addRow(6, 8).addRow(5, 7)
        assertEquals(expected, actual)
    }

    @Test
    fun times() {
        val builder: Matrix = ListMatrix.empty
        val a: Matrix = builder.addColumn(1, 2, 3).addColumn(4, 5, 6)
        val b: Matrix = builder.addRow(1, 2, 3).addRow(2, 1, 2)
        val actual: Matrix = a * b
        val expected = builder.addRow(9, 6, 11).addRow(12, 9, 16).addRow(15, 12, 21)
        assertEquals(expected, actual)
    }
}
