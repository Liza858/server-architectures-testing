package ru.ifmo.java.server_architectures_testing.application.logic;

public class TestResult {
    public final TestCaseInfo testCaseInfo;
    public double clientProcessTime = 0.0;
    public double taskExecutionTime = 0.0;
    public double requestAverageTime = 0.0;

    public TestResult(TestCaseInfo testCaseInfo) {
        this.testCaseInfo = testCaseInfo;
    }
}
