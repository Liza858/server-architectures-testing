package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AsynchronousServerReader {
    private final AsynchronousSocketChannel channel;
    private final AsynchronousClientContext clientContext;

    public AsynchronousServerReader(
            AsynchronousSocketChannel channel,
            AsynchronousClientContext clientContext
    ) {
        this.channel = channel;
        this.clientContext = clientContext;
    }

    public void run() {
        readHead(clientContext.getHeadBuffer());
    }

    public void readHead(ByteBuffer headBuffer) {
        channel.read(headBuffer, clientContext, new CompletionHandler<Integer, AsynchronousClientContext>() {
                    @Override
                    public void completed(Integer result, AsynchronousClientContext clientContext) {
                        if (result == -1) {
                            clientContext.closeConnection();
                            return;
                        }
                        if (headBuffer.limit() - headBuffer.position() != 0) {
                            readHead(headBuffer);
                        }
                        headBuffer.flip();
                        int size = headBuffer.getInt();
                        readBody(clientContext.getBodyBuffer(size));
                    }

                    @Override
                    public void failed(Throwable exc, AsynchronousClientContext clientContext) {
                        clientContext.error(exc);
                        clientContext.closeConnection();
                    }
                }
        );
    }

    public void readBody(ByteBuffer bodyBuffer) {
        channel.read(bodyBuffer, clientContext, new CompletionHandler<Integer, AsynchronousClientContext>() {
                    @Override
                    public void completed(Integer result, AsynchronousClientContext clientContext) {
                        if (result == -1) {
                            clientContext.closeConnection();
                            return;
                        }
                        if (bodyBuffer.limit() - bodyBuffer.position() != 0) {
                            readBody(bodyBuffer);
                        }
                        Protocol.SortRequest request = clientContext.getSortRequestMessage(bodyBuffer);
                        clientContext.processSortRequest(request);
                        readHead(clientContext.getHeadBuffer());
                    }

                    @Override
                    public void failed(Throwable exc, AsynchronousClientContext clientContext) {
                        clientContext.error(exc);
                        clientContext.closeConnection();
                    }
                }
        );
    }
}
