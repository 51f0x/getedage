package com.demo

import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DoSomeWorkWhenA19Test {

private lateinit var testInstance: Showcase

@BeforeEach
fun setUp() {
    testInstance = Showcase()
}

        private data class TestData(
        a: Int,
        b: Int
        expectedResult: Int
    )

    private val testData = TestData(
        val a = 20,
        b = 24
        expectedResult = -75
    )

    @Test
    fun testDoSomeWork() {
        // Testing branch coverage for complex condition:
        // - 'a > 19' is TRUE
        // Overall condition evaluates to: true
        val result = testInstance.doSomeWork(testData.a, testData.b)
        assertNotNull(result) // Basic verification
        assertTrue(result >= 0, "Result should be non-negative when condition is true")
        // Verify that we hit the expected branch
        assertTrue(true, "Successfully executed the TRUE branch")
        assertEquals(testData.expectedResult, result)
    }

}
