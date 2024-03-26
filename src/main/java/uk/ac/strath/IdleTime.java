package uk.ac.strath;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IdleTime implements Runnable {
    private Server server;
    private Map<String, Long> lastMessages;
    private Map<String, Boolean> idleUsers;
    private Lock lock;

    public IdleTime(Server server) {
        this.server = server;
        lastMessages = new HashMap<>();
        idleUsers = new HashMap<>();
        lock = new ReentrantLock();
    }

    @Override
    public void run() {
        while (server.isRunning()) {
            long time = new Date().getTime();

            Map<String, Long> copy;
            Map<String, Boolean> idleCopy;
            lock.lock();
            try {
                copy = new HashMap<>(lastMessages);
                idleCopy = new HashMap<>(idleUsers);
            } finally {
                lock.unlock();
            }

            for (Entry<String, Long> entry : copy.entrySet()) {
                idleCopy.put(entry.getKey(), time > entry.getValue() + 10000);
            }

            lock.lock();
            try {
                idleUsers = idleCopy;
            } finally {
                lock.unlock();
            }
        }
    }

    public void setLastMessage(String user, long time) {
        lock.lock();
        try {
            lastMessages.put(user, time);
        } finally {
            lock.unlock();
        }
    }

    public boolean isIdle(String user) {
        lock.lock();
        try {
            return idleUsers.containsKey(user) ? idleUsers.get(user) : true;
        } finally {
            lock.unlock();
        }
    }
}
