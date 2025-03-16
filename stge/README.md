# StGe - Static Kotlin Test Generator

StGe is a powerful static analysis tool for Kotlin projects that automatically generates comprehensive test cases to achieve 100% code coverage.

## Features

- **Effective Static Analysis**: Analyzes Kotlin source code using advanced pattern matching to identify code structures
- **Complete Branch Coverage**: Generates specific test cases for both true and false paths of every conditional statement
- **Comprehensive Test Generation**: Automatically generates JUnit 5 test cases for all code paths
- **100% Coverage Target**: Creates test suites designed to hit every branch and condition in your code
- **Smart Branch Verification**: Generates intelligent assertions tailored to each branch's specific behavior
- **Package Consistency**: Ensures tests are generated in the same package as their implementation classes
- **Varargs Support with Lookahead**: Intelligently handles functions with vararg parameters by analyzing actual usage
- **Test Organization**: Organizes tests into appropriate packages and files

## Usage

```bash
# Build the project
./gradlew build

# Run StGe on a Kotlin project
java -jar build/libs/stge-1.0-SNAPSHOT.jar /path/to/kotlin/project
```

## Requirements

- JDK 8 or higher
- Kotlin 1.9.22 or higher

## How It Works

1. StGe scans the target project for Kotlin source files
2. It performs static analysis to extract information about classes, functions, and their structure
3. It identifies all classes, methods, and conditional branches
4. It analyzes function calls to understand how vararg functions are typically used
5. **It generates test cases for every identified branch (both true and false conditions)**
6. It creates specific assertions that verify each branch behaves correctly
7. It writes JUnit 5 test files to the appropriate locations, preserving package structure

## Package Consistency

StGe maintains package consistency between implementation and test classes:

1. **Same Package Structure**: Test classes are always generated in the same package as the class being tested
2. **Proper Imports**: Import statements are optimized to avoid unnecessary imports from the same package 
3. **Package-Aware Test Structure**: Test directories mirror the source directory structure
4. **Default Package Support**: Classes in the default package get tests in the default package

For example, if you have a class:

```kotlin
package com.example.util

class StringUtils {
    // ...
}
```

StGe will generate a test class at:

```kotlin
package com.example.util

class StringUtilsTest {
    // ...
}
```

## Branch Coverage Strategy

StGe's branch coverage strategy ensures that every conditional branch in your code is exercised by test cases:

1. **If Statements**: Two test cases are generated for each `if` statement:
   - One that makes the condition evaluate to `true`
   - One that makes the condition evaluate to `false`

2. **When Expressions**: A test case is generated for each branch in a `when` expression:
   - Each test provides input to trigger a specific branch
   - The condition value is extracted and used to create appropriate test parameters

3. **Parameter Manipulation**: Test parameters are intelligently set to trigger specific branches:
   - For numeric comparisons (`>`, `<`, `==`), appropriate values are selected
   - For string checks, matching or non-matching strings are provided
   - For boolean conditions, true/false values are set appropriately

4. **Smart Assertions**: Generated tests include branch-specific assertions that verify:
   - The correct branch was executed
   - The return value is appropriate for that branch
   - Any expected side effects occurred

## Intelligent Branch-Specific Assertions

One of StGe's key features is its ability to generate intelligent assertions tailored to each branch:

### For If Conditions

```kotlin
// For numeric conditions like "if (value > 10)"
// TRUE branch test:
assertTrue(result > 0, "Expected positive result for condition 'value > 10'")

// FALSE branch test:
assertTrue(result <= 0, "Expected non-positive result for condition 'value > 10'")
```

### For When Expressions

```kotlin
// For when branches like "when (operation) { "add" -> ... }"
// Testing the "add" branch:
assertTrue(result > 0, "Result should be positive for addition operation")
// For exact value verification:
val expected = a + b
assertEquals(expected, result)
```

### For String Operations

```kotlin
// For string return types and conditions like "isEmpty()"
assertTrue(result.isNotEmpty(), "Expected non-empty result for condition 'string.isEmpty()'")
assertTrue(result.contains("specific text"), "Result should contain expected text for TRUE branch")
```

### For Equality Checks

```kotlin
// For conditions comparing parameters like "if (x == y)"
assertEquals(paramName, result, "Result should match parameter for condition 'x == y'")
```

## Technical Details

StGe uses a robust pattern-matching approach to analyze Kotlin code, providing:

- **Accurate detection** of Kotlin language constructs
- **Complete identification** of classes, functions, and parameters
- **Precise capturing** of conditional branches and control flow
- **Smart handling** of nested expressions and complex syntax

The implementation ensures that StGe can correctly handle common Kotlin language features including:

- Varargs parameters
- Default parameter values
- Class inheritance
- Function overloading
- Simple conditional expressions

## Configuration

StGe requires no configuration and works out of the box with any Kotlin project structure.

## Varargs with Lookahead

StGe includes special handling for functions with variable arguments (varargs) in Kotlin. When generating test cases for functions that use varargs, StGe:

1. Identifies all parameters marked with the `vararg` keyword
2. Analyzes the codebase to find actual calls to these functions
3. Extracts examples of how these vararg parameters are typically used
4. Generates test cases that use similar argument patterns

This "lookahead" approach ensures that test cases for vararg functions reflect how the functions are actually used in the real code, rather than using arbitrary values.

For example, if your code contains a function like:

```kotlin
fun printAll(prefix: String, vararg items: String) {
    for (item in items) {
        println("$prefix$item")
    }
}
```

And it's called in your code like:

```kotlin
printAll("Item: ", "book", "pen", "laptop")
```

StGe will generate a test case that uses the same pattern of arguments:

```kotlin
@Test
fun testPrintAll() {
    printAll("Item: ", "book", "pen", "laptop")
    // Assertions...
}
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.