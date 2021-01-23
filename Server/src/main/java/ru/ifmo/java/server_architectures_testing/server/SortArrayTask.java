package ru.ifmo.java.server_architectures_testing.server;

import ru.ifmo.java.server_architectures_testing.server.logic.BubbleSort;

import java.util.ArrayList;
import java.util.List;

public class SortArrayTask implements Runnable {

    public final ArrayList<Integer> arrayToSort;
    private final ClientContext clientContext;
    private final long startProcessTime;

    public SortArrayTask(List<Integer> arrayToSort, long startProcessTime, ClientContext clientContext) {
        this.arrayToSort = new ArrayList<>(arrayToSort);
        this.clientContext = clientContext;
        this.startProcessTime = startProcessTime;
    }

    @Override
    public void run() {
        long startTaskExecutionTime = System.nanoTime();
        BubbleSort.sort(arrayToSort);
        long endTaskExecutionTime = System.nanoTime();
        clientContext.sendToWrite(
                arrayToSort,
                endTaskExecutionTime - startProcessTime,
                endTaskExecutionTime - startTaskExecutionTime
        );
    }
}
