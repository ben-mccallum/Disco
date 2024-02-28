package uk.ac.strath;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private boolean running;
    private List<ClientConnection> connections;
    private ExecutorService pool;
    private ServerSocket server;

    public Server() {
        running = true;
        connections = new ArrayList<>();
        pool = Executors.newCachedThreadPool();

        try {
            server = new ServerSocket(App.PORT);
        } catch (IOException e) {
            running = false;
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                Socket client = server.accept();
                ClientConnection cc = new ClientConnection(client);

                connections.add(cc);
                pool.execute(cc);
            }
        } catch (IOException e) {
            stop();
        }
    }

    public void broadcast(String message) {
        for (ClientConnection cc : connections) {
            if (cc != null) {
                cc.sendMessage(message);
            }
        }
    }

    public void stop() {
        try {
            running = false;

            if (!server.isClosed()) {
                server.close();
            }

            for (ClientConnection cc : connections) {
                if (cc != null) {
                    cc.stop();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRunning() {
        return running;
    }
}
