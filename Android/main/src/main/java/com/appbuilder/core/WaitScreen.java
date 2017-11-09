package com.appbuilder.core;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
//import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;
import android.app.*;
//import android.content.pm.ActivityInfo;
//import android.graphics.PixelFormat;
import android.content.Intent;

public class WaitScreen extends Activity{
	
	private static ProgressDialog activityWaitDialog = null;
	
	private static WaitScreen mWaitScreen = null;
	private ProgressDialog waitDialog = null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        mWaitScreen = this;
        //Surface.setOrientation(getWindowManager().getDefaultDisplay().getDisplayId(), Surface.ROTATION_90);
		//getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       
	    setContentView(R.layout.wait);
        waitDialog = new ProgressDialog(this);
        waitDialog.setCancelable(false);
        waitDialog.setMessage(getString(R.string.load));
        waitDialog.getWindow().setGravity(Gravity.TOP);
        waitDialog.show();
	}
	
	public static synchronized void Remove() {
		if (WaitScreen.mWaitScreen != null) {
			WaitScreen.mWaitScreen.waitDialog.dismiss();
			WaitScreen.mWaitScreen.finish();
			WaitScreen.mWaitScreen = null;
		}
	}
	
	public static synchronized void Show(Activity holder) {
		Intent waitIntent = new Intent(holder, WaitScreen.class);
		holder.startActivity(waitIntent);
	}
	
	public static synchronized void ShowWait(Activity holder) {
		activityWaitDialog = new ProgressDialog(holder);
		activityWaitDialog.setCancelable(false);
		activityWaitDialog.getWindow().setGravity(Gravity.TOP);
		activityWaitDialog.show();
	}
	
	public static synchronized void DismissWait() {
		activityWaitDialog.dismiss();
		activityWaitDialog = null;
	}
	
}
