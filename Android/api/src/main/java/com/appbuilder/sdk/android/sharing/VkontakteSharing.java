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
package com.appbuilder.sdk.android.sharing;

import android.app.Activity;
import android.util.Log;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.entities.User;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import static java.net.URLEncoder.encode;

public class VkontakteSharing {

    final static String logname = "Vkontakte";
    private String userId;
    private String accessToken;

    public static void share(String title, String message, Integer imageResource, String imageUrl, Activity ctx) throws IllegalStateException {
        if (imageResource != null || imageUrl != null) {
            requestWallPostWithImage(imageResource, imageUrl, message, ctx);
        } else {
            requestWallPost(message);
        }
    }

    public static void requestWallPostWithImage(Integer imageResource, String imageUrl, String message, Activity ctx) throws IllegalStateException {
        try {
            if (Authorization.vkontakteUser != null) {
                //  Отправка изображения на стену пользователя происходит в несколько этапов:
                //  1. Запрос сервера ВКонтакте для загрузки нашего изображения (photos.getWallUploadServer)
                String uploadUrl = requestServerForImageUpload();
                //  2. По полученной ссылке в ответе сервера отправляем изображение методом POST
                byte[] image;
                if (imageResource != null)
                    image = Utils.loadImageFromResource(imageResource, ctx);
                else
                    image = Utils.loadImageFromUrl(imageUrl);
                JSONObject responseImageUpload = requestImageUpload(uploadUrl, image);
                //  3. Получив в ответе hash, photo, server отправлем команду на сохранение фото на стене (photos.saveWallPhoto)
                JSONObject responseSaveWallPhoto = saveWallPhoto(responseImageUpload);
                //  4. Получив в ответе photo id делаем запрос на размещение на стене картинки с помощью wall.post, где в качестве attachment указываем photo id
                wallPost(responseSaveWallPhoto, message);
            } else {
                throw new IllegalStateException("You must be authorized in VK");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("You must be authorized in VK");
        }
    }

    private static void wallPost(JSONObject responseSaveWallPhoto, String description) {
        try {
            JSONArray responseArray = (JSONArray) responseSaveWallPhoto.getJSONArray("response");
            JSONObject firstElement = (JSONObject) responseArray.get(0);
            String attachment = firstElement.getString("id");

            String url = createWallPostUrl(description, attachment);

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse response;

            response = client.execute(get);

            JSONObject object = Utils.inputStreamToJSONObject(response.getEntity().getContent());
            Log.d(logname, "wallPost result = " + object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void requestWallPost(String message) {
        try {
            String url = createWallSimplePostUrl(message);

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse response;

            response = client.execute(get);

            JSONObject object = Utils.inputStreamToJSONObject(response.getEntity().getContent());
            Log.d(logname, "requestWallPost result = " + object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String createWallSimplePostUrl(String message) {
        String url;
        String urlPattern = "https://api.vk.com/method/wall.post?owner_id=%s&access_token=%s&message=%s";
        User vkUser = Authorization.vkontakteUser;
        url = String.format(urlPattern, vkUser.getAccountId(), vkUser.getAccessToken(), encode(message));
        Log.d(logname, "createWallSimplePostUrl = " + url);
        return url;
    }

    private static String createWallPostUrl(String message, String attachment) {
        String url;
        String urlPattern = "https://api.vk.com/method/wall.post?owner_id=%s&access_token=%s&message=%s&attachment=%s";
        User vkUser = Authorization.vkontakteUser;

        url = String.format(urlPattern, vkUser.getAccountId(), vkUser.getAccessToken(), encode(message), attachment);
        Log.d(logname, "createWallPostUrl = " + url);
        return url;
    }

    private static JSONObject saveWallPhoto(JSONObject object) {
        JSONObject result = null;
        try {
            String server = object.getString("server");
            String hash = object.getString("hash");
            String photo = object.getString("photo");

            HttpGet get = new HttpGet(createSaveWallPhotoUrl(photo, server, hash));
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;

            response = client.execute(get);

            result = Utils.inputStreamToJSONObject(response.getEntity().getContent());
            Log.d(logname, "result = " + result);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    private static JSONObject requestImageUpload(String uploadUrl, byte[] image) {
        JSONObject object = null;
        try {
            // Send image to VK Image server
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(uploadUrl);

            MultipartEntity mpEntity = new MultipartEntity();
            ContentBody cbFile = new ByteArrayBody(image, "image/jpeg", "image.jpg");
            mpEntity.addPart("photo", cbFile);

            httpPost.setEntity(mpEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            Log.d(logname, "image is loaded");
            object = Utils.inputStreamToJSONObject(httpResponse.getEntity().getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }


    private static String createSaveWallPhotoUrl(String photo, String server, String hash) {
        String urlPattern = "https://api.vk.com/method/photos.saveWallPhoto?owner_id=%s&access_token=%s&server=%s&photo=%s&hash=%s";
        User vkUser = Authorization.vkontakteUser;
        String url = String.format(urlPattern, vkUser.getAccountId(), vkUser.getAccessToken(), server, encode(photo), hash);
        Log.d(logname, "createSaveWallPhotoUrl = " + url);
        return url;
    }

    private static String createGetWallUploadServerUrl() {
        String urlPattern = "https://api.vk.com/method/photos.getWallUploadServer?owner_id=%s&access_token=%s";
        User vkUser = Authorization.vkontakteUser;
        String url = String.format(urlPattern, vkUser.getAccountId(), vkUser.getAccessToken());
        Log.d(logname, "createGetWallUploadServerUrl = " + url);
        return url;
    }


    private static String requestServerForImageUpload() {
        String uploadUrl = "";

        Log.d(logname, "requestServerForImageUpload");
        String imageServerRequestUrl = createGetWallUploadServerUrl();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(imageServerRequestUrl);
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpGet);
            JSONObject object = Utils.inputStreamToJSONObject(httpResponse.getEntity().getContent());
            JSONObject response = object.getJSONObject("response");
            uploadUrl = response.getString("upload_url");

            Log.d(logname, "uploadUrl = " + uploadUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadUrl;
    }


}
