package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.ResponseMessage;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;
import ru.ifmo.java.server_architectures_testing.server.ClientContext;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AsynchronousClientContext extends ClientContext {

    private final @NotNull AsynchronousServerWriteHandler writeHandler;
    private final @NotNull AsynchronousServerReadHeadHandler readHeadHandler;
    private final @NotNull AsynchronousServerReadBodyHandler readBodyHandler;
    private final @NotNull AsynchronousSocketChannel channel;
    private final @NotNull ByteBuffer headBuffer = ByteBuffer.allocate(Integer.BYTES);
    private @NotNull ByteBuffer bodyBuffer = ByteBuffer.allocate(0);
    private volatile @NotNull ByteBuffer writeBuffer = ByteBuffer.allocate(0);

    public AsynchronousClientContext(
            @NotNull AsynchronousSocketChannel channel,
            @NotNull ExecutorService tasksPool,
            @NotNull PrintStream errorsOutputStream,
            @NotNull AsynchronousServerWriteHandler writeHandler,
            @NotNull AsynchronousServerReadHeadHandler readHeadHandler,
            @NotNull AsynchronousServerReadBodyHandler readBodyHandler
    ) {
        super(tasksPool, errorsOutputStream);
        this.channel = channel;
        this.writeHandler = writeHandler;
        this.readHeadHandler = readHeadHandler;
        this.readBodyHandler = readBodyHandler;
    }

    public @NotNull ByteBuffer getHeadBuffer() {
        return headBuffer;
    }

    public void allocateBodyBuffer(int size) {
        bodyBuffer = ByteBuffer.allocate(size);
    }

    public @NotNull ByteBuffer getBodyBuffer() {
        return bodyBuffer;
    }

    public @NotNull ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public Protocol.SortRequest getSortRequestMessage(ByteBuffer buffer) {
        try {
            buffer.flip();
            return Protocol.SortRequest.newBuilder().mergeFrom(buffer.array()).build();
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    public @NotNull AsynchronousSocketChannel getChannel() {
        return channel;
    }

    @Override
    public void sendToWrite(@NotNull ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime) {
        ResponseMessage responseMessage = new ResponseMessage(
                createSortResponse(sortedArray, taskExecutionTime, clientProcessTime)
        );
        int size = responseMessage.getHead().length + responseMessage.getBody().length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.put(responseMessage.getHead());
        byteBuffer.put(responseMessage.getBody());
        byteBuffer.flip();
        writeBuffer = byteBuffer;
        channel.write(writeBuffer, this, writeHandler);
    }

    public @NotNull AsynchronousServerReadBodyHandler getReadBodyHandler() {
        return readBodyHandler;
    }

    public @NotNull AsynchronousServerReadHeadHandler getReadHeadHandler() {
        return readHeadHandler;
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
