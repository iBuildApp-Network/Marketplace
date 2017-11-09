package com.appbuilder.core.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.appbuilder.core.PushNotification.services.PushNotificationService;

/**
 *
 * @author Roman Black
 */
public class SystemReceiver extends BroadcastReceiver{
    
    public static final String ACTION_PUSH_SERVICE_STOPPED = "push_service_stopped";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ACTION_PUSH_SERVICE_STOPPED.equals(intent.getAction())){
            PushNotificationService.writeToLog("push service stopped recieved");
            
            PushNotificationService.startPushPolling(context);
        }
    }
    
}
