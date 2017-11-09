package com.appbuilder.core.PushNotification;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.appbuilder.core.AppBuilder;
import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.core.R;
import com.appbuilder.sdk.android.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class AppPushNotification extends Activity implements OnTouchListener{

	private int position = 0;
	float startPos = 0;

	private ArrayList<String> notifications = new ArrayList<String>();
	private TextView textViewNotification = null;
	private TextView textViewCounter = null;
	private LinearLayout notificationPanel = null;
    private RelativeLayout notificationMain;
    private Button btnClose;
    private Button btnApp;
    private String cachePath;
    private AppConfigure appConfig = null;
    private boolean sdAvailable = false;
	private ImageView btnNext = null;
	private ImageView btnPrev = null;
	
	final private int SLIDE_TO_RIGHT_START = 0;
	final private int SLIDE_TO_LEFT_START = 1;
	final private int SLIDE_COMPLETE = 2;

	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message message) {
			switch (message.what) { 
				case SLIDE_TO_LEFT_START:{
					if(position < notifications.size() - 1){
						position ++;
						//storePosition();
						slidePanel(-500);
					}
				} break;
				case SLIDE_TO_RIGHT_START:{
					if(position > 0){
						position --;
						//storePosition();
						slidePanel(500);
					}
				} break;
				case SLIDE_COMPLETE:{
					showNotification();
				} break;
			};
		}		
	};	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.push_notification_screen);

        // *****************************************************************************************
        // get UI links
        textViewNotification = (TextView)findViewById(R.id.push_notification);
        textViewNotification.setMovementMethod(new ScrollingMovementMethod());
        textViewCounter= (TextView)findViewById(R.id.push_notification_counter);
        notificationMain = (RelativeLayout)findViewById(R.id.notification_main);
        notificationPanel = (LinearLayout)findViewById(R.id.notification_panel);
        btnClose = (Button)findViewById(R.id.push_button_close);
        btnApp = (Button)findViewById(R.id.push_button_app);
        btnNext = (ImageView)findViewById(R.id.push_notification_next);
        btnPrev = (ImageView)findViewById(R.id.push_notification_prev);

        // *****************************************************************************************
        // checking for cache *.xml file
        if (!Utils.sdcardAvailable()) {
            sdAvailable = false;
        }else{
            sdAvailable = true;
        }

        // *****************************************************************************************
        // logic of slpash screen
        if(sdAvailable){
            cachePath = Environment.getExternalStorageDirectory() + "/AppBuilder/" + getPackageName();
            File cache = new File(cachePath + "/cache.data");
            if( cache.exists() ) {

                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new FileInputStream(cache));
                    appConfig = (AppConfigure) ois.readObject();
                    ois.close();
                } catch (StreamCorruptedException ex) {
                    Log.d("", "");
                } catch (IOException ex) {
                    Log.d("", "");
                } catch (ClassNotFoundException ex) {
                    Log.d("", "");
                }

                // show advertizement
                try
                {
                    if ( appConfig.getShowLink() )
                    {
                        // show ibuildapp splash screen
                        notificationMain.setBackgroundResource(R.drawable.splash_screen_ibuildapp);
                    } else
                    {
                        // show user splash screen
                        File tempFile = new File(cachePath +"/splash.jpg");
                        if ( tempFile.exists() )
                        {
                            Drawable splashDrawable = new BitmapDrawable(getResources(), cachePath +"/splash.jpg");
                            notificationMain.setBackgroundDrawable(splashDrawable);
                        }
                        else
                        {
                            notificationMain.setBackgroundResource(R.drawable.splash_screen_ibuildapp_paied);
                        }
                    }
                } catch ( Exception e )
                {
                    Log.d("","");
                }
            }
        }

        // *****************************************************************************************
        // notificatoin message render
		String path = Environment.getExternalStorageDirectory() + "/AppBuilder/" + getPackageName() + "/.notifications";
    	File file = new File(path);
    	if(file.exists()){
    		try{
            	ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            	notifications = (ArrayList<String>) ois.readObject();
                ois.close();
        	}catch(Exception e){}
            file.delete();

            btnClose.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View v) {
    				closeMessage();
    			}
            });

            btnApp.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View v) {
    				openApp();
    			}
                     });
            

            btnNext.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View v) {
    				handler.sendEmptyMessage(SLIDE_TO_LEFT_START);
    			}
            });
            

            btnPrev.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View v) {
    				handler.sendEmptyMessage(SLIDE_TO_RIGHT_START);
    			}
                     });
            
            
            textViewNotification.setText("");
            textViewCounter.setText("");
            if(notifications.size() > 0){
                if(Locale.getDefault().toString().equals("ru_RU")){
                    textViewCounter.setText((position + 1) + " из " + notifications.size());
                }else{
            	    textViewCounter.setText((position + 1) + " from " + notifications.size());
                }
            	textViewNotification.setText(notifications.get(position));
            	
            	if(notifications.size() > 1)
            		btnNext.setVisibility(View.VISIBLE);
            }
            notificationMain.setOnTouchListener(this);
    	}else{
    		finish();
    	}
    }
    
 
	@Override
	public boolean onTouch(View v, MotionEvent event){
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:{
				startPos = event.getX(); // event.getY()
			}break;
			case MotionEvent.ACTION_MOVE:{
			}break;
			case MotionEvent.ACTION_UP:{
				if(event.getX() - startPos > 90){
					handler.sendEmptyMessage(SLIDE_TO_RIGHT_START);
				}else if(startPos - event.getX() > 90){
					handler.sendEmptyMessage(SLIDE_TO_LEFT_START);
				}
			}break;
		}
		return true;
	}    
    
	private void slidePanel(int pos){
		TranslateAnimation slideToRight = new TranslateAnimation(0, pos, 0, 0);
		slideToRight.setFillAfter(true);
		slideToRight.setInterpolator(new LinearInterpolator());
		slideToRight.setDuration(500);   
		slideToRight.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation){
				handler.sendEmptyMessage(SLIDE_COMPLETE);
			}

			@Override
			public void	onAnimationRepeat(Animation animation){
			}

			@Override
			public void	onAnimationStart(Animation animation){
			}			
		});
		notificationPanel.startAnimation(slideToRight);
	}	
    
	private void showNotification(){
		if(notifications.size() > 1){
			if(position > 0){
				btnPrev.setVisibility(View.VISIBLE);
			}else{
				btnPrev.setVisibility(View.INVISIBLE);
			}
			if(position < notifications.size() - 1){
				btnNext.setVisibility(View.VISIBLE);
			}else{
				btnNext.setVisibility(View.INVISIBLE);
			}
		}
		
		textViewNotification.setText(notifications.get(position));

        if(Locale.getDefault().toString().equals("ru_RU")){
            textViewCounter.setText((position + 1) + " из " + notifications.size());
        }else{
            textViewCounter.setText((position + 1) + " from " + notifications.size());
        }
		notificationPanel.clearAnimation();
		notificationPanel.refreshDrawableState();
	}
	
	private void openApp(){
		Intent intent = new Intent(this, AppBuilder.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		finish();
    }
    
    private void closeMessage(){
    	finish();
    }
}
