package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import ru.ifmo.java.server_architectures_testing.Constants;
import ru.ifmo.java.server_architectures_testing.server.ClientContext;
import ru.ifmo.java.server_architectures_testing.server.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NonBlockingServer extends Server {

    private final ExecutorService readPool = Executors.newSingleThreadExecutor();
    private final ExecutorService writePool = Executors.newSingleThreadExecutor();
    private final ExecutorService tasksPool;
    private final Selector readSelector = Selector.open();
    private final Selector writeSelector = Selector.open();
    private final Lock readSelectorLock = new ReentrantLock();
    private final PrintStream errorsOutputStream;
    private final ServerSocketChannel serverSocketChannel;
    private final ConcurrentHashMap<NonBlockingClientContext, NonBlockingClientContext> clientsContexts = new ConcurrentHashMap<>();

    public NonBlockingServer(int tasksThreadsNumber, OutputStream errorsOutputStream) throws IOException {
        tasksPool = Executors.newFixedThreadPool(tasksThreadsNumber);
        this.errorsOutputStream = new PrintStream(errorsOutputStream);
        serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(Constants.NON_BLOCKING_SERVER_PORT));
    }


    @Override
    public void run() {
        readPool.submit(new NonBlockingServerReader(readSelector, readSelectorLock, clientsContexts, errorsOutputStream));
        writePool.submit(new NonBlockingServerWriter(writeSelector, clientsContexts, errorsOutputStream));
        while (true) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                readSelectorLock.lock();
                readSelector.wakeup();
                NonBlockingClientContext clientContext = new NonBlockingClientContext(socketChannel, writeSelector, tasksPool, errorsOutputStream);
                clients.add(clientContext);
                clientsContexts.put(clientContext, clientContext);
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
        isAlive = false;
    }
}
