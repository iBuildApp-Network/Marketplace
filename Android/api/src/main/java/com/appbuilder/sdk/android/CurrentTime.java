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
package com.appbuilder.sdk.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.util.Log;

/**
 * Created by polzovatel on 11/27/14.
 * Класс для получения текущего времени со стороннего веб сервиса
 */
public class CurrentTime {

    private static URL URL_MILLIS;
    private final static String TAG = CurrentTime.class.getCanonicalName();
    static {
        try {
            URL_MILLIS = new URL("http://currentmillis.com/api/millis-since-unix-epoch.php");
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static long requestTime() {
        String inputLine;
        long currentTime = -1;
        try {
            final URLConnection connection = URL_MILLIS.openConnection();
            final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            if ((inputLine = in.readLine()) != null) {
                currentTime = Long.parseLong(inputLine);
            }
            in.close();
        } catch(Exception e){
            Log.e(TAG, e.getMessage());
        }

        return currentTime;
    }
}
