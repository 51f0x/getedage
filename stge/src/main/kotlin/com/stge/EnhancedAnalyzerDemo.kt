package com.stge

import java.io.File
import kotlin.system.exitProcess

/**
 * Demo utility to showcase the enhanced static analyzer capabilities.
 * This provides a rich demonstration of the full program context available for every line of code.
 */
object EnhancedAnalyzerDemo {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("EnhancedStaticAnalyzer Demo")
        println("===========================")
        
        // Create a temporary test file
        val tempDir = createTempDir("enhanced_analyzer_demo")
        val testFile = File(tempDir, "SampleCode.kt")
        
        try {
            println("Creating test file: ${testFile.absolutePath}")
            
            testFile.writeText("""
                package test
                
                /**
                 * A class with various features to demonstrate the enhanced analyzer
                 */
                class ContextDemo(val name: String, private val config: Map<String, Any>) {
                    // Properties with different types
                    val id = generateId()
                    var counter = 0
                    private val items = mutableListOf<String>()
                    
                    /**
                     * Simple method with conditionals
                     */
                    fun processValue(value: Int): String {
                        counter++
                        return if (value > 10) {
                            "Large value: ${'$'}value"
                        } else if (value < 0) {
                            "Negative value: ${'$'}value"
                        } else {
                            "Small value: ${'$'}value"
                        }
                    }
                    
                    /**
                     * Method with loops
                     */
                    fun processItems(vararg newItems: String): List<String> {
                        for (item in newItems) {
                            if (item.isNotBlank()) {
                                items.add(item)
                            }
                        }
                        
                        var i = 0
                        while (i < items.size) {
                            if (items[i].length > 20) {
                                items[i] = items[i].substring(0, 20) + "..."
                            }
                            i++
                        }
                        
                        return items.toList()
                    }
                    
                    /**
                     * Method with when expression
                     */
                    fun categorize(value: Any): String {
                        return when (value) {
                            is String -> "Text: ${'$'}value"
                            is Int -> "Number: ${'$'}value"
                            is Boolean -> "Flag: ${'$'}value"
                            is List<*> -> "Collection with items"
                            else -> "Unknown type"
                        }
                    }
                    
                    /**
                     * Private helper method
                     */
                    private fun generateId(): String {
                        return "ID-Static-${System.currentTimeMillis()}"
                    }
                    
                    /**
                     * Method with local variables and destructuring
                     */
                    fun complexProcess(data: Map<String, Any>, transform: (Any) -> String): List<String> {
                        val results = mutableListOf<String>()
                        
                        for ((key, value) in data) {
                            val processed = transform(value)
                            val prefix = "[${'$'}key]"
                            val result = "${'$'}prefix ${'$'}processed"
                            results.add(result)
                            
                            val (category, detail) = categorize(value).split(": ", limit = 2)
                            results.add("  Type: ${'$'}category, Detail: ${'$'}detail")
                        }
                        
                        return results
                    }
                }
                
                /**
                 * Extension function to demonstrate top-level functions
                 */
                fun ContextDemo.analyze(input: Any): Map<String, String> {
                    val category = this.categorize(input)
                    val result = mapOf(
                        "category" to category,
                        "length" to input.toString().length.toString(),
                        "processed" to when (input) {
                            is Number -> processValue(input.toInt())
                            is String -> input
                            else -> input.toString()
                        }
                    )
                    
                    return result
                }
                
                /**
                 * Top-level function
                 */
                fun runAnalysis(demo: ContextDemo, inputs: List<Any>): List<Map<String, String>> {
                    return inputs.map { demo.analyze(it) }
                }
            """.trimIndent())
            
            println("Test file created successfully.")
            println("Running enhanced static analyzer...")
            
            // Run the enhanced analyzer on the test file
            val analyzer = EnhancedStaticAnalyzer()
            val result = analyzer.analyzeProject(listOf(testFile))
            
            // Print overall analysis results
            println("\nAnalysis Results:")
            println("----------------")
            println("Files: ${result.files.size}")
            println("Classes: ${result.classes.size}")
            println("Functions: ${result.functions.size}")
            println("Variables: ${result.variables.size}")
            println("References: ${result.references.size}")
            println("Function Calls: ${result.functionCalls.size}")
            println("Conditional Branches: ${result.conditionalBranches.size}")
            println("Loops: ${result.loops.size}")
            
            // Print class information
            println("\nClasses:")
            result.classes.forEach { classInfo ->
                println("- ${classInfo.name}")
                println("  Properties:")
                classInfo.properties.forEach { prop ->
                    println("  - ${prop.name}: ${prop.type} (${if (prop.isVar) "var" else "val"})" +
                            (prop.initializer?.let { " = $it" } ?: ""))
                }
            }
            
            // Print function information
            println("\nFunctions:")
            result.functions.forEach { function ->
                val className = function.containingClass?.let { "$it." } ?: ""
                print("- $className${function.name}: ")
                print(function.parameters.joinToString(", ") { param ->
                    "${if (param.isVararg) "vararg " else ""}${param.name}: ${param.type}" +
                            (param.defaultValue?.let { " = $it" } ?: "")
                })
                println(" -> ${function.returnType}")
            }
            
            // Print conditional branches
            println("\nConditional Branches:")
            result.conditionalBranches.forEach { branch ->
                println("- [${branch.type}] in ${branch.functionName} (line ${branch.lineNumber}): ${branch.condition}")
            }
            
            // Print loop information
            println("\nLoops:")
            result.loops.forEach { loop ->
                when (loop.type) {
                    "for" -> println("- for loop at line ${loop.lineNumber}: ${loop.variable} in ${loop.iterable}")
                    else -> println("- ${loop.type} loop at line ${loop.lineNumber}: ${loop.condition}")
                }
            }
            
            // Print call graph
            println("\nFunction Call Graph:")
            result.callGraph.forEach { (caller, callees) ->
                println("- $caller calls: ${callees.joinToString(", ")}")
            }
            
            // Print line-by-line context for a specific method
            println("\nDetailed Line Context for 'processValue' method:")
            
            // Find the processValue method
            val processValueFunction = result.functions.find { it.name == "processValue" }
            if (processValueFunction != null) {
                // Get start and end lines for the function
                val functionLines = result.lineContexts
                    .filter { it.currentFunction == "processValue" }
                    .sortedBy { it.lineNumber }
                
                functionLines.forEach { lineContext ->
                    println("\nLine ${lineContext.lineNumber}: ${lineContext.code}")
                    println("  Scope: ${lineContext.scope}")
                    println("  Current class: ${lineContext.currentClass}")
                    println("  Current function: ${lineContext.currentFunction}")
                    
                    // Variables in scope
                    val variablesInScope = result.getVariablesInScope(lineContext.filePath, lineContext.lineNumber)
                    if (variablesInScope.isNotEmpty()) {
                        println("  Variables in scope:")
                        variablesInScope.forEach { variable ->
                            println("    - ${variable.name}: ${variable.type}" + 
                                    (if (variable.isParameter) " (parameter)" else ""))
                        }
                    }
                    
                    // Control flow
                    if (lineContext.controlFlowStatement != null) {
                        println("  Control flow: ${lineContext.controlFlowStatement}")
                    }
                    
                    // Function calls
                    if (lineContext.functionCalls.isNotEmpty()) {
                        println("  Function calls:")
                        lineContext.functionCalls.forEach { call ->
                            println("    - ${call.functionName}(${call.arguments.joinToString(", ")})")
                        }
                    }
                    
                    // References
                    if (lineContext.references.isNotEmpty()) {
                        println("  References:")
                        lineContext.references.forEach { ref ->
                            println("    - ${ref.name}")
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            println("Error during analysis: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        } finally {
            // Clean up
            tempDir.deleteRecursively()
        }
    }
} 