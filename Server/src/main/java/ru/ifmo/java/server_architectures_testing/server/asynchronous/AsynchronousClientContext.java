package ru.ifmo.java.server_architectures_testing.server.asynchronous;

import com.google.protobuf.InvalidProtocolBufferException;
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

    private final AsynchronousServerWriteHandler writeHandler;
    private final AsynchronousServerReadHeadHandler readHeadHandler;
    private final AsynchronousServerReadBodyHandler readBodyHandler;
    private final AsynchronousSocketChannel channel;
    private final ByteBuffer headBuffer = ByteBuffer.allocate(4);
    private ByteBuffer bodyBuffer;
    private volatile ByteBuffer writeBuffer;


    public AsynchronousClientContext(
            AsynchronousSocketChannel channel,
            ExecutorService tasksPool,
            PrintStream errorsOutputStream,
            AsynchronousServerWriteHandler writeHandler,
            AsynchronousServerReadHeadHandler readHeadHandler,
            AsynchronousServerReadBodyHandler readBodyHandler
    ) {
        super(tasksPool, errorsOutputStream);
        this.channel = channel;
        this.writeHandler = writeHandler;
        this.readHeadHandler = readHeadHandler;
        this.readBodyHandler = readBodyHandler;
    }

    public ByteBuffer getHeadBuffer() {
        return headBuffer;
    }

    public ByteBuffer getBodyBuffer(int size) {
        bodyBuffer = ByteBuffer.allocate(size);
        return bodyBuffer;
    }

    public ByteBuffer getBodyBuffer() {
        return bodyBuffer;
    }

    public ByteBuffer getWriteBuffer() {
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

    public AsynchronousSocketChannel getChannel() {
        return channel;
    }

    @Override
    public void sendToWrite(ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime) {
        updateTimeStatistics(clientProcessTime, taskExecutionTime);
        ResponseMessage responseMessage = new ResponseMessage(getSortResponse(sortedArray));
        int size = responseMessage.getHead().length + responseMessage.getBody().length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.put(responseMessage.getHead());
        byteBuffer.put(responseMessage.getBody());
        byteBuffer.flip();
        writeBuffer = byteBuffer;
        channel.write(writeBuffer, this, writeHandler);
    }

    public AsynchronousServerReadBodyHandler getReadBodyHandler() {
        return readBodyHandler;
    }

    public AsynchronousServerReadHeadHandler getReadHeadHandler() {
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
