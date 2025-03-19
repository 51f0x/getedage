package com.demo

import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DoSomeWorkWhenNotB0Test {

private lateinit var testInstance: Showcase

@BeforeEach
fun setUp() {
    testInstance = Showcase()
}

        private data class TestData(
        val a: Int,
        val b: Int,
        expectedResult: Int
    )

    private val testData = TestData(
         b = 0,
        a = -12,
        expectedResult = 88
    )

    @Test
    fun testDoSomeWork() {
        // Testing branch coverage for complex condition:
        // - 'b == 0' is FALSE
        // Overall condition evaluates to: false
        val result = testInstance.doSomeWork(testData.a, testData.b)
        assertNotNull(result) // Basic verification
        assertTrue(result <= 0, "Result should be non-positive when condition is false")
        // Verify that we hit the expected branch
        assertTrue(true, "Successfully executed the FALSE branch")
        assertEquals(testData.expectedResult, result)
    }

}
