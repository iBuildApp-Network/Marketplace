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

public class WidgetUIImage extends AppConfigureItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String mSourceUrl = "";
	private String mSourceCache = "";
	private String mImageData = "";
	private String mImageData_res = "";
	private int width = 0;
	private int height = 0;
	private int left = 0;
	private int top = 0;
	
	public WidgetUIImage() {
	}

	public String getmImageData_res() {
		return mImageData_res;
	}

	public void setmImageData_res(String mImageData_res) {
		this.mImageData_res = mImageData_res;
	}

	public void setHeight(int value) {
		height = value;
	}
	public int getHeight() {
		return height;
	}

	public void setWidth(int value) {
		width = value;
	}
	public int getWidth() {
		return width;
	}
	
	public void setTop(int value) {
		top = value;
	}
	public int getTop() {
		return top;
	}

	public void setLeft(int value) {
		left = value;
	}
	public int getLeft() {
		return left;
	}

	public void setSourceUrl(String value) {
		mSourceUrl = value;
	}
	public String getSourceUrl() {
		return mSourceUrl;
	}
	

	public void setSourceCache(String value) {
		mSourceCache = value;
	}
	public String getSourceCache() {
		return mSourceCache;
	}

    /**
     * @return the mImageData
     */
    public String getmImageData() {
        return mImageData;
    }

    /**
     * @param mImageData the mImageData to set
     */
    public void setmImageData(String mImageData) {
        this.mImageData = mImageData;
    }
}