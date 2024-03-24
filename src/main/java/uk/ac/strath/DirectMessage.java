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
        dmconnections.get(0).connectedTo = waitingC;
        waitingC.connectedTo = dmconnections.get(0);
        dmconnections.add(waitingC);
        dmconnections.get(0).send("NOTIFY " + waitingC.user.getUsername() + " has joined your dm, say hi!");
        waitingC.send("NOTIFY you have joined a dm with " + dmconnections.get(0).user.getUsername());
        waitingC = null;
    }

    public void rejectRequest(Server s) {
        dmconnections.get(0).send("NOTIFY " + waitingC.user.getUsername() + " rejected your request, you have rejoined the main message board");
        dmconnections.get(0).indm = false;
        s.connections.add(dmconnections.get(0));
    }

    public List<ClientConnection> getConnections() {
        return dmconnections;
    }
}