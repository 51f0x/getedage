import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { 
  FileSearch, 
  Code2, 
  FileCode, 
  GitBranch, 
  TestTube2, 
  ArrowRight, 
  ListTree, 
  FileText 
} from 'lucide-react';

const ArchitectureDiagram = () => {
  const [activeStep, setActiveStep] = useState(0);
  
  const steps = [
    {
      id: 'project-scan',
      title: 'Project Scanning',
      description: 'StGe scans the target project for Kotlin source files',
      icon: <FileSearch className="h-10 w-10 text-primary" />,
      details: 'The EnhancedStaticAnalyzer walks through the project directory, collecting all .kt files that are not in test directories. It constructs a map of all source files for analysis.'
    },
    {
      id: 'static-analysis',
      title: 'Static Analysis',
      description: 'StGe performs static analysis to extract code structure',
      icon: <Code2 className="h-10 w-10 text-primary" />,
      details: 'Using the Kotlin PSI (Program Structure Interface), the analyzer identifies classes, functions, parameters, and conditional branches. Multiple pass analysis provides deep semantic understanding of the code.'
    },
    {
      id: 'branch-analysis',
      title: 'Branch Analysis',
      description: 'StGe identifies all conditional branches in the code',
      icon: <GitBranch className="h-10 w-10 text-primary" />,
      details: 'For each if statement, when expression, and loop, it analyzes the conditions, extracts parameter dependencies, and determines the logic paths that need to be tested.'
    },
    {
      id: 'vararg-analysis',
      title: 'Vararg Lookahead',
      description: 'StGe analyzes how vararg parameters are used in actual calls',
      icon: <ListTree className="h-10 w-10 text-primary" />,
      details: 'For functions that use varargs, StGe scans the entire codebase for examples of how these functions are called, to generate realistic test cases with appropriate arguments.'
    },
    {
      id: 'test-generation',
      title: 'Test Generation',
      description: 'StGe generates comprehensive test cases for all branches',
      icon: <TestTube2 className="h-10 w-10 text-primary" />,
      details: 'The TestGenerator creates JUnit 5 test cases that exercise both true and false conditions for every branch, ensuring 100% branch coverage.'
    },
    {
      id: 'file-output',
      title: 'Output Tests',
      description: 'StGe writes tests to the appropriate package structure',
      icon: <FileText className="h-10 w-10 text-primary" />,
      details: 'Tests are written to the src/test directory, maintaining the same package structure as the original source files. Package-private functions are tested from the same package.'
    }
  ];
  
  const nextStep = () => {
    setActiveStep((prev) => (prev + 1) % steps.length);
  };
  
  const prevStep = () => {
    setActiveStep((prev) => (prev === 0 ? steps.length - 1 : prev - 1));
  };
  
  return (
    <div className="w-full max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-8">
        <Button variant="outline" onClick={prevStep}>Previous</Button>
        <h2 className="text-2xl font-bold text-center">StGe Architecture Flow</h2>
        <Button variant="outline" onClick={nextStep}>Next</Button>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-6 gap-4 mb-12">
        {steps.map((step, index) => (
          <Card 
            key={step.id} 
            className={`transition-all ${activeStep === index ? 'ring-2 ring-primary' : 'opacity-70'}`}
            onClick={() => setActiveStep(index)}
          >
            <CardHeader className="p-4">
              <div className="flex justify-center">{step.icon}</div>
              <CardTitle className="text-center mt-2 text-sm">{step.title}</CardTitle>
            </CardHeader>
            {index < steps.length - 1 && (
              <div className="hidden lg:flex justify-center">
                <ArrowRight className="text-muted-foreground" />
              </div>
            )}
          </Card>
        ))}
      </div>
      
      <Card className="mb-8">
        <CardHeader>
          <CardTitle>{steps[activeStep].title}</CardTitle>
          <CardDescription>{steps[activeStep].description}</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-lg">{steps[activeStep].details}</p>
        </CardContent>
      </Card>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <Card>
          <CardHeader>
            <CardTitle>Key Components</CardTitle>
            <CardDescription>Main classes and their responsibilities</CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-4">
              <li className="flex items-start">
                <FileCode className="h-5 w-5 mr-2 mt-1 text-primary" />
                <div>
                  <h4 className="font-medium">EnhancedStaticAnalyzer</h4>
                  <p className="text-sm text-muted-foreground">Analyzes Kotlin source code using PSI to build a complete model of the code structure, including classes, functions, and branches.</p>
                </div>
              </li>
              <li className="flex items-start">
                <FileCode className="h-5 w-5 mr-2 mt-1 text-primary" />
                <div>
                  <h4 className="font-medium">TestGenerator</h4>
                  <p className="text-sm text-muted-foreground">Generates JUnit 5 test cases for each function and branch, ensuring 100% branch coverage with specific tests for both true and false conditions.</p>
                </div>
              </li>
              <li className="flex items-start">
                <FileCode className="h-5 w-5 mr-2 mt-1 text-primary" />
                <div>
                  <h4 className="font-medium">Models.kt</h4>
                  <p className="text-sm text-muted-foreground">Defines data structures for representing code elements like classes, functions, parameters, and branches.</p>
                </div>
              </li>
            </ul>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader>
            <CardTitle>Data Flow</CardTitle>
            <CardDescription>How data moves through the system</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="p-4 rounded-md bg-secondary">
                <p className="font-medium">1. Kotlin Files → EnhancedStaticAnalyzer</p>
                <p className="text-sm text-muted-foreground">Source files are parsed and analyzed to extract structure</p>
              </div>
              <div className="flex justify-center">
                <ArrowRight className="text-primary" />
              </div>
              <div className="p-4 rounded-md bg-secondary">
                <p className="font-medium">2. EnhancedAnalysisResult → TestGenerator</p>
                <p className="text-sm text-muted-foreground">Analysis results are transformed into test cases</p>
              </div>
              <div className="flex justify-center">
                <ArrowRight className="text-primary" />
              </div>
              <div className="p-4 rounded-md bg-secondary">
                <p className="font-medium">3. TestCases → File System</p>
                <p className="text-sm text-muted-foreground">Generated tests are written to appropriate test directories</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default ArchitectureDiagram; 