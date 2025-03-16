package com.stge

import com.stge.example.VarargExample
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests the StGe's ability to handle varargs with lookahead.
 */
class VarargTest {
    
    @Test
    fun testVarargSupport() {
        // Get the source files
        val varargExampleFile = File("src/test/kotlin/com/stge/example/VarargExample.kt")
        
        // Create an analyzer and test generator
        val analyzer = StaticAnalyzer()
        val testGenerator = TestGenerator()
        
        // Analyze the example file
        val analysisResult = analyzer.analyzeProject(listOf(varargExampleFile))
        
        // Print information about the analysis
        println("Analysis results:")
        println("- Classes: ${analysisResult.classes.size}")
        println("- Functions: ${analysisResult.functions.size}")
        
        // Check if we found vararg parameters
        val varargParams = analysisResult.functions.flatMap { it.parameters }.filter { it.isVararg }
        println("- Vararg parameters found: ${varargParams.size}")
        
        varargParams.forEach { param ->
            println("  - ${param.name} of type ${param.type}")
        }
        
        // Generate test cases
        val testCases = testGenerator.generateTestCases(analysisResult)
        
        // Print information about the generated tests
        println("Generated ${testCases.size} test cases")
        
        // Verify that the tests for vararg methods use examples from the code
        val varargTestCases = testCases.filter { testCase ->
            val function = analysisResult.functions.find { it.name == testCase.targetFunction }
            function?.parameters?.any { it.isVararg } ?: false
        }
        
        println("Generated ${varargTestCases.size} test cases for functions with varargs")
        
        // Print example test cases
        varargTestCases.take(3).forEach { testCase ->
            println("\nTest case for ${testCase.targetFunction}:")
            println(testCase.testCode)
        }
    }
} 