package com.seanshubin.factor.analysis.domain

import com.seanshubin.factor.analysis.matrix.ListMatrix
import com.seanshubin.factor.analysis.matrix.Matrix
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.random.Random

object PrototypeApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val individuals = randomIndividuals()
        val observables = randomObservables()
        val observations = makeObservations(individuals, observables)
        val correlationCoefficients = observations.correlationCoefficients()
        println("correlation coefficients")
        println(correlationCoefficients.round())
        val minimumCorrelation = 0.9
        var current = correlationCoefficients
        do {
            val correlation = current.highestCorrelation()
            if(correlation.value >= minimumCorrelation){
                println("correlation")
                println(correlation)
                val (factor, residual) = current.extractFactor(correlation.rowIndex)
                println("factor")
                println(factor.round())
                val related = (0 until factor.columnCount).filter {
                    factor[it, it] >= minimumCorrelation
                }
                println("related")
                println(related)
                println("residual")
                println(residual.round())
                current = residual
            } else {
                break
            }
        }while(true)
    }

    fun Matrix.highestCorrelation():Correlation{
        var highestSoFar:Double? = null
        var coordinate:Pair<Int, Int>? = null
        for(rowIndex in 0 until rowCount){
            for(columnIndex in 0 until columnCount){
                if(rowIndex != columnIndex){
                    val value = abs(this[rowIndex, columnIndex])
                    if(highestSoFar == null || value > highestSoFar){
                        highestSoFar = value
                        coordinate = Pair(rowIndex, columnIndex)
                    }
                }
            }
        }
        highestSoFar!!
        coordinate!!
        return Correlation(highestSoFar, coordinate.first, coordinate.second )
    }

    fun Matrix.extractFactor(index:Int):Pair<Matrix, Matrix>{
        val factor = getColumnAsMatrix(index) * getRowAsMatrix(index)
        val residual = this - factor
        return Pair(factor,residual)
    }

    fun Matrix.round():Matrix = this.unaryOperation { x -> (x * 100).roundToLong() / 100.0 }

    val seed:Long = 12345L
    val random:Random = Random(seed)
    val factorCount = 3
    val individualCount = 1000
    val observableCountPerFactor = 3

    fun randomIndividuals():List<List<Double>> = (0 until individualCount).map{ randomIndividual() }
    fun randomIndividual():List<Double> = (0 until factorCount).map { randomFactor() }
    fun randomFactor():Double = random.nextDouble(-1.0,1.0) * 2 - 1
    fun randomMultiply():Double = random.nextDouble(-10.0, 10.0)
    fun randomAdd():Double = random.nextDouble(-5.0, 5.0)
    fun randomObservable(factorIndex:Int):Observable =Observable(factorIndex, randomMultiply(), randomAdd())
    fun randomObservablesForFactor(factorIndex:Int):List<Observable> = (0 until observableCountPerFactor).map(::randomObservable)
    fun randomObservables():List<Observable> = (0 until factorCount).flatMap(::randomObservablesForFactor)

    fun makeObservations(individuals:List<List<Double>>, observables: List<Observable>):Matrix =
        ListMatrix.empty.fromRows(individuals.map{ individual ->
            observables.map { observable ->
                observable.observe(individual)
            }
        })
}
