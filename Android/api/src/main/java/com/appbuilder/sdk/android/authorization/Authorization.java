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
package com.appbuilder.sdk.android.authorization;

import android.app.Activity;
import android.content.Intent;
import com.appbuilder.sdk.android.authorization.entities.User;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Authorization {

    public static final int AUTHORIZATION_TYPE_ANY = 0;
    public static final int AUTHORIZATION_TYPE_FACEBOOK = 1;
    public static final int AUTHORIZATION_TYPE_TWITTER = 2;
    public static final int AUTHORIZATION_TYPE_IBUILDAPP = 3;
    public static final int AUTHORIZATION_TYPE_VKONTAKTE = 4;
    public static final int AUTHORIZATION_TYPE_LINKEDIN = 5;


    static User primaryUser = null;
    static User facebookUser = null;
    static User twitterUser = null;
    static User ibuildappUser = null;
    public static User vkontakteUser = null;
    public static User linkedinUser = null;

    public static boolean isAuthorized() {
        return primaryUser != null;
    }

    public static boolean isAuthorized(int authorizationType) {
        switch (authorizationType) {
            case AUTHORIZATION_TYPE_ANY: {
                return primaryUser != null;
            }
            case AUTHORIZATION_TYPE_FACEBOOK: {
                return facebookUser != null;
            }
            case AUTHORIZATION_TYPE_TWITTER: {
                return twitterUser != null;
            }
            case AUTHORIZATION_TYPE_IBUILDAPP: {
                return ibuildappUser != null;
            }
            case AUTHORIZATION_TYPE_LINKEDIN: {
                return linkedinUser != null;
            }
            case AUTHORIZATION_TYPE_VKONTAKTE: {
                return vkontakteUser != null;
            }
        }

        return false;
    }

    public static void authorize(Activity ctx, int requestCode, int authorizationType) {
        switch (authorizationType) {
            case AUTHORIZATION_TYPE_FACEBOOK: {
                Intent it = new Intent(ctx, FacebookAuthorizationActivity.class);
                ctx.startActivityForResult(it, requestCode);
                return;
            }
            case AUTHORIZATION_TYPE_TWITTER: {
                Intent it = new Intent(ctx, TwitterAuthorizationActivity.class);
                ctx.startActivityForResult(it, requestCode);
                return;
            }
            case AUTHORIZATION_TYPE_LINKEDIN: {
                Intent it = new Intent(ctx, LinkedInAuthorizationActivity.class);
                ctx.startActivityForResult(it, requestCode);
                return;
            }
            case AUTHORIZATION_TYPE_VKONTAKTE: {
                Intent it = new Intent(ctx, VkontakteAuthorizationActivity.class);
                ctx.startActivityForResult(it, requestCode);
                return;
            }
        }

        throw new IllegalArgumentException("You must provide correct authorization type (facebook or twitter).");
    }

    public static boolean authorizeEmail(String login, String pass) {
        if (ibuildappUser != null) {
            throw new IllegalStateException("Already authorized");
        }

        String loginUrl = "http://" + com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/modules/user/login";

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 15000);
        HttpConnectionParams.setSoTimeout(params, 15000);
        HttpClient httpClient = new DefaultHttpClient(params);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        try {
            HttpPost httpPost = new HttpPost(loginUrl);

            // order details
            nameValuePairs.add(new BasicNameValuePair("login", login));
            nameValuePairs.add(new BasicNameValuePair("password", pass));
            nameValuePairs.add(new BasicNameValuePair("app_id", com.appbuilder.sdk.android.Statics.appId));
            nameValuePairs.add(new BasicNameValuePair("token", com.appbuilder.sdk.android.Statics.appToken));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            String resp = httpClient.execute(httpPost, new BasicResponseHandler());

            if (resp == null) {
                return false;
            }

            if (resp.length() == 0) {
                return false;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONObject dataObject = mainObject.getJSONObject("data");

            User fwUser = new User();
            fwUser.setAccountId(dataObject.getString("user_id"));
            fwUser.setUserName(dataObject.getString("username"));
            fwUser.setAvatarUrl(dataObject.getString("user_avatar"));
            fwUser.setAccountType("ibuildapp");

            if (primaryUser == null) {
                primaryUser = fwUser;
            }

            ibuildappUser = fwUser;

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean registerEmail(String firstName, String lastName,
                                        String email, String password, String rePassword) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        HttpClient httpClient = new DefaultHttpClient(params);

        try {
            HttpPost httpPost = new HttpPost("http://" +
                    com.appbuilder.sdk.android.Statics.BASE_DOMEN +
                    //"ibuilder.solovathost.com"+
                    "/modules/user/signup");

            String firstNameString = firstName;
            String lastNameString = lastName;
            String emailString = email;
            String passwordString = password;
            String rePasswordString = rePassword;
                  
            /*MultipartEntity multipartEntity = new MultipartEntity();
            multipartEntity.addPart("firstname", new StringBody(firstNameString, Charset.forName("UTF-8")));
            multipartEntity.addPart("lastname", new StringBody(lastNameString, Charset.forName("UTF-8")));
            multipartEntity.addPart("email", new StringBody(emailString, Charset.forName("UTF-8")));
            multipartEntity.addPart("password", new StringBody(passwordString, Charset.forName("UTF-8")));
            multipartEntity.addPart("password_confirm", new StringBody(rePasswordString, Charset.forName("UTF-8")));*/

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("firstname", firstNameString));
            nameValuePairs.add(new BasicNameValuePair("lastname", lastNameString));
            nameValuePairs.add(new BasicNameValuePair("email", emailString));
            nameValuePairs.add(new BasicNameValuePair("password", passwordString));
            nameValuePairs.add(new BasicNameValuePair("password_confirm", rePasswordString));
            nameValuePairs.add(new BasicNameValuePair("app_id", com.appbuilder.sdk.android.Statics.appId));
            nameValuePairs.add(new BasicNameValuePair("token", com.appbuilder.sdk.android.Statics.appToken));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            String resp = httpClient.execute(httpPost, new BasicResponseHandler());

            if (resp == null) {
                return false;
            }

            if (resp.length() == 0) {
                return false;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONObject dataObject = mainObject.getJSONObject("data");

            User fwUser = new User();
            fwUser.setAccountId(dataObject.getString("user_id"));
            fwUser.setUserName(dataObject.getString("username"));
            fwUser.setAvatarUrl(dataObject.getString("user_avatar"));
            fwUser.setAccountType("ibuildapp");
            fwUser.setUserEmail(emailString);

            if (primaryUser == null) {
                primaryUser = fwUser;
            }

            ibuildappUser = fwUser;

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void unauthorize() {
        primaryUser = null;
        facebookUser = null;
        twitterUser = null;
        ibuildappUser = null;
        vkontakteUser = null;
        linkedinUser = null;
    }

    public static void unauthorize(int authorizationType) {
        unauthorize(authorizationType, true);
    }

    public static void unauthorize(int authorizationType, boolean changeAuthorization) {
        switch (authorizationType) {
            case AUTHORIZATION_TYPE_ANY: {
                unauthorize();
            }
            break;
            case AUTHORIZATION_TYPE_FACEBOOK: {
                facebookUser = null;

                if (primaryUser.getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
                    primaryUser = null;

                    if (changeAuthorization) {
                        if (twitterUser != null) {
                            primaryUser = twitterUser;
                            return;
                        }

                        if (ibuildappUser != null) {
                            primaryUser = ibuildappUser;
                            return;
                        }
                    }
                }
            }
            break;
            case AUTHORIZATION_TYPE_TWITTER: {
                twitterUser = null;

                if (primaryUser.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
                    primaryUser = null;

                    if (changeAuthorization) {
                        if (facebookUser != null) {
                            primaryUser = facebookUser;
                            return;
                        }

                        if (ibuildappUser != null) {
                            primaryUser = ibuildappUser;
                            return;
                        }
                    }
                }
            }
            break;
            case AUTHORIZATION_TYPE_IBUILDAPP: {
                ibuildappUser = null;

                if (primaryUser.getAccountType() == User.ACCOUNT_TYPES.IBUILDAPP) {
                    primaryUser = null;

                    if (changeAuthorization) {
                        if (facebookUser != null) {
                            primaryUser = facebookUser;
                            return;
                        }

                        if (twitterUser != null) {
                            primaryUser = twitterUser;
                            return;
                        }
                    }
                }
            }
            break;
            case AUTHORIZATION_TYPE_LINKEDIN: {
                linkedinUser = null;

                if (primaryUser.getAccountType() == User.ACCOUNT_TYPES.LINKEDIN) {
                    primaryUser = null;

                    if (changeAuthorization) {
                        if (facebookUser != null) {
                            primaryUser = facebookUser;
                            return;
                        }

                        if (twitterUser != null) {
                            primaryUser = twitterUser;
                            return;
                        }
                    }
                }

            }
            case AUTHORIZATION_TYPE_VKONTAKTE: {
                vkontakteUser = null;
                if (primaryUser.getAccountType() == User.ACCOUNT_TYPES.VKONTAKTE) {
                    primaryUser = null;

                    if (changeAuthorization) {
                        if (facebookUser != null) {
                            primaryUser = facebookUser;
                            return;
                        }

                        if (twitterUser != null) {
                            primaryUser = twitterUser;
                            return;
                        }
                    }
                }
            }
        }
    }

    public static void setPrimaryUser(int authorizationType) {
        switch (authorizationType) {
            case AUTHORIZATION_TYPE_FACEBOOK: {
                if (facebookUser == null) {
                    throw new IllegalStateException("There is no facebook authorization");
                } else {
                    primaryUser = facebookUser;
                    return;
                }
            }
            case AUTHORIZATION_TYPE_TWITTER: {
                if (twitterUser == null) {
                    throw new IllegalStateException("There is no twitter authorization");
                } else {
                    primaryUser = twitterUser;
                    return;
                }
            }
            case AUTHORIZATION_TYPE_IBUILDAPP: {
                if (ibuildappUser == null) {
                    throw new IllegalStateException("There is no email authorization");
                } else {
                    primaryUser = ibuildappUser;
                    return;
                }
            }
            case AUTHORIZATION_TYPE_LINKEDIN: {
                if (linkedinUser == null) {
                    throw new IllegalStateException("There is no linkedin authorization");
                } else {
                    primaryUser = linkedinUser;
                    return;
                }
            }
            case AUTHORIZATION_TYPE_VKONTAKTE: {
                if (vkontakteUser == null) {
                    throw new IllegalStateException("There is no vkontakte authorization");
                } else {
                    primaryUser = vkontakteUser;
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Incorrect authorization type");
    }

    public static User getAuthorizedUser() {
        return primaryUser;
    }

    public static User getAuthorizedUser(int authorizationType) {
        switch (authorizationType) {
            case AUTHORIZATION_TYPE_ANY: {
                return primaryUser;
            }
            case AUTHORIZATION_TYPE_FACEBOOK: {
                return facebookUser;
            }
            case AUTHORIZATION_TYPE_TWITTER: {
                return twitterUser;
            }
            case AUTHORIZATION_TYPE_IBUILDAPP: {
                return ibuildappUser;
            }
            case AUTHORIZATION_TYPE_LINKEDIN: {
                return linkedinUser;
            }
            case AUTHORIZATION_TYPE_VKONTAKTE: {
                return vkontakteUser;
            }
        }

        return null;
    }

}
