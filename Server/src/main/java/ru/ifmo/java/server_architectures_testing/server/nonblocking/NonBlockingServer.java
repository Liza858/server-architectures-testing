package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.Constants;
import ru.ifmo.java.server_architectures_testing.server.ClientContext;
import ru.ifmo.java.server_architectures_testing.server.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NonBlockingServer extends Server {

    private final Set<NonBlockingClientContext> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final @NotNull ExecutorService readPool = Executors.newSingleThreadExecutor();
    private final @NotNull ExecutorService writePool = Executors.newSingleThreadExecutor();
    private final @NotNull ExecutorService tasksPool;
    private final @NotNull Selector readSelector = Selector.open();
    private final @NotNull Selector writeSelector = Selector.open();
    private final @NotNull Lock readSelectorLock = new ReentrantLock();
    private final @NotNull PrintStream errorsOutputStream;
    private final @NotNull ServerSocketChannel serverSocketChannel;

    public NonBlockingServer(int tasksThreadsNumber, @NotNull OutputStream errorsOutputStream) throws IOException {
        tasksPool = Executors.newFixedThreadPool(tasksThreadsNumber);
        this.errorsOutputStream = new PrintStream(errorsOutputStream);
        serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(Constants.NON_BLOCKING_SERVER_PORT));
    }

    @Override
    public void run() {
        readPool.submit(new NonBlockingServerReader(readSelector, readSelectorLock, clients, errorsOutputStream));
        writePool.submit(new NonBlockingServerWriter(writeSelector, clients, errorsOutputStream));
        while (true) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                readSelectorLock.lock();
                readSelector.wakeup();
                NonBlockingClientContext clientContext =
                        new NonBlockingClientContext(
                                socketChannel, writeSelector, tasksPool, errorsOutputStream
                        );
                clients.add(clientContext);
                socketChannel.register(readSelector, SelectionKey.OP_READ, clientContext);
                readSelectorLock.unlock();
            } catch (AsynchronousCloseException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace(errorsOutputStream);
            }
        }
    }

    @Override
    public void stop() throws IOException {
        if (serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
        }
        readSelector.close();
        writeSelector.close();
        readPool.shutdown();
        writePool.shutdown();
        tasksPool.shutdown();
        for (ClientContext clientContext : clients) {
            clientContext.closeConnection();
        }
        clients.clear();
        isAlive = false;
    }
}
