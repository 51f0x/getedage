package com.stge.example

/**
 * A simple example class to demonstrate StGe.
 */
class ExampleClass(
    val name: String,
    val value: Int
) {
    /**
     * A method with a simple if branch.
     */
    fun isValuePositive(): Boolean {
        return if (value > 0) {
            true
        } else {
            false
        }
    }

    /**
     * A method with a when expression.
     */
    fun describeValue(): String {
        return when {
            value < 0 -> "Negative"
            value == 0 -> "Zero"
            value < 10 -> "Small positive"
            value < 100 -> "Medium positive"
            else -> "Large positive"
        }
    }

    /**
     * A method with string operations and branches.
     */
    fun processName(): String {
        if (name.isEmpty()) {
            return "No name provided"
        }

        return when (name.length) {
            1 -> "Single character: $name"
            in 2..5 -> "Short name: $name"
            in 6..10 -> "Medium name: $name"
            else -> "Long name: $name"
        }
    }

    /**
     * A method with multiple nested conditions.
     */
    fun complexAnalysis(otherValue: Int, factor: Double): String {
        val sum = value + otherValue
        
        if (sum > 100) {
            if (factor > 1.0) {
                return "High sum, high factor"
            } else {
                return "High sum, low factor"
            }
        } else if (sum > 50) {
            when {
                factor < 0.5 -> return "Medium sum, very low factor"
                factor < 1.0 -> return "Medium sum, low factor"
                factor < 1.5 -> return "Medium sum, medium factor"
                else -> return "Medium sum, high factor"
            }
        } else {
            if (factor > 1.0) {
                return "Low sum, high factor"
            } else {
                return "Low sum, low factor"
            }
        }
    }
} 