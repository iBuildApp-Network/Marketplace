package com.appbuilder.core.PushNotification;

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
    private static String xmlUrlTable = "XML_URL_TABLE";
    private static SQLiteDatabase db = null;
    private final static String TAG = "PUSHNS_db";

    public static void init(Context contextArg) {
        context = contextArg;

        // объект подключения с БД
        if (db == null) {
            db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        }

        // TODO это еще вопрос, нужно ли здесь добавлять appID. Походу если указан Context.MODE_PRIVATE
        // TODO то база создастся на приложение и этого будет достаточно. Другое приложение с таким же названием базы не должно повредить
        // создаем таблицу для уведомлений

//        if (TextUtils.isEmpty(notificationTable))
//            notificationTable = "NOTIFICATION_MESSAGES_";// + appIdArg;
        
        if (!existTable(notificationTable)) {
            createNotificationTable();
            createXmlUrlTable();
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

    /**
     * создаем таблицу для хранения xml ссылки
     */
    private static void createXmlUrlTable() {
        String query = String.format("CREATE TABLE %s ", xmlUrlTable) +
                "( " +
                " ID INTEGER, " +
                " XML TEXT, " +
                " CONSTRAINT PK_NOTIFICATION PRIMARY KEY (ID) " +
                ")";
        db.execSQL(query);
    }

    /**
     * Кладем ссылку xml на хранение в БД
     *
     * @param url - ссылка на xml для приложения
     * @return => 0 - запись прошла успешка, -1 - неудача
     */
    public static long insertXmlUrl(String url) {
        try {
            Log.e(TAG, db.toString());

            long result = db.insertWithOnConflict(
                    xmlUrlTable,
                    "",
                    fillXmlString(url),
                    SQLiteDatabase.CONFLICT_REPLACE);

            logError("insertCategoryRows = " + String.valueOf(result));
            return result;
        } catch (Exception ex) {
            logError(ex);
            return -1;
        }
    }

    /**
     * возвращает самое свежее уведомление, если есть или null если уведомлений нет
     *
     * @return самое свежее уведомление или null
     */
    public static String getXmlUrl() {
        List<AppPushNotificationMessage> result = null;

        Cursor cursor;
        cursor = db.query(xmlUrlTable, null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0)
            return null;

        String url = "";
        if (cursor.moveToFirst()) {
            do {
                url = parseXmlUrl(cursor);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return url;
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
     * Функция добавляет уведомление в базу
     *
     * @param notificationMessage - объект PUSH сообщения
     * @return => 0 если запись успешно добавлена, иначе -1
     */
    public static long insertNotification(AppPushNotificationMessage notificationMessage) {
        try {
            long result = db.insertWithOnConflict(
                    notificationTable,
                    "",
                    fillNotification(notificationMessage),
                    SQLiteDatabase.CONFLICT_REPLACE);

            logError("ROW id = " + String.valueOf(result));
            return result;
        } catch (Exception ex) {
            logError(ex);
            return -1;
        }
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

    /**
     * обновляем путь к картинке для уведомления
     *
     * @param id
     * @param imgPath
     */
    public static void updateNotificationImage(long id, String imgPath) {
        AppPushNotificationMessage notification = getNotificationById(id);
        if (notification != null) {
            notification.imagePath = imgPath;
            db.update(notificationTable, fillNotification(notification), "ID = ?", new String[]{String.valueOf(id)});
        } else {
            logError("updateNotificationImage() => notification == null");
        }

    }

    /**
     * Обновляем статут пакета у записи нотификации
     *
     * @param id     - uid записи
     * @param status true - пакет есть в приложении, false - иначе
     */
    public static void updateNotificationPackageStatus(long id, boolean status) {
        AppPushNotificationMessage notification = getNotificationById(id);
        if (notification != null) {
            notification.isPackageExist = status;
            db.update(notificationTable, fillNotification(notification), "ID = ?", new String[]{String.valueOf(id)});
        } else {
            logError("updateNotificationPackageStatus() => notification == null");
        }

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
        
        try{
            entity.widgetOrder = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("WIDGET_ORDER")));
        }catch(Exception ex){
        }

        return entity;
    }
    

    /**
     * парсин полученный данные на запрос получения xml ссылки
     *
     * @param cursor - объек данных для парсинга
     * @return
     */
    private static String parseXmlUrl(Cursor cursor) {

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).equals("XML")) {
                return cursor.getString(i);
            }
        }
        return null;
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

    /**
     * заполняем объект БД для вставки в таблицу xmlUrlTable
     *
     * @param url - данные
     * @return объект для БД
     */
    private static ContentValues fillXmlString(String url) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID", 0); // !!! запись при вставке каждый раз будет перетираться - всегда будет висеть на позиции 0 !!!!!
        contentValues.put("XML", url);
        return contentValues;
    }

    private static void logError(Exception e) {
        Log.e(TAG, "", e);
    }

    private static void logError(String message) {
        Log.e(TAG, message);
    }
}
