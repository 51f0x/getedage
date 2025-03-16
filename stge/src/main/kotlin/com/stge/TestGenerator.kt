package com.stge

import java.io.File
import java.util.regex.Pattern
import kotlin.random.Random

/**
 * TestGenerator is responsible for generating test cases based on static analysis. It creates
 * comprehensive test cases to achieve full code coverage.
 */
class TestGenerator {
    
    // Map to store function call examples found in the source code
    private val functionCallExamples = mutableMapOf<String, List<String>>()

    /** Generates test cases from analysis results. */
    fun generateTestCases(analysis: AnalysisResult): List<TestCase> {
        // Scan the source code for function calls to build a reference for varargs
        scanForFunctionCalls(analysis)

        val testCases = mutableListOf<TestCase>()
        
        // Group functions by their containing class
        val functionsByClass = analysis.functions.groupBy { it.containingClass }
        
        // For each class, generate test cases
        for ((className, functions) in functionsByClass) {
            if (className == null) {
                // Top-level functions get their own test class
                for (function in functions) {
                    testCases.addAll(generateTestCasesForFunction(function, analysis))
                }
                continue
            }
            
            // Find the class info
            val classInfo = analysis.classes.find { it.name == className }
            if (classInfo != null) {
                testCases.addAll(generateTestCasesForClass(classInfo, functions, analysis))
            }
        }
        
        return testCases
    }
    
    /**
     * Scans all source files to find examples of function calls, especially focusing on vararg
     * parameter usage.
     */
    private fun scanForFunctionCalls(analysis: AnalysisResult) {
        // Get unique file paths from the analysis
        val filePaths =
                (analysis.functions.map { it.filePath } + analysis.classes.map { it.filePath })
                        .distinct()

        // Process each file to find function calls
        for (filePath in filePaths) {
            try {
                val fileContent = File(filePath).readText()

                // For each function in our analysis
                for (function in analysis.functions) {
                    // Check if this function has any vararg parameters
                    val hasVarargs = function.parameters.any { it.isVararg }
                    if (!hasVarargs) continue

                    // Look for calls to this function
                    val functionName = function.name
                    val pattern = Pattern.compile("\\b$functionName\\s*\\(([^)]*)")
                    val matcher = pattern.matcher(fileContent)

                    val calls = mutableListOf<String>()
                    while (matcher.find()) {
                        val arguments = matcher.group(1)
                        calls.add(arguments.trim())
                    }

                    if (calls.isNotEmpty()) {
                        val key =
                                if (function.containingClass != null) {
                                    "${function.containingClass}.${function.name}"
                                } else {
                                    function.name
                                }
                        functionCallExamples[key] = calls
                    }
                }
            } catch (e: Exception) {
                // Skip files that can't be read
                continue
            }
        }
    }

    /** Generates test cases for top-level functions. */
    private fun generateTestCasesForFunction(
        function: FunctionInfo,
        analysis: AnalysisResult
    ): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        
        // Create a basic test case that calls the function
        val testCase =
                TestCase(
            name = "test${function.name.capitalize()}",
            targetClassName = function.containingClass,
            targetFunction = function.name,
            fileName = "${function.name.capitalize()}Test",
            testCode = generateBasicTestCode(function, analysis),
            imports = generateImportsForTest(function)
        )
        
        testCases.add(testCase)
        
        // Find conditional branches in this function
        val branches =
                analysis.conditionalBranches.filter {
            it.functionName == function.name && 
            it.parentBranch == null && 
            (function.containingClass == null || it.filePath == function.filePath)
        }
        
        // Generate test cases for each branch
        for (branch in branches) {
            testCases.addAll(generateTestCasesForBranch(function, branch, analysis))
        }
        
