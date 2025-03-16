package com.stge

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import java.io.File

/**
 * EnhancedStaticAnalyzer provides comprehensive code analysis with full program context.
 * It captures detailed information about every line of code, including variable scopes,
 * types, control flow, and more.
 */
class EnhancedStaticAnalyzer {
    
    /**
     * Analyzes a list of Kotlin files and returns an enhanced analysis result.
     */
    fun analyzeProject(kotlinFiles: List<File>): EnhancedAnalysisResult {
        val result = EnhancedAnalysisResult()
        
        val disposable = Disposer.newDisposable()
        try {
            println("Debug: Starting to set up the environment")
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
            println("Debug: Environment set up complete")
            
            // Build a map of all KtFiles for cross-referencing
            val ktFileMap = mutableMapOf<String, KtFile>()
            val psiFileFactory = PsiFileFactory.getInstance(environment.project)
            
            // First pass: parse all files
            println("Debug: Starting first pass - parsing files")
            for (file in kotlinFiles) {
                println("Debug: Processing file: ${file.absolutePath}")
                val content = file.readText()
                println("Debug: Content length: ${content.length} chars")
                val psiFile = psiFileFactory.createFileFromText(
                    file.name, 
                    KotlinLanguage.INSTANCE, 
                    content
                )
                
                if (psiFile is KtFile) {
                    println("Debug: Successfully parsed as KtFile: ${file.name}")
                    ktFileMap[file.absolutePath] = psiFile
                    
                    // Basic file info
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        packageName = psiFile.packageFqName.asString(),
                        imports = psiFile.importDirectives.map { it.importedFqName?.asString() ?: "" }
                    )
                    result.files.add(fileInfo)
                    println("Debug: Added file info to result: ${fileInfo.path}, package: ${fileInfo.packageName}")
                } else {
                    println("Debug: Failed to parse as KtFile: ${file.name}, got ${psiFile.javaClass.name}")
                }
            }
            
            // Second pass: detailed analysis with full context
            println("Debug: Starting second pass - detailed analysis")
            for (file in kotlinFiles) {
                println("Debug: Second pass for file: ${file.absolutePath}")
                val ktFile = ktFileMap[file.absolutePath]
                if (ktFile == null) {
                    println("Debug: KtFile not found in map for: ${file.absolutePath}")
                    continue
                }
                analyzeKtFileWithContext(ktFile, result, file.absolutePath, ktFileMap)
            }
            
            // Third pass: build comprehensive call graphs and dependency analysis
            println("Debug: Starting third pass - building call graphs")
            buildCallGraphs(result)
            
            // After the third pass, perform data flow analysis
            println("Debug: Starting data flow analysis")
            performDataFlowAnalysis(result)
            println("Debug: Analysis pipeline complete. Classes: ${result.classes.size}, functions: ${result.functions.size}")
            
        } finally {
            disposable.dispose()
        }
        
