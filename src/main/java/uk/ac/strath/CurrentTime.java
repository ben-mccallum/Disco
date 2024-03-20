package uk.ac.strath;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CurrentTime implements Runnable {
    private GUI gui;

    public CurrentTime(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        while (true) {

            LocalDateTime currentTime = LocalDateTime.now();


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);

            gui.setCurrentTime(formattedTime);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

}
}
