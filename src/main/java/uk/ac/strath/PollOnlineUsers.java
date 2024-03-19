package uk.ac.strath;

import java.util.Date;

public class PollOnlineUsers implements Runnable {
    private Client client;
    private long lastPoll;

    public PollOnlineUsers(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (client.isRunning()) {
            long time = new Date().getTime();

            if (time > lastPoll + 5000) {
                client.getOut().println("ONLINE");
                lastPoll = time;
            }
        }
    }
}
