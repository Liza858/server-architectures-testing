import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.java.server_architectures_testing.ServerArchitectureType;
import ru.ifmo.java.server_architectures_testing.Util;
import ru.ifmo.java.server_architectures_testing.application.Client;
import ru.ifmo.java.server_architectures_testing.server.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ClientServerTests {

    @Test
    public void testBlockingServer() throws InterruptedException, ExecutionException, IOException {
        testSingleClientSingleRequest(ServerArchitectureType.BLOCKING);
        testMultipleClientsSingleRequest(ServerArchitectureType.BLOCKING);
        testSingleClientMultipleRequest(ServerArchitectureType.BLOCKING);
        testMultipleClientsMultipleRequest(ServerArchitectureType.BLOCKING);
    }

    @Test
    public void testNonBlockingServer() throws InterruptedException, ExecutionException, IOException {
        testSingleClientSingleRequest(ServerArchitectureType.NON_BLOCKING);
        testMultipleClientsSingleRequest(ServerArchitectureType.NON_BLOCKING);
        testSingleClientMultipleRequest(ServerArchitectureType.NON_BLOCKING);
        testMultipleClientsMultipleRequest(ServerArchitectureType.NON_BLOCKING);
    }

    @Test
    public void testAsynchronousServer() throws InterruptedException, ExecutionException, IOException {
        testSingleClientSingleRequest(ServerArchitectureType.ASYNCHRONOUS);
        testMultipleClientsSingleRequest(ServerArchitectureType.ASYNCHRONOUS);
        testSingleClientMultipleRequest(ServerArchitectureType.ASYNCHRONOUS);
        testMultipleClientsMultipleRequest(ServerArchitectureType.ASYNCHRONOUS);
    }

    private void testSingleClientSingleRequest(ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        test(1, 1, type);
    }

    private void testMultipleClientsSingleRequest(ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        test(10, 1, type);
    }

    private void testSingleClientMultipleRequest(ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        test(1, 10, type);
    }

    private void testMultipleClientsMultipleRequest(ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        test(10, 10, type);
    }

    private void test(int clientsCount, int requestsCount, ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
        ExecutorService clientsExecutor = Executors.newFixedThreadPool(clientsCount);

        Server server = Server.createServer(type, 10, System.err);
        if (server == null) {
            return;
        }
        serverExecutor.submit(server);
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < clientsCount; i++) {
            Client client = new Client(
                    "localhost",
                    Util.getServerPort(type),
                    System.err,
                    1000,
                    requestsCount,
                    10
            );
            clients.add(client);
        }

        List<Future<?>> tasks = new ArrayList<>();
        for (Client client : clients) {
            tasks.add(clientsExecutor.submit(client));
        }

        for (Future<?> task : tasks) {
            task.get();
        }

        server.stop();
        clientsExecutor.shutdown();
        serverExecutor.shutdown();

        for (Client client : clients) {
            Assert.assertNotNull(client.getSortedArray());
            List<Integer> expected = client.getArrayToSort().stream().sorted().collect(Collectors.toList());
            Assert.assertEquals(expected, client.getSortedArray());
            Assert.assertTrue(client.getRequestAverageTimeUs() >= client.getClientProcessAverageTimeUs());
            Assert.assertTrue(client.getClientProcessAverageTimeUs() >= client.getTaskExecutionAverageTimeUs());
        }
    }
}
