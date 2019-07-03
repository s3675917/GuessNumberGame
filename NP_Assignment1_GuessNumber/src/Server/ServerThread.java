package Server;

import Utils.User;

import java.io.*;
import java.net.Socket;

/*
* ServerThread handles the connection with Client thread.
* Processes:
* initialization ->
* create a keep alive thread ->
* if this player choose to play, put this user to the Console.lobbyQueue ->
* if this player choose to leave -> end.
*
*
* @gameDataSocket is the TCP connection serverSocket to transmit the game data between server and client.
* @keepAliveServerSocket is the TCP connection serverSocket to keep connection alive. It observes itself and
* gameDataSocket's connection.
* @playerName is the user name using this connection.
* @ipAddress is the user ip address using this connection.
* */
public class ServerThread implements Runnable {
    private Socket gameDataSocket, keepAliveServerSocket;
    private String playerName, ipAddress;
    private User user;

    public ServerThread(Socket connection, Socket keepAliveServerSocket) {
        this.gameDataSocket = connection;
        this.keepAliveServerSocket = keepAliveServerSocket;
        playerName = null;
    }

    @Override
    public void run() {
        //receive message

        try {
            //initialize the reader and writer
            BufferedReader readerFromClient = new BufferedReader(new InputStreamReader(gameDataSocket.getInputStream()));
            PrintWriter sendToClient = new PrintWriter(gameDataSocket.getOutputStream(), true);

            //read player name and ip address
            playerName = readerFromClient.readLine();
            ipAddress = readerFromClient.readLine();
            LogController.printLog("Player Name: "+ playerName + " IP address: "+ ipAddress + " connected");

            //create User to store this player's info, its writer and reader of the client layer.
            user = new User(playerName, readerFromClient, sendToClient, ipAddress);

            //Keep alive message thread
            // this thread send a ping String to client at port 61717 every second,
            // if client don't send any feedback, it will claim the connection is dead.
            // it can handle the normal or the unusual disconnection
            //
            // */
            new Thread() {
                public void run() {
                    try {
                        PrintWriter keepAliveToClient = new PrintWriter(keepAliveServerSocket.getOutputStream(), true);
                        BufferedReader keepAliveFromClient = new BufferedReader(new InputStreamReader(keepAliveServerSocket.getInputStream()));
                        String s;
                        long time = System.currentTimeMillis();
                        while (true) {
                            s = keepAliveFromClient.readLine();
                            //show the ping on the screen
                            //System.out.println(playerName + " client is alive, ping is " + (System.currentTimeMillis() - time) + " ms");
                            sleep(1000);
                            time = System.currentTimeMillis();
                            //send the request to client
                            keepAliveToClient.println("ping");

                            //judge the feedback from client, if it is a normal disconnection
                            if (s != null) {
                                if (s.equals("end"))
                                 break;
                                else if (s.equals("timeout")) {
                                    LogController.printLog("Player Name: "+ playerName + " IP address: "+ ipAddress + " time out");
                                }
                            } else if (s == null){
                                LogController.printLog("Player Name: "+ playerName + " IP address: "+ ipAddress + " lost connection");
                                user.setConnected(false);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        //judge feedback from client, if it is an unusual disconnection
                        LogController.printLog("Player Name: "+ playerName + " IP address: "+ ipAddress + " lost connection");
                        user.setConnected(false);
                        LogController.close();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        LogController.close();
                    }
                }
            }.start();
            // end of the keep alive thread

            // wait for the client to send a play game request/*
            // 'p' is to play
            // 'q' is to quit
            //
            // */
            String s;
            while (true) {
                s = readerFromClient.readLine();
                if (s != null) {
                    if (s.equals("p")) {
                        Console.lobbyQueue.put(user);
                        break;
                    } else if (s.equals("q")) {
                        LogController.printLog("Player Name: "+ playerName + " IP address: "+ ipAddress + " left");
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LogController.close();
        }
    }
}


