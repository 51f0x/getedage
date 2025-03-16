package com.stge

import java.io.File

/**
 * A simplified implementation of StaticAnalyzer that uses regex to parse Kotlin code.
 * This is less accurate than the full Kotlin compiler API version but more reliable for quick testing.
 */
class SimpleStaticAnalyzer {
    
    /**
     * Analyzes a list of Kotlin files and returns a comprehensive analysis result.
     */
    fun analyzeProject(kotlinFiles: List<File>): AnalysisResult {
        val result = AnalysisResult()
        
        for (file in kotlinFiles) {
            val content = file.readText()
            analyzeFileContent(file.absolutePath, content, result)
        }
        
        return result
    }
    
    /**
     * Analyzes a single file's content and updates the analysis result accordingly.
     */
    private fun analyzeFileContent(filePath: String, content: String, result: AnalysisResult) {
        // Extract class information using regex patterns
        val classPattern = """class\s+(\w+)""".toRegex()
        val classMatches = classPattern.findAll(content)
        
        for (classMatch in classMatches) {
            val className = classMatch.groupValues[1]
            val classInfo = ClassInfo(
                name = className,
                filePath = filePath,
                properties = extractProperties(content, className)
            )
            result.classes.add(classInfo)
        }
        
        // Extract function information
        val functionPattern = """fun\s+(\w+)\s*\(([^)]*)\)(\s*:\s*([^{]*))?""".toRegex()
        val functionMatches = functionPattern.findAll(content)
        
        for (functionMatch in functionMatches) {
            val functionName = functionMatch.groupValues[1]
            val parameters = functionMatch.groupValues[2]
            val returnType = functionMatch.groupValues[4].trim().ifEmpty { "Unit" }
            
            // Try to determine containing class
            val containingClass = determineContainingClass(content, functionMatch.range.first)
            
            val functionInfo = FunctionInfo(
                name = functionName,
                containingClass = containingClass,
                filePath = filePath,
                returnType = returnType,
                parameters = extractParameters(parameters)
            )
            result.functions.add(functionInfo)
            
            // Extract conditional branches
            extractConditionalBranches(content, functionName, filePath, result)
        }
    }
    
