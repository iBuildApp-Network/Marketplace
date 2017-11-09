package com.ibuildapp.masterapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.flurry.android.FlurryAgent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 26.11.14
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */
public class FlurryLogger {
    // const
    private static final String TAG = FlurryLogger.class.getCanonicalName();
    public static final String APP_STARTED = "MarketPlace App Started";
    public static final String FAVOURITE_PRESSED = "MarketPlace Favourite pressed";
    public static final String CATEGORY_OPENED = "MarketPlace category";
    public static final String APP_OPENED_COUNT = "MarketPlace opened app";
    public static final String FAVOURITE_ADDED = "MarketPlace аavourite added";
    public static final String APP_SESSION_TIME = "MarketPlace app session time";
    public static final String CATEGORY_SESSION_TIME = "MarketPlace category session time";

    public static final String SHARING_TWITTER_ATTEMPT = "MarketPlace sharing twitter attempt";
    public static final String SHARING_TWITTER_RESULT = "MarketPlace sharing twitter result";
    public static final String SHARING_FB_ATTEMPT = "MarketPlace sharing FB attempt";
    public static final String SHARING_FB_RESULT = "MarketPlace sharing FB result";
    public static final String SHARING_EMAIL_ATTEMPT = "MarketPlace sharing email attempt";
    public static final String SHARING_EMAIL_RESULT = "MarketPlace sharing email result";
    public static final String SHARING_SMS_ATTEMPT = "MarketPlace sharing sms attempt";
    public static final String SHARING_SMS_RESULT = "MarketPlace sharing sms result";

    public static final String SESSION_TIME = "session_time";
    public static final String LESS_THEN_MINUTE = "<1";
    public static final String ONE_FIVE_MINUTES = "1-5";
    public static final String FIVE_FIFTEEN_MINUTE = "5-15";
    public static final String FIFTEEN_MINUTE = "15>";


    public static final String KEY_COUNTER = "counter_key";
    public static final String FLURRY_PREFS = "fluffy_prefs";

    // backend
    private static boolean appLaunchBlock = false;
    private static boolean favouriteBlock = false;
    private static boolean sendCounter = false;
    // TODO подменить ключ
    private static String API_KEY = "3ZCDQ7SW7DQP6T5N2NDP";    // ibuildapp//"3ZCDQ7SW7DQP6T5N2NDP"; // myfor test "QJ6J383WY4JTQT4H5Z78"
    private static Context currentContext;

    public static void onStartInit( Context context )
    {
        currentContext = context;
//        FlurryAgent.init(context, API_KEY);
        FlurryAgent.init(context, API_KEY);
        FlurryAgent.onStartSession(context, API_KEY);
        Log.e(TAG, "onStartInit()");

        if ( !sendCounter )
        {
            // отправляем колличество открытых приложений за прошлую сессию
            SharedPreferences sPref = currentContext.getSharedPreferences(FLURRY_PREFS, 0);
            int counter = sPref.getInt(KEY_COUNTER, 0);

            Map<String, String> map = new HashMap<String, String>();
            map.put("result_count", Integer.toString(counter));
            FlurryAgent.logEvent(APP_OPENED_COUNT, map);
            Log.e(TAG, "onStartInit - send appOpenCounter = " + Integer.toString(counter));

            // зануляем переменную
            SharedPreferences.Editor ed = sPref.edit();
            ed.putInt(KEY_COUNTER, 0);
            ed.commit();

            sendCounter = true;
        }
    }

    public static void onStopInit()
    {
        // закрываем сессию
        FlurryAgent.onEndSession(currentContext);
        Log.e(TAG, "onStopInit()");
    }

    /**
     * how many times app was launched
     */
    public static void appLaunchEvent()
    {
        if ( !appLaunchBlock )
        {
            appLaunchBlock = true;
            FlurryAgent.logEvent(APP_STARTED);
            Log.e(TAG, "appLaunchEvent()");
        }
    }

