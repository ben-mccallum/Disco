package uk.ac.strath;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class User{
    String Username;
    String Password;
    PrintWriter fileWriter;

    public User(String username, String password) throws FileNotFoundException{
        fileWriter = new PrintWriter("register.txt");

        Scanner fileScanner = new Scanner("register.txt");
        while(fileScanner.hasNext()){
            String line = fileScanner.nextLine();
            String user = line.split(",")[0];
            String pass = line.split(",")[1];
            if(Objects.equals(user, username) && Objects.equals(pass, password)){
                this.Username = username;
                this.Password = password;
                UserManager.addUser(this);
                fileScanner.close();
            }
        }
        fileScanner.close();
        System.out.println("Username or password is incorrect.");
    }

    public User(String username) throws FileNotFoundException{
        fileWriter = new PrintWriter("register.txt");
        User userExists = UserManager.getUser(username);
        if(userExists != null){
            System.out.println("User with this name already exists.");
        }else{
            Scanner in = new Scanner(System.in);
            System.out.println("Creating user...");
            System.out.println("Enter a password: ");
            String password = in.nextLine();
            fileWriter.println(username + " " + password);
            UserManager.addUser(this);
            this.Username = username;
            this.Password = password;
            in.close();
            System.out.println("User created.");
        }
    }

    public String getUsername(){return this.Username;}

    public String getPassword(){return this.Password;}
}