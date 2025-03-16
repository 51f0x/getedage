package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class CalculatorTest {
    private lateinit var testInstance: Calculator
    
    @BeforeEach
    fun setUp() {
        testInstance = Calculator()
    }
    
    // Basic test for method coverage
    @Test
    fun calculateBasic() {
        val a = 10
        val b = 5
        val operation = "add"
        
        val result = testInstance.calculate(a, b, operation)
        assertNotNull(result) // Basic verification
    }
    
    // "When" branch test with specific assertion for "add" operation
    @Test
    fun calculateWhenAdd() {
        // Testing when branch coverage for value: 'add' in when expression on 'operation'
        val a = 10
        val b = 5
        val operation = "add"
        
        val result = testInstance.calculate(a, b, operation)
        assertNotNull(result, "Result should not be null for when branch 'add'")
        
        // For exact value verification, calculate the expected result
        val expected = a + b
        assertEquals(expected, result, "Result should be the sum of a and b")
        assertTrue(result > 0, "Result should be positive for addition operation")
    }
    
    // "When" branch test with specific assertion for "subtract" operation
    @Test
    fun calculateWhenSubtract() {
        // Testing when branch coverage for value: 'subtract' in when expression on 'operation'
        val a = 10
        val b = 5
        val operation = "subtract"
        
        val result = testInstance.calculate(a, b, operation)
        assertNotNull(result, "Result should not be null for when branch 'subtract'")
        
        // For exact value verification, calculate the expected result
        val expected = a - b
        assertEquals(expected, result, "Result should be a minus b")
    }
    
    // "When" branch test with specific assertion for "multiply" operation
    @Test
    fun calculateWhenMultiply() {
        // Testing when branch coverage for value: 'multiply' in when expression on 'operation'
        val a = 10
        val b = 5
        val operation = "multiply"
        
        val result = testInstance.calculate(a, b, operation)
        assertNotNull(result, "Result should not be null for when branch 'multiply'")
        
        // For exact value verification, calculate the expected result
        val expected = a * b
        assertEquals(expected, result, "Result should be a multiplied by b")
    }
    
    // "When" branch test with specific assertion for "divide" operation
    @Test
    fun calculateWhenDivide() {
        // Testing when branch coverage for value: 'divide' in when expression on 'operation'
        val a = 10
        val b = 5
        val operation = "divide"
        
        val result = testInstance.calculate(a, b, operation)
        assertNotNull(result, "Result should not be null for when branch 'divide'")
        
        // For exact value verification, calculate the expected result
        val expected = a / b
        assertEquals(expected, result, "Result should be a divided by b")
    }
    
    // "If" condition within "divide" branch - TRUE condition
    @Test
    fun calculateWhenDivideAndBEquals0True() {
        // Testing branch coverage for condition: 'b == 0' is TRUE
        val a = 10
        val b = 0
        val operation = "divide"
        
        // This should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            testInstance.calculate(a, b, operation)
        }
        
        // Verify the exception message
        assertEquals("Cannot divide by zero", exception.message)
    }
    
    // "If" condition within "divide" branch - FALSE condition
    @Test
    fun calculateWhenDivideAndBEquals0False() {
        // Testing branch coverage for condition: 'b == 0' is FALSE
        val a = 10
        val b = 5  // Non-zero value
        val operation = "divide"
        
        val result = testInstance.calculate(a, b, operation)
        
        // No exception should be thrown and result should be a valid division
        assertEquals(a / b, result, "Result should be a valid division when b is not zero")
    }
    
    // "When" branch test with specific assertion for "else" branch
    @Test
    fun calculateWhenUnknownOperation() {
        // Testing when branch coverage for value: 'else' in when expression on 'operation'
        val a = 10
        val b = 5
        val operation = "unknown"
        
        // This should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            testInstance.calculate(a, b, operation)
        }
        
        // Verify the exception message contains the operation name
        assertTrue(exception.message?.contains("unknown") ?: false, 
                   "Exception message should mention the unknown operation")
    }
    
    // Test for "if value == 0" branch - TRUE condition
    @Test
    fun evaluateNumberWhenValueEquals0True() {
        // Testing branch coverage for condition: 'value == 0' is TRUE
        val value = 0
        
        val result = testInstance.evaluateNumber(value)
        assertNotNull(result, "Result should not be null")
        assertEquals("Zero", result, "Result should be 'Zero' when value is 0")
    }
    
    // Test for "if value == 0" branch - FALSE condition
    @Test
    fun evaluateNumberWhenValueEquals0False() {
        // Testing branch coverage for condition: 'value == 0' is FALSE
        val value = 10  // Non-zero value
        
        val result = testInstance.evaluateNumber(value)
        assertNotNull(result, "Result should not be null")
        assertNotEquals("Zero", result, "Result should not be 'Zero' when value is not 0")
    }
    
    // Test for "if value < 0" branch - TRUE condition
    @Test
    fun evaluateNumberWhenValueLessThan0True() {
        // Testing branch coverage for condition: 'value < 0' is TRUE
        val value = -5  // Negative value
        
        val result = testInstance.evaluateNumber(value)
        assertNotNull(result, "Result should not be null")
        assertTrue(result.startsWith("Negative"), "Result should start with 'Negative' for negative value")
    }
    
    // Test for "if value < 0" branch - FALSE condition
    @Test
    fun evaluateNumberWhenValueLessThan0False() {
        // Testing branch coverage for condition: 'value < 0' is FALSE
        val value = 5  // Positive value
        
        val result = testInstance.evaluateNumber(value)
        assertNotNull(result, "Result should not be null")
        assertTrue(result.startsWith("Positive"), "Result should start with 'Positive' for positive value")
    }
    
    // Test for "if value % 2 == 0" branch - TRUE condition
    @Test
    fun evaluateNumberWhenValueMod2Equals0True() {
        // Testing branch coverage for condition: 'value % 2 == 0' is TRUE
        val value = 10  // Even value
        
        val result = testInstance.evaluateNumber(value)
        assertNotNull(result, "Result should not be null")
        assertTrue(result.contains("even"), "Result should contain 'even' for even value")
    }
    
    // Test for "if value % 2 == 0" branch - FALSE condition
    @Test
    fun evaluateNumberWhenValueMod2Equals0False() {
        // Testing branch coverage for condition: 'value % 2 == 0' is FALSE
        val value = 9  // Odd value
        
        val result = testInstance.evaluateNumber(value)
        assertNotNull(result, "Result should not be null")
        assertTrue(result.contains("odd"), "Result should contain 'odd' for odd value")
    }
    
    // Tests for processNumbers method with varargs
    
    // Test for isEmpty check - TRUE condition
    @Test
    fun processNumbersWhenNumbersIsEmptyTrue() {
        // Testing branch coverage for condition: 'numbers.isEmpty()' is TRUE
        val operation = "sum"
        
        // This should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            testInstance.processNumbers(operation) // Empty varargs
        }
        
        // Verify the exception message
        assertEquals("No numbers provided", exception.message)
    }
    
    // Test for when branch "sum"
    @Test
    fun processNumbersWhenSum() {
        // Testing when branch coverage for value: 'sum' in when expression on 'operation'
        val operation = "sum"
        val numbers = intArrayOf(1, 2, 3, 4, 5)
        
        val result = testInstance.processNumbers(operation, *numbers)
        assertNotNull(result, "Result should not be null for when branch 'sum'")
        
        // For exact value verification, calculate the expected result
        val expected = numbers.sum().toDouble()
        assertEquals(expected, result, "Result should be the sum of all numbers")
        assertTrue(result > 0, "Result should be positive for sum operation")
    }
    
    // Test for when branch "average"
    @Test
    fun processNumbersWhenAverage() {
        // Testing when branch coverage for value: 'average' in when expression on 'operation'
        val operation = "average"
        val numbers = intArrayOf(10, 20, 30, 40)
        
        val result = testInstance.processNumbers(operation, *numbers)
        assertNotNull(result, "Result should not be null for when branch 'average'")
        
        // For exact value verification, calculate the expected result
        val expected = numbers.average()
        assertEquals(expected, result, "Result should be the average of all numbers")
    }
    
    // Test for when branch "max"
    @Test
    fun processNumbersWhenMax() {
        // Testing when branch coverage for value: 'max' in when expression on 'operation'
        val operation = "max"
        val numbers = intArrayOf(5, 9, 2, 8, 1)
        
        val result = testInstance.processNumbers(operation, *numbers)
        assertNotNull(result, "Result should not be null for when branch 'max'")
        
        // For exact value verification, calculate the expected result
        val expected = numbers.maxOrNull()!!.toDouble()
        assertEquals(expected, result, "Result should be the maximum of all numbers")
    }
    
    // Test for when branch "min"
    @Test
    fun processNumbersWhenMin() {
        // Testing when branch coverage for value: 'min' in when expression on 'operation'
        val operation = "min"
        val numbers = intArrayOf(5, 9, 2, 8, 1)
        
        val result = testInstance.processNumbers(operation, *numbers)
        assertNotNull(result, "Result should not be null for when branch 'min'")
        
        // For exact value verification, calculate the expected result
        val expected = numbers.minOrNull()!!.toDouble()
        assertEquals(expected, result, "Result should be the minimum of all numbers")
    }
    
    // Test for when branch "else"
    @Test
    fun processNumbersWhenUnknownOperation() {
        // Testing when branch coverage for value: 'else' in when expression on 'operation'
        val operation = "unknown"
        val numbers = intArrayOf(1, 2, 3)
        
        // This should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            testInstance.processNumbers(operation, *numbers)
        }
        
        // Verify the exception message contains the operation name
        assertTrue(exception.message?.contains("unknown") ?: false, 
                   "Exception message should mention the unknown operation")
    }
} 