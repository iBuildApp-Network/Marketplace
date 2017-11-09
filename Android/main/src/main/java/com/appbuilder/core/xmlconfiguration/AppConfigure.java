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
package com.appbuilder.core.xmlconfiguration;

import android.graphics.Color;
import android.util.Log;
import com.appbuilder.core.GPSNotification.GPSItem;
import com.appbuilder.sdk.android.AppAdvData;
import com.appbuilder.sdk.android.BarDesigner;
import com.appbuilder.sdk.android.LoginScreen;
import com.appbuilder.sdk.android.Statics;
import com.appbuilder.sdk.android.Widget;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class which contain parsed structure of input *.xml file
 */
public class AppConfigure extends AppConfigureItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static AppConfigure mCurrentInstance;

    /**
     * Common properties of application
     * name, background image, color and so on...
     */
    private String mAppName = "";
    private String mAppId = "";
    private String mBackgroundColor = "";
    private String mBackgroundImageUrl = "";
    private String mBackgroundImageCache = "";
    private String mBackgorundImageData = "";
    private String mBackgorundImageRes = "";
    private boolean mShowLink = false;
    private boolean showSidebar = Statics.fromMasterApp;
    private boolean updateContentPushEnabled = false;
    private int mDateFormat = 0;
    private int mShowMenu = 1;
    private String mSplashScreen = "";
    private String mSplashScreenRes = "";
    private int defaultOrder = -1;
    private LoginScreen loginScreen;

    private String pushNotificationAccount = "";

    private String googleAnalyticsId = "";

    private AppAdvData mAppAdv = null;
    private BarDesigner navBarDesign;
    private BarDesigner tabBarDesign;
    private BarDesigner bottomBarDesign;


    /**
     * Arrays of UI widgets which were parsed from xml file
     */
    private ArrayList<WidgetUILabel> mLabels;
    private ArrayList<WidgetUIButton> mButtons;
    private ArrayList<WidgetUIImage> mImages;
    private ArrayList<WidgetUITab> mTabs;
    private ArrayList<Widget> mWidgets;
    private ArrayList<AppConfigureItem> mControls;
    private ArrayList<GPSItem> gpsNotifications;
    private ArrayList<WidgetUISidebarItem> sidebarItems;

    /*
     * Login form info
     */
    private LoginForm loginForm;

    public AppConfigure() {
        mLabels = new ArrayList<WidgetUILabel>();
        mButtons = new ArrayList<WidgetUIButton>();
        mImages = new ArrayList<WidgetUIImage>();
        mTabs = new ArrayList<WidgetUITab>();
        mWidgets = new ArrayList<Widget>();
        mControls = new ArrayList<AppConfigureItem>();
        gpsNotifications = new ArrayList<GPSItem>();
        sidebarItems = new ArrayList<>();

        // прописываем дефолтные значения для navbar
        navBarDesign = new BarDesigner();
        navBarDesign.color = Color.BLACK;
        navBarDesign.titleDesign.fontWeight = "bold";
        navBarDesign.titleDesign.fontSize = 16;
        navBarDesign.titleDesign.numberOfLines = 0;
        navBarDesign.titleDesign.textAlignment = "center";
        navBarDesign.titleDesign.textColor = Color.WHITE;
        navBarDesign.itemDesign.fontWeight = "normal";
        navBarDesign.itemDesign.fontSize = 16;
        navBarDesign.itemDesign.numberOfLines = 0;
        navBarDesign.itemDesign.textAlignment = "center";
        navBarDesign.itemDesign.textColor = Color.parseColor("#007aff");

        // прописываем дефолтные значения для navbar
        tabBarDesign = new BarDesigner();
        tabBarDesign.color = Color.BLACK;
        tabBarDesign = new BarDesigner();
        tabBarDesign.itemDesign.fontWeight = "bold";
        tabBarDesign.itemDesign.selectedColor = Color.parseColor("#CFCFCF");
        tabBarDesign.itemDesign.fontSize = 16;
        tabBarDesign.itemDesign.numberOfLines = 0;
        tabBarDesign.itemDesign.textAlignment = "center";
        tabBarDesign.itemDesign.textColor = Color.parseColor("#ffffff");

        // прописываем дефолтные значения для navbar
        bottomBarDesign = new BarDesigner();
        bottomBarDesign.color = Color.BLACK;
        bottomBarDesign = new BarDesigner();

        bottomBarDesign.leftButtonDesign.fontWeight = "bold";
        bottomBarDesign.leftButtonDesign.fontSize = 16;
        bottomBarDesign.leftButtonDesign.numberOfLines = 0;
        bottomBarDesign.leftButtonDesign.textAlignment = "left";
        bottomBarDesign.leftButtonDesign.textColor = Color.parseColor("#007aff");

        bottomBarDesign.rightButtonDesign.fontWeight = "bold";
        bottomBarDesign.rightButtonDesign.fontSize = 16;
        bottomBarDesign.rightButtonDesign.numberOfLines = 0;
        bottomBarDesign.rightButtonDesign.textAlignment = "right";
        bottomBarDesign.rightButtonDesign.textColor = Color.parseColor("#0005DE");

        //дефолтные значения для loginform
        loginForm = new LoginForm();

        mCurrentInstance = AppConfigure.this;
    }

    public String getmSplashScreenRes() {
        return mSplashScreenRes;
    }

    public void setmSplashScreenRes(String mSplashScreenRes) {
        this.mSplashScreenRes = mSplashScreenRes;
    }

    public String getmBackgorundImageRes() {
        return mBackgorundImageRes;
    }

    public void setmBackgorundImageRes(String mBackgorundImageRes) {
        this.mBackgorundImageRes = mBackgorundImageRes;
    }

    public String getmAppId() {
        return mAppId;
    }

    public void setmAppId(String mAppId) {
        this.mAppId = mAppId;
    }

    public BarDesigner getBottomBarDesign() {
        return bottomBarDesign;
    }

    public BarDesigner getNavBarDesign() {
        return navBarDesign;
    }

    public BarDesigner getTabBarDesign() {
        return tabBarDesign;
    }

    public void setTabBarDesign(BarDesigner tabBarDesign) {
        this.tabBarDesign = tabBarDesign;
    }

    public void setNavBarDesign(BarDesigner navBarDesign) {
        this.navBarDesign = navBarDesign;
    }

    public void setBottomBarDesign(BarDesigner bottomBarDesign) {
        this.bottomBarDesign = bottomBarDesign;
    }

    public boolean isShowSidebar() {
        return showSidebar;
    }

    public void setShowSidebar(boolean showSidebar) {
        this.showSidebar = showSidebar;
    }

    public String getSplashScreen() {
        return mSplashScreen;
    }

    public void setSplashScreen(String value) {
        mSplashScreen = value;
    }

    public static AppConfigure getCurrent() {
        return mCurrentInstance;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String value) {
        mAppName = value;
    }

    public String getPushNotificationAccount() {
        return pushNotificationAccount;
    }

    public void setPushNotificationAccount(String value) {
        pushNotificationAccount = value;
    }

    public void addGPSNotification(GPSItem gpsItem) {
        gpsNotifications.add(gpsItem);
    }

    public ArrayList<GPSItem> getGPSNotifications() {
        return gpsNotifications;
    }

    public GPSItem getGPSNotificationAtIndex(int index) {
        return gpsNotifications.get(index);
    }

    public void clearGPSNotifications() {
        gpsNotifications.clear();
    }

    public void removeGPSNotificationAtIndex(int index) {
        gpsNotifications.remove(index);
    }

    public String getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(String value) {
        mBackgroundColor = value;
    }

    public String getBackgroundImageUrl() {
        return mBackgroundImageUrl;
    }

    public void setBackgroundImageUrl(String value) {
        mBackgroundImageUrl = value;
    }

    public String getBackgroundImageCache() {
        return mBackgroundImageCache;
    }

    public void setBackgroundImageCache(String value) {
        mBackgroundImageCache = value;
    }

    public boolean getShowLink() {
        return mShowLink;
    }

    public void setShowLink(boolean value) {
        mShowLink = value;
    }

    public int getDateFormat() {
        return mDateFormat;
    }

    public void setDateFormat(int value) {
        mDateFormat = value;

        for(Widget tmpWidget : mWidgets){
            tmpWidget.setDateFormat(value);
        }
    }

    public int getShowMenu() {
        return mShowMenu;
    }

    public void setShowMenu(int value) {
        mShowMenu = value;
    }


    public AppAdvData getAppAdv() {
        return mAppAdv;
    }

    public void setAppAdv(AppAdvData value) {
        mAppAdv = value;
    }


    public void clearCollections() {
        mLabels.clear();
        mButtons.clear();
        mImages.clear();
        mWidgets.clear();
    }

    public int getLabelsCount() {
        return mLabels.size();
    }

    public void addLabel(WidgetUILabel value) {
        mLabels.add(value);
    }

    public WidgetUILabel getLabelAtIndex(int index) {
        if (index < 0 || index >= mLabels.size())
            return null;
        return mLabels.get(index);
    }

    public int getControlsCount() {
        return mControls.size();
    }

    public void addControl(AppConfigureItem value) {
        mControls.add(value);
    }

    public AppConfigureItem getControlAtIndex(int index) {
        if (index < 0 || index >= mControls.size())
            return null;
        return mControls.get(index);
    }

    public int getButtonsCount() {
        return mButtons.size();
    }

    public void addButton(WidgetUIButton value) {
        mButtons.add(value);
    }

    public WidgetUIButton getButtonAtIndex(int index) {
        if (index < 0 || index >= mButtons.size())
            return null;
        return mButtons.get(index);
    }

    public void setButtonAtIndex(int index, WidgetUIButton value) {
        mButtons.set(index, value);
    }


    public int getImagesCount() {
        return mImages.size();
    }

    public void addImage(WidgetUIImage value) {
        mImages.add(value);
    }

    public WidgetUIImage getImageAtIndex(int index) {
        if (index < 0 || index >= mImages.size())
            return null;
        return mImages.get(index);
    }

    public void setImageAtIndex(int index, WidgetUIImage value) {
        mImages.set(index, value);
    }


    public int getTabsCount() {
        return mTabs.size();
    }

    public void addTab(WidgetUITab value) {
        mTabs.add(value);
    }

    public WidgetUITab getTabAtIndex(int index) {
        if (index < 0 || index >= mTabs.size())
            return null;
        return mTabs.get(index);
    }

    public void setTabAtIndex(int index, WidgetUITab value) {
        mTabs.set(index, value);
    }

    public int getWidgetsCount() {
        return mWidgets.size();
    }

    public void addWidget(Widget value) {
        mWidgets.add(value);
    }

    public Widget getWidgetAtIndex(int index) {
        for (int i = 0; i < mWidgets.size(); i++) {
            Widget widget = mWidgets.get(i);
            if (widget != null) {
                if (widget.getOrder() == index) {
                    widget.setAppName(mAppName);
                    return widget;
                }
            }
        }
        return null;
    }

    public Widget getWidgetWithOrder(int order) {
        for (Widget w : mWidgets) {
            if (w.getOrder() == order)
                return w;
        }
        return null;
    }


    public void clearWidgets() {
        mWidgets.clear();
    }

    public ArrayList<Widget> getmWidgets() {
        // т.к. тег appname находится в самом конце xml, то у виджетов appneme не проставляется
        // делаем это ручками
        for (Widget w : mWidgets)
            w.setAppName(mAppName);

        return mWidgets;
    }

    public boolean needShowMenu() {
        return ((mShowMenu != 0) && (mTabs.size() > 0));
    }

    private DownloadStatus mBackgroundDownloaded = DownloadStatus.NOT_DOWNLOADED;

    public void setBackgroundDownloaded(DownloadStatus value) {
        mBackgroundDownloaded = value;
    }

    public int getAllDownloadStatus() {
        int status = 1; // possible values -1 - failed, 0 - no downloaded, 1 - all done

        for (WidgetUIImage obj : mImages) {
            if (status != 1)
                break;
            if (obj.getDownloadStatus() == DownloadStatus.NOT_DOWNLOADED)
                status = 0;
            if (obj.getDownloadStatus() == DownloadStatus.FAILED)
                status = -1;
        }
        Log.i("DOWNLOAD STATUS", "images: " + status);
        for (WidgetUIButton obj : mButtons) {
            if (status != 1)
                break;
            if (obj.getDownloadStatus() == DownloadStatus.NOT_DOWNLOADED)
                status = 0;
            if (obj.getDownloadStatus() == DownloadStatus.FAILED)
                status = -1;
        }
        Log.i("DOWNLOAD STATUS", "buttons: " + status);
        for (WidgetUILabel obj : mLabels) {
            if (status != 1)
                break;
            if (obj.getDownloadStatus() == DownloadStatus.NOT_DOWNLOADED)
                status = 0;
            if (obj.getDownloadStatus() == DownloadStatus.FAILED)
                status = -1;
        }
        Log.i("DOWNLOAD STATUS", "labels: " + status);
        for (WidgetUITab obj : mTabs) {
            if (status != 1)
                break;
            if (obj.getDownloadStatus() == DownloadStatus.NOT_DOWNLOADED)
                status = 0;
            if (obj.getDownloadStatus() == DownloadStatus.FAILED)
                status = -1;
        }
        Log.i("DOWNLOAD STATUS", "tabs: " + status);

        if (mBackgroundDownloaded == DownloadStatus.NOT_DOWNLOADED)
            status = 0;
        if (mBackgroundDownloaded == DownloadStatus.FAILED)
            status = -1;
        Log.i("DOWNLOAD STATUS", "background: " + status);

        return status;
    }

    /**
     * @return the mBackgorundImageData
     */
    public String getmBackgorundImageData() {
        return mBackgorundImageData;
    }

    /**
     * @param mBackgorundImageData the mBackgorundImageData to set
     */
    public void setmBackgorundImageData(String mBackgorundImageData) {
        this.mBackgorundImageData = mBackgorundImageData;
    }

    /**
     * @return the defaultOrder
     */
    public int getDefaultOrder() {
        return defaultOrder;
    }

    /**
     * @param defaultOrder the defaultOrder to set
     */
    public void setDefaultOrder(int defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public boolean hasDefaultOrder() {
        return defaultOrder != -1;
    }

    public String getGoogleAnalyticsId() {
        return googleAnalyticsId;
    }

    public void setGoogleAnalyticsId(String googleAnalyticsId) {
        this.googleAnalyticsId = googleAnalyticsId;
    }

    public LoginScreen getLoginScreen() {
        return loginScreen;
    }

    public void setLoginScreen(LoginScreen loginScreen) {
        this.loginScreen = loginScreen;
    }

    /**
     * @return the updateContentPushEnabled
     */
    public boolean isUpdateContentPushEnabled() {
        return updateContentPushEnabled;
    }

    /**
     * @param updateContentPushEnabled the updateContentPushEnabled to set
     */
    public void setUpdateContentPushEnabled(boolean updateContentPushEnabled) {
        this.updateContentPushEnabled = updateContentPushEnabled;
    }

    public void cleanWidgets(){
        if(mWidgets != null){
            for(int i = 0; i < mWidgets.size(); i++){
                mWidgets.get(i).setUpdated(false);
            }
        }
    }

    public void setLoginForm(LoginForm loginForm) {
        this.loginForm = loginForm;
    }

    public LoginForm getLoginForm() {
        return loginForm;
    }

    public void addSidebarItem(WidgetUISidebarItem sidebarItem) {
        sidebarItems.add(sidebarItem);
    }

    public ArrayList<WidgetUISidebarItem> getSidebarItems() {
        return sidebarItems;
    }
}
