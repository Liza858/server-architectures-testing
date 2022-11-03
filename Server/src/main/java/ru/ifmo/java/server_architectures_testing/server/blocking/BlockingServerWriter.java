package ru.ifmo.java.server_architectures_testing.server.blocking;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.ResponseMessage;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

public class BlockingServerWriter implements Runnable {

    private final @NotNull BlockingClientContext clientContext;
    private final @NotNull Protocol.SortResponse response;
    private final @NotNull OutputStream outputStream;

    public BlockingServerWriter(
            @NotNull BlockingClientContext clientContext,
            @NotNull Protocol.SortResponse response
    ) {
        this.clientContext = clientContext;
        this.response = response;
        this.outputStream = clientContext.getOutputStream();
    }

    @Override
    public void run() {
        ResponseMessage message = new ResponseMessage(response);
        try {
            outputStream.write(message.getHead());
            outputStream.write(message.getBody());
            outputStream.flush();
        } catch (SocketException ignored) {
        } catch (IOException e) {
            clientContext.error(e);
        }
    }
}
