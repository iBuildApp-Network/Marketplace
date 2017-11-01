/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appbuilder.core.PushNotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *
 * @author minberg
 */
public class CustomNotificationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        AppPushNotificationReceiver.messageReceived(context, intent);
    }
    
}
