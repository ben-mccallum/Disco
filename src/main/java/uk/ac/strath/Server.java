package uk.ac.strath;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private boolean running;
    private List<ClientConnection> connections;
    private ExecutorService pool;
    private ServerSocket server;
    private Database database;

    public Server() {
        running = true;
        connections = new ArrayList<>();
        pool = Executors.newCachedThreadPool();

        try {
            database = new Database("jdbc:mysql://localhost/chatapp", "root", "");
            server = new ServerSocket(App.PORT);
        } catch (IOException e) {
            System.out.println("Error binding to port " + App.PORT + "!");
            running = false;
        } catch (SQLException e) {
            System.out.println("Error connecting to database!");
            running = false;
        }
    }

    @Override
    public void run() {
        System.out.println("Server started and awaiting connections.");

        try {
            while (running) {
                Socket client = server.accept();
                ClientConnection cc = new ClientConnection(client);

                connections.add(cc);
                pool.execute(cc);
            }

            stop();
        } catch (IOException e) {
            stop();
        }
    }

    public void broadcast(String message) {
        for (ClientConnection cc : connections) {
            if (cc != null) {
                cc.send(message);
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

            System.out.println("Server stopped.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Database getDatabase() {
        return database;
    }

    public boolean isRunning() {
        return running;
    }
}
