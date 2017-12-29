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

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 05.05.14
 * Time: 10:52
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAnaliticsHandler {
    private final String TAG = "com.appbuilder.sdk.android.GoogleAnaliticsHandler";

    private String googleAnalyticsId;
    private String googleAnalyticsIbuildAppId = "UA-20239101-6";
    private Tracker ibuildAppTracker = null;
    private Tracker userTracker = null;
    private String appName;
    private GoogleAnalytics gaInstance;

    public GoogleAnaliticsHandler(Context context, String appName, String googleAnalyticsUserId) {
        this.appName = appName;
        gaInstance = GoogleAnalytics.getInstance(context);
        ibuildAppTracker = gaInstance.newTracker(googleAnalyticsIbuildAppId);
        if (!TextUtils.isEmpty(googleAnalyticsUserId))
            userTracker = gaInstance.newTracker(googleAnalyticsId);
    }

    public void sendIbuildAppEvent(String action, String label) {
        ibuildAppTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Android")
                .setAction(action)
                .setLabel(label)
                .build());

        gaInstance.dispatchLocalHits();
        Log.e(TAG, "IBUILDAPP Action = " + action + " Label = " + label);
    }


    public void sendUserEvent(String action, String label) {
        if (!TextUtils.isEmpty(googleAnalyticsId))
            userTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(appName + " (Android)")
                    .setAction(action)
                    .setLabel(label)
                    .build());

        gaInstance.dispatchLocalHits();
        Log.e(TAG, "USER Action = " + action + " Label = " + label);
    }
}
