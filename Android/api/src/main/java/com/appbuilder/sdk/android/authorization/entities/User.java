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
package com.appbuilder.sdk.android.authorization.entities;

import java.io.Serializable;

public class User implements Serializable{

    /**
     * @return the consumerKey
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * @param consumerKey the consumerKey to set
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * @return the consumerSecret
     */
    public String getConsumerSecret() {
        return consumerSecret;
    }

    /**
     * @param consumerSecret the consumerSecret to set
     */
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }
    
    public enum ACCOUNT_TYPES{FACEBOOK, TWITTER, IBUILDAPP, GUEST, VKONTAKTE, LINKEDIN };
    
    private String userName = "";
    private String userEmail = "";
    private String userFirstName = "";
    private String userLastName = "";
    private String avatarUrl = "";
    private ACCOUNT_TYPES accountType = ACCOUNT_TYPES.IBUILDAPP;
    private String accountId = "";
        
    private String accessToken = "";
    private String accessTokenSecret = ""; //OAuth accesstoken & accesstokensecret
    private String consumerKey = "";
    private String consumerSecret = "";

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private String fullName;

    public String getUserName() {
        if (userName == null) {
            userName = "";
        }
        if (userFirstName == null) {
            userFirstName="";
        }
        if (userLastName==null) {
            userLastName="";
        }
        if(userName.length() > 0){
            return userName;
        }else if((userFirstName.length() > 0) && (userLastName.length() > 0)){
            return userFirstName + " " + userLastName;
        }else if(userFirstName.length() > 0){
            return userFirstName;
        }else if(userLastName.length() > 0){
            return userLastName;
        }else{
            return "";
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public ACCOUNT_TYPES getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        if(accountType.equalsIgnoreCase("facebook")){
            this.accountType = ACCOUNT_TYPES.FACEBOOK;
        }else if(accountType.equalsIgnoreCase("twitter")){
            this.accountType = ACCOUNT_TYPES.TWITTER;
        }else if(accountType.equalsIgnoreCase("ibuildapp")){
            this.accountType = ACCOUNT_TYPES.IBUILDAPP;
        }else if(accountType.equalsIgnoreCase("vkontakte")){
            this.accountType = ACCOUNT_TYPES.VKONTAKTE;
        }else if(accountType.equalsIgnoreCase("linkedin")){
            this.accountType = ACCOUNT_TYPES.LINKEDIN;
        } else
            this.accountType = ACCOUNT_TYPES.GUEST;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }
    
}
