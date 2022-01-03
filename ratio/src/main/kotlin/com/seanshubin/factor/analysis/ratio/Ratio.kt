package com.seanshubin.factor.analysis.ratio

data class Ratio(val numerator: Int, val denominator: Int) : Comparable<Ratio> {
    operator fun plus(that: Ratio): Ratio {
        val lcm = leastCommonMultiple(denominator, that.denominator)
        return Ratio(numerator * lcm / denominator + that.numerator * lcm / that.denominator, lcm).simplify()
    }

    operator fun minus(that: Ratio): Ratio = (this + -that).simplify()
    operator fun times(that: Ratio): Ratio = Ratio(numerator * that.numerator, denominator * that.denominator).simplify()
    operator fun div(that: Ratio): Ratio = (this * that.recriprocal()).simplify()
    operator fun unaryMinus(): Ratio = Ratio(-numerator, denominator).simplify()
    fun recriprocal(): Ratio = Ratio(denominator, numerator).simplify()
    fun withDenominator(newDenominator: Int): Ratio = Ratio(numerator * newDenominator / denominator, newDenominator)
    override fun compareTo(that: Ratio): Int {
        val lcm = leastCommonMultiple(denominator, that.denominator)
        return (numerator * lcm / denominator).compareTo(that.numerator * lcm / that.denominator)
    }

    override fun toString(): String = "$numerator/$denominator"

    fun simplify(): Ratio = simplifyFactor().simplifySign()
    private fun simplifySign(): Ratio =
        if (denominator < 0) Ratio(-numerator, -denominator)
        else this

    private fun simplifyFactor(): Ratio {
        val gcf = greatestCommonFactor(numerator, denominator)
        return Ratio(numerator / gcf, denominator / gcf)
    }

    companion object {
        fun greatestCommonFactor(a: Int, b: Int): Int =
            if (b == 0) a
            else greatestCommonFactor(b, a % b)

        fun leastCommonMultiple(a: Int, b: Int): Int =
            if (a == 0 && b == 0) 0
            else a * b / greatestCommonFactor(a, b)

        val regex = Regex("""(-?\d+)/(-?\d+)""")

        fun parse(s: String): Ratio {
            val matchResult = regex.matchEntire(s)
            if (matchResult == null) throw RuntimeException("Value '$s' could did not match expression $regex")
            val numerator = matchResult.groupValues[1].toInt()
            val denominator = matchResult.groupValues[2].toInt()
            return Ratio(numerator, denominator).simplify()
        }
    }

    val toDouble: Double get() = numerator.toDouble() / denominator.toDouble()
}
