package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class NonBlockingServerReader implements Runnable {
    private final @NotNull Selector readSelector;
    private final @NotNull Lock readSelectorLock;
    private final @NotNull Set<NonBlockingClientContext> clients;
    private final @NotNull PrintStream errorsOutputStream;

    public NonBlockingServerReader(
            @NotNull Selector readSelector,
            @NotNull Lock readSelectorLock,
            @NotNull Set<NonBlockingClientContext> clients,
            @NotNull PrintStream errorsOutputStream
    ) {
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
                    processKey(key);
                    iterator.remove();
                }
                readSelectorLock.unlock();
            }
        } catch (ClosedSelectorException ignored) {
        } catch (Exception e) {
            e.printStackTrace(errorsOutputStream);
        }
    }

    private void processKey(@NotNull SelectionKey key) {
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
            return;
        }
        if (clientContext.readAllMessage()) {
            Protocol.SortRequest request = clientContext.getSortRequestMessage();
            if (request != null) {
                clientContext.processSortRequest(request);
                clientContext.clearBuffers();
            }
        }
    }
}
