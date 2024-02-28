package uk.ac.strath;

import java.util.Arrays;
import java.util.List;

public class App {
    public static final int PORT = 3000;
    private static App instance;
    private Client client;
    private Server server;
    private Thread thread;

    // client constructor
    private App(String ip) {
        instance = this;
        client = new Client(ip);
        thread = new Thread(client);
        thread.start();
    }

    // server constructor
    private App() {
        instance = this;
        server = new Server();
        thread = new Thread(server);
        thread.start();
    }

    public static void main(String[] a) {
        List<String> args = Arrays.asList(a);

        if (args.contains("-connect") && args.indexOf("-connect") + 1 < args.size()) {
            new App(args.get(args.indexOf("-connect") + 1));
        } else {
            new App();
        }
    }

    public Client getClient() {
        return client;
    }

    public Server getServer() {
        return server;
    }

    public static App getInstance() {
        return instance;
    }
}
