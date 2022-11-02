package ru.ifmo.java.server_architectures_testing.server.logic;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BubbleSort {

    private BubbleSort() {
    }

    public static void sort(@NotNull ArrayList<Integer> arrayToSort) {
        for (int i = 0; i < arrayToSort.size(); i++) {
            for (int j = 0; j < arrayToSort.size() - i - 1; j++) {
                if (arrayToSort.get(j) > arrayToSort.get(j + 1)) {
                    int tmp = arrayToSort.get(j);
                    arrayToSort.set(j, arrayToSort.get(j + 1));
                    arrayToSort.set(j + 1, tmp);
                }
            }
        }
    }
}
