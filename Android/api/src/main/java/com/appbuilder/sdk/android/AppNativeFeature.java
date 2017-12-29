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

public class AppNativeFeature {

	static public enum SMS {TEXT};
	static public enum EMAIL {ADDRESS, SUBJECT, TEXT};
	static public enum CONTACT {NAME, PHONE, EMAIL, WEBSITE};
	static public enum EVENT {TITLE, BEGIN, END, FREQUENCY};
	static public enum NOTIFICATION {TEXT};
	//static public enum GALLERY {PICTURE_URL};
	
	public AppNativeFeature(){
	}
}
