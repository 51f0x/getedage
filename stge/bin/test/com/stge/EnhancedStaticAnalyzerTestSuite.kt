package com.stge

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

/**
 * Test suite for running all tests for the EnhancedStaticAnalyzer
 */
@Suite
@SelectClasses(
    EnhancedStaticAnalyzerTest::class,
    EnhancedStaticAnalyzerIntegrationTest::class,
    EnhancedAnalysisResultTest::class
)
class EnhancedStaticAnalyzerTestSuite 