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
package com.appbuilder.sdk.android.pushnotification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 13.03.14
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class AppPushNotificationDB {
    private static Context context;

    private static String databaseName = "APP_NOTIFICATOINS";
    // название таблиц
    private static String notificationTable = "NOTIFICATION_MESSAGES";
    private static SQLiteDatabase db = null;
    private static String TAG = "com.appbuilder.core.PushNotification.AppPushNotificationDB";

    public static void init(Context contextArg) {
        context = contextArg;

        // объект подключения с БД
        if (db == null) {
            db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        }

        // создаем таблицу для уведомлений
        if (!existTable(notificationTable)) {
            createNotificationTable();
        }
    }

    /**
     * создаем таблицу для хранения push уведомлений
     */
    private static void createNotificationTable() {
        String query = String.format("CREATE TABLE %s ", notificationTable) +
                "( " +
                " ID INTEGER, " +
                " PACKAGE TEXT, " +
                " TITLE TEXT, " +
                " MESSAGE TEXT, " +
                " IMAGE_URL TEXT, " +
                " IMAGE_PATH TEXT, " +
                " NOTIFICATION_DATE INTEGER, " +
                " WIDGET_ORDER INTEGER, " +
                " PACKAGE_EXIST INTEGER, " +
                " CONSTRAINT PK_NOTIFICATION PRIMARY KEY (ID) " +
                ")";
        db.execSQL(query);
    }

    private static boolean existTable(String tableName) {
        String script = "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?";

        Cursor cursor = db.rawQuery(script, new String[]{tableName});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /**
     * выборка всех уведомлений из базы
     *
     * @return список уведомлений
     */
    public static List<AppPushNotificationMessage> selectAllNotifications() {
        List<AppPushNotificationMessage> result = null;

        Cursor cursor;
        cursor = db.query(notificationTable, null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0)
            return new ArrayList<AppPushNotificationMessage>();


        result = new ArrayList<AppPushNotificationMessage>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                result.add(parseNotification(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    /**
     * возвращает самое свежее уведомление, если есть или null если уведомлений нет
     *
     * @return самое свежее уведомление или null
     */
    public static AppPushNotificationMessage getNotificationIfExist() {
        List<AppPushNotificationMessage> result = null;

        Cursor cursor;
        cursor = db.query(notificationTable, null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0)
            return null;

        result = new ArrayList<AppPushNotificationMessage>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                result.add(parseNotification(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // находим саоме свежее уведомление
        AppPushNotificationMessage freshMessage = new AppPushNotificationMessage();
        for (AppPushNotificationMessage msg : result) {
            if (msg.notificationDate.getTime() > freshMessage.notificationDate.getTime())
                freshMessage = msg;
        }

        return freshMessage;
    }


    /**
     * удаление уведомления из базы
     *
     * @return список уведомлений
     */
    public static void deleteItemFromRelations(long id) {
        try {
            int rc = db.delete(notificationTable, "ID = ?", new String[]{String.valueOf(id)});
            logError(String.format("deleteItemFromNotifications = %d", rc));
        } catch (Exception ex) {
            logError(ex);
        }
    }

    /**
     * выборка из базы уведомления по id
     *
     * @param id - уникальный идентификатор записи в базе
     * @return
     */
    private static AppPushNotificationMessage getNotificationById(long id) {
        AppPushNotificationMessage result = null;
        Cursor cursor;

        cursor = db.query(notificationTable, null, "ID = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor == null || cursor.getCount() <= 0)
            return null;

        if (cursor.moveToFirst()) {
            result = parseNotification(cursor);
        }
        cursor.close();

        return result;
    }


    private static AppPushNotificationMessage parseNotification(Cursor cursor) {
        AppPushNotificationMessage entity = new AppPushNotificationMessage();

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).equals("ID")) {
                entity.uid = cursor.getLong(i);
            } else if (cursor.getColumnName(i).equals("MESSAGE")) {
                entity.descriptionText = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("PACKAGE")) {
                entity.packageName = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_URL")) {
                entity.imgUrl = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_PATH")) {
                entity.imagePath = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("TITLE")) {
                entity.titleText = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("NOTIFICATION_DATE")) {
                entity.notificationDate = new Date(cursor.getLong(i));
            } else if (cursor.getColumnName(i).equals("WIDGET_ORDER")) {
                entity.widgetOrder = cursor.getInt(i);
            } else if (cursor.getColumnName(i).equals("PACKAGE_EXIST")) {
                entity.isPackageExist = cursor.getInt(i) == 1 ? true : false;
            }
        }

        return entity;
    }

    /**
     * заполняем объект БД для вставки
     *
     * @param notificationMessage - объект уведомления
     * @return объект для БД
     */
    private static ContentValues fillNotification(AppPushNotificationMessage notificationMessage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("PACKAGE", notificationMessage.packageName);
        contentValues.put("TITLE", notificationMessage.titleText);
        contentValues.put("MESSAGE", notificationMessage.descriptionText);
        contentValues.put("IMAGE_URL", notificationMessage.imgUrl);
        contentValues.put("IMAGE_PATH", notificationMessage.imagePath);
        contentValues.put("NOTIFICATION_DATE", notificationMessage.notificationDate.getTime());
        contentValues.put("WIDGET_ORDER", notificationMessage.widgetOrder);
        contentValues.put("PACKAGE_EXIST", (notificationMessage.isPackageExist == true) ? 1 : 0);

        return contentValues;
    }

    private static void logError(Exception e) {
        Log.e(TAG, "", e);
    }

    private static void logError(String message) {
        Log.e(TAG, message);
    }
}
