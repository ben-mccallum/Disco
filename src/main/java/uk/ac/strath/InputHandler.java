package uk.ac.strath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;

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
                LinkedList<String> args = new LinkedList<>(Arrays.asList(message.split("\\s+")));
                String cmd = args.removeFirst();

                switch (cmd) {
                    case "/login":
                        if (args.size() < 2) {
                            System.out.println("Please provide a username and password!");
                        } else {
                            client.send("IDENTIFY " + args.get(0) + " " + args.get(1));
                        }
                        break;

                    default:
                        if (client.getToken() == null) {
                            System.out.println("You are not logged in!");
                            break;
                        }

                        client.send("MESSAGE " + client.getToken().toString() + " " + message);
                        break;
                }
            }
        } catch (IOException e) {
            client.stop();
        }
    }
}
