package com.stge

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import org.junit.jupiter.api.Assertions.*

class StaticAnalyzerTest {

    @TempDir
    lateinit var tempDir: File
    
    @Test
    fun `test analyzer can parse simple Kotlin class`() {
        // Create a test Kotlin file
        val testFile = File(tempDir, "TestClass.kt")
        testFile.writeText("""
            package test
            
            class TestClass(val name: String) {
                fun sayHello(): String {
                    return "Hello, ${'$'}name!"
                }
                
                fun processValue(value: Int): String {
                    return if (value > 10) {
                        "Large value: ${'$'}value"
                    } else {
                        "Small value: ${'$'}value"
                    }
                }
            }
        """.trimIndent())
        
        // Run the analyzer
        val analyzer = StaticAnalyzer()
        val result = analyzer.analyzeProject(listOf(testFile))
        
        // Verify the analysis results
        println("Analysis results:")
        println("Classes: ${result.classes.size}")
        println("Functions: ${result.functions.size}")
        println("Conditional branches: ${result.conditionalBranches.size}")
        
        result.classes.forEach { println("Class: ${it.name}") }
        result.functions.forEach { println("Function: ${it.name}") }
        result.conditionalBranches.forEach { println("Branch: ${it.condition} in ${it.functionName}") }
        
        assertAll(
            { assertTrue(result.classes.isNotEmpty(), "Should find at least one class") },
            { assertEquals("TestClass", result.classes.firstOrNull()?.name, "Should find TestClass") },
            { assertTrue(result.functions.size >= 2, "Should find at least two functions") },
            { assertTrue(result.conditionalBranches.isNotEmpty(), "Should find at least one conditional branch") }
        )
    }
    
    @Test
    fun `test analyzer can detect vararg parameters`() {
        // Create a test Kotlin file with vararg methods
        val testFile = File(tempDir, "VarargTest.kt")
        testFile.writeText("""
            package test
            
            class VarargTest {
                fun sum(vararg numbers: Int): Int {
                    return numbers.sum()
                }
                
                fun join(separator: String, vararg strings: String): String {
                    return strings.joinToString(separator)
                }
            }
        """.trimIndent())
        
        // Run the analyzer
        val analyzer = StaticAnalyzer()
        val result = analyzer.analyzeProject(listOf(testFile))
        
        // Find functions with vararg parameters
        val varargFunctions = result.functions.filter { function ->
            function.parameters.any { it.isVararg }
        }
        
        println("Found ${varargFunctions.size} functions with vararg parameters:")
        varargFunctions.forEach { function ->
            val varargParams = function.parameters.filter { it.isVararg }
            println("Function: ${function.name}, Vararg params: ${varargParams.map { it.name }}")
        }
        
        assertAll(
            { assertEquals(2, varargFunctions.size, "Should find two functions with vararg parameters") },
            { assertTrue(varargFunctions.any { it.name == "sum" }, "Should find 'sum' function") },
            { assertTrue(varargFunctions.any { it.name == "join" }, "Should find 'join' function") }
        )
    }
} 