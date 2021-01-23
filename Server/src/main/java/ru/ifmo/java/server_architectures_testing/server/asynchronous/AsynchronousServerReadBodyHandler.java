package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class AsynchronousServerReadBodyHandler implements CompletionHandler<Integer, AsynchronousClientContext> {

    @Override
    public void completed(Integer result, AsynchronousClientContext clientContext) {
        if (result == -1) {
            clientContext.closeConnection();
            return;
        }
        ByteBuffer bodyBuffer = clientContext.getBodyBuffer();
        if (bodyBuffer.hasRemaining()) {
            clientContext.getChannel().read(bodyBuffer, clientContext, this);
        }
        Protocol.SortRequest request = clientContext.getSortRequestMessage(bodyBuffer);
        clientContext.processSortRequest(request);
        clientContext.getChannel().read(clientContext.getHeadBuffer(), clientContext, clientContext.getReadHeadHandler());
    }

    @Override
    public void failed(Throwable exc, AsynchronousClientContext clientContext) {
        clientContext.error(exc);
        clientContext.closeConnection();
    }
}
