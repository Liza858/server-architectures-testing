package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class AsynchronousServerWriteHandler implements CompletionHandler<Integer, AsynchronousClientContext> {

    @Override
    public void completed(Integer result, AsynchronousClientContext clientContext) {
        if (result == -1) {
            clientContext.closeConnection();
            return;
        }
        ByteBuffer buffer = clientContext.getWriteBuffer();
        if (buffer.hasRemaining()) {
            clientContext.getChannel().write(buffer, clientContext, this);
        }
    }

    @Override
    public void failed(Throwable exc, AsynchronousClientContext clientContext) {
        clientContext.error(exc);
        clientContext.closeConnection();
    }
}
