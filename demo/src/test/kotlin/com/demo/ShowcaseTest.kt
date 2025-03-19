package com.demo

import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ShowcaseTest {

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
        a = -67,
        b = -72
        expectedResult = 55
    )

    @Test
    fun doSomeWorkBasic_814512702() {
        val result = testInstance.doSomeWork(testData.a, testData.b)
        assertNotNull(result)
        assertEquals(testData.expectedResult, result)
    }

        private data class TestData(
        a: Int,
        b: Int
        expectedResult: Int
    )

    private val testData = TestData(
        a = 6,
        b = -29
        expectedResult = -66
    )

    @Test
    fun doSomeOtherWorkBasic_7558252() {
        val result = testInstance.doSomeOtherWork(testData.a, testData.b)
        assertNotNull(result)
        assertEquals(testData.expectedResult, result)
    }

        private data class TestData(
        a: Int
        expectedResult: Int
    )

    private val testData = TestData(
        a = 2
        expectedResult = 94
    )

    @Test
    fun doSomeMoreWorkBasic_1595938281() {
        val result = testInstance.doSomeMoreWork(testData.a)
        assertNotNull(result)
        assertEquals(testData.expectedResult, result)
    }

}
