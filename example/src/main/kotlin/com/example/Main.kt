package com.example

fun main() {
    val calculator = Calculator()
    
    // Example of calculate method
    println("5 + 3 = ${calculator.calculate(5, 3, "add")}")
    println("10 - 4 = ${calculator.calculate(10, 4, "subtract")}")
    println("6 * 7 = ${calculator.calculate(6, 7, "multiply")}")
    println("20 / 5 = ${calculator.calculate(20, 5, "divide")}")
    
    // Example of evaluateNumber method
    println("0 is: ${calculator.evaluateNumber(0)}")
    println("7 is: ${calculator.evaluateNumber(7)}")
    println("-4 is: ${calculator.evaluateNumber(-4)}")
    println("10 is: ${calculator.evaluateNumber(10)}")
    
    // Example of processNumbers method with varargs
    println("Sum of 1, 2, 3, 4, 5: ${calculator.processNumbers("sum", 1, 2, 3, 4, 5)}")
    println("Average of 10, 20, 30, 40: ${calculator.processNumbers("average", 10, 20, 30, 40)}")
    println("Max of 5, 9, 2, 8, 1: ${calculator.processNumbers("max", 5, 9, 2, 8, 1)}")
    println("Min of 5, 9, 2, 8, 1: ${calculator.processNumbers("min", 5, 9, 2, 8, 1)}")
} 