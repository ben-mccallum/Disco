package uk.ac.strath;

public class App {
    private static App instance;

    private App() {
        System.out.println("Hello World!");
    }

    public static void main(String[] a) {
        instance = new App();
    }

    public static App getInstance() {
        return instance;
    }
}
