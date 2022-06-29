package com.seanshubin.factor.analysis.matrix

import com.seanshubin.factor.analysis.matrix.Matrix.Companion.ONE
import com.seanshubin.factor.analysis.matrix.Matrix.Companion.ZERO
import com.seanshubin.factor.analysis.ratio.Ratio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

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
        val original = builder
            .addRow(2, 8, 4, 2)
            .addRow(2, 5, 1, 5)
            .addRow(4, 10, -1, 1)
        val actual = original.reducedRowEchelonForm()
        val expected = builder
            .addRow(1, 0, 0, 11)
            .addRow(0, 1, 0, -4)
            .addRow(0, 0, 1, 3)
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
    fun reducedRowEchelonFormForNonInvertibleMatrix() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(3, 4, 1, 0)
            .addRow(6, 8, 0, 1)
        val actual = original.reducedRowEchelonForm()
        val expected = builder
            .addRow(ONE, Ratio(4, 3).toDouble, ZERO, Ratio(1, 6).toDouble)
            .addRow(ZERO, ZERO, ONE, Ratio(-1, 2).toDouble)
        assertEquals(expected, actual)
    }

    @Test
    fun reducedRowEchelonFormLongExample() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(0, 3, -6, 6, 4, -5)
            .addRow(3, -7, 8, -5, 8, 9)
            .addRow(3, -9, 12, -9, 6, 15)
        val actual = original.reducedRowEchelonForm()
        val expected = builder
            .addRow(1, 0, -2, 3, 0, -24)
            .addRow(0, 1, -2, 2, 0, -7)
            .addRow(0, 0, 0, 0, 1, 4)
        assertMatrixEquals(expected, actual)
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
        assertMatrixEquals(expected, actual)
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
            .addRow(Ratio(8, 5).toDouble, Ratio(-1, 5).toDouble, Ratio(12, 5).toDouble)
            .addRow(Ratio(-1, 5).toDouble, Ratio(8, 5).toDouble, Ratio(-1, 1).toDouble)
            .addRow(Ratio(12, 5).toDouble, Ratio(-1, 1).toDouble, Ratio(22, 5).toDouble)
        val actual = original.covariance()
        assertMatrixEquals(expected, actual)
    }

    @Test
    fun determinant1() {
        val builder: Matrix = ListMatrix.empty
        val original = builder.addRow(123)
        val expected = Ratio(123, 1).toDouble
        val actual = original.determinant()
        assertEquals(expected, actual)
    }

    @Test
    fun determinant2() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(3, 8)
            .addRow(4, 6)
        val expected = Ratio(-14, 1).toDouble
        val actual = original.determinant()
        assertEquals(expected, actual)
    }

    @Test
    fun determinant3() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(6, 1, 1)
            .addRow(4, -2, 5)
            .addRow(2, 8, 7)
        val expected = Ratio(-306, 1).toDouble
        val actual = original.determinant()
        assertEquals(expected, actual)
    }

    @Test
    fun cofactor() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(3, 1, -6)
            .addRow(5, 2, -1)
            .addRow(-4, 3, 0)
        val expected = builder
            .addRow(3, 4, 23)
            .addRow(-18, -24, -13)
            .addRow(11, -27, 1)
        val actual = original.cofactor()
        assertEquals(expected, actual)
    }

    @Test
    fun adjugate() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(3, 1, -6)
            .addRow(5, 2, -1)
            .addRow(-4, 3, 0)
        val expected = builder
            .addRow(3, -18, 11)
            .addRow(4, -24, -27)
            .addRow(23, -13, 1)
        val actual = original.adjugate()
        assertEquals(expected, actual)
    }

    @Test
    fun inverseCramersRule() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(1, 1, -3)
            .addRow(2, 5, 1)
            .addRow(1, 3, 2)
        val expected = builder
            .addRow(7, -11, 16)
            .addRow(-3, 5, -7)
            .addRow(1, -2, 3)
        val actual = original.inverseCramersRule()
        assertEquals(expected, actual)
    }

    @Test
    fun inverseCramersRuleNoSolution() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(3, 4)
            .addRow(6, 8)
        val expected = null
        val actual = original.inverseCramersRule()
        assertEquals(expected, actual)
    }

    @Test
    fun correlationCoefficients() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(43, 99)
            .addRow(21, 65)
            .addRow(25, 79)
            .addRow(42, 75)
            .addRow(57, 87)
            .addRow(59, 81)
        val actual = original.correlationCoefficients()
        val expected = builder
            .addRow(1.0, 0.5298089018901744)
            .addRow(0.5298089018901744, 1.0)
        assertEquals(expected, actual)
    }

    @Test
    fun correlationCoefficients2() {
        val builder: Matrix = ListMatrix.empty
        val original = builder
            .addRow(6, 5, 4, 8, 6, 2)
            .addRow(8, 7, 2, 7, 5, 3)
            .addRow(9, 8, 1, 9, 7, 1)
            .addRow(5, 4, 5, 9, 7, 1)
            .addRow(4, 3, 6, 9, 7, 1)
            .addRow(7, 6, 3, 7, 5, 3)
            .addRow(3, 2, 7, 7, 5, 3)
        val actual = original.correlationCoefficients()
        val expected = builder
            .addRow(1.0, 1.0, -1.0, 0.0, 0.0, 0.0)
            .addRow(1.0, 1.0, -1.0, 0.0, 0.0, 0.0)
            .addRow(-1.0, -1.0, 1.0, 0.0, 0.0, 0.0)
            .addRow(0.0, 0.0, 0.0, 1.0, 1.0, -1.0)
            .addRow(0.0, 0.0, 0.0, 1.0, 1.0, -1.0)
            .addRow(0.0, 0.0, 0.0, -1.0, -1.0, 1.0)
        assertEquals(expected, actual)
    }

    private fun assertMatrixEquals(expected: Matrix?, actual: Matrix?) {
        if (expected == null) {
            if (actual != null) {
                fail("expected null")
            }
        } else {
            if (actual == null) {
                fail("actual was null")
            } else {
                val tolerance = Matrix.tolerance
                assertEquals(expected.size, actual.size)
                for (i in 0 until expected.rowCount) {
                    for (j in 0 until expected.columnCount) {
                        assertEquals(expected[i, j], actual[i, j], tolerance, "difference at [$i, $j]")
                    }
                }
            }
        }
    }

    // correlation coefficients
    // https://www.statisticshowto.com/probability-and-statistics/correlation-coefficient-formula/#Pearson
}
