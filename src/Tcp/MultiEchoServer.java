

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// to run save changes and run in terminal
// cs into src and run:
// Javac *.java
// cd in Tcp
// run:
// java MultiEchoServer 10002
// open another terminal and cd into src and run:
// java Main  localhost 10002
// run ^ for as many clients as you need

public class MultiEchoServer {

    private static List<PrintWriter> clientWriters = new ArrayList<>();

   public static int numClients = 0;

    public static void main(String[] args) {

        // read port number from command line
        int portNumber = Integer.parseInt(args[0]);

        try {
            // creates a new server socket with the given port number
            ServerSocket serverSocket = new ServerSocket(portNumber);

            System.out.println("Server listening on port " + portNumber);

            // continuously accept new client connections
            while (true) {
                // accepts a connection from a client
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // create a new thread to handle communication with the client
                PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(clientWriter);
                numClients++;
                new ClientHandler(clientSocket, clientWriter).start();
            }
        } catch (IOException e) {
            System.err.println("I/O error");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Inner class to handle communication with a client
        private static class ClientHandler extends Thread {
            private Socket clientSocket;
            private PrintWriter clientWriter;

            private String clientName;

            public ClientHandler(Socket socket, PrintWriter writer) {
                this.clientSocket = socket;
                this.clientWriter = writer;
                this.clientName = numClients + "";
            }

            @Override
            public void run() {
                try {
                    System.out.println("im watching");
                    // Add the new MessageHandler thread to handle messages for this client
                    new MessageHandler(clientSocket, clientWriter, clientWriters, clientName).start();
                } catch (Exception e) {
                    System.err.println("Error creating MessageHandler");
                    e.printStackTrace();
                }
            }
        }

    }
