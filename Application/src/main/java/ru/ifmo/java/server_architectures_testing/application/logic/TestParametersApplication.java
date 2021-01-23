package ru.ifmo.java.server_architectures_testing.application.logic;

import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TestParametersApplication {

    private static final String taskExecutionTimeFileName = "taskExecutionTime.txt";
    private static final String clientProcessTimeFileName = "clientProcessTime.txt";
    private static final String requestAverageTimeFileName = "requestAverageTime.txt";

    private TestParametersApplication() {
    }

    public static void main(String[] args) {
        testDifferentArraySize(
                new File(System.getProperty("user.dir")),
                10,
                ServerArchitectureType.BLOCKING,
                1,
                2,
                0,
                1,
                1,
                1
        );
    }

    private static void writeDescriptionToFile(
            File downloadFolder,
            ArrayList<TestResult> results,
            TestParam testParam,
            int startValue,
            int endValue,
            int step
    ) {
        if (results.size() == 0) {
            return;
        }
        String descriptionFileName = "description.txt";
        File descriptionFile = new File(downloadFolder, descriptionFileName);
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

    private static void writeResultsToFile(
            File downloadFolder,
            String fileName,
            ArrayList<TestResult> results,
            TimeType timeType
    ) {
        if (results.size() == 0) {
            return;
        }
        String architecture = results.get(0).testParameters.serverArchitectureType.toString();
        File file = new File(downloadFolder, architecture.toLowerCase() + "_" + fileName);
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

    public static ArrayList<TestResult> testDifferentArraySize(
            File downloadFolder,
            int tasksThreadsNumber,
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

        writeDescriptionToFile(downloadFolder, results, TestParam.N, startValue, endValue, step);
        writeResultsToFile(downloadFolder, taskExecutionTimeFileName, results, TimeType.TASK_EXECUTION_TIME);
        writeResultsToFile(downloadFolder, clientProcessTimeFileName, results, TimeType.CLIENT_PROCESS_TIME);
        writeResultsToFile(downloadFolder, requestAverageTimeFileName, results, TimeType.REQUEST_AVERAGE_TIME);

        return results;
    }

    public static ArrayList<TestResult> testDifferentClientsCount(
            File downloadFolder,
            int tasksThreadsNumber,
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

        writeDescriptionToFile(downloadFolder, results, TestParam.M, startValue, endValue, step);
        writeResultsToFile(downloadFolder, taskExecutionTimeFileName, results, TimeType.TASK_EXECUTION_TIME);
        writeResultsToFile(downloadFolder, clientProcessTimeFileName, results, TimeType.CLIENT_PROCESS_TIME);
        writeResultsToFile(downloadFolder, requestAverageTimeFileName, results, TimeType.REQUEST_AVERAGE_TIME);

        return results;
    }

    public static ArrayList<TestResult> testDifferentTimeDelta(
            File downloadFolder,
            int tasksThreadsNumber,
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

        writeDescriptionToFile(downloadFolder, results, TestParam.delta, startValue, endValue, step);
        writeResultsToFile(downloadFolder, taskExecutionTimeFileName, results, TimeType.TASK_EXECUTION_TIME);
        writeResultsToFile(downloadFolder, clientProcessTimeFileName, results, TimeType.CLIENT_PROCESS_TIME);
        writeResultsToFile(downloadFolder, requestAverageTimeFileName, results, TimeType.REQUEST_AVERAGE_TIME);

        return results;
    }

    private enum TimeType {
        CLIENT_PROCESS_TIME,
        TASK_EXECUTION_TIME,
        REQUEST_AVERAGE_TIME
    }
}
