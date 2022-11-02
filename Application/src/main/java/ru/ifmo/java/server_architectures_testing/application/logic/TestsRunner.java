package ru.ifmo.java.server_architectures_testing.application.logic;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TestsRunner {

    private static final String taskExecutionTimeFileName = "taskExecutionTime.txt";
    private static final String clientProcessTimeFileName = "clientProcessTime.txt";
    private static final String requestAverageTimeFileName = "requestAverageTime.txt";

    private TestsRunner() {
    }

    // simple example
    // you must start the server before that
    public static void main(String[] args) {
        test(
                new File(System.getProperty("user.dir")),
                "localhost",
                ServerArchitectureType.BLOCKING,
                1,
                2,
                0,
                1,
                TestParam.N,
                1,
                101,
                10
        );
    }

    private static void writeDescription(
            PrintStream printStream,
            TestCaseInfo info,
            TestParam testParam,
            int startParamValue,
            int endParamValue,
            int step
    ) {
        printStream.print("TestParam: ");
        printStream.println(testParam);
        printStream.print("StartParamValue: ");
        printStream.println(startParamValue);
        printStream.print("EndParamValue: ");
        printStream.println(endParamValue);
        printStream.print("Step: ");
        printStream.println(step);
        printStream.print("Architecture: ");
        printStream.println(info.serverArchitectureType);
        printStream.print("N (ArraySize): ");
        printStream.println(info.arraySize);
        printStream.print("M (ClientsCount): ");
        printStream.println(info.clientsCount);
        printStream.print("delta (TimeDelta): ");
        printStream.println(info.timeDeltaBetweenRequests);
        printStream.print("RequestsCount: ");
        printStream.println(info.requestsCount);
        printStream.flush();
    }

    private static void writeResultsToFiles(
            File resultsFolder,
            String fileName,
            ArrayList<TestResult> results,
            TimeType timeType,
            TestParam testParam,
            int startParamValue,
            int endParamValue,
            int step
    ) {
        if (results.size() == 0) {
            return;
        }
        TestResult first = results.get(0);
        ServerArchitectureType architectureType = first.testCaseInfo.serverArchitectureType;
        File file = new File(
                resultsFolder,
                architectureType.toString().toLowerCase() + "_" + fileName
        );
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            try (PrintStream printStream = new PrintStream(fileOutputStream)) {
                writeDescription(
                        printStream, first.testCaseInfo, testParam,
                        startParamValue, endParamValue, step
                );
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

    private static void writeResultsToFiles(
            File resultsFolder,
            ArrayList<TestResult> results,
            TestParam testParam,
            int startParamValue,
            int endParamValue,
            int step
    ) {
        writeResultsToFiles(
                resultsFolder, taskExecutionTimeFileName,
                results, TimeType.TASK_EXECUTION_TIME, testParam,
                startParamValue, endParamValue, step
        );
        writeResultsToFiles(
                resultsFolder, clientProcessTimeFileName,
                results, TimeType.CLIENT_PROCESS_TIME, testParam,
                startParamValue, endParamValue, step
        );
        writeResultsToFiles(
                resultsFolder, requestAverageTimeFileName,
                results, TimeType.REQUEST_AVERAGE_TIME, testParam,
                startParamValue, endParamValue, step
        );
    }

    public static @NotNull ArrayList<TestResult> test(
            @NotNull File resultsFolder,
            @NotNull String serverHost,
            @NotNull ServerArchitectureType serverArchitectureType,
            int arraySize,
            int clientsCount,
            int timeDeltaBetweenRequests,
            int requestsCount,
            TestParam testParam,
            int startParamValue,
            int endParamValue,
            int step
    ) {
        TestCaseGenerator testCaseGenerator = new TestCaseGenerator(
                serverArchitectureType,
                arraySize,
                clientsCount,
                timeDeltaBetweenRequests,
                requestsCount,
                testParam,
                startParamValue,
                endParamValue,
                step
        );

        ArrayList<TestResult> results = test(serverHost, testCaseGenerator);

        writeResultsToFiles(
                resultsFolder, results, testParam,
                startParamValue, endParamValue, step
        );

        return results;
    }

    public static @NotNull ArrayList<TestResult> test(
            @NotNull String serverHost,
            @NotNull TestCaseGenerator testCaseGenerator
    ) {
        ArrayList<TestResult> results = new ArrayList<>();

        while (testCaseGenerator.hasNext()) {
            TestCase testCase = testCaseGenerator.next();
            if (testCase == null) break;
            TestApplication testApplication = new TestApplication(
                    serverHost,
                    testCase.serverArchitectureType,
                    testCase.arraySize,
                    testCase.clientsCount,
                    testCase.timeDeltaBetweenRequests,
                    testCase.requestsCount
            );
            testApplication.run();
            results.add(testApplication.getTestingResult());
        }
        return results;
    }

    private enum TimeType {
        CLIENT_PROCESS_TIME,
        TASK_EXECUTION_TIME,
        REQUEST_AVERAGE_TIME
    }
}
