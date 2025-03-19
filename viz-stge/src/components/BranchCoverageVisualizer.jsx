import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';

const BranchCoverageVisualizer = () => {
  const [showTests, setShowTests] = useState(false);
  
  const codeExample = `
package com.example

class Calculator {
    fun calculate(a: Int, b: Int, operation: String): Int {
        return when (operation) {
            "add" -> a + b
            "subtract" -> a - b
            "multiply" -> a * b
            "divide" -> {
                if (b == 0) {
                    throw IllegalArgumentException("Cannot divide by zero")
                }
                a / b
            }
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }
    }
}
  `;
  
  const testExample = `
package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class CalculatorTest {
    private lateinit var testInstance: Calculator
    
    @BeforeEach
    fun setUp() {
        testInstance = Calculator()
    }
    
    // "When" branch test for "add" operation
    @Test
    fun calculateWhenAdd() {
        val a = 10
        val b = 5
        val operation = "add"
        
        val result = testInstance.calculate(a, b, operation)
        assertEquals(a + b, result)
    }
    
    // "When" branch test for "subtract" operation
    @Test
    fun calculateWhenSubtract() {
        val a = 10
        val b = 5
        val operation = "subtract"
        
        val result = testInstance.calculate(a, b, operation)
        assertEquals(a - b, result)
    }
    
    // "When" branch test for "multiply" operation
    @Test
    fun calculateWhenMultiply() {
        val a = 10
        val b = 5
        val operation = "multiply"
        
        val result = testInstance.calculate(a, b, operation)
        assertEquals(a * b, result)
    }
    
    // "When" branch test for "divide" operation - non-zero denominator
    @Test
    fun calculateWhenDivideAndBNonZero() {
        val a = 10
        val b = 5
        val operation = "divide"
        
        val result = testInstance.calculate(a, b, operation)
        assertEquals(a / b, result)
    }
    
    // "When" branch test for "divide" operation - zero denominator
    @Test
    fun calculateWhenDivideAndBZero() {
        val a = 10
        val b = 0
        val operation = "divide"
        
        assertThrows<IllegalArgumentException> {
            testInstance.calculate(a, b, operation)
        }
    }
    
    // "When" branch test for "else" (unknown operation)
    @Test
    fun calculateWhenUnknownOperation() {
        val a = 10
        val b = 5
        val operation = "unknown"
        
        assertThrows<IllegalArgumentException> {
            testInstance.calculate(a, b, operation)
        }
    }
}
  `;
  
  const branchesIdentified = [
    { id: 1, description: 'When branch: operation == "add"' },
    { id: 2, description: 'When branch: operation == "subtract"' },
    { id: 3, description: 'When branch: operation == "multiply"' },
    { id: 4, description: 'When branch: operation == "divide"' },
    { id: 5, description: 'If branch: b == 0 (inside divide)' },
    { id: 6, description: 'When branch: else (unknown operation)' },
  ];
  
  return (
    <div className="w-full max-w-6xl mx-auto">
      <h2 className="text-2xl font-bold text-center mb-8">Branch Coverage Visualization</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
        <Card>
          <CardHeader>
            <CardTitle>Original Code</CardTitle>
            <CardDescription>Source code to be analyzed</CardDescription>
          </CardHeader>
          <CardContent>
            <pre className="bg-secondary p-4 rounded-md overflow-x-auto">
              <code>{codeExample}</code>
            </pre>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader>
            <CardTitle>Branches Identified</CardTitle>
            <CardDescription>StGe identifies these conditional branches</CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2 mt-2">
              {branchesIdentified.map(branch => (
                <li key={branch.id} className="bg-secondary p-3 rounded-md">
                  <div className="flex items-center">
                    <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-2">
                      {branch.id}
                    </span>
                    <span>{branch.description}</span>
                  </div>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>
      
      <div className="flex justify-center mb-8">
        <Button 
          onClick={() => setShowTests(!showTests)}
          className="px-8"
        >
          {showTests ? 'Hide Generated Tests' : 'Show Generated Tests'}
        </Button>
      </div>
      
      {showTests && (
        <Card>
          <CardHeader>
            <CardTitle>Generated Tests</CardTitle>
            <CardDescription>Tests that ensure 100% branch coverage</CardDescription>
          </CardHeader>
          <CardContent>
            <pre className="bg-secondary p-4 rounded-md overflow-x-auto">
              <code>{testExample}</code>
            </pre>
          </CardContent>
        </Card>
      )}
      
      <Card className="mt-8">
        <CardHeader>
          <CardTitle>Branch Coverage Strategy</CardTitle>
          <CardDescription>How StGe ensures complete branch coverage</CardDescription>
        </CardHeader>
        <CardContent>
          <ul className="space-y-4">
            <li className="flex">
              <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-2">1</span>
              <div>
                <h4 className="font-medium">Identify all branches</h4>
                <p className="text-sm text-muted-foreground">StGe identifies all if statements, when expressions, and loops in the code.</p>
              </div>
            </li>
            <li className="flex">
              <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-2">2</span>
              <div>
                <h4 className="font-medium">Generate test cases for each branch</h4>
                <p className="text-sm text-muted-foreground">For each branch, StGe creates test cases that exercise both the true and false conditions.</p>
              </div>
            </li>
            <li className="flex">
              <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-2">3</span>
              <div>
                <h4 className="font-medium">Create appropriate assertions</h4>
                <p className="text-sm text-muted-foreground">StGe generates assertions that verify each branch behaves correctly.</p>
              </div>
            </li>
            <li className="flex">
              <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-2">4</span>
              <div>
                <h4 className="font-medium">Handle nested conditions</h4>
                <p className="text-sm text-muted-foreground">For nested conditions (like the b == 0 check inside the "divide" branch), StGe creates specific tests for each nested branch.</p>
              </div>
            </li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
};

export default BranchCoverageVisualizer; 