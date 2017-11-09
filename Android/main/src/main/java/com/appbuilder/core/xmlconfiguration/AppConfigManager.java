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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.appbuilder.core.R;
import com.appbuilder.core.Statics;
import com.appbuilder.core.config.ConfigDBHelper;
import com.appbuilder.core.tools.Prefs;
import com.appbuilder.core.tools.Tools;
import com.appbuilder.sdk.android.Base64;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 18.11.14
 * Time: 9:17
 * To change this template use File | Settings | File Templates.
 */
public class AppConfigManager {
    private final static String TAG = AppConfigManager.class.getCanonicalName();
    private final static String CAHCE_DATA_FILE = "cache.data";
    private final static String CAHCE_MD5_FILE = "cache.md5";
    private final static String CAHCE_CONFIG_FILE = "cache.config";
    private final static String SPLASH_FILE_NAME = "splash_screen.png";

    public static class ConfigManagerSettings {

        public static enum CONFIG_SOURCE {
            FROM_BUILDIN, FROM_CACHE, FROM_INTERNET
        }

        public CONFIG_SOURCE source;
        public String cachePath;
        public String xmlUrl;
        public long lastModified;

        public ConfigManagerSettings(CONFIG_SOURCE source, String cachePath, String xmlUrl, long lastModified) {
            this.source = source;
            this.cachePath = cachePath;
            this.xmlUrl = xmlUrl;
            this.lastModified = lastModified;
        }
    }

