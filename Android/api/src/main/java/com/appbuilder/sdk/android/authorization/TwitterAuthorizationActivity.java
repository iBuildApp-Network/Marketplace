/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.appbuilder.sdk.android.authorization;

import android.app.AlarmManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.*;
import android.widget.LinearLayout;
import com.appbuilder.sdk.android.AppBuilderModule;
import com.appbuilder.sdk.android.CurrentTime;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author minberg
 */
public class TwitterAuthorizationActivity extends AppBuilderModule {

    private static final String CALLBACK_URL = "twitterapp://connect";

    private final String TAG = TwitterAuthorizationActivity.class.getCanonicalName();

    private final int HIDE_PROGRESS_DIALOG = 0;
    private final int PROCESS_TOKEN = 1;
    private final int POST_INITIALIZATION = 2;
    private final int SHOW_PROGRESS_DIALOG = 3;
    private final int ACTIVITY_CLOSE_OK = 4;
    private final int ACTIVITY_CLOSE_BAD = 5;

    private String twitterConsumerKey = com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_KEY;
    private String twitterSecretKey = com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_SECRET;

    public final static String ADV_TWITTER_CONSUMER_KEY = "p48aBftV8vXXfG6UWo0BcQ";//"szTSJaoSHMviPwfdb1PDCg";
    public final static String ADV_TWITTER_CONSUMER_SECRET = "YYkHCKtSD7uYhSC3jtPL1H2b6NaX2u6x5kOLLgRUA";//"nv4PGlEDLKVgxzGoPJ7uumWzYW2eMYuxo9XtLWNbM";

    private String twitpic_api_key = "cd457da64a01a64105a6db68728ea1a6";

    private String authUrl = "";
    private String url = "";

    private CommonsHttpOAuthConsumer mHttpOauthConsumer;
    private CommonsHttpOAuthProvider mHttpOauthprovider;
    private AccessToken mAccessToken;

    private Twitter mTwitter;

    private com.appbuilder.sdk.android.authorization.entities.User fwUser = null;

//    private Twitter mTwitter;
    //  private AccessToken mAccessToken;
    //private CommonsHttpOAuthConsumer mHttpOauthConsumer;
//    private CommonsHttpOAuthProvider mHttpOauthprovider;

    private WebView webView = null;
    private ProgressDialog progressDialog = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_PROGRESS_DIALOG: {
                    hideProgressDialog();
                }
                break;
                case SHOW_PROGRESS_DIALOG: {
                    showProgressDialog();
                }
                break;
                case PROCESS_TOKEN: {
                    processToken(url);
                }
                break;

                case POST_INITIALIZATION: {
                    webView.loadUrl(authUrl);
                }
                break;

                case ACTIVITY_CLOSE_OK: {
                    closeActivityWithOkResult();
                }
                break;

                case ACTIVITY_CLOSE_BAD: {
                    closeActivityWithBadResult();
                }
                break;
            }