        return testCases
    }
    
    /** Generates test cases for a class. */
    private fun generateTestCasesForClass(
        classInfo: ClassInfo,
        functions: List<FunctionInfo>,
        analysis: AnalysisResult
    ): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        
        // Create a basic test class with setup
        val testClassName = "${classInfo.name.split('.').last()}Test"
        val fileName = testClassName
        
        // Generate common setup code
        val setupCode = generateSetupCode(classInfo, functions)
        
        // Generate test cases for each method
        for (function in functions) {
            // Skip constructors for now (they'll be tested through other methods)
            if (function.name == "init" || function.name == "<init>") continue
            
            val testCaseName = "test${function.name.capitalize()}"
            val testCode = generateMethodTestCode(classInfo, function, analysis)
            
            val testCase =
                    TestCase(
                name = testCaseName,
                targetClassName = classInfo.name,
                targetFunction = function.name,
                fileName = fileName,
                setupCode = setupCode,
                testCode = testCode,
                imports = generateImportsForTest(function, classInfo)
            )
            
            testCases.add(testCase)
            
            // Find conditional branches in this function
            val branches =
                    analysis.conditionalBranches.filter {
                        it.functionName == function.name && it.parentBranch == null
            }
            
            // Generate test cases for each branch
            for (branch in branches) {
                testCases.addAll(
                        generateTestCasesForBranch(function, branch, analysis, classInfo, setupCode)
                )
            }
        }
        
        return testCases
    }
    
    /** Generates test cases for conditional branches. */
    private fun generateTestCasesForBranch(
        function: FunctionInfo,
        branch: ConditionalBranchInfo,
        analysis: AnalysisResult,
        classInfo: ClassInfo? = null,
        setupCode: String? = null
    ): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        
        when (branch.type) {
            "if" -> {
                // Generate a test for the true condition
                val testCaseTrue =
                        TestCase(
                                name =
                                        "test${function.name.capitalize()}When${branch.condition.toValidMethodName()}True",
                    targetClassName = function.containingClass ?: "TopLevel",
                    targetFunction = function.name,
                                fileName = classInfo?.name?.split('.')?.last()?.plus("Test")
                                                ?: "${function.name.capitalize()}Test",
                    setupCode = setupCode,
                                testCode =
                                        generateBranchTestCode(
                                                function,
                                                branch,
                                                true,
                                                analysis,
                                                classInfo
                                        ),
                    imports = generateImportsForTest(function, classInfo)
                )
                testCases.add(testCaseTrue)
                
                // Generate a test for the false condition
                val testCaseFalse =
                        TestCase(
                                name =
                                        "test${function.name.capitalize()}When${branch.condition.toValidMethodName()}False",
                    targetClassName = function.containingClass ?: "TopLevel",
                    targetFunction = function.name,
                                fileName = classInfo?.name?.split('.')?.last()?.plus("Test")
                                                ?: "${function.name.capitalize()}Test",
                    setupCode = setupCode,
                                testCode =
                                        generateBranchTestCode(
                                                function,
                                                branch,
                                                false,
                                                analysis,
                                                classInfo
                                        ),
                    imports = generateImportsForTest(function, classInfo)
                )
                testCases.add(testCaseFalse)
            }
            "when" -> {
                // Find child branches representing when entries
                val entries = analysis.conditionalBranches.filter { it.parentBranch == branch }
                
                // Generate a test for each when entry
                for (entry in entries) {
                    val testCase =
                            TestCase(
                                    name =
                                            "test${function.name.capitalize()}When${entry.condition.toValidMethodName()}",
                        targetClassName = function.containingClass ?: "TopLevel",
                        targetFunction = function.name,
                                    fileName = classInfo?.name?.split('.')?.last()?.plus("Test")
                                                    ?: "${function.name.capitalize()}Test",
                        setupCode = setupCode,
                                    testCode =
                                            generateWhenEntryTestCode(
                                                    function,
                                                    branch,
                                                    entry,
                                                    analysis,
                                                    classInfo
                                            ),
                        imports = generateImportsForTest(function, classInfo)
                    )
                    testCases.add(testCase)
                }
            }
        }
        
        return testCases
    }
    
    /** Generates parameter values for a function call, handling varargs appropriately. */
    private fun generateParameterValues(function: FunctionInfo): String {
        return function.parameters
                .mapIndexed { index, param ->
                    if (param.isVararg) {
                        generateVarargValue(function, param, index)
                    } else {
                        generateValueForType(param.type)
                    }
                }
                .joinToString(", ")
    }

    /** Generates a value for a vararg parameter, using lookahead if possible. */
    private fun generateVarargValue(
            function: FunctionInfo,
            param: ParameterInfo,
            paramIndex: Int
    ): String {
        // Try to find an example in the scanned code
        val functionKey =
                if (function.containingClass != null) {
                    "${function.containingClass}.${function.name}"
                } else {
                    function.name
                }

        val examples = functionCallExamples[functionKey]
        if (examples != null && examples.isNotEmpty()) {
            for (example in examples) {
                // Parse the arguments
                val args = parseArguments(example)

                // Check if we have enough arguments and if there are multiple values for the vararg
                if (args.size > paramIndex) {
                    val varargArgs = args.subList(paramIndex, args.size)
                    if (varargArgs.isNotEmpty()) {
                        return varargArgs.joinToString(", ")
                    }
                }
            }
        }

        // If no suitable example was found, generate a random number of values
        val count = Random.nextInt(1, 4)
        return (1..count).joinToString(", ") { generateValueForType(param.type) }
    }

    /** Parses a comma-separated argument list, handling nested commas in collections. */
    private fun parseArguments(argsString: String): List<String> {
        val args = mutableListOf<String>()
        var currentArg = StringBuilder()
        var depth = 0

        for (char in argsString) {
            when (char) {
                '(', '[', '{' -> {
                    depth++
                    currentArg.append(char)
                }
                ')', ']', '}' -> {
                    depth--
                    currentArg.append(char)
                }
                ',' -> {
                    if (depth == 0) {
                        // This comma separates top-level arguments
                        args.add(currentArg.toString().trim())
                        currentArg = StringBuilder()
                    } else {
                        // This comma is within a nested structure
                        currentArg.append(char)
                    }
                }
                else -> currentArg.append(char)
            }
        }

        // Add the last argument if there is one
        val lastArg = currentArg.toString().trim()
        if (lastArg.isNotEmpty()) {
            args.add(lastArg)
        }

        return args
    }

    /** Generates basic test code for a function. */
    private fun generateBasicTestCode(function: FunctionInfo, analysis: AnalysisResult): String {
        val sb = StringBuilder()
        sb.appendLine("@Test")
        sb.appendLine("fun ${function.name}Basic() {")
        
        // Generate parameter values
        val paramValues = generateParameterValues(function)
        
        // Call the function
        if (function.returnType != "Unit") {
            sb.appendLine("    val result = ${function.name}($paramValues)")
            sb.appendLine("    assertNotNull(result) // Basic coverage check")
        } else {
            sb.appendLine("    ${function.name}($paramValues)")
            sb.appendLine(
                    "    // Basic coverage verification - ensures function executes without exceptions"
            )
        }
        
        sb.appendLine("}")
        return sb.toString()
    }
    
    /** Generates test code for a method in a class. */
    private fun generateMethodTestCode(
        classInfo: ClassInfo,
        function: FunctionInfo,
        analysis: AnalysisResult
    ): String {
        val sb = StringBuilder()
        sb.appendLine("@Test")
        sb.appendLine("fun ${function.name}Basic() {")
        
        // Generate parameter values
        val paramValues = generateParameterValues(function)
        
        // Call the method on the instance
        if (function.returnType != "Unit") {
            sb.appendLine("    val result = testInstance.${function.name}($paramValues)")
            sb.appendLine("    assertNotNull(result)")
        } else {
            sb.appendLine("    testInstance.${function.name}($paramValues)")
            sb.appendLine("    // Verify no exceptions were thrown")
        }
        
        sb.appendLine("}")
        return sb.toString()
    }
    
    /** Generates branch test code ensuring coverage of all conditions with specific assertions */
    private fun generateBranchTestCode(
        function: FunctionInfo,
        branch: ConditionalBranchInfo,
        conditionValue: Boolean,
        analysis: AnalysisResult,
        classInfo: ClassInfo? = null
    ): String {
        val sb = StringBuilder()
        sb.appendLine("@Test")
        sb.appendLine(
                "fun ${function.name}When${branch.condition.toValidMethodName()}${if (conditionValue) "True" else "False"}() {"
        )
        
        // Set up parameters that would make the condition evaluate to the desired value
        val paramSetups = mutableListOf<String>()
        for (param in function.parameters) {
            val paramName = param.name
            val paramType = param.type
            
            // Check if this parameter is used in the condition
            if (branch.condition.contains(paramName)) {
                if (conditionValue) {
                    // Try to make it true
                    when {
                        paramType.contains("Boolean") -> paramSetups.add("val $paramName = true")
                        paramType.contains("Int") || paramType.contains("Long") -> {
                            if (branch.condition.contains("$paramName > ")) {
                                paramSetups.add("val $paramName = 100") // A large value
                            } else if (branch.condition.contains("$paramName < ")) {
                                paramSetups.add("val $paramName = 0") // A small value
                            } else if (branch.condition.contains("$paramName ==")) {
                                // Extract the value it's compared to
                                val parts = branch.condition.split("==")
                                if (parts.size > 1) {
                                    val valueStr = parts[1].trim()
                                    paramSetups.add("val $paramName = $valueStr")
                                } else {
                                    paramSetups.add("val $paramName = 1")
                                }
                            } else {
                                paramSetups.add("val $paramName = 1")
                            }
                        }
                        paramType.contains("String") -> {
                            if (branch.condition.contains("$paramName.is")) {
                                paramSetups.add("val $paramName = \"test\"")
                            } else if (branch.condition.contains("$paramName ==")) {
                                // Extract the value it's compared to
                                val parts = branch.condition.split("==")
                                if (parts.size > 1) {
                                    val valueStr = parts[1].trim().replace("\"", "\\\"")
                                    paramSetups.add("val $paramName = \"$valueStr\"")
                                } else {
                                    paramSetups.add("val $paramName = \"test value\"")
                                }
                            } else {
                                paramSetups.add("val $paramName = \"non-empty string\"")
                            }
                        }
                        else ->
                                paramSetups.add(
                                        "val $paramName = ${generateValueForType(paramType)}"
                                )
                    }
                } else {
                    // Try to make it false
                    when {
                        paramType.contains("Boolean") -> paramSetups.add("val $paramName = false")
                        paramType.contains("Int") || paramType.contains("Long") -> {
                            if (branch.condition.contains("$paramName > ")) {
                                paramSetups.add("val $paramName = 0") // A small value
                            } else if (branch.condition.contains("$paramName < ")) {
                                paramSetups.add("val $paramName = 100") // A large value
                            } else if (branch.condition.contains("$paramName ==")) {
                                // Choose a different value than the one it's compared to
                                paramSetups.add("val $paramName = -1")
                            } else {
                                paramSetups.add("val $paramName = 0")
                            }
                        }
                        paramType.contains("String") -> {
                            if (branch.condition.contains("$paramName.is")) {
                                paramSetups.add("val $paramName = \"\"")
                            } else if (branch.condition.contains("$paramName ==")) {
                                paramSetups.add("val $paramName = \"different value\"")
                            } else {
                                paramSetups.add("val $paramName = \"\"")
                            }
                        }
                        else -> paramSetups.add("val $paramName = null")
                    }
                }
            } else {
                // Just use a default value
                paramSetups.add("val $paramName = ${generateValueForType(paramType)}")
            }
        }
        
        // Add parameter setups
        for (setup in paramSetups) {
            sb.appendLine("    $setup")
        }
        sb.appendLine()

        // Add code comment to highlight branch coverage purpose
        sb.appendLine(
                "    // Testing branch coverage for condition: '${branch.condition}' is ${if (conditionValue) "TRUE" else "FALSE"}"
        )
        
        // Call the function with the parameters
        val paramCalls = function.parameters.joinToString(", ") { it.name }
        
        // Generate branch-specific assertions based on the branch condition and expected behavior
        val branchSpecificAssertions =
                generateBranchSpecificAssertions(function, branch, conditionValue, paramCalls)

        if (classInfo != null) {
            // Method in a class
            if (function.returnType != "Unit") {
                sb.appendLine("    val result = testInstance.${function.name}($paramCalls)")

                // Add branch-specific assertions for class methods with return values
                for (assertion in branchSpecificAssertions) {
                    sb.appendLine("    $assertion")
                }
            } else {
                sb.appendLine("    testInstance.${function.name}($paramCalls)")

                // Add branch-specific assertions for void class methods
                for (assertion in branchSpecificAssertions) {
                    sb.appendLine("    $assertion")
                }
            }
        } else {
            // Top-level function
            if (function.returnType != "Unit") {
                sb.appendLine("    val result = ${function.name}($paramCalls)")

                // Add branch-specific assertions for top-level functions with return values
                for (assertion in branchSpecificAssertions) {
                    sb.appendLine("    $assertion")
                }
            } else {
                sb.appendLine("    ${function.name}($paramCalls)")

                // Add branch-specific assertions for void top-level functions
                for (assertion in branchSpecificAssertions) {
                    sb.appendLine("    $assertion")
                }
            }
        }
        
        sb.appendLine("}")
        return sb.toString()
    }
    
    /**
     * Generates specific assertions for branch verification based on branch condition and
     * parameters
     */
    private fun generateBranchSpecificAssertions(
            function: FunctionInfo,
            branch: ConditionalBranchInfo,
            conditionValue: Boolean,
            paramCalls: String
    ): List<String> {
        val assertions = mutableListOf<String>()

        // First make sure we have a basic assertion
        if (function.returnType != "Unit") {
            assertions.add("assertNotNull(result) // Basic verification")
        }

        // Extract the primary parameter involved in the condition
        val paramName = extractPrimaryParameterFromCondition(branch.condition)

        // Generate appropriate assertions based on the function's return type and branch condition
        when {
            // For numeric return types, generate assertions that check specific numeric conditions
            function.returnType.contains("Int") ||
                    function.returnType.contains("Long") ||
                    function.returnType.contains("Double") ||
                    function.returnType.contains("Float") -> {
                if (branch.condition.contains(">")) {
                    if (conditionValue) {
                        assertions.add(
                                "assertTrue(result > 0, \"Expected positive result for condition '${branch.condition}'\")"
                        )
                    } else {
                        assertions.add(
                                "assertTrue(result <= 0, \"Expected non-positive result for condition '${branch.condition}'\")"
                        )
                    }
                } else if (branch.condition.contains("<")) {
                    if (conditionValue) {
                        assertions.add(
                                "assertTrue(result < 100, \"Expected small result for condition '${branch.condition}'\")"
                        )
                    } else {
                        assertions.add(
                                "assertTrue(result >= 100, \"Expected large result for condition '${branch.condition}'\")"
                        )
                    }
                } else if (branch.condition.contains("==") && paramName != null) {
                    // Check for specific equality conditions
                    if (conditionValue) {
                        assertions.add(
                                "assertEquals($paramName, result, \"Result should match parameter for condition '${branch.condition}'\")"
                        )
                    } else {
                        assertions.add(
                                "assertNotEquals($paramName, result, \"Result should not match parameter for condition '${branch.condition}'\")"
                        )
                    }
                } else {
                    // Generic numeric assertion
                    assertions.add(
                            "// Verify result is appropriate for branch: ${branch.condition} = ${conditionValue}"
                    )
                    assertions.add("// For example:")
                    if (conditionValue) {
                        assertions.add(
                                "assertTrue(result >= 0, \"Expected non-negative result for TRUE condition\")"
                        )
                    } else {
                        assertions.add(
                                "assertTrue(result >= 0, \"Expected appropriate result for FALSE condition\")"
                        )
                    }
                }
            }

            // For string return types
            function.returnType.contains("String") -> {
                if (branch.condition.contains(".isEmpty()") ||
                                branch.condition.contains(".isBlank()")
                ) {
                    if (conditionValue) {
                        assertions.add(
                                "assertTrue(result.isNotEmpty(), \"Expected non-empty result for condition '${branch.condition}'\")"
                        )
                    } else {
                        assertions.add(
                                "assertTrue(result.contains(\"empty\") || result.isEmpty(), \"Result should indicate emptiness for FALSE branch\")"
                        )
                    }
                } else if (branch.condition.contains("==") && paramName != null) {
                    // Check for specific string equality
                    if (conditionValue) {
                        assertions.add(
                                "assertTrue(result.contains($paramName), \"Result should contain parameter value for TRUE condition\")"
                        )
                    } else {
                        assertions.add(
                                "assertFalse(result.contains($paramName), \"Result should not contain parameter value for FALSE condition\")"
                        )
                    }
                } else {
                    // Generic string assertion
                    if (branch.type == "if" && branch.condition.contains("==")) {
                        val expectedValue = extractExpectedValueFromCondition(branch.condition)
                        if (expectedValue != null) {
                            if (conditionValue) {
                                assertions.add(
                                        "assertTrue(result.contains(\"$expectedValue\"), \"Result should reflect TRUE branch\")"
                                )
                            } else {
                                assertions.add(
                                        "assertFalse(result.contains(\"$expectedValue\"), \"Result should reflect FALSE branch\")"
                                )
                            }
                        } else {
                            assertions.add(
                                    "assertNotNull(result, \"Result should be valid for branch: ${branch.condition} = ${conditionValue}\")"
                            )
                        }
                    } else {
                        assertions.add(
                                "assertNotNull(result, \"Result should be valid for branch: ${branch.condition} = ${conditionValue}\")"
                        )
                    }
                }
            }

            // For boolean return types
            function.returnType.contains("Boolean") -> {
                if (branch.condition.contains("==") ||
                                branch.condition.contains(">") ||
                                branch.condition.contains("<")
                ) {
                    // For direct condition checks
                    if (conditionValue) {
                        assertions.add(
                                "assertTrue(result, \"Expected TRUE result for TRUE branch condition\")"
                        )
                    } else {
                        assertions.add(
                                "assertFalse(result, \"Expected FALSE result for FALSE branch condition\")"
                        )
                    }
                } else {
                    // Generic boolean assertion
                    assertions.add(
                            "assertEquals(${conditionValue}, result, \"Result should match condition state\")"
                    )
                }
            }

            // For exception cases
            branch.condition.contains("throws") || branch.condition.contains("exception") -> {
                assertions.add(
                        "// This test verifies the code does not throw an exception when ${if(conditionValue) "TRUE" else "FALSE"}"
                )
                assertions.add(
                        "// No assertion needed as the test would fail if exception was thrown"
                )
            }

            // For void functions that may have side effects
            function.returnType == "Unit" -> {
                assertions.add(
                        "// For void methods, verify that the method executed without exceptions"
                )
                assertions.add(
                        "// This confirms the ${if(conditionValue) "TRUE" else "FALSE"} branch was taken"
                )
                assertions.add(
                        "// In a real test, add assertions to check any side effects caused by this branch"
                )
            }

            // Fall back for other return types
            else -> {
                assertions.add(
                        "assertNotNull(result, \"Result should be valid for branch: ${branch.condition} = ${conditionValue}\")"
                )
                assertions.add("// Specific assertion for this branch: ${branch.condition}")
                assertions.add(
                        "// Add additional assertions based on expected behavior in this branch"
                )
            }
        }

        return assertions
    }

    /** Extracts the primary parameter name from a condition */
    private fun extractPrimaryParameterFromCondition(condition: String): String? {
        // Match patterns like "paramName > 5" or "paramName.isEmpty()"
        val paramPattern = """(\w+)(?:\s*[><=!]+|\s*\.\w+\(\))""".toRegex()
        val match = paramPattern.find(condition)
        return match?.groupValues?.get(1)
    }

    /** Extracts the expected value from a condition like "x == 5" -> "5" */
    private fun extractExpectedValueFromCondition(condition: String): String? {
        val equalityPattern = """\w+\s*==\s*["']?([^"']+?)["']?${'$'}""".toRegex()
        val match = equalityPattern.find(condition)
        return match?.groupValues?.get(1)?.trim()
    }

    /**
     * Generates when entry test code ensuring coverage of all conditions with specific assertions
     */
    private fun generateWhenEntryTestCode(
        function: FunctionInfo,
        whenBranch: ConditionalBranchInfo,
        entry: ConditionalBranchInfo,
        analysis: AnalysisResult,
        classInfo: ClassInfo? = null
    ): String {
        val sb = StringBuilder()
        sb.appendLine("@Test")
        sb.appendLine("fun ${function.name}When${entry.condition.toValidMethodName()}() {")
        
        // Extract the subject of the when expression
        val subjectName = whenBranch.condition
        
        // Find a parameter that matches the subject name
        val subjectParam = function.parameters.find { it.name == subjectName }
        
        val paramSetups = mutableListOf<String>()
        
        if (subjectParam != null) {
            // Set up the parameter to match this entry's condition
            val condition = entry.condition
            val paramType = subjectParam.type
            
            when {
                paramType.contains("Int") || paramType.contains("Long") -> {
                    // Try to extract a literal value from the condition
                    if (condition.matches(Regex("\\d+"))) {
                        paramSetups.add("val $subjectName = $condition")
                    } else if (condition.contains("..")) {
                        // It's a range
                        val rangeParts = condition.split("..")
                        if (rangeParts.size == 2) {
                            val start = rangeParts[0].trim()
                            paramSetups.add("val $subjectName = $start")
                        } else {
                            paramSetups.add("val $subjectName = 1")
                        }
                    } else {
                        paramSetups.add("val $subjectName = 1")
                    }
                }
                paramType.contains("String") -> {
                    if (condition.startsWith("\"") && condition.endsWith("\"")) {
                        paramSetups.add("val $subjectName = $condition")
                    } else {
                        paramSetups.add("val $subjectName = \"${condition.replace("\"", "\\\"")}\"")
                    }
                }
                paramType.contains("Boolean") -> {
                    paramSetups.add("val $subjectName = ${condition.toBoolean()}")
                }
                else -> {
                    paramSetups.add("val $subjectName = ${generateValueForType(paramType)}")
                }
            }
        }
        
        // Set up other parameters with default values
        for (param in function.parameters) {
            if (param.name != subjectName) {
                paramSetups.add("val ${param.name} = ${generateValueForType(param.type)}")
            }
        }
        
        // Add parameter setups
        for (setup in paramSetups) {
            sb.appendLine("    $setup")
        }
        sb.appendLine()

        // Add code comment to highlight branch coverage purpose
        sb.appendLine(
                "    // Testing when branch coverage for value: '${entry.condition}' in when expression on '$subjectName'"
        )
        
        // Call the function with the parameters
        val paramCalls = function.parameters.joinToString(", ") { it.name }
        
        // Generate when-branch specific assertions
        val whenBranchAssertions =
                generateWhenBranchAssertions(
                        function,
                        whenBranch,
                        entry,
                        subjectName,
                        entry.condition
                )

        if (classInfo != null) {
            // Method in a class
            if (function.returnType != "Unit") {
                sb.appendLine("    val result = testInstance.${function.name}($paramCalls)")

                // Add when-branch specific assertions
                for (assertion in whenBranchAssertions) {
                    sb.appendLine("    $assertion")
                }
            } else {
                sb.appendLine("    testInstance.${function.name}($paramCalls)")

                // Add when-branch specific assertions for void methods
                for (assertion in whenBranchAssertions) {
                    sb.appendLine("    $assertion")
                }
            }
        } else {
            // Top-level function
            if (function.returnType != "Unit") {
                sb.appendLine("    val result = ${function.name}($paramCalls)")

                // Add when-branch specific assertions
                for (assertion in whenBranchAssertions) {
                    sb.appendLine("    $assertion")
                }
            } else {
                sb.appendLine("    ${function.name}($paramCalls)")

                // Add when-branch specific assertions for void functions
                for (assertion in whenBranchAssertions) {
                    sb.appendLine("    $assertion")
                }
            }
        }
        
        sb.appendLine("}")
        return sb.toString()
    }
    
    /** Generates specific assertions for when-branch verification */
    private fun generateWhenBranchAssertions(
            function: FunctionInfo,
            whenBranch: ConditionalBranchInfo,
            entry: ConditionalBranchInfo,
            subjectName: String,
            branchCondition: String
    ): List<String> {
        val assertions = mutableListOf<String>()

        // Basic assertion for non-void functions
        if (function.returnType != "Unit") {
            assertions.add(
                    "assertNotNull(result, \"Result should not be null for when branch '$branchCondition'\")"
            )
        }

        // Generate assertions based on expected behavior for different when branches
        when {
            // For common string operations in when expressions
            branchCondition == "\"add\"" || branchCondition == "\"sum\"" -> {
                if (function.returnType.contains("Int") || function.returnType.contains("Double")) {
                    assertions.add(
                            "assertTrue(result > 0, \"Result should be positive for addition operation\")"
                    )
                    assertions.add("// For exact value verification, calculate the expected result")
                    assertions.add("// Example: val expected = a + b")
                    assertions.add("// assertEquals(expected, result)")
                }
            }
            branchCondition == "\"subtract\"" -> {
                if (function.returnType.contains("Int") || function.returnType.contains("Double")) {
                    assertions.add("// For exact value verification, calculate the expected result")
                    assertions.add("// Example: val expected = a - b")
                    assertions.add("// assertEquals(expected, result)")
                }
            }
            branchCondition == "\"multiply\"" -> {
                if (function.returnType.contains("Int") || function.returnType.contains("Double")) {
                    assertions.add("// For exact value verification, calculate the expected result")
                    assertions.add("// Example: val expected = a * b")
                    assertions.add("// assertEquals(expected, result)")
                }
            }
            branchCondition == "\"divide\"" -> {
                if (function.returnType.contains("Int") || function.returnType.contains("Double")) {
                    assertions.add("// For exact value verification, calculate the expected result")
                    assertions.add("// Example: val expected = a / b")
                    assertions.add("// assertEquals(expected, result)")
                }
            }
            branchCondition == "\"max\"" -> {
                assertions.add("// For a max operation, verify result is the maximum value")
                assertions.add("// Example: val numbers = listOf(${subjectName})")
                assertions.add("// assertEquals(numbers.maxOrNull(), result)")
            }
            branchCondition == "\"min\"" -> {
                assertions.add("// For a min operation, verify result is the minimum value")
                assertions.add("// Example: val numbers = listOf(${subjectName})")
                assertions.add("// assertEquals(numbers.minOrNull(), result)")
            }
            branchCondition == "\"average\"" -> {
                assertions.add("// For an average operation, verify result is the average value")
                assertions.add("// Example: val numbers = listOf(${subjectName})")
                assertions.add("// assertEquals(numbers.average(), result)")
            }
            // For string return types
            function.returnType.contains("String") -> {
                assertions.add(
                        "// For string operations, verify the result contains expected content"
                )
                assertions.add(
                        "assertTrue(result.contains(\"$branchCondition\") || result.contains(\"${branchCondition.replace("\"", "")}\"), " +
                                "\"Result should reflect the when branch condition\")"
                )
            }
            // For boolean return types
            function.returnType.contains("Boolean") -> {
                assertions.add(
                        "// For boolean operations, verify the result matches expected behavior"
                )
                assertions.add("// assertTrue(result, \"Expected TRUE for this when branch\")")
                // Custom logic could be added based on specific branch conditions
            }
            // For enum or specific value matches
            branchCondition.matches(Regex("\\d+")) -> {
                assertions.add(
                        "// For numeric branch conditions, verify the result correctly handles this specific value"
                )
                assertions.add(
                        "assertEquals($branchCondition.toString(), result.toString(), " +
                                "\"Result should reflect handling of specific numeric value\")"
                )
            }
            // Default case for other types
            else -> {
                assertions.add("// Verify the result is appropriate for '$branchCondition' branch")
                if (function.returnType.contains("String")) {
                    assertions.add(
                            "assertTrue(result.isNotEmpty(), \"Result should not be empty for '$branchCondition' branch\")"
                    )
                } else if (function.returnType != "Unit") {
                    assertions.add(
                            "// Add specific assertions for this branch based on expected behavior"
                    )
                } else {
                    assertions.add(
                            "// For void methods, verify that any expected side effects occurred"
                    )
                    assertions.add("// This verifies the '$branchCondition' branch was taken")
                }
            }
        }

        return assertions
    }

    /** Generates setup code for a class test. */
    private fun generateSetupCode(classInfo: ClassInfo, functions: List<FunctionInfo>): String {
        val sb = StringBuilder()
        sb.appendLine("private lateinit var testInstance: ${classInfo.name.split('.').last()}")
        sb.appendLine()
        sb.appendLine("@BeforeEach")
        sb.appendLine("fun setUp() {")
        
        // Look for a constructor among the functions
        val constructor = functions.find { it.name == "<init>" || it.name == "init" }
        
        if (constructor != null && constructor.parameters.isNotEmpty()) {
            // Generate parameters for the constructor
            val constructorParams =
                    constructor.parameters.joinToString(", ") { param ->
                generateValueForType(param.type)
            }
            sb.appendLine(
                    "    testInstance = ${classInfo.name.split('.').last()}($constructorParams)"
            )
        } else {
            // No constructor or default constructor
            sb.appendLine("    testInstance = ${classInfo.name.split('.').last()}()")
        }
        
        sb.appendLine("}")
        return sb.toString()
    }
    
    /** Generates a random value for a given type. */
    private fun generateValueForType(type: String): String {
        return when {
            type.contains("Int") -> Random.nextInt(1, 100).toString()
            type.contains("Long") -> "${Random.nextLong(1, 100)}L"
            type.contains("Double") -> "${Random.nextDouble(1.0, 100.0)}"
            type.contains("Float") -> "${Random.nextFloat()}f"
            type.contains("Boolean") -> Random.nextBoolean().toString()
            type.contains("String") -> "\"test${Random.nextInt(100)}\""
            type.contains("List") || type.contains("Collection") -> {
                val innerType = type.substringAfter("<").substringBefore(">")
                "listOf(${generateValueForType(innerType)})"
            }
            type.contains("Map") -> {
                val parts = type.substringAfter("<").substringBefore(">").split(",")
                if (parts.size == 2) {
                    val keyType = parts[0].trim()
                    val valueType = parts[1].trim()
                    "mapOf(${generateValueForType(keyType)} to ${generateValueForType(valueType)})"
                } else {
                    "mapOf()"
                }
            }
            type.contains("Set") -> {
                val innerType = type.substringAfter("<").substringBefore(">")
                "setOf(${generateValueForType(innerType)})"
            }
            type.contains("Array") -> {
                val innerType = type.substringAfter("<").substringBefore(">")
                "arrayOf(${generateValueForType(innerType)})"
            }
            type.contains("Function") || type.contains("()->") -> "{ ${generateLambdaBody(type)} }"
            type.contains("?") -> "null" // Nullable type
            else -> "mock()" // Use Mockito for complex types
        }
    }
    
    /** Generates a lambda body for a function type. */
    private fun generateLambdaBody(type: String): String {
        // Extract return type from function type, e.g., "() -> Int" -> "Int"
        val returnType = type.substringAfter("->").trim()
        
        return when {
            returnType.contains("Int") -> "0"
            returnType.contains("Long") -> "0L"
            returnType.contains("Double") -> "0.0"
            returnType.contains("Float") -> "0.0f"
            returnType.contains("Boolean") -> "false"
            returnType.contains("String") -> "\"\""
            returnType.contains("Unit") -> ""
            else -> "null"
        }
    }
    
    /** Generates imports needed for the test file. */
    private fun generateImportsForTest(
        function: FunctionInfo,
        classInfo: ClassInfo? = null
    ): List<String> {
        val imports =
                mutableListOf("org.junit.jupiter.api.Test", "org.junit.jupiter.api.Assertions.*")
        
        if (classInfo != null) {
            imports.add("org.junit.jupiter.api.BeforeEach")

            // Only add the class import if it's not in the default package
            if (classInfo.name.contains(".")) {
            imports.add(classInfo.name)
            }
        }

        // If the function is from a different package, add its import
        if (function.containingClass != null && function.containingClass.contains(".")) {
            val packageName = function.containingClass.substringBeforeLast(".")
            val className = function.containingClass.substringAfterLast(".")

            // Only add if not already imported through classInfo
            if (classInfo == null || classInfo.name != function.containingClass) {
                imports.add("${packageName}.${className}")
            }
        }
        
        // Check if we need Mockito
        val needsMockito =
                function.parameters.any { param ->
            !param.type.contains("Int") && 
            !param.type.contains("Long") && 
            !param.type.contains("Double") && 
            !param.type.contains("Float") && 
            !param.type.contains("Boolean") && 
            !param.type.contains("String") && 
            !param.type.contains("List") && 
            !param.type.contains("Map") && 
            !param.type.contains("Set") && 
            !param.type.contains("Array") && 
            !param.type.contains("Function") && 
            !param.type.contains("()") && 
            !param.type.endsWith("?")
        }
        
        if (needsMockito) {
            imports.add("org.mockito.Mockito.mock")
        }
        
        return imports
    }
    
    /** Writes test cases to files. */
    fun writeTestFiles(testCases: List<TestCase>, testDir: File) {
        // Group test cases by file name
        val testCasesByFile = testCases.groupBy { it.fileName }
        
        for ((fileName, cases) in testCasesByFile) {
            // First try: Extract package from any test case with a fully qualified class name
            val targetPackage = cases.asSequence()
                .mapNotNull { it.targetClassName }
                .filter { it.contains(".") }
                .map { it.substringBeforeLast(".") }
                .firstOrNull()
                // Second try: Look for classes containing dots in any function name
                ?: cases.asSequence()
                    .mapNotNull { testCase -> 
                        if (testCase.targetFunction.contains(".")) {
                            testCase.targetFunction.substringBeforeLast(".")
                        } else null
                    }
                    .firstOrNull()
                // Third try: Check imports for potential package information
                ?: cases.asSequence()
                    .flatMap { it.imports.asSequence() }
                    .filter { it.contains(".") && !it.startsWith("org.junit") && !it.startsWith("org.mockito") }
                    .map { it.substringBeforeLast(".") }
                    .firstOrNull()
                // Default fallback
                ?: "com.demo" // Default package to avoid empty package
            
            // Create the package structure based on the target class's package
            val packageDir = if (targetPackage.isNotEmpty()) {
                val packagePath = targetPackage.replace('.', File.separatorChar)
                File(testDir, packagePath).also { it.mkdirs() }
            } else {
                testDir
            }
            
            val testFile = File(packageDir, "$fileName.kt")
            val writer = testFile.bufferedWriter()
            
            try {
                // Write package declaration matching the original class's package
                if (targetPackage.isNotEmpty()) {
                    writer.write("package $targetPackage\n\n")
                }
                
                // Write imports
                val allImports = cases.flatMap { it.imports }.distinct().sorted()

                // Filter out imports from the same package
                val filteredImports =
                        allImports.filter { import ->
                            !import.startsWith(targetPackage) ||
                                    import.substring(targetPackage.length).count { it == '.' } > 1
                        }

                // Add test framework imports
                val testImports =
                        listOf(
                                "org.junit.jupiter.api.Test",
                                "org.junit.jupiter.api.BeforeEach",
                                "org.junit.jupiter.api.Assertions.*",
                                "org.junit.jupiter.api.assertThrows"
                        )

                // Combine and sort all imports
                val imports = (filteredImports + testImports).distinct().sorted()
                for (import in imports) {
                    writer.write("import $import\n")
                }
                writer.write("\n")
                
                // Write the class declaration
                val className = fileName
                writer.write("class $className {\n\n")
                
                // Write setup code if any
                val setupCode = cases.firstOrNull()?.setupCode
                if (setupCode != null) {
                    writer.write(setupCode)
                    writer.write("\n")
                }
                
                // Write test methods
                for (testCase in cases) {
                    writer.write("    ${testCase.testCode}")
                    writer.write("\n")
                }
                
                // Close the class
                writer.write("}\n")
            } finally {
                writer.close()
            }
        }
    }
}

/** Converts a string to a valid method name. */
private fun String.toValidMethodName(): String {
    return this.replace(Regex("[^a-zA-Z0-9]"), " ")
        .split(" ")
        .filter { it.isNotEmpty() }
        .joinToString("") { it.capitalize() }
}

/** Capitalizes the first letter of a string. */
private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercaseChar() + substring(1)
    } else {
        this
    }
}

/** Represents a generated test case. */
data class TestCase(
    val name: String,
    val targetClassName: String?,
    val targetFunction: String,
    val fileName: String,
    val testCode: String,
    val setupCode: String? = null,
    val imports: List<String> = emptyList()
) 
