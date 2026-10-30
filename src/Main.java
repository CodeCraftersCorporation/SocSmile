import controller.HttpServerManager;

public class Main {
    public static void main(String[] args) {
        try {
            new HttpServerManager().start();
        } catch (Exception e) {
            System.err.println("Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
