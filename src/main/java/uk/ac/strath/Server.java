package uk.ac.strath;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    protected boolean running;
    protected List<ClientConnection> connections;
    protected List<String> connectedUsers;
    protected ExecutorService pool;
    protected ServerSocket server;
    protected Database database;
    protected ArrayList<GroupChat> chatRooms;

    public Server() {
        running = true;
        connections = new ArrayList<>();
        connectedUsers = new ArrayList<>();
        pool = Executors.newCachedThreadPool();
        chatRooms = new ArrayList<>();

        try {
            database = new Database("jdbc:mysql://localhost/cgb21121", "cgb21121", "esh0CaijooQu");
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
        if(running){
            System.out.println("Server started and awaiting connections.");
        }else{
            stop();
            return;
        }
        try {
            while (running) {
                Socket client = server.accept();
                ClientConnection cc = new ClientConnection(client, this);

                connections.add(cc);
                pool.execute(cc);
            }

            stop();
        } catch (IOException e) {
            stop();
        }
    }

    public void broadcastMessage(String message) {
        if (chatRooms.isEmpty()) {
            for (ClientConnection cc : connections) {
                if (cc != null) {
                    cc.send(message);
                }
            }
        } else {
            String[] parts = message.split(" ");
            String username = parts[1];
            boolean inchat = false;
            for (GroupChat gc : chatRooms) {
                for (String user : gc.getMembers()) {
                    if (Objects.equals(user, username)) {
                        inchat = true;
                        for (ClientConnection cc : gc.getConnections()) {
                            if (cc != null) {
                                cc.send(message);
                            }
                        }
                    }
                }
            }
            if (!inchat) {
                for (ClientConnection cc : connections) {
                    if (cc != null) {
                        cc.send(message);
                    }
                }
            }
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

    public void newChat(ClientConnection c, String chatName, String user){
        for (GroupChat gc : chatRooms) {
            gc.getConnections().removeIf(cc -> cc == c);
        }
        connections.removeIf(cc -> cc == c);
        GroupChat chat = new GroupChat(c, chatName, user);
        chatRooms.add(chat);
    }

    public void leaveChat(ClientConnection c, String user){
        for (GroupChat gc : chatRooms) {
            gc.getConnections().removeIf(cc -> cc == c);
            gc.getMembers().removeIf(u -> Objects.equals(u, user));
        }
        connections.add(c);
    }

    public ArrayList<GroupChat> getChats(){
        return chatRooms;
    }

    public List<String> getConnected(){
    return connectedUsers;
}

    public void addConnectedUser(String User){
        connectedUsers.add(User);
    }

    public List<ClientConnection> getConnections(){
        return connections;
    }
}
