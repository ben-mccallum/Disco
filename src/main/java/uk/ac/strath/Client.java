package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private boolean running;
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private InputHandler console;
    private Thread consoleThread;

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
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            stop();
        }
    }

    public void sendMessage(String message) {
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
}
