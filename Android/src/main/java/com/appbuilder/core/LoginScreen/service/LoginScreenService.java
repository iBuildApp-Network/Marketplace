package com.appbuilder.core.LoginScreen.service;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by Artem on 11.02.14.
 */
public class LoginScreenService {
    public static String TAG = LoginScreenService.class.getName();

    public static void doLogin(final String url, final String username, final String password, final String type,
                               final String appId, final OnDone onDone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String link =
                            String.format("%s?login=%s&password=%s&type=%s&app_id=%s",
                                    url,
                                    URLEncoder.encode(username),
                                    URLEncoder.encode(password),
                                    URLEncoder.encode(type),
                                    URLEncoder.encode(appId));
                    Log.d(TAG, link);
                    HttpGet httpGet = new HttpGet(link);
                    HttpClient httpClient = new DefaultHttpClient();
                    String resp = httpClient.execute(httpGet, new ResponseHandler<String>() {
                        @Override
                        public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            return new String(String.valueOf(httpResponse.getStatusLine().getStatusCode()));
                        }
                    });

                    onDone.onDone(new Integer(resp));
                } catch (Exception e) {
                    Log.d(TAG, "", e);

                    onDone.onDone(new Integer("404"));
                }
            }
        }).start();
    }

    public static void doRecovery(final String url, final String username, final String appId, final OnDone onDone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String link =
                            String.format("%s?login=%s&app_id=%s",
                                    url,
                                    URLEncoder.encode(username),
                                    URLEncoder.encode(appId));
                    Log.d(TAG, link);
                    HttpGet httpGet = new HttpGet(link);
                    HttpClient httpClient = new DefaultHttpClient();
                    String resp = httpClient.execute(httpGet, new ResponseHandler<String>() {
                        @Override
                        public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            return new String(String.valueOf(httpResponse.getStatusLine().getStatusCode()));
                        }
                    });

                    onDone.onDone(new Integer(resp));
                } catch (Exception e) {
                    Log.d(TAG, "", e);

                    onDone.onDone(new Integer("404"));
                }
            }
        }).start();
    }

    public static void doCreateAccount(final String url, final String username, final String password,
                                       final String appId, final OnDone onDone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String link =
                            String.format("%s?login=%s&password=%s&type=%s&app_id=%s",
                                    url, username, password, appId);
                    Log.d(TAG, link);
                    HttpGet httpGet = new HttpGet(link);
                    HttpClient httpClient = new DefaultHttpClient();
                    String resp = httpClient.execute(httpGet, new ResponseHandler<String>() {
                        @Override
                        public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            return new String(String.valueOf(httpResponse.getStatusLine().getStatusCode()));
                        }
                    });

                    onDone.onDone(new Integer(resp));
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
            }
        }).start();
    }
}

