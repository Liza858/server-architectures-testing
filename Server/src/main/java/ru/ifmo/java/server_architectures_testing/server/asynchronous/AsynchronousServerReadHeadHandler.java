package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class AsynchronousServerReadHeadHandler implements CompletionHandler<Integer, AsynchronousClientContext> {

    @Override
    public void completed(Integer result, AsynchronousClientContext clientContext) {
        if (result == -1) {
            clientContext.closeConnection();
            return;
        }
        ByteBuffer headBuffer = clientContext.getHeadBuffer();
        if (headBuffer.hasRemaining()) {
            clientContext.getChannel().read(headBuffer, clientContext, this);
        } else {
            headBuffer.flip();
            int size = headBuffer.getInt();
            headBuffer.clear();
            clientContext.allocateBodyBuffer(size);
            clientContext.getChannel().read(clientContext.getBodyBuffer(), clientContext, clientContext.getReadBodyHandler());
        }
    }

    @Override
    public void failed(Throwable exc, AsynchronousClientContext clientContext) {
        clientContext.error(exc);
        clientContext.closeConnection();
    }
}
