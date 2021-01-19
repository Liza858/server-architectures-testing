package ru.ifmo.java.server_architectures_testing.application.logic;

import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TestParametersApplication {

    private final int tasksThreadsNumber;
    private final File descriptionFile = new File(System.getProperty("user.dir") + File.separator + "description.txt");
    private final File taskExecutionTimeFile = new File(System.getProperty("user.dir") + File.separator + "taskExecutionTime.txt");
    private final File clientProcessTimeFile = new File(System.getProperty("user.dir") + File.separator + "clientProcessTime.txt");
    private final File requestAverageTimeFile = new File(System.getProperty("user.dir") + File.separator + File.separator + "requestAverageTime.txt");

    public TestParametersApplication(int tasksThreadsNumber) {
        this.tasksThreadsNumber = tasksThreadsNumber;
    }

    public static void main(String[] args) {
        TestParametersApplication testingParameters = new TestParametersApplication(10);
        testingParameters.testDifferentArraySize(
                ServerArchitectureType.BLOCKING,
                5,
                5,
                0,
                10,
                1000,
                200
        );
    }

    private void writeDescriptionToFile(
            ArrayList<TestResult> results,
            TestParam testParam,
            int startValue,
            int endValue,
            int step
    ) {
        if (results.size() == 0) {
            return;
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(descriptionFile)) {
            try (PrintStream printStream = new PrintStream(fileOutputStream)) {
                printStream.print("TestParam: ");
                printStream.println(testParam);
                printStream.print("Architecture: ");
                printStream.println(results.get(0).testParameters.serverArchitectureType);
                printStream.print("ArraySize: ");
                printStream.println(results.get(0).testParameters.arraySize);
                printStream.print("ClientsCount: ");
                printStream.println(results.get(0).testParameters.clientsCount);
                printStream.print("TimeDelta: ");
                printStream.println(results.get(0).testParameters.timeDeltaBetweenRequests);
                printStream.print("RequestsCount: ");
                printStream.println(results.get(0).testParameters.requestsCount);
                printStream.print("StartParamValue: ");
                printStream.println(startValue);
                printStream.print("EndParamValue: ");
                printStream.println(endValue);
                printStream.print("Step: ");
                printStream.println(step);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeResultsToFile(
            File file,
            ArrayList<TestResult> results,
            TimeType timeType
    ) {
        if (results.size() == 0) {
            return;
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            try (PrintStream printStream = new PrintStream(fileOutputStream)) {
                for (TestResult result : results) {
                    switch (timeType) {
                        case TASK_EXECUTION_TIME:
                            printStream.println(result.taskExecutionTime);
                            break;
                        case CLIENT_PROCESS_TIME:
                            printStream.println(result.clientProcessTime);
                            break;
                        case REQUEST_AVERAGE_TIME:
                            printStream.println(result.requestAverageTime);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<TestResult> testDifferentArraySize(
            ServerArchitectureType type,
            int clientsCount,
            int requestsCount,
            int timeDeltaBetweenRequests,
            int startValue,
            int endValue,
            int step
    ) {
        ArrayList<TestResult> results = new ArrayList<>();
        int arraySize = startValue;
        while (true) {
            TestApplication testApplication = new TestApplication(
                    type,
                    tasksThreadsNumber,
                    arraySize,
                    clientsCount,
                    timeDeltaBetweenRequests,
                    requestsCount
            );
            testApplication.run();
            results.add(testApplication.getTestingResult());
            if (arraySize == endValue) {
                break;
            }
            arraySize = Math.min(endValue, arraySize + step);
        }

        writeDescriptionToFile(results, TestParam.N, startValue, endValue, step);
        writeResultsToFile(taskExecutionTimeFile, results, TimeType.TASK_EXECUTION_TIME);
        writeResultsToFile(clientProcessTimeFile, results, TimeType.CLIENT_PROCESS_TIME);
        writeResultsToFile(requestAverageTimeFile, results, TimeType.REQUEST_AVERAGE_TIME);

        return results;
    }

    public ArrayList<TestResult> testDifferentClientsCount(
            ServerArchitectureType type,
            int arraySize,
            int requestsCount,
            int timeDeltaBetweenRequests,
            int startValue,
            int endValue,
            int step
    ) {
        ArrayList<TestResult> results = new ArrayList<>();
        int clientsCount = startValue;
        while (true) {
            TestApplication testingApplication = new TestApplication(
                    type,
                    tasksThreadsNumber,
                    arraySize,
                    clientsCount,
                    timeDeltaBetweenRequests,
                    requestsCount
            );
            testingApplication.run();
            results.add(testingApplication.getTestingResult());
            if (clientsCount == endValue) {
                break;
            }
            clientsCount = Math.min(endValue, clientsCount + step);
        }

        writeDescriptionToFile(results, TestParam.M, startValue, endValue, step);
        writeResultsToFile(taskExecutionTimeFile, results, TimeType.TASK_EXECUTION_TIME);
        writeResultsToFile(clientProcessTimeFile, results, TimeType.CLIENT_PROCESS_TIME);
        writeResultsToFile(requestAverageTimeFile, results, TimeType.REQUEST_AVERAGE_TIME);

        return results;
    }

    public ArrayList<TestResult> testDifferentTimeDelta(
            ServerArchitectureType type,
            int clientsCount,
            int requestsCount,
            int arraySize,
            int startValue,
            int endValue,
            int step
    ) {
        ArrayList<TestResult> results = new ArrayList<>();
        int timeDeltaBetweenRequests = startValue;
        while (true) {
            TestApplication testingApplication = new TestApplication(
                    type,
                    tasksThreadsNumber,
                    arraySize,
                    clientsCount,
                    timeDeltaBetweenRequests,
                    requestsCount
            );
            testingApplication.run();
            results.add(testingApplication.getTestingResult());
            if (timeDeltaBetweenRequests == endValue) {
                break;
            }
            timeDeltaBetweenRequests = Math.min(endValue, timeDeltaBetweenRequests + step);
        }

        writeDescriptionToFile(results, TestParam.delta, startValue, endValue, step);
        writeResultsToFile(taskExecutionTimeFile, results, TimeType.TASK_EXECUTION_TIME);
        writeResultsToFile(clientProcessTimeFile, results, TimeType.CLIENT_PROCESS_TIME);
        writeResultsToFile(requestAverageTimeFile, results, TimeType.REQUEST_AVERAGE_TIME);

        return results;
    }

    private enum TimeType {
        CLIENT_PROCESS_TIME,
        TASK_EXECUTION_TIME,
        REQUEST_AVERAGE_TIME
    }
}
