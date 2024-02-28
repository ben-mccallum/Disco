package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputHandler implements Runnable {
    private Client client;
    private BufferedReader input;

    @Override
    public void run() {
        client = App.getInstance().getClient();
        input = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (client.isRunning()) {
                String message = input.readLine();

                client.sendMessage(message);
            }
        } catch (IOException e) {
            client.stop();
        }
    }
}
