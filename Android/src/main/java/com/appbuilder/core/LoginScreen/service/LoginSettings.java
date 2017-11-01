package com.appbuilder.core.LoginScreen.service;

/**
 * Created by Artem on 12.02.14.
 */
public class LoginSettings {
    private String username;
    private String password;

    public LoginSettings(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
