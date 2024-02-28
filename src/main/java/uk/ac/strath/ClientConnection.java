package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

public class ClientConnection implements Runnable {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private UUID userID;

    public ClientConnection(Socket client) {
        this.client = client;

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

                if (userID == null && !op.equals("IDENTIFY")) {
                    send("NOTIFY You are not logged in!");
                    continue;
                }

                switch (op) {
                    case "IDENTIFY":
                        if (args.size() < 2) {
                            send("NOTIFY Please provide a username and password!");
                            break;
                        }

                        if (args.get(0).equals("username") && args.get(1).equals("password")) {
                            userID = UUID.randomUUID();

                            send("HANDSHAKE " + userID.toString());
                        } else {
                            send("NOTIFY Invalid username or password!");
                        }
                        break;

                    case "MESSAGE":
                        App.getInstance().getServer().broadcast("MESSAGE " + String.join(" ", args));
                        break;

                    default:
                        break;
                }
            }
        } catch (IOException e) {
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
