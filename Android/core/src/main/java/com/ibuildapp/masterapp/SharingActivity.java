package com.ibuildapp.masterapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.ibuildapp.masterapp.utils.Statics;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookGraphException;
import com.restfb.types.FacebookType;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 25.11.14
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
public class SharingActivity extends Activity implements
        View.OnClickListener {

    // const
    private final int HIDE_PROGRESS_DIALOG = 3;

    // UI
    private RelativeLayout topBar;
    private LinearLayout cancelBtn = null;
    private LinearLayout postBtn = null;
    private EditText editText = null;
    private ProgressDialog progressDialog = null;
    private TextView sharingPostText;


    // backend
    private String sharingType;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case HIDE_PROGRESS_DIALOG: {
                    hideProgressDialog();
                }
                break;
            }
        }
    };

    private void initializeUI()
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.masterapp_sharing_layout);
        Intent currentIntent = getIntent();
        sharingType = currentIntent.getStringExtra("type");

        cancelBtn = (LinearLayout) findViewById(R.id.sharing_cancel);
        cancelBtn.setOnClickListener(this);

        postBtn = (LinearLayout) findViewById(R.id.sharing_post);
        postBtn.setOnClickListener(this);

        sharingPostText = (TextView) findViewById(R.id.sharing_post_text);
        topBar = (RelativeLayout) findViewById(R.id.sharing_topbar);

        if ( sharingType.compareToIgnoreCase("facebook") == 0 )
        {
            topBar.setBackgroundColor(getResources().getColor(R.color.facebook_topbar));
            sharingPostText.setText(R.string.sharing_post_facebook);
        } else if ( sharingType.compareToIgnoreCase("twitter") == 0 )
        {
            topBar.setBackgroundColor(getResources().getColor(R.color.twitter_topbar));
            sharingPostText.setText(R.string.sharing_post_twitter);
        }

        editText = (EditText) findViewById(R.id.sharing_edittext);
        String stringToShare = getString( R.string.sharing_text );
        editText.setText( getString( R.string.sharing_text ));
        editText.setSelection(stringToShare.length());
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUI();

    }

    private void showProgressDialog() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.load));
            progressDialog.setCancelable(true);
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        finish();
    }

    private void closeOk()
    {
        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
        setResult(RESULT_OK);
        finish();
    }

    private void closeCancel( int errorCode, String errorMsg )
    {

        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SharingActivityResultDataPreferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("error_code", String.valueOf(errorCode));
        editor.putString("error_message", errorMsg);
        editor.commit();

        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Post button and home button handler.
     */
    public void onClick(View arg0) {
        switch (arg0.getId())
        {
            case R.id.sharing_cancel:
            {
                closeCancel(-2,"");
            } break;

            case R.id.sharing_post:
            {
                showProgressDialog();
                if (sharingType.equalsIgnoreCase("facebook")) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                String message_text = editText.getText().toString() + " " + Statics.SHARING_URL;
                                boolean res = FacebookAuthorizationActivity.sharing(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken(), message_text, null);
                                if ( res )
                                    closeOk();
                                else
                                    closeCancel(-1, "error");
                            } catch (Exception exception) {
                                closeCancel(-1, exception.getMessage());
                            }
//                            try{
//                                FacebookClient fbClient = new DefaultFacebookClient(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken());
//                                fbClient.publish("me/feed",FacebookType.class, Parameter.with("message", editText.getText().toString() + " " + Statics.SHARING_URL));
//                                closeOk();
//                            } catch (Exception e)
//                            {
//                                if(!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("Duplicate"))
//                                    closeOk();
//                                else
//                                    closeCancel(-1, ((FacebookGraphException) e).getErrorMessage());
//                            }
                        }
                    }).start();

                } else if (sharingType.equalsIgnoreCase("twitter")) {
                    new Thread(new Runnable() {
                        public void run() {
                            try{
                                // twitter init
                                ConfigurationBuilder builder = new ConfigurationBuilder();
                                builder.setDebugEnabled(true)
                                        .setOAuthAccessToken(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER).getAccessToken())
                                        .setOAuthAccessTokenSecret(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER).getAccessTokenSecret())
                                        .setOAuthConsumerSecret(com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_SECRET)
                                        .setOAuthConsumerKey(com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_KEY);
                                Configuration configuration = builder.build();
                                Twitter twitter = new TwitterFactory(configuration).getInstance();

                                // post message
                                StatusUpdate su = new StatusUpdate(editText.getText().toString() + " " + Statics.SHARING_URL);
                                Status st = twitter.updateStatus(su);
                                closeOk();
                            } catch ( Exception e)
                            {
                                if ( ((TwitterException) e).getErrorCode() == 187)
                                    closeCancel(1, "");
                                else
                                    closeCancel(-1, "");
                            }
                        }
                    }).start();
                }

            } break;


        }
    }
}
