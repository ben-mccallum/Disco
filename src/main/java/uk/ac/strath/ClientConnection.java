package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

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

                        break;

                    case "CHANNEL":
                        serve.broadcast("CHANNEL " +  String.join(" ", args));
                        String chat = args.get(0);
                        ArrayList<GroupChat> ChatRooms = App.getInstance().getServer().getChats();
                        if (ChatRooms.isEmpty()) {
                            App.getInstance().getServer().newChat(this, chat, user.getUsername());
                        } else {
                            for(GroupChat r: ChatRooms){
                                if(r.getID().equals(chat)){
                                    System.out.println(user.getUsername());
                                    r.addMember(serve, this, user.getUsername());
                                }else{
                                    App.getInstance().getServer().newChat(this, chat, user.getUsername());
                                }
                            }
                        }
                        send("NOTIFY Welcome to " + chat);
                        break;


                    case "LOGOUT":
                        user = null;
                        send("NOTIFY You have been logged out!");
                        break;

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
