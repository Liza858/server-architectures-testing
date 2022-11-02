package ru.ifmo.java.server_architectures_testing.application;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.concurrent.*;

public class Client implements Runnable {

    private final @Nullable CyclicBarrier barrier;
    private final @NotNull List<Integer> arrayToSort;
    private final @NotNull InputStream inputStream;
    private final @NotNull OutputStream outputStream;
    private final int requestsCount;
    private final int timeDeltaBetweenRequests;
    private final @NotNull PrintStream errorsOutputStream;
    private final @NotNull Socket socket;
    private double requestAverageTimeUs = 0;
    private long taskExecutionSumTimeUs = 0;
    private long clientProcessSumTimeUs = 0;
    private @Nullable List<Integer> sortedArray = null;

    public Client(
            @NotNull String serverIp,
            int serverPort,
            @NotNull OutputStream errorsOutputStream,
            int arraySize,
            int requestsCount,
            int timeDeltaBetweenRequests
    ) throws IOException {
        this(serverIp, serverPort, errorsOutputStream, arraySize,
                requestsCount, timeDeltaBetweenRequests, null);
    }

    public Client(
            @NotNull String serverIp,
            int serverPort,
            @NotNull OutputStream errorsOutputStream,
            int arraySize,
            int requestsCount,
            int timeDeltaBetweenRequests,
            @Nullable CyclicBarrier barrier
    ) throws IOException {
        this.arrayToSort = new ArrayList<>();
        initArrayToSort(arraySize);
        this.errorsOutputStream = new PrintStream(errorsOutputStream);
        this.requestsCount = requestsCount;
        this.timeDeltaBetweenRequests = timeDeltaBetweenRequests;
        this.barrier = barrier;
        socket = new Socket(serverIp, serverPort);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    // simple example
    // you must start the server before that
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // create clients
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

        // run all
        List<Future<?>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Client client : clients) {
            futures.add(executorService.submit(client));
        }

        // wait until the end of the work
        for (Future<?> future : futures) {
            future.get();
        }

        // print requestAverageTime
        for (Client client : clients) {
            System.out.println(client.getRequestAverageTimeUs());
        }
    }

    private static void barrierWait(@NotNull CyclicBarrier barrier) throws InterruptedException {
        try {
            barrier.await();
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
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
            if (barrier != null) {
                barrierWait(barrier);
            }

            long startTime = System.nanoTime();

            for (int i = 0; i < requestsCount; i++) {
                sendSortRequest();
                Protocol.SortResponse response = receiveSortResponse();
                if (response == null) {
                    errorsOutputStream.println("error! response is null!");
                    break;
                }
                updateResults(response);
                Thread.sleep(timeDeltaBetweenRequests);
            }

            long endTime = System.nanoTime();
            requestAverageTimeUs = calculateRequestAverageTime(startTime, endTime);
        } catch (InterruptedException e) {
            errorsOutputStream.println("the client was interrupted!");
        } catch (Exception e) {
            e.printStackTrace(errorsOutputStream);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(errorsOutputStream);
            }
        }
    }

    private void updateResults(Protocol.SortResponse response) {
        sortedArray = response.getValueList();
        Protocol.SortResponse.MetaInfo info = response.getMetaInfo();
        taskExecutionSumTimeUs += TimeUnit.MICROSECONDS.convert(info.getTaskExecutionTime(), TimeUnit.NANOSECONDS);
        clientProcessSumTimeUs += TimeUnit.MICROSECONDS.convert(info.getClientProcessTime(), TimeUnit.NANOSECONDS);
    }

    private double calculateRequestAverageTime(long startTimeNs, long endTimeNs) {
        long timeInUs = TimeUnit.MICROSECONDS.convert(endTimeNs - startTimeNs, TimeUnit.NANOSECONDS);
        return ((double) timeInUs) / requestsCount;
    }

    private void sendSortRequest() throws IOException {
        RequestMessage message = new RequestMessage(getSortRequest());
        outputStream.write(message.getHead());
        outputStream.write(message.getBody());
        outputStream.flush();
    }

    private @Nullable Protocol.SortResponse receiveSortResponse() throws IOException {
        Integer size = readMessageSize();
        if (size == null) {
            return null;
        }
        return readResponseMessage(size);
    }

    private byte[] readBytes(int size) throws IOException {
        byte[] buffer = new byte[size];
        int bytes = 0;
        while (bytes != size) {
            int readResult = inputStream.read(buffer, bytes, size - bytes);
            if (readResult == -1) {
                return null;
            }
            bytes += readResult;
        }
        return buffer;
    }

    private Integer readMessageSize() throws IOException {
        byte[] head = readBytes(Integer.BYTES);
        if (head == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).put(head);
        buffer.flip();
        return buffer.getInt();
    }


    private Protocol.SortResponse readResponseMessage(Integer size) throws IOException {
        byte[] body = readBytes(size);
        if (body == null) {
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

    public @NotNull List<Integer> getArrayToSort() {
        return arrayToSort;
    }

    public @Nullable List<Integer> getSortedArray() {
        return sortedArray;
    }

    public double getRequestAverageTimeUs() {
        return requestAverageTimeUs;
    }

    public double getTaskExecutionAverageTimeUs() {
        return ((double) taskExecutionSumTimeUs) / requestsCount;
    }

    public double getClientProcessAverageTimeUs() {
        return ((double) taskExecutionSumTimeUs) / requestsCount;
    }
}
