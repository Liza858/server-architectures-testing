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

    ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
    ExecutorService clientsExecutor = Executors.newCachedThreadPool();

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
        testSingleClient(1, type);
    }

    private void testMultipleClientsSingleRequest(ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        testMultipleClients(1, type);
    }

    private void testSingleClientMultipleRequest(ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        testSingleClient(10, type);
    }

    private void testMultipleClientsMultipleRequest(ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        testMultipleClients(10, type);
    }


    private void testSingleClient(int requestsCount, ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        Server server = Server.createServer(type, 10, System.err);
        if (server == null) {
            return;
        }
        serverExecutor.submit(server);
        Client client = new Client(
                "localhost",
                Util.getServerPort(type),
                System.err,
                1000,
                requestsCount,
                10
        );
        Future<?> task = clientsExecutor.submit(client);

        task.get();

        server.stop();
        clientsExecutor.shutdown();
        serverExecutor.shutdown();

        Assert.assertNotNull(client.getSortedArray());
        List<Integer> expected = client.getArrayToSort().stream().sorted().collect(Collectors.toList());
        Assert.assertEquals(expected, client.getSortedArray());

        serverExecutor = Executors.newSingleThreadExecutor();
        clientsExecutor = Executors.newCachedThreadPool();
    }

    private void testMultipleClients(int requestsCount, ServerArchitectureType type) throws IOException, ExecutionException, InterruptedException {
        Server server = Server.createServer(type, 10, System.err);
        if (server == null) {
            return;
        }
        serverExecutor.submit(server);
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
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
        }

        serverExecutor = Executors.newSingleThreadExecutor();
        clientsExecutor = Executors.newCachedThreadPool();
    }
}
