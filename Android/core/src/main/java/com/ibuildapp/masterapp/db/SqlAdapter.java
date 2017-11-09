package com.ibuildapp.masterapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.ibuildapp.masterapp.model.ApplicationEntity;
import com.ibuildapp.masterapp.model.CategoryEntity;
import com.ibuildapp.masterapp.utils.Statics;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 15.07.14
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
public class SqlAdapter {

    private static String TAG = SqlAdapter.class.getCanonicalName();
    private static Context context;


    private static SQLiteDatabase db = null;

    private final static String CATEGORY = "CATEGORY";
    private final static String APPLICATION = "APPLICATION";
    private final static String FAVOURITES = "FAVOURITES";

    private static String tableNames[] = {"CATEGORY", "APPLICATION", "FAVOURITES"};

    private static Map<String, String> categoryColumns = new HashMap<String, String>();
    private static Map<String, String> applicationColumns = new HashMap<String, String>();
    private static Map<String, String> favouriteColumns = new HashMap<String, String>();


    public static void init( Context context )
    {
        SqlAdapter.context = context;

        prepareTableColumns();

        if ( db == null ) {
            try {
                db = context.openOrCreateDatabase(Statics.databasePath, Context.MODE_PRIVATE, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        Cursor cursor = db.query(CATEGORY, null, null,null,null,null,"ORDER_ID",null);
//        ArrayList<CategoryEntity> result = new ArrayList<CategoryEntity>(cursor.getCount());
//        if (cursor.moveToFirst()) {
//            do {
//                result.add(parseCategory(cursor));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();


        isExistOrCreate();
    }

    private static void prepareTableColumns()
    {
        prepareTableColumnsCategory();
        prepareTableColumnsApplication();
        prepareTableColumnsFavourites();
    }

    private static void prepareTableColumnsApplication() {
        applicationColumns.put("APPID", "INTEGER PRIMARY KEY" );
        applicationColumns.put("CATEGORY_ID", "INTEGER" );
        applicationColumns.put("TOKEN", "TEXT" );
        applicationColumns.put("TITLE", "TEXT" );
        applicationColumns.put("DESCRIPTION", "TEXT" );
        applicationColumns.put("PICTURE_URL", "TEXT" );
        applicationColumns.put("PICTURE_PATH", "TEXT" );
        applicationColumns.put("BACKGROUND", "TEXT" );
    }

    private static void prepareTableColumnsCategory() {
        categoryColumns.put("ID", "INTEGER PRIMARY KEY" );
        categoryColumns.put("TITLE", "TEXT");
        categoryColumns.put("PICTURE_URL", "TEXT");
        categoryColumns.put("PICTURE_PATH", "TEXT");
        categoryColumns.put("ORDER_ID", "INTEGER");
        categoryColumns.put("ENABLE", "TEXT");
        categoryColumns.put("SORTED_APPS_LIST", "TEXT");
    }

    private static void prepareTableColumnsFavourites() {
        favouriteColumns.put("APPID", "INTEGER PRIMARY KEY" );
        favouriteColumns.put("CATEGORY_ID", "INTEGER" );
        favouriteColumns.put("TOKEN", "TEXT" );
        favouriteColumns.put("TITLE", "TEXT" );
        favouriteColumns.put("TITLE_LOWER", "TEXT" );
        favouriteColumns.put("DESCRIPTION", "TEXT" );
        favouriteColumns.put("DESCRIPTION_LOWER", "TEXT" );
        favouriteColumns.put("PICTURE_URL", "TEXT" );
        favouriteColumns.put("PICTURE_PATH", "TEXT" );
        favouriteColumns.put("BACKGROUND", "TEXT" );
        favouriteColumns.put("FAVOURITED", "INTEGER" );
        favouriteColumns.put("ACTIVE", "INTEGER");
//        favouriteColumns.put("TIMESTAMP", "TEXT");
    }

    private static void createTableCategory() {
        StringBuilder columns = new StringBuilder();

        // create columns
        int counter = 0;
        for (String key : categoryColumns.keySet()) {
            columns.append(String.format(" %s %s", key, categoryColumns.get(key)));
            if ( counter != categoryColumns.size()-1 )
                columns.append(",");
            ++counter;
        }

        // result query string
        String query = String.format( "CREATE TABLE " + CATEGORY + " ( %s )", columns );
        db.execSQL(query);
    }

    private static void createTableApplication() {
        StringBuilder columns = new StringBuilder();

        // create columns
        int counter = 0;
        for ( String key : applicationColumns.keySet() ) {
            columns.append(String.format(" %s %s", key, applicationColumns.get(key)));
            if ( counter != applicationColumns.size()-1 )
                columns.append(",");
            ++counter;
        }

        // result query string
        String query = String.format( "CREATE TABLE " + APPLICATION + " ( %s )", columns );
        db.execSQL(query);
    }

    private static void createTableFavourites() {
        StringBuilder columns = new StringBuilder();

        // create columns
        int counter = 0;
        for ( String key : favouriteColumns.keySet() ) {
            columns.append(String.format(" %s %s", key, favouriteColumns.get(key)));

            if ( counter != favouriteColumns.size()-1 )
                columns.append(",");
            ++counter;
        }

        // result query string
        String query = String.format("CREATE TABLE " + FAVOURITES + " ( %s )", columns);
        db.execSQL(query);
    }

    private static boolean existTable(String tableName) {
//        String script = "SELECT * FROM " + tableName;
//
//        try {
//            Cursor cursor = db.rawQuery(script, new String[]{});
//            if (cursor != null) {
//                cursor.close();
//                return  true;
//            }
//        } catch (Exception e) {
//            return false;
//        }


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
     * Проверка на существование БД и таблиц в ней
     *
     * @return заключение о работоспособности БД
     */
    private static boolean isExistOrCreate() {
        boolean result = true;

        try {
            if (db == null) {
                if (context == null) {
                    Log.e(TAG, "context == NULL");
                } else if (Statics.DB_NAME == null) {
                    Log.e(TAG, "databaseName == NULL");
                }

                db = context.openOrCreateDatabase(Statics.DB_NAME, Context.MODE_PRIVATE, null);
            }

            if (!existsTable()) {
                createTables();
                result = existsTable();
            } else
            {
                alterTables();
            }
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    private static void alterTables() {
        alterTableCategory();
        alterTableApplication();
        alterTableFavourites();
    }

    private static void alterTableCategory() {
        String alterTableScript = "ALTER TABLE %s ADD COLUMN %s %s";

        db.beginTransaction();
        for (String key : categoryColumns.keySet()) {
            String query = String.format(alterTableScript, CATEGORY, key, categoryColumns.get(key));
            try {
                db.execSQL(query);
            } catch (Exception e) {

            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private static void alterTableApplication() {
        String alterTableScript = "ALTER TABLE %s ADD COLUMN %s %s";

        db.beginTransaction();
        for (String key : applicationColumns.keySet()) {
            String query = String.format( alterTableScript, APPLICATION, key, applicationColumns.get(key) );
            try {
                db.execSQL(query);
            } catch (Exception e) {
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private static void alterTableFavourites() {
        String alterTableScript = "ALTER TABLE %s ADD COLUMN %s %s";

        db.beginTransaction();
        for (String key : favouriteColumns.keySet()) {
            String query = String.format(alterTableScript, FAVOURITES, key, favouriteColumns.get(key));
            try {
                db.execSQL(query);
            } catch (Exception e) {

            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private static boolean existsTable() {
        boolean result = true;

        try {
            for (String tableName : tableNames) {
                result &= existTable(tableName);
            }
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    private static void createTables() {
        dropTables();
        createTableCategory();
        createTableApplication();
        createTableFavourites();
    }

    private static void dropTables() {
        if (db == null) {
            db = context.openOrCreateDatabase(Statics.DB_NAME, Context.MODE_PRIVATE, null);
        }

        try {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", CATEGORY));
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", APPLICATION));
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", FAVOURITES));
        } catch (Exception e) {
        }
    }


    // *****************************************************************************************************************
    // ************************** TABLE CATEGORY **************************
    // *****************************************************************************************************************
    public static void insertCategory( List<CategoryEntity> list) {
        for (CategoryEntity entity : list) {
            try {
                long result = db.insertWithOnConflict(CATEGORY, null, fillCategory( entity ), SQLiteDatabase.CONFLICT_REPLACE);
                entity.id = (int) result;
            } catch (Exception ex) {
                Log.e("","");
            }
        }
    }

    public static List<CategoryEntity> selectAllCategory(){
        List<CategoryEntity> result = null;

        try {
            if (!isExistOrCreate()) {
                return new ArrayList<CategoryEntity>();
            }

            Cursor cursor = db.query(CATEGORY, null, null,null,null,null,"ORDER_ID",null);

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<CategoryEntity>();
            }

            result = new ArrayList<CategoryEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseCategory(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            result = new ArrayList<CategoryEntity>();
        }
        return result;
    }

    public static CategoryEntity selectCategoryByID(int categoryId) throws IllegalArgumentException {

        if (categoryId < 0) {
            throw new IllegalArgumentException("Parent id must be great or equal 0");
        }

        CategoryEntity res = null;

        try {

            Cursor cursor = db.query(CATEGORY, null, "ID = ?",
                    new String[]{String.valueOf(categoryId)},
                    null,
                    null,
                    null,
                    null);

            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            ArrayList<CategoryEntity> result = new ArrayList<CategoryEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    res = parseCategory(cursor);
                    break;
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            return null;
        }
        return res;
    }

    public static List<String> getSortedAppsForCategoryByID(int categoryId) throws IllegalArgumentException {

        if (categoryId < 0) {
            throw new IllegalArgumentException("Parent id must be great or equal 0");
        }

        List<String> res = new ArrayList<String>();

        try {

            Cursor cursor = db.query(CATEGORY, null, "ID = ?",
                    new String[]{String.valueOf(categoryId)},
                    null,
                    null,
                    null,
                    null);

            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            ArrayList<CategoryEntity> result = new ArrayList<CategoryEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    CategoryEntity temp = parseCategory(cursor);
                    if ( temp.sorted_apps_list != null && temp.sorted_apps_list.size() >0 )
                    {
                        res.addAll(temp.sorted_apps_list);
                    }
                    break;
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            return res;
        }
        return res;
    }


    private static ContentValues fillCategory( CategoryEntity entity )
    {
        ContentValues res = new ContentValues();
        res.put("ID", entity.id);
        res.put("TITLE", entity.title);
        res.put("PICTURE_URL", entity.pictureUrl);
        res.put("PICTURE_PATH", entity.picturePath);
        res.put("ORDER_ID", entity.order);
        res.put("ENABLE", entity.enable);

        return res;
    }

    private static CategoryEntity parseCategory(Cursor cursor) {
        CategoryEntity entity = new CategoryEntity();

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).compareToIgnoreCase("ID") == 0) {
                entity.id = cursor.getInt(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("PICTURE_URL")== 0) {
                entity.pictureUrl = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("PICTURE_PATH")== 0) {
                entity.picturePath = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("ORDER_ID")== 0) {
                entity.order = cursor.getInt(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("ENABLE")== 0) {
                entity.enable = cursor.getInt(i) == 0 ? false : true;
            } else if (cursor.getColumnName(i).compareToIgnoreCase("TITLE")== 0) {
                entity.title = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("SORTED_APPS_LIST")== 0) {
                String resString = cursor.getString(i);
                if (!TextUtils.isEmpty(resString))
                {
                    String ar[] = resString.replace("[","").replace("]","").split(",");
                    for ( int j = 0; j < ar.length; j++ )
                    {
                        ar[j] = ar[j].trim();
                    }
                    entity.sorted_apps_list = Arrays.asList(ar);
                }
            }
        }

        return entity;
    }

    public static void clearTableCategory()
    {
        try{
            db.execSQL(String.format("DELETE FROM %s", CATEGORY));
        } catch (Exception e)
        {
            Log.e(TAG, "clearTableCategory()");
        }
    }

    public static CategoryEntity selectFirstCategory(  )
    {
        Cursor cursor = db.query(CATEGORY, null, null,null,null,null,null,"1");

        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        CategoryEntity res = null;
        if (cursor.moveToFirst()) {
            do {
                res = parseCategory(cursor);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return res;
    }

    public static void updateCategorySortedAps( int categoryId, List<String> sortedList )
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("SORTED_APPS_LIST", sortedList.toString());
        db.update(CATEGORY, contentValues, "ID = ?", new String[]{String.valueOf(categoryId)});
    }

    // *****************************************************************************************************************
    // ************************** TABLE APPLICATION **************************
    // *****************************************************************************************************************
    public static void insertApplication( List<ApplicationEntity> list ) {
        for (ApplicationEntity entity : list) {
            try {
                long result = db.insertWithOnConflict(APPLICATION, null, fillApplication(entity), SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception ex) {

            }
        }
    }

    public static void updateApplicationImagePath( int appId, String imagePath )
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("PICTURE_PATH", imagePath);
        db.update(APPLICATION, contentValues, "APPID = ?", new String[]{String.valueOf(appId)});
        Log.e("","");
    }

    public static void clearTableApplication()
    {
        try{
            db.execSQL(String.format("DELETE FROM %s", APPLICATION));
        } catch (Exception e)
        {
            Log.e(TAG, "clearTableApplication()");
        }
    }

    private static ContentValues fillApplication( ApplicationEntity entity )
    {
        ContentValues res = new ContentValues();
        res.put("APPID", entity.appid);
        res.put("CATEGORY_ID", entity.categoryid);
        res.put("TOKEN", entity.token);
        res.put("TITLE", entity.title);
        res.put("DESCRIPTION", entity.description);
        res.put("PICTURE_URL", entity.pictureUrl);
        res.put("PICTURE_PATH", entity.picturePath);

        return res;
    }

    private static ApplicationEntity parseApplication(Cursor cursor) {
        ApplicationEntity entity = new ApplicationEntity();

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).compareToIgnoreCase("APPID")== 0) {
                entity.appid = cursor.getInt(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("CATEGORY_ID")== 0) {
                entity.categoryid = cursor.getInt(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("TOKEN")== 0) {
                entity.token = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("TITLE")== 0) {
                entity.title = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("DESCRIPTION")== 0) {
                entity.description = cursor.getString(i);
            }else if (cursor.getColumnName(i).compareToIgnoreCase("PICTURE_URL")== 0) {
                entity.pictureUrl = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("PICTURE_PATH")== 0) {
                entity.picturePath = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("BACKGROUND")== 0) {
                entity.background = cursor.getString(i);
            }
        }

        return entity;
    }

    public static List<ApplicationEntity> selectApplicationsByIdArray( List<String> appIdList )
    {
        List<ApplicationEntity> result = null;
        try {
            String args = appIdList.toString().replace("[","").replace("]","");
            String queryString = String.format("select * from APPLICATION where APPID IN (%s)", args);

            Cursor cursor =  db.rawQuery(queryString, null);

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<ApplicationEntity>();
            }

            result = new ArrayList<ApplicationEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseApplication(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            result = new ArrayList<ApplicationEntity>();
        }
        return result;
    }

    public static ApplicationEntity selectApplicationById( int appId )
    {
        ApplicationEntity result = null;
        try {
            Cursor cursor = db.query(APPLICATION, null, "APPID = ?",
                    new String[]{String.valueOf(appId)},
                    null,
                    null,
                    null,
                    null);

            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            if (cursor.moveToFirst()) {
                do {
                    result = parseApplication(cursor);
                    break;
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public static List<ApplicationEntity> selectAllApplication(  )
    {
        List<ApplicationEntity> result = null;
        try {

            Cursor cursor = db.query(APPLICATION, null, null,null,null,null,null,null);

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<ApplicationEntity>();
            }

            result = new ArrayList<ApplicationEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseApplication(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            result = new ArrayList<ApplicationEntity>();
        }
        return result;
    }

    // *****************************************************************************************************************
    // ************************** TABLE FAVOURITES **************************
    // *****************************************************************************************************************
    private static ContentValues fillFavourite( ApplicationEntity entity )
    {
        ContentValues res = new ContentValues();
        res.put("APPID", entity.appid);
        res.put("CATEGORY_ID", entity.categoryid);
        res.put("TOKEN", entity.token);
        res.put("TITLE", entity.title);
        res.put("TITLE_LOWER", entity.title.toLowerCase());
        res.put("DESCRIPTION", entity.description);
        res.put("DESCRIPTION_LOWER", entity.description.toLowerCase());
        res.put("PICTURE_URL", entity.pictureUrl);
        res.put("PICTURE_PATH", entity.picturePath);
        res.put("BACKGROUND", entity.background);
//        res.put("TIMESTAMP", entity.timestamp);
        if ( entity.favourited )
            res.put("FAVOURITED", 1);
        else
            res.put("FAVOURITED", 0);

        if ( entity.active )
            res.put("ACTIVE", 1);
        else
            res.put("ACTIVE", 0);

        return res;
    }

    private static ApplicationEntity parseFavourite(Cursor cursor) {
        ApplicationEntity entity = new ApplicationEntity();

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).compareToIgnoreCase("APPID")== 0) {
                entity.appid = cursor.getInt(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("CATEGORY_ID")== 0) {
                entity.categoryid = cursor.getInt(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("TOKEN")== 0) {
                entity.token = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("TITLE")== 0) {
                entity.title = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("DESCRIPTION")== 0) {
                entity.description = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("PICTURE_URL")== 0) {
                entity.pictureUrl = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("PICTURE_PATH")== 0) {
                entity.picturePath = cursor.getString(i);
            } else if (cursor.getColumnName(i).compareToIgnoreCase("FAVOURITED")== 0) {
                if (cursor.getInt(i) == 0)
                    entity.favourited = false;
                else
                    entity.favourited = true;
            } else if (cursor.getColumnName(i).compareToIgnoreCase("ACTIVE")== 0) {
                if (cursor.getInt(i) == 0)
                    entity.active = false;
                else
                    entity.active = true;
            } else if (cursor.getColumnName(i).compareToIgnoreCase("BACKGROUND")== 0) {
                entity.background = cursor.getString(i);
            }
//             else if(cursor.getColumnName(i).compareToIgnoreCase("TIMESTAMP")== 0) {
//                entity.timestamp = Long.valueOf(cursor.getString(i));
//            }
        }

        return entity;
    }

    public static void insertFavourites( ApplicationEntity app ) {
        try {
            long result = db.insertWithOnConflict(FAVOURITES, null, fillFavourite(app), SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception ex) {
        }
    }

    public static ApplicationEntity selectFavouriteAppById( int appId )
    {
        ApplicationEntity result = null;
        try {

            Cursor cursor = db.query(FAVOURITES, null, "APPID = ?",
                    new String[]{String.valueOf(appId)},
                    null,null,null,null);

            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            if (cursor.moveToFirst()) {
                do {
                    result = parseFavourite(cursor);
                    break;
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public static List<ApplicationEntity> selectFavouriteLike( String likeString )
    {
        List<ApplicationEntity> result = null;

        try {
            String query = String.format("SELECT * FROM %s WHERE (TITLE_LOWER LIKE \'%%%s%%\' OR DESCRIPTION_LOWER LIKE '%%%s%%') AND ACTIVE=1", FAVOURITES, likeString.toLowerCase(),likeString.toLowerCase());
            Cursor cursor = db.rawQuery(query,new String[]{});

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<ApplicationEntity>();
            }

            result = new ArrayList<ApplicationEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseFavourite(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            return new ArrayList<ApplicationEntity>();
        }
        return result;
    }

    public static List<ApplicationEntity> selectAllFavourites(  )
    {
        List<ApplicationEntity> result = null;
        try {

            Cursor cursor = db.query(FAVOURITES, null, "ACTIVE = ?",new String[]{String.valueOf("1")},
                    null,null,null,null);

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<ApplicationEntity>();
            }

            result = new ArrayList<ApplicationEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseFavourite(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            result = new ArrayList<ApplicationEntity>();
        }
        return result;
    }

    public static List<ApplicationEntity> selectFavouritesByActiveByFavourited( Boolean active, Boolean favourites  )
    {
        List<ApplicationEntity> result = null;
        try {

            String queryStr = null;
            if ( favourites == null )
            {
                queryStr = String.format("SELECT * FROM %s WHERE ACTIVE=%d",
                        FAVOURITES,
                        (active)?1:0);
            } else
            {
                queryStr = String.format("SELECT * FROM %s WHERE ACTIVE=%d AND FAVOURITED=%d",
                        FAVOURITES,
                        (active)?1:0,
                        (favourites)?1:0);
            }

            Cursor cursor = db.rawQuery(queryStr, null);

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<ApplicationEntity>();
            }

            result = new ArrayList<ApplicationEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseFavourite(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            result = new ArrayList<ApplicationEntity>();
        }
        return result;
    }

    public static void deleteFavourite( int appid ) {
        try {
            int rc = db.delete(FAVOURITES, "APPID = ?", new String[]{String.valueOf(appid)});
        } catch (Exception ex) {
        }
    }

    public static void updateFavourite( int appid, boolean active, boolean favourited ) {
        try {
            ContentValues contentValues = new ContentValues();
            if ( favourited )
                contentValues.put("FAVOURITED", 1);
            else
                contentValues.put("FAVOURITED", 0);

            if (active)
                contentValues.put("ACTIVE", 1);
            else
                contentValues.put("ACTIVE", 0);

            db.update(FAVOURITES, contentValues, "APPID = ?", new String[]{String.valueOf(appid)});
        } catch (Exception ex) {
        }
    }
}
