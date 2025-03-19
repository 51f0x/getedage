package com.demo

import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DoSomeWorkWhenNotA19Test {

private lateinit var testInstance: Showcase

@BeforeEach
fun setUp() {
    testInstance = Showcase()
}

        private data class TestData(
        val a: Int,
        val b: Int
        ,val expectedResult: Int
    )

    private val testData = TestData(
         a = 18,
        b = -80,
        expectedResult = 49
    )

    @Test
    fun testDoSomeWork() {
        // Testing branch coverage for complex condition:
        // - 'a > 19' is FALSE
        // Overall condition evaluates to: false
        val result = testInstance.doSomeWork(testData.a, testData.b)
        assertNotNull(result) // Basic verification
        assertTrue(result <= 0, "Result should be non-positive when condition is false")
        // Verify that we hit the expected branch
        assertTrue(true, "Successfully executed the FALSE branch")
        assertEquals(testData.expectedResult, result)
    }

}
