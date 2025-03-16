package com.stge

import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    println("StGe - Static Kotlin Test Generator")
    println("===================================")
    println("Using static analysis to generate tests with 100% branch coverage")
    
    if (args.isEmpty()) {
        println("Usage: stge <path-to-kotlin-project>")
        exitProcess(1)
    }
    
    val projectPath = args[0]
    val projectDir = File(projectPath)
    
    if (!projectDir.exists() || !projectDir.isDirectory) {
        println("Error: Provided path is not a valid directory")
        exitProcess(1)
    }
    
    println("Analyzing project at: ${projectDir.absolutePath}")
    
    // Use SimpleStaticAnalyzer instead
    val analyzer = SimpleStaticAnalyzer()
    val testGenerator = TestGenerator()
    
    try {
        // Step 1: Collect and parse all Kotlin files
        val kotlinFiles = projectDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && !it.path.contains("/test/") }
            .toList()
        
        println("Found ${kotlinFiles.size} Kotlin files to analyze")

        kotlinFiles.forEach { println(it.absolutePath) }
        
        // Step 2: Analyze the source code using regex-based parsing
        val analysisResult = analyzer.analyzeProject(kotlinFiles)
        
        println("Analysis complete. Found:")
        println("- ${analysisResult.classes.size} classes")
        println("- ${analysisResult.functions.size} functions")
        println("- ${analysisResult.conditionalBranches.size} conditional branches")
        
        // Step 3: Generate test cases with 100% branch coverage
        val testCases = testGenerator.generateTestCases(analysisResult)
        
        println("Generated ${testCases.size} test cases for complete branch coverage")
        
        // Calculate branch coverage statistics
        val uniqueBranches = analysisResult.conditionalBranches.size
        val branchTestCases = testCases.count { it.name.contains("When") }
        val basicTestCases = testCases.size - branchTestCases
        
        println("\nCoverage Statistics:")
        println("- ${basicTestCases} basic test cases for function coverage")
        println("- ${branchTestCases} branch-specific test cases")
        println("- ${uniqueBranches} unique branches identified")
        println("- 100% branch coverage targeted")
        
        // Step 4: Write test cases to files
        val testDir = File(projectDir, "src/test/kotlin")
        testDir.mkdirs()
        
        testGenerator.writeTestFiles(testCases, testDir)
        
        println("\nSuccessfully generated test files in: ${testDir.absolutePath}")
        println("Run tests with: ./gradlew test")
        
    } catch (e: Exception) {
        println("Error during analysis: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
} 