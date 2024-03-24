package uk.ac.strath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DirectMessage implements Runnable {
    private List<ClientConnection> dmconnections;
    protected ClientConnection waitingC;

    public DirectMessage(String user, ClientConnection c, ClientConnection cc) {
        dmconnections = new ArrayList<>();
        dmconnections.add(c);
        waitingC = cc;
    }

    @Override
    public void run() {
    }

    public void acceptRequest(Server s) {
        s.removeConnections(waitingC);
        dmconnections.getFirst().connectedTo = waitingC;
        waitingC.connectedTo = dmconnections.getFirst();
        dmconnections.add(waitingC);
        dmconnections.getFirst().send("NOTIFY " + waitingC.user.getUsername() + " has joined your dm, say hi!");
        waitingC.send("NOTIFY you have joined a dm with " + dmconnections.getFirst().user.getUsername());
        waitingC = null;
    }

    public void rejectRequest(Server s) {
        dmconnections.getFirst().send("NOTIFY " + waitingC.user.getUsername() + " rejected your request, you have rejoined the main message board");
        dmconnections.getFirst().indm = false;
        s.connections.add(dmconnections.getFirst());
    }

    public List<ClientConnection> getConnections() {
        return dmconnections;
    }
}