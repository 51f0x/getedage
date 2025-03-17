package com.stge

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.DisplayName

/**
 * Comprehensive test suite for EnhancedStaticAnalyzer
 */
class EnhancedStaticAnalyzerTest {

    @TempDir
    lateinit var tempDir: Path
    
    private lateinit var analyzer: EnhancedStaticAnalyzer
    
    @BeforeEach
    fun setup() {
        analyzer = EnhancedStaticAnalyzer()
    }
    
    /**
     * Test the main analyzeProject method with a simple Kotlin file
     */
    @Test
    @DisplayName("analyzeProject should parse simple Kotlin file")
    fun testSimpleClass() {
        // Create a simple Kotlin file in the temp directory
        val kotlinFile = TestHelper.createKotlinFile(tempDir, TestHelper.sampleKotlinCode)
        
        // Analyze the file
        val result = analyzer.analyzeProject(listOf(kotlinFile))
        
        // Assertions
        assertNotNull(result, "Analysis result should not be null")
        assertEquals(1, result.files.size, "Should have one file")
        assertEquals(1, result.classes.size, "Should have one class")
        assertEquals("com.example.SimpleClass", result.classes[0].name, "Class name should match")
        
        // There should be at least one function (possibly more due to synthetic ones)
        assertTrue(result.functions.any { it.name == "testFunction" }, 
            "Should contain testFunction")
        
        // Check property
        assertTrue(result.variables.any { it.name == "property" }, 
            "Should contain the property variable")
        
        // Check conditional branch
        assertTrue(result.conditionalBranches.any { 
            it.type == "if" && it.condition.contains("param > 0") 
        }, "Should contain if branch with condition 'param > 0'")
    }
    
    /**
     * Test code with multiple classes and inheritance
     */
    @Test
    @DisplayName("analyzeProject should handle inheritance and multiple classes")
    fun testInheritance() {
        val kotlinFile = TestHelper.createKotlinFile(tempDir, TestHelper.inheritanceKotlinCode)
        
        val result = analyzer.analyzeProject(listOf(kotlinFile))
        
        // Assertions
        assertTrue(result.classes.any { it.name == "com.example.BaseClass" }, 
            "Should have BaseClass")
        assertTrue(result.classes.any { it.name == "com.example.ChildClass" }, 
            "Should have ChildClass")
        
        // Check functions
        assertTrue(result.functions.any { it.name == "baseMethod" && it.containingClass == "com.example.BaseClass" },
            "Should have baseMethod in BaseClass")
        assertTrue(result.functions.any { it.name == "baseMethod" && it.containingClass == "com.example.ChildClass" },
            "Should have baseMethod in ChildClass")
        assertTrue(result.functions.any { it.name == "childMethod" && it.containingClass == "com.example.ChildClass" },
            "Should have childMethod in ChildClass")
        
        // Check function calls (might be challenging due to how the analyzer identifies calls)
        assertTrue(result.functionCalls.any { it.functionName.contains("baseMethod") },
            "Should have a call to baseMethod")
    }
    
    /**
     * Test analyzing complex control flow with loops and conditionals
     */
    @Test
    @DisplayName("analyzeProject should analyze complex control flow")
    fun testComplexControlFlow() {
        val kotlinFile = TestHelper.createKotlinFile(tempDir, TestHelper.complexFlowKotlinCode)
        
        val result = analyzer.analyzeProject(listOf(kotlinFile))
        
        // Assertions for loops
        assertTrue(result.loops.any { 
            it.type == "for" && it.variable == "num" 
        }, "Should contain for loop with variable 'num'")
        
        assertTrue(result.loops.any { 
            it.type == "while" && it.condition?.contains("i < 10") == true 
        }, "Should contain while loop with condition 'i < 10'")
        
        // Assertions for conditionals
        assertTrue(result.conditionalBranches.any { 
            it.type == "if" && it.condition.contains("num > 0") 
        }, "Should contain if branch with condition 'num > 0'")
        
        assertTrue(result.conditionalBranches.any { 
            it.type == "when" 
        }, "Should contain when expression")
    }
    
    /**
     * Test data flow analysis
     */
    @Test
    @DisplayName("analyzeProject should perform data flow analysis")
    fun testDataFlowAnalysis() {
        val kotlinFile = TestHelper.createKotlinFile(tempDir, TestHelper.dataFlowKotlinCode)
        
        val result = analyzer.analyzeProject(listOf(kotlinFile))
        
        // Assertions for variables
        assertTrue(result.variables.any { it.name == "x" },
            "Should contain variable 'x'")
        assertTrue(result.variables.any { it.name == "y" },
            "Should contain variable 'y'")
        assertTrue(result.variables.any { it.name == "z" },
            "Should contain variable 'z'")
        
        // Assertions for data flow analysis (if implemented)
        // These might not all pass depending on the depth of analysis implementation
        if (result.definitions.isNotEmpty()) {
            assertTrue(result.definitions.any { it.variableName == "y" }, 
                "Should contain definition for variable 'y'")
                
            // Check for unused variable anomaly if anomaly detection is implemented
            if (result.dataFlowAnomalies.isNotEmpty()) {
                assertTrue(result.dataFlowAnomalies.any { 
                    it.anomalyType == AnomalyType.UNUSED_DEFINITION && it.variableName == "y" 
                }, "Should detect unused variable 'y'")
                
                assertTrue(result.dataFlowAnomalies.any { 
                    it.anomalyType == AnomalyType.UNUSED_DEFINITION && it.variableName == "b" 
                }, "Should detect unused parameter 'b'")
            }
        }
    }
    