    /**
     * Extracts properties from a class definition.
     */
    private fun extractProperties(content: String, className: String): List<PropertyInfo> {
        val properties = mutableListOf<PropertyInfo>()
        val classContentPattern = """class\s+$className[^{]*\{([^}]*)}""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val classContentMatch = classContentPattern.find(content)
        
        if (classContentMatch != null) {
            val classContent = classContentMatch.groupValues[1]
            val propertyPattern = """(val|var)\s+(\w+)\s*:\s*([^=\n]*)(=\s*([^,\n]*))?""".toRegex()
            val propertyMatches = propertyPattern.findAll(classContent)
            
            for (propertyMatch in propertyMatches) {
                val isVar = propertyMatch.groupValues[1] == "var"
                val name = propertyMatch.groupValues[2]
                val type = propertyMatch.groupValues[3].trim()
                val initializer = propertyMatch.groupValues[5].trim().ifEmpty { null }
                
                properties.add(
                    PropertyInfo(
                        name = name,
                        type = type,
                        isVar = isVar,
                        initializer = initializer
                    )
                )
            }
        }
        
        return properties
    }
    
    /**
     * Extracts parameters from a function parameter list.
     */
    private fun extractParameters(parametersString: String): List<ParameterInfo> {
        val parameters = mutableListOf<ParameterInfo>()
        if (parametersString.isBlank()) return parameters
        
        // Split by commas, but handle generic types correctly
        val paramParts = splitParameterString(parametersString)
        
        for (paramPart in paramParts) {
            // Check for vararg pattern - either "vararg name: Type" or "name: vararg Type"
            val varargPattern = """(?:vararg\s+(\w+)\s*:\s*([^,=]*))|(?:(\w+)\s*:\s*vararg\s+([^,=]*))(?:=\s*([^,]*))?""".toRegex()
            val varargMatch = varargPattern.find(paramPart)
            
            if (varargMatch != null) {
                // This is a vararg parameter
                val name = varargMatch.groupValues[1].ifEmpty { varargMatch.groupValues[3] }
                val type = varargMatch.groupValues[2].ifEmpty { varargMatch.groupValues[4] }.trim()
                val defaultValue = varargMatch.groupValues[5].trim().ifEmpty { null }
                
                parameters.add(
                    ParameterInfo(
                        name = name,
                        type = type,
                        defaultValue = defaultValue,
                        isVararg = true
                    )
                )
            } else {
                // Regular parameter
                val parameterPattern = """(\w+)\s*:\s*([^,=]*)(=\s*([^,]*))?""".toRegex()
                val parameterMatch = parameterPattern.find(paramPart)
                
                if (parameterMatch != null) {
                    val name = parameterMatch.groupValues[1]
                    val type = parameterMatch.groupValues[2].trim()
                    val defaultValue = parameterMatch.groupValues[4].trim().ifEmpty { null }
                    
                    parameters.add(
                        ParameterInfo(
                            name = name,
                            type = type,
                            defaultValue = defaultValue,
                            isVararg = false
                        )
                    )
                }
            }
        }
        
        return parameters
    }
    
    /**
     * Splits a parameter string by commas, but respects nested generic types.
     */
    private fun splitParameterString(parametersString: String): List<String> {
        val parts = mutableListOf<String>()
        var currentPart = StringBuilder()
        var angleDepth = 0
        var parenDepth = 0
        
        for (char in parametersString) {
            when (char) {
                '<' -> {
                    angleDepth++
                    currentPart.append(char)
                }
                '>' -> {
                    angleDepth--
                    currentPart.append(char)
                }
                '(' -> {
                    parenDepth++
                    currentPart.append(char)
                }
                ')' -> {
                    parenDepth--
                    currentPart.append(char)
                }
                ',' -> {
                    if (angleDepth == 0 && parenDepth == 0) {
                        // This comma separates parameters
                        parts.add(currentPart.toString().trim())
                        currentPart = StringBuilder()
                    } else {
                        // This comma is within a generic type or lambda
                        currentPart.append(char)
                    }
                }
                else -> currentPart.append(char)
            }
        }
        
        // Add the last part
        val lastPart = currentPart.toString().trim()
        if (lastPart.isNotEmpty()) {
            parts.add(lastPart)
        }
        
        return parts
    }
    
    /**
     * Determines the containing class of a function based on its position in the file.
     */
    private fun determineContainingClass(content: String, functionPosition: Int): String? {
        val contentBeforeFunction = content.substring(0, functionPosition)
        val classPattern = """class\s+(\w+)""".toRegex()
        val classMatches = classPattern.findAll(contentBeforeFunction).toList()
        
        return if (classMatches.isNotEmpty()) {
            classMatches.last().groupValues[1]
        } else {
            null
        }
    }
    
    /**
     * Extracts conditional branches from a function body.
     */
    private fun extractConditionalBranches(content: String, functionName: String, filePath: String, result: AnalysisResult) {
        // Extract if statements
        val ifPattern = """if\s*\(([^)]*)\)""".toRegex()
        val ifMatches = ifPattern.findAll(content)
        
        for (ifMatch in ifMatches) {
            val condition = ifMatch.groupValues[1]
            val branchInfo = ConditionalBranchInfo(
                type = "if",
                condition = condition,
                functionName = functionName,
                filePath = filePath,
                lineNumber = content.substring(0, ifMatch.range.first).count { it == '\n' } + 1
            )
            result.conditionalBranches.add(branchInfo)
        }
        
        // Extract when statements
        val whenPattern = """when\s*\(([^)]*)\)""".toRegex()
        val whenMatches = whenPattern.findAll(content)
        
        for (whenMatch in whenMatches) {
            val subject = whenMatch.groupValues[1]
            val branchInfo = ConditionalBranchInfo(
                type = "when",
                condition = subject,
                functionName = functionName,
                filePath = filePath,
                lineNumber = content.substring(0, whenMatch.range.first).count { it == '\n' } + 1
            )
            result.conditionalBranches.add(branchInfo)
        }
    }
} 