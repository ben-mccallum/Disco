import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


// to run save changes and run in terminal
// cs into src/Tcp and run:
// Javac *.java
// run:
// java MultiEchoServer 10002
// open another terminal and run:
// java echoClient localhost 10002
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
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received from client "+ clientName +" : " + inputLine);

                    // Send the message to all connected clients
                    for (PrintWriter writer : clientWriters) {
                        if (writer != clientWriter) {
                            writer.println("Client "+ clientName + " says: " + inputLine);
                        }
                    }
                }

                System.out.println("Client disconnected");
                clientWriters.remove(clientWriter);
            } catch (IOException e) {
                System.err.println("Error handling client communication");
                e.printStackTrace();
            }
        }
    }
}
