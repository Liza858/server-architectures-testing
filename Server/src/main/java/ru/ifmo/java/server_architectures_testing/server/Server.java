package ru.ifmo.java.server_architectures_testing.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;
import ru.ifmo.java.server_architectures_testing.server.asynchronous.AsynchronousServer;
import ru.ifmo.java.server_architectures_testing.server.blocking.BlockingServer;
import ru.ifmo.java.server_architectures_testing.server.nonblocking.NonBlockingServer;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Server implements Runnable {
    protected volatile boolean isAlive = true;

    public static @Nullable Server createServer(
            @NotNull ServerArchitectureType type,
            int tasksThreadsNumber,
            @NotNull OutputStream errorsOutputStream
    ) throws IOException {
        switch (type) {
            case BLOCKING:
                return new BlockingServer(tasksThreadsNumber, errorsOutputStream);
            case NON_BLOCKING:
                return new NonBlockingServer(tasksThreadsNumber, errorsOutputStream);
            case ASYNCHRONOUS:
                return new AsynchronousServer(tasksThreadsNumber, errorsOutputStream);
        }
        return null;
    }

    public abstract void stop() throws IOException;

    public boolean isAlive() {
        return isAlive;
    }
}
