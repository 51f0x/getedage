package com.stge

import com.stge.example.ExampleClass
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Demonstrates how StGe works by analyzing and generating tests for ExampleClass.
 */
class DemoTest {
    
    @Test
    fun demonstrateStGe() {
        // Create a temporary directory for the output tests
        val tempDirPath = createTempDirectory("stge_demo")
        try {
            // Get the source directory for the example class
            val exampleClassFile = File("src/test/kotlin/com/stge/example/ExampleClass.kt")
            
            // Create an analyzer and test generator
            val analyzer = StaticAnalyzer()
            val testGenerator = TestGenerator()
            
            // Analyze the example class
            val analysisResult = analyzer.analyzeProject(listOf(exampleClassFile))
            
            println("Analysis complete. Found:")
            println("- ${analysisResult.classes.size} classes")
            println("- ${analysisResult.functions.size} functions")
            println("- ${analysisResult.conditionalBranches.size} conditional branches")
            
            // Generate test cases
            val testCases = testGenerator.generateTestCases(analysisResult)
            
            println("Generated ${testCases.size} test cases")
            
            // Write test files to the temp directory
            val testDir = File(tempDirPath.toFile(), "src/test/kotlin")
            testDir.mkdirs()
            
            testGenerator.writeTestFiles(testCases, testDir)
            
            println("Test files written to: ${testDir.absolutePath}")
            
            // List the generated files
            val generatedFiles = testDir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .toList()
            
            println("Generated ${generatedFiles.size} test files:")
            generatedFiles.forEach { println("- ${it.relativeTo(tempDirPath.toFile())}") }
            
        } finally {
            // Clean up
            tempDirPath.toFile().deleteRecursively()
        }
    }
} 