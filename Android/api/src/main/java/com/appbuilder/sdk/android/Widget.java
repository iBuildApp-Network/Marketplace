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

import android.graphics.Color;
import android.os.Environment;
import android.view.View;

import java.io.*;
import java.util.HashMap;

/**
 * This class describe xml representation of module
 * F.E.
 * <p/>
 * <widget>
 * <order>3</order>
 * <name>WebPlugin</name>
 * <package>com.ibuildapp.romanblack.WebPlugin</package>
 * <hash>0001</hash>
 * <url>UNKNOWN</url>
 * <title><![CDATA[ABout us]]></title>
 * <data><![CDATA[
 * PGRhdGE+CiAgICAgICAgIDxjb250ZW50IHNyYz0iaHR0cDovL2lidWlsZGFwcC5jb20vZXh0L2NlYy9zY3JlZW4yLmh0bWwiPjwvY29udGVudD4KPC9kYXRhPgo=
 * ]]></data>
 * </widget>
 */

public class Widget implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean mHaveAdvertisement = true;

    private int mOrder = -1;
    private int widgetId = -1;
    private String mPluginType = "";        // for Google Analitics
    private String mPluginName = "";
    private String mPluginPackage = "";
    private String mPluginUrl = "";
    private String mPluginHash = "";
    private String mPluginXmlData = "";
    private String pluginType = "";
    private String pluginId = "0";
    private String background = "";
    private String subtitle = "";
    private String faviconURL = "";
    private String faviconFilePath = "";
    private boolean addToSidebar = false;
    private boolean updated = false;

    private boolean drawSharing;
    private int iconResourceId;
    private String label;
    private boolean hidden;

    private int textColor = Color.TRANSPARENT;
    private int backgroundColor = Color.TRANSPARENT;
    private int dateFormat = 0;
    private String appname = "AppBuilder";
    private String title = "";

    private String cachePath = "";
    private HashMap<String, Object> params = new HashMap<String, Object>();

    private boolean largeXml;

    public String getPathToXmlFile() {
        return pathToXmlFile;
    }

    public void setPathToXmlFile(String pathToXmlFile) {
        this.pathToXmlFile = pathToXmlFile;
    }

    private String pathToXmlFile;

    public Widget() {
    }

    public boolean isAddToSidebar() {
        return addToSidebar;
    }

    public String getmPluginType() {
        return mPluginType;
    }

    public void setmPluginType(String mPluginType) {
        this.mPluginType = mPluginType;
    }

    public void setAddToSidebar(boolean addToSidebar) {
        this.addToSidebar = addToSidebar;
    }

    public void setFaviconFilePath(String faviconFilePath) {
        this.faviconFilePath = faviconFilePath;
    }

    public String getFaviconFilePath() {
        return faviconFilePath;
    }

    public String getFaviconURL() {
        return faviconURL;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setFaviconURL(String faviconURL) {
        this.faviconURL = faviconURL;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int value) {
        mOrder = value;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String value) {
        cachePath = value;
    }

    public String getPluginName() {
        return mPluginName;
    }

    public void setPluginName(String value) {
        mPluginName = value;
    }

    public String getPluginPackage() {
        return mPluginPackage;
    }

    public void setPluginPackage(String value) {
        mPluginPackage = value;
    }

    public String getPluginUrl() {
        return mPluginUrl;
    }

    public void setPluginUrl(String value) {
        mPluginUrl = value;
    }

    public String getPluginHash() {
        return mPluginHash;
    }

    public void setPluginHash(String value) {
        mPluginHash = value;
    }

    public String getPluginXmlData() {
        return getmPluginXmlData();
    }

    public void setPluginXmlData(String value) {
        setmPluginXmlData(Utils.fromBase64(value));
    }

    public void setNormalPluginXmlData(String value) {
        setmPluginXmlData(value);
    }

    public void setTextColor(String color) {
        try {
            textColor = Color.parseColor(color.toUpperCase());
        } catch (IllegalArgumentException e) {
            textColor = Color.TRANSPARENT;
        }
    }

    public int getTextColor() {
        return textColor;
    }

    public void setBackground(String color) {
        background = color;
        /*backgroundColor = Color.TRANSPARENT;
        if(color.length() > 0){
            try {
                backgroundColor = Color.parseColor(color.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }*/
    }

    public int getBackgroundColor() {
        try {
            return Color.parseColor(background.toUpperCase());
        } catch (IllegalArgumentException iAEx) {
            return Color.TRANSPARENT;
        } catch (StringIndexOutOfBoundsException sIOOBEx) {
            return Color.TRANSPARENT;
        }
        //return backgroundColor;
    }

    public String getBackgroundURL() {
        return background;
    }

    public boolean isBackgroundColor() {
        if ((background.length() == 6) || (background.length() == 7)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBackgroundURL() {
        if (background.contains("http://") || background.contains("https://")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBackgroundInAssets() {
        if (isBackgroundColor()) {
            return false;
        } else {
            if (isBackgroundURL()) {
                return false;
            } else {
                return true;
            }
        }
    }

    public void setDateFormat(int value) {
        //dateFormat = (value != 0 || value != 1) ? 0 : 1;
        switch(value){
            case 0:
            case 1:
                dateFormat = value;
                break;
            default:
                dateFormat = 0;
        }
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setAppName(String value) {
        appname = value;
    }

    public String getAppName() {
        return appname;
    }

    /* methods for extra features */
    public void addParameter(String key, Object value) {
        params.put(key, value);
    }

    public Object getParameter(String key) {
        return (params.containsKey(key)) ? params.get(key) : null;
    }

    public boolean hasParameter(String key) {
        return (params.containsKey(key)) ? true : false;
    }

    /**
     * @return the mHaveAdvertisement
     */
    public boolean isHaveAdvertisement() {
        return mHaveAdvertisement;
    }

    /**
     * @param mHaveAdvertisement the mHaveAdvertisement to set
     */
    public void setHaveAdvertisement(boolean mHaveAdvertisement) {
        this.mHaveAdvertisement = mHaveAdvertisement;
    }

    /**
     * @return the pluginType
     */
    public String getPluginType() {
        return pluginType;
    }

    /**
     * @param pluginType the pluginType to set
     */
    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    /**
     * @return the pluginId
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * @param pluginId the pluginId to set
     */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public Widget(Widget w) {
        this.mHaveAdvertisement = w.mHaveAdvertisement;
        this.mOrder = w.mOrder;
        this.mPluginName = w.mPluginName;
        this.mPluginPackage = w.mPluginPackage;
        this.mPluginUrl = w.mPluginUrl;
        this.mPluginHash = w.mPluginHash;
        setmPluginXmlData(w.getmPluginXmlData());
        this.pluginType = w.pluginType;
        this.pluginId = w.pluginId;
        this.background = w.background;
        this.subtitle = w.subtitle;
        this.faviconURL = w.faviconURL;
        this.faviconFilePath = w.faviconFilePath;
        this.addToSidebar = w.addToSidebar;
        this.textColor = w.textColor;
        this.backgroundColor = w.backgroundColor;
        this.dateFormat = w.dateFormat;
        this.appname = w.appname;
        this.title = w.title;
        this.cachePath = w.cachePath;
        this.params = (HashMap<String, Object>) w.params.clone();
        this.label = w.label;
    }

    /**
     * @return the widgetId
     */
    public int getWidgetId() {
        return widgetId;
    }

    /**
     * @param widgetId the widgetId to set
     */
    public void setWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }

    /**
     * @return the updated
     */
    public boolean isUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    private void setmPluginXmlData(String mPluginXmlData) {
        this.mPluginXmlData = mPluginXmlData;
        if(largeXml = this.mPluginXmlData.length() > 50000)
            mPluginDataToFile();
    }

    private String getmPluginXmlData() {
        return largeXml ? mPluginDataFromFile() : mPluginXmlData;
    }

    private void mPluginDataToFile() {
        String path = Environment.getExternalStorageDirectory() + "/AppBuilder/" + Statics.cachePath + "cache/" + mOrder;
        File dirs = new File(path);
        if (!dirs.exists())
            dirs.mkdirs();

        path += "/mPluginXmlData";

        try {
            File file = new File(path);
            if (!file.createNewFile()) {
                file.delete();
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(mPluginXmlData);
            fileWriter.flush();
            fileWriter.close();

            mPluginXmlData = path;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String mPluginDataFromFile() {
        String path = mPluginXmlData;
        String mPluginXmlData = "";

        try {
            File file = new File(path);
            if (!file.exists())
                return this.mPluginXmlData;

            StringBuilder result = new StringBuilder();
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String cache;

            while ((cache = bufferedReader.readLine()) != null)
                result.append(cache);

            fileReader.close();

            mPluginXmlData = result.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return mPluginXmlData;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public boolean isDrawSharing() {
        return drawSharing;
    }

    public void setDrawSharing(boolean drawSharing) {
        this.drawSharing = drawSharing;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

}
