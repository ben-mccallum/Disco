package uk.ac.strath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupChat implements Runnable{
    private List<ClientConnection> gcconnections;
    private String ID;

    public GroupChat(ClientConnection cc, String chatname){
        gcconnections = new ArrayList<>();
        gcconnections.add(cc);
        ID = chatname;
    }

    @Override
    public void run(){
    }

    public String getID(){
        return ID;
    }

    public List<ClientConnection> getConnections(){
        return gcconnections;
    }

    public void addMember(Server s, ClientConnection c) {
        s.removeConnections(c);
        gcconnections.add(c);
    }
}