    /**
     * - how many times opened each category
     * @param categoryName - название категори
     */
    public static void categoryOpened( String categoryName )
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("category", categoryName);
        FlurryAgent.logEvent(CATEGORY_OPENED, map);
        Log.e(TAG, "categoryOpened() name = " + categoryName);
    }

    /**
     * - how long user have been in app
     * @param howLong - скоклько времени пользователь был в категории
     */
    public static void appSessionTime( Date howLong )
    {
        Map<String, String> map = new HashMap<String, String>();
        String howLongLog="";
        if ( howLong.getTime() < 60*1000 )
        {
            map.put(SESSION_TIME, LESS_THEN_MINUTE);
            howLongLog = LESS_THEN_MINUTE;
        } else if ( howLong.getTime() > 60*1000 && howLong.getTime() < 5*60*1000)
        {
            map.put(SESSION_TIME, ONE_FIVE_MINUTES);
            howLongLog = ONE_FIVE_MINUTES;
        } else if ( howLong.getTime() > 5*60*1000 && howLong.getTime() < 15*60*1000)
        {
            map.put(SESSION_TIME, FIVE_FIFTEEN_MINUTE);
            howLongLog = FIVE_FIFTEEN_MINUTE;
        } else if ( howLong.getTime() > 15*60*1000 )
        {
            map.put(SESSION_TIME, FIFTEEN_MINUTE);
            howLongLog = FIFTEEN_MINUTE;
        }

        FlurryAgent.logEvent(APP_SESSION_TIME, map);
        Log.e(TAG, "appSessionTime() howLong = " + howLongLog);
    }

    /**
     * - how long user have been in category
     * @param howLong - скоклько времени пользователь был в категории
     */
    public static void categorySessionTime( String category, Date howLong, int count )
    {
        Map<String, String> map = new HashMap<String, String>();
        String howLongLog="";
        if ( howLong.getTime() < 60*1000 )
        {
            map.put(SESSION_TIME, LESS_THEN_MINUTE);
            howLongLog = LESS_THEN_MINUTE;
        } else if ( howLong.getTime() > 60*1000 && howLong.getTime() < 5*60*1000)
        {
            map.put(SESSION_TIME, ONE_FIVE_MINUTES);
            howLongLog = ONE_FIVE_MINUTES;
        } else if ( howLong.getTime() > 5*60*1000 && howLong.getTime() < 15*60*1000)
        {
            map.put(SESSION_TIME, FIVE_FIFTEEN_MINUTE);
            howLongLog = FIVE_FIFTEEN_MINUTE;
        } else if ( howLong.getTime() > 15*60*1000 )
        {
            map.put(SESSION_TIME, FIFTEEN_MINUTE);
            howLongLog = FIFTEEN_MINUTE;
        }
        map.put("result_count", Integer.toString(count));
        map.put("category", category);

        FlurryAgent.logEvent(CATEGORY_SESSION_TIME, map);
        Log.e(TAG, "categorySessionTime() Category =" + category +" howLong = " + howLongLog + " count = " + count);
    }

    public static void increaseAppOpenCount()
    {
        SharedPreferences sPref = currentContext.getSharedPreferences(FLURRY_PREFS, 0);
        int counter = sPref.getInt(KEY_COUNTER, 0);

        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(KEY_COUNTER, ++counter);
        ed.commit();

        sPref = currentContext.getSharedPreferences(FLURRY_PREFS, 0);
        counter = sPref.getInt(KEY_COUNTER, 0);

        Log.e(TAG, "increaseAppOpenCount() increaseCounter = " + counter);
    }

    /**
     *  how many users used favourites
     */
    public static void favouritesTrigger()
    {
        if ( !favouriteBlock )
        {
            favouriteBlock = true;
            FlurryAgent.logEvent(FAVOURITE_PRESSED);
            Log.e(TAG, "favouritesTrigger()");
        }
    }

    /**
     *  how many users used favourites
     */
    public static void favouritesAdded()
    {
        FlurryAgent.logEvent(FAVOURITE_ADDED);
        Log.e(TAG, "favouritesAdded()");
    }

    /**
     * sharing twitter attempt
     */
    public static void sharingTwitterAttempt()
    {
        FlurryAgent.logEvent(SHARING_TWITTER_ATTEMPT);
        Log.e(TAG, "sharingTwitterAttempt()");
    }

    /**
     * sharing twitter attempt
     */
    public static void sharingTwitterResult( int result_ok )
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("result", result_ok == 0 ? "success" : "cancel");
        FlurryAgent.logEvent(SHARING_TWITTER_RESULT, map);
        Log.e(TAG, "sharingTwitterResult() result = " + (result_ok == 0 ? "success" : "cancel"));
    }

    /**
     * sharing facebook attempt
     */
    public static void sharingFacebookAttempt()
    {
        FlurryAgent.logEvent(SHARING_FB_ATTEMPT);
        Log.e(TAG, "sharingFacebookAttempt()");
    }

    /**
     * sharing facebook attempt
     */
    public static void sharingFacebookResult( int result_ok )
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("result_fb", result_ok == 0 ? "success" : "cancel");
        FlurryAgent.logEvent(SHARING_FB_RESULT, map);
        Log.e(TAG, "sharingFacebookResult() result = " + (result_ok == 0 ? "success" : "cancel"));
    }

    /**
     * sharing email attempt
     */
    public static void sharingEmailAttempt()
    {
        FlurryAgent.logEvent(SHARING_EMAIL_ATTEMPT);
        Log.e(TAG, "sharingEmailAttempt()");
    }

    /**
     * sharing email attempt
     */
    public static void sharingEmalResult( int result_ok )
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("result_email", result_ok == 0 ? "success" : "cancel");
        FlurryAgent.logEvent(SHARING_EMAIL_RESULT, map);
        Log.e(TAG, "sharingEmailResult() result = " + (result_ok == 0 ? "success" : "cancel"));
    }

    /**
     * sharing email attempt
     */
    public static void sharingSmsAttempt()
    {
        FlurryAgent.logEvent(SHARING_SMS_ATTEMPT);
        Log.e(TAG, "sharingSmsAttempt()");
    }

    /**
     * sharing email attempt
     */
    public static void sharingSmsResult( int result_ok )
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("result_sms", result_ok == 0 ? "success" : "cancel");
        FlurryAgent.logEvent(SHARING_SMS_RESULT, map);
        Log.e(TAG, "sharingSmsResult() result = " + (result_ok == 0 ? "success" : "cancel"));
    }

    public static void clearState()
    {
        appLaunchBlock = false;
        favouriteBlock = false;
        sendCounter = false;
    }
}
