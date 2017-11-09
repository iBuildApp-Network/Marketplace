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

import com.appbuilder.core.xmlconfiguration.AppConfigureItem;

import java.io.Serializable;

public class WidgetUITab extends AppConfigureItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String mLabel = "";
	private String mIconUrl = "";
	private String mIconCache = "";
	private String mIconData = "";
	private String mIconData_res = "";
	private int mOrder = 0;
	
	public WidgetUITab() {}

	public String getmIconData_res() {
		return mIconData_res;
	}

	public void setmIconData_res(String mIconData_res) {
		this.mIconData_res = mIconData_res;
	}

	public void setLabel(String value) {
		mLabel = value;
	}
	public String getLabel() {
		return mLabel;
	}
	
	
	public void setIconUrl(String value) {
		mIconUrl = value;
	}
	public String getIconUrl() {
		return mIconUrl;
	}
	
	
	public void setIconCache(String value) {
		mIconCache = value;
	}
	public String getIconCache() {
		return mIconCache;
	}
	
	
	public void setOrder(int value) {
		mOrder = value;
	}
	public int getOrder() {
		return mOrder;
	}

    /**
     * @return the mIconData
     */
    public String getmIconData() {
        return mIconData;
    }

    /**
     * @param mIconData the mIconData to set
     */
    public void setmIconData(String mIconData) {
        this.mIconData = mIconData;
    }
}
