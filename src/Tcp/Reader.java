package Tcp;

import java.net.*;
import java.io.*;
class Reader implements Runnable {

    PrintWriter out;
    BufferedReader in;
    BufferedReader stdIn;



    public Reader(BufferedReader in, BufferedReader stdIn, PrintWriter out) {
        this.in = in;
        this.out = out;
        this.stdIn = stdIn;
    }

    public Reader() {
    }

    @Override
    public void run() {
        try {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }
            }catch (IOException e) {
            e.printStackTrace();
        }
    }
}