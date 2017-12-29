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

/**
 * Created by Artem on 11.02.14.
 */
public class LoginScreen implements Serializable {
    String logo;
    Boolean allowSignup;
    String appId;

    Boolean useFacebook;
    Boolean useTwitter;
    Boolean useEmail;

    String signupEndpoint;
    String loginEndpoint;
    String recoveryPasswordEndpoint;


    public LoginScreen() {
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Boolean getAllowSignup() {
        return allowSignup;
    }

    public void setAllowSignup(Boolean allowSignup) {
        this.allowSignup = allowSignup;
    }

    public Boolean getUseFacebook() {
        return useFacebook;
    }

    public void setUseFacebook(Boolean useFacebook) {
        this.useFacebook = useFacebook;
    }

    public Boolean getUseTwitter() {
        return useTwitter;
    }

    public void setUseTwitter(Boolean useTwitter) {
        this.useTwitter = useTwitter;
    }

    public Boolean getUseEmail() {
        return useEmail;
    }

    public void setUseEmail(Boolean useEmail) {
        this.useEmail = useEmail;
    }

    public String getSignupEndpoint() {
        return signupEndpoint;
    }

    public void setSignupEndpoint(String signupEndpoint) {
        this.signupEndpoint = signupEndpoint;
    }

    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public void setLoginEndpoint(String loginEndpoint) {
        this.loginEndpoint = loginEndpoint;
    }

    public String getRecoveryPasswordEndpoint() {
        return recoveryPasswordEndpoint;
    }

    public void setRecoveryPasswordEndpoint(String recoveryPasswordEndpoint) {
        this.recoveryPasswordEndpoint = recoveryPasswordEndpoint;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
