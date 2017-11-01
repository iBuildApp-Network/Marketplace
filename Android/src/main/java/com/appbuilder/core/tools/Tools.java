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
package com.appbuilder.core.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.core.xmlconfiguration.AppConfigureParser;
import com.appbuilder.sdk.android.Widget;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 28.03.14
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public class Tools {

    /**
     * Функция проверяет наличие интернет соединения
     *
     * @param context - контекст приложения
     * @return -1 - соединение отсутствует
     *         1 - WI-FI соединение
     *         2- мобильный интернет
     */
    public static int checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            try {
                NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobile.isConnected() && !wifi.isConnectedOrConnecting()) {
                    return 2;
                }
            } catch (Exception ex) {
            }
            return 1;
        }
        return -1;
    }

    /**
     * Функция выкачивает xml с сервера и парсит его в настроечный
     * Далее считываем старый настроечный файлл и обновляет у него поле widgets
     *
     * @param context - контекст
     * @param xmlUrl  - ссылка на xml
     * @return обновленный объект настроек обновлен ,null - иначе
     * @link функция работает с тырнетом, поэтому запускать в отдельном потоке!!!
     */
    public static AppConfigure updateAppConfig(Context context, String xmlUrl) {
        final int BUFFER_SIZE = 1024;
        AppConfigure appConfig = null;
        try {
            // ***************************************
            // выкачиваем XML с сервере
            URL url = new URL(xmlUrl);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buf[] = new byte[BUFFER_SIZE];
            int flag = 0;
            while ((flag = is.read(buf, 0, BUFFER_SIZE)) != -1) {
                baos.write(buf, 0, flag);
                Arrays.fill(buf, (byte) 0);
            }
            String builtInXml = baos.toString();

            // ***************************************
            // проверка на well-formed
            boolean isXMLWellFormed = true;
            try {
                Xml.parse(builtInXml, null);
            } catch (Exception e) {
                isXMLWellFormed = false;
            }

            // ***************************************
            // если проверка на well-formed прошла успешно -> парсим и позвращаем результат
            if (isXMLWellFormed) {
                AppConfigureParser acp = new AppConfigureParser(context, builtInXml);
                appConfig = acp.parseSAX();
                if (appConfig != null) {
                    // ***************************************
                    // формируем путь в кэш файлу
                    String cachePath = Environment.getExternalStorageDirectory() + "/AppBuilder/" + com.appbuilder.sdk.android.Statics.cachePath;
                    File cache = new File(cachePath + "/cache.data");

                    // если кеш файл есть - считываем его и обновляем только поле widget
                    AppConfigure oldConfig = null;
                    if (cache.exists()) {
                        try {
                            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
                            oldConfig = (AppConfigure) ois.readObject();
                            ois.close();
                            oldConfig.clearWidgets();

                            // перетираем все виджеты у концигурации и записываем новые
                            for (Widget w : appConfig.getmWidgets())
                                oldConfig.addWidget(w);

                            // сериализуем настроечный файл на диск
                            cache.delete();
                            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
                            oos.writeObject(oldConfig);
                            oos.close();

                        } catch (Exception e) {
                            Log.e("", "");
                        }
                    }

                    return oldConfig;
                } else
                    return null;
            } else
                return null;

        } catch (Exception e) {
            return null;
        }
    }
}
