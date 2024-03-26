package uk.ac.strath;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.*;

public class Client implements Runnable {
    private boolean running;
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private GUI gui;
    private PollOnlineUsers pollOnlineUsers;
    private Thread guiThread, pollOnlineThread;

    public Client(String ip) {
        running = true;
        gui = new GUI(this);
        guiThread = new Thread(gui);
        pollOnlineUsers = new PollOnlineUsers(this);
        pollOnlineThread = new Thread(pollOnlineUsers);

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
        guiThread.start();
        pollOnlineThread.start();

        String message;

        try {
            while ((message = in.readLine()) != null && running) {
                LinkedList<String> args = new LinkedList<>(Arrays.asList(message.split("\\s+")));
                String op = args.removeFirst();

                switch (op) {
                    case "HANDSHAKE":
                        if (args.isEmpty()) {
                            break;
                        }

                        gui.showMessage("Logged in as " + args.getFirst());
                        break;

                    case "MESSAGE":
                        gui.showMessage("[" + args.removeFirst() + "] " + String.join(" ", args));
                        break;

                    case "NOTIFY":
                        gui.showMessage(String.join(" ", args));
                        break;

                    case "ONLINE":
                        gui.setOnlineUsers(args.toArray(new String[args.size()]));
                        break;

                    case "FILE":
                        byte[] decodedFileBytes = Base64.getDecoder().decode(args.getFirst());
                        String name = args.getLast();
                        fileDownload(decodedFileBytes, name);
                        break;

                    case "IMAGE":
                        byte[] decodedImageBytes = Base64.getDecoder().decode(args.getFirst());
                        displayImage(decodedImageBytes);
                        break;

                    case "VIDEO":
                        byte[] decodedVideoBytes = Base64.getDecoder().decode(args.getFirst());
                        displayVideo(decodedVideoBytes);
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

    public PrintWriter getOut() {
        return out;
    }

    public boolean isRunning() {
        return running;
    }

    public void displayImage(byte[] data){
        SwingUtilities.invokeLater(() -> new Image(data));
    }

    public void fileDownload(byte[] data, String fileName){
        String downloadFolderPath = System.getProperty("user.home") + File.separator + "Downloads";
        String filePath = downloadFolderPath + File.separator + fileName;
        try (FileOutputStream stream = new FileOutputStream(filePath)) {
            stream.write(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void displayVideo(byte[] data) {
        SwingUtilities.invokeLater(() -> {
            MediaPlayer mediaPlayer = new MediaPlayer(data);
            mediaPlayer.setVisible(true);
        });
    }



}
