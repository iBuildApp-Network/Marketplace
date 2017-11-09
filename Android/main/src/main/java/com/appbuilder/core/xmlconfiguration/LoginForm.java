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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dnevolin on 16.02.2015.
 */
public class LoginForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String endpoint;
    private Map<String, Integer> colorScheme;
    private boolean mainScreen;
    private ArrayList<Integer> widgetList;

    public LoginForm() {
        endpoint = "";
        mainScreen = false;
        colorScheme = new HashMap<String, Integer>();
        widgetList = new ArrayList<Integer>();
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void addSchemeColor(String key, int color) {
        colorScheme.put(key, color);
    }

    public int getColor(String key) {
        return colorScheme.get(key);
    }

    public boolean isMainScreen() {
        return mainScreen;
    }

    public void setMainScreen(boolean mainScreen) {
        this.mainScreen = mainScreen;
    }

    public void addWidget(int widgetId) {
        widgetList.add(widgetId);
    }

    public int getWidget(int index) {
        return widgetList.get(index);
    }

    public boolean containsWidget(int widgetId) {
        return widgetList.contains(widgetId);
    }
}
