package com.appbuilder.core.PushNotification.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.appbuilder.core.BuildConfig;
import com.appbuilder.core.PushNotification.AppPushNotificationReceiver;
import com.appbuilder.core.system.SystemReceiver;
import com.appbuilder.sdk.android.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import org.json.JSONObject;

/**
 *
 * @author Roman Black
 */
public class PushNotificationService extends Service {
    
    public static final String EXTRA_COMMAND = "command";
    
    public static final String COMMAND_START_POLLING = "start_polling";
    public static final String COMMAND_STOP_POLLING = "stop_polling";
    
    private static final String PREFERENCES_NAME = "push_prefs";
    private static final String PREFERENCE_LAST_MODIFIED = "last_modified";
    
    private static volatile boolean threadStarted = false;
    
    private final static String TAG = "PUSH_NOTIFICATION_SERVICE";
    private final static String endpoint = "http://54.89.221.183:80/lp?channels=";
    
    private static Thread pollingThread;
    
    private final Handler handler = new MainHandler();

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "service onBind");

        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.e(TAG, "service onStart");

        super.onStart(intent, startId); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "service onStartCommand");
        writeToLog("service onStartCommand");
        
        String extraCommand = null; 
        if(intent != null){
            extraCommand = intent.getStringExtra(EXTRA_COMMAND);
        }
        
        if(extraCommand != null){
            if(extraCommand.equalsIgnoreCase(COMMAND_START_POLLING)){
                writeToLog("service command start_polling");
                
                if (!threadStarted) {
                    startThread();
                }
            }else if(extraCommand.equalsIgnoreCase(COMMAND_STOP_POLLING)){
                writeToLog("service command stop_polling");
                
                stopThread();
            }
        }else{
            writeToLog("service command start_polling");

            if (!threadStarted) {
                startThread();
            }
        }

        return super.onStartCommand(intent, flags, startId); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "service onDestroy");
        writeToLog("service onDestroy");

        if (threadStarted) {
            stopThread();
        }
        
        Intent it = new Intent(SystemReceiver.ACTION_PUSH_SERVICE_STOPPED);
        sendBroadcast(it);

        super.onDestroy(); //To change body of generated methods, choose Tools | Templates.
    }

    public void startThread() {
        if (!threadStarted) {
            String channelUrl = endpoint + "p" + 
                    Utils.md5(com.appbuilder.core.AppBuilder.DOMEN + com.appbuilder.core.AppBuilder.APP_ID);
            
            //Log.e(TAG, channelUrl);
            
            pollingThread = new PollingThread(this.getApplicationContext(), channelUrl,// +
                    //"&time=" + URLEncoder.encode("Thu, 04 Sep 2014 12:07:20 GMT"), 
                    handler);
            pollingThread.start();
        }
    }

    public void stopThread() {
        if (pollingThread != null) {
            pollingThread.interrupt();
            try {
                pollingThread.join();
            } catch (Exception ex) {
                Log.e(TAG, "Thread.join() failed", ex);
            }
        }

        pollingThread = null;
        threadStarted = false;
    }
    
    public static void stopPushPolling(Context ctx){
        Intent it = new Intent(ctx.getApplicationContext(), PushNotificationService.class);
        it.putExtra(PushNotificationService.EXTRA_COMMAND, PushNotificationService.COMMAND_STOP_POLLING);
        ctx.startService(it);
    }
    
    public static void startPushPolling(Context ctx){
        Intent it = new Intent(ctx.getApplicationContext(), PushNotificationService.class);
        it.putExtra(PushNotificationService.EXTRA_COMMAND, PushNotificationService.COMMAND_START_POLLING);
        ctx.startService(it);
    }

    public static boolean isThreadStarted() {
        return threadStarted;
    }

    private class MainHandler extends Handler {

        public static final int MESSAGE_RECEIVED = 0;
        public static final int RESTART = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVED:
                    Intent it = new Intent(AppPushNotificationReceiver.ACTION);
                    
                    HandlerObject obj = (HandlerObject)msg.obj;
                    
                    if(obj.descr != null && obj.descr.length() > 0){
                        it.putExtra("descr", obj.descr);
                    }
                    
                    if(obj.image != null && obj.image.length() > 0){
                        it.putExtra("image", obj.image);
                    }
                    
                    if(obj.message != null && obj.message.length() > 0){
                        it.putExtra("message", obj.message);
                    }
                    
                    if(obj.order != null && obj.order.length() > 0){
                        it.putExtra("order", obj.order);
                    }
                    
                    if(obj.package_ != null && obj.package_.length() > 0){
                        it.putExtra("package", obj.package_);
                    }
                    
                    if(obj.title != null && obj.title.length() > 0){
                        it.putExtra("title", obj.title);
                    }
                    
                    if(obj.type != null && obj.type.length() > 0){
                        it.putExtra("type", obj.type);
                    }
                    
                    getApplicationContext().sendBroadcast(it);
                    Log.e(TAG, "broadcast sent");
                    break;
                case RESTART:
                    Log.e(TAG, "restart");
                    
                    writeToLog("restart");
                    
                    //startThread();
                    startPushPolling(getApplicationContext());
                    break;
            }
        }
    }

    private static class PollingThread extends Thread {

        public PollingThread(Context context, String url, Handler handler) {
            this.urlString = url;
            this.handler = handler;
            this.context = context;
        }
        
        private int failedCount = 0;
        
        private String urlString;
        private HttpURLConnection httpConnection;
        
        private Context context;
        private Handler handler;

        @Override
        public void run() {
            threadStarted = true;
            
            writeToLog("polling thread started");

            while (!isInterrupted()) {
                try {
                    //Log.e(TAG, "new poll request --------------------------------------------------------------");
                    writeToLog("new poll request ----------" + new Date().toString());
                    
                    SharedPreferences pref = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
                    String lUpString = pref.getString(PREFERENCE_LAST_MODIFIED, null);

                    URL url;
                    
                    if(lUpString == null){
                        url = new URL(urlString);
                    }else{
                        url = new URL(urlString + "&time=" + URLEncoder.encode(lUpString, "utf-8").replace("+", "%20"));
                    }
                    
                    //Log.e(TAG, url.toString());
                    
                    URLConnection conn = url.openConnection();

                    httpConnection = (HttpURLConnection) conn;
                    httpConnection.setConnectTimeout(660000);
                    httpConnection.setReadTimeout(660000);

                    InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

                    BufferedReader br = new BufferedReader(streamReader);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    
                    int bracketsCount = 0;
                    
                    failedCount = 0;
                    
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    br.close();
                    String resp = sb.toString();
                    
                    try {
                        if (resp.length() > 0) {
                            JSONObject json = new JSONObject(resp);

                            HandlerObject obj = new HandlerObject();

                            obj.id = json.getString("id");

                            try{
                                obj.descr = json.getString("descr");
                            }catch(Exception ex){
                            }

                            try{
                                obj.image = json.getString("image");
                            }catch(Exception ex){
                            }

                            try{
                                obj.message = json.getString("message");
                            }catch(Exception ex){
                            }

                            try{
                                obj.order = json.getString("order");
                            }catch(Exception ex){
                            }

                            try{
                                obj.package_ = json.getString("package");
                            }catch(Exception ex){
                            }

                            try{
                                obj.title = json.getString("title");
                            }catch(Exception ex){
                            }

                            try{
                                obj.type = json.getString("type");
                            }catch(Exception ex){
                            }

                            if(PushDatabaseHelper.saveNewMessage(context, obj) && handler != null){
                                handler.sendMessage(handler.obtainMessage(MainHandler.MESSAGE_RECEIVED, obj));
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "JSON parsing failed", ex);
                    }
                    
                    String luString = conn.getHeaderField("Last-Modified");
                    
                    if(luString != null){
                        SharedPreferences sPref = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sPref.edit();
                        editor.putString(PREFERENCE_LAST_MODIFIED, luString);
                        editor.commit();
                    }
                    
                    Log.e("", "");
                    
                    /*while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                        
                        bracketsCount = bracketsCount + (line.length() - line.replace("{", "").length());
                        bracketsCount = bracketsCount - (line.length() - line.replace("}", "").length());
                        
                        if(bracketsCount == 0){
                            String resp = sb.toString();
                            
                            try {
                                if (resp.length() > 0) {
                                    JSONObject json = new JSONObject(resp);
                                    
                                    HandlerObject obj = new HandlerObject();
                                    
                                    obj.id = json.getString("id");
                                    
                                    try{
                                        obj.descr = json.getString("descr");
                                    }catch(Exception ex){
                                    }
                                    
                                    try{
                                        obj.image = json.getString("image");
                                    }catch(Exception ex){
                                    }
                                    
                                    try{
                                        obj.message = json.getString("message");
                                    }catch(Exception ex){
                                    }
                                    
                                    try{
                                        obj.order = json.getString("order");
                                    }catch(Exception ex){
                                    }
                                    
                                    try{
                                        obj.package_ = json.getString("package");
                                    }catch(Exception ex){
                                    }
                                    
                                    try{
                                        obj.title = json.getString("title");
                                    }catch(Exception ex){
                                    }
                                    
                                    try{
                                        obj.type = json.getString("type");
                                    }catch(Exception ex){
                                    }

                                    if(PushDatabaseHelper.saveNewMessage(context, obj) && handler != null){
                                        handler.sendMessage(handler.obtainMessage(MainHandler.MESSAGE_RECEIVED, obj));
                                    }
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "JSON parsing failed", ex);
                            }

                            sb.setLength(0);
                        }
                    }*/
                    //br.close();
                    
                    writeToLog("poll request ends <<<<<<<" + new Date().toString());

                } catch (Exception ex) {
                    failedCount++;
                    
                    if(failedCount > 10){
                        if(handler != null){
                            handler.sendEmptyMessageDelayed(MainHandler.RESTART, 600000); // 10 * 60 * 1000
                        }
                        break;
                    }
                    
                    Log.e(TAG, "poll failed", ex);
                    
                    writeToLog("poll request failed" + ex);
                }
            }

            threadStarted = false;
            this.handler = null;
            this.context = null;
            
            writeToLog("polling thread stopped");
        }

        @Override
        public void interrupt() {
            super.interrupt(); //To change body of generated methods, choose Tools | Templates.
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.putExtra(EXTRA_COMMAND, COMMAND_START_POLLING);
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }
    
    public static class HandlerObject{
        String id;
        String type;
        String message;
        String order;
        String title;
        String descr;
        String image;
        String package_;
    }
    
    public static void writeToLog(String str){
        if(BuildConfig.DEBUG){
            String logPath = Environment.getExternalStorageDirectory() + "/push.log";
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(logPath), true));
                Date d = new Date();d.setTime(System.currentTimeMillis());
                bw.append(d.toLocaleString() + " : " + str + "\n");
                bw.close();
            }catch(Exception e){
            }
        }
    }
    
}
