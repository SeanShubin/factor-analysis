package com.seanshubin.factor.analysis.domain

data class Observable(val index:Int, val multiply:Double, val add:Double){
    fun observe(factors:List<Double>):Double = factors[index] * multiply + add
}

