package uk.ac.strath;

import java.util.ArrayList;
import java.util.List;

public class GroupChat extends Server implements Runnable{
    private List<ClientConnection> connections;
    private String ID;

    public GroupChat(ClientConnection cc, String chatname){
        connections = new ArrayList<>();
        connections.add(cc);
        ID = chatname;
    }

    public void broadcast(String message) {
        for (ClientConnection cc : connections) {
            if (cc != null) {
                cc.send(message);
            }
        }
    }

    @Override
    public void run(){
    }

    public String getID(){
        return ID;
    }
}
