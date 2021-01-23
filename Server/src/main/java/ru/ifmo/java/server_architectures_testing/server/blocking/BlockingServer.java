package ru.ifmo.java.server_architectures_testing.server.blocking;

import ru.ifmo.java.server_architectures_testing.Constants;
import ru.ifmo.java.server_architectures_testing.server.ClientContext;
import ru.ifmo.java.server_architectures_testing.server.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingServer extends Server {

    private final ExecutorService readPool = Executors.newCachedThreadPool();
    private final ExecutorService tasksPool;
    private final PrintStream errorsOutputStream;
    private final ServerSocket serverSocket;

    public BlockingServer(int tasksThreadsNumber, OutputStream errorsOutputStream) throws IOException {
        tasksPool = Executors.newFixedThreadPool(tasksThreadsNumber);
        this.errorsOutputStream = new PrintStream(errorsOutputStream);
        serverSocket = new ServerSocket(Constants.BLOCKING_SERVER_PORT);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                BlockingClientContext clientContext = new BlockingClientContext(socket, tasksPool, errorsOutputStream);
                clients.add(clientContext);
                readPool.submit(new BlockingServerReader(clientContext));
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace(errorsOutputStream);
            }
        }
    }

    @Override
    public void stop() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
        tasksPool.shutdown();
        readPool.shutdown();
        for (ClientContext clientContext : clients) {
            clientContext.closeConnection();
        }
        isAlive = false;
    }
}
