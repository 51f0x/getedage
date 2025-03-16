package com.stge.example

/**
 * Example class demonstrating vararg usage.
 */
class VarargExample {
    /**
     * Concatenates all string arguments with a separator.
     */
    fun concatStrings(separator: String, vararg strings: String): String {
        return strings.joinToString(separator)
    }
    
    /**
     * Calculates the sum of all integer arguments.
     */
    fun sum(vararg numbers: Int): Int {
        return numbers.sum()
    }
    
    /**
     * Finds the maximum value in the provided numbers.
     */
    fun findMax(firstNumber: Int, vararg otherNumbers: Int): Int {
        var max = firstNumber
        for (number in otherNumbers) {
            if (number > max) {
                max = number
            }
        }
        return max
    }
    
    /**
     * Creates a map from pairs of keys and values.
     */
    fun <K, V> createMap(vararg pairs: Pair<K, V>): Map<K, V> {
        return pairs.toMap()
    }
}

/**
 * Top-level function with varargs to demonstrate handling outside of classes.
 */
fun printWithPrefix(prefix: String, vararg items: Any) {
    for (item in items) {
        println("$prefix$item")
    }
}

// Example usages to demonstrate how the analyzer should use lookahead
fun demonstrateVarargUsage() {
    val example = VarargExample()
    
    // Calls with various numbers of arguments
    example.concatStrings(", ", "apple", "banana", "cherry")
    example.concatStrings("-", "one", "two", "three", "four", "five")
    
    example.sum(1, 2, 3, 4, 5)
    example.sum(10, 20, 30)
    
    example.findMax(5, 10, 8, 12, 3)
    example.findMax(100, 50, 75)
    
    example.createMap(Pair("a", 1), Pair("b", 2), Pair("c", 3))
    
    // Top-level function calls
    printWithPrefix("Item: ", "book", "pen", "laptop")
    printWithPrefix("Number: ", 1, 2, 3, 4, 5)
} 