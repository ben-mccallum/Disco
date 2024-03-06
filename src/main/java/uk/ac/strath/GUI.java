package uk.ac.strath;

import uk.ac.strath.gui.View;
import uk.ac.strath.gui.ViewChat;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class GUI implements Runnable {
    private Client client;
    private HashMap<String, View> views;
    private JFrame frame;

    public GUI(Client client) {
        this.client = client;

        views = new HashMap<>();
        frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addView(new ViewChat(this));
    }

    public void addView(View v) {
        views.put(v.getID(), v);
    }

    public boolean setView(String id) {
        if (!views.containsKey(id)) {
            return false;
        }

        frame.setContentPane(views.get(id).getMainPanel());
        frame.pack();
        frame.setVisible(true);
        showMessage("Welcome to Discord. Type /login to start");
        return true;
    }

    private void clearChat() {
        ViewChat vc = (ViewChat) views.get("chat");
        vc.getMessages().setText("");
    }

    @Override
    public void run() {
        setView("chat");

    }

    public void showMessage(String message) {
        ViewChat vc = (ViewChat) views.get("chat");

        vc.getMessages().setText(vc.getMessages().getText() + message + "\n");

    }


    public void sendMessage(String message) {

        LinkedList<String> args = new LinkedList<>(Arrays.asList(message.split("\\s+")));
        String cmd = args.removeFirst();

        switch (cmd) {
            case "/login":
                if (args.size() < 2) {
                    showMessage("Please provide a username and password!");
                    return;
                }

                client.send("IDENTIFY " + args.get(0) + " " + args.get(1));
                break;

            case "/chat":
                if(args.isEmpty()){
                    showMessage("Please provide which chat you want to display!");
                    return;
                }

                client.send("CHANNEL " + args.get(0));
                clearChat();
                break;


            case "/help":
                if(args.size() > 1){
                    //help lists for specific commands???
                    return;
                }
                showMessage(" ");
                showMessage("---------------------------------------------------");
                showMessage("To login, type '/login' followed by your username and password");
                showMessage("Once you're logged in, you can send messages here, or use '/chat' to enter a specific chatroom");
                showMessage("To create a new account, type '/signup' followed by your username and password");
                showMessage("To logout, type '/logout'");
                showMessage("To see this message again, type '/help'");
                showMessage("---------------------------------------------------");
                showMessage(" ");
                return;


            case "/signup":
                if(args.size() < 2){
                    showMessage("Please provide a username and a password!");
                    return;
                }
                client.send("SIGNUP " + args.get(0) + " " + args.get(1));
                break;


            case "/logout":
                client.send("LOGOUT");
                break;

            case "/leave":
                client.send("LEAVE");
                clearChat();
                break;


            case "/online":
                client.send("ONLINE");
                break;

            default:
                if (!message.isEmpty()) {
                    client.send("MESSAGE " + message);
                }
                break;
        }
    }
}
