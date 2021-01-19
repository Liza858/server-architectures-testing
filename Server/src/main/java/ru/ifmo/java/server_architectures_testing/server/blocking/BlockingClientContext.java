package ru.ifmo.java.server_architectures_testing.server.blocking;

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

    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public BlockingClientContext(Socket socket, ExecutorService tasksPool, PrintStream errorsOutputStream) throws IOException {
        super(tasksPool, errorsOutputStream);
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean connectionIsClosed() {
        return socket.isClosed();
    }

    public OutputStream getOutputStream() {
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
    public void sendToWrite(ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime) {
        updateTimeStatistics(clientProcessTime, taskExecutionTime);
        Protocol.SortResponse response = getSortResponse(sortedArray);
        writeExecutor.submit(new BlockingServerWriter(this, response));
    }
}
