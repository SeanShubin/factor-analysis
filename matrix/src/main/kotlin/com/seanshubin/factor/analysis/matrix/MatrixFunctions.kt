package com.seanshubin.factor.analysis.matrix

operator fun Matrix<Int>.plus(that: Matrix<Int>): Matrix<Int> = binaryOp(that) { a, b -> a + b }
operator fun Matrix<Int>.times(that: Int): Matrix<Int> = unaryOp { a -> a * that }
fun dotProduct(a: List<Int>, b: List<Int>): Int = a.zip(b).map { (c, d) -> c * d }.sum()
operator fun Matrix<Int>.times(that: Matrix<Int>): Matrix<Int> = rowColOp(that) { a, b -> dotProduct(a, b) }
fun List<Int>.toMatrix(rows: Int) = Matrix(this.chunked(this.size / rows))
