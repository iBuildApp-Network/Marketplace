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
import com.appbuilder.sdk.android.authorization.Authorization;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.IOException;

/**
 * User: Artem
 * Date: 06.09.13
 * Time: 14:46
 */
public class LinkedInSharing {
    private static final String logname = "LinkedIn";


    public static String createSharingUrl() {
        String url = "";
        String urlPattern = "https://api.linkedin.com/v1/people/~/shares?oauth2_access_token=%s";
        String accessToken = Authorization.linkedinUser.getAccessToken();
        url = String.format(urlPattern, accessToken);
        Log.d(logname, "createSharingUrl = " + url);
        return url;
    }

    public static void share(String title, String message, Integer imageResource, String imageUrl, Activity ctx,
                             String description) throws IllegalStateException {
        Log.d(logname, "onClick");
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(createSharingUrl());
        HttpResponse response;
        JSONObject object = null;

        try {
            StringEntity se = new StringEntity(createXML(title, message, description, imageUrl), HTTP.UTF_8);
            se.setContentType("application/xml");
            httpPost.setHeader("Content-Type", "application/xml;charset=UTF-8");
            httpPost.setEntity(se);
            response = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String createXML(String title, String comment, String description, String imageUrl) {
        String submittedUrl = "ibuildapp.com";
        if (title == null)
            title = "";
        if (comment == null)
            comment = "";
        if (description == null)
            description = "";
        if (imageUrl == null)
            imageUrl = "";

        String xmlPattern = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<share>" +
            "<comment>%s</comment>" +
            "<content>" +
            "<title>%s</title>" +
            "<description>%s</description>" +
            "<submitted-url>%s</submitted-url>" +
            "<submitted-image-url>%s</submitted-image-url>" +
            "</content>" +
            "<visibility>" +
            "<code>anyone</code>" +
            "</visibility>" +
            "</share>";

        String xml = String.format(xmlPattern, comment, title, description, submittedUrl, imageUrl);
        Log.d(logname, "createXML" + xml);
        return xml;
    }
}
