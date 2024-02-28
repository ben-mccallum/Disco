

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class MessageHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter clientWriter;
    private List<PrintWriter> clientWriters;
    private String clientName;

    public MessageHandler(Socket socket, PrintWriter writer, List<PrintWriter> writers,String clientName) {
        this.clientSocket = socket;
        this.clientWriter = writer;
        this.clientWriters = writers;
        this.clientName = clientName;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                // closes the connection if the client types exit
                if (inputLine.equals("exit")) {
                    System.out.println("Client " + clientName + " disconnected");
                    clientSocket.close();
                    clientWriters.remove(clientWriter);
                    break;
                }
                System.out.println("Received from client " + clientName + ": " + inputLine);

                // Broadcast the message to all connected clients
                for (PrintWriter writer : clientWriters) {
                    if (writer != clientWriter) {
                        writer.println("Client " + clientName + " says: " + inputLine);
                    }
                }
            }

            //System.out.println("Client disconnected");
            //clientWriters.remove(clientWriter);
        } catch (IOException e) {
            System.err.println("Error handling client communication");
            e.printStackTrace();
        }
    }
}
