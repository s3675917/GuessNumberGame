package Server;

import Utils.User;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;

/*processes:
initialization ->
create 100 server threads to stand by ->
listen to the input from the console:
s-> start the game
e-> quit and store the backlog

 * @lobbyQueue is an queue array to list the player socket's output and input and other info, the details
 * are in the Utils.User.java
 * @serverSocket is the TCP connection serverSocket to transmit the game data between server and client.
 * @serverSocketReader is to read data from @serverSocket.
 * @keepAliveServerSocket the TCP connection serverSocket to keep connection alive. It observes itself and
 * serverSocket's connection.
 * both serverSocket and keepAliveServerSocket will be used in the ServerThread and ClientThread.
 * @LogController provides a static method to write the backlog.
 * */
public class Console {
    public static ArrayBlockingQueue<User> lobbyQueue;
    private static ServerSocket serverSocket, keepAliveServerSocket;
    private static BufferedReader serverSocketReader;


    public static void main(String[] args) throws IOException {
        //initialize all the variable
        init();
        //create 100 server threads waiting for connection.
        new Thread() {
            public void run() {
                try {
                    Thread thread;
                    for (int i = 0; i < 100; i++) {
                        ServerThread serverThread = new ServerThread(serverSocket.accept(), keepAliveServerSocket.accept());
                        thread = new Thread(serverThread);
                        thread.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LogController.close();
                }
            }
        }.start();
        /*
         * Command:
         * s: start the game
         * e: exit the game
         * */
        String input;
        System.out.println("press 's' to start the game, press 'e' to terminate the game server");
        while ((input = serverSocketReader.readLine()) != null) {
            if (input.equals("s")) {
                // Game started, create a game.
                new GameController();
                System.out.println("Game over, press 's' to start the game, press 'e' to terminate the game server");
            }
            //terminate the game server and close the backlog.
            if (input.equals("e")) {
                LogController.close();
                System.exit(0);
            }
        }
    }

    private static void init() throws IOException {
        serverSocketReader = new BufferedReader(new InputStreamReader(System.in));
        lobbyQueue = new ArrayBlockingQueue<>(100);
        serverSocket = new ServerSocket(61707, 10); // Binds to the server port
        keepAliveServerSocket = new ServerSocket(61717, 10);
        new LogController();
    }

}
