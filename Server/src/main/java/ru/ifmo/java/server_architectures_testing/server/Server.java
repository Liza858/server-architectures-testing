package ru.ifmo.java.server_architectures_testing.server;

import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;
import ru.ifmo.java.server_architectures_testing.server.asynchronous.AsynchronousServer;
import ru.ifmo.java.server_architectures_testing.server.blocking.BlockingServer;
import ru.ifmo.java.server_architectures_testing.server.nonblocking.NonBlockingServer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

public abstract class Server implements Runnable {
    protected final HashSet<ClientContext> clients = new HashSet<>();
    protected volatile boolean isAlive = true;

    public static Server createServer(ServerArchitectureType type, int taskThreadsNumber, OutputStream errorsOutputStream) throws IOException {
        switch (type) {
            case BLOCKING:
                return new BlockingServer(taskThreadsNumber, errorsOutputStream);
            case NON_BLOCKING:
                return new NonBlockingServer(taskThreadsNumber, errorsOutputStream);
            case ASYNCHRONOUS:
                return new AsynchronousServer(taskThreadsNumber, errorsOutputStream);
        }
        return null;
    }

    public double getClientProcessTimeStatistic() {
        return getAverageTimeByClients(TimeType.CLIENT_PROCESS_TIME);
    }

    public double getTaskExecutionTimeStatistic() {
        return getAverageTimeByClients(TimeType.TASK_EXECUTION_TIME);
    }

    private double getAverageTimeByClients(TimeType type) {
        double time = 0;
        int count = clients.size();
        for (ClientContext clientContext : clients) {
            if (type == TimeType.CLIENT_PROCESS_TIME) {
                time += clientContext.getClientProcessTime() / count;
            } else if (type == TimeType.TASK_EXECUTION_TIME) {
                time += clientContext.getTaskExecutionTime() / count;
            }
        }
        return time;
    }

    public abstract void stop() throws IOException;

    public boolean isAlive() {
        return isAlive;
    }

    private enum TimeType {
        CLIENT_PROCESS_TIME,
        TASK_EXECUTION_TIME
    }
}
