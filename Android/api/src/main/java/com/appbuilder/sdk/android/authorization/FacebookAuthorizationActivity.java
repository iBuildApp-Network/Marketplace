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
import android.location.LocationListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModule;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.restfb.*;
import com.restfb.types.FacebookType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FacebookAuthorizationActivity extends AppBuilderModule{

    private final int NEED_INTERNET_CONNECTION = 0;
    private final int CLOSE_ACTIVITY = 1;
    private final int CLOSE_ACTIVITY_OK = 2;
    private final int GET_USER_INFO = 3;
    private final int HIDE_PROGRESS_DIALOG = 4;

    private final String ACCESS_TOKEN = "access_token";
    private final String REDIRECT_URL = "ibuildapp.com/facebook.stub.php";
    public static final String NO_ADV_APPLICATION_ID = com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID;
    //"280308948710174";
    public static final String ADV_APPLICATION_ID = //com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID;
            //"306052656174768";
            "207296122640913";
    public static final String ADV_APPLICATION_SECRET = //com.appbuilder.sdk.android.Statics.FACEBOOK_APP_SECRET;
            //"9a9afde710374c96463bd68165cdced9";
            "8ce2f515309ba56afbfe9c1431b59d9a";

    private String applicationId = ADV_APPLICATION_ID;//NO_ADV_APPLICATION_ID;
    private String redirectURL = REDIRECT_URL;
    private String accessToken = "";

    private User fwUser = null;

    private ProgressDialog progressDialog = null;
    private WebView webView = null;

    private static final String TAG = "com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity";

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case NEED_INTERNET_CONNECTION:{
                    Toast.makeText(FacebookAuthorizationActivity.this,
                            "Cellular data is turned off.",
                            Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {

                        }
                    }, 3000);
                    closeActivityWithBadResult();
                }break;
                case CLOSE_ACTIVITY:{
                    closeActivityWithBadResult();
                }break;
                case CLOSE_ACTIVITY_OK:{
                    closeActivityWithOkResult();
                }break;
                case GET_USER_INFO:{
                    getUserInfo();
                }break;
                case HIDE_PROGRESS_DIALOG:{
                    hideProgressDialog();
                }break;
            }
        }
        
    };

    @Override
    public void create(){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        
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
        
            //setContentView(R.layout.romanblack_photogallery_facebook_auth);
            setContentView(mainLayout);
            
            if(!com.appbuilder.sdk.android.Statics.showLink){
                applicationId = NO_ADV_APPLICATION_ID;
                
                if(com.appbuilder.sdk.android.Statics.BASE_DOMEN != null &&
                        com.appbuilder.sdk.android.Statics.BASE_DOMEN.length() > 0){
                    redirectURL = com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/facebook.stub.php";
//                    redirectURL =  "ibuildapp.com/facebook.stub.php";
                }
            }
        
            //webView = (WebView)findViewById(R.id.romanblack_photogallery_facebook_auth_webview);
            webView.setBackgroundColor(Color.WHITE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:{
                        }break;    
                        case MotionEvent.ACTION_UP:{
                            if (!v.hasFocus()) {
                                v.requestFocus();
                            }
                        }break;
                    }
                    return false;
                }
            });
           
            webView.setWebViewClient(new WebViewClient(){

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    showProgressDialog();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    
                    if(url.startsWith("http://" + redirectURL)){
                        try{
                            URL urlClass = null;
                            try{
                                urlClass = new URL(url);
                            }catch(MalformedURLException mFURLEx){
                                Log.d("", "");
                            }
                            String ref = urlClass.getRef();
                            String[] refs = ref.split("&");
                            for(int i = 0; i < refs.length; i++){
                                if(refs[i].contains(ACCESS_TOKEN)){
                                    accessToken = refs[i].split("=")[1];
                                }
                            }
                        }catch(NullPointerException nPEx){
                            Log.d("", "");
                        }
                        
                        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                        
                        handler.sendEmptyMessage(GET_USER_INFO);
                    }else{
                        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    }
                }
            });
            
            webView.loadUrl("https://graph.facebook.com/v2.2/oauth/authorize?type=user_"
                    + "agent&client_id=" + applicationId + "&redirect_uri=http%3A%2F%2F"
                    + redirectURL + "&scope=publish_stream");

        Log.e(TAG, "webView.loadUrl = " + "https://graph.facebook.com/v2.2/oauth/authorize?type=user_"
                + "agent&client_id=" + applicationId + "&redirect_uri=http%3A%2F%2F"
                + redirectURL + "&scope=publish_stream");
    }
    
    private void getUserInfo(){


        new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    final FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_2);
                    com.restfb.types.User user = fbClient.fetchObject("me", com.restfb.types.User.class);
                    fwUser = new com.appbuilder.sdk.android.authorization.entities.User();
                    fwUser.setUserEmail(user.getEmail());
                    fwUser.setUserFirstName(user.getFirstName());
                    fwUser.setUserLastName(user.getLastName());
                    fwUser.setAvatarUrl("https://graph.facebook.com/v2.2/" + user.getId() +
                            "/picture?type=large");
                    fwUser.setAccountId(user.getId());
                    fwUser.setAccountType("facebook");
                    fwUser.setAccessToken(accessToken);

                    Authorization.facebookUser = fwUser;

                    if(Authorization.primaryUser == null){
                        Authorization.primaryUser = fwUser;
                    }

                    handler.sendEmptyMessage(CLOSE_ACTIVITY_OK);

                    Log.e("", "");

                }catch(Exception e){
                    handler.sendEmptyMessage(CLOSE_ACTIVITY);
                }
            }
        }).start();

    }
    
    private void showProgressDialog(){
        try{
            if(progressDialog.isShowing()){
                return;
            }
        }catch(NullPointerException nPEx){
        }
        
        try{
            Locale current = getResources().getConfiguration().locale;
            String loadingStr = "Loading...";
            if ( current.getLanguage().compareToIgnoreCase("en") == 0 )
                loadingStr = "Loading...";
            else if ( current.getLanguage().compareToIgnoreCase("ru") == 0 )
                loadingStr = "Загрузка...";

            progressDialog = ProgressDialog.show(this, null, loadingStr);
        }catch(Throwable thr){
        }
    }
    
    private void hideProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private void closeActivityWithOkResult(){
        hideProgressDialog();

        Authorization.facebookUser = fwUser;

        Intent it = new Intent();
        it.putExtra("user", fwUser);
        setResult(RESULT_OK, it);

        finish();
    }

    private void closeActivityWithBadResult(){
        hideProgressDialog();

        setResult(RESULT_CANCELED);

        finish();
    }

    /**
     * Лайк определенного урла на facebook
     * @param object - объект для лайка
     * @return true/false
     * @throws FacebookNotAuthorizedException - в случае если отсутствет токен, т.е. пользователь еще не авторизован
     */
    public static boolean like(String object) throws FacebookNotAuthorizedException,FacebookAlreadyLiked{
        try {
            if (TextUtils.isEmpty(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken()))
                throw new FacebookNotAuthorizedException("You have no token - You have not authorized yet");

            boolean isPost = !URLUtil.isValidUrl(object);

            String url = isPost ? "https://graph.facebook.com/v2.3/" + object + "/likes" : "https://graph.facebook.com/v2.3/me/og.likes";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (iPhone; U; "
                            + "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 "
                            + "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 "
                            + "Safari/6531.22.7");

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            StringBuilder sb = new StringBuilder();
            sb.append("method=");
            sb.append("POST");
            sb.append("&");
            sb.append("access_token=");
            sb.append(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken());

            if(!isPost) {
                sb.append("&");
                sb.append("object=");
                sb.append(URLEncoder.encode(object));
            }

            String params = sb.toString();

            conn.getOutputStream().write(params.getBytes("UTF-8"));

            String response = "";
            try {
                InputStream in = conn.getInputStream();

                StringBuilder sbr = new StringBuilder();
                BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    sbr.append(line);
                }
                in.close();

                response = sbr.toString();
            } catch (FileNotFoundException e) {
                InputStream in = conn.getErrorStream();

                StringBuilder sbr = new StringBuilder();
                BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    sbr.append(line);
                }
                in.close();

                response = sbr.toString();
                Log.e(TAG, "response = " + response);
            }

            try {
                // уже пролайкано
                if ( response.contains("already associated") )
                    throw new FacebookAlreadyLiked("This link has been already liked by you");

                // отлайкался - вернулся свежий id
                JSONObject obj = new JSONObject(response);

                if(!isPost) {
                    obj.getString("id");

                    return true;
                } else
                    return obj.getString("success").equals("true");


            } catch (JSONException jSONEx) {
                // какого то Х fb выдает ошибку когда лайкаешь картинку первый раз
//                      // со второго раза все норм поэтому дублируем запрос в таком случае
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (iPhone; U; "
                                + "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 "
                                + "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 "
                                + "Safari/6531.22.7");

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                sb = new StringBuilder();
                sb.append("method=");
                sb.append("POST");
                sb.append("&");
                sb.append("access_token=");
                sb.append(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken());

                if(!isPost) {
                    sb.append("&");
                    sb.append("object=");
                    sb.append(URLEncoder.encode(object));
                }

                params = sb.toString();

                conn.getOutputStream().write(params.getBytes("UTF-8"));

                try {
                    InputStream in = conn.getInputStream();

                    StringBuilder sbr = new StringBuilder();
                    BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
                    for (String line = r.readLine(); line != null; line = r.readLine()) {
                        sbr.append(line);
                    }
                    in.close();

                    response = sbr.toString();
                    Log.e(TAG, "response2 = " + response);

                    try{
                        // уже пролайкано
                        if ( response.contains("already associated") )
                            throw new FacebookAlreadyLiked("This link has been already liked by you");

                        JSONObject obj = new JSONObject(response);

                        if(!isPost) {
                            obj.getString("id");

                            return true;
                        } else
                            return obj.getString("success").equals("true");

                    } catch (JSONException e)
                    {
                        return false;
                    }
                } catch (FileNotFoundException e) {
                    return false;
                }
            }
        } catch (MalformedURLException mURLEx) {
            return false;
        } catch (IOException iOEx) {
            return false;
        }
    }

    /**
     * Подготавливаем список урлов, которые лайкал пользователь
     * @return - список урлов
     * @throws FacebookNotAuthorizedException - в случае если отсутствет токен, т.е. пользователь еще не авторизован
     */
    public static ArrayList<String> getUserOgLikes() throws FacebookNotAuthorizedException{
        try {
            if (TextUtils.isEmpty(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken()))
                throw new FacebookNotAuthorizedException("You have no token - You have not authorized yet");

            ArrayList<String> res = new ArrayList<String>();

            String likesResult = loadURLData("https://graph.facebook.com/v2.2/me/"
                    + "og.likes?fields=data&limit=999999999&access_token="
                    + Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken());

            JSONObject mainObject = new JSONObject(likesResult);
            JSONArray dataJSONArray = mainObject.getJSONArray("data");

            for (int i = 0; i < dataJSONArray.length(); i++) {
                JSONObject likeObject = dataJSONArray.getJSONObject(i)
                        .getJSONObject("data").getJSONObject("object");
                res.add(likeObject.getString("url"));
            }

            return res;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get facebook token for FB app_id and app_secret
     * @param FACEBOOK_APP_ID
     * @param FACEBOOK_APP_SECRET
     * @return token
     */
    public static String getFbToken(String FACEBOOK_APP_ID, String FACEBOOK_APP_SECRET) {
        try {
            String tokenUrl = "https://graph.facebook.com/v2.2/oauth/access_token?"
                    + "client_id="
                    + FACEBOOK_APP_ID +
                    "&client_secret="
                    + FACEBOOK_APP_SECRET +
                    "&grant_type=client_credentials";

            String accessResult = loadURLData(tokenUrl);

            return accessResult.trim().split("=")[1];
        } catch (Exception ex) {
            Log.d("", "");
            return null;
        }
    }

    /**
     * Download URL data to String.
     * @param msgsUrl URL to download
     * @return data string
     */
    private static String loadURLData(String msgsUrl) {
        try {
            URL url = new URL(msgsUrl);
            URLConnection conn = url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

            BufferedReader br = new BufferedReader(streamReader);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            String resp = sb.toString();

            return resp;
        } catch (IOException iOEx) {
            return "";
        }
    }

    /**
     * Получаем список колличества лайков для списка урлов
     * @param urls список url
     * @param accessToken токен
     * @return возвращает мапку ключ - URL, значение - колличество лайков для урла
     */
    public static Map<String,String> getLikesForUrls( List<String> urls, String accessToken )
    {
        // в связи с тем, что эти пидоры с FB ограничили колличество id в запросе (max 50) пришлось написать цикл
        int MAX_ID_COUNT = 40;
        // преобразуем урлы в id
        Map<String, String> idMap = new HashMap<String, String>();
        Map<String, String> likeMap = new HashMap<String, String>();
        int circleSize = (urls.size()/MAX_ID_COUNT) + 1;
        for (int i = 0; i < circleSize; i++) {
            idMap.clear();
            int startIdx = i*MAX_ID_COUNT;
            int endIdx = ((i+1)*MAX_ID_COUNT) > urls.size() ? urls.size() : (i+1)*MAX_ID_COUNT;
            List<String> subUlrs = urls.subList(startIdx, endIdx);

            // получаем список ID
            String httpQuery = String.format("https://graph.facebook.com/v2.2/?ids=%s&access_token=%s", subUlrs.toString().replace("[", "").replace("]","").replace(" ",""),accessToken);
            String response = loadURLData(httpQuery);

            // парсим список ID
            JSONObject mainObject = null;
            try {
                mainObject = new JSONObject(response);
                JSONArray names = mainObject.names();
                for (int j = 0; j < names.length(); j++) {
                    String url = (String) names.get(j);

                    JSONObject object = null;
                    try {
                        object = mainObject.getJSONObject(url);
                        String id = object.getJSONObject("og_object").getString("id");
                        Log.e("","");
                        idMap.put(id, url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // по id получаем колличество лайков
            httpQuery = String.format("https://graph.facebook.com/v2.2/likes?ids=%s&summary=1&limit=0&access_token=%s", idMap.keySet().toString().replace("[","").replace("]","").replace(" ",""),accessToken);
            response = loadURLData(httpQuery);

            // парсим лайки
            try {
                mainObject = new JSONObject(response);
                JSONArray names = mainObject.names();
                for (int j = 0; j < names.length(); j++) {
                    String id = (String) names.get(j);
                    JSONObject object = null;
                    try {
                        object = mainObject.getJSONObject(id).getJSONObject("summary");
                        String likeCount = object.getString("total_count");
                        String url = idMap.get(id);
                        likeMap.put(url, likeCount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return likeMap;
    }

    /**
     * Получаем список колличества лайков для списка айдишников
     * @param ids список айдишников
     * @param accessToken токен
     * @return возвращает мапку ключ - URL, значение - колличество лайков для урла
     */
    public static Map<String,String> getLikesForObjects(List<String> ids, String accessToken) {
        // в связи с тем, что эти пидоры с FB ограничили колличество id в запросе (max 50) пришлось написать цикл
        int MAX_ID_COUNT = 40;
        Map<String, String> idMap = new HashMap<String, String>();
        Map<String, String> likeMap = new HashMap<String, String>();
        int circleSize = (ids.size() / MAX_ID_COUNT) + 1;
        for (int i = 0; i < circleSize; i++) {
            idMap.clear();
            int startIdx = i*MAX_ID_COUNT;
            int endIdx = ((i+1)*MAX_ID_COUNT) > ids.size() ? ids.size() : (i+1)*MAX_ID_COUNT;
            List<String> subIds = ids.subList(startIdx, endIdx);

            for(String id : subIds)
                idMap.put(id, "https://graph.facebook.com/v2.3/" + id + "/likes");

            // по id получаем колличество лайков
            String httpQuery = String.format("https://graph.facebook.com/v2.2/likes?ids=%s&summary=1&limit=0&access_token=%s", idMap.keySet().toString().replace("[","").replace("]","").replace(" ",""), accessToken);
            String response = loadURLData(httpQuery);

            JSONObject mainObject = null;

            // парсим лайки
            try {
                mainObject = new JSONObject(response);
                JSONArray names = mainObject.names();
                for (int j = 0; j < names.length(); j++) {
                    String id = (String) names.get(j);
                    JSONObject object = null;
                    try {
                        object = mainObject.getJSONObject(id).getJSONObject("summary");
                        String likeCount = object.getString("total_count");
                        String url = idMap.get(id);
                        likeMap.put(url, likeCount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return likeMap;
    }

    /**
     * Получаем список колличества комментов для списка айдишников
     * @param ids список айдишников
     * @param accessToken токен
     * @return возвращает мапку ключ - URL, значение - колличество лайков для урла
     */
    public static Map<String,String> getCommentsForObjects(List<String> ids, String accessToken) {
        // в связи с тем, что эти пидоры с FB ограничили колличество id в запросе (max 50) пришлось написать цикл
        int MAX_ID_COUNT = 40;
        Map<String, String> idMap = new HashMap<String, String>();
        Map<String, String> commentMap = new HashMap<String, String>();
        int circleSize = (ids.size() / MAX_ID_COUNT) + 1;
        for (int i = 0; i < circleSize; i++) {
            idMap.clear();
            int startIdx = i*MAX_ID_COUNT;
            int endIdx = ((i+1)*MAX_ID_COUNT) > ids.size() ? ids.size() : (i+1)*MAX_ID_COUNT;
            List<String> subIds = ids.subList(startIdx, endIdx);

            for(String id : subIds)
                idMap.put(id, "https://graph.facebook.com/v2.3/" + id + "/comments");

            // по id получаем колличество комментов
            String httpQuery = String.format("https://graph.facebook.com/v2.2/comments?ids=%s&summary=1&limit=0&access_token=%s", idMap.keySet().toString().replace("[","").replace("]","").replace(" ",""), accessToken);
            String response = loadURLData(httpQuery);

            JSONObject mainObject = null;

            // парсим комменты
            try {
                mainObject = new JSONObject(response);
                JSONArray names = mainObject.names();
                for (int j = 0; j < names.length(); j++) {
                    String id = (String) names.get(j);
                    JSONObject object = null;
                    try {
                        object = mainObject.getJSONObject(id).getJSONObject("summary");
                        String commentCount = object.getString("total_count");
                        String url = idMap.get(id);
                        commentMap.put(url, commentCount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return commentMap;
    }

    /**
     * Функция шаринга через FB
     * @param accessToken - FB токен
     * @param message - сообщение для публикации
     * @param imageUrl - картинка ( если есть ) для аттача к посту (может быть как файлом, так URL ссылкой )
     * @return true/falce
     * @throws FacebookNotAuthorizedException - в случае если отсутствет токен, т.е. пользователь еще не авторизован
     */
    public static boolean sharing( String accessToken, String message, String imageUrl ) throws FacebookNotAuthorizedException {
        if (TextUtils.isEmpty(accessToken))
            throw new FacebookNotAuthorizedException("You have no token - You have not authorized yet");

        FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_2);

        if ( !TextUtils.isEmpty(imageUrl) ) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String photo_name = "IMG_IBUILDAPP_" + timeStamp + ".jpg";

            InputStream input = null;
            try {
                input = new URL(imageUrl).openStream();
            } catch (IOException e) {
                try {
                    input = new FileInputStream(imageUrl);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }

            if ( input != null )
            {
                try {
                    fbClient.publish("me/photos",
                            FacebookType.class,
                            BinaryAttachment.with(photo_name, input),
                            Parameter.with("description", message),
                            Parameter.with("message", message));
                } catch (Exception e) {
                    return false;
                }
            } else
            {
                try {
                    fbClient.publish("me/feed", FacebookType.class, Parameter.with("message", message));
                } catch (Exception e) {
                    if ( !TextUtils.isEmpty( e.getMessage() )  )
                    {
                        if ( e.getMessage().contains("Duplicate")  )
                            return true;
                    }
                    return false;
                }
            }
        } else {
            try {
                fbClient.publish("me/feed", FacebookType.class, Parameter.with("message", message));
            } catch (Exception e) {
                if ( !TextUtils.isEmpty( e.getMessage() )  )
                {
                    if ( e.getMessage().contains("Duplicate")  )
                        return true;
                }
                return false;
            }
        }
        return true;
    }

    //for posts
    public static boolean sharePost(String accessToken, String message, String url) throws FacebookNotAuthorizedException {
        if (TextUtils.isEmpty(accessToken))
            throw new FacebookNotAuthorizedException("You have no token - You have not authorized yet");

        FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_2);

        try {
            fbClient.publish("me/feed", FacebookType.class, Parameter.with("link", url), Parameter.with("message", message));
        } catch (Exception e) {
            if ( !TextUtils.isEmpty( e.getMessage() )  )
            {
                if ( e.getMessage().contains("Duplicate")  )
                    return true;
            }
            return false;
        }

        return true;
    }

    /**
     * класс Exception
     * Выбрасывать когда пользовател не автризован через Facebook
     */
    public static class FacebookNotAuthorizedException extends Exception
    {
        public FacebookNotAuthorizedException() {
            super();
        }

        public FacebookNotAuthorizedException(String detailMessage) {
            super(detailMessage);
        }
    }

    /**
     * класс Exception
     * Выбрасывать когда пользователь уже пролайкал этот объект
     */
    public static class FacebookAlreadyLiked extends Exception
    {
        public FacebookAlreadyLiked() {
        }

        public FacebookAlreadyLiked(String detailMessage) {
            super(detailMessage);
        }
    }

}
