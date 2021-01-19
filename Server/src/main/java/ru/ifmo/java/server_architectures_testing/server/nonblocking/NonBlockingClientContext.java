package ru.ifmo.java.server_architectures_testing.server.nonblocking;

import com.google.protobuf.InvalidProtocolBufferException;
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

    private final SocketChannel channel;
    private final ByteBuffer headBuffer;
    private final ConcurrentLinkedQueue<ByteBuffer> queueToWrite = new ConcurrentLinkedQueue<>();
    private final Selector writeSelector;
    private ByteBuffer bodyBuffer;
    private Integer bodySize = null;
    private volatile ClientStatus status = ClientStatus.NON_REGISTER;

    public NonBlockingClientContext(
            SocketChannel channel,
            Selector writeSelector,
            ExecutorService tasksPool,
            PrintStream errorsOutputStream
    ) {
        super(tasksPool, errorsOutputStream);
        this.channel = channel;
        int intByteSize = 4;
        headBuffer = ByteBuffer.allocate(intByteSize);
        bodyBuffer = ByteBuffer.allocate(1);
        this.writeSelector = writeSelector;
    }

    public ByteBuffer getBuffer() {
        if (bodySize == null && headBuffer.limit() - headBuffer.position() != 0) {
            return headBuffer;
        } else {
            if (bodySize == null) {
                headBuffer.flip();
                bodySize = headBuffer.getInt();
                bodyBuffer = ByteBuffer.allocate(bodySize);
            }
            return bodyBuffer;
        }
    }

    public boolean readAllMessage() {
        if (bodySize == null) {
            return false;
        }
        return bodyBuffer.limit() == bodyBuffer.position();
    }

    public Protocol.SortRequest getSortRequestMessage() {
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
    public void sendToWrite(ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime) {
        updateTimeStatistics(clientProcessTime, taskExecutionTime);
        ResponseMessage responseMessage = new ResponseMessage(getSortResponse(sortedArray));
        ByteBuffer byteBuffer = ByteBuffer.allocate(responseMessage.getHead().length + responseMessage.getBody().length);
        byteBuffer.put(responseMessage.getHead());
        byteBuffer.put(responseMessage.getBody());
        byteBuffer.flip();
        queueToWrite.add(byteBuffer);
        writeSelector.wakeup();
    }

    public ClientStatus getStatus() {
        if (status == ClientStatus.NON_REGISTER && !queueToWrite.isEmpty()) {
            status = ClientStatus.NEW;
        }
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public ConcurrentLinkedQueue<ByteBuffer> getQueueToWrite() {
        return queueToWrite;
    }

    public SocketChannel getChannel() {
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
