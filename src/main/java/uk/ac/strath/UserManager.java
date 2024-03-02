package uk.ac.strath;

import java.util.*;

public class UserManager {
    private static HashMap<User, Boolean> userList = new HashMap<User, Boolean>();

    public static void addUser(User user){
        userList.put(user, false);
    }

    public static void removeUser(User user){
        userList.remove(user);
    }

    public static HashMap<User, Boolean> getUserList(){
        return userList;
    }

    public static void setOnline(User user){
        userList.put(user, true);
    }

    public static void setOffline(User user){
        userList.put(user, false);
    }

    public static boolean isOnline(User user){
        return userList.get(user);
    }

    public static ArrayList<User> getOnlineUsers(){
        ArrayList<User> onlineUsers = new ArrayList<User>();
        for (User user : userList.keySet()){
            if (userList.get(user)){
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }

    public static ArrayList<User> getOfflineUsers(){
        ArrayList<User> offlineUsers = new ArrayList<User>();
        for (User user : userList.keySet()){
            if (!userList.get(user)){
                offlineUsers.add(user);
            }
        }
        return offlineUsers;
    }

    public static User getUser(String username){
        for (User user : userList.keySet()){
            if (user.getUsername().equals(username)){
                return user;
            }
        }
        return null;
    }
}