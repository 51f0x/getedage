package com.stge

import java.io.File
import java.nio.file.Path
import org.jetbrains.kotlin.psi.KtFile
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import com.intellij.openapi.Disposable

/**
 * Helper class with utility methods for testing EnhancedStaticAnalyzer
 */
class TestHelper {
    companion object {
        /**
         * Creates a Kotlin file in the temporary directory for testing
         */
        fun createKotlinFile(tempDir: Path, content: String, fileName: String = "Test.kt"): File {
            val file = File(tempDir.toFile(), fileName)
            file.writeText(content)
            return file
        }
        
        /**
         * Creates a KotlinCoreEnvironment for testing
         */
        fun createKotlinEnvironment(): Pair<KotlinCoreEnvironment, Disposable> {
            val disposable = Disposer.newDisposable()
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
            
            return Pair(environment, disposable)
        }
        
        /**
         * Creates a KtFile from a string content
         */
        fun createKtFile(environment: KotlinCoreEnvironment, content: String, fileName: String = "Test.kt"): KtFile {
            val psiFileFactory = PsiFileFactory.getInstance(environment.project)
            val psiFile = psiFileFactory.createFileFromText(
                fileName,
                KotlinLanguage.INSTANCE,
                content
            )
            return psiFile as KtFile
        }
        
        /**
         * Sample Kotlin code with a simple class and function for testing
         */
        val sampleKotlinCode = """
            package com.example
            
            class SimpleClass {
                private val property: String = "test"
                
                fun testFunction(param: Int): Boolean {
                    if (param > 0) {
                        return true
                    }
                    return false
                }
            }
        """.trimIndent()
        
        /**
         * Sample Kotlin code with inheritance and multiple classes
         */
        val inheritanceKotlinCode = """
            package com.example
            
            open class BaseClass {
                open fun baseMethod(): String = "base"
            }
            
            class ChildClass : BaseClass() {
                override fun baseMethod(): String = "child"
                
                fun childMethod() {
                    val result = baseMethod()
                    println(result)
                }
            }
        """.trimIndent()
        
        /**
         * Sample Kotlin code with complex control flow
         */
        val complexFlowKotlinCode = """
            package com.example
            
            class ComplexFlowClass {
                fun processNumbers(numbers: List<Int>): Int {
                    var sum = 0
                    
                    for (num in numbers) {
                        if (num > 0) {
                            sum += num
                        } else if (num < 0) {
                            sum -= num
                        } else {
                            continue
                        }
                        
                        when {
                            sum > 100 -> return sum
                            sum < -100 -> return -sum
                        }
                    }
                    
                    var i = 0
                    while (i < 10) {
                        sum += i
                        i++
                    }
                    
                    return sum
                }
            }
        """.trimIndent()
        
        /**
         * Sample Kotlin code for data flow analysis
         */
        val dataFlowKotlinCode = """
            package com.example
            
            class DataFlowClass {
                fun calculateValue(input: Int): Int {
                    var x = input
                    var y = 0  // Unused variable
                    
                    if (x > 10) {
                        x = x * 2
                    }
                    
                    val z = x + 5
                    return z
                }
                
                fun unusedParameter(a: Int, b: Int): Int {
                    return a * 2  // b is unused
                }
            }
        """.trimIndent()
        
        /**
         * Sample Kotlin code with various declaration types
         */
        val declarationTypesKotlinCode = """
            package com.example
            
            // Top-level variable and function
            val topLevelVar = "Top level"
            
            fun topLevelFunction() = "Top level function"
            
            // Class with companion object
            class ClassWithCompanion {
                val instanceVar = "Instance variable"
                
                fun instanceMethod() = "Instance method"
                
                companion object {
                    const val COMPANION_VAR = "Companion variable"
                    
                    fun companionMethod() = "Companion method"
                }
            }
            
            // Object declaration
            object SingletonObject {
                val objectVar = "Object variable"
                
                fun objectMethod() = "Object method"
            }
            
            // Data class
            data class DataClass(
                val prop1: String,
                val prop2: Int
            )
            
            // Interface
            interface MyInterface {
                fun interfaceMethod()
            }
            
            // Enum class
            enum class Direction {
                NORTH, SOUTH, EAST, WEST
            }
        """.trimIndent()
        
        /**
         * Sample Kotlin code with lambda expressions
         */
        val lambdaKotlinCode = """
            package com.example
            
            class LambdaClass {
                fun processWithLambda(items: List<String>, processor: (String) -> String): List<String> {
                    return items.map { item -> processor(item) }
                }
                
                fun useHigherOrderFunction() {
                    val items = listOf("a", "b", "c")
                    val result = processWithLambda(items) { it.uppercase() }
                    println(result)
                }
                
                fun lambdaCapturingVariables() {
                    val prefix = "Item: "
                    val processor: (Int) -> String = { number -> 
                        val suffix = " (#" + number + ")"
                        prefix + "Value" + suffix
                    }
                    
                    val result = processor(1)
                    println(result)
                }
            }
        """.trimIndent()
    }
} 