//            mProgressDlg.dismiss();

            /*if (msg.what == 1) {
                if (msg.arg1 == 1){
                    //Error//mListener.onError("Error getting request token");
                }else{
                    //Error//mListener.onError("Error getting access token");
                }
            } else {
                if (msg.arg1 == 1){
                    showLoginDialog((String) msg.obj);
                }else{
  //                  mListener.onComplete("");
                }
            }*/
        }
    };
    /*private final int LOGIN_SUCCESS = 1;
    private final int CLOSE_ACTIVITY = 2;
    
    private TwitterApp mTwitter;
    
    private String twitter_consumer_key = *//*"3Cmvm09oN7Vgrec32D1FLg";*//*"szTSJaoSHMviPwfdb1PDCg";
    private String twitter_secret_key = *//*"henbtitGc35W7PgyQT9Q6S8I5C1n82vqr2pY0e4o";*//*"nv4PGlEDLKVgxzGoPJ7uumWzYW2eMYuxo9XtLWNbM";
    private String twitpic_api_key = "cd457da64a01a64105a6db68728ea1a6";
    
    private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
        @Override
        public void onComplete(String value) {
            String username = mTwitter.getUsername();
            username = (username.equals("")) ? "No Name" : username;
            handler.sendEmptyMessage(LOGIN_SUCCESS);
        }
		
        @Override
        public void onError(String value) {
            Log.d("", "");
            handler.sendEmptyMessage(CLOSE_ACTIVITY);
        }
    };
    
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch(msg.what){
                case LOGIN_SUCCESS:{
      //              loginSuccess();
                }break;
                case CLOSE_ACTIVITY:{
                    closeActivity();
                }break;
            }
        }
        
    };*/

    @Override
    public void create() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.e("TRACE", new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "TwitterAuth");

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mainLayout.setLayoutParams(lp);

        webView = new WebView(this);
        webView.setLayoutParams(lp);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        mainLayout.addView(webView);

        //setContentView(R.layout.romanblack_photogallery_facebook_auth);
        setContentView(mainLayout);

        if (com.appbuilder.sdk.android.Statics.showLink) {
            twitterConsumerKey = ADV_TWITTER_CONSUMER_KEY;
            twitterSecretKey = ADV_TWITTER_CONSUMER_SECRET;
        }

        //webView = (WebView)findViewById(R.id.romanblack_photogallery_facebook_auth_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                    }
                    break;
                }
                return false;
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e(TAG, new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "Started Url = " + url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                super.shouldOverrideUrlLoading(view, url);
                //Log.d(TAG, "Redirecting URL " + url);
                Log.e(TAG, new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "shouldOverrideUrlLoading  = " + url);

                if (url.startsWith(CALLBACK_URL)) {
                    TwitterAuthorizationActivity.this.url = url;

                    mHandler.sendEmptyMessage(PROCESS_TOKEN);

                    //mListener.onComplete(url);

                    //TwitterDialog.this.dismiss();

                    return true;
                } else if (url.startsWith("authorize")) {
                    return false;
                }

                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "Error code = " + errorCode + " descr = " + description);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mHandler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                Log.e(TAG, new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "finished Url = " + url);
            }
        });

        // так как postInitializatin() занимает около 3 секунд, вынесем его в отдельный поток, чтобы успел показаться progress dialog
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                postInitializatin();
                mHandler.sendEmptyMessage(POST_INITIALIZATION);
            }
        }).start();

    }

    // инициализация объектов твиттера и загрузка данных в webview
    private void postInitializatin() {
        mTwitter = new TwitterFactory().getInstance();

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        //запрашиваем текущее время с веб сервиса
        long timestamp = CurrentTime.requestTime();

        //вычисляем разницу в часах со временем на смартфоне
        int zone;
        if (timestamp > 0) {
            zone = (int) ((timestamp - System.currentTimeMillis())/1000.0/3600.0);
        } else {
            zone = 0;
        }

        //выставляем автоопределение времени
        Settings.System.putInt(getContentResolver(), Settings.System.AUTO_TIME, 1);

        TimeZone tz = TimeZone.getDefault();

        int newZone = ((int) (tz.getRawOffset()/1000.0/3600.0)) - zone;

        String firstDigit = (newZone < 10)?"0":"";
        String sign = (newZone < 0)?"-":"+";
        String GMTString = "GMT" + sign + firstDigit + newZone + "00";

        am.setTimeZone(GMTString);

        Log.e( TAG, "twitterConsumerKey = "+ twitterConsumerKey + " twitterSecretKey = " + twitterSecretKey);
        mHttpOauthConsumer = new CommonsHttpOAuthConsumer(twitterConsumerKey, twitterSecretKey);
        mHttpOauthprovider = new /*DefaultOAuthProvider*/CommonsHttpOAuthProvider("https://twitter.com/oauth/request_token",
                "https://twitter.com/oauth/access_token",
                "https://twitter.com/oauth/authorize");

        try {
            authUrl = mHttpOauthprovider.retrieveRequestToken(mHttpOauthConsumer, CALLBACK_URL);
            Log.e(TAG, new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "finished AuthUrl = " + authUrl);
        } catch (OAuthCommunicationException oACEx) {
            Log.e(TAG, oACEx.getMessage());
        } catch (OAuthMessageSignerException oAMSEx) {
            Log.e(TAG, oAMSEx.getMessage());
        } catch (OAuthNotAuthorizedException oANAEx) {
            Log.e(TAG, oANAEx.getMessage());
        } catch (OAuthExpectationFailedException oAEFEx) {
            Log.e(TAG, oAEFEx.getMessage());
        }

        //webView.loadUrl(authUrl);
        /*mTwitter = new TwitterFactory().getInstance();

        mHttpOauthConsumer = new CommonsHttpOAuthConsumer(twitterConsumerKey, twitterSecretKey);
        mHttpOauthprovider = new CommonsHttpOAuthProvider("http://twitter.com/oauth/request_token",
                "http://twitter.com/oauth/access_token",
                "http://twitter.com/oauth/authorize");

        mAccessToken = new AccessToken(twitterConsumerKey, twitterSecretKey);

        configureToken();*/
        /*mTwitter = new TwitterApp(this, twitter_consumer_key,twitter_secret_key);
        mTwitter.setListener(mTwLoginDialogListener);
        auth();*/
    }

    @Override
    public void destroy() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        super.destroy(); //To change body of generated methods, choose Tools | Templates.
    }

    public void processToken(String callbackUrl) {
        //mProgressDlg.setMessage("Finalizing ...");
        //mProgressDlg.show();
        final String verifier = getVerifier(callbackUrl);

        new Thread() {
        @Override
        public void run() {

        try {
            mHttpOauthprovider.retrieveAccessToken(mHttpOauthConsumer, verifier);

            mAccessToken = new AccessToken(mHttpOauthConsumer.getToken(), mHttpOauthConsumer.getTokenSecret());

            mTwitter.setOAuthConsumer(twitterConsumerKey, twitterSecretKey);

            mTwitter.setOAuthAccessToken(mAccessToken);
            //configureToken();

                    User user = mTwitter.verifyCredentials();

                    fwUser = new com.appbuilder.sdk.android.authorization.entities.User();
                    fwUser.setUserName(user.getName());
                    //fwUser.setUserLastName(user.getLastName());
                    fwUser.setAvatarUrl(user.getOriginalProfileImageURL());
                    //fwUser.setAvatarUrl("https://api.twitter.com/1/users/show.xml?screen_name=" + user.getName());
                    fwUser.setAccountId(user.getId() + "");
                    fwUser.setAccountType("twitter");
                    fwUser.setAccessToken(mHttpOauthConsumer.getToken());
                    fwUser.setAccessTokenSecret(mHttpOauthConsumer.getTokenSecret());
                    fwUser.setConsumerKey(mHttpOauthConsumer.getConsumerKey());
                    fwUser.setConsumerSecret(mHttpOauthConsumer.getConsumerSecret());
                    //fwUser.setConsumerKey(twitterConsumerKey);
                    //fwUser.setConsumerSecret(twitterSecretKey);

                    Authorization.twitterUser = fwUser;
                    if (Authorization.primaryUser == null) {
                        Authorization.primaryUser = fwUser;
                    }

                    mHandler.sendEmptyMessage(ACTIVITY_CLOSE_OK);

            // mSession.storeAccessToken(mAccessToken, user.getName());

                } catch (Exception e) {

                    mHandler.sendEmptyMessage(ACTIVITY_CLOSE_BAD);
                    //Log.d(TAG, "Error getting access token");

                    //e.printStackTrace();
                }

        //mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
        }
        }.start();
    }

    private String getVerifier(String callbackUrl) {
        String verifier = "";

        try {
            callbackUrl = callbackUrl.replace("twitterapp", "http");

            URL url = new URL(callbackUrl);
            String query = url.getQuery();

            String array[] = query.split("&");

            for (String parameter : array) {
                String v[] = parameter.split("=");

                if (URLDecoder.decode(v[0]).equals(oauth.signpost.OAuth.OAUTH_VERIFIER)) {
                    verifier = URLDecoder.decode(v[1]);
                    break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return verifier;
    }

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, "Loading...");
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void closeActivityWithOkResult() {
        hideProgressDialog();

        Intent it = new Intent();
        it.putExtra("user", fwUser);
        setResult(RESULT_OK, it);

        finish();
    }

    private void closeActivityWithBadResult() {
        hideProgressDialog();

        setResult(RESULT_CANCELED);

        finish();
    }

    /*private void auth(){
        mTwitter.authorize();
    }*/
    
    /*@SuppressWarnings("deprecation")
    private void configureToken() {
        if (mAccessToken != null) {
            mTwitter.setOAuthConsumer(twitterConsumerKey, twitterSecretKey);

            mTwitter.setOAuthAccessToken(mAccessToken);
        }
    }*/
    
    /*private void auth(){
        String authUrl = "";
        
        int what = 1;
        
        try{
            authUrl = mHttpOauthprovider.retrieveRequestToken(mHttpOauthConsumer, CALLBACK_URL);
            
            what = 0;
        }catch(Exception ex){
            Log.e("", "");
        }
        
        mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0, authUrl));
    }*/
    
    /*private void showLoginDialog(String url) {
        final TwDialogListener listener = new TwDialogListener() {
			@Override
			public void onComplete(String value) {
//				processToken(value);
			}
			
			@Override
			public void onError(String value) {
//				mListener.onError("Failed opening authorization page");
			}
		};
		
//		new TwitterDialog(context, url, listener).show();
	}*/
    
    /*private void closeActivity(){
        finish();
    }*/

}
