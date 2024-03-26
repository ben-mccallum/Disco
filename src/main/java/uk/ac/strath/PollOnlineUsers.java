package uk.ac.strath;

import java.util.Date;

public class PollOnlineUsers implements Runnable {
    private volatile Client client; // Adding volatile keyword for visibility
    private volatile long lastPoll; // Adding volatile keyword for visibility

    public PollOnlineUsers(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (client.isRunning()) {
            long time = new Date().getTime();

            if (time > lastPoll + 1000) {
                if (client != null && client.getOut() != null) { // Check for null client or output stream
                    client.getOut().println("ONLINE");
                    lastPoll = time;
                } else {
                    System.out.println("Client or output stream is null."); // Print error message
                }
            }
        }
    }
}