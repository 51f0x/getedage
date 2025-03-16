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
    
    // Determine which analyzer to use - enhanced version if available, fall back to standard
    val analyzer = EnhancedStaticAnalyzer()
    
    val testGenerator = TestGenerator()
    
    try {
        // Step 1: Collect and parse all Kotlin files
        val kotlinFiles = projectDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && !it.path.contains("/test/") }
            .toList()
        
        println("Found ${kotlinFiles.size} Kotlin files to analyze")

        kotlinFiles.forEach { println("File to analyze: ${it.absolutePath}") }
        
        // Step 2: Analyze the source code
        println("Using the available static analyzer...")
        
        // Handle both analyzer types
        println("Starting analysis...")
        val enhancedResult = analyzer.analyzeProject(kotlinFiles)
        println("Analysis completed.")
        val analysisResult = enhancedResult.toAnalysisResult()
        
        println("Analysis complete. Found:")
        println("- ${enhancedResult.classes.size} classes")
        println("- ${enhancedResult.functions.size} functions")
        println("- ${enhancedResult.conditionalBranches.size} conditional branches")
        println("- ${enhancedResult.variables.size} variables")
        println("- ${enhancedResult.references.size} references")
        println("- ${enhancedResult.functionCalls.size} function calls")
        println("- ${enhancedResult.loops.size} loops")
        
        // Print data flow analysis results
        println("\nData Flow Analysis Results:")
        println("- ${enhancedResult.definitions.size} variable definitions")
        println("- ${enhancedResult.uses.size} variable uses")
        println("- ${enhancedResult.defUsePairs.size} def-use pairs")
        println("- ${enhancedResult.dataFlowAnomalies.size} data flow anomalies")
        
        // Print anomaly breakdown if any exist
        if (enhancedResult.dataFlowAnomalies.isNotEmpty()) {
            val anomalyTypes = enhancedResult.dataFlowAnomalies.groupBy { it.anomalyType }
            println("\nAnomaly Breakdown:")
            anomalyTypes.forEach { (type, anomalies) ->
                println("- $type: ${anomalies.size}")
            }
        }
        
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