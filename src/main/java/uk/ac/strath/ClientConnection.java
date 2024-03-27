package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.*;



public class ClientConnection implements Runnable {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    protected User user;
    private final Server serve;
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

                if (user == null && !op.equals("IDENTIFY") && !op.equals("SIGNUP") && !op.equals("ONLINE")){
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
                            serve.getIdleTime().setLastMessage(user.getUsername(), new Date().getTime());

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
                        String chat = args.get(0);
                        ArrayList<GroupChat> ChatRooms = App.getInstance().getServer().getChats();
                        if (ChatRooms.isEmpty()) {
                            serve.newChat(this, chat);
                        } else {
                            for(GroupChat gc : ChatRooms){
                                boolean inchat = false;
                                if(gc.getID().equals(chat)){
                                    for (ClientConnection con : gc.getConnections()) {
                                        if (Objects.equals(user.getUsername(), con.user.getUsername())) {
                                            send("NOTIFY You are already in this chat");
                                            inchat = true;
                                        }
                                    }
                                    if (!inchat){
                                        gc.addMember(serve, this);
                                    }
                                }else{
                                    boolean inch = false;
                                    GroupChat group = null;
                                    for (GroupChat g : ChatRooms) {
                                        for (ClientConnection con : g.getConnections()) {
                                            if (Objects.equals(user.getUsername(), con.user.getUsername())) {
                                                inch = true;
                                                group = g;
                                            }
                                        }
                                    }
                                    if (inch){
                                        group.getConnections().remove(this);
                                    }
                                    serve.newChat(this, chat);
                                }
                            }
                        }
                        send("NOTIFY Welcome to " + chat);
                        break;

                    case "DM":
                        String userDM = args.getFirst();
                        if (Objects.equals(userDM, user.getUsername()) || indm){
                            send("NOTIFY You can't start a dm with yourself or when you are in a dm already");
                        } else {
                            boolean online = false;
                            ClientConnection cc = null;
                            for (GroupChat gc : serve.getChats()) {
                                for (ClientConnection con : gc.getConnections()) {
                                    if (Objects.equals(userDM, con.user.getUsername())) {
                                        online = true;
                                        cc = con;
                                        break;
                                    }
                                }
                            }
                            if (!online) {
                                for (ClientConnection con : serve.connections) {
                                    if (Objects.equals(userDM, con.user.getUsername())) {
                                        online = true;
                                        cc = con;
                                    }
                                }
                            }
                            if (!online) {
                                send("NOTIFY That user is not online!");
                                break;
                            } else {
                                send("NOTIFY You have entered a dm, waiting for invited user to accept...");
                                indm = true;
                                cc.send("NOTIFY " + user.getUsername() + " wants to start a dm with you, /yes to accept, /no to decline");
                                dmSetUp(user.getUsername(), this, cc);
                            }
                        }
                        break;

                    case "YES":
                        boolean waiting = false;
                        for (DirectMessage dm : serve.getActiveDMs()) {
                            if (Objects.equals(user.getUsername(), dm.waitingC.user.getUsername())) {
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
                            if (Objects.equals(user.getUsername(), dm.waitingC.user.getUsername())) {
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
                                g.getConnections().remove(this);
                            } else {
                                serve.connections.remove(this);
                            }
                        } else {
                            send("NOTIFY Goodbye!");
                            if (activeDM.waitingC == null) {
                                connectedTo.send("NOTIFY The user you were talking to has left this direct message, returning to main chat");
                                connectedTo.indm = false;
                                indm = false;
                                connectedTo.activeDM = null;
                                serve.connections.add(connectedTo);
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
                        serve.connections.remove(this);
                        break;

                    case "LEAVE":
                        if (!indm) {
                            send("NOTIFY Leaving chat");
                            serve.leaveChat(this);
                            send("NOTIFY Welcome back!");
                        } else {
                            send("NOTIFY You have left your direct message, returning to main chat");
                            connectedTo.send("NOTIFY The user you were talking to has left this direct message, returning to main chat");
                            connectedTo.indm = false;
                            indm = false;
                            connectedTo.activeDM = null;
                            serve.connections.add(connectedTo);
                            serve.connections.add(connectedTo.connectedTo);
                            connectedTo.connectedTo = null;
                            connectedTo = null;
                            serve.activeDMs.remove(activeDM);
                            activeDM = null;
                        }

                        break;

                    case "TARDIS":
                        SafeCounter safeCounter = new SafeCounter();
                        Runnable safeTask = () -> {
                            for (int i = 0; i < 1012; i++) {
                                safeCounter.increment();
                            }
                        };
                        Thread thread1 = new Thread(safeTask);
                        Thread thread2 = new Thread(safeTask);
                        thread1.start();
                        thread2.start();
                        try {
                            thread1.join();
                            thread2.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        send("NOTIFY You've returned to: " + safeCounter.getCount());
                        break;

                    case "BADTARDIS":
                        UnsafeCounter unsafeCounter = new UnsafeCounter();
                        Runnable unsafeTask = () -> {
                            for (int i = 0; i < 1012; i++) {
                                unsafeCounter.increment();
                            }
                        };
                        Thread thread3 = new Thread(unsafeTask);
                        Thread thread4 = new Thread(unsafeTask);
                        thread3.start();
                        thread4.start();
                        try {
                            thread3.join();
                            thread4.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        send("NOTIFY Tardis malfunction! You're in: " + unsafeCounter.getCount());
                        break;

                    case "FILE":
                        if (indm) {
                            send("NOTIFY Initiating file transfer...");
                            try {
                                JFileChooser fileChooser = new JFileChooser();
                                int returnValue = fileChooser.showOpenDialog(null);

                                if (returnValue == JFileChooser.APPROVE_OPTION) {
                                    File selectedFile = fileChooser.getSelectedFile();
                                    send("NOTIFY Selected file: " + selectedFile.getAbsolutePath());

                                    FileInputStream fileInputStream = new FileInputStream(selectedFile.getAbsolutePath());

                                    String fileName = selectedFile.getName();
                                    String fileType = fileName.substring(Math.max(fileName.length() - 3, 0)).toLowerCase();

                                    byte[] fileContentBytes = new byte[(int) selectedFile.length()];
                                    fileInputStream.read(fileContentBytes);
                                    String base64String = Base64.getEncoder().encodeToString(fileContentBytes);

                                    if (fileType.equals("mp4")) {
                                        send("NOTIFY Video sent");
                                        connectedTo.send("VIDEO " + base64String);
                                        connectedTo.send("NOTIFY You have been sent a video! It is now playing");
                                    } else {
                                        if (fileType.equals("png")) {
                                            send("NOTIFY Image sent");
                                            connectedTo.send("IMAGE " + base64String);
                                            connectedTo.send("NOTIFY You have been sent a image! It is now displaying");
                                        } else {
                                            send("NOTIFY File sent");
                                            connectedTo.send("NOTIFY You have been sent a file! It is in your downloads folder");
                                            connectedTo.send("FILE " + base64String + " " + fileName);
                                        }
                                    }

                                } else {
                                    send("NOTIFY No file selected.");
                                }
                            } catch (IOException e) {
                                send("NOTIFY Error occurred during file transfer: " + e.getMessage());
                            }
                        } else {
                            send("NOTIFY You must be in a DM to send a file.");
                        }
                        break;

                    case "ONLINE":
                        if (user == null || serve.connections == null) {
                            break;
                        }
                        List<String> onlineUsers = new ArrayList<>();
                        boolean ingc = false;
                        if (!indm) {
                            for (GroupChat gc : serve.chatRooms) {
                                for (ClientConnection cc : gc.getConnections()) {
                                    if (Objects.equals(this, cc)){
                                        ingc = true;
                                        for (ClientConnection con : gc.getConnections()){
                                            if (con.user != null){
                                                onlineUsers.add(con.user.getUsername());
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            if (!ingc) {
                                for (ClientConnection cc : serve.connections){
                                    if (cc.user != null){
                                        onlineUsers.add(cc.user.getUsername());
                                    }
                                }
                            }
                        } else {
                            onlineUsers.add(user.getUsername());
                            if (connectedTo != null) {
                                onlineUsers.add(connectedTo.user.getUsername());
                            }
                        }

                        List<String> newOnlineUsers = new ArrayList<>();

                        for (String i : onlineUsers) {
                            newOnlineUsers.add(serve.getIdleTime().isIdle(i) ? ("!" + i) : i);
                        }

                        send("ONLINE " + String.join(" ", newOnlineUsers));



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
            serve.removeConnections(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void dmSetUp(String user, ClientConnection c, ClientConnection cc){
        serve.removeConnections(c);
        activeDM = new DirectMessage(user, c, cc);
        serve.activeDMs.add(activeDM);
    }

}

