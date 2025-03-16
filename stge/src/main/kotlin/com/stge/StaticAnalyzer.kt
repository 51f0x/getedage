package com.stge

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import java.io.File
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage

/**
 * StaticAnalyzer is responsible for parsing and analyzing Kotlin source code.
 * It extracts structural information about classes, functions, and control flow
 * to facilitate test generation.
 */
class StaticAnalyzer {

    /**
     * Analyzes a list of Kotlin files and returns a comprehensive analysis result.
     */
    fun analyzeProject(kotlinFiles: List<File>): AnalysisResult {
        val result = AnalysisResult()
        
        val disposable = Disposer.newDisposable()
        try {
            val configuration = CompilerConfiguration()
            configuration.put(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false)
            )
            
            val environment = KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            )
            
            // Parse each file and collect information
            val psiFileFactory = PsiFileFactory.getInstance(environment.project)
            for (file in kotlinFiles) {
                val content = file.readText()
                val psiFile = psiFileFactory.createFileFromText(
                    file.name, 
                    KotlinLanguage.INSTANCE, 
                    content
                )
                if (psiFile is KtFile) {
                    analyzeKtFile(psiFile, result, file.absolutePath)
                }
            }
            
        } finally {
            disposable.dispose()
        }
        
        return result
    }
    
    /**
     * Analyzes a single KtFile and updates the analysis result accordingly.
     */
    private fun analyzeKtFile(ktFile: KtFile, result: AnalysisResult, filePath: String) {
        val visitor = object : KtVisitorVoid() {
            
            override fun visitClass(klass: KtClass) {
                val classInfo = ClassInfo(
                    name = klass.fqName?.asString() ?: klass.name ?: "Unknown",
                    filePath = filePath,
                    properties = klass.getChildrenOfType<KtProperty>().mapNotNull { property ->
                        PropertyInfo(
                            name = property.name ?: "unknown",
                            type = property.typeReference?.text ?: "Any",
                            isVar = property.isVar,
                            initializer = property.initializer?.text
                        )
                    }
                )
                
                result.classes.add(classInfo)
                super.visitClass(klass)
            }
            
            override fun visitNamedFunction(function: KtNamedFunction) {
                val functionInfo = FunctionInfo(
                    name = function.name ?: "unknown",
                    containingClass = function.fqName?.parent()?.asString(),
                    filePath = filePath,
                    returnType = function.typeReference?.text ?: "Unit",
                    parameters = function.valueParameters.map { param ->
                        ParameterInfo(
                            name = param.name ?: "unknown",
                            type = param.typeReference?.text ?: "Any",
                            defaultValue = param.defaultValue?.text,
                            isVararg = param.isVarArg
                        )
                    }
                )
                
                result.functions.add(functionInfo)
                
                // Analyze the function body for conditionals
                analyzeConditionals(function, functionInfo.name, filePath, result)
                
                super.visitNamedFunction(function)
            }
            
            override fun visitIfExpression(expression: KtIfExpression) {
                val containingFunction = expression.getContainingFunction()
                if (containingFunction != null) {
                    val condition = expression.condition?.text ?: "unknown"
                    val branchInfo = ConditionalBranchInfo(
                        type = "if",
                        condition = condition,
                        functionName = containingFunction.name ?: "unknown",
                        filePath = filePath,
                        lineNumber = expression.node.startOffset
                    )
                    result.conditionalBranches.add(branchInfo)
                }
                super.visitIfExpression(expression)
            }
            
            override fun visitWhenExpression(expression: KtWhenExpression) {
                val containingFunction = expression.getContainingFunction()
                if (containingFunction != null) {
                    val subject = expression.subjectExpression?.text ?: "unconditional"
                    val branchInfo = ConditionalBranchInfo(
                        type = "when",
                        condition = subject,
                        functionName = containingFunction.name ?: "unknown",
                        filePath = filePath,
                        lineNumber = expression.node.startOffset
                    )
                    result.conditionalBranches.add(branchInfo)
                    
                    // Add each entry as a branch
                    expression.entries.forEach { entry ->
                        val conditionText = entry.conditions.joinToString(", ") { it.text }
                        val entryBranchInfo = ConditionalBranchInfo(
                            type = "when-entry",
                            condition = conditionText,
                            functionName = containingFunction.name ?: "unknown",
                            filePath = filePath,
                            lineNumber = entry.node.startOffset,
                            parentBranch = branchInfo
                        )
                        result.conditionalBranches.add(entryBranchInfo)
                    }
                }
                super.visitWhenExpression(expression)
            }
        }
        
        ktFile.accept(visitor)
    }
    
    /**
     * Analyzes conditionals in a function body.
     */
    private fun analyzeConditionals(function: KtNamedFunction, functionName: String, filePath: String, result: AnalysisResult) {
        // The visitor handles this more efficiently
    }
    
    /**
     * Helper method to find the containing function of an expression.
     */
    private fun KtIfExpression.getContainingFunction(): KtNamedFunction? {
        var parent = this.parent
        while (parent != null && parent !is KtNamedFunction) {
            parent = parent.parent
        }
        return parent as? KtNamedFunction
    }
    
    /**
     * Helper method to find the containing function of an expression.
     */
    private fun KtWhenExpression.getContainingFunction(): KtNamedFunction? {
        var parent = this.parent
        while (parent != null && parent !is KtNamedFunction) {
            parent = parent.parent
        }
        return parent as? KtNamedFunction
    }
}

/**
 * Container for all analysis results.
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
 * Information about a class property.
 */
data class PropertyInfo(
    val name: String,
    val type: String,
    val isVar: Boolean,
    val initializer: String? = null
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
 * Information about a function parameter.
 */
data class ParameterInfo(
    val name: String,
    val type: String,
    val defaultValue: String? = null,
    val isVararg: Boolean = false
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