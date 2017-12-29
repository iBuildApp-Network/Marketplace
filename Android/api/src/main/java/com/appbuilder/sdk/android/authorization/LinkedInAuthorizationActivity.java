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

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import com.appbuilder.sdk.android.AppBuilderModule;
import com.appbuilder.sdk.android.Statics;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.authorization.entities.User;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: Artem
 * Date: 05.09.13
 * Time: 10:26
 */
public class LinkedInAuthorizationActivity extends AppBuilderModule {

    private final static String logname = "LinkedIn";
    private static String state = "123123123";
    private static String bearerToken;
    private static String REDIRECT_URL = "http://ibuildapp.com";
    private static String accessToken;

    private WebView webView;
    private ProgressDialog progressDialog = null;
    private User linkedInUser;

    public LinkedInAuthorizationActivity() {
        linkedInUser = new User();
    }

    private static String createUrlForAccessToken() {
        String url = null;

        url = String.format("https://www.linkedin.com/uas/oauth2/accessToken?" +
                "grant_type=authorization_code" +
                "&code=%s" +
                "&redirect_uri=%s" +
                "&client_id=%s" +
                "&client_secret=%s",
                bearerToken,
                REDIRECT_URL,
                Statics.LINKEDIN_CLIENT_ID,
                Statics.LINKEDIN_CLIENT_SECRET);

        Log.d(logname, "createUrlForAccessToken = " + url);
        return url;
    }

    private static String createUrlForBearerToken() {
        String url = null;
        Log.d(logname, "createUrlForBearerToken");
        url = String.format("https://www.linkedin.com/uas/oauth2/authorization?" +
                "response_type=code" +
                "&client_id=%s" +
                "&state=%s" +
                "&redirect_uri=%s",
                Statics.LINKEDIN_CLIENT_ID,
                state,
                REDIRECT_URL);
        Log.d(logname, "url = " + url);
        return url;
    }

    public static String getAccessToken() {
        accessToken = null;

        String urlAccessToken = createUrlForAccessToken();
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(urlAccessToken);
        HttpResponse response;
        JSONObject object = null;

        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            object = Utils.inputStreamToJSONObject(entity.getContent());
            accessToken = object.getString("access_token");
            String experisIn = object.getString("expires_in");
            Log.d(logname, String.format("accessToken = %s, experiseIn = %s", accessToken, experisIn));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    @Override
    public void create() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mainLayout.setLayoutParams(lp);

        webView = new WebView(this);
        webView.setLayoutParams(lp);

        mainLayout.addView(webView);

        setContentView(mainLayout);

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
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(logname, "shouldOverrideUrlLoading: url = " + url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // read bearer token from url while redirect
                String accessToken = null;
                Log.d(logname, "pageStarted url = " + url);
                if (url.startsWith(REDIRECT_URL)) {
                    try {
                        URL urlClass = null;
                        try {
                            urlClass = new URL(url);
                        } catch (MalformedURLException mFURLEx) {
                        }

                        String query = urlClass.getQuery();
                        String[] params = query.split("&");
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("code")) {
                                bearerToken = params[i].split("=")[1];
                                accessToken = getAccessToken();
                                break;
                            }
                        }
                    } catch (NullPointerException nPEx) {
                        Log.d("", "");
                    }
                }

                if (accessToken != null) {
                    Log.d(logname, "bearerToken = " + bearerToken);
                    Log.d(logname, "accessToken = " + accessToken);
                    linkedInUser.setAccessToken(accessToken);
                    linkedInUser.setAccountType("linkedin");
                    Authorization.linkedinUser = linkedInUser;
                    finish();
                }
            }
        });
        String url = createUrlForBearerToken();
        webView.loadUrl(url);
    }

}

