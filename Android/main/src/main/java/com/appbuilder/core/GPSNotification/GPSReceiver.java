package com.appbuilder.core.GPSNotification;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GPSReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
    	boolean isRunning = false;
        String serviceName = context.getPackageName() + ".GPSNotification.GPSService";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceName.equals(service.service.getClassName())) {
            	isRunning = true;
            }
        }
        if(!isRunning){
            Intent service = new Intent(context, GPSService.class);
            context.startService(service);
        }
	}
}
