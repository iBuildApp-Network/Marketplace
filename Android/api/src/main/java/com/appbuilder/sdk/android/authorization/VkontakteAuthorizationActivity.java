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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import com.appbuilder.sdk.android.AppBuilderModule;
import com.appbuilder.sdk.android.Statics;
import com.appbuilder.sdk.android.authorization.entities.User;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Artem
 * Date: 05.09.13
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public class VkontakteAuthorizationActivity extends AppBuilderModule {

    private static final String LOGNAME = "Vkontakte";
    private static final String REDIRECT_URL = "http://oauth.vk.com/blank.html";

    private WebView webView;
    private ProgressDialog progressDialog = null;
    private User vkUser;

    public VkontakteAuthorizationActivity() {
        vkUser = new User();
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

            public static final String USER_ID = "";

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // read bearer token from url while redirect
                Log.d(LOGNAME, "pageStarted url = " + url);

                String accessToken = null;
                String expiresIn = null;
                String userId = null;
                if (url.startsWith(REDIRECT_URL)) {
                    try {
                        URL urlClass = null;
                        try {
                            urlClass = new URL(url);
                        } catch (MalformedURLException mFURLEx) {
                        }

                        String query = urlClass.getRef();
                        String[] params = query.split("&");
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("access_token")) {
                                accessToken = params[i].split("=")[1];
                            }
                            if (params[i].contains("expires_in")) {
                                expiresIn = params[i].split("=")[1];
                            }
                            if (params[i].contains("user_id")) {
                                userId = params[i].split("=")[1];
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                if (accessToken != null) {
                    Log.d(LOGNAME, "accessToken = " + accessToken);
                    Log.d(LOGNAME, "expiresIn = " + expiresIn);
                    Log.d(LOGNAME, "userId = " + userId);


                    if (vkUser != null) {
                        vkUser.setAccessToken(accessToken);
                        vkUser.setAccountId(userId);
                        vkUser.setAccountType("vkontakte");

                        Authorization.vkontakteUser = vkUser;

                        finish();
                    }


                }
            }
        });
        webView.loadUrl(createUrlForAccessToken());
    }

    @Override
    public void destroy() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
    }

    private String createUrlForAccessToken() {
        String url;
        String urlPattern = "http://oauth.vk.com/oauth/authorize?client_id=%s&scope=wall,photos,offline&redirect_uri=http://oauth.vk.com/blank.html&display=mobile&response_type=token";
        url = String.format(urlPattern, Statics.VKONTAKTE_CLIENT_ID);
        Log.d(LOGNAME, "createUrlForAccessToken = " + url);
        return url;
    }

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, "Loading");
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void closeActivityWithOkResult() {
        hideProgressDialog();

        Intent it = new Intent();
        it.putExtra("user", vkUser);
        setResult(RESULT_OK, it);

        finish();
    }

    private void closeActivityWithBadResult() {
        hideProgressDialog();
        setResult(RESULT_CANCELED);
        finish();
    }


}
