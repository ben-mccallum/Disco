package uk.ac.strath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupChat implements Runnable{
    private List<ClientConnection> gcconnections;
    private List<String> Members;
    private String ID;

    public GroupChat(ClientConnection cc, String chatname, String user){
        gcconnections = new ArrayList<>();
        gcconnections.add(cc);
        Members = new ArrayList<>();
        Members.add(user);
        ID = chatname;
    }

    @Override
    public void run(){
    }

    public String getID(){
        return ID;
    }

    public List<String> getMembers(){
        return Members;
    }

    public List<ClientConnection> getConnections(){
        return gcconnections;
    }

    public void addMember(Server s, ClientConnection c, String user) {
        s.removeConnections(c, user);
        Members.add(user);
        gcconnections.add(c);
    }
}
