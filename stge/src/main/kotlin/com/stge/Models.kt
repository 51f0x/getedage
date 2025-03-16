package com.stge

/**
 * Information about a class property.
 */
data class PropertyInfo(
    val name: String,
    val type: String,
    val isVar: Boolean,
    val initializer: String? = null
)

/**
 * Information about a function parameter.
 */
data class ParameterInfo(
    val name: String,
    val type: String,
    val defaultValue: String? = null,
    val isVararg: Boolean = false
)

/**
 * Container for analysis results.
 */
class AnalysisResult {
    val classes = mutableListOf<ClassInfo>()
    val functions = mutableListOf<FunctionInfo>()
    val conditionalBranches = mutableListOf<ConditionalBranchInfo>()
}

/**
 * Information about a Kotlin class.
 */
data class ClassInfo(
    val name: String,
    val filePath: String,
    val properties: List<PropertyInfo> = emptyList()
)

/**
 * Information about a function.
 */
data class FunctionInfo(
    val name: String,
    val containingClass: String? = null,
    val filePath: String,
    val returnType: String,
    val parameters: List<ParameterInfo> = emptyList()
)

/**
 * Information about conditional branches in code.
 */
data class ConditionalBranchInfo(
    val type: String, // "if", "when", "when-entry"
    val condition: String,
    val functionName: String,
    val filePath: String,
    val lineNumber: Int,
    val parentBranch: ConditionalBranchInfo? = null
) 