package ru.ifmo.java.server_architectures_testing.server.logic;

import java.util.ArrayList;
import java.util.List;

public class BubbleSort {

    private final List<Integer> arrayToSort;

    public BubbleSort(List<Integer> arrayToSort) {
        this.arrayToSort = arrayToSort;
    }

    public ArrayList<Integer> sort() {
        ArrayList<Integer> array = new ArrayList<>(arrayToSort);
        for (int i = 0; i < array.size(); i++) {
            for (int j = 0; j < array.size() - i - 1; j++) {
                if (array.get(j) > array.get(j + 1)) {
                    int tmp = array.get(j);
                    array.set(j, array.get(j + 1));
                    array.set(j + 1, tmp);
                }
            }
        }
        return array;
    }
}
