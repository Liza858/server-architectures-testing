package ru.ifmo.java.server_architectures_testing.application.logic;

public class TestResult {
    public TestParameters testParameters = new TestParameters();
    public double clientProcessTime = -1.0;
    public double taskExecutionTime = -1.0;
    public double requestAverageTime = 0.0;
}
