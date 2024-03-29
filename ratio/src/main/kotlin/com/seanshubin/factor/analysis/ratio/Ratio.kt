package com.seanshubin.factor.analysis.ratio

data class Ratio(val numerator: Int, val denominator: Int) : Comparable<Ratio> {
    init {
        require(denominator != 0){
            "Denominator must not be zero in $numerator/$denominator"
        }
    }
    operator fun plus(that: Ratio): Ratio {
        val lcm = leastCommonMultiple(denominator, that.denominator)
        return Ratio(numerator * lcm / denominator + that.numerator * lcm / that.denominator, lcm).simplify()
    }

    operator fun minus(that: Ratio): Ratio = (this + -that).simplify()
    operator fun times(that: Ratio): Ratio = Ratio(numerator * that.numerator, denominator * that.denominator).simplify()
    operator fun times(that: Int): Ratio = this * Ratio(that,1)
    operator fun div(that: Ratio): Ratio = (this * that.recriprocal()).simplify()
    operator fun unaryMinus(): Ratio = Ratio(-numerator, denominator).simplify()
    fun recriprocal(): Ratio = Ratio(denominator, numerator).simplify()
    fun withDenominator(newDenominator: Int): Ratio = Ratio(numerator * newDenominator / denominator, newDenominator)
    override fun compareTo(that: Ratio): Int {
        val lcm = leastCommonMultiple(denominator, that.denominator)
        return (numerator * lcm / denominator).compareTo(that.numerator * lcm / that.denominator)
    }

    override fun toString(): String = if(denominator == 1) "$numerator" else "$numerator/$denominator"

    fun simplify(): Ratio = simplifyFactor().simplifySign()
    private fun simplifySign(): Ratio =
        if (denominator < 0) Ratio(-numerator, -denominator)
        else this

    private fun simplifyFactor(): Ratio {
        val gcf = greatestCommonFactor(numerator, denominator)
        return Ratio(numerator / gcf, denominator / gcf)
    }

    companion object {
        val ZERO = Ratio(0,1)
        val ONE = Ratio(1,1)
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
        fun Int.toRatio():Ratio = Ratio(this, 1)
        fun List<Ratio>.toRatioArray():Array<Ratio> = toTypedArray()
        operator fun Int.div(x:Ratio):Ratio = ONE / x
        fun List<Ratio>.sum():Ratio {
                var sum: Ratio = ZERO
                for (element in this) {
                    sum += element
                }
                return sum
        }
    }

    val toDouble: Double get() = numerator.toDouble() / denominator.toDouble()
}
