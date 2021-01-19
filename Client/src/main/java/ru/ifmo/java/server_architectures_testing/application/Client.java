package ru.ifmo.java.server_architectures_testing.application;

import ru.ifmo.java.server_architectures_testing.Constants;
import ru.ifmo.java.server_architectures_testing.RequestMessage;
import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable {

    private final List<Integer> arrayToSort;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final int requestsCount;
    private final int timeDeltaBetweenRequests;
    private final PrintStream errorsOutputStream;
    private final Socket socket;
    private double requestAverageTime = 0;
    private List<Integer> sortedArray = null;

    public Client(
            String serverIp,
            int serverPort,
            OutputStream errorsOutputStream,
            int arraySize,
            int requestsCount,
            int timeDeltaBetweenRequests
    ) throws IOException {
        this.arrayToSort = new ArrayList<>();
        initArrayToSort(arraySize);
        this.errorsOutputStream = new PrintStream(errorsOutputStream);
        this.requestsCount = requestsCount;
        this.timeDeltaBetweenRequests = timeDeltaBetweenRequests;
        socket = new Socket(serverIp, serverPort);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public static void main(String[] args) throws IOException {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            clients.add(
                    new Client(
                            "localhost",
                            Constants.BLOCKING_SERVER_PORT,
                            System.err,
                            100,
                            100,
                            0
                    )
            );
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Client client : clients) {
            executorService.submit(client);
        }
    }

    private void initArrayToSort(int size) {
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            arrayToSort.add(random.nextInt(1000));
        }
    }

    @Override
    public void run() {
        try {
            long startTime = System.nanoTime();

            for (int i = 0; i < requestsCount; i++) {
                sendSortRequest();
                Protocol.SortResponse response = receiveSortResponse();
                if (response == null) {
                    errorsOutputStream.println("error response!");
                    break;
                }
                sortedArray = response.getValueList();
                Thread.sleep(timeDeltaBetweenRequests);
            }

            long endTime = System.nanoTime();
            requestAverageTime = (endTime - startTime) / 1000000.0 / requestsCount;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(errorsOutputStream);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(errorsOutputStream);
            }
        }
    }

    private void sendSortRequest() throws IOException {
        RequestMessage message = new RequestMessage(getSortRequest());
        outputStream.write(message.getHead());
        outputStream.write(message.getBody());
        outputStream.flush();
    }

    private Protocol.SortResponse receiveSortResponse() throws IOException {
        Integer size = readMessageSize();
        if (size == null) {
            return null;
        }
        return readResponseMessage(size);
    }

    private Integer readMessageSize() throws IOException {
        int intBytesSize = 4;
        byte[] head = new byte[intBytesSize];
        int bytes = inputStream.read(head);
        if (bytes != intBytesSize) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(4).put(head);
        buffer.flip();
        return buffer.getInt();
    }

    private Protocol.SortResponse readResponseMessage(Integer size) throws IOException {
        byte[] body = new byte[size];
        int bytes = inputStream.read(body);
        if (bytes != size) {
            return null;
        }
        return Protocol.SortResponse.newBuilder().mergeFrom(body).build();
    }

    private Protocol.SortRequest getSortRequest() {
        return Protocol.SortRequest
                .newBuilder()
                .setCount(arrayToSort.size())
                .addAllValue(arrayToSort)
                .build();
    }

    public List<Integer> getArrayToSort() {
        return arrayToSort;
    }

    public List<Integer> getSortedArray() {
        return sortedArray;
    }

    public double getRequestAverageTime() {
        return requestAverageTime;
    }
}
