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
    protected ExecutorService pool;
    protected ServerSocket server;
    protected Database database;
    protected IdleTime idleTime;
    protected Thread idleTimeThread;
    protected ArrayList<GroupChat> chatRooms;
    protected ArrayList<DirectMessage> activeDMs;
    private Thread timeThread;
    protected timeFinder tF;

    public Server() {
        running = true;
        connections = new ArrayList<>();
        pool = Executors.newCachedThreadPool();
        idleTime = new IdleTime(this);
        idleTimeThread = new Thread(idleTime);
        chatRooms = new ArrayList<>();
        activeDMs = new ArrayList<>();

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
        if (running) {
            idleTimeThread.start();
            System.out.println("Server started and awaiting connections.");
        } else {
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
        if (chatRooms.isEmpty() && activeDMs.isEmpty()) {
            timeFinder tF = new timeFinder(message);
            message = tF.getMsg();
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
                for (ClientConnection con: gc.getConnections()) {
                    if (Objects.equals(username, con.user.getUsername())) {
                        inchat = true;
                        timeFinder tF = new timeFinder(message);
                        message = tF.getMsg();
                        for (ClientConnection cc : gc.getConnections()) {
                            if (cc != null) {
                                cc.send(message);
                            }
                        }
                    }
                }
            }
            if (!inchat) {
                timeFinder tF = new timeFinder(message);
                message = tF.getMsg();
                for (ClientConnection cc : connections) {
                    if (cc != null) {
                        cc.send(message);
                    }
                }
            }
        }
    }

    private void broadcast(String message) {
        for (ClientConnection cc : connections){
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

    public void newChat(ClientConnection c, String chatName){
        removeConnections(c);
        GroupChat chat = new GroupChat(c, chatName);
        chatRooms.add(chat);
    }

    public void leaveChat(ClientConnection c){
        boolean inchat = false;
        for (GroupChat gc : chatRooms) {
            boolean remove = false;
            gc.getConnections().removeIf(cc -> cc == c);
            for (ClientConnection con: gc.getConnections()) {
                if(Objects.equals(con, c)){
                    inchat = true;
                    remove = true;
                }
            }
            if (remove){
                gc.getConnections().remove(c);
            }
        }
        if (inchat){
            connections.add(c);
        }
    }

    public ArrayList<GroupChat> getChats(){
        return chatRooms;
    }

    public ArrayList<DirectMessage> getActiveDMs(){
        return activeDMs;
    }

    public List<ClientConnection> getConnections(){
        return connections;
    }

    public void removeConnections(ClientConnection c){
        for (GroupChat gc : getChats()) {
            gc.getConnections().removeIf(cc -> cc == c);
        }
        for (DirectMessage dm : getActiveDMs()) {
            dm.getConnections().removeIf(cc -> cc == c);
        }
        connections.removeIf(cc -> cc == c);
    }

    public IdleTime getIdleTime() {
        return idleTime;
    }
}
