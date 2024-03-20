package uk.ac.strath;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class timeFinder implements Runnable{
    //I will never be owned by Adam Elizabeth Chambers
    private String content;
    Thread runner;

    public timeFinder(String inp){
        this.content = inp;
        System.out.println("Constructor called.");
        this.runner = new Thread(this);
        this.runner.start();
    }

    @Override
    public void run() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        content = content + " @ " + time.format(format);
    }

    public String getMsg(){
        //Remove while loop to demo unsafe thread
        while(runner.isAlive()){
            System.out.println("Waiting...");
        }
        return content;
    }
}