    public static AppConfigure getConfig(Context context, ConfigManagerSettings settings) {
        switch (settings.source) {

            // *********************************************************************************************************
            case FROM_BUILDIN: {
                AppConfigure resConfig = null;
                System.gc();

                try {
                    InputStream is = new BufferedInputStream(context.getResources().openRawResource(R.raw.configuration));
                    BufferedInputStream mstream = new BufferedInputStream(is);
                    String s = settings.cachePath + File.separator + CAHCE_CONFIG_FILE;
                    FileOutputStream fileOutputStream = new FileOutputStream(settings.cachePath + File.separator + CAHCE_CONFIG_FILE, false);
                    final int BUFFER_SIZE = 2048;
                    byte buf[] = new byte[BUFFER_SIZE];
                    int byteCount;
                    while ((byteCount = mstream.read(buf, 0, BUFFER_SIZE)) != -1)
                        fileOutputStream.write(buf, 0, byteCount);
                    fileOutputStream.flush();


                } catch (IOException e) {
                    Log.e(TAG, "read conf from buildin resource = " + e.getMessage());
                    return null;
                }

                System.gc();

                try {
                    // парсим конфигурацию в объект
                    AppConfigureParser acp = new AppConfigureParser(context, new FileInputStream(settings.cachePath + File.separator + CAHCE_CONFIG_FILE));
                    resConfig = acp.parseSAX();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File file = null;
                Bitmap srcBitmap = null;

                // splashscreen
                AssetManager assetManager = context.getAssets();
                // background ресурс кладем его на диск ( в кеш )
                try {
                    srcBitmap = BitmapFactory.decodeStream(new BufferedInputStream(assetManager.open(resConfig.getmBackgorundImageRes())));
                    if ( srcBitmap != null )
                    {
                        file = new File(settings.cachePath + File.separator + Utils.md5(resConfig.getBackgroundImageUrl()));
                        srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( file.getAbsolutePath() ));
                        srcBitmap.recycle();
                        resConfig.setBackgroundImageCache(file.getAbsolutePath());
                        resConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error decoding base64 background.");
                }

                // для каждой кнопочки вынимаем встроенный ресурс и кладем его на диск ( в кеш )
                // check images
                for (int i = 0; i < resConfig.getButtonsCount(); i++) {
                    WidgetUIButton button = resConfig.getButtonAtIndex(i);
                    try {
                        srcBitmap = BitmapFactory.decodeStream(new BufferedInputStream(assetManager.open(button.getmImageData_res())));
                        if ( srcBitmap != null )
                        {
                            file = new File(settings.cachePath + File.separator + Utils.md5(button.getImageSourceUrl()));
                            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( file.getAbsolutePath() ));
                            srcBitmap.recycle();
                            button.setImageSourceCache(file.getAbsolutePath());
                            button.setDownloadStatus(DownloadStatus.SUCCESS);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error decoding base64 button. Index = " + i);
                    }
                }

                // check images
                for (int i = 0; i < resConfig.getImagesCount(); i++) {
                    WidgetUIImage image = resConfig.getImageAtIndex(i);

                    try {
                        if ( srcBitmap != null )
                        {
                            srcBitmap = BitmapFactory.decodeStream(assetManager.open(image.getmImageData_res()));
                            file = new File(settings.cachePath + File.separator + Utils.md5(image.getSourceUrl()));
                            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( file.getAbsolutePath() ));
                            srcBitmap.recycle();
                            image.setSourceCache(file.getAbsolutePath());
                            image.setDownloadStatus(DownloadStatus.SUCCESS);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error decoding base64 image. Index = " + i);
                    }
                }

                // check tabs
                for (int i = 0; i < resConfig.getTabsCount(); i++) {
                    WidgetUITab tab = resConfig.getTabAtIndex(i);
                    try {

                        srcBitmap = BitmapFactory.decodeStream(assetManager.open(tab.getmIconData_res()));
                        if ( srcBitmap != null )
                        {
                            file = new File(settings.cachePath + File.separator + Utils.md5(tab.getIconUrl()));
                            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( file.getAbsolutePath() ));
                            srcBitmap.recycle();
                            tab.setIconCache(file.getAbsolutePath());
                            tab.setDownloadStatus(DownloadStatus.SUCCESS);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error decoding base64 tab. Index = " + i);
                    }
                }

                // serialize conf
                resConfig.setmAppId(com.appbuilder.sdk.android.Statics.appId);
                ConfigDAO.setConfig(settings.cachePath, CAHCE_DATA_FILE, resConfig);

                // проставляем дату 0 т.к. читали из buildin
                Prefs.with(context).save(resConfig.getmAppId() + "_" + Prefs.PREFERENCE_CONFIG_TIMESTAMP, (long)0);

                return resConfig;
            }

            case FROM_CACHE: {
                return ConfigDAO.getConfig(settings.cachePath, CAHCE_DATA_FILE);
            }

            case FROM_INTERNET: {
                // делаем head запрос на сервер чтобы получить заголовок  App-Config-Last-Modified
                AppConfigure resConfig = null;
                long headerLastMod = 0;
                try {
                    HttpResponse response = new DefaultHttpClient().execute(new HttpHead(settings.xmlUrl));
                    Header[] headerAr = response.getHeaders("App-Config-Last-Modified");
                    if ( headerAr.length > 0 )
                        headerLastMod = Long.valueOf(headerAr[0].getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if ( headerLastMod > settings.lastModified )
                {
                    MessageDigest messageDigest = null;
                    try {
                        messageDigest = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException exception) {
                        exception.printStackTrace();
                    }
                    //String downloadedSrting = "";
                    try { /* reading xml data from url */
                        URL url = new URL(settings.xmlUrl);

                        URLConnection conn = url.openConnection();
                        BufferedInputStream mstream = new BufferedInputStream(conn.getInputStream());
                        new DigestInputStream(mstream, messageDigest);
                        FileOutputStream fileOutputStream = new FileOutputStream(settings.cachePath + File.separator + CAHCE_CONFIG_FILE, false);
                        final int BUFFER_SIZE = 2048;
                        byte buf[] = new byte[BUFFER_SIZE];
                        int byteCount;
                        while ((byteCount = mstream.read(buf, 0, BUFFER_SIZE)) != -1)
                            fileOutputStream.write(buf, 0, byteCount);
                        fileOutputStream.flush();
                    } catch (Exception e) {
                        Log.e(TAG, "downloading conf error = " + e.getMessage() );
                        return null;
                    }

                    System.gc();

                    // грохаем старый md5 и создаем новый
                    try {
//                        String xmlMD5 = Utils.md5(downloadedSrting);
                        byte[] digest = messageDigest != null ? messageDigest.digest() : new byte[0];
                        String xmlMD5 = Utils.toHex(digest);
                        FileWriter writerConfigMd5 = new FileWriter(settings.cachePath + File.separator + CAHCE_MD5_FILE, false);
                        writerConfigMd5.write(xmlMD5);
                        writerConfigMd5.flush();
                        // парсим конфигурацию в объект
//                    AppConfigureParser acp = new AppConfigureParser(context, downloadedSrting);
                        AppConfigureParser acp = new AppConfigureParser(context, new FileInputStream(settings.cachePath + File.separator + CAHCE_CONFIG_FILE));
                        resConfig = acp.parseSAX();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // докачиваем новые ресурсы если необходимо

                    // выкачиваем background
                    File file = null;
                    file = new File(settings.cachePath + File.separator + Utils.md5(resConfig.getBackgroundImageUrl()));
                    if ( file.exists() )
                    {
                        resConfig.setBackgroundImageCache(file.getAbsolutePath());
                        resConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                    } else
                    {
                        String resPath = downloadFile(resConfig.getBackgroundImageUrl(), settings.cachePath);
                        if (!TextUtils.isEmpty(resPath))
                        {
                            resConfig.setBackgroundImageCache(resPath);
                            resConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                            Log.e(TAG, "Background downloaded - successfully");
                        } else
                        {
                            resConfig.setBackgroundImageCache("");
                            resConfig.setBackgroundDownloaded(DownloadStatus.NOT_DOWNLOADED);
                            Log.e(TAG, "Background downloaded - ERROR");
                        }
                    }

                    // выкачиваем splashscreen
                    file = new File(settings.cachePath + File.separator + Utils.md5(resConfig.getSplashScreen()));
                    if ( !file.exists() )
                    {
                        String resPath = downloadFile(resConfig.getSplashScreen(), settings.cachePath);
                        if (!TextUtils.isEmpty(resPath))
                            Log.e(TAG, "splash screen downloaded - successfully");
                        else
                            Log.e(TAG, "splash screen downloaded - ERROR");
                    }

                    // для каждой кнопочки вынимаем встроенный ресурс и кладем его на диск ( в кеш )
                    for (int i = 0; i < resConfig.getButtonsCount(); i++) {
                        WidgetUIButton button = resConfig.getButtonAtIndex(i);
                        file = new File(settings.cachePath + File.separator + Utils.md5(button.getImageSourceUrl()));
                        if ( file.exists() )
                        {
                            button.setImageSourceCache(file.getAbsolutePath());
                            button.setDownloadStatus(DownloadStatus.SUCCESS);
                        } else
                        {
                            String resPath = downloadFile(button.getImageSourceUrl(), settings.cachePath);
                            if (!TextUtils.isEmpty(resPath))
                            {
                                button.setImageSourceCache(resPath);
                                button.setDownloadStatus(DownloadStatus.SUCCESS);
                            } else
                            {
                                button.setImageSourceCache("");
                                button.setDownloadStatus(DownloadStatus.NOT_DOWNLOADED);
                            }
                        }
                    }

                    // check images
                    for (int i = 0; i < resConfig.getImagesCount(); i++) {
                        WidgetUIImage image = resConfig.getImageAtIndex(i);
                        file = new File(settings.cachePath + File.separator + Utils.md5(image.getSourceUrl()));
                        if ( file.exists() )
                        {
                            image.setSourceCache(file.getAbsolutePath());
                            image.setDownloadStatus(DownloadStatus.SUCCESS);
                        } else
                        {
                            String resPath = downloadFile(image.getSourceUrl(), settings.cachePath);
                            if (!TextUtils.isEmpty(resPath))
                            {
                                image.setSourceCache(resPath);
                                image.setDownloadStatus(DownloadStatus.SUCCESS);
                            } else
                            {
                                image.setSourceCache("");
                                image.setDownloadStatus(DownloadStatus.NOT_DOWNLOADED);
                            }
                        }
                    }

                    // check tabs
                    for (int i = 0; i < resConfig.getTabsCount(); i++) {
                        WidgetUITab tab = resConfig.getTabAtIndex(i);
                        file = new File(settings.cachePath + File.separator + Utils.md5(tab.getIconUrl()));
                        if ( file.exists() )
                        {
                            tab.setIconCache(file.getAbsolutePath());
                            tab.setDownloadStatus(DownloadStatus.SUCCESS);
                        } else
                        {
                            String resPath = downloadFile(tab.getIconUrl(), settings.cachePath);
                            if (!TextUtils.isEmpty(resPath))
                            {
                                tab.setIconCache(resPath);
                                tab.setDownloadStatus(DownloadStatus.SUCCESS);
                            } else
                            {
                                tab.setIconCache("");
                                tab.setDownloadStatus(DownloadStatus.NOT_DOWNLOADED);
                            }
                        }
                    }

                    // serialize conf
                    resConfig.setmAppId(com.appbuilder.sdk.android.Statics.appId);
                    ConfigDAO.setConfig(settings.cachePath, CAHCE_DATA_FILE, resConfig);

                    // проставляем дату
                    Prefs.with(context).save(resConfig.getmAppId() + "_" + Prefs.PREFERENCE_CONFIG_TIMESTAMP, headerLastMod);

                    return resConfig;
                } else
                    return null; // конфа старая - нет смысла качать -> бери из кеша
            }
        }

        return null;
    }

    public static String downloadFile(String url, String path) {
        int BYTE_ARRAY_SIZE = 1024;

        // downloading cover image and saving it into file
        try {
            URL imageUrl = new URL(URLDecoder.decode(url));
            URLConnection conn = imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File resFile = new File(path + File.separator + Utils.md5(url));
            FileOutputStream fos = new FileOutputStream(resFile);
            int current = 0;
            byte[] buf = new byte[BYTE_ARRAY_SIZE];
            Arrays.fill(buf, (byte) 0);
            while ((current = bis.read(buf, 0, BYTE_ARRAY_SIZE)) != -1) {
                fos.write(buf, 0, current);
                Arrays.fill(buf, (byte) 0);
            }

            bis.close();
            fos.flush();
            fos.close();
            Log.d("", "");
            return resFile.getAbsolutePath();
        } catch (SocketTimeoutException e) {
            Log.e("API - downloadFile() - SocketTimeoutException", "An error has occurred downloading the image: " + url);
            return null;
        } catch (IllegalArgumentException e) {
            Log.e("API - downloadFile() - IllegalArgumentException", "An error has occurred downloading the image: " + url);
            return null;
        } catch (Exception e) {
            Log.e("API - downloadFile() - Exception", "An error has occurred downloading the image: " + url);
            return null;
        }
    }}
