package ru.ifmo.java.server_architectures_testing.server.asynchronous;

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

    private final ExecutorService tasksPool;
    private final AsynchronousServerSocketChannel serverSocketChannel;
    private final PrintStream errorsOutputStream;
    private final AsynchronousChannelGroup group;

    public AsynchronousServer(int tasksThreadsNumber, OutputStream errorsOutputStream) throws IOException {
        tasksPool = Executors.newFixedThreadPool(tasksThreadsNumber);
        group = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory());
        serverSocketChannel = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(Constants.ASYNCHRONOUS_SERVER_PORT));
        this.errorsOutputStream = new PrintStream(errorsOutputStream);
    }


    @Override
    public void run() {
        isAlive = true;
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                serverSocketChannel.accept(null, this);
                AsynchronousClientContext clientContext = new AsynchronousClientContext(result, tasksPool, errorsOutputStream);
                clients.add(clientContext);
                new AsynchronousServerReader(result, clientContext).run();
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