        return result
    }
    
    /**
     * Analyzes a single KtFile with full context awareness.
     */
    private fun analyzeKtFileWithContext(
        ktFile: KtFile, 
        result: EnhancedAnalysisResult, 
        filePath: String,
        allFiles: Map<String, KtFile>
    ) {
        println("Debug: Starting analysis for ${filePath}")
        // Dump the entire file content for debugging
        println("Debug: File content start ----")
        println(ktFile.text)
        println("Debug: File content end ----")
        
        // Create line context map for this file
        val lineContexts = mutableMapOf<Int, LineContext>()
        val content = ktFile.text
        val lines = content.lines()
        
        // Initialize basic line context for each line
        for (i in lines.indices) {
            lineContexts[i+1] = LineContext(
                lineNumber = i+1,
                code = lines[i],
                filePath = filePath,
                scope = "global"
            )
        }
        println("Debug: Initialized ${lines.size} line contexts")
        
        // First collect all declarations (classes, functions, properties)
        println("Debug: Starting class visitor")
        val classVisitor = object : KtTreeVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                println("Debug: VISITOR CALLED - Found class: ${klass.name ?: "unnamed"}")
                println("Debug: Class modifiers: ${klass.modifierList?.text}")
                val startPosition = klass.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                val properties = klass.getChildrenOfType<KtProperty>().mapNotNull { property ->
                    PropertyInfo(
                        name = property.name ?: "unknown",
                        type = property.typeReference?.text ?: "Any",
                        isVar = property.isVar,
                        initializer = property.initializer?.text
                    )
                }
                println("Debug: Found ${properties.size} properties in class ${klass.name ?: "unnamed"}")
                
                val classInfo = EnhancedClassInfo(
                    name = klass.fqName?.asString() ?: klass.name ?: "Unknown",
                    filePath = filePath,
                    properties = properties,
                    lineNumber = lineNumber
                )
                
                result.classes.add(classInfo)
                println("Debug: Added class to result: ${classInfo.name}")
                
                // Update line context for class declaration
                val startLine = lineNumber
                val endLine = content.substring(0, klass.textRange.endOffset).count { it == '\n' } + 1
                
                for (line in startLine..endLine) {
                    val context = lineContexts[line]
                    if (context != null) {
                        context.scope = "class:${classInfo.name}"
                        context.currentClass = classInfo.name
                    }
                }
                
                super.visitClass(klass)
            }
            
            override fun visitNamedFunction(function: KtNamedFunction) {
                println("Debug: Found function: ${function.name ?: "unnamed"}, fqName: ${function.fqName}")
                println("Debug: Function modifiers: ${function.modifierList?.text}")
                val startPosition = function.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                val parameters = function.valueParameters.map { param ->
                    ParameterInfo(
                        name = param.name ?: "unknown",
                        type = param.typeReference?.text ?: "Any",
                        defaultValue = param.defaultValue?.text,
                        isVararg = param.isVarArg
                    )
                }
                println("Debug: Found ${parameters.size} parameters in function ${function.name ?: "unnamed"}")
                
                val functionInfo = EnhancedFunctionInfo(
                    name = function.name ?: "unknown",
                    containingClass = function.fqName?.parent()?.asString(),
                    filePath = filePath,
                    returnType = function.typeReference?.text ?: "Unit",
                    parameters = parameters,
                    lineNumber = lineNumber
                )
                
                result.functions.add(functionInfo)
                println("Debug: Added function to result: ${functionInfo.name}, parent: ${functionInfo.containingClass}")
                
                // Update line context for function scope
                val startLine = lineNumber
                val endLine = content.substring(0, function.textRange.endOffset).count { it == '\n' } + 1
                val scopeName = if (functionInfo.containingClass != null) 
                    "method:${functionInfo.containingClass}.${functionInfo.name}" 
                else 
                    "function:${functionInfo.name}"
                
                for (line in startLine..endLine) {
                    val context = lineContexts[line]
                    if (context != null) {
                        context.scope = scopeName
                        context.currentFunction = functionInfo.name
                        
                        // Add function parameters as variables in scope
                        if (line == startLine) {
                            functionInfo.parameters.forEach { param ->
                                context.variables.add(VariableInfo(
                                    name = param.name,
                                    type = param.type,
                                    lineNumber = lineNumber,
                                    scope = scopeName,
                                    isParameter = true
                                ))
                            }
                        }
                    }
                }
                
                super.visitNamedFunction(function)
            }
        }
        
        // Visit the file to collect class and function declarations
        println("Debug: Accepting class visitor")
        
        ktFile.accept(classVisitor)
        println("Debug: Class visitor processing complete")
        
        // Now collect all variables and their scopes
        println("Debug: Starting variable visitor")
        val variableVisitor = object : KtTreeVisitorVoid() {
            override fun visitProperty(property: KtProperty) {
                val startPosition = property.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                // Get containing function or class
                val function = property.getParentOfType<KtNamedFunction>(strict = true)
                val containingClass = property.containingClassOrObject
                
                val scope = when {
                    function != null -> {
                        val className = function.fqName?.parent()?.asString()
                        if (className != null) "method:$className.${function.name}" else "function:${function.name}"
                    }
                    containingClass != null -> "class:${containingClass.fqName?.asString() ?: containingClass.name}"
                    else -> "global"
                }
                
                val variableInfo = VariableInfo(
                    name = property.name ?: "unknown",
                    type = property.typeReference?.text ?: "Any",
                    lineNumber = lineNumber,
                    scope = scope,
                    filePath = filePath,
                    initialValue = property.initializer?.text
                )
                
                result.variables.add(variableInfo)
                
                // Add to line context
                lineContexts[lineNumber]?.variables?.add(variableInfo)
                
                super.visitProperty(property)
            }
            
            override fun visitDestructuringDeclaration(destructuringDeclaration: KtDestructuringDeclaration) {
                val startPosition = destructuringDeclaration.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                // Get containing function
                val function = destructuringDeclaration.getParentOfType<KtNamedFunction>(strict = true)
                val containingClass = destructuringDeclaration.containingClassOrObject
                
                val scope = when {
                    function != null -> {
                        val className = function.fqName?.parent()?.asString()
                        if (className != null) "method:$className.${function.name}" else "function:${function.name}"
                    }
                    containingClass != null -> "class:${containingClass.fqName?.asString() ?: containingClass.name}"
                    else -> "global"
                }
                
                destructuringDeclaration.entries.forEach { entry ->
                    val variableInfo = VariableInfo(
                        name = entry.name ?: "unknown",
                        type = "Any", // Can't easily determine without type inference
                        lineNumber = lineNumber,
                        scope = scope,
                        filePath = filePath,
                        initialValue = destructuringDeclaration.initializer?.text
                    )
                    
                    result.variables.add(variableInfo)
                    
                    // Add to line context
                    lineContexts[lineNumber]?.variables?.add(variableInfo)
                }
                
                super.visitDestructuringDeclaration(destructuringDeclaration)
            }
        }
        
        // Visit the file to collect variable declarations
        ktFile.accept(variableVisitor)
        println("Debug: Variable visitor processing complete")
        
        // Collect control flow elements
        println("Debug: Starting control flow visitor")
        val controlFlowVisitor = object : KtTreeVisitorVoid() {
            override fun visitIfExpression(expression: KtIfExpression) {
                val startPosition = expression.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                val containingFunction = expression.getContainingFunction()
                if (containingFunction != null) {
                    val condition = expression.condition?.text ?: "unknown"
                    val branchInfo = EnhancedConditionalBranchInfo(
                        type = "if",
                        condition = condition,
                        functionName = containingFunction.name ?: "unknown",
                        filePath = filePath,
                        lineNumber = lineNumber
                    )
                    
                    result.conditionalBranches.add(branchInfo)
                    
                    // Add to line context
                    lineContexts[lineNumber]?.controlFlowStatement = "if ($condition)"
                }
                
                super.visitIfExpression(expression)
            }
            
            override fun visitWhenExpression(expression: KtWhenExpression) {
                val startPosition = expression.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                val containingFunction = expression.getContainingFunction()
                if (containingFunction != null) {
                    val subject = expression.subjectExpression?.text ?: "unconditional"
                    val branchInfo = EnhancedConditionalBranchInfo(
                        type = "when",
                        condition = subject,
                        functionName = containingFunction.name ?: "unknown",
                        filePath = filePath,
                        lineNumber = lineNumber
                    )
                    
                    result.conditionalBranches.add(branchInfo)
                    
                    // Add to line context
                    lineContexts[lineNumber]?.controlFlowStatement = "when ($subject)"
                    
                    // Add each entry as a branch
                    expression.entries.forEach { entry ->
                        val entryStartPosition = entry.textRange.startOffset
                        val entryLineNumber = content.substring(0, entryStartPosition).count { it == '\n' } + 1
                        val conditionText = entry.conditions.joinToString(", ") { it.text }
                        
                        val entryBranchInfo = EnhancedConditionalBranchInfo(
                            type = "when-entry",
                            condition = conditionText,
                            functionName = containingFunction.name ?: "unknown",
                            filePath = filePath,
                            lineNumber = entryLineNumber,
                            parentBranch = branchInfo
                        )
                        
                        result.conditionalBranches.add(entryBranchInfo)
                        
                        // Add to line context
                        lineContexts[entryLineNumber]?.controlFlowStatement = "when-entry ($conditionText)"
                    }
                }
                
                super.visitWhenExpression(expression)
            }
            
            override fun visitForExpression(expression: KtForExpression) {
                val startPosition = expression.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                val loopInfo = LoopInfo(
                    type = "for",
                    variable = expression.loopParameter?.name ?: "unknown",
                    iterable = expression.loopRange?.text ?: "unknown",
                    filePath = filePath,
                    lineNumber = lineNumber
                )
                
                result.loops.add(loopInfo)
                
                // Add to line context
                lineContexts[lineNumber]?.controlFlowStatement = "for (${loopInfo.variable} in ${loopInfo.iterable})"
                
                super.visitForExpression(expression)
            }
            
            override fun visitWhileExpression(expression: KtWhileExpression) {
                val startPosition = expression.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                // Check if it's a do-while by inspecting parent nodes
                val isDoWhile = expression.parent is KtDoWhileExpression
                
                val loopInfo = LoopInfo(
                    type = if (isDoWhile) "do-while" else "while",
                    condition = expression.condition?.text ?: "unknown",
                    filePath = filePath,
                    lineNumber = lineNumber
                )
                
                result.loops.add(loopInfo)
                
                // Add to line context
                val loopType = if (isDoWhile) "do-while" else "while"
                lineContexts[lineNumber]?.controlFlowStatement = "$loopType (${loopInfo.condition})"
                
                super.visitWhileExpression(expression)
            }
        }
        
        // Visit the file to collect control flow information
        ktFile.accept(controlFlowVisitor)
        println("Debug: Control flow visitor processing complete")
        
        // Collect references and usages
        println("Debug: Starting reference visitor")
        val referenceVisitor = object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                val startPosition = expression.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                val reference = ReferenceInfo(
                    name = expression.text,
                    referencedName = expression.text, // Simplified - actual identifier name
                    filePath = filePath,
                    lineNumber = lineNumber
                )
                
                result.references.add(reference)
                
                // Add to line context
                lineContexts[lineNumber]?.references?.add(reference)
                
                super.visitReferenceExpression(expression)
            }
            
            override fun visitCallExpression(expression: KtCallExpression) {
                val startPosition = expression.textRange.startOffset
                val lineNumber = content.substring(0, startPosition).count { it == '\n' } + 1
                
                val calleeText = expression.calleeExpression?.text ?: "unknown"
                val functionCaller = expression.getParentOfType<KtNamedFunction>(strict = true)?.name
                
                val callInfo = FunctionCallInfo(
                    functionName = calleeText,
                    callerFunction = functionCaller,
                    filePath = filePath,
                    lineNumber = lineNumber,
                    arguments = expression.valueArguments.map { it.text }
                )
                
                result.functionCalls.add(callInfo)
                
                // Add to line context
                lineContexts[lineNumber]?.functionCalls?.add(callInfo)
                
                super.visitCallExpression(expression)
            }
        }
        
        // Visit the file to collect references and function calls
        ktFile.accept(referenceVisitor)
        println("Debug: Reference visitor processing complete")
        
        // Add line contexts to result
        result.lineContexts.addAll(lineContexts.values)
        println("Debug: Added ${lineContexts.size} line contexts to result")
        println("Debug: Analysis for ${filePath} complete")
    }
    
    /**
     * Builds comprehensive call graphs for the analyzed code.
     */
    private fun buildCallGraphs(result: EnhancedAnalysisResult) {
        val graph = mutableMapOf<String, Set<String>>()
        
        // Build function call graph
        result.functionCalls.forEach { call ->
            val caller = call.callerFunction ?: return@forEach
            
            // Find the full function name
            val callerFunction = result.functions.find { it.name == caller }
            val callerFullName = if (callerFunction?.containingClass != null) {
                "${callerFunction.containingClass}.${callerFunction.name}"
            } else {
                caller
            }
            
            // Add to graph
            if (graph.containsKey(callerFullName)) {
                graph[callerFullName] = graph[callerFullName]!! + call.functionName
            } else {
                graph[callerFullName] = setOf(call.functionName)
            }
        }
        
        result.callGraph = graph
    }
    
    /**
     * Helper method to find the containing function of an expression.
     */
    private fun KtExpression.getContainingFunction(): KtNamedFunction? {
        var parent = this.parent
        while (parent != null && parent !is KtNamedFunction) {
            parent = parent.parent
        }
        return parent as? KtNamedFunction
    }
    
    /**
     * Performs data flow analysis to identify def-use pairs and anomalies.
     */
    private fun performDataFlowAnalysis(result: EnhancedAnalysisResult) {
        // Extract variable definitions and uses from the analyzed code
        collectDefinitionsAndUses(result)
        
        // Build def-use pairs by matching definitions with their uses
        buildDefUsePairs(result)
        
        // Detect data flow anomalies
        detectDataFlowAnomalies(result)
    }
    
    /**
     * Collects all variable definitions and uses from the analyzed code.
     */
    private fun collectDefinitionsAndUses(result: EnhancedAnalysisResult) {
        // Process variable declarations as definitions
        result.variables.forEach { variable ->
            // Each variable declaration is a definition
            result.definitions.add(
                DefinitionInfo(
                    variableName = variable.name,
                    filePath = variable.filePath ?: "",
                    lineNumber = variable.lineNumber,
                    scope = variable.scope,
                    definitionType = if (variable.isParameter) DefinitionType.PARAMETER else DefinitionType.DECLARATION
                )
            )
        }
        
        // Process function parameters as definitions
        result.functions.forEach { function ->
            function.parameters.forEach { param ->
                val functionScope = if (function.containingClass != null) 
                    "method:${function.containingClass}.${function.name}" 
                else 
                    "function:${function.name}"
                
                result.definitions.add(
                    DefinitionInfo(
                        variableName = param.name,
                        filePath = function.filePath,
                        lineNumber = function.lineNumber,
                        scope = functionScope,
                        definitionType = DefinitionType.PARAMETER
                    )
                )
            }
        }
        
        // Process loop variables as definitions
        result.loops.forEach { loop ->
            if (loop.type == "for" && loop.variable != null) {
                // Find the scope from line context
                val lineContext = result.getLineContext(loop.filePath, loop.lineNumber)
                if (lineContext != null) {
                    result.definitions.add(
                        DefinitionInfo(
                            variableName = loop.variable,
                            filePath = loop.filePath,
                            lineNumber = loop.lineNumber,
                            scope = lineContext.scope,
                            definitionType = DefinitionType.LOOP_VARIABLE
                        )
                    )
                }
            }
        }
        
        // Process references as uses
        result.references.forEach { reference ->
            // Extract variable name from reference
            val variableName = reference.referencedName
            
            // Skip if this is not a variable reference (e.g., it's a type reference)
            if (!isLikelyVariableReference(variableName, result)) {
                return@forEach
            }
            
            // Find the scope from line context
            val lineContext = result.getLineContext(reference.filePath, reference.lineNumber)
            if (lineContext != null) {
                // Determine use type based on context
                val useType = determineUseType(reference, lineContext, result)
                
                result.uses.add(
                    UseInfo(
                        variableName = variableName,
                        filePath = reference.filePath,
                        lineNumber = reference.lineNumber,
                        scope = lineContext.scope,
                        useType = useType
                    )
                )
            }
        }
        
        // Process function calls to collect variable uses as arguments
        result.functionCalls.forEach { call ->
            // Find the scope from line context
            val lineContext = result.getLineContext(call.filePath, call.lineNumber)
            if (lineContext != null) {
                // Process each argument
                call.arguments.forEach { arg ->
                    // Try to extract variable name from argument
                    val variableName = extractVariableFromExpression(arg)
                    if (variableName != null && isLikelyVariableReference(variableName, result)) {
                        result.uses.add(
                            UseInfo(
                                variableName = variableName,
                                filePath = call.filePath,
                                lineNumber = call.lineNumber,
                                scope = lineContext.scope,
                                useType = UseType.FUNCTION_ARG
                            )
                        )
                    }
                }
            }
        }
        
        // Process conditional branches to collect variable uses in conditions
        result.conditionalBranches.forEach { branch ->
            // Find variables used in the condition
            val variableNames = extractVariablesFromExpression(branch.condition)
            
            // Find the scope from line context
            val lineContext = result.getLineContext(branch.filePath, branch.lineNumber)
            if (lineContext != null) {
                variableNames.forEach { variableName ->
                    if (isLikelyVariableReference(variableName, result)) {
                        result.uses.add(
                            UseInfo(
                                variableName = variableName,
                                filePath = branch.filePath,
                                lineNumber = branch.lineNumber,
                                scope = lineContext.scope,
                                useType = UseType.CONDITION
                            )
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Builds def-use pairs by matching definitions with their uses.
     */
    private fun buildDefUsePairs(result: EnhancedAnalysisResult) {
        // Group definitions and uses by variable name
        val definitionsByVariable = result.definitions.groupBy { it.variableName }
        val usesByVariable = result.uses.groupBy { it.variableName }
        
        // For each variable, match each use with the most recent definition
        // that comes before the use in the same scope
        usesByVariable.forEach { (variableName, uses) ->
            val definitions = definitionsByVariable[variableName] ?: emptyList()
            
            uses.forEach { use ->
                // Find definitions that:
                // 1. Are in the same or enclosing scope
                // 2. Come before the use
                val validDefinitions = definitions.filter { def ->
                    isInSameOrEnclosingScope(def.scope, use.scope) && 
                    (def.filePath != use.filePath || def.lineNumber < use.lineNumber)
                }
                
                if (validDefinitions.isNotEmpty()) {
                    // Find the most recent definition
                    val mostRecentDef = validDefinitions.maxByOrNull { 
                        if (it.filePath == use.filePath) it.lineNumber else -1 
                    }
                    
                    if (mostRecentDef != null) {
                        // Create a def-use pair
                        result.defUsePairs.add(
                            DefUsePair(
                                definition = mostRecentDef,
                                use = use,
                                variable = variableName
                            )
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Detects data flow anomalies in the code.
     */
    private fun detectDataFlowAnomalies(result: EnhancedAnalysisResult) {
        // 1. Detect undefined uses (variables used without definition)
        result.uses.forEach { use ->
            val hasPair = result.defUsePairs.any { it.use == use }
            if (!hasPair) {
                result.dataFlowAnomalies.add(
                    DataFlowAnomaly(
                        anomalyType = AnomalyType.UNDEFINED_USE,
                        variableName = use.variableName,
                        filePath = use.filePath,
                        lineNumber = use.lineNumber,
                        description = "Variable '${use.variableName}' is used before definition"
                    )
                )
            }
        }
        
        // 2. Detect unused definitions (variables defined but never used)
        result.definitions.forEach { def ->
            val isUsed = result.defUsePairs.any { it.definition == def }
            if (!isUsed) {
                result.dataFlowAnomalies.add(
                    DataFlowAnomaly(
                        anomalyType = AnomalyType.UNUSED_DEFINITION,
                        variableName = def.variableName,
                        filePath = def.filePath,
                        lineNumber = def.lineNumber,
                        description = "Variable '${def.variableName}' is defined but never used"
                    )
                )
            }
        }
        
        // 3. Detect redundant definitions (variables defined multiple times before use)
        val defUsePairsByUse = result.defUsePairs.groupBy { it.use }
        
        defUsePairsByUse.forEach { (use, pairs) ->
            if (pairs.size > 1) {
                // Multiple definitions for the same use
                result.dataFlowAnomalies.add(
                    DataFlowAnomaly(
                        anomalyType = AnomalyType.REDUNDANT_DEFINITION,
                        variableName = use.variableName,
                        filePath = use.filePath,
                        lineNumber = use.lineNumber,
                        description = "Variable '${use.variableName}' has ${pairs.size} definitions before this use"
                    )
                )
            }
        }
        
        // 4. Detect uninitialized uses (variables potentially used without initialization)
        result.defUsePairs.forEach { pair ->
            if (pair.definition.definitionType == DefinitionType.DECLARATION &&
                pair.definition.lineNumber != pair.use.lineNumber) {
                
                // Check if the variable has an initial value or is a parameter
                val varInfo = result.variables.find { 
                    it.name == pair.variable && 
                    it.filePath == pair.definition.filePath && 
                    it.lineNumber == pair.definition.lineNumber 
                }
                
                if (varInfo != null && varInfo.initialValue == null && !varInfo.isParameter) {
                    result.dataFlowAnomalies.add(
                        DataFlowAnomaly(
                            anomalyType = AnomalyType.UNINITIALIZED_USE,
                            variableName = pair.variable,
                            filePath = pair.use.filePath,
                            lineNumber = pair.use.lineNumber,
                            description = "Variable '${pair.variable}' might be used uninitialized"
                        )
                    )
                }
            }
        }
    }
    
    /**
     * Determines if a scope is the same as or encloses another scope.
     */
    private fun isInSameOrEnclosingScope(potentialParentScope: String, childScope: String): Boolean {
        return potentialParentScope == childScope || 
               childScope.startsWith(potentialParentScope + ".") ||
               potentialParentScope == "global"
    }
    
    /**
     * Determines the type of variable use based on context.
     */
    private fun determineUseType(reference: ReferenceInfo, context: LineContext, result: EnhancedAnalysisResult): UseType {
        // Check if this reference is used in a function call
        val isFunctionArg = context.functionCalls.any { 
            it.arguments.any { arg -> arg.contains(reference.name) } 
        }
        if (isFunctionArg) return UseType.FUNCTION_ARG
        
        // Check if this reference is used in a condition
        if (context.controlFlowStatement != null && 
            context.controlFlowStatement!!.contains(reference.name)) {
            return UseType.CONDITION
        }
        
        // Check if this is a return statement
        if (context.code.trim().startsWith("return") && 
            context.code.contains(reference.name)) {
            return UseType.RETURN
        }
        
        // Default to computation use
        return UseType.COMPUTATION
    }
    
    /**
     * Extracts a variable name from an expression, if it's a simple variable reference.
     */
    private fun extractVariableFromExpression(expression: String): String? {
        // This is a simplified implementation
        // In a real implementation, this would use the AST to properly extract variable references
        val trimmed = expression.trim()
        if (trimmed.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) {
            return trimmed
        }
        return null
    }
    
    /**
     * Extracts all potential variable names from an expression.
     */
    private fun extractVariablesFromExpression(expression: String): List<String> {
        // This is a simplified implementation
        // In a real implementation, this would use the AST to properly extract all variable references
        val result = mutableListOf<String>()
        val regex = Regex("[a-zA-Z_][a-zA-Z0-9_]*")
        val matches = regex.findAll(expression)
        
        matches.forEach { match ->
            val potential = match.value
            // Filter out common keywords
            if (!isKeyword(potential)) {
                result.add(potential)
            }
        }
        
        return result
    }
    
    /**
     * Checks if a string is a Kotlin keyword.
     */
    private fun isKeyword(str: String): Boolean {
        val keywords = setOf(
            "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if",
            "in", "interface", "is", "null", "object", "package", "return", "super", "this",
            "throw", "true", "try", "typealias", "val", "var", "when", "while"
        )
        return keywords.contains(str)
    }
    
    /**
     * Checks if a reference is likely to be a variable reference.
     */
    private fun isLikelyVariableReference(name: String, result: EnhancedAnalysisResult): Boolean {
        // Check if this name appears in variable definitions
        if (result.variables.any { it.name == name }) {
            return true
        }
        
        // Check if this name appears as a function parameter
        for (function in result.functions) {
            if (function.parameters.any { it.name == name }) {
                return true
            }
        }
        
        // Check if this name appears as a loop variable
        for (loop in result.loops) {
            if (loop.variable == name) {
                return true
            }
        }
        
        // Not likely a variable reference
        return false
    }
}

/**
 * Container for enhanced analysis results with full program context.
 */
class EnhancedAnalysisResult {
    val files = mutableListOf<FileInfo>()
    val classes = mutableListOf<EnhancedClassInfo>()
    val functions = mutableListOf<EnhancedFunctionInfo>()
    val variables = mutableListOf<VariableInfo>()
    val conditionalBranches = mutableListOf<EnhancedConditionalBranchInfo>()
    val loops = mutableListOf<LoopInfo>()
    val references = mutableListOf<ReferenceInfo>()
    val functionCalls = mutableListOf<FunctionCallInfo>()
    val lineContexts = mutableListOf<LineContext>()
    
    // Derived information
    var callGraph: Map<String, Set<String>> = emptyMap()
    
    // Data flow analysis results
    val definitions = mutableListOf<DefinitionInfo>()
    val uses = mutableListOf<UseInfo>()
    val defUsePairs = mutableListOf<DefUsePair>()
    val dataFlowAnomalies = mutableListOf<DataFlowAnomaly>()
    
    /**
     * Gets the full context for a specific line in a file.
     */
    fun getLineContext(filePath: String, lineNumber: Int): LineContext? {
        return lineContexts.find { it.filePath == filePath && it.lineNumber == lineNumber }
    }
    
    /**
     * Gets all variables in scope at a specific line.
     */
    fun getVariablesInScope(filePath: String, lineNumber: Int): List<VariableInfo> {
        val lineContext = getLineContext(filePath, lineNumber) ?: return emptyList()
        return variables.filter { 
            it.filePath == filePath && 
            it.lineNumber <= lineNumber && 
            it.scope == lineContext.scope 
        }
    }
    
    /**
     * Converts this enhanced result to a standard AnalysisResult for compatibility.
     */
    fun toAnalysisResult(): AnalysisResult {
        val result = AnalysisResult()
        
        // Convert enhanced classes to standard classes
        result.classes.addAll(classes.map { 
            ClassInfo(
                name = it.name,
                filePath = it.filePath,
                properties = it.properties
            )
        })
        
        // Convert enhanced functions to standard functions
        result.functions.addAll(functions.map { 
            FunctionInfo(
                name = it.name,
                containingClass = it.containingClass,
                filePath = it.filePath,
                returnType = it.returnType,
                parameters = it.parameters
            )
        })
        
        // Convert enhanced conditional branches to standard ones
        result.conditionalBranches.addAll(conditionalBranches.map { 
            ConditionalBranchInfo(
                type = it.type,
                condition = it.condition,
                functionName = it.functionName,
                filePath = it.filePath,
                lineNumber = it.lineNumber,
                parentBranch = it.parentBranch?.let { parent ->
                    ConditionalBranchInfo(
                        type = parent.type,
                        condition = parent.condition,
                        functionName = parent.functionName,
                        filePath = parent.filePath,
                        lineNumber = parent.lineNumber
                    )
                }
            )
        })
        
        return result
    }
    
    /**
     * Gets all definitions of a variable.
     */
    fun getDefinitionsForVariable(variableName: String): List<DefinitionInfo> {
        return definitions.filter { it.variableName == variableName }
    }
    
    /**
     * Gets all uses of a variable.
     */
    fun getUsesForVariable(variableName: String): List<UseInfo> {
        return uses.filter { it.variableName == variableName }
    }
    
    /**
     * Gets all def-use pairs for a variable.
     */
    fun getDefUsePairsForVariable(variableName: String): List<DefUsePair> {
        return defUsePairs.filter { it.variable == variableName }
    }
    
    /**
     * Gets all anomalies for a variable.
     */
    fun getAnomaliesForVariable(variableName: String): List<DataFlowAnomaly> {
        return dataFlowAnomalies.filter { it.variableName == variableName }
    }
}

/**
 * Information about a source file.
 */
data class FileInfo(
    val path: String,
    val packageName: String,
    val imports: List<String>
)

/**
 * Enhanced information about a Kotlin class.
 */
data class EnhancedClassInfo(
    val name: String,
    val filePath: String,
    val properties: List<PropertyInfo> = emptyList(),
    val lineNumber: Int = 0
)


/**
 * Enhanced information about a function.
 */
data class EnhancedFunctionInfo(
    val name: String,
    val containingClass: String? = null,
    val filePath: String,
    val returnType: String,
    val parameters: List<ParameterInfo> = emptyList(),
    val lineNumber: Int = 0
)


/**
 * Information about a variable declaration.
 */
data class VariableInfo(
    val name: String,
    val type: String,
    val lineNumber: Int,
    val scope: String,
    val filePath: String? = null,
    val initialValue: String? = null,
    val isParameter: Boolean = false
)

/**
 * Information about a reference to a symbol in the code.
 */
data class ReferenceInfo(
    val name: String,
    val referencedName: String,
    val filePath: String,
    val lineNumber: Int
)

/**
 * Information about a function call.
 */
data class FunctionCallInfo(
    val functionName: String,
    val callerFunction: String? = null,
    val filePath: String,
    val lineNumber: Int,
    val arguments: List<String> = emptyList()
)

/**
 * Enhanced information about conditional branches in code.
 */
data class EnhancedConditionalBranchInfo(
    val type: String, // "if", "when", "when-entry"
    val condition: String,
    val functionName: String,
    val filePath: String,
    val lineNumber: Int,
    val parentBranch: EnhancedConditionalBranchInfo? = null
)

/**
 * Information about a loop in the code.
 */
data class LoopInfo(
    val type: String, // "for", "while", "do-while"
    val condition: String? = null, // For while loops
    val variable: String? = null,  // For for loops
    val iterable: String? = null,  // For for loops
    val filePath: String,
    val lineNumber: Int
)

/**
 * Represents the full context for a specific line of code.
 */
data class LineContext(
    val lineNumber: Int,
    val code: String,
    val filePath: String,
    var scope: String,
    val variables: MutableList<VariableInfo> = mutableListOf(),
    val references: MutableList<ReferenceInfo> = mutableListOf(),
    val functionCalls: MutableList<FunctionCallInfo> = mutableListOf(),
    var controlFlowStatement: String? = null,
    var currentClass: String? = null,
    var currentFunction: String? = null
)

/**
 * Represents a definition of a variable (where it gets a value).
 */
data class DefinitionInfo(
    val variableName: String,
    val filePath: String,
    val lineNumber: Int,
    val scope: String,
    val definitionType: DefinitionType
)

/**
 * Represents a use of a variable (where its value is used).
 */
data class UseInfo(
    val variableName: String,
    val filePath: String,
    val lineNumber: Int,
    val scope: String,
    val useType: UseType
)

/**
 * Types of variable definitions.
 */
enum class DefinitionType {
    DECLARATION,     // Initial declaration (possibly with initialization)
    ASSIGNMENT,      // Assignment to existing variable
    PARAMETER,       // Function parameter
    LOOP_VARIABLE    // Loop iteration variable
}

/**
 * Types of variable uses.
 */
enum class UseType {
    COMPUTATION,     // Used in a computation
    CONDITION,       // Used in a condition
    RETURN,          // Used in a return statement
    FUNCTION_ARG,    // Used as an argument to a function
    ARRAY_INDEX      // Used as an array index
}

/**
 * Represents a def-use pair for a variable.
 */
data class DefUsePair(
    val definition: DefinitionInfo,
    val use: UseInfo,
    val variable: String
)

/**
 * Information about a data flow anomaly.
 */
data class DataFlowAnomaly(
    val anomalyType: AnomalyType,
    val variableName: String,
    val filePath: String,
    val lineNumber: Int,
    val description: String
)

/**
 * Types of data flow anomalies.
 */
enum class AnomalyType {
    UNDEFINED_USE,          // Variable used before definition
    UNUSED_DEFINITION,      // Variable defined but never used
    REDUNDANT_DEFINITION,   // Variable defined multiple times before use
    UNINITIALIZED_USE       // Variable potentially used without initialization
} 
