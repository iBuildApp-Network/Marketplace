package com.appbuilder.core.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.appbuilder.core.PushNotification.services.PushNotificationService;

/**
 *
 * @author Roman Black
 */
public class WifiStateReceiver extends BroadcastReceiver{
    
    private static final String TAG = "WIFI_STATE_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        /*int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                WifiManager.WIFI_STATE_UNKNOWN);

        switch (extraWifiState) {
            case WifiManager.WIFI_STATE_DISABLED: {
            }
            break;
            case WifiManager.WIFI_STATE_DISABLING: {
                com.appbuilder.sdk.android.Statics.isOnline = false;
                PushNotificationService.stopPushPolling(context);
            }
            break;
            case WifiManager.WIFI_STATE_ENABLED: {
                com.appbuilder.sdk.android.Statics.isOnline = true;
                PushNotificationService.startPushPolling(context);
            }
            break;
            case WifiManager.WIFI_STATE_ENABLING: {
            }
            break;
            case WifiManager.WIFI_STATE_UNKNOWN: {
            }
            break;
        }*/
        
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        
        if(ni == null){
            PushNotificationService.writeToLog("internet disconnected");
            
            com.appbuilder.sdk.android.Statics.isOnline = false;
            PushNotificationService.stopPushPolling(context);
        }else{
            PushNotificationService.writeToLog("internet connected");
            PushNotificationService.writeToLog("connection type: " + ni.getTypeName());
            
            Log.e(TAG, "type : " + ni.getTypeName());
            Log.e(TAG, "connectted : " + ni.isConnected() + " " + ni.isConnectedOrConnecting());
            
            switch(ni.getType()){
                case ConnectivityManager.TYPE_MOBILE:
                    com.appbuilder.sdk.android.Statics.isOnline = true;
                    PushNotificationService.startPushPolling(context);
                    break;
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_WIMAX:
                    com.appbuilder.sdk.android.Statics.isOnline = true;
                    PushNotificationService.startPushPolling(context);
                    break;
            }
        }
        
        Log.d(TAG, "ok");
    }
    
}
