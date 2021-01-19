package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class AsynchronousServerWriter {
    private final AsynchronousClientContext clientContext;

    public AsynchronousServerWriter(AsynchronousClientContext clientContext) {
        this.clientContext = clientContext;
    }

    public void write(ByteBuffer byteBuffer) {
        clientContext.getChannel().write(
                byteBuffer,
                clientContext,
                new CompletionHandler<Integer, AsynchronousClientContext>() {
                    @Override
                    public void completed(Integer result, AsynchronousClientContext attachment) {
                        if (result == -1) {
                            clientContext.closeConnection();
                            return;
                        }
                        if (byteBuffer.limit() - byteBuffer.position() != 0) {
                            write(byteBuffer);
                            return;
                        }
                    }

                    @Override
                    public void failed(Throwable exc, AsynchronousClientContext attachment) {
                        clientContext.error(exc);
                        clientContext.closeConnection();
                    }
                }
        );
    }
}
