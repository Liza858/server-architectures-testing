package ru.ifmo.java.server_architectures_testing.server.blocking;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;
import ru.ifmo.java.server_architectures_testing.server.ClientContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingClientContext extends ClientContext {

    private final @NotNull ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
    private final @NotNull Socket socket;
    private final @NotNull InputStream inputStream;
    private final @NotNull OutputStream outputStream;

    public BlockingClientContext(
            @NotNull Socket socket,
            @NotNull ExecutorService tasksPool,
            @NotNull PrintStream errorsOutputStream
    ) throws IOException {
        super(tasksPool, errorsOutputStream);
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public @NotNull InputStream getInputStream() {
        return inputStream;
    }

    public boolean connectionIsClosed() {
        return socket.isClosed();
    }

    public @NotNull OutputStream getOutputStream() {
        return outputStream;
    }

    public void closeConnection() {
        writeExecutor.shutdown();
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                error(e);
            }
        }
    }

    @Override
    public void sendToWrite(@NotNull ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime) {
        Protocol.SortResponse response =
                createSortResponse(sortedArray, taskExecutionTime, clientProcessTime);
        writeExecutor.submit(new BlockingServerWriter(this, response));
    }
}
