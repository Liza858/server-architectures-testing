package ru.ifmo.java.server_architectures_testing.server;

import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ClientContext {

    private final ExecutorService tasksPool;
    private final PrintStream errorsOutputStream;
    private final AtomicLong clientProcessTime = new AtomicLong(0);
    private final AtomicLong taskExecutionTime = new AtomicLong(0);
    private final AtomicLong requestsCount = new AtomicLong(0);


    public ClientContext(ExecutorService tasksPool, PrintStream errorsOutputStream) {
        this.tasksPool = tasksPool;
        this.errorsOutputStream = errorsOutputStream;
    }


    public void updateTimeStatistics(long clientProcessTime, long taskExecutionTime) {
        this.requestsCount.incrementAndGet();
        this.clientProcessTime.addAndGet(Math.round(clientProcessTime / 1000.0));
        this.taskExecutionTime.addAndGet(Math.round(taskExecutionTime / 1000.0));
    }

    public abstract void sendToWrite(ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime);

    public void processSortRequest(Protocol.SortRequest sortRequest) {
        long startProcessTime = System.nanoTime();
        List<Integer> array = sortRequest.getValueList();
        SortArrayTask task = new SortArrayTask(array, startProcessTime, this);
        tasksPool.submit(task);
    }

    public void error(Throwable ex) {
        ex.printStackTrace(errorsOutputStream);
    }

    public Protocol.SortResponse getSortResponse(ArrayList<Integer> arrayToWrite) {
        return Protocol.SortResponse
                .newBuilder()
                .setCount(arrayToWrite.size())
                .addAllValue(arrayToWrite)
                .build();
    }

    public abstract void closeConnection();

    public double getClientProcessTime() {
        return clientProcessTime.get() / 1000.0 / requestsCount.get();
    }

    public double getTaskExecutionTime() {
        return taskExecutionTime.get() / 1000.0 / requestsCount.get();
    }
}