package ru.ifmo.java.server_architectures_testing.server;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class ClientContext {

    private final @NotNull ExecutorService tasksPool;
    private final @NotNull PrintStream errorsOutputStream;

    public ClientContext(
            @NotNull ExecutorService tasksPool,
            @NotNull PrintStream errorsOutputStream
    ) {
        this.tasksPool = tasksPool;
        this.errorsOutputStream = errorsOutputStream;
    }

    public abstract void sendToWrite(@NotNull ArrayList<Integer> sortedArray, long clientProcessTime, long taskExecutionTime);

    public abstract void closeConnection();

    public void processSortRequest(@NotNull Protocol.SortRequest sortRequest) {
        long startProcessTime = System.nanoTime();
        List<Integer> array = sortRequest.getValueList();
        SortArrayTask task = new SortArrayTask(array, startProcessTime, this);
        tasksPool.submit(task);
    }

    public void error(Throwable ex) {
        ex.printStackTrace(errorsOutputStream);
    }

    public void error(String message) {
        errorsOutputStream.println(message);
    }

    public Protocol.SortResponse createSortResponse(
            @NotNull ArrayList<Integer> arrayToWrite,
            long taskExecutionTime,
            long clientProcessTime
    ) {
        Protocol.SortResponse.MetaInfo metaInfo =
                Protocol.SortResponse.MetaInfo
                        .newBuilder()
                        .setTaskExecutionTime(taskExecutionTime)
                        .setClientProcessTime(clientProcessTime)
                        .build();
        return Protocol.SortResponse
                .newBuilder()
                .setCount(arrayToWrite.size())
                .addAllValue(arrayToWrite)
                .setMetaInfo(metaInfo)
                .build();
    }
}