package ru.ifmo.java.server_architectures_testing.server;

import ru.ifmo.java.server_architectures_testing.server.logic.BubbleSort;

import java.util.ArrayList;
import java.util.List;

public class SortArrayTask implements Runnable {

    public final List<Integer> arrayToSort;
    private final ClientContext clientContext;
    private final long startProcessTime;

    public SortArrayTask(List<Integer> arrayToSort, long startProcessTime, ClientContext clientContext) {
        this.arrayToSort = arrayToSort;
        this.clientContext = clientContext;
        this.startProcessTime = startProcessTime;
    }

    @Override
    public void run() {
        long startTaskExecutionTime = System.nanoTime();
        BubbleSort bubbleSort = new BubbleSort(arrayToSort);
        ArrayList<Integer> sorted = bubbleSort.sort();
        long endTaskExecutionTime = System.nanoTime();
        long endProcessTime = System.nanoTime();
        clientContext.sendToWrite(
                sorted,
                endProcessTime - startProcessTime,
                endTaskExecutionTime - startTaskExecutionTime
        );
    }
}
