package com.appbuilder.core.PushNotification.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * @author Roman Black
 */
public class PushDatabaseHelper extends SQLiteOpenHelper{
    
    public PushDatabaseHelper(Context context){
        super(context, null, null, VERSION_1);
    }
    
    private static final int VERSION_1 = 1;
    
    private static final String DATABASE_NAME = "push_database";
    
    private static final String TABLE_PUSH_MESSAGES = "push_messages";
    
    private static final String PUSH_MESSAGES_COLUMN_ID = "id";
    private static final String PUSH_MESSAGES_COLUMN_DESCRIPTION = "descr";
    private static final String PUSH_MESSAGES_COLUMN_IMAGE = "image";
    private static final String PUSH_MESSAGES_COLUMN_MESSAGE = "message";
    private static final String PUSH_MESSAGES_COLUMN_ORDER = "order_";
    private static final String PUSH_MESSAGES_COLUMN_PACKAGE = "package_";
    private static final String PUSH_MESSAGES_COLUMN_TYPE = "type";
    private static final String PUSH_MESSAGES_COLUMN_TITLE = "title";

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL("create table " + TABLE_PUSH_MESSAGES + " ("
                    + "id integer,"
                    + "descr text,"
                    + "image text,"
                    + "message text,"
                    + "order_ integer,"
                    + "package_ text,"
                    + "type integer,"
                    + "title text"
                    + ");");
        }catch(Exception ex){
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    public static synchronized boolean saveNewMessage(Context context, PushNotificationService.HandlerObject message){
        boolean result;
        
        SQLiteDatabase db = new PushDatabaseHelper(context).getWritableDatabase();
        
        try{
            Cursor curs = db.query(TABLE_PUSH_MESSAGES, new String[]{PUSH_MESSAGES_COLUMN_ID}, PUSH_MESSAGES_COLUMN_ID + " = ?", 
                    new String[]{message.id}, null, null, null);
            
            if(curs.moveToFirst()){
                result = false;
            }else{
                ContentValues cv = new ContentValues();
                cv.put(PUSH_MESSAGES_COLUMN_ID, message.id);
                cv.put(PUSH_MESSAGES_COLUMN_DESCRIPTION, message.descr);
                cv.put(PUSH_MESSAGES_COLUMN_IMAGE, message.image);
                cv.put(PUSH_MESSAGES_COLUMN_MESSAGE, message.message);
                cv.put(PUSH_MESSAGES_COLUMN_ORDER, message.order);
                cv.put(PUSH_MESSAGES_COLUMN_PACKAGE, message.package_);
                cv.put(PUSH_MESSAGES_COLUMN_TITLE, message.title);
                cv.put(PUSH_MESSAGES_COLUMN_TYPE, message.type);

                db.insertOrThrow(TABLE_PUSH_MESSAGES, null, cv);

                result = true;
            }
        }catch(Exception ex){
            result = false;
        }
        
        db.close();
        
        return result;
    }
    
}
