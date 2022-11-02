package ru.ifmo.java.server_architectures_testing.application.logic;


import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;
import ru.ifmo.java.server_architectures_testing.Util;
import ru.ifmo.java.server_architectures_testing.application.Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class TestApplication {

    private final @NotNull String serverHost;
    private final int serverPort;
    private final int arraySize;
    private final int clientsCount;
    private final int timeDeltaBetweenRequests;
    private final int requestsCount;
    @NotNull
    private final ArrayList<Client> clients = new ArrayList<>();
    @NotNull
    private final ExecutorService clientsExecutor = Executors.newCachedThreadPool();
    @NotNull
    private final TestResult testResult;

    public TestApplication(
            @NotNull String serverHost,
            @NotNull ServerArchitectureType architectureType,
            int arraySize,
            int clientsCount,
            int timeDeltaBetweenRequests,
            int requestsCount
    ) {
        this.serverHost = serverHost;
        this.serverPort = Util.getServerPort(architectureType);
        this.arraySize = arraySize;
        this.clientsCount = clientsCount;
        this.timeDeltaBetweenRequests = timeDeltaBetweenRequests;
        this.requestsCount = requestsCount;
        this.testResult = new TestResult(new TestCaseInfo(
                architectureType,
                arraySize,
                clientsCount,
                timeDeltaBetweenRequests,
                requestsCount
        ));
    }

    public void run() {
        try {
            CyclicBarrier barrier = new CyclicBarrier(clientsCount);
            ArrayList<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < clientsCount; i++) {
                Client client = new Client(
                        serverHost,
                        serverPort,
                        System.err,
                        arraySize,
                        requestsCount,
                        timeDeltaBetweenRequests,
                        barrier
                );
                clients.add(client);
                tasks.add(clientsExecutor.submit(client));
            }

            for (Future<?> task : tasks) {
                task.get();
            }

            clientsExecutor.shutdown();

            int clientsCount = clients.size();
            for (Client client : clients) {
                testResult.requestAverageTime += client.getRequestAverageTimeUs() / 1000.0 / clientsCount; // and convert to milliseconds
                testResult.taskExecutionTime += client.getTaskExecutionAverageTimeUs() / 1000.0 / clientsCount; // and convert to milliseconds
                testResult.clientProcessTime += client.getClientProcessAverageTimeUs() / 1000.0 / clientsCount; // and convert to milliseconds
            }
        } catch (IOException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }

    public TestResult getTestingResult() {
        return testResult;
    }

    public double getRequestAverageTime() {
        return testResult.requestAverageTime;
    }

    public double getTaskExecutionTime() {
        return testResult.taskExecutionTime;
    }

    public double getClientProcessTime() {
        return testResult.clientProcessTime;
    }
}
