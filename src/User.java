import java.io.*;
import java.util.Scanner;

public class User{
    String Username;
    String Password;
    File register = new File("/storage/register.txt");
    PrintWriter fileWriter;

    public User(String username, String password) throws FileNotFoundException{
        fileWriter = new PrintWriter(register);
        User userExists = Users.getUser(username);
        if(userExists != null){
            if(userExists.getPassword() != password){
                System.out.println("Incorrect password.");
            }else{
                this.Username = username;
                this.Password = password;
            }
        }else{
            System.out.println("User with this name does not exist.");
        }
    }

    public User(String username) throws FileNotFoundException{
        fileWriter = new PrintWriter(register);
        User userExists = Users.getUser(username);
        if(userExists != null){
            System.out.println("User with this name already exists.");
        }else{
            Scanner in = new Scanner(System.in);
            System.out.println("Creating user...");
            System.out.println("Enter a password: ");
            String password = in.nextLine();
            fileWriter.println(username + ", " + password);
            Users.addUser(this);
            this.Username = username;
            this.Password = password;
            in.close();
            System.out.println("User created.");
        }
    }

    public String getUsername(){return this.Username;}

    public String getPassword(){return this.Password;}
}