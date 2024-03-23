package uk.ac.strath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DirectMessage implements Runnable {
    private List<ClientConnection> dmconnections;
    protected List<String> Members;
    protected ClientConnection waitingC;
    protected String waiting;

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
        s.removeConnections(waitingC, waiting);
        dmconnections.getFirst().connectedTo = waitingC;
        waitingC.connectedTo = dmconnections.getFirst();
        dmconnections.add(waitingC);
        Members.add(waiting);
        dmconnections.getFirst().send("NOTIFY " + waiting + " has joined your dm, say hi!");
        waitingC.send("NOTIFY you have joined a dm with " + Members.getFirst());
        waiting = null;
        waitingC = null;
    }

    public void rejectRequest(Server s) {
        dmconnections.getFirst().send("NOTIFY " + waiting + " rejected your request, you have rejoined the main message board");
        dmconnections.getFirst().indm = false;
        s.connections.add(dmconnections.getFirst());
        s.connectedUsers.add(Members.getFirst());
    }

    public List<String> getMembers() {
        return Members;
    }

    public List<ClientConnection> getConnections() {
        return dmconnections;
    }
}