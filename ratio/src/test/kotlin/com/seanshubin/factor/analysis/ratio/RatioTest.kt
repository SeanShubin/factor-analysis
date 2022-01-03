package com.seanshubin.factor.analysis.ratio

import kotlin.test.assertEquals

import kotlin.test.Test

class RatioTest {
    @Test
    fun add() {
        assertAdd("3/5", "1/2", "11/10")
        assertAdd("3/-5", "-1/-2", "-1/10")
    }

    @Test
    fun negate() {
        assertNegate("3/5", "-3/5")
        assertNegate("1/-5", "1/5")
    }

    @Test
    fun subtract() {
        assertSubtract("3/5", "1/2", "1/10")
        assertSubtract("1/2", "3/5", "-1/10")
        assertSubtract("-3/5", "1/2", "-11/10")
        assertSubtract("1/-2", "-3/-5", "-11/10")
    }

    fun assertAdd(a: String, b: String, expected: String) {
        val actual = (Ratio.parse(a) + Ratio.parse(b)).toString()
        assertEquals(expected, actual, "$a + $b")
    }

    fun assertSubtract(a: String, b: String, expected: String) {
        val actual = (Ratio.parse(a) - Ratio.parse(b)).toString()
        assertEquals(expected, actual, "$a + $b")
    }

    fun assertNegate(a: String, expected: String) {
        val actual = (-Ratio.parse(a)).toString()
        assertEquals(expected, actual, "-$a")
    }
}
