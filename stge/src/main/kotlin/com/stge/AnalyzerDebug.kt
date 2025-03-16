package com.stge

import java.io.File

/**
 * Simple debug utility to test the StaticAnalyzer with a sample file.
 * Run this class directly to see if the analyzer is working correctly.
 */
object AnalyzerDebug {
    @JvmStatic
    fun main(args: Array<String>) {
        println("StGe Analyzer Debug Tool")
        println("========================")
        
        // Create a temporary test file
        val tempDir = createTempDir("stge_debug")
        val testFile = File(tempDir, "TestClass.kt")
        
        try {
            println("Creating test file: ${testFile.absolutePath}")
            
            testFile.writeText("""
                package test
                
                /**
                 * A simple test class
                 */
                class TestClass(val name: String) {
                    /**
                     * Greets the person by name
                     */
                    fun sayHello(): String {
                        return "Hello, ${'$'}name!"
                    }
                    
                    /**
                     * Processes a numeric value
                     */
                    fun processValue(value: Int): String {
                        return if (value > 10) {
                            "Large value: ${'$'}value"
                        } else {
                            "Small value: ${'$'}value"
                        }
                    }
                    
                    /**
                     * Vararg test function
                     */
                    fun sum(vararg numbers: Int): Int {
                        return numbers.sum()
                    }
                }
                
                fun topLevelFunction(arg1: String, arg2: Int = 0): String {
                    return "arg1: ${'$'}arg1, arg2: ${'$'}arg2"
                }
            """.trimIndent())
            
            println("Test file created successfully.")
            println("Running static analyzer...")
            
            // Use the Kotlin Compiler API analyzer for more accurate results
            val analyzer = StaticAnalyzer()
            val result = analyzer.analyzeProject(listOf(testFile))
            
            // Print results
            println("\nAnalysis Results:")
            println("----------------")
            println("Classes: ${result.classes.size}")
            println("Functions: ${result.functions.size}")
            println("Conditional branches: ${result.conditionalBranches.size}")
            
            println("\nClasses:")
            result.classes.forEach { classInfo ->
                println("- ${classInfo.name}")
                println("  Properties:")
                classInfo.properties.forEach { prop ->
                    println("  - ${prop.name}: ${prop.type} (${if (prop.isVar) "var" else "val"})" +
                            (prop.initializer?.let { " = $it" } ?: ""))
                }
            }
            
            println("\nFunctions:")
            result.functions.forEach { function ->
                val className = function.containingClass?.let { "$it." } ?: ""
                print("- $className${function.name}(")
                print(function.parameters.joinToString(", ") { param ->
                    "${if (param.isVararg) "vararg " else ""}${param.name}: ${param.type}" +
                            (param.defaultValue?.let { " = $it" } ?: "")
                })
                println("): ${function.returnType}")
            }
            
            println("\nConditional Branches:")
            result.conditionalBranches.forEach { branch ->
                val functionName = branch.functionName
                println("- [${branch.type}] in $functionName: ${branch.condition}")
            }
            
        } catch (e: Exception) {
            println("Error during analysis: ${e.message}")
            e.printStackTrace()
        } finally {
            // Clean up
            tempDir.deleteRecursively()
        }
    }
} 