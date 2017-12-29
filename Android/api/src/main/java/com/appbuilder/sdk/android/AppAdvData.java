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

public class AppAdvData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int AD_VISIBLE = 1;
	public static final int AD_HIDDEN = 2;
	
        private String advSessionUid = "";
	private String advType = "";
	private String advContent = "";
	private String advRedirect = "";
        private String advApId = "";
        private int advAdSpaceId = 0;
        private int advPublisherId = 0;
        
	private int advState = AD_VISIBLE;
        
        

	public AppAdvData(){
            advSessionUid = "" + System.currentTimeMillis();
	}

	
	public int getAdvState(){
		return advState;
	}
	public void setAdvState(int value){
		if(value != AD_VISIBLE && value != AD_HIDDEN)
			value = AD_VISIBLE;
		advState = value;
	}
	
	public String getAdvType(){
		return advType;
	}
	public void setAdvType(String value){
		advType = value;
	}

	public String getAdvContent(){
		return advContent;
	}
	public void setAdvContent(String value){
		advContent = value;
	}
	
	public String getAdvRedirect(){
		return advRedirect;
	}
	public void setAdvRedirect(String value){
		advRedirect = value;
	}
        
        public String getAdvSessionUid(){
            return advSessionUid;
        }
        
    /**
     * @return the advApId
     */
    public String getAdvApId() {
        return advApId;
    }

    /**
     * @param advApId the advApId to set
     */
    public void setAdvApId(String advApId) {
        this.advApId = advApId;
    }

    /**
     * @return the advAdSpaceId
     */
    public int getAdvAdSpaceId() {
        return advAdSpaceId;
    }

    /**
     * @param advAdSpaceId the advAdSpaceId to set
     */
    public void setAdvAdSpaceId(int advAdSpaceId) {
        this.advAdSpaceId = advAdSpaceId;
    }
    
    public void setAdvAdSpaceId(String advAdSpaceId){
        this.advAdSpaceId = Integer.parseInt(advAdSpaceId);
    }

    /**
     * @return the advPublisherId
     */
    public int getAdvPublisherId() {
        return advPublisherId;
    }

    /**
     * @param advPublisherId the advPublisherId to set
     */
    public void setAdvPublisherId(int advPublisherId) {
        this.advPublisherId = advPublisherId;
    }
    
    public void setAdvPublisherId(String advPublisherId){
        this.advPublisherId = Integer.parseInt(advPublisherId);
    }
}
