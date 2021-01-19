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

    private final AsynchronousSocketChannel channel;
    private final AsynchronousServerWriter writer;


    public AsynchronousClientContext(
            AsynchronousSocketChannel channel,
            ExecutorService tasksPool,
            PrintStream errorsOutputStream
    ) {
        super(tasksPool, errorsOutputStream);
        this.channel = channel;
        writer = new AsynchronousServerWriter(this);
    }

    public ByteBuffer getHeadBuffer() {
        return ByteBuffer.allocate(4);
    }

    public ByteBuffer getBodyBuffer(int size) {
        return ByteBuffer.allocate(size);
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
        writer.write(byteBuffer);
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
