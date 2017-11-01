package com.appbuilder.core.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.appbuilder.core.PushNotification.services.PushNotificationService;

/**
 *
 * @author Roman Black
 */
public class BootReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        PushNotificationService.writeToLog("device booted");
        
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            com.appbuilder.sdk.android.Statics.isOnline = true;
        }else{
            com.appbuilder.sdk.android.Statics.isOnline = false;
        }
        
        if(com.appbuilder.sdk.android.Statics.isOnline){
            PushNotificationService.startPushPolling(context);
        }
    }
    
}
