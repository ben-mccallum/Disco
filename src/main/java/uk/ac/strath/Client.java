package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

public class Client implements Runnable {
    private boolean running;
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private InputHandler console;
    private Thread consoleThread;
    private UUID token;

    public Client(String ip) {
        running = true;
        console = new InputHandler();
        consoleThread = new Thread(console);

        try {
            client = new Socket(ip, App.PORT);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (Exception e) {
            running = false;
        }
    }

    @Override
    public void run() {
        consoleThread.start();

        String message;

        try {
            while ((message = in.readLine()) != null && running) {
                LinkedList<String> args = new LinkedList<>(Arrays.asList(message.split("\\s+")));
                String op = args.removeFirst();

                if (token == null && !op.equals("HANDSHAKE") && !op.equals("NOTIFY")) {
                    continue;
                }

                switch (op) {
                    case "HANDSHAKE":
                        if (args.isEmpty()) {
                            break;
                        }

                        token = UUID.fromString(args.getFirst());
                        System.out.println("Logged in as " + token.toString());
                        break;

                    case "MESSAGE":
                        if (!UUID.fromString(args.removeFirst()).equals(token)) {
                            System.out.println(String.join(" ", args));
                        }
                        break;

                    case "NOTIFY":
                        System.out.println(String.join(" ", args));
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
        running = false;

        try {
            in.close();
            out.close();

            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public UUID getToken() {
        return token;
    }
}
