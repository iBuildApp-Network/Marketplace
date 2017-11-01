package com.appbuilder.core.GPSNotification;

import android.app.*;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.core.R;

import java.io.*;
import java.util.Date;
import java.util.HashMap;

public class GPSService extends IntentService implements LocationListener{
    private String TAG = "GPSService";

    private LocationManager locationManager = null;
    private Location locationBefore = null;
    private long timeBefore = 0;
    private int counter = 0;

    private AppConfigure appConfig = null;
    private boolean isRunning = false;
    private boolean isListen = false;
    private String cachePath = "";

    private HashMap<Integer, GPSItem> notifications = new HashMap<Integer, GPSItem>();

    private String notificationBarTitle = "";
    private String notificationTitle = "";
    private String notificationText = "";

    final private int LOCATION_LISTENER_START = 0;
    final private int LOCATION_LISTENER_STOP = 1;
    final private int LOCATION_LISTENER_ERROR = 2;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case LOCATION_LISTENER_START:{
                    startLocationListener(0, 0);
                }break;
                case LOCATION_LISTENER_STOP:{
                    stopLocationListener();
                }break;
                case LOCATION_LISTENER_ERROR:{
                    isRunning = false;
                }break;
            }
        }
    };

    public GPSService() {
        super("GPSService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "Service started " + this.getPackageName(), Toast.LENGTH_LONG).show();
        isRunning = true;
        cachePath = Environment.getExternalStorageDirectory() + "/AppBuilder/" + getPackageName() + "/cache.data";
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager == null){
            isRunning = false;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
        isRunning = false;
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        notificationBarTitle = getResources().getString(R.string.app_name);
        notificationTitle = getResources().getString(R.string.app_name);

        writeToLog("\n\n");

        while (isRunning) {
            synchronized (this) {
                try {
                    File cache = new File(cachePath);
                    if(!cache.exists()){
                        isRunning = false;
                    }else{
                        try{
                            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
                            appConfig = (AppConfigure) ois.readObject();
                            ois.close();
                        }catch(Exception e){
                            isRunning = false;
                        }

                        for(Integer key: notifications.keySet()){
                            boolean isExist = false;
                            for(int j = 0; j < appConfig.getGPSNotifications().size(); j ++){
                                GPSItem item = appConfig.getGPSNotifications().get(j);
                                if(notifications.get(key).getLatitude() == item.getLatitude()
                                    && notifications.get(key).getLongitude() == item.getLongitude()){
                                    isExist = true;
                                    break;
                                }
                            }
                            if(isExist == false){
                                GPSItem item = notifications.get(key);
                                int notificationId = (int)(item.getLatitude()*1E6 + item.getLongitude()*1E6);
                                notifications.remove(key);
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(notificationId);
                            }
                        }
                    }

                    if(appConfig.getGPSNotifications().size() > 0){
                        if(!isListen){
                            handler.sendEmptyMessage(LOCATION_LISTENER_START);
                        }
                        
                        wait(5 * 60 * 1000);
                    }else{
                        isRunning = false;
                    }

                    if(!isRunning){
                        handler.sendEmptyMessage(LOCATION_LISTENER_STOP);
                    }
      
                } catch (Exception e) {	
                    Log.w(TAG, e);
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        int minDistance = 100000000;

        for(int i = 0; i < appConfig.getGPSNotifications().size(); i ++){
            GPSItem item = appConfig.getGPSNotifications().get(i);

            Location point = new Location(item.getDescription());
            point.setLatitude(item.getLatitude());
            point.setLongitude(item.getLongitude());
            item.setDistance((int)location.distanceTo(point));

            int notificationId = (int)(item.getLatitude()*1E6 + item.getLongitude()*1E6);
            if(item.getDistance() < item.getRadius()){

                if(minDistance > (item.getRadius() - item.getDistance())){
                    minDistance = (item.getRadius() - item.getDistance());
                }

                notifications.put(notificationId, item);

                Bundle store = new Bundle();
                store.putString("gpsNotificationMessage", "hasMessage");
                store.putSerializable("gpsNotificationData", item);
                store.putDouble("srcLatitude", location.getLatitude());
                store.putDouble("srcLongitude", location.getLongitude());

                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                boolean isOnline = false;
                if (ni != null && ni.isConnectedOrConnecting()){
                    isOnline = true;
                }

                Intent intent;
                if(isOnline){
                    intent = new Intent(this, GPSLocationMap.class);
                }else{
                    intent = new Intent(this, GPSLocationText.class);
                }
                    
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtras(store);

                notificationText = item.getDescription();

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                PendingIntent pendingIntent = PendingIntent.getActivity(this.getBaseContext(), notificationId, intent, 0);
              //  notification.setLatestEventInfo(this, notificationTitle, notificationText, pendingIntent);

                notificationManager.notify(notificationId, notification);					
            }else{
                if(minDistance > item.getDistance() - item.getRadius()){
                    minDistance = item.getDistance() - item.getRadius();
                }

                for(Integer key: notifications.keySet()){
                    if(notifications.get(key).getLatitude() == item.getLatitude() && notifications.get(key).getLongitude() == item.getLongitude()){
                        notifications.remove(key);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(key.intValue());
                    }
                }
            }

            writeToLog("Found location lon:" + location.getLongitude() + " lat:" + location.getLatitude() + " distance: " + item.getDistance() + " to " + item.getTitle());
        }

        long delay = 5 * 60 * 1000;;
        if(locationBefore == null){
            locationBefore = location;
            timeBefore = System.currentTimeMillis();
        }else{
            float distance = location.distanceTo(locationBefore);
            float speed = ((distance / 1000)/((float)(System.currentTimeMillis() - timeBefore) / (60 * 60 * 1000))); //km/hour

            delay = (long)((((float)minDistance / 1000) / speed) * (60 * 60 * 1000));
            if(speed < 1){
                counter ++;
            }else{
                counter = 0;
            }

            writeToLog("Distance from last location: " + distance + " speed: " + speed + " km/h");
            writeToLog("near location: " + minDistance + " next request: " + delay + " counter:" + counter);

            if(counter == 1){
                delay = 5 * 60 * 1000;
            }else if(counter == 2){
                delay = 15 * 60 * 1000;
            }else if(counter > 2){
                delay = 30 * 60 * 1000;
            }

            writeToLog("next request: " + delay + " counter:" + counter);

            if(delay < (5 * 60 * 1000)){
                delay = 5 * 60 * 1000;
            }
            if(delay > 30 * 60 * 1000){
                delay = 30 * 60 * 1000;
            }
        }

        writeToLog("Next request trough: " + delay);

        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
        
        handler.sendEmptyMessageDelayed(LOCATION_LISTENER_START, delay);
    }

    @Override
    public void onProviderDisabled(String provider) {
        writeToLog("GPS provider disabled");
        isListen = true;
        handler.sendEmptyMessage(LOCATION_LISTENER_STOP);
    }

    @Override
    public void onProviderEnabled(String provider) {
        writeToLog("GPS provider enabled");
        isListen = false;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        writeToLog("GPS provider status: " + status);
    }

    /* PRIVATE METHODS */
    private void startLocationListener(int timeout, int distance){

    	boolean isRunning = false;
        String serviceName = this.getPackageName() + ".GPSNotification.GPSService";
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceName.equals(service.service.getClassName())) {
                isRunning = true;
            }
        }
        if(isRunning){
            writeToLog("Start Location Listener");
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeout, distance, this);
                isListen = true;
            }
        }else{
            stopLocationListener();
        }
    }

    private void stopLocationListener(){
        writeToLog("Stop Location Listener");
        if(locationManager != null){
            locationManager.removeUpdates(this);
            isListen = false;
        }
    }

    private void writeToLog(String str){
        String logPath = Environment.getExternalStorageDirectory() + "/gpsservice.log";
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(logPath), true));
            Date d = new Date();d.setTime(System.currentTimeMillis());
            bw.append(d.toLocaleString() + " : " + str + "\n");
            bw.close();
        }catch(Exception e){}
        /**/
	}
}
