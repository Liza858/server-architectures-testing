package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NonBlockingServerWriter implements Runnable {
    private final Selector writeSelector;
    private final ConcurrentHashMap<NonBlockingClientContext, NonBlockingClientContext> clients;
    private final PrintStream errorsOutputStream;

    public NonBlockingServerWriter(
            Selector writeSelector,
            ConcurrentHashMap<NonBlockingClientContext, NonBlockingClientContext> clients,
            PrintStream errorsOutputStream) {
        this.writeSelector = writeSelector;
        this.clients = clients;
        this.errorsOutputStream = errorsOutputStream;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                writeSelector.select(1000);
                Set<SelectionKey> keys = writeSelector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
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
                            continue;
                        }
                        if (!buffer.hasRemaining()) {
                            clientContext.getQueueToWrite().poll();
                        }
                    }
                    if (buffer == null || clientContext.getQueueToWrite().isEmpty()) {
                        key.interestOps(0);
                        clientContext.setStatus(ClientStatus.NON_REGISTER);
                    }

                    iterator.remove();
                }

                for (NonBlockingClientContext clientContext : clients.keySet()) {
                    if (clientContext.getStatus() == ClientStatus.NEW) {
                        clientContext.getChannel().register(writeSelector, SelectionKey.OP_WRITE, clientContext);
                        clientContext.setStatus(ClientStatus.REGISTER);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(errorsOutputStream);
        }
    }
}
