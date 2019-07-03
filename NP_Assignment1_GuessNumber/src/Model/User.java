package Utils;

import java.io.BufferedReader;
import java.io.PrintWriter;
/*
* This Class is to store the temporary data for one player.
*@outPutWriter and @inputReader are linked to the client socket. write and read is valid
* by using these streams.
*@point: what is the score for this round of this player.
*@connected: the socket connection status.
*
* */
public class User implements Comparable<User> {
    private String name, ipAddress;
    private int point;
    private PrintWriter outPutWriter;
    private BufferedReader inputReader;
    private Boolean connected;

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public User(String name, BufferedReader in, PrintWriter out,String idAddress) {
        this.name = name;
        this.outPutWriter = out;
        inputReader = in;
        this.ipAddress = idAddress;
        connected = true;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public BufferedReader getInputReader() {
        return inputReader;
    }

    public String getName() {
        return name;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public PrintWriter getOutPutWriter() {
        return outPutWriter;
    }

    @Override
    public int compareTo(User o) {
        if (point > o.getPoint()) return 1;
        if (point == o.getPoint()) return 0;
        if (point < o.getPoint()) return -1;

        return 0;
    }

}
