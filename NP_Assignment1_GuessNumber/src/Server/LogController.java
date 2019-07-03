package Server;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
* this is a global class, every class in the server package could call and write into the log.
* */
class LogController {
    //log file path
    private final String filePath = "log.txt";
    private static PrintWriter logFilePrintWriter;

    protected LogController(){
        try {
            File file = new File(filePath);
            FileWriter logFileOutputFileWriter = new FileWriter(file,true);
            logFilePrintWriter = new PrintWriter(logFileOutputFileWriter,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void printLog(String s){
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        logFilePrintWriter.println(dateFormat.format(new Date())+ ": "+ s);
        System.out.println(s);
    }

    protected static void close(){
        logFilePrintWriter.close();
    }
}
