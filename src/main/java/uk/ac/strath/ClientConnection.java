package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

public class ClientConnection implements Runnable {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private User user;
    private Server serve;

    public ClientConnection(Socket client, Server s) {
        this.client = client;
        this.serve = s;

        try {
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String message;

        try {
            while ((message = in.readLine()) != null) {
                LinkedList<String> args = new LinkedList<>(Arrays.asList(message.split("\\s+")));
                String op = args.removeFirst();

                if (user == null && !op.equals("IDENTIFY") && !op.equals("SIGNUP")){
                    send("NOTIFY You are not logged in!");
                    continue;
                }

                switch (op) {
                    case "IDENTIFY":
                        if (args.size() < 2) {
                            send("NOTIFY Please provide a username and password!");
                            break;
                        }

                        User userLogin = serve.getDatabase().getUser(args.get(0));

                        if (userLogin == null) {
                            send("NOTIFY User does not exist!");
                            break;
                        }

                        if (args.get(1).equals(userLogin.getPassword())) {
                            user = userLogin;

                            send("HANDSHAKE " + userLogin.getUsername());
                            App.getInstance().getServer().addConnectedUser(args.getFirst());
                        } else {
                            send("NOTIFY Invalid username or password!");
                        }
                        break;

                    case "MESSAGE":
                        if (!args.isEmpty()) {
                            serve.broadcastMessage("MESSAGE " + user.getUsername() + " " + String.join(" ", args));
                        }
                        break;

                    case "SIGNUP":
                        if (args.size() < 2) {
                            send("NOTIFY Please provide a username and password!");
                            break;
                        }

                        if (serve.getDatabase().getUser(args.get(0)) != null) {
                            send("NOTIFY Username already taken!");
                            break;
                        }

                        send("NOTIFY Creating user " + args.get(0) +  "!");
                        serve.getDatabase().addUser(args.get(0), args.get(1));
                        App.getInstance().getServer().addConnectedUser(args.get(0));

                        break;

                    case "CHANNEL":
                        serve.broadcast("CHANNEL " +  String.join(" ", args));
                        String chat = args.get(0);
                        ArrayList<GroupChat> ChatRooms = App.getInstance().getServer().getChats();
                        if (ChatRooms.isEmpty()) {
                            App.getInstance().getServer().newChat(this, chat, user.getUsername());
                        } else {
                            boolean chatexists = false;
                            for(GroupChat r: ChatRooms){
                                if(r.getID().equals(chat)){
                                    r.addMember(serve, this, user.getUsername());
                                    chatexists = true;
                                }
                            }
                            if (!chatexists) {
                                //line referring to balls
                                App.getInstance().getServer().newChat(this, chat, user.getUsername());
                            }
                        }
                        send("NOTIFY Welcome to " + chat);
                        break;

                    case "DM":
                        String userDM = args.get(0);
                        boolean online = false;
                        ClientConnection cc = null;
                        for (GroupChat gc : serve.getChats()){
                            Integer index = 0;
                            for (String u : gc.getMembers()){
                                if (Objects.equals(userDM, u)) {
                                    online = true;
                                    List<ClientConnection> ccs = gc.getConnections();
                                    cc = ccs.get(index);
                                    break;
                                }
                                index ++;
                            }
                        }
                        if (!online) {
                            Integer index = 0;
                            for (String u : serve.getConnected()) {
                                if (Objects.equals(userDM, u)) {
                                    online = true;
                                    List<ClientConnection> ccs = App.getInstance().getServer().getConnections();
                                    cc = ccs.get(index);
                                    break;
                                }
                                index ++;
                            }
                        }
                        if (!online) {
                            send("NOTIFY That user is not online!");
                            break;
                        } else {
                            send("Waiting for User to accept...");
                            cc.send("NOTIFY " + user.getUsername() + "wants to start a dm with you, /yes to accept, /no to decline");
                        }
                        break;



                    case "LOGOUT":
                        user = null;
                        send("NOTIFY You have been logged out!");
                        break;

                    case "LEAVE":
                        send("NOTIFY Leaving chat");
                        App.getInstance().getServer().leaveChat(this, user.getUsername());
                        send("NOTIFY Welcome back!");

                        break;

                    case "ONLINE":
                        ArrayList<String> onlineUsers = new ArrayList<>();
                        for (GroupChat gc : serve.getChats()){
                            for (String u : gc.getMembers()){
                                onlineUsers.add(u);
                            }
                        }
                        send("NOTIFY Online users: " + onlineUsers);

                    default:
                        break;
                }
            }
        } catch (IOException | SQLException e) {
            stop();
        }
    }

    public void send(String message) {
        out.println(message);
    }

    public void stop() {
        try {
            in.close();
            out.close();

            if (client.isConnected()) {
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
