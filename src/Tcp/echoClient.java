package Tcp;

import java.net.*;
import java.io.*;

public class echoClient implements Runnable {

    String hostName;
    int portNumber;
    public echoClient(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public echoClient() {
    }

    @Override
    public void run() {

        try {
            // trys to create a new socket with the given hostname and portnumber
            Socket echoSocket = new Socket(hostName, portNumber);

            // creates a new PrintWriter and BufferedReader to read and write to the socket
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            // reads from the command line and writes to the socket
            Thread reader = new Thread(new Reader(in, stdIn, out));
            /*String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }*/

            out.close();
            in.close();
            stdIn.close();
          //  echoSocket.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            e.printStackTrace();
            System.exit(1);
        }
    }
}





