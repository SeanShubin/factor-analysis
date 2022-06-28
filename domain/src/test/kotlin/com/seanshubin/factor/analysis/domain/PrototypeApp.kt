package com.seanshubin.factor.analysis.domain

import com.seanshubin.factor.analysis.matrix.ListMatrix
import com.seanshubin.factor.analysis.matrix.Matrix
import kotlin.random.Random

object PrototypeApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val individuals = randomIndividuals()
        val observables = randomObservables()
        val observations = makeObservations(individuals, observables)
        val correlationCoefficients = observations.correlationCoefficients()
        println(correlationCoefficients)
    }

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
