package ru.ifmo.java.server_architectures_testing.application.logic;


import ru.ifmo.java.server_architectures_testing.Constants;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;
import ru.ifmo.java.server_architectures_testing.application.Client;
import ru.ifmo.java.server_architectures_testing.server.Server;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestApplication {

    private final ServerArchitectureType serverArchitectureType;
    private final int serverPort;
    private final int taskThreadsNumber;
    private final int arraySize;
    private final int clientsCount;
    private final int timeDeltaBetweenRequests;
    private final int requestsCount;
    private final ArrayList<Client> clients = new ArrayList<>();
    private final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService clientsExecutor = Executors.newCachedThreadPool();
    private final TestResult testResult = new TestResult();


    public TestApplication(
            ServerArchitectureType type,
            int taskThreadsNumber,
            int arraySize,
            int clientsCount,
            int timeDeltaBetweenRequests,
            int requestsCount
    ) {
        this.serverArchitectureType = type;
        int port = 8080;
        switch (type) {
            case BLOCKING:
                port = Constants.BLOCKING_SERVER_PORT;
                break;
            case NON_BLOCKING:
                port = Constants.NON_BLOCKING_SERVER_PORT;
                break;
            case ASYNCHRONOUS:
                port = Constants.ASYNCHRONOUS_SERVER_PORT;
        }
        this.serverPort = port;
        this.taskThreadsNumber = taskThreadsNumber;
        this.arraySize = arraySize;
        this.clientsCount = clientsCount;
        this.timeDeltaBetweenRequests = timeDeltaBetweenRequests;
        this.requestsCount = requestsCount;
        this.testResult.testParameters.arraySize = arraySize;
        this.testResult.testParameters.clientsCount = clientsCount;
        this.testResult.testParameters.timeDeltaBetweenRequests = timeDeltaBetweenRequests;
        this.testResult.testParameters.requestsCount = requestsCount;
        this.testResult.testParameters.serverArchitectureType = type;
    }


    public void run() {
        try {
            Server server;
            while (true) {
                try {
                    server = Server.createServer(serverArchitectureType, taskThreadsNumber, System.err);
                    break;
                } catch (BindException ex) {
                    ex.printStackTrace();
                }
            }

            if (server != null) {
                serverExecutor.submit(server);
                for (int i = 0; i < clientsCount; i++) {
                    Client client = new Client(
                            "localhost",
                            serverPort,
                            System.err,
                            arraySize,
                            requestsCount,
                            timeDeltaBetweenRequests
                    );
                    clients.add(client);
                }

                ArrayList<Future<?>> tasks = new ArrayList<>();
                for (Client client : clients) {
                    tasks.add(clientsExecutor.submit(client));
                }

                for (Future<?> task : tasks) {
                    task.get();
                }

                server.stop();
                while (server.isAlive()) {
                }

                serverExecutor.shutdown();
                clientsExecutor.shutdown();

                int clientsCount = clients.size();
                for (Client client : clients) {
                    testResult.requestAverageTime += client.getRequestAverageTime() / clientsCount;
                }
                testResult.clientProcessTime = server.getClientProcessTimeStatistic();
                testResult.taskExecutionTime = server.getTaskExecutionTimeStatistic();
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
