package com.appbuilder.core.LoginScreen.service;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Created by Artem on 12.02.14.
 */
public class LoginSettingsService {

    public static String PASSWORD_SETTING = "PASSWORD_SETTING";
    public static String LOGIN_SETTING = "LOGIN_SETTING";

    public static void saveSettings(SharedPreferences preferences, LoginSettings settings) {
        SharedPreferences.Editor editor = preferences.edit();
        editor = editor.putString(LOGIN_SETTING, settings.getUsername());
        editor = editor.putString(PASSWORD_SETTING, settings.getPassword());
        editor.commit();
    }

    public static void deleteSettings(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor = editor.remove(LOGIN_SETTING);
        editor = editor.remove(PASSWORD_SETTING);
        editor.commit();
    }

    public static LoginSettings loadSettings(SharedPreferences preferences) {
        return new LoginSettings(preferences.getString(LOGIN_SETTING, ""),
                preferences.getString(PASSWORD_SETTING, ""));
    }
}
