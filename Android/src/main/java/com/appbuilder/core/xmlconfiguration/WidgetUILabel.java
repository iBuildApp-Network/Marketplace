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


public class WidgetUILabel extends AppConfigureItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String mTitle = "";
	private int mFontSize = -1;
	private String mColor = "";
	private String mStyle = "";
    private String mAlign = "";
	private int width = 0;
	private int height = 0;
	private int left = 0;
	private int top = 0;

    public String getAlign() { return mAlign; }
    public void setAlign(String mAlign) { this.mAlign = mAlign; }

    public WidgetUILabel() {
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

	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String value) {
		mTitle = value;
	}
	
	public int getFontSize() {
		return mFontSize;
	}
	
	public void setFontSize(int value) {
		mFontSize = value;
	}
	
	public String getColor() {
		return mColor;
	}
	
	public void setColor(String value) {
		mColor = value;
	}
	
	public String getStyle() {
		return mStyle;
	}
	
	public void setStyle(String value) {
		mStyle = value;
	}
	
	
}
