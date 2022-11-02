package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingServerWriter implements Runnable {

    private static final long SELECT_TIMEOUT = 100;
    private final @NotNull Selector writeSelector;
    private final @NotNull Set<NonBlockingClientContext> clients;
    private final @NotNull PrintStream errorsOutputStream;

    public NonBlockingServerWriter(
            @NotNull Selector writeSelector,
            @NotNull Set<NonBlockingClientContext> clients,
            @NotNull PrintStream errorsOutputStream) {
        this.writeSelector = writeSelector;
        this.clients = clients;
        this.errorsOutputStream = errorsOutputStream;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                writeSelector.select(SELECT_TIMEOUT);
                Set<SelectionKey> keys = writeSelector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    processKey(key);
                    iterator.remove();
                }

                registerClientsIfNeeded();
            }
        } catch (ClosedSelectorException ignored) {
        } catch (Exception e) {
            e.printStackTrace(errorsOutputStream);
        }
    }

    private void registerClientsIfNeeded() throws ClosedChannelException {
        for (NonBlockingClientContext clientContext : clients) {
            if (clientContext.getStatus() == ClientStatus.NEW) {
                clientContext.getChannel().register(writeSelector, SelectionKey.OP_WRITE, clientContext);
                clientContext.setStatus(ClientStatus.REGISTER);
            }
        }
    }

    private void processKey(@NotNull SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        NonBlockingClientContext clientContext = (NonBlockingClientContext) key.attachment();
        ByteBuffer buffer = clientContext.getQueueToWrite().peek();

        if (buffer != null) {
            int bytesWrite = -1;
            try {
                bytesWrite = channel.write(buffer);
            } catch (IOException e) {
                clientContext.error(e);
            }
            if (bytesWrite == -1) {
                key.cancel();
                clientContext.closeConnection();
                clients.remove(clientContext);
                return;
            }

            if (!channel.isOpen()) {
                clients.remove(clientContext);
                return;
            }

            if (!buffer.hasRemaining()) {
                clientContext.getQueueToWrite().poll();
            }
        }

        if (buffer == null || clientContext.getQueueToWrite().isEmpty()) {
            key.interestOps(0); // nothing interesting
            clientContext.setStatus(ClientStatus.NON_REGISTER);
        }
    }
}
