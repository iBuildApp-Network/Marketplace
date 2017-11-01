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

public class WidgetUIButton extends AppConfigureItem implements Serializable {

    private static final long serialVersionUID = 1L;
    //private Rect mRect;
    private String mImageSourceUrl = "";
    private String mImageSourceCache = "";
    private String mImageData = "";
    private String mImageData_res = "";
    private String mTitle = "";
    private int mFontSize = 12;
    private String mAlign = "";
    private String mVAlign = "";
    private int paddingX = 0;
    private int paddingY = 0;
    private String mColor = "#000";
    private String mStyle = "";
    private int mOrder = 0;
    private int width = 0;
    private int height = 0;
    private int left = 0;
    private int top = 0;

    public WidgetUIButton() {
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

    public void setImageSourceUrl(String value) {
        mImageSourceUrl = value;
    }

    public String getImageSourceUrl() {
        return mImageSourceUrl;
    }

    public void setImageSourceCache(String value) {
        mImageSourceCache = value;
    }

    public String getImageSourceCache() {
        return mImageSourceCache;
    }

    public void setTitle(String value) {
        mTitle = value;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setFontSize(int value) {
        mFontSize = value;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public void setAlign(String value) {
        mAlign = value;
    }

    public String getAlign() {
        return mAlign;
    }

    public void setColor(String value) {
        mColor = value;
    }

    public String getColor() {
        return mColor;
    }

    public void setStyle(String value) {
        mStyle = value;
    }

    public String getStyle() {
        return mStyle;
    }

    public void setOrder(int value) {
        mOrder = value;
    }

    public int getOrder() {
        return mOrder;
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

    /**
     * @return the mVAlign
     */
    public String getVAlign() {
        return mVAlign;
    }

    /**
     * @param mVAlign the mVAlign to set
     */
    public void setVAlign(String mVAlign) {
        this.mVAlign = mVAlign;
    }

    /**
     * @return the paddingX
     */
    public int getPaddingX() {
        return paddingX;
    }

    /**
     * @param paddingX the paddingX to set
     */
    public void setPaddingX(int paddingX) {
        this.paddingX = paddingX;
    }

    /**
     * @return the paddingY
     */
    public int getPaddingY() {
        return paddingY;
    }

    /**
     * @param paddingY the paddingY to set
     */
    public void setPaddingY(int paddingY) {
        this.paddingY = paddingY;
    }
    
}
