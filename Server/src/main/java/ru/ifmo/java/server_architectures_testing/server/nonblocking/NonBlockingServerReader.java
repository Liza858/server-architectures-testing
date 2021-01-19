package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class NonBlockingServerReader implements Runnable {
    private final Selector readSelector;
    private final Lock readSelectorLock;
    private final ConcurrentHashMap<NonBlockingClientContext, NonBlockingClientContext> clients;
    private final PrintStream errorsOutputStream;

    public NonBlockingServerReader(Selector readSelector, Lock readSelectorLock, ConcurrentHashMap<NonBlockingClientContext, NonBlockingClientContext> clients, PrintStream errorsOutputStream) {
        this.readSelector = readSelector;
        this.readSelectorLock = readSelectorLock;
        this.clients = clients;
        this.errorsOutputStream = errorsOutputStream;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                readSelector.select();
                readSelectorLock.lock();
                Set<SelectionKey> keys = readSelector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    SocketChannel channel = (SocketChannel) key.channel();
                    NonBlockingClientContext clientContext = (NonBlockingClientContext) key.attachment();
                    int readBytes = -1;
                    try {
                        readBytes = channel.read(clientContext.getBuffer());
                    } catch (IOException e) {
                        clientContext.error(e);
                    }
                    if (readBytes == -1) {
                        key.cancel();
                        clientContext.closeConnection();
                        clients.remove(clientContext);
                        continue;
                    }
                    if (clientContext.readAllMessage()) {
                        Protocol.SortRequest request = clientContext.getSortRequestMessage();
                        if (request != null) {
                            clientContext.processSortRequest(request);
                            clientContext.clearBuffers();
                        }
                    }
                    iterator.remove();
                }
                readSelectorLock.unlock();
            }
        } catch (IOException e) {
            e.printStackTrace(errorsOutputStream);
        }
    }
}
