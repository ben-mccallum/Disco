import java.net.*;
import java.io.*;

public class Main{
    public static void main(String[] args) {
        // read hostname and portnumber from command line
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        System.out.println("Server listening on port " + portNumber);

        System.out.println("New client connected");

        Thread clientThread = new Thread(new echoClient(hostName, portNumber));
        clientThread.start();
    }
}
