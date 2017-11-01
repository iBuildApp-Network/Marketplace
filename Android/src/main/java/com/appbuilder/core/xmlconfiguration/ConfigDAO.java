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

import android.util.Log;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 18.11.14
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class ConfigDAO {

    private static final String LOG_TAG = ConfigDAO.class.getCanonicalName();

    public static AppConfigure getConfig( String cachePath, String cacheFileName) {
        // DEserialization
        File userCache = new File(cachePath + File.separator + cacheFileName);
        if (userCache.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userCache));
                AppConfigure filter = (AppConfigure) ois.readObject();
                ois.close();
                return filter;
            } catch (Exception e) {
                Log.e(LOG_TAG, "", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static void setConfig(String cachePath, String cacheFileName, AppConfigure config ) {
        // serialization
        File cachePathFile = new File(cachePath);
        if ( !cachePathFile.exists() )
            cachePathFile.mkdirs();

        File cache = new File(cachePath + File.separator + cacheFileName);
        if (cache.exists()) {
            cache.delete();
        }

        try {
            cache.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
            oos.writeObject(config);
            oos.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            cache.delete();
        }

    }
}
