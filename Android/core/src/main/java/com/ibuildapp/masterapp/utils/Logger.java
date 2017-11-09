package com.ibuildapp.masterapp.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 05.11.14
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
public class Logger {

    private static Logger logger;
    private static FileWriter writer;

    public static Logger getInstance()
    {
        if ( logger == null )
            try {
                Logger.init(Environment.getExternalStorageDirectory().getAbsolutePath(),"log.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }

        return logger;
    }

    /**
     * Full path from root
     * @param logPath
     */
    public static void init(String logPath, String filename) throws IOException {

        if ( TextUtils.isEmpty(logPath) || TextUtils.isEmpty(filename) )
            throw new IllegalArgumentException("wrong path of filename");

        logger = new Logger(logPath, filename);
    }

    public Logger(String logPath, String filename) throws IOException {
        File path = new File(logPath);
        path.mkdirs();

        File resFile = new File(logPath + File.separator + filename);
        if ( !resFile.exists() )
            resFile.createNewFile();

        writer = new FileWriter( resFile, true );
    }

    public void logMsg( String[] messages ) throws IOException {
        StringBuilder builder = new StringBuilder();
        if ( messages.length != 0 )
        {
            for (int i = 0; i < messages.length; i++) {
                    builder.append(messages[i]).append(";");
            }
            writer.write(builder.append("\n").toString());
            writer.flush();
        }
    }

    public void closeLogger()
    {
        if ( writer != null  )
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
