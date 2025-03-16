package com.stge

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class TestGeneratorTest {

    @TempDir
    lateinit var tempDir: Path
    
    @Test
    fun `test files are created in the same package as their implementation`() {
        // Create mock analysis result
        val analysisResult = AnalysisResult()
        
        // Add a class with a package
        val classInfo = ClassInfo(
            name = "com.example.TestClass",
            filePath = "/path/to/TestClass.kt"
        )
        analysisResult.classes.add(classInfo)
        
        // Add a function in that class
        val functionInfo = FunctionInfo(
            name = "testFunction",
            containingClass = "com.example.TestClass",
            filePath = "/path/to/TestClass.kt",
            returnType = "Int",
            parameters = listOf(
                ParameterInfo(
                    name = "param1",
                    type = "Int"
                )
            )
        )
        analysisResult.functions.add(functionInfo)
        
        // Add a conditional branch
        val branchInfo = ConditionalBranchInfo(
            type = "if",
            condition = "param1 > 0",
            functionName = "testFunction",
            filePath = "/path/to/TestClass.kt",
            lineNumber = 10
        )
        analysisResult.conditionalBranches.add(branchInfo)
        
        // Generate test cases
        val testGenerator = TestGenerator()
        val testCases = testGenerator.generateTestCases(analysisResult)
        
        // Write the test files
        val testDir = tempDir.toFile()
        testGenerator.writeTestFiles(testCases, testDir)
        
        // Verify test file was created in the correct package structure
        val expectedPackagePath = File(testDir, "com/example")
        assertTrue(expectedPackagePath.exists(), "Package directory was not created correctly")
        
        // Verify test file exists
        val testFile = File(expectedPackagePath, "TestClassTest.kt")
        assertTrue(testFile.exists(), "Test file was not created in the correct package")
        
        // Read the file content to verify package declaration
        val content = testFile.readText()
        assertTrue(content.contains("package com.example"), "Package declaration is incorrect")
        
        // Verify imports don't include unnecessary imports from the same package
        val packageLine = content.indexOf("package com.example")
        val importsSection = content.substring(packageLine + "package com.example".length)
        
        // The class being tested should not be imported if it's in the same package
        assertFalse(importsSection.contains("import com.example.TestClass"), 
                    "Class from same package should not be imported")
    }
    
    @Test
    fun `test files for classes without packages are created in root test directory`() {
        // Create mock analysis result
        val analysisResult = AnalysisResult()
        
        // Add a class without a package
        val classInfo = ClassInfo(
            name = "TestClass",  // No package
            filePath = "/path/to/TestClass.kt"
        )
        analysisResult.classes.add(classInfo)
        
        // Add a function in that class
        val functionInfo = FunctionInfo(
            name = "testFunction",
            containingClass = "TestClass",  // No package
            filePath = "/path/to/TestClass.kt",
            returnType = "Int",
            parameters = listOf(
                ParameterInfo(
                    name = "param1",
                    type = "Int"
                )
            )
        )
        analysisResult.functions.add(functionInfo)
        
        // Generate test cases
        val testGenerator = TestGenerator()
        val testCases = testGenerator.generateTestCases(analysisResult)
        
        // Write the test files
        val testDir = tempDir.toFile()
        testGenerator.writeTestFiles(testCases, testDir)
        
        // Verify test file was created directly in the test directory (not in a package)
        val testFile = File(testDir, "TestClassTest.kt")
        assertTrue(testFile.exists(), "Test file was not created in the root directory")
        
        // Read the file content to verify no package declaration
        val content = testFile.readText()
        assertFalse(content.contains("package"), "There should be no package declaration")
    }
} 