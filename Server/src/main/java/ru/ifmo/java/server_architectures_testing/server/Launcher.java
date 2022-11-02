package ru.ifmo.java.server_architectures_testing.server;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;

import java.io.IOException;
import java.net.BindException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(ServerArchitectureType.values().length);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("wrong number of arguments!");
            return;
        }
        int tasksThreadsNumber = Integer.parseInt(args[0]);
        Server blocking = tryCreateServer(ServerArchitectureType.BLOCKING, tasksThreadsNumber);
        Server nonblocking = tryCreateServer(ServerArchitectureType.NON_BLOCKING, tasksThreadsNumber);
        Server asynchronous = tryCreateServer(ServerArchitectureType.ASYNCHRONOUS, tasksThreadsNumber);

        executorService.submit(blocking);
        executorService.submit(nonblocking);
        executorService.submit(asynchronous);
    }

    private static @NotNull Server tryCreateServer(
            @NotNull ServerArchitectureType serverArchitectureType,
            int tasksThreadsNumber
    ) throws IOException {
        BindException exception = null;
        int maxNumberOfAttempts = 10;
        int currentAttemptNumber = 1;
        Server server = null;
        while (currentAttemptNumber <= maxNumberOfAttempts) {
            try {
                server = Server.createServer(serverArchitectureType, tasksThreadsNumber, System.err);
                break;
            } catch (BindException ex) {
                exception = ex;
            }
            currentAttemptNumber += 1;
        }

        if (server == null) {
            if (exception != null) throw exception;
            throw new RuntimeException();
        }

        return server;
    }
}
