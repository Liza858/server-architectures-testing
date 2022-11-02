package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ifmo.java.server_architectures_testing.ResponseMessage;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;
import ru.ifmo.java.server_architectures_testing.server.ClientContext;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class NonBlockingClientContext extends ClientContext {

    private final @NotNull SocketChannel channel;
    private final @NotNull ConcurrentLinkedQueue<ByteBuffer> queueToWrite = new ConcurrentLinkedQueue<>();
    private final @NotNull Selector writeSelector;
    private final @NotNull ByteBuffer headBuffer;
    private @NotNull ByteBuffer bodyBuffer;
    private @Nullable Integer bodySize = null;
    private volatile @NotNull ClientStatus status = ClientStatus.NON_REGISTER;

    public NonBlockingClientContext(
            @NotNull SocketChannel channel,
            @NotNull Selector writeSelector,
            @NotNull ExecutorService tasksPool,
            @NotNull PrintStream errorsOutputStream
    ) {
        super(tasksPool, errorsOutputStream);
        this.channel = channel;
        headBuffer = ByteBuffer.allocate(Integer.BYTES);
        bodyBuffer = ByteBuffer.allocate(0);
        this.writeSelector = writeSelector;
    }

    public @NotNull ByteBuffer getBuffer() {
        if (bodySize == null && headBuffer.hasRemaining()) {
            return headBuffer;
        } else if (bodySize == null) {
            headBuffer.flip();
            bodySize = headBuffer.getInt();
            bodyBuffer = ByteBuffer.allocate(bodySize);
        }
        return bodyBuffer;
    }

    public boolean readAllMessage() {
        if (bodySize == null) {
            return false;
        }
        return bodyBuffer.limit() == bodyBuffer.position();
    }

    public @Nullable Protocol.SortRequest getSortRequestMessage() {
        try {
            bodyBuffer.flip();
            return Protocol.SortRequest.newBuilder().mergeFrom(bodyBuffer.array()).build();
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    public void clearBuffers() {
        headBuffer.clear();
        bodyBuffer.clear();
        bodySize = null;
    }

    @Override
    public void sendToWrite(@NotNull ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime) {
        ResponseMessage responseMessage = new ResponseMessage(
                createSortResponse(sortedArray, taskExecutionTime, clientProcessTime)
        );
        ByteBuffer byteBuffer = ByteBuffer.allocate(responseMessage.getHead().length + responseMessage.getBody().length);
        byteBuffer.put(responseMessage.getHead());
        byteBuffer.put(responseMessage.getBody());
        byteBuffer.flip();
        queueToWrite.add(byteBuffer);
        writeSelector.wakeup();
    }

    public @NotNull ClientStatus getStatus() {
        if (status == ClientStatus.NON_REGISTER && !queueToWrite.isEmpty()) {
            status = ClientStatus.NEW;
        }
        return status;
    }

    public void setStatus(@NotNull ClientStatus status) {
        this.status = status;
    }

    public @NotNull ConcurrentLinkedQueue<ByteBuffer> getQueueToWrite() {
        return queueToWrite;
    }

    public @NotNull SocketChannel getChannel() {
        return channel;
    }

    public void closeConnection() {
        if (channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException e) {
                error(e);
            }
        }
    }
}
