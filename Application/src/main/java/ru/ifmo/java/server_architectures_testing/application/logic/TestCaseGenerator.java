package ru.ifmo.java.server_architectures_testing.application.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;

import java.util.function.Predicate;

public class TestCaseGenerator {

    private final @NotNull ServerArchitectureType serverArchitectureType;
    private final int requestsCount;

    private final @NotNull TestParam testParam;
    private final int endParamValue;
    private final int step;
    boolean hasNext;
    private int arraySize;
    private int clientsCount;
    private int timeDeltaBetweenRequests;

    public TestCaseGenerator(
            @NotNull ServerArchitectureType serverArchitectureType,
            int arraySize,
            int clientsCount,
            int timeDeltaBetweenRequests,
            int requestsCount,
            @NotNull TestParam testParam,
            int startParamValue,
            int endParamValue,
            int step
    ) {
        this.serverArchitectureType = serverArchitectureType;
        this.arraySize = testParam == TestParam.N ? startParamValue : arraySize;
        this.clientsCount = testParam == TestParam.M ? startParamValue : clientsCount;
        this.timeDeltaBetweenRequests = testParam == TestParam.delta ? startParamValue : timeDeltaBetweenRequests;
        this.requestsCount = requestsCount;
        this.testParam = testParam;
        this.endParamValue = endParamValue;
        this.step = step;
        this.hasNext = hasNext(paramCurrentValue -> paramCurrentValue <= endParamValue);
    }

    public boolean hasNext() {
        return hasNext;
    }

    public @Nullable TestCase next() {
        if (!hasNext()) return null;
        TestCase testCase = new TestCase(
                serverArchitectureType,
                arraySize,
                clientsCount,
                timeDeltaBetweenRequests,
                requestsCount
        );
        hasNext = hasNext(paramCurrentValue -> paramCurrentValue < endParamValue);
        switch (testParam) {
            case N:
                arraySize = Math.min(endParamValue, arraySize + step);
                break;
            case M:
                clientsCount = Math.min(endParamValue, clientsCount + step);
                break;
            case delta:
                timeDeltaBetweenRequests = Math.min(endParamValue, timeDeltaBetweenRequests + step);
        }
        return testCase;
    }

    private boolean hasNext(@NotNull Predicate<Integer> predicate) {
        switch (testParam) {
            case N:
                return predicate.test(arraySize);
            case M:
                return predicate.test(clientsCount);
            case delta:
                return predicate.test(timeDeltaBetweenRequests);
        }
        return false;
    }
}
