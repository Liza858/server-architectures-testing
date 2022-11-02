package ru.ifmo.java.server_architectures_testing;

public class Util {

    private Util() {
    }

    public static int getServerPort(ServerArchitectureType type) {
        switch (type) {
            case BLOCKING:
                return Constants.BLOCKING_SERVER_PORT;
            case NON_BLOCKING:
                return Constants.NON_BLOCKING_SERVER_PORT;
            case ASYNCHRONOUS:
                return Constants.ASYNCHRONOUS_SERVER_PORT;
        }
        return 8080;
    }
}
