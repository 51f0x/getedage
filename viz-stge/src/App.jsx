import React, { useState } from 'react';
import { Button } from './components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './components/ui/card';
import { FileCode, GitBranch, TestTube2 } from 'lucide-react';
import ArchitectureDiagram from './components/ArchitectureDiagram';
import BranchCoverageVisualizer from './components/BranchCoverageVisualizer';

const App = () => {
  const [activeTab, setActiveTab] = useState('overview');
  
  return (
    <div className="min-h-screen bg-background">
      <header className="border-b">
        <div className="container mx-auto py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <FileCode className="h-8 w-8 text-primary" />
              <h1 className="text-3xl font-bold">StGe Explorer</h1>
            </div>
            <nav className="flex gap-2">
              <Button 
                variant={activeTab === 'overview' ? 'default' : 'ghost'} 
                onClick={() => setActiveTab('overview')}
              >
                Overview
              </Button>
              <Button 
                variant={activeTab === 'architecture' ? 'default' : 'ghost'} 
                onClick={() => setActiveTab('architecture')}
              >
                Architecture
              </Button>
              <Button 
                variant={activeTab === 'coverage' ? 'default' : 'ghost'} 
                onClick={() => setActiveTab('coverage')}
              >
                Branch Coverage
              </Button>
            </nav>
          </div>
        </div>
      </header>
      
      <main className="container mx-auto py-8">
        {activeTab === 'overview' && (
          <div className="max-w-5xl mx-auto">
            <h2 className="text-3xl font-bold text-center mb-8">StGe - Static Kotlin Test Generator</h2>
            <p className="text-xl text-center mb-10 text-muted-foreground">
              A powerful static analysis tool for Kotlin that automatically generates comprehensive test cases to achieve 100% code coverage
            </p>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-12">
              <Card>
                <CardHeader>
                  <div className="flex justify-center mb-2">
                    <FileCode className="h-12 w-12 text-primary" />
                  </div>
                  <CardTitle className="text-center">Static Analysis</CardTitle>
                  <CardDescription className="text-center">Advanced pattern matching of Kotlin code</CardDescription>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Analyzes Kotlin source code with PSI</span>
                    </li>
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Identifies classes, methods, and parameters</span>
                    </li>
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Extracts control flow and data dependencies</span>
                    </li>
                  </ul>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader>
                  <div className="flex justify-center mb-2">
                    <GitBranch className="h-12 w-12 text-primary" />
                  </div>
                  <CardTitle className="text-center">Branch Coverage</CardTitle>
                  <CardDescription className="text-center">Tests for every condition in your code</CardDescription>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Tests for true/false paths of every condition</span>
                    </li>
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Handles if statements, when expressions, and loops</span>
                    </li>
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Achieves 100% branch coverage target</span>
                    </li>
                  </ul>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader>
                  <div className="flex justify-center mb-2">
                    <TestTube2 className="h-12 w-12 text-primary" />
                  </div>
                  <CardTitle className="text-center">Test Generation</CardTitle>
                  <CardDescription className="text-center">Smart, comprehensive tests</CardDescription>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Generates JUnit 5 test cases</span>
                    </li>
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Intelligent branch-specific assertions</span>
                    </li>
                    <li className="flex items-start">
                      <span className="h-5 w-5 rounded-full bg-primary text-white flex items-center justify-center text-xs font-medium mr-2 mt-0.5">✓</span>
                      <span>Maintains package structure with your code</span>
                    </li>
                  </ul>
                </CardContent>
              </Card>
            </div>
            
            <div className="mt-16">
              <Card>
                <CardHeader>
                  <CardTitle>How It Works</CardTitle>
                  <CardDescription>The process from code analysis to test generation</CardDescription>
                </CardHeader>
                <CardContent>
                  <ol className="space-y-4">
                    <li className="flex">
                      <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-3">1</span>
                      <div>
                        <h4 className="font-medium">Scan Project</h4>
                        <p className="text-muted-foreground">StGe scans your project directory to find all Kotlin source files, excluding test files.</p>
                      </div>
                    </li>
                    <li className="flex">
                      <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-3">2</span>
                      <div>
                        <h4 className="font-medium">Analyze Code Structure</h4>
                        <p className="text-muted-foreground">It uses the Kotlin PSI to extract classes, functions, parameters, and more from your code.</p>
                      </div>
                    </li>
                    <li className="flex">
                      <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-3">3</span>
                      <div>
                        <h4 className="font-medium">Identify Branches</h4>
                        <p className="text-muted-foreground">All conditional branches are identified, including if statements, when expressions, and loops.</p>
                      </div>
                    </li>
                    <li className="flex">
                      <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-3">4</span>
                      <div>
                        <h4 className="font-medium">Analyze Vararg Usage</h4>
                        <p className="text-muted-foreground">For functions with varargs, StGe looks at how they're typically called to create realistic tests.</p>
                      </div>
                    </li>
                    <li className="flex">
                      <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-3">5</span>
                      <div>
                        <h4 className="font-medium">Generate Test Cases</h4>
                        <p className="text-muted-foreground">Tests are generated for every branch, with specific test cases for both true and false conditions.</p>
                      </div>
                    </li>
                    <li className="flex">
                      <span className="flex-shrink-0 h-6 w-6 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium mr-3">6</span>
                      <div>
                        <h4 className="font-medium">Write Tests to Files</h4>
                        <p className="text-muted-foreground">Test cases are written to the appropriate package in your test directory, ready to run.</p>
                      </div>
                    </li>
                  </ol>
                </CardContent>
              </Card>
            </div>
          </div>
        )}
        
        {activeTab === 'architecture' && <ArchitectureDiagram />}
        
        {activeTab === 'coverage' && <BranchCoverageVisualizer />}
      </main>
      
      <footer className="border-t mt-12">
        <div className="container mx-auto py-6">
          <p className="text-center text-muted-foreground">
            StGe - Static Kotlin Test Generator | Automatic branch coverage for Kotlin projects
          </p>
        </div>
      </footer>
    </div>
  );
};

export default App; 