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
    protected boolean indm;
    protected ClientConnection connectedTo;
    protected DirectMessage activeDM;

    public ClientConnection(Socket client, Server s) {
        this.client = client;
        this.serve = s;
        indm = false;

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
                            if (!indm) {
                                serve.broadcastMessage("MESSAGE " + user.getUsername() + " " + String.join(" ", args));
                            } else {
                                connectedTo.send("MESSAGE " + user.getUsername() + " " + String.join(" ", args));
                                send("MESSAGE " + user.getUsername() + " " + String.join(" ", args));
                            }
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
                                boolean inchat = false;
                                if(r.getID().equals(chat)){
                                    for (String username : r.getMembers()) {
                                        if (Objects.equals(user.getUsername(), username)) {
                                            send("NOTIFY You are already in this chat");
                                            inchat = true;
                                        }
                                    }
                                    if (!inchat){
                                        r.addMember(serve, this, user.getUsername());
                                    }
                                }else{
                                    App.getInstance().getServer().newChat(this, chat, user.getUsername());
                                }
                            }
                        }
                        send("NOTIFY Welcome to " + chat);
                        break;

                    case "DM":
                        String userDM = args.get(0);
                        if (Objects.equals(userDM, user.getUsername()) || indm){
                            send("NOTIFY You can't start a dm with yourself or when you are in a dm already");
                        } else {
                            boolean online = false;
                            ClientConnection c = null;
                            ClientConnection cc = null;
                            for (GroupChat gc : serve.getChats()) {
                                Integer index = 0;
                                for (String u : gc.getMembers()) {
                                    if (Objects.equals(userDM, u)) {
                                        online = true;
                                        List<ClientConnection> ccs = gc.getConnections();
                                        cc = ccs.get(index);
                                    }
                                    if (Objects.equals(user.getUsername(), u)) {
                                        List<ClientConnection> ccs = gc.getConnections();
                                        c = ccs.get(index);
                                    }
                                    index++;
                                }
                            }
                            if (!online) {
                                Integer index = 0;
                                for (String u : serve.getConnected()) {
                                    if (Objects.equals(userDM, u)) {
                                        online = true;
                                        List<ClientConnection> ccs = App.getInstance().getServer().getConnections();
                                        cc = ccs.get(index);
                                    }
                                    if (Objects.equals(user.getUsername(), u)) {
                                        List<ClientConnection> ccs = App.getInstance().getServer().getConnections();
                                        c = ccs.get(index);
                                    }
                                    index++;
                                }
                            }
                            if (!online) {
                                send("NOTIFY That user is not online!");
                                break;
                            } else {
                                send("You have entered a dm, waiting for invited user to accept...");
                                indm = true;
                                cc.send("NOTIFY " + user.getUsername() + " wants to start a dm with you, /yes to accept, /no to decline");
                                dmSetUp(user.getUsername(), userDM, c, cc);
                            }
                        }
                        break;

                    case "YES":
                        boolean waiting = false;
                        for (DirectMessage dm : serve.getActiveDMs()) {
                            if (Objects.equals(user.getUsername(), dm.getWaiting())) {
                                waiting = true;
                                indm = true;
                                activeDM = dm;
                                activeDM.acceptRequest(serve);
                            }
                        }
                        if (!waiting){
                            send("NOTIFY No one has currently requested a dm with you");
                        }
                        break;

                    case "NO":
                        boolean wait = false;
                        DirectMessage target = null;
                        for (DirectMessage dm : serve.getActiveDMs()) {
                            if (Objects.equals(user.getUsername(), dm.getWaiting())) {
                                wait = true;
                                target = dm;
                            }
                        }
                        if (!wait){
                            send("NOTIFY No one has currently requested a dm with you");
                        } else {
                            send("NOTIFY Request rejected");
                            target.rejectRequest(serve);
                            serve.activeDMs.remove(target);
                        }
                        break;

                    case "LOGOUT":
                        boolean ing = false;
                        GroupChat g = null;
                        if (!indm) {
                            for (GroupChat gc : serve.chatRooms) {
                                for (ClientConnection cc : gc.getConnections()) {
                                    if (Objects.equals(this, cc)){
                                        ing = true;
                                        g = gc;
                                        break;
                                    }
                                }
                            }
                            if (ing) {
                                g.getMembers().remove(user.getUsername());
                                g.getConnections().remove(this);
                            } else {
                                serve.connections.remove(this);
                                serve.connectedUsers.remove(user.getUsername());
                            }
                        } else {
                            send("NOTIFY Goodbye!");
                            if (activeDM.waiting == null) {
                                connectedTo.send("NOTIFY The user you were talking to has left this direct message, returning to main chat");
                                connectedTo.indm = false;
                                indm = false;
                                connectedTo.activeDM = null;
                                serve.connections.add(connectedTo);
                                serve.connectedUsers.add(activeDM.Members.get(1));
                                connectedTo.connectedTo = null;
                                connectedTo = null;
                                serve.activeDMs.remove(activeDM);
                                activeDM = null;
                            }
                            activeDM.waitingC.send("NOTIFY the user who wished to direct message you has logged out");
                            serve.activeDMs.remove(activeDM);
                            activeDM = null;
                        }
                        user = null;
                        send("NOTIFY You have been logged out!");
                        break;

                    case "LEAVE":
                        if (!indm) {
                            send("NOTIFY Leaving chat");
                            App.getInstance().getServer().leaveChat(this, user.getUsername());
                            send("NOTIFY Welcome back!");
                        } else {
                            send("NOTIFY You have left your direct message, returning to main chat");
                            connectedTo.send("NOTIFY The user you were talking to has left this direct message, returning to main chat");
                            connectedTo.indm = false;
                            indm = false;
                            connectedTo.activeDM = null;
                            serve.connections.add(connectedTo);
                            serve.connectedUsers.add(activeDM.Members.get(1));
                            serve.connections.add(connectedTo.connectedTo);
                            serve.connectedUsers.add(activeDM.Members.get(0));
                            connectedTo.connectedTo = null;
                            connectedTo = null;
                            serve.activeDMs.remove(activeDM);
                            activeDM = null;
                        }

                        break;

                    case "ONLINE":
                        List<String> onlineUsers = new ArrayList<>();
                        boolean ingc = false;
                        if (!indm) {
                            for (GroupChat gc : serve.chatRooms) {
                                for (ClientConnection cc : gc.getConnections()) {
                                    if (Objects.equals(this, cc)){
                                        ingc = true;
                                        onlineUsers = gc.getMembers();
                                        break;
                                    }
                                }
                            }
                            if (!ingc) {
                                onlineUsers = serve.connectedUsers;
                            }
                        } else {
                            onlineUsers.add(user.getUsername());
                            onlineUsers.add(connectedTo.user.getUsername());
                        }





                        send("NOTIFY " + String.join("", (String[]) onlineUsers.toArray()));

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

    public void dmSetUp(String user, String userWaiting, ClientConnection c, ClientConnection cc){
        for (GroupChat gc : serve.chatRooms) {
            gc.getConnections().removeIf(con -> con == c);
            gc.getMembers().removeIf(u -> Objects.equals(u, user));
        }
        for (DirectMessage d : serve.activeDMs) {
            d.getConnections().removeIf(con -> con == c);
            d.getMembers().removeIf(u -> Objects.equals(u, user));
        }
        serve.connections.removeIf(con -> con == c);
        serve.connectedUsers.removeIf(u -> Objects.equals(u, user));
        activeDM = new DirectMessage(user, userWaiting, c, cc);
        serve.activeDMs.add(activeDM);
    }
}
