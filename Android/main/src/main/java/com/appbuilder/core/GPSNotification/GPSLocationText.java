package com.appbuilder.core.GPSNotification;

import com.appbuilder.core.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class GPSLocationText extends Activity{
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.gps_text);
        
        Button btnClose = (Button)findViewById(R.id.gps_button_close);
        btnClose.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
        });

        GPSItem gpsItem = null;
        try{
        	gpsItem = (GPSItem)getIntent().getSerializableExtra("gpsNotificationData");
        }catch(Exception e){}
		
        if(gpsItem != null){
        	TextView distance = (TextView)findViewById(R.id.gps_notification_distance);
        	distance.setText(gpsItem.getDistance() + " m");

        	TextView title = (TextView)findViewById(R.id.gps_notification_title);
        	title.setText(gpsItem.getTitle());
        	
        	TextView description = (TextView)findViewById(R.id.gps_notification_description);
        	description.setText(gpsItem.getDescription());
        }else{
        	finish();
        }
    }
}
