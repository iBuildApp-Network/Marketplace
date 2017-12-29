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

import java.io.Serializable;

public interface AppBuilderInterface {
	public void create();
	public void start();
	public void restart();
	public void resume();
	public void pause();
	public void stop();
	public void destroy();
	public void setSession(Serializable object);
	public Serializable getSession();
}
