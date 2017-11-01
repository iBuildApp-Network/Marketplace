package com.appbuilder.core;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.appbuilder.core.rs.BlurBuilder;
import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.sdk.android.Utils;

import java.io.*;

public class SplashScreen extends Activity implements OnPostListener {

    private static final String TAG = "com.ibuildapp.core.SplashScreen";
    private final int SET_SPLASH = 1;
    private ProgressDialog progressDialog = null;
    private boolean sdAvailable;
    private Bitmap splashScreen = null;
    private AppConfigure appConfig = null;
    private ImageView imageSource;
    private ImageView imageBlured;
    private FrameLayout frameBackground;
    private boolean splashSet = false;
    private AnimationSet alpha;


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SET_SPLASH: {
                    setSplash();
                }
                break;
            }
        }
    };

    // *************************************************************************
    public void onPost(AppConfigure appConfig) {
        // show advertizement
        if (splashSet == false) {
            this.appConfig = appConfig;
            handler.sendEmptyMessage(SET_SPLASH);
        }
    }

    private void setSplash() {
        if (appConfig.getShowLink()) {
            // show ibuildapp splash screen
            frameBackground.setBackgroundResource(R.drawable.splash_screen_ibuildapp);
        } else {
            // show user splash screen
            File tempFile = new File(com.appbuilder.sdk.android.Statics.cachePath + "/splash.jpg");
            if (tempFile.exists()) {
                Drawable splashDrawable = new BitmapDrawable(getResources(), com.appbuilder.sdk.android.Statics.cachePath + "/splash.jpg");
                frameBackground.setBackgroundDrawable(splashDrawable);
            } else {
                frameBackground.setBackgroundResource(R.drawable.splash_screen_ibuildapp_paied);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Statics.addActivityIntefrace(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);

        frameBackground = (FrameLayout) findViewById(R.id.splash_layout);
        imageBlured = (ImageView) findViewById(R.id.blured_image);
        imageSource = (ImageView) findViewById(R.id.source_image);


        if (!TextUtils.isEmpty( getIntent().getStringExtra("splash") ))
        {
            Bitmap source = BitmapFactory.decodeFile(getIntent().getStringExtra("splash"));
            if ( source != null )
            {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {

                    // todo  blur
                    Bitmap blured = BlurBuilder.blur(SplashScreen.this, source);
                    imageBlured.setImageBitmap(blured);
                    imageSource.setImageBitmap(source);

                    alpha = (AnimationSet) AnimationUtils.loadAnimation(SplashScreen.this, R.anim.splash_alpha_animation);
                    alpha.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            imageBlured.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    imageBlured.clearAnimation();
                    imageBlured.startAnimation(alpha);

                    splashSet = true;
                    return;
                } else
                    imageSource.setImageBitmap(source);
            }
        }

        showProgress();
        // checking for cache *.xml file
        if (!Utils.sdcardAvailable()) {
            sdAvailable = false;
        } else {
            sdAvailable = true;
        }

        if (sdAvailable) {
            File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.data");
            if (cache.exists()) {

                ObjectInputStream ois;
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

                try {
                    // show advertizement
                    if (appConfig.getShowLink()) {
                        // show ibuildapp splash screen
                        frameBackground.setBackgroundResource(R.drawable.splash_screen_ibuildapp);
                    } else {
                        // show user splash screen
                        File tempFile = new File(com.appbuilder.sdk.android.Statics.cachePath + "/splash.jpg");
                        if (tempFile.exists()) {
                            Drawable splashDrawable = new BitmapDrawable(getResources(), com.appbuilder.sdk.android.Statics.cachePath + "/splash.jpg");
                            frameBackground.setBackgroundDrawable(splashDrawable);
                        } else {
                            frameBackground.setBackgroundResource(R.drawable.splash_screen_ibuildapp_paied);
                        }
                    }

                } catch (Exception e) {
                    LogError(e);
                }
            }
        }
        splashSet = true;
    }

    private void LogError(Exception e) {
        Log.e(TAG, "", e);
    }

    @Override
    public void onPause() {
        if ( progressDialog != null )
            progressDialog.hide();
        super.onPause();
    }

    @Override
    public void onStop() {
        if ( progressDialog != null )
            progressDialog.dismiss();
        super.onStop();
    }

    private void showProgress()
    {
        // show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.load));
        progressDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL);
        progressDialog.show();
    }
}


