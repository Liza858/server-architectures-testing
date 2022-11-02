package ru.ifmo.java.server_architectures_testing.application.logic;

import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;

public class TestCaseInfo {
    public final @NotNull ServerArchitectureType serverArchitectureType;
    public final int arraySize;
    public final int clientsCount;
    public final int timeDeltaBetweenRequests;
    public final int requestsCount;

    public TestCaseInfo(
            @NotNull ServerArchitectureType serverArchitectureType,
            int arraySize,
            int clientsCount,
            int timeDeltaBetweenRequests,
            int requestsCount
    ) {
        this.serverArchitectureType = serverArchitectureType;
        this.arraySize = arraySize;
        this.clientsCount = clientsCount;
        this.timeDeltaBetweenRequests = timeDeltaBetweenRequests;
        this.requestsCount = requestsCount;
    }
}
