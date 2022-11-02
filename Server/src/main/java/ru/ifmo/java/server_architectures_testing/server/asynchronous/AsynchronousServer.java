package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.Constants;
import ru.ifmo.java.server_architectures_testing.server.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsynchronousServer extends Server {

    private static final int HANDLERS_THREADS_NUMBER = 1;
    private final @NotNull ExecutorService tasksPool;
    private final @NotNull AsynchronousServerSocketChannel serverSocketChannel;
    private final @NotNull PrintStream errorsOutputStream;
    private final @NotNull AsynchronousChannelGroup group;
    private final @NotNull AsynchronousServerWriteHandler writeHandler = new AsynchronousServerWriteHandler();
    private final @NotNull AsynchronousServerReadHeadHandler readHeadHandler = new AsynchronousServerReadHeadHandler();
    private final @NotNull AsynchronousServerReadBodyHandler readBodyHandler = new AsynchronousServerReadBodyHandler();

    public AsynchronousServer(int tasksThreadsNumber, @NotNull OutputStream errorsOutputStream) throws IOException {
        tasksPool = Executors.newFixedThreadPool(tasksThreadsNumber);
        group = AsynchronousChannelGroup.withFixedThreadPool(HANDLERS_THREADS_NUMBER, Executors.defaultThreadFactory());
        serverSocketChannel = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(Constants.ASYNCHRONOUS_SERVER_PORT));
        this.errorsOutputStream = new PrintStream(errorsOutputStream);
    }

    @Override
    public void run() {
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Object attachment) {
                AsynchronousClientContext clientContext = new AsynchronousClientContext(
                        channel,
                        tasksPool,
                        errorsOutputStream,
                        writeHandler,
                        readHeadHandler,
                        readBodyHandler
                );
                channel.read(clientContext.getHeadBuffer(), clientContext, readHeadHandler);
                serverSocketChannel.accept(null, this);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
            }
        });
    }

    @Override
    public void stop() throws IOException {
        if (serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
        }
        group.shutdown();
        tasksPool.shutdown();
        isAlive = false;
    }
}
