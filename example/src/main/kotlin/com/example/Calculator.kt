package com.example

/**
 * A simple calculator class with multiple branches for demonstrating test generation
 * Tests will be generated in the same package as this class.
 */
class Calculator {
    /**
     * Performs basic arithmetic operations
     * @param a First number
     * @param b Second number
     * @param operation String indicating the operation: "add", "subtract", "multiply", or "divide"
     * @return Result of the operation
     */
    fun calculate(a: Int, b: Int, operation: String): Int {
        return when (operation) {
            "add" -> a + b
            "subtract" -> a - b
            "multiply" -> a * b
            "divide" -> {
                if (b == 0) {
                    throw IllegalArgumentException("Cannot divide by zero")
                }
                a / b
            }
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }
    }

    /**
     * Evaluates a number and returns a description
     * @param value Number to evaluate
     * @return Description of the number
     */
    fun evaluateNumber(value: Int): String {
        val result = StringBuilder()
        
        if (value == 0) {
            return "Zero"
        }
        
        if (value < 0) {
            result.append("Negative ")
        } else {
            result.append("Positive ")
        }
        
        if (value % 2 == 0) {
            result.append("even")
        } else {
            result.append("odd")
        }
        
        if (isPrime(Math.abs(value))) {
            result.append(" prime")
        }
        
        return result.toString()
    }
    
    /**
     * Checks if a number is prime
     * @param n Number to check
     * @return true if the number is prime, false otherwise
     */
    private fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        if (n % 2 == 0 || n % 3 == 0) return false
        
        var i = 5
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false
            i += 6
        }
        
        return true
    }
    
    /**
     * Processes a list of numbers with varargs
     * @param operation The operation to perform: "sum", "average", "max", "min"
     * @param numbers Variable number of integers to process
     * @return Result of the operation
     */
    fun processNumbers(operation: String, vararg numbers: Int): Double {
        if (numbers.isEmpty()) {
            throw IllegalArgumentException("No numbers provided")
        }
        
        return when (operation) {
            "sum" -> numbers.sum().toDouble()
            "average" -> numbers.average()
            "max" -> numbers.maxOrNull()?.toDouble() ?: throw IllegalStateException("Empty")
            "min" -> numbers.minOrNull()?.toDouble() ?: throw IllegalStateException("Empty")
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }
    }
} 