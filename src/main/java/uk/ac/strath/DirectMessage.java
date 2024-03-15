package uk.ac.strath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DirectMessage implements Runnable {
    private List<ClientConnection> dmconnections;
    private List<String> Members;
    private ClientConnection waitingC;
    private String waiting;

    public DirectMessage(String user, String userWaiting, ClientConnection c, ClientConnection cc) {
        dmconnections = new ArrayList<>();
        dmconnections.add(c);
        Members = new ArrayList<>();
        Members.add(user);
        waitingC = cc;
        waiting = userWaiting;
    }

    @Override
    public void run() {
    }

    public String getWaiting() {
        return waiting;
    }

    public void acceptRequest(Server s) {
        for (GroupChat gc : s.getChats()) {
            gc.getConnections().removeIf(c -> c == waitingC);
            gc.getMembers().removeIf(u -> Objects.equals(u, waiting));
        }
        for (DirectMessage dm : s.getActiveDMs()) {
            dm.getConnections().removeIf(c -> c == waitingC);
            dm.getMembers().removeIf(u -> Objects.equals(u, waiting));
        }
        s.connections.removeIf(c -> c == waitingC);
        s.connectedUsers.removeIf(u -> Objects.equals(u, waiting));
        dmconnections.add(waitingC);
        Members.add(waiting);
        dmconnections.get(0).send("NOTIFY " + waiting + " has joined your dm, say hi!");
        waitingC.send("NOTIFY you have joined a dm with " + Members.get(0));
        waiting = null;
        waitingC = null;
    }

    public void rejectRequest(Server s) {
        dmconnections.get(0).send("NOTIFY " + waiting + " rejected ur request, you have rejoined the main message board");
        s.connections.add(dmconnections.get(0));
        s.connectedUsers.add(Members.get(0));
    }

    public List<String> getMembers() {
        return Members;
    }

    public List<ClientConnection> getConnections() {
        return dmconnections;
    }
}