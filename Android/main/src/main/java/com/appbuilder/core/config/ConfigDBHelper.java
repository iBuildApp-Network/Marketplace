package com.appbuilder.core.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.appbuilder.core.AppBuilder;
import com.appbuilder.sdk.android.Widget;
import java.util.List;

/**
 *
 * @author Roman Black
 */
public class ConfigDBHelper extends SQLiteOpenHelper{
    
    public ConfigDBHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION_1);
    }
    
    private static final int VERSION_1 = 1;
    
    private static final String TAG = "DB_CONF_WIDGET";
    
    private static final String DATABASE_NAME = "config_database";
    
    private static final String TABLE_WIDGETS = "widget";
    
    private static final String TABLE_WIDGETS_COLUMN_APP_ID = "app_id";
    private static final String TABLE_WIDGETS_COLUMN_ID = "id";
    private static final String TABLE_WIDGETS_COLUMN_CONFIG_HASH = "config_hash";

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL("create table " + TABLE_WIDGETS + " ("
                    + TABLE_WIDGETS_COLUMN_ID + " integer, "
                    + TABLE_WIDGETS_COLUMN_APP_ID + " integer, "
                    + TABLE_WIDGETS_COLUMN_CONFIG_HASH + " text"
                    + ");");
        }catch(Exception ex){
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    public static boolean hasWidgetChanged(Context ctx, int widgetId, String widgetHash) {
        Log.e(TAG, "widgetId " + widgetId + " widgetHash " + widgetHash);
        
        SQLiteDatabase db = new ConfigDBHelper(ctx).getWritableDatabase();
        
        try {
            Cursor curs = db.query(TABLE_WIDGETS, null, 
                    TABLE_WIDGETS_COLUMN_ID + " = " + widgetId + " AND " +
                    TABLE_WIDGETS_COLUMN_APP_ID + " = " + AppBuilder.APP_ID,
                    null, 
                    null, null, null);
            
            Log.e(TAG, "curs prepared");
            
            if (curs.moveToFirst()) {
                Log.e(TAG, "curs.moveToFirst true");
                String oldHash = curs.getString(curs.getColumnIndex(TABLE_WIDGETS_COLUMN_CONFIG_HASH));
                
                Log.e(TAG, "curs.moveToFirst true oldHash " + oldHash);
                
                boolean changed = !widgetHash.equals(oldHash);
                
                Log.e(TAG, "curs.moveToFirst true changed " + changed);
                
                if (changed) {
                    Log.e(TAG, "curs.moveToFirst true changed true");
                    
                    ContentValues cv = new ContentValues();
                    cv.put(TABLE_WIDGETS_COLUMN_CONFIG_HASH, widgetHash);
                    
                    db.update(TABLE_WIDGETS, cv, 
                            TABLE_WIDGETS_COLUMN_ID + " = " + widgetId + " AND " + 
                            TABLE_WIDGETS_COLUMN_APP_ID + " = " + AppBuilder.APP_ID, 
                            null);
                    
                    Log.e(TAG, "curs.moveToFirst true changed true inserted");
                }
                
                curs.close();
                db.close();
                
                return changed;
            } else {
                Log.e(TAG, "curs.moveToFirst false");
                ContentValues cv = new ContentValues();
                cv.put(TABLE_WIDGETS_COLUMN_ID, widgetId);
                cv.put(TABLE_WIDGETS_COLUMN_CONFIG_HASH, widgetHash);
                cv.put(TABLE_WIDGETS_COLUMN_APP_ID, AppBuilder.APP_ID);
                
                db.insertOrThrow(TABLE_WIDGETS, null, cv);
                
                curs.close();
                db.close();
                
                Log.e(TAG, "curs.moveToFirst false inserted");
                
                return true;
            }
        } catch (Exception ex) {
            
        }
        
        db.close();
        
        return false;
    }
    
    public static void removeOldWidgets(Context ctx, List<Widget> actualWidgets){
        SQLiteDatabase db = new ConfigDBHelper(ctx).getWritableDatabase();
        
        try{
            StringBuilder sb = new StringBuilder();
            sb.append(TABLE_WIDGETS_COLUMN_ID);
            sb.append(" NOT IN (");
            for(int i = 0; i < actualWidgets.size(); i++){
                sb.append("'");
                sb.append(actualWidgets.get(i).getWidgetId());
                sb.append("'");
                if(i != actualWidgets.size() - 1) sb.append(",");
            }
            sb.append(") AND ");
            sb.append(TABLE_WIDGETS_COLUMN_APP_ID);
            sb.append(" = ");
            sb.append(AppBuilder.APP_ID);
            
            db.delete(TABLE_WIDGETS, sb.toString(), null);
        }catch(Exception ex){
            Log.e("", "");
        }
        
        db.close();
    }
    
}
