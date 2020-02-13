package com.seanshubin.factor.analysis.matrix

import kotlin.test.Test
import kotlin.test.assertEquals

class MatrixTest {
    @Test
    fun createEmpty() {
        // when
        val matrix = Matrix<Int>()

        // then
        assertEquals(matrix.size.row, 0)
        assertEquals(matrix.size.col, 0)
    }

    @Test
    fun createWithSizeAndValue() {
        // when
        val matrix = Matrix(Size(2, 3), 123)

        // then
        assertEquals(2, matrix.size.row)
        assertEquals(3, matrix.size.col)
        assertEquals(123, matrix[0, 0])
        assertEquals(123, matrix[1, 2])
    }

    @Test
    fun createWithSizeAndValueFunction() {
        // when
        fun computeValue(i: Int, j: Int): String = "$i-$j"

        val matrix = Matrix(Size(2, 3), ::computeValue)

        // then
        assertEquals(2, matrix.size.row)
        assertEquals(3, matrix.size.col)
        assertEquals("0-0", matrix[0, 0])
        assertEquals("1-2", matrix[1, 2])
    }

    @Test
    fun createFromRows() {
        // when
        val row1 = listOf(1, 2, 3)
        val row2 = listOf(4, 5, 6)
        val rows = listOf(row1, row2)
        val matrix = Matrix(rows)

        // then
        assertEquals(2, matrix.size.row)
        assertEquals(3, matrix.size.col)
        assertEquals(1, matrix[0, 0])
        assertEquals(6, matrix[1, 2])
    }

    @Test
    fun transpose() {
        // given
        val original = Matrix(
                listOf(
                        listOf(1, 2, 3),
                        listOf(4, 5, 6)
                )
        )
        // when
        val matrix = original.transpose()

        // then
        assertEquals(3, matrix.size.row)
        assertEquals(2, matrix.size.col)
        assertEquals(1, matrix[0, 0])
        assertEquals(6, matrix[2, 1])
    }

    @Test
    fun row() {
        // when
        val matrix = Matrix.row(1, 2, 3)

        // then
        assertEquals(1, matrix.size.row)
        assertEquals(3, matrix.size.col)
        assertEquals(1, matrix[0, 0])
        assertEquals(3, matrix[0, 2])
    }

    @Test
    fun col() {
        // when
        val matrix = Matrix.col(1, 2, 3)

        // then
        assertEquals(3, matrix.size.row)
        assertEquals(1, matrix.size.col)
        assertEquals(1, matrix[0, 0])
        assertEquals(3, matrix[2, 0])
    }

    @Test
    fun add() {
        // given
        val a = Matrix(listOf(listOf(1, 3, 1), listOf(1, 0, 0)))
        val b = Matrix(listOf(listOf(0, 0, 5), listOf(7, 5, 0)))
        val expected = Matrix(listOf(listOf(1, 3, 6), listOf(8, 5, 0)))

        // when
        val actual = a + b

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun scalarMultiply() {
        // given
        val a = Matrix(listOf(listOf(1, 2, 3), listOf(4, 5, 6)))
        val expected = Matrix(listOf(listOf(2, 4, 6), listOf(8, 10, 12)))

        // when
        val actual = a * 2

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun rowVector() {
        // given
        val a = Matrix(listOf(listOf(1, 2, 3), listOf(4, 5, 6)))

        // when
        val actual = a.rowVector(1)

        // then
        assertEquals(listOf(4, 5, 6), actual)
    }

    @Test
    fun colVector() {
        // given
        val a = Matrix(listOf(listOf(1, 2, 3), listOf(4, 5, 6)))

        // when
        val actual = a.colVector(1)

        // then
        assertEquals(listOf(2, 5), actual)
    }

    @Test
    fun dotProduct() {
        // given
        val a = listOf(1, 2, 3)
        val b = listOf(4, 5, 6)

        // when
        val actual = dotProduct(a, b)

        // then
        assertEquals(32, actual)
    }

    @Test
    fun matrixMultiply() {
        // given
        val a = Matrix(listOf(listOf(1, 2, 3), listOf(4, 5, 6)))
        val b = Matrix(listOf(listOf(7, 8), listOf(9, 10), listOf(11, 12)))
        val expected = Matrix(listOf(listOf(58, 64), listOf(139, 154)))

        // when
        val actual = a * b

        // then
        assertEquals(expected, actual)
    }
}
