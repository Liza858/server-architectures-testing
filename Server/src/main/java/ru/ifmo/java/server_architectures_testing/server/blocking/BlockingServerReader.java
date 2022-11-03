package ru.ifmo.java.server_architectures_testing.server.blocking;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;

public class BlockingServerReader implements Runnable {

    private final @NotNull BlockingClientContext clientContext;
    private final @NotNull InputStream inputStream;
    private final @NotNull Set<BlockingClientContext> allClients;

    public BlockingServerReader(@NotNull BlockingClientContext clientContext, @NotNull Set<BlockingClientContext> allClients) throws IOException {
        this.clientContext = clientContext;
        this.inputStream = clientContext.getInputStream();
        this.allClients = allClients;
    }

    @Override
    public void run() {
        try {
            while (!clientContext.connectionIsClosed() && !Thread.interrupted()) {
                Protocol.SortRequest request = receiveSortRequest();
                if (request != null) {
                    clientContext.processSortRequest(request);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            clientContext.error(e);
        } finally {
            clientContext.closeConnection();
            allClients.remove(clientContext);
        }
    }

    private Protocol.SortRequest receiveSortRequest() throws IOException {
        Integer size = readMessageSize();
        if (size == null) {
            return null;
        }
        return readRequestMessage(size);
    }

    private byte[] readBytes(int size) throws IOException {
        byte[] buffer = new byte[size];
        int bytes = 0;
        while (bytes != size) {
            int readResult = inputStream.read(buffer, bytes, size - bytes);
            if (readResult == -1) {
                return null;
            }
            bytes += readResult;
        }
        return buffer;
    }

    private Integer readMessageSize() throws IOException {
        byte[] head = readBytes(Integer.BYTES);
        if (head == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).put(head);
        buffer.flip();
        return buffer.getInt();
    }

    private Protocol.SortRequest readRequestMessage(Integer size) throws IOException {
        byte[] body = readBytes(size);
        if (body == null) {
            return null;
        }
        return Protocol.SortRequest.newBuilder().mergeFrom(body).build();
    }
}
