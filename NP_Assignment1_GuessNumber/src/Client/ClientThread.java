package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/*
 * ClientThread handles game data transmission of the client side.
 * Processes: checkIfPlayerWantsToPlay()->waitingForGameStarted()->startGaming()->Print ranking->Game end
 *
 * @gameDataSocket: is the TCP connection serverSocket to transmit the game data between server and client.
 * @keepAliveSocket: the TCP connection serverSocket to keep connection alive. It observes itself and
 * gameDataSocket's connection.
 * @playerName: the user name using this connection.
 * @outToServer: the PrintWriter tool to output data to Server.
 * @gamePlaying: true = game is on, false = game is over or waiting for game started.
 * @isConnected: true = the connection is working, false = if game is ended, the connection will be closed.
 * */
public class ClientThread implements Runnable {
    private Socket gameDataSocket, keepAliveSocket;
    private static PrintWriter outToServer;
    private static BufferedReader readerFromServer, readerFromLocal;
    String playerInputNum;
    private Boolean gamePlaying, isConnected;
    private String playerName;
    private long timeOut;
    private final String ipName = "localhost";

    public ClientThread(String playerName) {
        try {
            init();
            if (playerName == null) {
                register();
            } else {
                this.playerName = playerName;
                outToServer.println(playerName);
                outToServer.println(InetAddress.getLocalHost().getHostAddress());
            }

            //Keep alive thread
            new Thread() {
                public void run() {
                    try {
                        PrintWriter keepAliveToServer = new PrintWriter(keepAliveSocket.getOutputStream(), true);
                        BufferedReader keepAliveFromServer = new BufferedReader(new InputStreamReader(keepAliveSocket.getInputStream()));
                        while (true) {
                            //if the game is ended, send a "end" to Server.
                            if (!isConnected) {
                                keepAliveToServer.println("end");
                                break;
                                //if players did not finish game in 60 seconds in the game, it will cause a time out.
                            } else if (System.currentTimeMillis() - timeOut > 60000 && gamePlaying) {
                                keepAliveToServer.println("timeout");
                                System.out.println("Time Out, please relaunch the game");
                                System.exit(0);
                                //send the feedback to the client
                            } else {
                                keepAliveToServer.println(System.currentTimeMillis());
                                keepAliveFromServer.readLine();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }//end of the keep alive thread
    }//end of the construction

    @Override
    public void run() {
        try {
            checkIfPlayerWantsToPlay();
            waitingForGameStarted();
            startGaming();
            //Print ranking
            System.out.println(readerFromServer.readLine());
            //Game End
            new Thread(new ClientThread(playerName)).start();
            gamePlaying = false;
            isConnected = false;
        } catch (Exception e) {
            System.out.println("Sorry, Game Server Collapse.");
            System.exit(0);
        }
    }//end of the thread processes

    private void checkIfPlayerWantsToPlay() throws IOException {
        String s;
        System.out.println("input 'p' for playing, 'q' for quit");

        while ((s = readerFromLocal.readLine()) != null) {
            if (s.equals("p") || s.equals("q")) {
                outToServer.println(s);
                if (s.equals("p")) break;
                if (s.equals("q")) System.exit(0);
            }
        }
    }

    private void startGaming() throws IOException {
        //Welcome Message
        System.out.println(readerFromServer.readLine());
        System.out.println("Game Started");
        String responseMessage;

        //game loop until game is over or time out
        while (true) {
            playerInputNum = playerInputAGuessNum();
            if (playerInputNum != null) {
                outToServer.println(playerInputNum);
                responseMessage = readerFromServer.readLine();
                if (responseMessage != null) {
                    System.out.println(responseMessage);
                    //game is over, feedback message will start will "The".
                    if (!responseMessage.substring(0, 3).equals("The")) break;
                }
            }
        }
    }

    private void init() throws IOException {
        gamePlaying = false;
        isConnected = true;
        gameDataSocket = new Socket(InetAddress.getByName(ipName), 61707);
        keepAliveSocket = new Socket(InetAddress.getByName(ipName), 61717);
        outToServer = new PrintWriter(gameDataSocket.getOutputStream(), true);
        readerFromLocal = new BufferedReader(new InputStreamReader(System.in));
        readerFromServer = new BufferedReader((new InputStreamReader(gameDataSocket.getInputStream())));
    }

    private void waitingForGameStarted() throws IOException {
        System.out.println("Please wait for the game start ");
        while (true) {
            if ((readerFromServer.readLine()).equals("begin")) {
                break;
            }
        }
        gamePlaying = true;
        timeOut = System.currentTimeMillis();
    }

    private void register() throws IOException {
        System.out.println("Please write down your name: ");
        playerName = readerFromLocal.readLine();
        outToServer.println(playerName);
        outToServer.println(InetAddress.getLocalHost().getHostAddress());
    }

    //verification of the input number
    private String playerInputAGuessNum() {
        System.out.println("Guess a number between 0 and 9");
        try {
            String s = readerFromLocal.readLine();
            if (s.equals("e")) {
                System.out.println("exit");
                System.exit(0);
            } else if ("0".compareTo(s) > 0 || "9".compareTo(s) < 0 || s.length() != 1) {
                System.out.println("Valid Input! Please input an integer num from 0 to 9");
                return null;
            } else return s;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}


