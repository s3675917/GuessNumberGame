package Client;

public class Client {
    public static void main(String[] args) {
        new Thread(new ClientThread(null)).start();
    }
}
