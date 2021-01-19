package ru.ifmo.java.server_architectures_testing.server.blocking;

import ru.ifmo.java.server_architectures_testing.ResponseMessage;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

public class BlockingServerWriter implements Runnable {

    private final BlockingClientContext clientContext;
    private final Protocol.SortResponse response;
    private final OutputStream outputStream;

    public BlockingServerWriter(BlockingClientContext clientContext, Protocol.SortResponse response) {
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
        } catch (IOException e) {
            clientContext.error(e);
        }
    }
}
