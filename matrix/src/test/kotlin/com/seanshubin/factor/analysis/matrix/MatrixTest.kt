package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.ratio.Ratio
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

    // https://www.usna.edu/Users/math/uhan/sm286a/rref.pdf
    @Test
    fun reducedRowEchelonFormOperations() {
        val builder: Matrix = ListMatrix.empty
        val a = builder.addRow(2, 8, 4, 2).addRow(2, 5, 1, 5).addRow(4, 10, -1, 1)
        val b = a.divideRowBy(0, 2)
        val c = b.subtractMultipleOfRow(1, 0, 2)
        val d = c.subtractMultipleOfRow(2, 0, 4)
        val e = d.divideRowBy(1, -3)
        val f = e.addMultipleOfRow(2, 1, 6)
        val g = f.divideRowBy(2, -3)
        val h = g.subtractMultipleOfRow(0, 1, 4)
        val i = h.subtractMultipleOfRow(1, 2, 1)
        val j = i.addMultipleOfRow(0, 2, 2)
        val expected = builder.addRow(1, 0, 0, 11).addRow(0, 1, 0, -4).addRow(0, 0, 1, 3)
        assertEquals(expected, j)
    }

    @Test
    fun reducedRowEchelonForm() {
        val builder: Matrix = ListMatrix.empty
        val original = builder.addRow(2, 8, 4, 2).addRow(2, 5, 1, 5).addRow(4, 10, -1, 1)
        val actual = original.reducedRowEchelonForm()
        val expected = builder.addRow(1, 0, 0, 11).addRow(0, 1, 0, -4).addRow(0, 0, 1, 3)
        assertEquals(expected, actual)
    }

    @Test
    fun reducedRowEchelonFormSwapZeroWithNonZeroLeadingEntry() {
        val builder: Matrix = ListMatrix.empty
        val original = builder.addRow(0, 1).addRow(1, 0)
        val actual = original.reducedRowEchelonForm()
        val expected = builder.addRow(1, 0).addRow(0, 1)
        assertEquals(expected, actual)
    }

    @Test
    fun reducedRowEchelonFormForEmpty() {
        val original = ListMatrix.empty
        val actual = original.reducedRowEchelonForm()
        assertEquals(original, actual)
    }

    @Test
    fun reducedRowEchelonFormSwapZeroWithNonZeroLeadingEntryAfterFirst() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(1, 0, 0)
            .addRow(0, 0, 1)
            .addRow(0, 1, 0)
        val actual = original.reducedRowEchelonForm()
        val expected = builder
            .addRow(1, 0, 0)
            .addRow(0, 1, 0)
            .addRow(0, 0, 1)
        assertEquals(expected, actual)
    }

    @Test
    fun inverse() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(1, 1, -3)
            .addRow(2, 5, 1)
            .addRow(1, 3, 2)
        val expected = builder
            .addRow(7, -11, 16)
            .addRow(-3, 5, -7)
            .addRow(1, -2, 3)
        val actual = original.inverse()
        assertEquals(expected, actual)
    }

    @Test
    fun noInverse() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(3, 4)
            .addRow(6, 8)
        val expected = null
        val actual = original.inverse()
        assertEquals(expected, actual)
    }

    @Test
    fun transpose() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(1, -1, 2, -1, -1)
            .addRow(-2, 1, 1, 1, -1)
            .addRow(3, -1, 2, -2, -2)
        val expected = builder
            .addRow(1, -2, 3)
            .addRow(-1, 1, -1)
            .addRow(2, 1, 2)
            .addRow(-1, 1, -2)
            .addRow(-1, -1, -2)
        val actual = original.transpose()
        assertEquals(expected, actual)
    }

    @Test
    fun covariance() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(1, -1, 2, -1, -1)
            .addRow(-2, 1, 1, 1, -1)
            .addRow(3, -1, 2, -2, -2)
        val expected = builder
            .addRow(Ratio(8,5), Ratio(-1,5), Ratio(12,5))
            .addRow(Ratio(-1,5), Ratio(8,5), Ratio(-1,1))
            .addRow(Ratio(12,5), Ratio(-1,1), Ratio(22,5))
        val actual = original.covariance()
        assertEquals(expected, actual)
    }
}
