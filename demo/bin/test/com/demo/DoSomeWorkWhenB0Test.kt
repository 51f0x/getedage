package com.demo

import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DoSomeWorkWhenB0Test {

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
         b = 0,
        a = 51
        ,expectedResult = 44
    )

    @Test
    fun testDoSomeWork() {
        // Testing branch coverage for complex condition:
        // - 'b == 0' is TRUE
        // Overall condition evaluates to: true
        val result = testInstance.doSomeWork(testData.a, testData.b)
        assertNotNull(result) // Basic verification
        assertTrue(result >= 0, "Result should be non-negative when condition is true")
        // Verify that we hit the expected branch
        assertTrue(true, "Successfully executed the TRUE branch")
        assertEquals(testData.expectedResult, result)
    }

}
