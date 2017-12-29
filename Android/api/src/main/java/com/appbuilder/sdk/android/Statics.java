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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appbuilder.sdk.android;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.View;

import java.util.HashMap;

/**
 * @author minberg
 */
public class Statics {
    public static volatile boolean localFirstStart = true;
    // константа для получения сообщений через broadcastreceiver
    public static final String FAVOURITES_BROADCAST = "add_to_favourites";

    // секция констант для push notificatoins
    static final public String FORCE_CLOSE_MODULE_FLAG = "closeme_flag";
    static final public String FORCE_CLOSE_APP_FLAG = "close_app_flag";
    static final public String FORCE_CLOSE_MODULE = "closeme";
    static final public String FORCE_CLOSE_NEW_MODULE_ORDER = "new_order";

    static public boolean isOnline = false;

    static public boolean closeMain = false;

    static public boolean firstStart = false;

    static boolean isAdClosed;

    static public String appName;
    static public boolean fromMasterApp;
    static public boolean favouritedMasterApp;
    static public String cachePath = "";
    static public String googleAnalyticsId;
    static public String googleAnalyticsIbuildAppId;
    static public GoogleAnaliticsHandler analiticsHandler;

    static public boolean showLink = false;

    static public String BASE_DOMEN = "ibuildapp.com";

    static public String FACEBOOK_APP_ID = "207296122640913";
    static public String FACEBOOK_APP_SECRET = "3cae87e561313d4dd07c076566e2c67a";

    static public String TWITTER_CONSUMER_KEY = "p48aBftV8vXXfG6UWo0BcQ";
    static public String TWITTER_CONSUMER_SECRET = "YYkHCKtSD7uYhSC3jtPL1H2b6NaX2u6x5kOLLgRUA";

    static public String VKONTAKTE_CLIENT_ID = "3829591";

    static public String LINKEDIN_CLIENT_ID = "83kt13bp2ex3";
    static public String LINKEDIN_CLIENT_SECRET = "qKeywrtNNAbseOXV";


    public static HashMap<String, String> mapPluginConversation = setupHashMap();
    public static String appId = "1416";
    public static String appToken = "TTooKKeeNN";
    public static SparseArray<View.OnClickListener> linkWidgets = new SparseArray<>();
    private static Intent mainLink;
    private static FragmentActivity context;


    private static HashMap<String, String> setupHashMap() {
        mapPluginConversation = new HashMap<String, String>();
        mapPluginConversation.put("AudioPlugin", "Audio List");
        mapPluginConversation.put("CalculatorPlugin", "Calculator");
        mapPluginConversation.put("CallPlugin", "Tap To Call");
        mapPluginConversation.put("CameraPlugin", "Take a Picture");
        mapPluginConversation.put("ContactsPlugin", "Grouped Contacts");
        mapPluginConversation.put("CouponPlugin", "Coupons");
        mapPluginConversation.put("CustomFormPlugin", "Custom Form");
        mapPluginConversation.put("DirectoryPlugin", "Directory");
        mapPluginConversation.put("ECommercePlugin", "eCommerce");
        mapPluginConversation.put("EMailPlugin", "Tap To Email");
        mapPluginConversation.put("FanWallPlugin", "FanWall");
        mapPluginConversation.put("MapPlugin", "Google Map");
        mapPluginConversation.put("MenuPlugin", "Menu");
        mapPluginConversation.put("MultiContactsPlugin", "Grouped Contacts");
        mapPluginConversation.put("NewsPlugin", "News");
        mapPluginConversation.put("OpenTablePlugin", "OpenTable");
        mapPluginConversation.put("QRPlugin", "QR-Code Scaner");
        mapPluginConversation.put("PhotoGalleryPlugin", "Photo Gallery");
        mapPluginConversation.put("SkCataloguePlugin", "Book Store");
        mapPluginConversation.put("TablePlugin", "eBook");
        mapPluginConversation.put("TableReservationPlugin", "Reservation");
        mapPluginConversation.put("TwitterPlugin", "Twitter");
        mapPluginConversation.put("VideoPlugin", "Video List");
        mapPluginConversation.put("WebPlugin", "Web");
        return mapPluginConversation;
    }

    private static int sidebarNonWidgetClickListenerIndex = -2;
    public static SparseArray<View.OnClickListener> sidebarClickListeners = new SparseArray<>();
    public static int getSidebarNonWidgetClickListenerIndex() {
        return sidebarNonWidgetClickListenerIndex--;
    }

    public static void startMain(){
    }

    public static void setMainIntent(Intent intent, FragmentActivity appBuilder) {
        mainLink = intent;
        context = appBuilder;
    }
    public static void launchMain(){
        ActivityCompat.finishAffinity(context);
        mainLink.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainLink);
    }

    public static void clearSidebarNonWidgetClickListenerIndex() {
        sidebarNonWidgetClickListenerIndex = -2;
    }

    public static void resetAdStatus() {
        isAdClosed = false;
    }
}
