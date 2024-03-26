package uk.ac.strath;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

public class IdleTime implements Runnable{
    private Server server;
    private Map<String, Long> lastMessages;
    private Map<String, Boolean> idleUsers;

    public IdleTime(Server server){
        this.server = server;
        lastMessages = new HashMap<>();
        idleUsers = new HashMap<>();
    }

    @Override
    public void run(){
        while(server.isRunning()){
            long time = new Date().getTime();

            Map<String, Long> copy = new HashMap<>(lastMessages);
            Map<String, Boolean> idleCopy = new HashMap<>(idleUsers);

            for (Entry<String, Long> i : copy.entrySet()) {
                idleCopy.put(i.getKey(), time > i.getValue() + 10000);
            }
            idleUsers = idleCopy;
        }

    }

    public void setLastMessage(String user, long time) {
        lastMessages.put(user, time);
    }

    public boolean isIdle(String user) {
        return idleUsers.containsKey(user) ? idleUsers.get(user) : true;
    }
}
