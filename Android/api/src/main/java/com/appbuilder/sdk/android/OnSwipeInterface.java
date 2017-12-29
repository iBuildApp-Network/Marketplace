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

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 03.06.13
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public interface OnSwipeInterface{
    public void onSwipeLeft();
    public void onSwipeRight();
    public void onSwipeTop();
    public void onSwipeBottom();
    public boolean onTouchEvent( float x);
}
