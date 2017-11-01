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

public class AppConfigureItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected DownloadStatus mDownloadStatus = DownloadStatus.NOT_DOWNLOADED;
	
	public DownloadStatus getDownloadStatus() {
		return mDownloadStatus;
	}
	
	public void setDownloadStatus(DownloadStatus value) {
		mDownloadStatus = value;
		//AppConfigure appConfig = AppConfigure.getCurrent();
		//if (appConfig != null) {
		//	appConfig.checkDownloadStatus();
		//}
	}
}
