package Server;

import Utils.User;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/*
* Every time a new game started, this class will be created to handle the game processes.
* Processes:
* initialization ->
* print Announcement Message to Client ->
* start the game process ->
* sort the score and print the ranking ->
* end
*
* @answer is the correct answer in this round.
* @playerList is a list to store the players in this round.
* @playersNameAnnouncementMessage is a message sent to all players
* in this round to show all the players name.
* @countDownLatch is to stop processes to continue until all players
*  finish their games.
* */
public class GameController {
    private static String answer;
    private ArrayList<User> playerList;
    private String playersNameAnnouncementMessage;
    private static CountDownLatch countDownLatch;

    public GameController() {
        try {
            init();
            printAnnounceMessage();
            startGame();
            // wait if some players didn't finish game.
            countDownLatch.await();
            // sort the result from lowest to highest.
            sortWinnerandPrint();
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogController.close();
        }
    }

    private void sortWinnerandPrint() {
        String rankingMessage = "Ranking:";
        Collections.sort(playerList);
        for (User player : playerList
        ) {
            if (player.getPoint() < 5) {
                rankingMessage += "|"+player.getName() + " wins by " + player.getPoint() + " attempts";
            } else rankingMessage += "|"+player.getName() + " lose";
        }

        PrintWriter out;
        for (User player : playerList) {
            out = player.getOutPutWriter();
            out.println(rankingMessage);
        }
        LogController.printLog(rankingMessage);
        LogController.printLog("-----------------------------------");
    }

    /*
    * it will start a thread for each player so they can guess at any time.
    * Players could not interact or effect with each other.
    * Each player only connect to this game server.
    * When one player finish the game, the result will be stored in its user.point variable.
    *
    * @attempt is to represent the result. 5 means this player lost the game in 4 attempts or
    * lost connection.
    * */
    private void startGame() {
        countDownLatch = new CountDownLatch(playerList.size());
        for (User player : playerList) {

            //create a thread for this player
            new Thread() {
                boolean winOrLose = false;
                int attempt = 0;
                BufferedReader reader = player.getInputReader();
                PrintWriter out = player.getOutPutWriter();
                String playerInputNum, response;

                public void run() {
                    while (!winOrLose & attempt < 4) {
                        try {
                            playerInputNum = reader.readLine();
                            attempt++;
                            LogController.printLog(player.getName() + " inputs " + playerInputNum + ", " + (4 - attempt) + " attempts left");

                            if (playerInputNum != null) {
                                response = responseMessage(playerInputNum);
                                LogController.printLog("Send to " + player.getName() + ": " + response);
                                out.println(response);
                            } else {
                                //Lost Connection
                                LogController.printLog(player.getName() + " lost Connection or quit the game");
                                attempt = 5;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            LogController.close();
                        }
                    }//end of the while loop, game is over.

                    //store the point in the user Object.
                    player.setPoint(attempt);
                    //claim one player finish the game.
                    countDownLatch.countDown();
                }
                /*
                * judging the guess num from player.
                * The verification of the input(only 0 - 9) is in the Client side.
                *
                * This is a synchronized method.
                */
                private synchronized String responseMessage(String playerInputNum) {
                    if (playerInputNum.equals(answer)) {
                        winOrLose = true;
                        return "Congratulation, the correct answer is " + answer;
                    } else if (attempt == 4) {
                        attempt = 5;
                        return "Game over, the answer is " + answer;
                    } else if (playerInputNum.compareTo(answer) > 0)
                        return "The guess number " + playerInputNum + " is bigger than the generated number, " + (4 - attempt) + " chances left.";
                    else
                        return "The guess number " + playerInputNum + " is smaller than the generated number, " + (4 - attempt) + " chances left.";
                }
            }.start();
            //end of the thread for one player
        }//end of the players loop
    }


    public void init() {
        LogController.printLog("===============\n                    New Game \n                   ===============");
        answer = String.valueOf((int) (Math.random() * 10));
        LogController.printLog("This game correct answer is " + answer);
        int playerNum = 3;
        User user;
        playerList = new ArrayList();
        playersNameAnnouncementMessage = "Players in this Round: | ";

        while (playerNum > 0 & Console.lobbyQueue.size() > 0) {
            //get players from queue
            try {
                user = Console.lobbyQueue.take();
                if (user.getConnected()) {
                    playerNum--;
                    playersNameAnnouncementMessage += user.getName() + " | ";
                    playerList.add(user);
                    LogController.printLog(user.getName() + " is added to the game");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogController.close();

            }
        }
    }

    private void printAnnounceMessage() {
        //print announcement message
        PrintWriter out;
        for (User player : playerList
        ) {
            out = player.getOutPutWriter();
            out.println("begin");
            out.println(playersNameAnnouncementMessage);
        }
    }
}