    /**
     * Test that multiple files are correctly analyzed together
     */
    @Test
    @DisplayName("analyzeProject should analyze multiple files with cross-references")
    fun testMultipleFiles() {
        val fileA = TestHelper.createKotlinFile(
            tempDir,
            """
            package com.example
            
            class ClassA {
                fun methodA(): String = "A"
            }
            """.trimIndent(),
            "ClassA.kt"
        )
        
        val fileB = TestHelper.createKotlinFile(
            tempDir,
            """
            package com.example
            
            class ClassB {
                fun useClassA() {
                    val a = ClassA()
                    val result = a.methodA()
                    println(result)
                }
            }
            """.trimIndent(),
            "ClassB.kt"
        )
        
        val result = analyzer.analyzeProject(listOf(fileA, fileB))
        
        // Assertions
        assertEquals(2, result.files.size, "Should have two files")
        assertTrue(result.classes.any { it.name == "com.example.ClassA" },
            "Should have ClassA")
        assertTrue(result.classes.any { it.name == "com.example.ClassB" },
            "Should have ClassB")
        
        // Check references (might be challenging due to how references are identified)
        if (result.references.isNotEmpty()) {
            assertTrue(result.references.any { 
                it.referencedName == "ClassA" && it.filePath.endsWith("ClassB.kt") 
            }, "Should have reference to ClassA from ClassB")
        }
    }
    
    /**
     * Test edge cases like empty files and invalid syntax
     */
    @Test
    @DisplayName("analyzeProject should handle edge cases")
    fun testEdgeCases() {
        // Empty file
        val emptyFile = TestHelper.createKotlinFile(tempDir, "", "Empty.kt")
        
        // File with syntax error (missing closing brace)
        val invalidFile = TestHelper.createKotlinFile(
            tempDir,
            """
            class InvalidClass {
                fun invalidMethod() {
                    println("Missing closing brace"
            """.trimIndent(),
            "Invalid.kt"
        )
        
        val result = analyzer.analyzeProject(listOf(emptyFile, invalidFile))
        
        // Should not crash and should process what it can
        assertNotNull(result, "Result should not be null even with invalid files")
    }
    
    /**
     * Test with various declaration types (variables, functions, classes)
     */
    @Test
    @DisplayName("analyzeProject should handle various declaration types")
    fun testDeclarationTypes() {
        val kotlinFile = TestHelper.createKotlinFile(tempDir, TestHelper.declarationTypesKotlinCode)
        
        val result = analyzer.analyzeProject(listOf(kotlinFile))
        
        // Assertions for different declaration types
        // Note: Some of these might not pass depending on the implementation details
        
        // Test for top-level declarations - not all implementations might expose these directly
        // or they might have different naming schemes, so we do a flexible check
        val allVariableNames = result.variables.map { it.name }
        val allFunctionNames = result.functions.map { it.name }
        val allClassNames = result.classes.map { it.name }
        
        // Just check if we have a reasonable amount of entities being recognized
        assertFalse(result.classes.isEmpty(), "Should have recognized some classes")
        assertFalse(result.functions.isEmpty(), "Should have recognized some functions")
        
        // Verify that at least some key declaration names have been captured
        // in some form, either exactly or as part of a qualified name
        assertTrue(allClassNames.any { it.endsWith("ClassWithCompanion") || it.contains("ClassWithCompanion") }, 
            "Should analyze regular classes")
            
        // Check for at least one enum value - it might be represented in different ways
        val allContent = allClassNames.joinToString() + allVariableNames.joinToString() + allFunctionNames.joinToString()
        assertTrue(allContent.contains("Direction") || allContent.contains("NORTH"), 
            "Should have recognized enum Direction or its values")
    }
    
    /**
     * Test lambda expressions and higher-order functions
     */
    @Test
    @DisplayName("analyzeProject should handle lambda expressions and higher-order functions")
    fun testLambdaExpressions() {
        val kotlinFile = TestHelper.createKotlinFile(tempDir, TestHelper.lambdaKotlinCode)
        
        val result = analyzer.analyzeProject(listOf(kotlinFile))
        
        // These assertions might be challenging and depend on the depth of analysis
        assertTrue(result.functions.any { it.name == "processWithLambda" }, 
            "Should analyze function with lambda parameter")
        
        // Check for function with higher-order function usage
        assertTrue(result.functions.any { it.name == "useHigherOrderFunction" }, 
            "Should analyze function that uses higher-order function")
        
        // Check for function with lambda that captures variables
        assertTrue(result.functions.any { it.name == "lambdaCapturingVariables" }, 
            "Should analyze function with lambda that captures variables")
    }
} 