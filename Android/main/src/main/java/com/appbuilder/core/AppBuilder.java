package com.appbuilder.core;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appbuilder.core.GPSNotification.GPSService;
import com.appbuilder.core.LoginScreen.LoginWithEmailActivity;
import com.appbuilder.core.LoginScreen.service.LoginScreenService;
import com.appbuilder.core.LoginScreen.service.LoginSettings;
import com.appbuilder.core.LoginScreen.service.LoginSettingsService;
import com.appbuilder.core.LoginScreen.service.OnDone;
import com.appbuilder.core.PushNotification.AppPushNotification;
import com.appbuilder.core.PushNotification.AppPushNotificationDB;
import com.appbuilder.core.PushNotification.AppPushNotificationDialogLayout;
import com.appbuilder.core.PushNotification.AppPushNotificationMessage;
import com.appbuilder.core.PushNotification.services.PushNotificationService;
import com.appbuilder.core.config.ConfigDBHelper;
import com.appbuilder.core.plugin.PluginLoader;
import com.appbuilder.core.tools.Prefs;
import com.appbuilder.core.tools.Tools;
import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.core.xmlconfiguration.AppConfigureItem;
import com.appbuilder.core.xmlconfiguration.AppConfigureParser;
import com.appbuilder.core.xmlconfiguration.DownloadStatus;
import com.appbuilder.core.xmlconfiguration.WidgetUIButton;
import com.appbuilder.core.xmlconfiguration.WidgetUIImage;
import com.appbuilder.core.xmlconfiguration.WidgetUILabel;
import com.appbuilder.core.xmlconfiguration.WidgetUISidebarItem;
import com.appbuilder.core.xmlconfiguration.WidgetUITab;
import com.appbuilder.sdk.android.Base64;
import com.appbuilder.sdk.android.GoogleAnaliticsHandler;
import com.appbuilder.sdk.android.MyContact;
import com.appbuilder.sdk.android.OnSwipeInterface;
import com.appbuilder.sdk.android.SideBarComponent;
import com.appbuilder.sdk.android.SidebarAdapter;
import com.appbuilder.sdk.android.SidebarSharing;
import com.appbuilder.sdk.android.SwipeLinearLayout;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.flurry.android.FlurryAgent;
import com.google.android.c2dm.C2DMessaging;
import com.google.gson.Gson;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import retrofit.RetrofitError;

//import com.appbuilder.core.ErrorLogger.DeviceConfig;
//import com.google.android.gcm.GCMRegistrar;

public class AppBuilder extends Activity implements PluginLoader, DownloadHelperCallback, AdapterView.OnItemClickListener, SidebarSharing {

    private static final int AUTHORIZATION_FACEBOOK = 1001;
    private static final int AUTHORIZATION_TWITTER = 1002;

    private static final int SHARING_FACEBOOK = 1004;
    private static final int SHARING_TWITTER = 1005;
    private static final int SHARING_EMAIL = 1006;
    private static final int SHARING_SMS = 1007;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 1008 ;

    private final String PREFERENCE_CONFIG_TIMESTAMP = "config_timestamp";

    static String FLURYY_ID = "WXFRQZ8HX5Q74RDZ447P";
    //"QJ6J383WY4JTQT4H5Z78"; // Brazhnik test

    static String FACEBOOK_APP_ID = "__FACEBOOK_USER_APP_ID__";
    //"207296122640913";
    static String FACEBOOK_APP_SECRET = "__FACEBOOK_USER_APP_SECRET__";
    //"3cae87e561313d4dd07c076566e2c67a";

    static String TWITTER_CONSUMER_KEY = "__TWITTER_USER_CONSUMER_KEY__";
    //"p48aBftV8vXXfG6UWo0BcQ";
    static String TWITTER_CONSUMER_SECRET = "__TWITTER_USER_CONSUMER_SECRET__";
    //"YYkHCKtSD7uYhSC3jtPL1H2b6NaX2u6x5kOLLgRUA";

    private static final String TAG = "com.ibuildapp.appbuilder";
    private static final String TAG_PUSH = "PUSHNS_appbuilder";
    //"Ygi9MXR19vXsAYAwKzS0kesLl4IkJ9o5lnqMh0P0";

    private final String MENU_INDEX = "MenuIndex";
    private final String MENU_TOP_COORDINATE = "MenuCoordinate";
    private final int START_MODULE = 10001;

    private final int LOGIN_SCREEN = 777;

    private final String WIDGET_HOLDER_BACKGROUND = "#3f434b";
    AppConfigure appConfig = new AppConfigure();

    private double screenCoef = 1;
    public boolean isAuthorized = false;

    private String fileName = "xmlData.xml";

    private boolean sdAvailable = false;
    private boolean flurryStarted = false;
    private boolean dontLaunchModule = false;

    private Random rand = new Random();
    private ProgressDialog progressDialog = null;
    private String userID = "";
    final private int requestCode = rand.nextInt(65535);

    public static String APP_ID = "RReePPLLaaCCee";

    private String APP_TOKEN = "TTooKKeeNN";

    public static  String DOMEN = "uuRRLL";

    private String xmlUrl = //"http://ibuilder.solovathost.com/xml.php?project=1523&type=android";
            "http://" + DOMEN + "/xml.php?project=" + APP_ID + "&type=android";

    private String pushRegistrationUrl = //"http://ibuilder.solovathost.com/pushns.registration.php?project=1523&platform=android";
            "http://" + DOMEN + "/pushns.registration.php?project=" + APP_ID + "&platform=android";

    //private boolean isOnline = false;
    //private boolean isMobileConnection = false;
    private boolean firstPluginStart = true;

    final private int CONFIGURATION_LOADED_SUCCESS = 1;
    final private int CONFIGURATION_LOADED_FAILED = 2;
    final private int INTERFACE_BUILDED_SUCCESS = 3;
    final private int INTERFACE_BUILDED_FAILED = 4;
    final private int LISTEN_INTERFACE_BUILDING = 5;
    final private int NO_SOURCE_URL = 6;
    final private int NO_SD_CARD = 7;
    final private int PUSH_NOTIFICATION_INIT = 8;
    final private int GPS_NOTIFICATION_SERVICE_START = 9;
    final private int GPS_NOTIFICATION_SERVICE_STOP = 10;
    final private int GPS_NOTIFICATION_SERVICE_INTENT_START = 11;
    final private int REFRESH_APP_DATA = 12;
    final private int NEED_INTERNET_CONNECTION = 13;
    private int iterator = 0;
    private NotificationManager mManager;
    private BroadcastReceiver broadcastReceiver;
    private boolean foreground = true;

    // ***************************************************************************************
    // 13.06.2013 Brazhnik
    // ***************************************************************************************

    private boolean allowCloseAnimation = false;
    private long startTime;

    private LinearLayout dialogHolder;
    private LinearLayout favouritesHolder;
    private boolean isDialogShowen = false;
    private AlphaAnimation animShowDialog;
    private AlphaAnimation animHideDialog;
    private Animation animShowMenu;
    private Animation animHideMenu;
    private String startedPluginName;

    private SwipeLinearLayout rootContainer;
    private FrameLayout rootFrameLayout;
    private LinearLayout userContainer;
    private LinearLayout menuContainer;
    private SideBarComponent rootScroller;
    private OnSwipeInterface swipeInterface;

    private LayoutInflater layoutInflater;
    private int screenWidth;
    private ArrayList<Widget> actualWidgetList = new ArrayList<Widget>();
    private ArrayList<Bitmap> thumbnails = new ArrayList<Bitmap>();
    private SharedPreferences sPref;
    private boolean isShown = false;

    // Для сохранения статуса приложения
    private SharedPreferences preferences;
    private String packageName;
    private String statusRunning;
    private String statusClosed;

    private ListView widgetList;

    private SmsBody body;
    // handler for interconnection between mainUI thread and work thread
    // which checks appropriate configuration and pars source XML file
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NO_SD_CARD: {
                    Toast.makeText(AppBuilder.this, getString(R.string.no_sd_card), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finishActivity(requestCode);
                            finish();
                        }
                    }, 5000);
                }
                break;
                case NO_SOURCE_URL: {
                    Toast.makeText(AppBuilder.this, getString(R.string.no_datasource), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finishActivity(requestCode);
                            finish();
                        }
                    }, 5000);
                }
                break;
                case CONFIGURATION_LOADED_SUCCESS: {
                    try {
                        com.appbuilder.sdk.android.Statics.showLink = appConfig.getShowLink();
                        com.appbuilder.sdk.android.Statics.appName = appConfig.getAppName();
                        com.appbuilder.sdk.android.Statics.analiticsHandler = new GoogleAnaliticsHandler(
                                getApplicationContext(),
                                appConfig.getAppName(),
                                appConfig.getGoogleAnalyticsId());
                        com.appbuilder.sdk.android.Statics.analiticsHandler.sendUserEvent("Start Application", "-");
                        com.appbuilder.sdk.android.Statics.analiticsHandler.sendIbuildAppEvent("Start Application", "-");
                    } catch (Exception e) {
                        logWarning(e);
                    }
                    listenInterfaceBuilder();
                }
                break;
                case CONFIGURATION_LOADED_FAILED: {
                    Toast.makeText(AppBuilder.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finishActivity(requestCode);
                            finish();
                        }
                    }, 5000);
                }
                break;
                case INTERFACE_BUILDED_SUCCESS: {
                    putDataInCache();
                    finishActivity(requestCode);
                    //rootScroller.recomputeContentSize();

                    if ( !com.appbuilder.sdk.android.Statics.fromMasterApp )
                    {
                        if ( appConfig.getLoginScreen() != null && !isAuthorized) {
                            isAuthorized = true;

                            LoginSettings settings = LoginSettingsService.loadSettings(
                                    AppBuilder.this.getSharedPreferences("LOGIN_SETTINGS", MODE_PRIVATE));

                            final String password = settings.getPassword();
                            final String username = settings.getUsername();

                            if (password.length() > 0 && username.length() > 0) {
                                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo ni = cm.getActiveNetworkInfo();
                                if (ni != null && ni.isConnectedOrConnecting()) {
                                } else {
                                    Log.d("", "");

                                    return;
                                }

                                LoginScreenService.doLogin(appConfig.getLoginScreen().getLoginEndpoint(),
                                        username, password, "email", appConfig.getLoginScreen().getAppId(), new OnDone() {
                                    @Override
                                    public void onDone(int result) {
                                        if (result == 200) {
                                            if (userID != null && userID.equals("186589")) {
                                                FlurryAgent.setUserId(username);
                                            }
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent it = new Intent(AppBuilder.this, LoginWithEmailActivity.class);
                                                    it.putExtra("loginScreen", appConfig.getLoginScreen());
                                                    it.putExtra("navBarDesign", appConfig.getNavBarDesign());
                                                    startActivityForResult(it, LOGIN_SCREEN);
                                                    needToClose = true;
                                                }
                                            });

                                        }
                                    }
                                });
                            } else {
                                Intent it = new Intent(AppBuilder.this, LoginWithEmailActivity.class);
                                it.putExtra("loginScreen", appConfig.getLoginScreen());
                                it.putExtra("navBarDesign", appConfig.getNavBarDesign());
                                startActivityForResult(it, LOGIN_SCREEN);
                                needToClose = true;
                            }
                        } else {
                    /* if found GPS data and service is not started then start service */
                            if (appConfig.getGPSNotifications().size() > 0) {
                                handler.sendEmptyMessage(GPS_NOTIFICATION_SERVICE_START);
                            } else {
                                handler.sendEmptyMessage(GPS_NOTIFICATION_SERVICE_STOP);
                            }
                        }
                    }

                }
                break;
                case INTERFACE_BUILDED_FAILED: {
                    Toast.makeText(AppBuilder.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finishActivity(requestCode);
                            finish();
                        }
                    }, 5000);
                }
                break;
                case LISTEN_INTERFACE_BUILDING: {
                    listenInterfaceBuilder();
                }
                break;
                case PUSH_NOTIFICATION_INIT: {
                    pushNotificationInit();
                }
                break;
                case GPS_NOTIFICATION_SERVICE_START: {
                    startGPSNotificationService();
                }
                break;
                case GPS_NOTIFICATION_SERVICE_INTENT_START: {
                    startService();
                }
                break;
                case GPS_NOTIFICATION_SERVICE_STOP: {
                    stopGPSNotificationService();
                }
                break;
                case REFRESH_APP_DATA: {
                    try {
                        reloadAppConfigure();
                    } catch (Exception e) {
                    }
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(AppBuilder.this, getString(R.string.need_internet_connection), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finishActivity(requestCode);
                            finish();
                        }
                    }, 5000);
                }
                break;
            }
        }
    };

    private void logWarning(Exception e) {
        Log.w(TAG, e);
    }

    private void logError(Exception e) {
        Log.e(TAG, "", e);
    }

    public AppConfigure getAppConfig() {
        return appConfig;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            startTime = new Date().getTime();

            Log.e(TAG, "onCreate()");
            super.onCreate(savedInstanceState);

            overridePendingTransition(R.anim.activity_open_translate_main, R.anim.activity_close_scale_main);

            preferences = getSharedPreferences(getString(R.string.core_pushns_status), Context.MODE_PRIVATE);
            packageName = getPackageName();
            statusRunning = getString(R.string.core_pushns_status_running);
            statusClosed = getString(R.string.core_pushns_status_closed);


            //if(!LibsChecker.checkVitamioLibs(this,R.raw.libarm))return;
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.core_main);
            layoutInflater = LayoutInflater.from(this);

       //     postInit();

            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_REQUEST);
            } else {
                postInit();
            }

        } catch (Exception e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    postInit();
                } else {
                    finish();
                }
                return;
            }
        }
    }

    protected  void postInit() {
        try {
            String packName = getPackageName();
            String lastPart = packName.substring(packName.lastIndexOf(".") + 1);
            String uId = lastPart.substring(lastPart.indexOf("u") + 1, lastPart.indexOf("p"));
            userID = uId;
            String projectId = packName.substring(packName.lastIndexOf("p") + 1);
            if (projectId.equals("382087")) {
                FLURYY_ID = "FQ7DCSYHT8MXJPJ5DKDX";
            } else if (projectId.equals("478651")) {
                FLURYY_ID = "JNJ56CS9QKXYVQG7YH62";
            }
        } catch (Throwable thr) {
        }

        // computing display width
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        String UID = md5(Secure.getString(getContentResolver(), Secure.ANDROID_ID));
        xmlUrl += "&device=" + UID;
        pushRegistrationUrl += "&device=" + UID;

        com.appbuilder.sdk.android.Statics.appId = APP_ID;
        com.appbuilder.sdk.android.Statics.appToken = APP_TOKEN;
        com.appbuilder.sdk.android.Statics.BASE_DOMEN = DOMEN;
        com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID = FACEBOOK_APP_ID;
        com.appbuilder.sdk.android.Statics.FACEBOOK_APP_SECRET = FACEBOOK_APP_SECRET;
        com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_KEY = TWITTER_CONSUMER_KEY;
        com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_SECRET = TWITTER_CONSUMER_SECRET;
        appConfig.setmAppId(APP_ID);//"RReePPLLaaCCee");


        Log.e(TAG, "xmlUrl = " + xmlUrl + " \npushRegUrl = " + pushRegistrationUrl);
        String appFromURLScheme = "";
        Intent urlSchemeIntent = getIntent();

        if (Intent.ACTION_VIEW.equals(urlSchemeIntent.getAction())) {
            Uri uri = urlSchemeIntent.getData();
            appFromURLScheme = uri.getQueryParameter("app");
        }

        int appIDfromURLScheme = -1;


        Intent parent = getIntent();
        int appid = appIDfromURLScheme > 0 ? appIDfromURLScheme : parent.getIntExtra("appid",-1);

        if ( appid != -1 )
        {
            com.appbuilder.sdk.android.Statics.fromMasterApp = true;
            com.appbuilder.sdk.android.Statics.favouritedMasterApp = getIntent().getBooleanExtra("favourites",false);

            APP_ID = Integer.toString(appid);
            com.appbuilder.sdk.android.Statics.appId = Integer.toString(appid);
            appConfig.setmAppId(Integer.toString(appid));
            com.appbuilder.sdk.android.Statics.appToken = parent.getStringExtra("token");
            APP_TOKEN = parent.getStringExtra("token");

            com.appbuilder.sdk.android.Statics.BASE_DOMEN = "ibuildapp.com";
            DOMEN = "ibuildapp.com";

            com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID = "207296122640913";
            FACEBOOK_APP_ID = "207296122640913";
            com.appbuilder.sdk.android.Statics.FACEBOOK_APP_SECRET = "3cae87e561313d4dd07c076566e2c67a";
            FACEBOOK_APP_SECRET = "3cae87e561313d4dd07c076566e2c67a";
            com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_KEY = "p48aBftV8vXXfG6UWo0BcQ";
            TWITTER_CONSUMER_KEY = "p48aBftV8vXXfG6UWo0BcQ";
            com.appbuilder.sdk.android.Statics.TWITTER_CONSUMER_SECRET = "YYkHCKtSD7uYhSC3jtPL1H2b6NaX2u6x5kOLLgRUA";
            TWITTER_CONSUMER_SECRET = "YYkHCKtSD7uYhSC3jtPL1H2b6NaX2u6x5kOLLgRUA";
            xmlUrl = "http://" + DOMEN + "/xml.php?project=" + APP_ID + "&type=android";

            pushRegistrationUrl = "http://" + DOMEN + "/pushns.registration.php?project=" + APP_ID + "&platform=android";
        } else
        {
            UID = md5( Secure.getString(getContentResolver(), Secure.ANDROID_ID) );
            xmlUrl += "&device=" + UID;
            pushRegistrationUrl += "&device=" + UID;
        }


        boolean notParsed = false;

        if (appFromURLScheme.length() > 0) {
            try {
                appIDfromURLScheme = Integer.parseInt(appFromURLScheme);
                com.appbuilder.sdk.android.Statics.fromMasterApp = true;
            } catch (Exception exception) {
                notParsed = true;
                finish();
            }
        }
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG_PUSH, "Notification received");
                if (foreground) {
                    Log.e(TAG_PUSH, "FOREGROUND - SHOW");
                    createPushDialog();
                } else
                    Log.e(TAG_PUSH, "BACKGROUND - DONT SHOW");

            }
        };
        Statics.BROADCAST_UID = APP_ID;
        IntentFilter intFilt = new IntentFilter(APP_ID);
        registerReceiver(broadcastReceiver, intFilt);
        AppPushNotificationDB.init(AppBuilder.this);
        AppPushNotificationDB.insertXmlUrl(xmlUrl);

        long configTimestamp = Prefs.with(AppBuilder.this).getLong(appid + "_" + Prefs.PREFERENCE_CONFIG_TIMESTAMP, 0);

        com.appbuilder.sdk.android.Statics.firstStart = !Prefs.with(AppBuilder.this).getBoolean(appid + "_" + Prefs.PREFERENCE_NOT_FIRST_LAUNCH, false);
        if(com.appbuilder.sdk.android.Statics.firstStart) {
            Prefs.with(AppBuilder.this).save(appid + "_" + Prefs.PREFERENCE_NOT_FIRST_LAUNCH, true);
        }

        xmlUrl += "&timestamp=" + configTimestamp;

        String pushMessage = getIntent().getStringExtra("pushNotificationMessage");
        if (pushMessage != null || notParsed) {
            Intent pushNotification = new Intent(this, AppPushNotification.class);
            Bundle store = new Bundle();
            store.putString("pushNotificationMessage", pushMessage);
            pushNotification.putExtras(store);
            startActivity(pushNotification);
            finish();
        } else {
            // splash screen loading
            try {
                Intent intent = new Intent(this, SplashScreen.class);
                String splashPath = getIntent().getStringExtra("splash");
                if ( !TextUtils.isEmpty(splashPath) )
                    intent.putExtra("splash", splashPath);
                startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                handler.sendEmptyMessageDelayed(CONFIGURATION_LOADED_FAILED, 100);
            }

            if (!Utils.sdcardAvailable()) {
                Log.w("SD CARD", "false");
                sdAvailable = false;
            } else {
                if (sdAvailableBytes() > 3072) {
                    sdAvailable = true;
                } else {
                    sdAvailable = false;
                }
            }

            if (sdAvailable) {
                // creating cache file...
                // /sdcard/AppBuilder/com.appbuilder.u12425r
                if ( !com.appbuilder.sdk.android.Statics.fromMasterApp )
                    com.appbuilder.sdk.android.Statics.cachePath = Environment.getExternalStorageDirectory() + "/AppBuilder/" + getPackageName();
                else
                    com.appbuilder.sdk.android.Statics.cachePath = Environment.getExternalStorageDirectory() + "/AppBuilder/" + APP_ID+File.separator;
                File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/assets");
                if (!cache.exists()) {
                    cache.mkdirs();
                }
                File nomedia = new File(Environment.getExternalStorageDirectory() + "/AppBuilder/.nomedia");
                if (!nomedia.exists()) {
                    try {
                        nomedia.createNewFile();
                    } catch (Exception e) {
                    }
                }
            }

            //isOnline = false;

            Thread mainThread = new Thread() {

                @Override
                public void run() {
                    try {
                        boolean isConfigurationLoaded = false;

                        /**
                         * if internet connection exists -> start downloading
                         * configuration XML file from ibuildapp server
                         * */
                        if (Tools.checkNetwork(AppBuilder.this) > 0) {
                            isConfigurationLoaded = loadDatafromURL();
                        }


                        /**
                         * If loading *.xml file from Internet failed
                         * read *.xml configuration file from cache
                         * */
                        if (!isConfigurationLoaded) {
                            if (sdAvailable) {
                                File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.data");
                                if (cache.exists()) {
                                    try {
                                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
                                        appConfig = (AppConfigure) ois.readObject();
                                        ois.close();
                                        isConfigurationLoaded = true;
                                    } catch (Exception e) {
                                        Log.w("LOAD CONFIG", e);
                                    }
                                }
                            }
                        }

                        /**
                         * Reading default *.xml file from folder /raw
                         * */
                        if (!isConfigurationLoaded) {
                            String builtInXml = "";
                            InputStream is = getResources().openRawResource(R.raw.configuration);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte buf[] = new byte[512];
                            int flag = 0;
                            while ((flag = is.read(buf, 0, 512)) != -1) {
                                baos.write(buf, 0, flag);
                                Arrays.fill(buf, (byte) 0);
                            }
                            builtInXml = baos.toString();

                            boolean builtInLoaded = false;

                            if (builtInXml.length() > 0) {
                                try {
                                    Xml.parse(new ByteArrayInputStream(builtInXml.getBytes()), Xml.Encoding.UTF_8, null);
                                    builtInLoaded = true;
                                } catch (SAXException sAXEx) {
                                }
                            }

                            if (builtInLoaded) {
                                try {
                                    AppConfigureParser acp = new AppConfigureParser(AppBuilder.this, builtInXml);
                                    appConfig = acp.parseSAX();//appConfig = acp.parse();
                                    isConfigurationLoaded = true;

                                    // check if cache xml and buildin xml are equal
                                    if (sdAvailable) {
                                        String xmlMD5 = md5(builtInXml);
                                        File cacheMD5 = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.md5");

                                        if (cacheMD5.exists()) {
                                            StringBuilder sb = new StringBuilder();
                                            try {
                                                BufferedReader br = new BufferedReader(new FileReader(cacheMD5));
                                                String line;
                                                while ((line = br.readLine()) != null) {
                                                    sb.append(line);
                                                }
                                                br.close();
                                            } catch (Exception e) {
                                            }

                                            // if downloaded md5(xmlString) == md5(cacheFile)
                                            if (!xmlMD5.equals(sb.toString())) {
                                                try {
                                                    cacheMD5.delete();
                                                    cacheMD5.createNewFile();
                                                    try {
                                                        BufferedWriter bw = new BufferedWriter(new FileWriter(cacheMD5));
                                                        bw.write(xmlMD5);
                                                        bw.close();
                                                    } catch (Exception e) {
                                                    }
                                                } catch (IOException iOEx) {
                                                }
                                            }
                                        } else {
                                            try {
                                                cacheMD5.createNewFile();
                                                try {
                                                    BufferedWriter bw = new BufferedWriter(new FileWriter(cacheMD5));
                                                    bw.write(xmlMD5);
                                                    bw.close();
                                                } catch (Exception e) {
                                                }
                                            } catch (IOException e) {
                                            }
                                        }
                                    }


                                    // save to cache
                                    if (sdAvailable) {
                                        File file = null;
                                        String fileName = "";
                                        String cacheFileName = "";
                                        ArrayList<String> fileNames = new ArrayList<String>();

                                            /* check background */
                                        fileName = md5(appConfig.getBackgroundImageUrl());
                                        cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                                        file = new File(cacheFileName);
                                        if (file.exists()) {
                                            appConfig.setBackgroundImageCache(cacheFileName);
                                            appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                                        }
                                        fileNames.add(fileName);
                                            /* check buttons */
                                        for (int i = 0; i < appConfig.getButtonsCount(); i++) {
                                            WidgetUIButton button = appConfig.getButtonAtIndex(i);
                                            fileName = md5(button.getImageSourceUrl());
                                            cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                                            file = new File(cacheFileName);
                                            if (file.exists()) {
                                                button.setImageSourceCache(cacheFileName);
                                                button.setDownloadStatus(DownloadStatus.SUCCESS);
                                                appConfig.setButtonAtIndex(i, button);
                                            }
                                            fileNames.add(fileName);
                                        }

                                            /* check images */
                                        for (int i = 0; i < appConfig.getImagesCount(); i++) {
                                            WidgetUIImage image = appConfig.getImageAtIndex(i);
                                            fileName = md5(image.getSourceUrl());
                                            cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                                            file = new File(cacheFileName);
                                            if (file.exists()) {
                                                image.setSourceCache(cacheFileName);
                                                image.setDownloadStatus(DownloadStatus.SUCCESS);
                                                appConfig.setImageAtIndex(i, image);
                                            }
                                            fileNames.add(fileName);
                                        }

                                            /* check tabs */
                                        for (int i = 0; i < appConfig.getTabsCount(); i++) {
                                            WidgetUITab tab = appConfig.getTabAtIndex(i);
                                            fileName = md5(tab.getIconCache());
                                            cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                                            file = new File(cacheFileName);
                                            if (file.exists()) {
                                                tab.setIconCache(cacheFileName);
                                                tab.setDownloadStatus(DownloadStatus.SUCCESS);
                                                appConfig.setTabAtIndex(i, tab);
                                            }
                                            fileNames.add(fileName);
                                        }

                                            /* CLEAR old cache files */
                                        File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/assets/");
                                        File[] files = cache.listFiles();
                                        for (int i = 0; i < files.length; i++) {
                                            String currentFile = files[i].getName();
                                            boolean fl = false;
                                            for (int j = 0; j < fileNames.size(); j++) {
                                                if (currentFile.equals(fileNames.get(j))) {
                                                    fl = true;
                                                }
                                            }
                                            if (!fl) {
                                                files[i].delete();
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    Log.e("", ex.getMessage());
                                }
                            }
                        }

                        // *************************************************
                        // Sergey 22.02.2013
                        // downloading splashscreen image and saving it
                        // to application cache directory
                        if (appConfig.getSplashScreen() != null) {
                            splashScreenDownload(appConfig.getSplashScreen(), com.appbuilder.sdk.android.Statics.cachePath);
                        }

                        for (Widget w : appConfig.getmWidgets()) {
                            String md5Data = Utils.md5(w.getPluginXmlData());
                            w.setUpdated(ConfigDBHelper.hasWidgetChanged(AppBuilder.this, w.getWidgetId(), md5Data));
                        }

                        // *************************************************
                        // proccess modules favicons
                        downloadFavicons(appConfig);
                        for (int i = 0; i < appConfig.getmWidgets().size(); i++) {
                            try {
                                Bitmap tempBM = proccessBitmap(appConfig.getmWidgets().get(i).getFaviconFilePath());
                                thumbnails.add(i, tempBM);
                            } catch (Exception e) {
                                Log.d("", "");
                            }
                        }

                        appConfig.setmAppId(APP_ID);

                        // start splash screen processing after XML is ready!
                        if ( !com.appbuilder.sdk.android.Statics.fromMasterApp )
                        {
                            if (Statics.innerInterface != null) {
                                Statics.innerInterface.onPost(appConfig);
                            }
                        }

                        // ************** Sergey's snippet ends ************
                        /**
                         * Sending message to handler about state of configuration loading
                         * */
                        if (isConfigurationLoaded) {
                            handler.sendEmptyMessage(CONFIGURATION_LOADED_SUCCESS);
                            if ( !com.appbuilder.sdk.android.Statics.fromMasterApp )
                                handler.sendEmptyMessage(PUSH_NOTIFICATION_INIT);
                        } else {
                            handler.sendEmptyMessageDelayed(CONFIGURATION_LOADED_FAILED, 100);
                            return;
                        }

                            /*
                             * At last starting rendering UI interface
                             * !! still in separate thread using runOnUithread()
                             * for intrerracting with main UI thread
                             * */
                        AppBuilder.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    drawInterface();
                                } catch (Exception e) {
                                    Log.e("", e.getMessage());
                                }
                            }
                        });

                    } catch (Exception e) {
                    }
                }
            };
            mainThread.start();
        }

        if (notParsed){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        profiling("onDestroy() - start");

        try {
            SharedPreferences.Editor ed = preferences.edit();
            ed.putString(packageName, statusClosed);
            ed.commit();
            Log.e(TAG, "onDestroy() - Sharedpref save OK. Arg = " + packageName + " Val = " + statusClosed);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy() - Sharedpref save error.");
        }

        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);

        profiling("onDestroy() - stop");

        com.appbuilder.sdk.android.Statics.sidebarClickListeners.clear();
        com.appbuilder.sdk.android.Statics.clearSidebarNonWidgetClickListenerIndex();
        com.appbuilder.sdk.android.Statics.resetAdStatus();

        super.onDestroy();

        System.gc();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart()");
        if (!"".equals(FLURYY_ID) && !"FFLLuuRRRRyy".equals(FLURYY_ID) &&
                !flurryStarted) {
            try {
                FlurryAgent.onStartSession(this, AppBuilder.FLURYY_ID);
                flurryStarted = true;
            } catch (Exception e) {
                Log.e("", "");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        profiling("onStop() - start");
        foreground = false;

        if (flurryStarted) {
            try {
                FlurryAgent.onEndSession(this);
                flurryStarted = false;
            } catch (Exception e) {
                Log.e("", "");
            }
        }
        profiling("onStop() - end");
    }

    private boolean needToClose = false;

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume()");

        foreground = true;
        try{
            SharedPreferences.Editor ed = preferences.edit();
            ed.putString(packageName, statusRunning);
            ed.commit();
        }catch(Exception ex){
        }

        dontLaunchModule = false;

        if (needToClose) {
            if (com.appbuilder.sdk.android.Statics.closeMain) {
                com.appbuilder.sdk.android.Statics.closeMain = false;
                super.onResume();
                finish();
                return;
            } else {
                needToClose = false;
            }
        }

        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        if (rootLayout != null) {
            for (int i = 0; i < rootLayout.getChildCount(); i++) {
                Drawable d = rootLayout.getChildAt(i).getBackground();
                if (d != null) {
                    d.setColorFilter(null);
                    rootLayout.getChildAt(i).setBackgroundDrawable(d);
                }
                //rootLayout.getChildAt(i).getBackground().setColorFilter(null);
            }
        }

        // restore list of widgets position
        sPref = getSharedPreferences("MyPref", MODE_PRIVATE);

        if (dialogHolder != null)
            createPushDialog();

        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.e(TAG, "onRestart()");

        if (dialogHolder != null)
        createPushDialog();

        super.onRestart();

        if (!TextUtils.isEmpty(startedPluginName)) {
            com.appbuilder.sdk.android.Statics.analiticsHandler.sendUserEvent("Stop Module", startedPluginName);
        }
    }

    private void createPushDialog() {
        Log.e(TAG_PUSH, "createPushDialog");
        final AppPushNotificationMessage freshMessage = AppPushNotificationDB.getNotificationIfExist();
        if (freshMessage != null) {
            if (!isDialogShowen) {
                AppPushNotificationDialogLayout dialog = null;
                if (freshMessage.isPackageExist) {
                    dialog = new AppPushNotificationDialogLayout(
                            AppBuilder.this,
                            freshMessage.titleText,
                            freshMessage.descriptionText,
                            freshMessage.imagePath,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialogHolder.clearAnimation();
                                    isDialogShowen = false;
                                    dialogHolder.startAnimation(animHideDialog);
                                    createPushDialog();
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialogHolder.clearAnimation();
                                    isDialogShowen = false;
                                    dialogHolder.removeAllViews();
                                    dialogHolder.setVisibility(View.INVISIBLE);
                                    launchWidgetWithOrder(freshMessage.widgetOrder);
                                }
                            }
                    );
                } else {
                    if (freshMessage.widgetOrder == -1)
                    {
                        dialog = new AppPushNotificationDialogLayout(
                                AppBuilder.this,
                                freshMessage.titleText,
                                freshMessage.descriptionText,
                                freshMessage.imagePath,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialogHolder.clearAnimation();
                                        isDialogShowen = false;
                                        dialogHolder.startAnimation(animHideDialog);
                                        createPushDialog();
                                    }
                                },
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //To change body of implemented methods use File | Settings | File Templates.
                                    }
                                }
                        );
                    } else {
                        dialog = new AppPushNotificationDialogLayout(
                                AppBuilder.this,
                                freshMessage.titleText,
                                freshMessage.descriptionText,
                                freshMessage.imagePath,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialogHolder.clearAnimation();
                                        isDialogShowen = false;
                                        dialogHolder.startAnimation(animHideDialog);
                                        createPushDialog();
                                    }
                                },
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialogHolder.clearAnimation();
                                        isDialogShowen = false;
                                        dialogHolder.startAnimation(animHideDialog);
                                    }
                                }
                        );
                    }
                }

                int tabCount = appConfig.getTabsCount();
                if (tabCount != 0) {
                    FrameLayout.LayoutParams fparams = (FrameLayout.LayoutParams) dialogHolder.getLayoutParams();
                    fparams.setMargins(0, 0, 0, (int) (75 / 2));
                    dialogHolder.setLayoutParams(fparams);
                } else {
                    FrameLayout.LayoutParams fparams = (FrameLayout.LayoutParams) dialogHolder.getLayoutParams();
                    fparams.setMargins(0, 0, 0, 0);
                    dialogHolder.setLayoutParams(fparams);
                }

                dialogHolder.removeAllViews();
                dialogHolder.addView(dialog);
                dialogHolder.setVisibility(View.VISIBLE);
                isDialogShowen = true;
                dialogHolder.clearAnimation();
                dialogHolder.startAnimation(animShowDialog);


                AppPushNotificationDB.deleteItemFromRelations(freshMessage.uid);
                mManager.cancel((int) freshMessage.uid);
                Log.e(TAG_PUSH, "We have message = " + freshMessage.toString());
            }
        } else {
            Log.e(TAG_PUSH, " no message found");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.clear();

        MenuItem menuItem = menu.add("");
        menuItem.setTitle(getResources().getString(R.string.core_refresh_date));
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handler.sendEmptyMessage(REFRESH_APP_DATA);
                return true;
            }

        });

        return true;
    }

    private void reloadAppConfigure() {
        try {
            if(appConfig.isShowSidebar() && isShown)
                hideSidebar();

            actualWidgetList.clear();
            com.appbuilder.sdk.android.Statics.sidebarClickListeners.clear();

            progressDialog = ProgressDialog.show(this, null, "Loading...", true);
            progressDialog.setCancelable(true);
            progressDialog.show();

            new Thread() {
                @Override
                public void run() {
                    com.appbuilder.sdk.android.Statics.firstStart = false;
                    boolean isConfigurationLoaded = false;
                    try {
                        isConfigurationLoaded = loadDatafromURL();
                    } catch (Exception e) {
                    }

                    if (isConfigurationLoaded) {
                        handler.sendEmptyMessage(CONFIGURATION_LOADED_SUCCESS);
                        handler.sendEmptyMessage(PUSH_NOTIFICATION_INIT);

                        for ( Widget w : appConfig.getmWidgets() )
                        {
                            String md5Data = Utils.md5(w.getPluginXmlData());
                            w.setUpdated(ConfigDBHelper.hasWidgetChanged(AppBuilder.this, w.getWidgetId(), md5Data));
                        }

                        AppBuilder.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    drawInterface();
                                } catch (Exception e) {
                                }
                            }
                        });
                    }

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }.start();

        } catch (Exception e) {
        }
    }

    /* Loading *.xml file from ibuilapp server */
    private boolean loadDatafromURL() throws IOException {
        try {//ErrorLogging

            boolean useCache = false;
            long ping = 0;
            long timestamp = 0;


            try { /* reading xml data from url */
                long start = System.currentTimeMillis();
                URL url = new URL(xmlUrl);

                URLConnection conn = url.openConnection();
                InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

                ping = System.currentTimeMillis() - start;
                if (Tools.checkNetwork(AppBuilder.this) == 2 && ping > 5000) {
                    useCache = true;
                }

                BufferedReader br = new BufferedReader(streamReader);
                File file = Environment.getExternalStorageDirectory();
                FileOutputStream f = new FileOutputStream(new File(file, fileName));
                org.apache.commons.io.IOUtils.copy(br, f);
                br.close();

                try {
                    timestamp = Long.valueOf(conn.getHeaderField("App-Config-Last-Modified"));
                } catch (Exception ex) {
                    Log.e("", "");
                }

                Log.e("", "");
            } catch (Exception e) {
                return false;
            }
        
        /* is XML well-formed document? */
            boolean isXMLWellFormed = true;
            try {
                File dir = Environment.getExternalStorageDirectory();
                File file = new File(dir, fileName);
                Xml.parse(new FileReader(file), null);
            } catch (Exception e) {
                isXMLWellFormed = false;
                useCache = true;
            }

            if (sdAvailable && isXMLWellFormed) { /* check using cache */
                File dir = Environment.getExternalStorageDirectory();
                File file = new File(dir, fileName);
                String xmlMD5 = md5(new FileInputStream(file));
                File cacheMD5 = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.md5");

                if (cacheMD5.exists()) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(cacheMD5));
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                    } catch (Exception e) {
                    }

                    // if downloaded md5(xmlString) == md5(cacheFile)
                    if (xmlMD5.equals(sb.toString())) {
                        File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.data");
                        if (cache.exists()) {
                            useCache = true;
                        }
                    } else {
                        try {
                            cacheMD5.delete();
                            cacheMD5.createNewFile();

                            File mfile = new File(com.appbuilder.sdk.android.Statics.cachePath + File.separator + "splash.jpg");
                            if (mfile.exists()) {
                                mfile.delete();
                            }
                            try {
                                BufferedWriter bw = new BufferedWriter(new FileWriter(cacheMD5));
                                bw.write(xmlMD5);
                                bw.close();
                            } catch (Exception e) {
                            }
                        } catch (IOException iOEx) {
                        }
                    }
                } else {
                    try {
                        cacheMD5.createNewFile();
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(cacheMD5));
                            bw.write(xmlMD5);
                            bw.close();
                        } catch (Exception e) {
                        }
                    } catch (IOException e) {
                    }
                }
            }

            boolean isConfLoaded = false;
            if (useCache) {
                File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.data");
                File cacheMD5 = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.md5");

                if (cache.exists()) {
                    ObjectInputStream ois = null;
                    try {
                        ois = new ObjectInputStream(new FileInputStream(cache));
                        appConfig = (AppConfigure) ois.readObject();
                        ois.close();

                        /**
                         * parsing configuration XML file
                         * */


                        File dir = Environment.getExternalStorageDirectory();
                        File file1 = new File(dir, fileName);
                        AppConfigureParser parser = new AppConfigureParser(AppBuilder.this, new FileInputStream(file1));
                        AppConfigure tmpAppConfig = parser.parseSAX();

                        /**
                         * in case, that configuration readed sucessfully, well-formed
                         * and parsed -> update(create) cash configuration
                         * */
                        if (appConfig.equals(tmpAppConfig)) {
                            boolean allDownloaded = true;

                            File file = null;
                            String fileName = "";
                            String cacheFileName = "";

                            fileName = md5(tmpAppConfig.getBackgroundImageUrl());
                            cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                            file = new File(cacheFileName);
                            if (!file.exists()) {
                                allDownloaded = false;
                            }

                            if (allDownloaded) {
                                for (int i = 0; i < tmpAppConfig.getButtonsCount(); i++) {
                                    WidgetUIButton button = tmpAppConfig.getButtonAtIndex(i);
                                    fileName = md5(button.getImageSourceUrl());
                                    cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                                    file = new File(cacheFileName);
                                    if (!file.exists()) {
                                        allDownloaded = false;
                                        break;
                                    }
                                }
                            }

                            if (allDownloaded) {
                                for (int i = 0; i < tmpAppConfig.getImagesCount(); i++) {
                                    WidgetUIImage image = tmpAppConfig.getImageAtIndex(i);
                                    fileName = md5(image.getSourceUrl());
                                    cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                                    file = new File(cacheFileName);
                                    if (!file.exists()) {
                                        allDownloaded = false;
                                        break;
                                    }
                                }
                            }

                            if (allDownloaded) {
                                for (int i = 0; i < tmpAppConfig.getTabsCount(); i++) {
                                    WidgetUITab tab = tmpAppConfig.getTabAtIndex(i);
                                    fileName = md5(tab.getIconCache());
                                    cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                                    file = new File(cacheFileName);
                                    if (!file.exists()) {
                                        allDownloaded = false;
                                        break;
                                    }
                                }
                            }

                            if (allDownloaded) {
                                isConfLoaded = true;
                            }
                        } else {
                            isConfLoaded = false;
                        }
                    } catch (Exception e) { /* configuration cannot be loaded */
                        if (ois != null) {
                            ois.close();
                        }
                        cache.delete();

                        if (cacheMD5.exists()) {
                            cacheMD5.delete();
                        }
                    }
                } else {
                    if (cacheMD5.exists()) {
                        cacheMD5.delete();
                    }

                    useCache = false;
                }
            }

            if (isConfLoaded) {
                return true;
            } else {
                if (!isXMLWellFormed) {
                    return false;
                }
                if (ping > 5000) {
                    return false;
                }

                File dir = Environment.getExternalStorageDirectory();
                File file1 = new File(dir, fileName);
                AppConfigureParser acp = new AppConfigureParser(AppBuilder.this, new FileInputStream(file1));
//                AppConfigureParser acp = new AppConfigureParser(xmlData);
                appConfig = acp.parseSAX();

        	/* preapare cache data */
                if (sdAvailable) {
                    File file = null;
                    String fileName = "";
                    String cacheFileName = "";
                    ArrayList<String> fileNames = new ArrayList<String>();

/* check background */
                    fileName = md5(appConfig.getBackgroundImageUrl());
                    cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                    file = new File(cacheFileName);
                    if (file.exists()) {
                        appConfig.setBackgroundImageCache(cacheFileName);
                        appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                    }
                    fileNames.add(fileName);
                /* check buttons */
                    for (int i = 0; i < appConfig.getButtonsCount(); i++) {
                        WidgetUIButton button = appConfig.getButtonAtIndex(i);
                        fileName = md5(button.getImageSourceUrl());
                        cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                        file = new File(cacheFileName);
                        if (file.exists()) {
                            button.setImageSourceCache(cacheFileName);
                            button.setDownloadStatus(DownloadStatus.SUCCESS);
                            appConfig.setButtonAtIndex(i, button);
                        }
                        fileNames.add(fileName);
                    }

            /* check images */
                    for (int i = 0; i < appConfig.getImagesCount(); i++) {
                        WidgetUIImage image = appConfig.getImageAtIndex(i);
                        fileName = md5(image.getSourceUrl());
                        cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                        file = new File(cacheFileName);
                        if (file.exists()) {
                            image.setSourceCache(cacheFileName);
                            image.setDownloadStatus(DownloadStatus.SUCCESS);
                            appConfig.setImageAtIndex(i, image);
                        }
                        fileNames.add(fileName);
                    }

            /* check tabs */
                    for (int i = 0; i < appConfig.getTabsCount(); i++) {
                        WidgetUITab tab = appConfig.getTabAtIndex(i);
                        fileName = md5(tab.getIconCache());
                        cacheFileName = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + fileName;
                        file = new File(cacheFileName);
                        if (file.exists()) {
                            tab.setIconCache(cacheFileName);
                            tab.setDownloadStatus(DownloadStatus.SUCCESS);
                            appConfig.setTabAtIndex(i, tab);
                        }
                        fileNames.add(fileName);
                    }
            
            /* CLEAR old cache files */
                    File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/assets/");
                    File[] files = cache.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        String currentFile = files[i].getName();
                        boolean fl = false;
                        for (int j = 0; j < fileNames.size(); j++) {
                            if (currentFile.equals(fileNames.get(j))) {
                                fl = true;
                            }
                        }
                        if (!fl) {
                            files[i].delete();
                        }
                    }
                }
            }

            SharedPreferences sPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putLong(PREFERENCE_CONFIG_TIMESTAMP, timestamp);
            ed.commit();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /* GPS SERVICE METHODS */
    private void startGPSNotificationService() {
        try {//ErrorLogging

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            }

            boolean isRunning = false;
            String serviceName = this.getPackageName() + ".GPSNotification.GPSService";
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(service.service.getClassName())) {
                    isRunning = true;
                }
            }
            if (!isRunning) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("\"" + appConfig.getAppName() + "\" Would Like to Use Your Current Location?")
                        .setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        handler.sendEmptyMessage(GPS_NOTIFICATION_SERVICE_INTENT_START);
                    }
                }).setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        } catch (Exception e) {
        }
    }

    private void startService() {
        Intent intent = new Intent(this, GPSService.class);
        startService(intent);
    }

    private void stopGPSNotificationService() {
        String serviceName = this.getPackageName() + ".GPSNotification.GPSService";
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                Intent intent = new Intent(this, GPSService.class);
                this.stopService(intent);
            }
        }
    }
    
    /*  INTERFACE'S METHODS */

    public int LoadPluginFromActivity(Activity activity, String pluginName, String pluginPackage, String pluginURL, String pluginHash) {
        // 1. find plugin itself
        // 2. check hash
        // 3. try load
        // 4. download and install plugin
        // 5. try load

        return 0;
    }

    public void DownloadHelperCallbackStarted(DownloadHelper obj) {
        Toast.makeText(AppBuilder.this, "Started", Toast.LENGTH_LONG).show();
    }

    public void DownloadHelperCallbackSuccess(DownloadHelper obj) {
        Runnable action = new Runnable() {
            public void run() {

            }
        };
        Handler h = new Handler();
        h.postDelayed(action, 200);
        Toast.makeText(AppBuilder.this, "Finish", Toast.LENGTH_LONG).show();
    }

    public void DownloadHelperCallbackFailed(DownloadHelper obj, String errorString) {
        Runnable action = new Runnable() {
            public void run() {
            }
        };
        Handler h = new Handler();
        h.postDelayed(action, 200);
        Toast.makeText(AppBuilder.this, errorString, Toast.LENGTH_LONG).show();
    }

    /* SERVICE METHODS */
    private void listenInterfaceBuilder() {
        iterator++;
        if (iterator > 30) {
            //handler.sendEmptyMessage(this.INTERFACE_BUILDED_FAILED);
            handler.sendEmptyMessage(this.INTERFACE_BUILDED_SUCCESS);
        }

        int status = appConfig.getAllDownloadStatus();
        Log.i("LISTEN INTERFACE BUILDER", iterator + ": " + status);

        switch (status) {
            case -1: {
                handler.sendEmptyMessage(this.INTERFACE_BUILDED_FAILED);
            }
            break;
            case 0: {
                handler.sendEmptyMessageDelayed(this.LISTEN_INTERFACE_BUILDING, 3000);
            }
            break;
            case 1: {
                handler.sendEmptyMessage(this.INTERFACE_BUILDED_SUCCESS);
            }
            break;
        }
    }

    private void pushNotificationInit() {
        try {//LogError

            if (appConfig.getPushNotificationAccount().length() == 0) {
                Intent pushServiceIntent = new Intent(AppBuilder.this, PushNotificationService.class);
                pushServiceIntent.putExtra(PushNotificationService.EXTRA_COMMAND, PushNotificationService.COMMAND_START_POLLING);
                startService(pushServiceIntent);
                return;
            }else{
                PushNotificationService.stopPushPolling(this);
            }
            
           /* Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
// sets the app name in the intent
            registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
            registrationIntent.putExtra("sender", "mycuteproject");
            startService(registrationIntent);
            Log.d("", "");*/

        /* Push Notification Registration if It need */
            /*GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            String id = GCMRegistrar.getRegistrationId(this);
            if(id.length() == 0){
                GCMRegistrar.register(this, 
                        "akabotanick@mail.ru");//appConfig.getPushNotificationAccount());
                id = GCMRegistrar.getRegistrationId(this);
            }
            
            if(id.length() != 0){
                try{
                    pushRegistrationUrl += "&token="+id;	
                    URL url = new URL(pushRegistrationUrl);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str;
                    StringBuilder sb = new StringBuilder(); 
                    while ((str = in.readLine()) != null) {
                        sb.append(str);
                    }
                    in.close();
                }catch(Exception e){
                }
            }*/

            String id = C2DMessaging.getRegistrationId(this);
            if (id.length() == 0) {
                Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
                // sets the app name in the intent
                registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
                registrationIntent.putExtra("sender", appConfig.getPushNotificationAccount());
                //"511098409524");// "mycuteproject");
                startService(registrationIntent);
                Log.d("", "");
                //C2DMessaging.register(this, appConfig.getPushNotificationAccount());
            } else {
                try {
                    pushRegistrationUrl += "&token=" + id;
                    URL url = new URL(pushRegistrationUrl);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str;
                    StringBuilder sb = new StringBuilder();
                    while ((str = in.readLine()) != null) {
                        sb.append(str);
                    }
                    in.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     */
    private void putDataInCache() {
        File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.data");
        if (cache.exists())
            cache.delete();

        try {
            cache.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
            oos.writeObject(appConfig);
            oos.close();
            Log.i("CACHE APP CONF", "success");
        } catch (Exception e) {
            Log.w("CACHE APP CONF", e);
            cache.delete();
        }
    }


    private String readFileToString(String pathToFile) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(pathToFile)));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
        } catch (Exception e) {
        }
        return sb.toString();
    }


    private void writeStringToFile(String pathToFile, String str) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(pathToFile)));
            bw.write(str);
            bw.close();
        } catch (Exception e) {
        }
    }


    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.w("WebPlugin CREATE MD5", e);
        }

        return null;
    }


    /**
     * Executing appropriate module, which connected to specified button
     */
    private void launchWidgetWithOrder(int order) {
        try {//ErrorLogging

            // TODO для того чтобы пользовать эту функцию для открытия модулей через пуш, нужно
            // todo 1. Обновлять конфигурацию из кеша, т.к. она будет лежать обновленная по событию прихода пуша
            // todo 2. Перерисовывать весь интерфейс, чтобы обновить индекса запуска модулей(order) у кнопок

            if (order == -1) {
                finish();
                return;
            }

//            if (dontLaunchModule) {
//                return;
//            } else {
//                dontLaunchModule = true;
//            }

            /**
             * Initialization of class responsible for start of plugins
             * */
            final PluginManager pm = PluginManager.getManager();
            pm.setFirstStart(firstPluginStart);
            firstPluginStart = false;

            try {
                // getting widget according to order value
                final Widget widget = appConfig.getWidgetAtIndex(order);
                widget.setCachePath(com.appbuilder.sdk.android.Statics.cachePath);
                widget.setHaveAdvertisement(appConfig.getShowLink());

                needToClose = widget.getPluginName().equals("FeedbackPlugin");

                // if such module exists -> start launch
                if (widget.getPluginPackage().length() > 0) {

                    startedPluginName = widget.getTitle();
                    if (TextUtils.isEmpty(startedPluginName)) {
                        startedPluginName = widget.getPluginName();
                    }

                    if(isShown) {
                        Animation animHideMenuStartActivity = new TranslateAnimation(0, Double.valueOf(screenWidth * 0.85).intValue(), 0, 0);
                        animHideMenuStartActivity.setInterpolator(new SmoothInterpolator());
                        animHideMenuStartActivity.setDuration(400);
                        animHideMenuStartActivity.setFillEnabled(true);
                        animHideMenuStartActivity.setAnimationListener(
                                new Animation.AnimationListener() {

                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Double.valueOf(screenWidth * 1.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
                                        params.gravity = Gravity.LEFT;
                                        params.setMargins(0, 0, 0, 0);
                                        rootContainer.setLayoutParams(params);

                                        pm.loadPlugin(AppBuilder.this, AppBuilder.this, widget, appConfig.getAppAdv(), false, appConfig.getmWidgets(), appConfig);
                                    }
                                }
                        );

                        applySidebarAnimation(animHideMenuStartActivity);
                        isShown = false;
                    } else {
                        pm.loadPlugin(AppBuilder.this, AppBuilder.this, widget, appConfig.getAppAdv(), false, appConfig.getmWidgets(), appConfig);
                    }
                } else {
                    dontLaunchModule = false;
                    Toast.makeText(AppBuilder.this, "The functionality is not available.", Toast.LENGTH_LONG).show();
                }
            } catch (RuntimeException e) {
                Log.w("PLUGINDATA", e);
            }

        } catch (Exception e) {
        }
    }

    private int DpToPixel(int dp) {
        return (int) ((getResources().getDisplayMetrics().density * (float) dp) + 0.5f);
    }

    private int PixelToDp(int pixel) {
        return (int) (pixel / getResources().getDisplayMetrics().density);
    }

    private void drawInterface() {
        try {
            LinearLayout root = (LinearLayout) findViewById(R.id.root);
            root.removeAllViews();

            rootScroller = new SideBarComponent(AppBuilder.this);
            root.addView(rootScroller, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            rootFrameLayout = new FrameLayout(AppBuilder.this);
            rootFrameLayout.setFocusableInTouchMode(true);
            rootFrameLayout.setClickable(true);
            rootFrameLayout.removeAllViews();

            rootScroller.addView(rootFrameLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            rootContainer = new SwipeLinearLayout(this, appConfig.isShowSidebar());
            rootContainer.setOrientation(LinearLayout.HORIZONTAL);
            rootContainer.disableSwipe();

            menuContainer = new LinearLayout(this);
            menuContainer.setBackgroundColor(Color.parseColor(WIDGET_HOLDER_BACKGROUND));
            menuContainer.setOrientation(LinearLayout.VERTICAL);

            widgetList = new ListView(this);
            widgetList.setBackgroundColor(Color.parseColor(WIDGET_HOLDER_BACKGROUND));
            widgetList.setCacheColorHint(Color.parseColor(WIDGET_HOLDER_BACKGROUND));
            widgetList.setVerticalScrollBarEnabled(false);
            widgetList.setDivider(null);
            widgetList.setDividerHeight(0);

            addMasterappActions();

            int onlyMasterappItems = actualWidgetList.size();

            for(WidgetUISidebarItem widgetUISidebarItem : appConfig.getSidebarItems()) {
                final Widget widget = appConfig.getWidgetWithOrder(widgetUISidebarItem.getOrder());
                widget.setAddToSidebar(true);
                widget.setLabel(widgetUISidebarItem.getLabel());
                actualWidgetList.add(widget);
                com.appbuilder.sdk.android.Statics.sidebarClickListeners.put(widgetUISidebarItem.getOrder(), new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        launchWidgetWithOrder(widget.getOrder());
                    }
                });
            }

            if(onlyMasterappItems == actualWidgetList.size())
                actualWidgetList.remove(onlyMasterappItems - 1);

            widgetList.setAdapter(new SidebarAdapter(this, actualWidgetList, -1, this));
            widgetList.setOnItemClickListener(this);

            menuContainer.addView(widgetList, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
                topMargin = Float.valueOf(getResources().getDisplayMetrics().density * 15).intValue();
            }});

            userContainer = new LinearLayout(this);
            userContainer.setOrientation(LinearLayout.VERTICAL);
            userContainer.setBackgroundColor(Color.parseColor("#c0b9ff"));

            rootContainer.addView(userContainer, new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT));
            rootContainer.addView(menuContainer, new LinearLayout.LayoutParams(Double.valueOf(screenWidth * 0.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT));

            rootFrameLayout.addView(rootContainer, new FrameLayout.LayoutParams((int) (screenWidth + screenWidth * 0.85), ViewGroup.LayoutParams.MATCH_PARENT) {{
                gravity = Gravity.LEFT;
                setMargins(0, 0, 0, 0);
            }});

            dialogHolder = new LinearLayout(AppBuilder.this);
            dialogHolder.setVisibility(View.INVISIBLE);
            FrameLayout.LayoutParams fparams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            fparams.gravity = Gravity.CENTER;
            dialogHolder.setLayoutParams(fparams);
            rootFrameLayout.addView(dialogHolder);

            prepareAnimationObjects();

            swipeInterface = new OnSwipeInterface() {
                @Override
                public void onSwipeLeft() {
                    if (appConfig.isShowSidebar() && !isShown) {
                        showSidebar();
                    }
                }

                @Override
                public void onSwipeRight() {
                    if (appConfig.isShowSidebar() && isShown) {
                        hideSidebar();
                    }
                }

                @Override
                public void onSwipeTop() {
                }

                @Override
                public void onSwipeBottom() {
                }

                @Override
                public boolean onTouchEvent(float x) {
                    return false;
                }
            };


            if (appConfig.isShowSidebar())
                rootContainer.setOnSwipeEvents(swipeInterface);

            RelativeLayout coreTemplate = (RelativeLayout) LayoutInflater.from(AppBuilder.this).inflate(R.layout.main, null);
            RelativeLayout rootLayout = (RelativeLayout) coreTemplate.findViewById(R.id.rootLayout);
            rootLayout.removeAllViews();
            setBackgroundUseConfig(appConfig, rootLayout, coreTemplate);
            coreTemplate.findViewById(R.id.hamburger_main).setVisibility(appConfig.isShowSidebar() ? View.VISIBLE : View.INVISIBLE);
            coreTemplate.findViewById(R.id.hamburger_main).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inverseSidebarState();
                }
            });
            userContainer.addView(coreTemplate);

            Display dpy = getWindowManager().getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            dpy.getMetrics(dm);
            screenCoef = (float) dm.widthPixels / ((float) 320 * dm.scaledDensity);

            if (appConfig.getShowLink())
                drawHyperlink(appConfig, rootLayout);

            for (int i = 0; i < appConfig.getControlsCount(); i++) {
                AppConfigureItem item = appConfig.getControlAtIndex(i);

                if (item instanceof WidgetUILabel) {
                    WidgetUILabel label = (WidgetUILabel) appConfig.getControlAtIndex(i);
                    drawLabel(label, rootLayout);
                } else if (item instanceof WidgetUIButton) {
                    WidgetUIButton button = (WidgetUIButton) appConfig.getControlAtIndex(i);
                    drawButton(button, rootLayout);
                } else if (item instanceof WidgetUIImage) {
                    WidgetUIImage image = (WidgetUIImage) appConfig.getControlAtIndex(i);
                    drawImage(image, rootLayout);
                }
            }

            if (appConfig.needShowMenu()) {
                drawTabsUseConfig(appConfig, rootLayout);
                Log.d(TAG, "drawTabsUseConfig() time = " + (new Date().getTime() - startTime));
            }

            createPushDialog();
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    private void drawHyperlink(AppConfigure appConfig, RelativeLayout rootLayout) {

        SpannableString ss = new SpannableString("Created using iBuildApp.com.\nCreate Your Own App");
        ss.setSpan(new StyleSpan(Typeface.NORMAL), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        URLSpan urlSpan = new URLSpan("http://ibuildapp.com");
        urlSpan.getUnderlying().updateDrawState(new TextPaint());
        ForegroundColorSpan fcs = new ForegroundColorSpan(Color.WHITE);
        ss.setSpan(fcs, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView view = new TextView(this);
        view.setText(ss);

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String _url = "http://ibuildapp.com";
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse(_url));
                startActivity(webIntent);
            }

        });
        view.setShadowLayer(0.1f, 2f, 2f, Color.BLACK);
        view.setGravity(Gravity.CENTER_HORIZONTAL);

        Display dpy = getWindowManager().getDefaultDisplay();
        int tHeight = dpy.getHeight();
        int tWidth = dpy.getWidth();

        if (tWidth > tHeight) {
            int tmp = tWidth;
            tWidth = tHeight;
            tHeight = tmp;
        }

        final int dpi_w = 180;
        int dpw = PixelToDp(tWidth);
        int dph = PixelToDp(tHeight);

        if /*(dpy.getOrientation() == Surface.ROTATION_90 || dpy.getOrientation() == Surface.ROTATION_270)*/
                (tWidth > tHeight) {
            int tmp = dpw;
            dpw = dph;
            dph = tmp;
        }

        int width = DpToPixel(dpi_w);
        int height;

        height = DpToPixel(35);

        int top = dph - (height + PixelToDp(0));
        int left = (int) ((dpw - dpi_w) / 2);

        final int tabHeightDp = 75;
        if (appConfig.needShowMenu()) {
            top = top - tabHeightDp - 5;
        } else {
            top = top - 18;
        }

        Log.d("density", String.valueOf(getResources().getDisplayMetrics().density));
        Log.d("display width", String.valueOf(dpy.getWidth()));
        Log.d("display height", String.valueOf(dpy.getHeight()));

        Log.d("dpw", String.valueOf(dpw));
        Log.d("dph", String.valueOf(dph));
        Log.d("width", String.valueOf(width));
        Log.d("height", String.valueOf(height));
        Log.d("top", String.valueOf(top));
        Log.d("left", String.valueOf(left));

        width = getResources().getDisplayMetrics().widthPixels;

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        lp.topMargin = DpToPixel(top);
        lp.leftMargin = 0;//DpToPixel(left);
        rootLayout.addView(view, lp);
    }


    private void drawImage(final WidgetUIImage image, RelativeLayout rootLayout) {

        final ImageView view = new ImageView(this);
        final Rect viewRect = new Rect((int) (image.getLeft() * screenCoef),
                (int) (image.getTop() * screenCoef),
                (int) ((image.getLeft() + image.getWidth()) * screenCoef),
                (int) ((image.getTop() + image.getHeight()) * screenCoef));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
        lp.topMargin = DpToPixel(viewRect.top);
        lp.leftMargin = DpToPixel(viewRect.left);

        boolean useCache = false;
        if (image.getSourceCache().length() > 0) {
            File file = new File(image.getSourceCache());
            if (file.exists()) {
                useCache = true;
            } else {
                image.setSourceCache("");
            }
        }
        if (useCache) {
            try {
                System.gc();
                Drawable drawable = null;
                try {
                    System.gc();
                    drawable = (Drawable.createFromPath(image.getSourceCache()));
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError e) {
                    Log.d("", "");
                    System.gc();
                    try {
                        drawable = (Drawable.createFromPath(image.getSourceCache()));
                    } catch (Exception ex) {
                        Log.d("", "");
                    } catch (OutOfMemoryError ex) {
                        Log.e("decodeImageFile", "OutOfMemoryError");
                    }
                }

                drawable = drawable.mutate();
                Bitmap srcBitmap = ((BitmapDrawable) drawable).getBitmap();
                Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
                BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                view.setBackgroundDrawable(bmpDrawable);

                image.setDownloadStatus(DownloadStatus.SUCCESS);
                Log.d("Draw image from cache", "Success");
            } catch (Exception e) {
                Log.w("Draw image from cache", "");
                try {
                    File file = new File(image.getSourceCache());
                    file.delete();
                    drawImage(image, rootLayout);
                } catch (Exception ex) {
                }
            }
        } else {
            if (Tools.checkNetwork(AppBuilder.this) > 0) {
                if (!sdAvailable) {
                    try {
                        URL url = new URL(image.getSourceUrl());
                        URLConnection urlConn = url.openConnection();
                        HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                        httpConn.connect();
                        //Drawable drawable = Drawable.createFromStream();
                        Bitmap srcBitmap = BitmapFactory.decodeStream(httpConn.getInputStream());
                        Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
                        BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                        view.setBackgroundDrawable(bmpDrawable);
                        image.setDownloadStatus(DownloadStatus.SUCCESS);
                    } catch (IOException iOEx) {
                        Log.d("", "");
                    }
                } else {
                    String imageUrl = image.getSourceUrl();
                    if (imageUrl.length() > 0) {
                        final String imageCache = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + md5(imageUrl);
                        image.setSourceCache(imageCache);
                        final DownloadHelper dh = new DownloadHelper((Activity) this, imageUrl, imageCache, null, false, true);
                        dh.setStartedRunnable(
                                new Runnable() {
                                    public void run() {
                                        Log.d("Download widget image", "Start");
                                    }
                                }
                        );
                        dh.setFailedRunnable(
                                new Runnable() {
                                    public void run() {
                                        image.setDownloadStatus(DownloadStatus.FAILED);
                                        Log.w("Download widget image", "Failed: " + dh.getErrorString());
                                    }
                                }
                        );
                        dh.setSuccessRunnable(
                                new Runnable() {
                                    public void run() {
                                        try {
                                            System.gc();
                                            Drawable drawable = null;
                                            try {
                                                System.gc();
                                                drawable = (Drawable.createFromPath(imageCache));
                                            } catch (Exception ex) {
                                                Log.d("", "");
                                            } catch (OutOfMemoryError e) {
                                                Log.d("", "");
                                                System.gc();
                                                try {
                                                    drawable = (Drawable.createFromPath(imageCache));
                                                } catch (Exception ex) {
                                                    Log.d("", "");
                                                } catch (OutOfMemoryError ex) {
                                                    Log.e("decodeImageFile", "OutOfMemoryError");
                                                }
                                            }

                                            drawable = drawable.mutate();
                                            Bitmap srcBitmap = ((BitmapDrawable) drawable).getBitmap();
                                            Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
                                            BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                                            view.setBackgroundDrawable(bmpDrawable);

                                            Log.d("Draw widget image", "Success");
                                        } catch (Exception e) {
                                            Log.w("Draw widget image", "");
                                        }

                                        Log.d("Download widget image", "Success");
                                        image.setDownloadStatus(DownloadStatus.SUCCESS);
                                    }
                                });

                        dh.start();
                    } else {
                        image.setDownloadStatus(DownloadStatus.SUCCESS);
                    }
                }
            } else {
                if (image.getmImageData() != null) {
                    try {
                        Bitmap srcBitmap = BitmapFactory.decodeStream
                                (new ByteArrayInputStream(Base64.decode(image.getmImageData())));
                        Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
                        BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                        view.setBackgroundDrawable(bmpDrawable);
                        image.setDownloadStatus(DownloadStatus.SUCCESS);
                    } catch (Exception ex) {
                        Log.e("", "");
                    }
                }
            }
        }
        rootLayout.addView(view, lp);
    }


    /**
     * rendering template button
     *
     * @param button     - class with source parametrs of button
     * @param rootLayout - parent root on what button will be drawen
     */
    private void drawButton(final WidgetUIButton button, final RelativeLayout rootLayout) {
        final Button view = new Button(this);

        Display dpy = getWindowManager().getDefaultDisplay();
        int h = dpy.getHeight() - 20;
        int w = dpy.getWidth();
        int hc = (int) (h * screenCoef);
        int bb = (int) ((button.getTop() + button.getHeight()) * screenCoef);
        int r = hc - bb;

        final Rect viewRect;

        if (((r > 14) && appConfig.getShowLink()) || !appConfig.getShowLink()) {

		/*final Rect*/
            viewRect = new Rect((int) (button.getLeft() * screenCoef),
                    (int) (button.getTop() * screenCoef),
                    (int) ((button.getLeft() + button.getWidth()) * screenCoef),
                    (int) ((button.getTop() + button.getHeight()) * screenCoef));

        } else {

            viewRect = new Rect((int) (button.getLeft() * screenCoef),
                    (int) (button.getTop() * screenCoef),
                    (int) ((button.getLeft() + button.getWidth()) * screenCoef),
                    (int) ((button.getTop() + button.getHeight() - 18) * screenCoef));

        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
        lp.topMargin = DpToPixel(viewRect.top);
        lp.leftMargin = DpToPixel(viewRect.left);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            view.setAllCaps(false);

        view.setText(button.getTitle());
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) DpToPixel((int) (button.getFontSize() * screenCoef)));
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setPadding(0, 0, 0, 0);

        String align = button.getAlign();
        String valign = button.getVAlign();

        int paddingX = DpToPixel((int) (button.getPaddingX() * screenCoef));
        int paddingY = DpToPixel((int) (button.getPaddingY() * screenCoef));

        int vGravity = Gravity.CENTER_VERTICAL;
        int hGravity = Gravity.CENTER_HORIZONTAL;

        if (align.equalsIgnoreCase("left")) {
            //view.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            hGravity = Gravity.LEFT;
        } else if (align.equalsIgnoreCase("right")) {
            //view.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            hGravity = Gravity.RIGHT;
        }

        if (valign.equalsIgnoreCase("top")) {
            vGravity = Gravity.TOP;
        } else if (valign.equalsIgnoreCase("bottom")) {
            vGravity = Gravity.BOTTOM;
        }

        int gravity = vGravity | hGravity;

        view.setGravity(gravity);

        float kostyllo = DpToPixel((int) ((float) button.getFontSize() / 5f * screenCoef));

        switch (gravity) {
            case Gravity.LEFT | Gravity.TOP:
                view.setPadding(paddingX, (int) (paddingY - kostyllo), 0, 0);
                break;
            case Gravity.LEFT | Gravity.CENTER_VERTICAL:
                view.setPadding(paddingX, 0, 0, 0);
                break;
            case Gravity.LEFT | Gravity.BOTTOM:
                view.setPadding(paddingX, 0, 0, paddingY);
                break;
            case Gravity.CENTER_HORIZONTAL | Gravity.TOP:
                view.setPadding(0, (int) (paddingY - kostyllo), 0, 0);
                break;
            case Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL:
                view.setPadding(0, 0, 0, 0);
                break;
            case Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM:
                view.setPadding(0, 0, 0, paddingY);
                break;
            case Gravity.RIGHT | Gravity.TOP:
                view.setPadding(0, (int) (paddingY - kostyllo), paddingX, 0);
                break;
            case Gravity.RIGHT | Gravity.CENTER_VERTICAL:
                view.setPadding(0, 0, paddingX, 0);
                break;
            case Gravity.RIGHT | Gravity.BOTTOM:
                view.setPadding(0, 0, paddingX, paddingY);
                break;
        }

        String style = button.getStyle();
        if (style.equalsIgnoreCase("bold")) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else if (style.equalsIgnoreCase("italic")) {
            view.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        }

        try {
            view.setTextColor(Color.parseColor(button.getColor().toUpperCase()));
        } catch (IllegalArgumentException e) {
        }

        final Widget tmpWidget = appConfig.getWidgetAtIndex(button.getOrder());

        boolean useCache = false;
        if ( !TextUtils.isEmpty(button.getImageSourceCache()) ) {
            File file = new File(button.getImageSourceCache());
            if (file.exists()) {
                useCache = true;
            } else {
                button.setImageSourceCache("");
            }
        }

        if (useCache) {
            try {
                System.gc();
                Drawable drawable = null;
                try {
                    System.gc();
                    drawable = (Drawable.createFromPath(button.getImageSourceCache()));
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError e) {
                    Log.d("", "");
                    System.gc();
                    try {
                        drawable = (Drawable.createFromPath(button.getImageSourceCache()));
                    } catch (Exception ex) {
                        Log.d("", "");
                    } catch (OutOfMemoryError ex) {
                        Log.e("decodeImageFile", "OutOfMemoryError");
                    }
                }

                drawable = drawable.mutate();
                Bitmap srcBitmap = ((BitmapDrawable) drawable).getBitmap();
                Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()),
                        DpToPixel(viewRect.height()));
                BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                view.setBackgroundDrawable(bmpDrawable);

                Log.d("Draw button", "success");
                button.setDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                Log.w("Draw button", e);
                try {
                    File file = new File(button.getImageSourceCache());
                    file.delete();
                    drawButton(button, rootLayout);
                } catch (Exception ex) {
                    Log.d("", "");
                }
            }
        } else {
            if (Tools.checkNetwork(AppBuilder.this) > 0) {
                if (sdAvailable) {
                    String imageUrl = button.getImageSourceUrl();
                    if (imageUrl.length() > 0) {
                        final String imageCache = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + md5(imageUrl);
                        button.setImageSourceCache(imageCache);
                        final DownloadHelper dh = new DownloadHelper((Activity) this, imageUrl, imageCache, null, false, true);
                        dh.setStartedRunnable(new Runnable() {
                            public void run() {
                                Log.d("Download button image", "Start");
                            }
                        }
                        );

                        dh.setFailedRunnable(
                                new Runnable() {
                                    public void run() {
                                        Log.d("Download button image", "Failed: " + dh.getErrorString());
                                        button.setDownloadStatus(DownloadStatus.FAILED);
                                    }
                                }
                        );

                        dh.setSuccessRunnable(
                                new Runnable() {
                                    public void run() {
                                        try {
                                            System.gc();
                                            Drawable drawable = null;
                                            try {
                                                System.gc();
                                                drawable = (Drawable.createFromPath(imageCache));
                                            } catch (Exception ex) {
                                                Log.d("", "");
                                            } catch (OutOfMemoryError e) {
                                                Log.d("", "");
                                                System.gc();
                                                try {
                                                    drawable = (Drawable.createFromPath(imageCache));
                                                } catch (Exception ex) {
                                                    Log.d("", "");
                                                } catch (OutOfMemoryError ex) {
                                                    Log.e("decodeImageFile", "OutOfMemoryError");
                                                }
                                            }

                                            drawable = drawable.mutate();
                                            Bitmap srcBitmap = ((BitmapDrawable) drawable).getBitmap();
                                            Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()),
                                                    DpToPixel(viewRect.height()));
                                            BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                                            view.setBackgroundDrawable(bmpDrawable);
                                        } catch (Exception e) {
                                        }
                                        button.setDownloadStatus(DownloadStatus.SUCCESS);

                                        Log.d(TAG, "setSuccessRunnable() time = " + (new Date().getTime() - startTime));
                                    }
                                }
                        );

                        dh.start();
                    } else {
                        button.setDownloadStatus(DownloadStatus.SUCCESS);
                    }
                } else {
                    try {
                        URL url = new URL(button.getImageSourceUrl());
                        URLConnection urlConn = url.openConnection();

                        HttpURLConnection httpConn = (HttpURLConnection) urlConn;

                        httpConn.connect();

                        //Drawable drawable = Drawable.createFromStream();
                        Bitmap srcBitmap = BitmapFactory.decodeStream(httpConn.getInputStream());
                        Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
                        BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                        view.setBackgroundDrawable(bmpDrawable);
                        button.setDownloadStatus(DownloadStatus.SUCCESS);
                    } catch (IOException iOEx) {
                        Log.d("", "");
                    } catch (NullPointerException nPEx) {
                        Log.d("", "");
                    }
                }
            } else {
                if (button.getmImageData() != null) {
                    try {
                        Bitmap srcBitmap = BitmapFactory.decodeStream
                                (new ByteArrayInputStream(Base64.decode(button.getmImageData())));
                        Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
                        BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                        view.setBackgroundDrawable(bmpDrawable);
                        button.setDownloadStatus(DownloadStatus.SUCCESS);
                    } catch (Exception ex) {
                        Log.e("", "");
                    }
                }
            }
        }

        rootLayout.addView(view, lp);

        View tmpView = null;

        if(tmpWidget.isUpdated() && appConfig.isUpdateContentPushEnabled()){
            float dispCoef = getResources().getDisplayMetrics().widthPixels / 320f;

            tmpView = drawBadge(rootLayout,
                    lp.topMargin + (int)(5 * dispCoef),
                    lp.leftMargin + lp.width - (int)(15 * dispCoef));
        }

        final View badgeView = tmpView;

        // adding onClickHandler
        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * Change background color of button to Gray when
                         * it's pushed
                         * */
                        Drawable d = v.getBackground();
                        d.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        v.setBackgroundDrawable(d);

                        tmpWidget.setUpdated(false);

                        if (badgeView != null) rootLayout.removeView(badgeView);

                        /**
                         * Running appropriate module with getOrder() number
                         * after 200ms seconds
                         * */
                        Runnable action = new Runnable() {
                            public void run() {
                                AppBuilder.this.launchWidgetWithOrder(button.getOrder());
                            }
                        };
                        Handler h = new Handler();
                        h.postDelayed(action, /*200*/10);
                    }
                }
        );
    }

    private View drawBadge(RelativeLayout rootLayout, int centerX, int centerY) {
        if(com.appbuilder.sdk.android.Statics.firstStart) return null;

        ImageView badge = new ImageView(this);
        badge.setScaleType(ImageView.ScaleType.FIT_XY);
        badge.setImageResource(R.drawable.new_content);

        float dispCoef = (float)getResources().getDisplayMetrics().widthPixels / 320;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(20 * dispCoef), (int)(20 * dispCoef));
        params.leftMargin = centerY - (int)(10f * dispCoef);
        params.topMargin = centerX - (int)(10f * dispCoef);

        rootLayout.addView(badge,0, params);
        rootLayout.bringChildToFront(badge);
        badge.bringToFront();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            badge.setElevation(100);
        return badge;
    }

    private void drawLabel(WidgetUILabel label, RelativeLayout rootLayout) {
        TextView view = new TextView(this);

        final Rect viewRect = new Rect((int) (label.getLeft() * screenCoef),
                (int) (label.getTop() * screenCoef),
                (int) ((label.getLeft() + label.getWidth()) * screenCoef),
                (int) ((label.getTop() + label.getHeight()) * screenCoef));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
        lp.topMargin = DpToPixel(viewRect.top);
        lp.leftMargin = DpToPixel(viewRect.left);

        view.setText(label.getTitle());
        view.setTextSize((float) label.getFontSize());
        view.setGravity(Gravity.CENTER_HORIZONTAL);

        String align = label.getAlign();
        if (align.equalsIgnoreCase("left")) {
            view.setGravity(Gravity.LEFT);
        } else if (align.equalsIgnoreCase("right")) {
            view.setGravity(Gravity.RIGHT);
        } else {
            view.setGravity(Gravity.CENTER_HORIZONTAL);
        }

                /*view.setLines(1);
                
                float textSize = view.getTextSize();
                while (getTextWidth(view) > label.getWidth()){
                    int s = getTextWidth(view);
                    textSize = textSize - 1;
                    view.setTextSize(textSize);
                }*/
                
                /*if(getTextWidth(view) > label.getWidth()){
                    for(int i = 4; i > 1; i--){
                        String shortText = label.getTitle().substring(0, i - 1)
                                + ".." + label.getTitle().substring(
                                label.getTitle().length() - i, 
                                label.getTitle().length() - 1);
                        view.setText(shortText);
                        if(getTextWidth(view) < label.getWidth()){
                            break;
                        }
                    }
                }*/

        String style = label.getStyle();
        if (style.equalsIgnoreCase("bold")) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else if (style.equalsIgnoreCase("italic")) {
            view.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        }

		/* set text color */
        try {
            view.setTextColor(Color.parseColor(label.getColor().toUpperCase()));
        } catch (IllegalArgumentException e) {
        }

        rootLayout.addView(view, lp);
        label.setDownloadStatus(DownloadStatus.SUCCESS);
    }

    private int getTextWidth(TextView textView) {
        Paint paint = new Paint();
        Rect bounds = new Rect();

        //int text_height = 0;
        int text_width = 0;

        paint.setTypeface(Typeface.DEFAULT);// your preference here
        paint.setTextSize(textView.getTextSize());// have this the same as your text size

        String text = (String) textView.getText();
        text_width = (int) paint.measureText(text);

        //paint.getTextBounds(text, 0, text.length(), bounds);

        //text_height =  bounds.height();
        //text_width =  bounds.width();
        return text_width;
    }


    private void drawTabsUseConfig(final AppConfigure appConfig, final RelativeLayout rootLayout) {
        final int tabHeightDp = 75;
        final int tabIconSquareDp = 50;
        final int tabLabelHeightDp = 20;

        Display dpy = getWindowManager().getDefaultDisplay();
        //final int dpi_w = 180;
        int dpw = PixelToDp(dpy.getWidth());
        int dph = PixelToDp(dpy.getHeight());

        if /*(dpy.getOrientation() == Surface.ROTATION_90 || dpy.getOrientation() == Surface.ROTATION_270)*/
                (dpw > dph) {
            int tmp = dpw;
            dpw = dph;
            dph = tmp;
        }

        int tabCount = appConfig.getTabsCount();
        int tabWidthDp = dpw / tabCount;
        for (int i = 0; i < tabCount; i++) {
            WidgetUITab tab = appConfig.getTabAtIndex(i);
            try {
                int padding = 0;
                if (i == tabCount - 1) {
                    padding = 10;
                }
                Rect tabRect = new Rect(tabWidthDp * i, dph - tabHeightDp, tabWidthDp * i + tabWidthDp /*+ padding*/, dph);
                drawTabInRect(tab, rootLayout, tabRect);
            } catch (Exception e) {
                try {
                    tab.setDownloadStatus(DownloadStatus.FAILED);
                } catch (Exception ex) {
                }
            }
        }
    }

    private void drawTabInRect(final WidgetUITab tab, final RelativeLayout rootLayout, final Rect viewRect) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.menu_item, null);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DpToPixel(viewRect.width()), DpToPixel(viewRect.height()));
        lp.topMargin = DpToPixel(viewRect.top);
        lp.leftMargin = DpToPixel(viewRect.left);

        final int order = tab.getOrder();

        final TextView text = (TextView) view.findViewById(R.id.core_tab_label);
        text.setText(tab.getLabel());

        // START Brazhnik 30.10.2013
        // TabBar customization
//        StateListDrawable mIcon = new StateListDrawable();
//        ShapeDrawable normalState = new ShapeDrawable(new RectShape());
//        normalState.getPaint().setColor(appConfig.getTabBarDesign().color);
//        ShapeDrawable pressedState = new ShapeDrawable(new RectShape());
//        pressedState.getPaint().setColor(appConfig.getTabBarDesign().itemDesign.selectedColor);
//        mIcon.addState(new int[] { android.R.attr.state_pressed }, pressedState );
//        mIcon.addState(new int[] { android.R.attr.state_selected }, pressedState );
//        mIcon.addState(new int[] { }, normalState );

        //view.setBackgroundDrawable(mIcon);
        text.setTextColor(appConfig.getTabBarDesign().itemDesign.textColor);
        text.setLines(1);     // ?? ???????????? ????
        text.setTextSize(10); // ?? ???????????? ????
        if (appConfig.getTabBarDesign().itemDesign.textAlignment.compareTo("left") == 0)
            text.setGravity(Gravity.LEFT);
        else if (appConfig.getTabBarDesign().itemDesign.textAlignment.compareTo("center") == 0)
            text.setGravity(Gravity.CENTER_HORIZONTAL);
        else if (appConfig.getTabBarDesign().itemDesign.textAlignment.compareTo("right") == 0)
            text.setGravity(Gravity.RIGHT);
        // Brazhnik 30.10.2013 END

        final ImageView icon = (ImageView) view.findViewById(R.id.core_tab_icon);
        icon.setImageBitmap(null);
        icon.setBackgroundColor(Color.TRANSPARENT);

        boolean useCache = false;
        if (tab.getIconCache().length() > 0) {
            File file = new File(tab.getIconCache());
            if (file.exists()) {
                useCache = true;
            } else {
                tab.setIconCache("");
            }
        }

        if (useCache) {
            try {
                System.gc();
                Drawable drawable = null;
                try {
                    System.gc();
                    drawable = (Drawable.createFromPath(tab.getIconCache()));
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError e) {
                    Log.d("", "");
                    System.gc();
                    try {
                        drawable = (Drawable.createFromPath(tab.getIconCache()));
                    } catch (Exception ex) {
                        Log.d("", "");
                    } catch (OutOfMemoryError ex) {
                        Log.e("decodeImageFile", "OutOfMemoryError");
                    }
                }

                drawable = drawable.mutate();
                Bitmap srcBitmap = ((BitmapDrawable) drawable).getBitmap();
                Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(/*viewRect.width()) */50), DpToPixel(/*viewRect.height()*/ 50));
                BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);

                if (icon != null) {
                    icon.setBackgroundDrawable(bmpDrawable);
                }

                tab.setDownloadStatus(DownloadStatus.SUCCESS);
                Log.d("Draw tab image", "Success");
            } catch (Exception e) {
                Log.w("Draw tab image", "");
                try {
                    File file = new File(tab.getIconCache());
                    file.delete();
                    drawTabInRect(tab, rootLayout, viewRect);
                } catch (Exception ex) {
                }
            }
        } else {
            if (Tools.checkNetwork(AppBuilder.this) > 0) {
                if (sdAvailable) {
                    String iconUrl = tab.getIconUrl();
                    if (iconUrl.length() > 0) {
                        final String iconCache = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + md5(iconUrl);
                        tab.setIconCache(iconCache);
                        final DownloadHelper dh = new DownloadHelper((Activity) this, iconUrl, iconCache, null, false, true);

                        dh.setStartedRunnable(new Runnable() {
                            public void run() {
                                Log.d("Download tab image", "Start");
                            }
                        });

                        dh.setFailedRunnable(new Runnable() {
                            public void run() {
                                Log.d("Download tab image", "Failed: " + dh.getErrorString());
                                tab.setDownloadStatus(DownloadStatus.FAILED);
                            }
                        });

                        dh.setSuccessRunnable(new Runnable() {
                            public void run() {
                                try {
                                    System.gc();
                                    Drawable drawable = null;
                                    try {
                                        System.gc();
                                        drawable = Drawable.createFromPath(iconCache);
                                    } catch (Exception ex) {
                                        Log.d("", "");
                                    } catch (OutOfMemoryError e) {
                                        Log.d("", "");
                                        System.gc();
                                        try {
                                            drawable = Drawable.createFromPath(iconCache);
                                        } catch (Exception ex) {
                                            Log.d("", "");
                                        } catch (OutOfMemoryError ex) {
                                            Log.e("decodeImageFile", "OutOfMemoryError");
                                        }
                                    }

                                    drawable = drawable.mutate();
                                    Bitmap srcBitmap = ((BitmapDrawable) drawable).getBitmap();
                                    Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(/*viewRect.width()) */50), DpToPixel(/*viewRect.height()*/ 50));
                                    BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);

                                    if (icon != null) {
                                        icon.setBackgroundDrawable(bmpDrawable);
                                    }
                                    Log.d("Download tab image", "Success");
                                } catch (Exception e) {
                                    Log.w("Download tab image", "");
                                }
                                tab.setDownloadStatus(DownloadStatus.SUCCESS);
                            }
                        });

                        dh.start();
                    } else {
                        tab.setDownloadStatus(DownloadStatus.SUCCESS);
                    }
                } else {
                    try {
                        URL url = new URL(tab.getIconUrl());
                        URLConnection urlConn = url.openConnection();

                        HttpURLConnection httpConn = (HttpURLConnection) urlConn;

                        httpConn.connect();

                        //Drawable drawable = Drawable.createFromStream();
                        Bitmap srcBitmap = BitmapFactory.decodeStream(httpConn.getInputStream());
                        Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(/*viewRect.width()) */50), DpToPixel(/*viewRect.height()*/ 50));
                        BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                        if (icon != null) {
                            icon.setBackgroundDrawable(bmpDrawable);
                        }
                        tab.setDownloadStatus(DownloadStatus.SUCCESS);
                    } catch (IOException iOEx) {
                    }
                }
            } else {
                try {
                    Bitmap srcBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(Base64.decode(tab.getmIconData())));
                    Bitmap bmp = Utils.BmpResize(srcBitmap, DpToPixel(/*viewRect.width()) */50), DpToPixel(/*viewRect.height()*/ 50));
                    BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                    if (icon != null) {
                        icon.setBackgroundDrawable(bmpDrawable);
                    }
                    tab.setDownloadStatus(DownloadStatus.SUCCESS);
                } catch (IOException iOEx) {
                } catch (NullPointerException nPEx) {
                }
            }
        }
        StateListDrawable mIcon = new StateListDrawable();
        ShapeDrawable normalState = new ShapeDrawable(new RectShape());
        normalState.getPaint().setColor(appConfig.getTabBarDesign().color);
        ShapeDrawable pressedState = new ShapeDrawable(new RectShape());
        pressedState.getPaint().setColor(appConfig.getTabBarDesign().itemDesign.selectedColor);
        mIcon.addState(new int[]{android.R.attr.state_pressed}, pressedState);
        mIcon.addState(new int[]{android.R.attr.state_selected}, pressedState);
        mIcon.addState(new int[]{}, normalState);
        view.setBackgroundDrawable(mIcon);

        LinearLayout viewHolder = new LinearLayout(this);
        viewHolder.addView(view, lp);
        viewHolder.setBackgroundColor(Color.BLACK);

        final Widget tmpWidget = appConfig.getWidgetAtIndex(tab.getOrder());

        rootLayout.addView(viewHolder, lp);

        View tmpView = null;

        int displayHeight = 0;

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            displayHeight = getResources().getDimensionPixelSize(resourceId);
        }

        if(tmpWidget.isUpdated() && appConfig.isUpdateContentPushEnabled()){
            float dispCoef = getResources().getDisplayMetrics().widthPixels / 320f;
            tmpView = drawBadge(rootLayout,
                    lp.topMargin + (int)(12 * dispCoef),
                    lp.leftMargin + lp.width - (int)(15 * dispCoef));
        }

        final View badgeView = tmpView;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Runnable action = new Runnable() {
                    public void run() {
                        tmpWidget.setUpdated(false);

                        if (badgeView != null) rootLayout.removeView(badgeView);

                        AppBuilder.this.launchWidgetWithOrder(order);
                    }
                };
                Handler h = new Handler();
                h.postDelayed(action, 200);
            }
        });
    }

    private void setBackgroundUseConfig(final AppConfigure appConfig, final RelativeLayout view,
                                        RelativeLayout parentView) {
        try {

            /**
             * Installing background color
             * */
            try {
                parentView.setBackgroundColor
                        (Color.parseColor(appConfig.getBackgroundColor()));
            } catch (Exception e) {
            }

            /**
             * Installing background image
             * */
            String bgImageUrl = appConfig.getBackgroundImageUrl();


            if (!TextUtils.isEmpty(appConfig.getBackgroundImageUrl())) {
                boolean useCache = false;
                // checking... if background image already exists in cache file
                if (appConfig.getBackgroundImageCache().length() > 0) {
                    File file = new File(appConfig.getBackgroundImageCache());
                    if (file.exists()) {
                        useCache = true;
                    } else {
                        appConfig.setBackgroundImageCache("");
                    }
                }
                if (useCache) { // if cache file exists -> assign it to background
                    try {
                        System.gc();
                        // decode image with appropriate options
                        Bitmap bitmap = null;
                        try {
                            System.gc();
                            bitmap = BitmapFactory.decodeFile(appConfig.getBackgroundImageCache());
                        } catch (Exception ex) {
                            Log.d("", "");
                        } catch (OutOfMemoryError e) {
                            Log.d("", "");
                            System.gc();
                            try {
                                bitmap = BitmapFactory.decodeFile(appConfig.getBackgroundImageCache());
                            } catch (Exception ex) {
                                Log.d("", "");
                            } catch (OutOfMemoryError ex) {
                                Log.e("decodeImageFile", "OutOfMemoryError");
                            }
                        }

                        //Bitmap bitmap = BitmapFactory.decodeFile(appConfig.getBackgroundImageCache());
                        bitmap = Utils.BmpResizeDisplay(bitmap, (Activity) AppBuilder.this);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                        view.setBackgroundDrawable(bitmapDrawable);
                        appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                        Log.d("Download background", "Success");
                    } catch (Exception e) {
                        try {
                            File file = new File(appConfig.getBackgroundImageCache());
                            file.delete();
                            setBackgroundUseConfig(appConfig, view, parentView); // second attempt to load image
                        } catch (Exception ex) {
                        }
                    }
                } else { // otherwise -> download it from server
                    if (Tools.checkNetwork(AppBuilder.this) > 0) {
                        if (sdAvailable) {
                            final String bgImageCache = com.appbuilder.sdk.android.Statics.cachePath + "/assets/" + md5(bgImageUrl);
                            appConfig.setBackgroundImageCache(bgImageCache);
                            final DownloadHelper dh = new DownloadHelper((Activity) this, bgImageUrl, bgImageCache, null, false, true);
                            dh.setStartedRunnable(new Runnable() {
                                public void run() {
                                    Log.d("Download", "Start");
                                }
                            });

                            dh.setFailedRunnable(new Runnable() {
                                public void run() {
                                    Log.d("Download", "Failed: " + dh.getErrorString());
                                    appConfig.setBackgroundDownloaded(DownloadStatus.FAILED);
                                }
                            });

                            dh.setSuccessRunnable(new Runnable() {
                                public void run() {
                                    try {
                                        // decode image with appropriate options
                                        System.gc();
                                        Drawable immutableDrawable = null;
                                        try {
                                            System.gc();
                                            immutableDrawable = Drawable.createFromPath(bgImageCache);
                                        } catch (Exception ex) {
                                            Log.d("", "");
                                        } catch (OutOfMemoryError e) {
                                            Log.d("", "");
                                            System.gc();
                                            try {
                                                immutableDrawable = Drawable.createFromPath(bgImageCache);
                                            } catch (Exception ex) {
                                                Log.d("", "");
                                            } catch (OutOfMemoryError ex) {
                                                Log.e("decodeImageFile", "OutOfMemoryError");
                                            }
                                        }

                                        Drawable mutableDrawable = immutableDrawable.mutate();
                                        Bitmap srcBitmap = ((BitmapDrawable) mutableDrawable).getBitmap();
                                        Bitmap bmp = Utils.BmpResizeDisplay(srcBitmap, (Activity) AppBuilder.this);
                                        BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                                        view.setBackgroundDrawable(bmpDrawable);

                                        Log.d("Download background", "Success");
                                    } catch (Exception e) {
                                        Log.w("Download background", "");
                                    }
                                    appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                                }
                            });

                            dh.start();
                        } else {
                            try {
                                URL url = new URL(bgImageUrl);
                                URLConnection urlConn = url.openConnection();

                                HttpURLConnection httpConn = (HttpURLConnection) urlConn;

                                httpConn.connect();

                                //Drawable drawable = Drawable.createFromStream();
                                Bitmap srcBitmap = BitmapFactory.decodeStream(httpConn.getInputStream());
                                Bitmap bmp = Utils.BmpResizeDisplay(srcBitmap, (Activity) AppBuilder.this);
                                BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                                view.setBackgroundDrawable(bmpDrawable);
                                appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                            } catch (IOException iOEx) {
                            } catch (NullPointerException nPEx) {
                            }
                        }
                    } else {
                        if (appConfig.getmBackgorundImageData() != null) {
                            try {
                                Bitmap srcBitmap = BitmapFactory.decodeStream
                                        (new ByteArrayInputStream(Base64.decode(appConfig.getmBackgorundImageData())));
                                Bitmap bmp = Utils.BmpResizeDisplay(srcBitmap, (Activity) AppBuilder.this);
                                BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                                view.setBackgroundDrawable(bmpDrawable);
                                appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                            } catch (Exception ex) {
                                Log.e("", "");
                            }
                        }
                    }
                }
            } else if ( !TextUtils.isEmpty(appConfig.getmBackgorundImageData()) ) {
                try {
                    Bitmap srcBitmap = BitmapFactory.decodeStream
                            (new ByteArrayInputStream(Base64.decode(appConfig.getmBackgorundImageData())));
                    Bitmap bmp = Utils.BmpResizeDisplay(srcBitmap, (Activity) AppBuilder.this);
                    BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                    view.setBackgroundDrawable(bmpDrawable);
                    appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
                } catch (Exception ex) {
                    Log.e("", "");
                }
            }else { // in case that URL to background file doesn't exist -> set default background color
                if (appConfig.getBackgroundColor().length() > 0) {
                    try {
                        Bitmap bmp = Utils.BmpResizeDisplay(Utils.CreateSquareColorBitmap(appConfig.getBackgroundColor()), (Activity) AppBuilder.this);
                        BitmapDrawable bmpDrawable = new BitmapDrawable(bmp);
                        view.setBackgroundDrawable(bmpDrawable);

                        Log.d("Set background color", "Success");
                    } catch (Exception e) {
                        Log.w("Download background color", e);
                    }
                }
                appConfig.setBackgroundDownloaded(DownloadStatus.SUCCESS);
            }

        } catch (Exception e) {
        }
    }

    private long sdAvailableBytes() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesCount = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        return bytesCount;
    }


    // ***********************************************************************************
    // this function downloads splash screen
    private void splashScreenDownload(String splashUrl, String cachePath) {
        try {
            URL url = new URL(splashUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // sending request ...
            conn.connect();
            if (conn.getResponseCode() == 200) {
                BufferedInputStream mstream = new BufferedInputStream(conn.getInputStream());
                File mfile = new File(cachePath + "/splash.jpg");
                if (!mfile.exists()) {
                    mfile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(mfile);
                byte buf[] = new byte[1024];
                int count = 0;
                while ((count = mstream.read(buf, 0, 1024)) != -1) {
                    fos.write(buf, 0, count);
                    Arrays.fill(buf, (byte) 0);
                }
                fos.flush();
                fos.close();
                mstream.close();
            }
        } catch (ProtocolException e) {
            Log.d("splashScreenDownload", e.getMessage());
        } catch (IOException e) {
            Log.d("splashScreenDownload", e.getMessage());
        } catch (NullPointerException e) {
            Log.d("splashScreenDownload", e.getMessage());
        }
    }

    private void downloadFavicons(AppConfigure appConfig) {
        for (int i = 0; i < appConfig.getWidgetsCount() - 1; i++) {
            Widget tempWidget = appConfig.getWidgetAtIndex(i);
            if (tempWidget != null) {
                String faviconFilePath = new String(com.appbuilder.sdk.android.Statics.cachePath + File.separator + md5(tempWidget.getFaviconURL()));
                File temp = new File(faviconFilePath);
                if (!temp.exists()) {
                    widgetFaviconDownload(tempWidget.getFaviconURL());
                }
                tempWidget.setFaviconFilePath(faviconFilePath);
            }
        }
    }

    private String widgetFaviconDownload(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(8000);
            conn.setConnectTimeout(8000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // sending request ...
            conn.connect();
            File mfile = null;
            if (conn.getResponseCode() == 200) {
                BufferedInputStream mstream = new BufferedInputStream(conn.getInputStream());
                mfile = new File(com.appbuilder.sdk.android.Statics.cachePath + File.separator + md5(urlStr));
                if (!mfile.exists()) {
                    mfile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(mfile);
                byte buf[] = new byte[1024];
                int count = 0;
                while ((count = mstream.read(buf, 0, 1024)) != -1) {
                    fos.write(buf, 0, count);
                    Arrays.fill(buf, (byte) 0);
                }
                fos.flush();
                fos.close();
                mstream.close();
            }

            return mfile.getAbsolutePath();

        } catch (ProtocolException e) {
            Log.d("splashScreenDownload", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.d("splashScreenDownload", e.getMessage());
            return null;
        } catch (NullPointerException e) {
            Log.d("splashScreenDownload", e.getMessage());
            return null;
        }
    }

    /* *************************************************************************
     * Calling when child activity finished it's work and trying to return result
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // проверка если от модуля пришло сообщение срочно закрыться и просьба запустить другой модуль
        if (data != null) {
            String res = data.getStringExtra(com.appbuilder.sdk.android.Statics.FORCE_CLOSE_MODULE_FLAG);
            if (!TextUtils.isEmpty(res)) {
                if (res.compareToIgnoreCase(com.appbuilder.sdk.android.Statics.FORCE_CLOSE_MODULE) == 0) {
                    int order = data.getIntExtra(com.appbuilder.sdk.android.Statics.FORCE_CLOSE_NEW_MODULE_ORDER, -1);
                    Log.e(TAG, "Order = " + order);

                    if (sdAvailable) {
                        File cache = new File(com.appbuilder.sdk.android.Statics.cachePath + "/cache.data");
                        if (cache.exists()) {
                            try {
                                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
                                appConfig = (AppConfigure) ois.readObject();
                                ois.close();

                                PluginManager pm = PluginManager.getManager();
                                pm.setFirstStart(firstPluginStart);
                                firstPluginStart = false;

                                try {
                                    // getting widget according to order value
                                    Widget widget = appConfig.getWidgetWithOrder(order);
                                    widget.setCachePath(com.appbuilder.sdk.android.Statics.cachePath);
                                    widget.setHaveAdvertisement(appConfig.getShowLink());

                                    needToClose = widget.getPluginName().equals("FeedbackPlugin");

                                    // if such module exists -> start launch
                                    if (widget.getPluginPackage().length() > 0) {
                                        startedPluginName = widget.getTitle();
                                        if (TextUtils.isEmpty(startedPluginName)) {
                                            startedPluginName = widget.getPluginName();
                                        }
                                        pm.loadPlugin(AppBuilder.this, AppBuilder.this, widget, appConfig.getAppAdv(),
                                                false, appConfig.getmWidgets(), appConfig);
                                        Log.d("PLUGINDATA", widget.getPluginXmlData());
                                    } else {
                                        dontLaunchModule = false;
                                        Toast.makeText(AppBuilder.this, "The functionality is not available.", Toast.LENGTH_LONG).show();
                                    }
                                } catch (RuntimeException e) {
                                    Log.w("PLUGINDATA", e);
                                }


                            } catch (Exception e) {
                                Log.e(TAG, "Configuration load error");
                            }
                        }
                    }
                    return;
                }
            }

            String resCloseApp = data.getStringExtra(com.appbuilder.sdk.android.Statics.FORCE_CLOSE_APP_FLAG);
            if (!TextUtils.isEmpty(resCloseApp)) {
                finish();
            }
        }


        switch (requestCode) {
            case START_MODULE: {
                if (resultCode == RESULT_OK) {

                    /**
                     * Initialization of class responsible for start of plugins
                     * */
                    PluginManager pm = PluginManager.getManager();
                    pm.setFirstStart(firstPluginStart);
                    firstPluginStart = false;

                    try {
                        // getting widget according to order value
                        Widget widget = (Widget) data.getSerializableExtra("widget");
                        widget.setCachePath(com.appbuilder.sdk.android.Statics.cachePath);
                        widget.setHaveAdvertisement(appConfig.getShowLink());

                        // if such module exists -> start launch
                        if (widget.getPluginPackage().length() > 0) {
                            startedPluginName = widget.getTitle();
                            if (TextUtils.isEmpty(startedPluginName)) {
                                startedPluginName = widget.getPluginName();
                            }

                            pm.loadPlugin(AppBuilder.this, AppBuilder.this, widget, appConfig.getAppAdv(),
                                    false, appConfig.getmWidgets(), appConfig);
                        }
                    } catch (Exception e) {
                        Log.d("", "");
                    }
                } else {
                    if (isShown) {
                        isShown = false;
                    }

                }
            }
            break;
            case LOGIN_SCREEN: {
                if (resultCode == RESULT_OK) {
                    String userName = data.getStringExtra("username");
                    if (userID != null && userID.equals("186589")) {
                        if (userName != null) {
                            FlurryAgent.setUserId(userName);
                        }
                    }
                }
            }
            break;
        }

        if ( requestCode == AUTHORIZATION_FACEBOOK )
        {
            if ( resultCode == RESULT_OK )
            {
//                if ( Utils.checkNetwork(MainActivity.this)  > 0 )
//                {
                shareViaFacebook();
//                    FlurryLogger.sharingFacebookAttempt();
//                }
//                else
//                    Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

            }
        } else if ( requestCode == AUTHORIZATION_TWITTER )
        {
            if ( resultCode == RESULT_OK )
            {
//                if ( Utils.checkNetwork(MainActivity.this)  > 0 )
//                {
                shareViaTwitter();
//                    FlurryLogger.sharingTwitterAttempt();
//                }
//                else
//                    Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        } else if ( requestCode == SHARING_FACEBOOK )
        {
            if ( resultCode == RESULT_OK)
            {
                Toast.makeText(AppBuilder.this, getString(R.string.sharing_success), Toast.LENGTH_SHORT).show();
//                FlurryLogger.sharingFacebookResult(0);
            } else if ( resultCode == RESULT_CANCELED )
            {
//                FlurryLogger.sharingFacebookResult(-1);
                Toast.makeText(AppBuilder.this, getString(R.string.sharing_error), Toast.LENGTH_SHORT).show();
            }
        } else if ( requestCode == SHARING_TWITTER )
        {
            if ( resultCode == RESULT_OK)
            {
                Toast.makeText(AppBuilder.this, getString(R.string.sharing_success), Toast.LENGTH_SHORT).show();
//                FlurryLogger.sharingTwitterResult(0);
            } else if ( resultCode == RESULT_CANCELED )
            {
//                FlurryLogger.sharingTwitterResult(-1);
//                int error_code;
//                try {
//                    error_code = Integer.valueOf(popSharingActivityResultData("error_code"));
//                } catch(NumberFormatException exception) {
//                    error_code = -1;
//                }
//                switch ( error_code )
//                {
//                    case 1:
//                    {
//                        Toast.makeText(MainActivity.this, getString(R.string.sharing_dublicated), Toast.LENGTH_SHORT).show();
//                    } break;
//
//                    case -1:
//                    {
                Toast.makeText(AppBuilder.this, getString(R.string.sharing_error), Toast.LENGTH_SHORT).show();
//                    } break;

            }
        } else if ( requestCode == SHARING_EMAIL )
        {
            if ( resultCode == RESULT_OK )
            {
//                Toast.makeText(AppBuilder.this, getString(R.string.sharing_success), Toast.LENGTH_SHORT).show();
//                FlurryLogger.sharingEmalResult(0);
            } else if ( resultCode == RESULT_CANCELED )
            {
//                Toast.makeText(AppBuilder.this, getString(R.string.sharing_error), Toast.LENGTH_SHORT).show();
//                FlurryLogger.sharingEmalResult(-1);
            }
        } else if ( requestCode == SHARING_SMS )
        {
            if ( resultCode == RESULT_OK )
            {
                body = new SmsBody();
                List<MyContact> list = (List<MyContact>) data.getSerializableExtra("list");

                // если ни один контакт не выбран - просто выходим
                if ( list == null || list.size() == 0)
                    return;

                int index = 1;
                for ( MyContact c : list )
                {
                    String modifiedPhone = c.phones.get(0).replace(" ", "").replace("-","").replace("+","");
                    if ( modifiedPhone.charAt(0) == '8')
                    {
                        char[] temp = modifiedPhone.toCharArray();
                        temp[0] = '7';
                        modifiedPhone = new String(temp);
                    }
                    SmsBody.PhoneNamePare tempPair = new SmsBody.PhoneNamePare(modifiedPhone,c.name);
                    body.phones.put(Integer.toString(index), tempPair);
                    index++;
                    //body.phones.add(tempPair);
                }

                String str = new Gson().toJson(body);
                Log.e(TAG, str);

                showProgressDialog();
                new Thread( new Runnable() {
                    @Override
                    public void run() {
                        Runnable action = null;
                        try {
                            SmsSharingResponse response = ServerApi.getInstance().smsSharing(body);
//                            FlurryLogger.sharingSmsResult(0);
                            action = new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AppBuilder.this, getString(R.string.sharing_sms_success), Toast.LENGTH_SHORT).show();
                                    hideProgressDialog();
                                }
                            };
                        } catch (RetrofitError e) {
//                            FlurryLogger.sharingSmsResult(-1);
                            action = new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AppBuilder.this, getString(R.string.sharing_sms_error), Toast.LENGTH_SHORT).show();
                                    hideProgressDialog();
                                }
                            };
                        }
                        runOnUiThread( action );
                    }
                }).start();
            } //else
//                FlurryLogger.sharingSmsResult(-1);

        }
    }

    private void prepareAnimationObjects() {
        animShowMenu = new TranslateAnimation(0, -(Double.valueOf(screenWidth * 0.85).intValue()), 0, 0);
        animShowMenu.setInterpolator(new SmoothInterpolator());
        animShowMenu.setDuration(400);
        animShowMenu.setFillEnabled(true);
        animShowMenu.setAnimationListener(
                new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Double.valueOf(screenWidth * 1.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
                        params.gravity = Gravity.LEFT;
                        params.setMargins(-(Double.valueOf(screenWidth * 0.85).intValue()), 0, 0, 0);
                        rootContainer.setLayoutParams(params);
                    }
                }
        );

        animHideMenu = new TranslateAnimation(0, Double.valueOf(screenWidth * 0.85).intValue(), 0, 0);
        animHideMenu.setInterpolator(new SmoothInterpolator());
        animHideMenu.setDuration(400);
        animHideMenu.setFillEnabled(true);
        animHideMenu.setAnimationListener(
                new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Double.valueOf(screenWidth * 1.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
                        params.gravity = Gravity.LEFT;
                        params.setMargins(0, 0, 0, 0);
                        rootContainer.setLayoutParams(params);
                    }
                }
        );

        // notification fialog animation
        animShowDialog = new AlphaAnimation(0, 1);
        animShowDialog.setDuration(500);
        animShowDialog.setFillAfter(true);
        animShowDialog.setInterpolator(new LinearInterpolator());
        animShowDialog.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialogHolder.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });


        animHideDialog = new AlphaAnimation(1, 0);
        animHideDialog.setDuration(500);
        animHideDialog.setFillAfter(true);
        animHideDialog.setInterpolator(new LinearInterpolator());
        animHideDialog.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialogHolder.removeAllViews();
                dialogHolder.setVisibility(View.INVISIBLE);
                Log.e(TAG, "dialogHolder.setVisibility(View.GONE)");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        int order = actualWidgetList.get(i).getOrder();

        if(order != -1)
            com.appbuilder.sdk.android.Statics.sidebarClickListeners.get(order).onClick(view);

//        Widget tempWidget = actualWidgetList.get(i);
//
//        PluginManager pm = PluginManager.getManager();
//        pm.setFirstStart(firstPluginStart);
//        firstPluginStart = false;
//
//        try {
//            // getting widget according to order value
//            tempWidget.setCachePath(com.appbuilder.sdk.android.Statics.cachePath);
//            tempWidget.setHaveAdvertisement(appConfig.getShowLink());
//
//            // if such module exists -> start launch
//            if (tempWidget.getPluginPackage().length() > 0) {
//                // запоминаем название модуля -> нужно для того чтобы отловить событие onRestart и сказать что именно ЭТОТ модуль закрылся
//                startedPluginName = tempWidget.getTitle();
//                if (TextUtils.isEmpty(startedPluginName)) {
//                    startedPluginName = tempWidget.getPluginName();
//                }
//
//                pm.loadPlugin(AppBuilder.this, AppBuilder.this, tempWidget, appConfig.getAppAdv(),
//                        false, appConfig.getmWidgets(), appConfig);
//            }
//        } catch (Exception e) {
//            Log.d("", "");
//        }
    }

    protected class SmoothInterpolator implements android.view.animation.Interpolator {
        @Override
        public float getInterpolation(float v) {
            return (float) Math.pow(v - 1, 3) + 1;
        }
    }

    // *******************************************************************************
    // decoding image from file path
    public static Bitmap proccessBitmap(String fileName) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //opts.inSampleSize = 2;

        Bitmap bitmap = null;
        try {
            // decode image with appropriate options
            File tempFile = new File(fileName);
            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
            } catch (Exception ex) {
                Log.d("", "");
            } catch (OutOfMemoryError e) {
                Log.d("", "");
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError ex) {
                    Log.e("decodeImageFile", "OutOfMemoryError");
                }
            }
        } catch (Exception e) {
            Log.d("", "");
            return null;
        }

        return bitmap;
    }

    @Override
    final public void onPause() {
        // activity animation
        if ( allowCloseAnimation )
        {
            profiling("overridePendingTransition");
            //overridePendingTransition(R.anim.activity_open_scale_main, R.anim.activity_close_translate_main);
        }

        super.onPause();
    }

    public String md5(FileInputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            FileChannel channel = inputStream.getChannel();
            ByteBuffer buff = ByteBuffer.allocate(2048);
            while (channel.read(buff) != -1) {
                buff.flip();
                md.update(buff);
                buff.clear();
            }
            byte[] hashValue = md.digest();
            return new String(hashValue);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {

            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (!TextUtils.isEmpty(intent.getStringExtra(com.appbuilder.sdk.android.Statics.FORCE_CLOSE_APP_FLAG)))
            finish();
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed()");
        if (isDialogShowen) {
            isDialogShowen = false;
            dialogHolder.startAnimation(animHideDialog);
            createPushDialog();
        } else if(appConfig.isShowSidebar() && isShown) {
            hideSidebar();
        } else
        {
            finish();
            overridePendingTransition(R.anim.activity_open_scale_main, R.anim.activity_close_translate_main);
            //allowCloseAnimation = true;
        }

    }

    private void profiling ( String msg )
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        Log.e(TAG, formatter.format(new Date()) +" "+  msg );
    }

//    private View createMasrerappRightSide()
//    {
//        LinearLayout holder = new LinearLayout(this);
//        holder.setBackgroundColor(Color.parseColor("#333639"));
//        holder.setPadding(0, (int) (40* density), 0,0);
//        holder.setOrientation(LinearLayout.VERTICAL);
//        holder.setLayoutParams( new ViewGroup.LayoutParams((int) (screenWidth * 0.5), ViewGroup.LayoutParams.MATCH_PARENT) );
//
//        //homebtn
//        String homeStr = "";
//        String favouritesStr = "";
//        String complainStr = "";
//
//        Locale current = getResources().getConfiguration().locale;
//        if ( current.getLanguage().compareToIgnoreCase("en") == 0 )
//        {
//            homeStr = "Home";
//            favouritesStr = "Favorite";
//            complainStr = "Flag Content";
//        } else if ( current.getLanguage().compareToIgnoreCase("ru") == 0 )
//        {
//            homeStr = "Домой";
//            favouritesStr = "Избранное";
//            complainStr = "Пожаловаться";
//        }
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (50* density));
//
//        View masterappHome = createMasterappBtn(R.drawable.masterapp_home, homeStr);
//        masterappHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//
//        View masterappFavourites = createMasterappCheckbox(favouritesStr);
//
//        View masterappComplain = createMasterappBtn(R.drawable.masterapp_complain, complainStr);
//        masterappComplain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // делаем запрос
//                complainOk = "";
//                complainFalse = "";
//                complainNoInternet = "";
//
//                Locale current = getResources().getConfiguration().locale;
//                if ( current.getLanguage().compareToIgnoreCase("en") == 0 )
//                {
//                    complainOk = "Your complain has been send and will be consider in short";
//                    complainNoInternet = "No internet connection available";
//                    complainFalse = "Unable connect to server";
//                } else if ( current.getLanguage().compareToIgnoreCase("ru") == 0 )
//                {
//                    complainOk = "Ваш запрос успешно отправлен и будет в ближайшее время рассмотрен";
//                    complainNoInternet = "Отсутствует соединение с интернетом";
//                    complainFalse = "Неозможно подключиться к серверу";
//                }
//
//                // http request
//                final String imagePath = rootScroller.makeScreenshot(getExternalCacheDir().getAbsolutePath());
//                if ( !TextUtils.isEmpty(imagePath) )
//                {
//                    if ( Utils.networkAvailable(AppBuilder.this) )
//                    {
//                        showProgressDialog();
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                final boolean status;
//                                try {
//                                    status = Utils.sendClaim(imagePath);
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if ( status )
//                                                Toast.makeText(AppBuilder.this, complainOk, Toast.LENGTH_SHORT).show();
//                                            else
//                                                Toast.makeText(AppBuilder.this, complainFalse, Toast.LENGTH_SHORT).show();
//                                            hideProgressDialog();
//                                        }
//                                    });
//                                } catch (Exception e) {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(AppBuilder.this, complainFalse, Toast.LENGTH_SHORT).show();
//                                            hideProgressDialog();
//                                        }
//                                    });
//                                }
//                            }
//                        }).start();
//                    } else
//                        Toast.makeText(AppBuilder.this, complainNoInternet, Toast.LENGTH_SHORT).show();
//                }else
//                    Toast.makeText(AppBuilder.this, complainFalse, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        holder.addView(masterappHome, params);
//        holder.addView(masterappFavourites,params);
//        holder.addView(masterappComplain,params);
//
//        return holder;
//    }
//
//    private View createMasterappBtn( int resPath, String text )
//    {
//        LinearLayout holder = new LinearLayout(this);
//        holder.setOrientation(LinearLayout.HORIZONTAL);
//        holder.setGravity(Gravity.CENTER_VERTICAL);
//
//        StateListDrawable stateList = new StateListDrawable();
//        GradientDrawable pressed = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#805d5d5d"), Color.parseColor("#805d5d5d")});
//        stateList.addState(new int[]{android.R.attr.state_pressed}, pressed);
//        GradientDrawable normal = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#00ffffff"), Color.parseColor("#00ffffff")});
//        stateList.addState(new int[]{}, normal);
//        holder.setBackgroundDrawable(stateList);
//
//        ImageView backImage = new ImageView(AppBuilder.this);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (25* density), (int) (25* density));
//        params.setMargins((int) (18* density), 0, (int) (8* density), 0);
//
//        backImage.setLayoutParams( params );
//        backImage.setImageResource(resPath);
//
//
//        TextView textView = new TextView(this);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//        textView.setText(text);
//        textView.setTextColor(Color.WHITE);
//
//        holder.addView( backImage );
//        holder.addView( textView );
//
//        return holder;
//    }
//
//    private View createMasterappCheckbox( String text )
//    {
//        LinearLayout holder = new LinearLayout(this);
//        holder.setOrientation(LinearLayout.HORIZONTAL);
//        holder.setGravity(Gravity.CENTER_VERTICAL);
//
//        final GradientDrawable pressed = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#805d5d5d"), Color.parseColor("#805d5d5d")});
//        final GradientDrawable normal = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#00ffffff"), Color.parseColor("#00ffffff")});
//        holder.setBackgroundDrawable(new StateListDrawable() {{
//            addState(new int[]{android.R.attr.state_pressed}, pressed);
//            addState(new int[]{}, normal);
//        }});
//
//        final CheckBox checkBox = new CheckBox(this);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (25 * density), (int) (25 * density));
//        params.setMargins((int) (18* density), 0, (int) (8* density), 0);
//        checkBox.setLayoutParams(params);
//        checkBox.setChecked(com.appbuilder.sdk.android.Statics.favouritedMasterApp);
//        checkBox.setButtonDrawable(new BitmapDrawable());
//
//        StateListDrawable stateList = new StateListDrawable();
//        Drawable btmAdded = getResources().getDrawable(R.drawable.masterapp_favourits_added);
//        Drawable btmRemoved = getResources().getDrawable(R.drawable.masterapp_favourits);
//        stateList.addState(new int[]{android.R.attr.state_checked}, btmAdded);
//        stateList.addState(new int[]{}, btmRemoved);
//        checkBox.setBackgroundDrawable(stateList);
//        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                com.appbuilder.sdk.android.Statics.favouritedMasterApp = isChecked;
//                Intent messageIntent = new Intent(com.appbuilder.sdk.android.Statics.FAVOURITES_BROADCAST);
//                messageIntent.putExtra("appid", com.appbuilder.sdk.android.Statics.appId);
//                messageIntent.putExtra("favourites", isChecked);
//                sendBroadcast(messageIntent);
//            }
//        });
//
//        holder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                checkBox.toggle();
//            }
//        });
//
//        TextView textView = new TextView(this);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//        textView.setText(text);
//        textView.setTextColor(Color.WHITE);
//
//        holder.addView( checkBox );
//        holder.addView( textView );
//
//        return holder;
//    }

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, getString(R.string.load));
        progressDialog.setCancelable(false);
    }

    private void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (NullPointerException nPEx) {
        }
    }

    private void applySidebarAnimation(Animation animation) {
        rootContainer.clearAnimation();
        rootContainer.startAnimation(animation);
    }

    private void showSidebar() {
        applySidebarAnimation(animShowMenu);
        isShown = true;
        rootContainer.enableSwipe();
    }

    private void hideSidebar() {
        applySidebarAnimation(animHideMenu);
        isShown = false;
        rootContainer.disableSwipe();
    }

    private void inverseSidebarState() {
        if(appConfig.isShowSidebar()) {
            if(isShown) {
                hideSidebar();
            } else {
                showSidebar();
            }
        }
    }

    private void addMasterappActions() {
        Widget toMasterapp = new Widget();
        toMasterapp.setOrder(com.appbuilder.sdk.android.Statics.getSidebarNonWidgetClickListenerIndex());
        toMasterapp.setLabel(getString(R.string.sidebar_to_masterapp));
        toMasterapp.setAddToSidebar(true);
        toMasterapp.setIconResourceId(R.drawable.masterapp_home);
        com.appbuilder.sdk.android.Statics.sidebarClickListeners.put(toMasterapp.getOrder(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Widget toFavorites = new Widget();
        toFavorites.setOrder(com.appbuilder.sdk.android.Statics.getSidebarNonWidgetClickListenerIndex());
        toFavorites.setLabel(getString(R.string.sidebar_to_favorites));
        toFavorites.setAddToSidebar(true);
        toFavorites.setIconResourceId(com.appbuilder.sdk.android.Statics.favouritedMasterApp ? R.drawable.masterapp_favourits_added : R.drawable.masterapp_favourits);
        com.appbuilder.sdk.android.Statics.sidebarClickListeners.put(toFavorites.getOrder(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.appbuilder.sdk.android.Statics.favouritedMasterApp = !com.appbuilder.sdk.android.Statics.favouritedMasterApp;
                ((ImageView) v.findViewById(R.id.sidebar_item_icon))
                        .setImageResource(com.appbuilder.sdk.android.Statics.favouritedMasterApp ? R.drawable.masterapp_favourits_added : R.drawable.masterapp_favourits);
                sendBroadcast(new Intent(com.appbuilder.sdk.android.Statics.FAVOURITES_BROADCAST) {{
                    putExtra("appid", com.appbuilder.sdk.android.Statics.appId);
                    putExtra("favourites", com.appbuilder.sdk.android.Statics.favouritedMasterApp);
                }});
            }
        });

        Widget shareApp = new Widget();
        shareApp.setOrder(com.appbuilder.sdk.android.Statics.getSidebarNonWidgetClickListenerIndex());
        shareApp.setLabel(getString(R.string.sidebar_share));
        shareApp.setAddToSidebar(true);
        shareApp.setIconResourceId(R.drawable.masterapp_app_share);
        shareApp.setDrawSharing(true);
        com.appbuilder.sdk.android.Statics.sidebarClickListeners.put(shareApp.getOrder(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View bottom = v.findViewById(R.id.sidebar_item_bottom_view);
                bottom.setVisibility(bottom.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                widgetList.getLayoutParams().height = widgetList.getHeight() +
                        (bottom.getVisibility() == View.VISIBLE ?
                                Float.valueOf(60 * getResources().getDisplayMetrics().density).intValue() :
                                -Float.valueOf(60 * getResources().getDisplayMetrics().density).intValue());
            }
        });

        Widget flagContent = new Widget();
        flagContent.setOrder(com.appbuilder.sdk.android.Statics.getSidebarNonWidgetClickListenerIndex());
        flagContent.setLabel(getString(R.string.sidebar_flag_content));
        flagContent.setAddToSidebar(true);
        flagContent.setIconResourceId(R.drawable.masterapp_complain);
        com.appbuilder.sdk.android.Statics.sidebarClickListeners.put(flagContent.getOrder(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userContainer.setDrawingCacheEnabled(true);
                userContainer.buildDrawingCache(true);
                Bitmap b = Bitmap.createBitmap(userContainer.getDrawingCache());
                userContainer.setDrawingCacheEnabled(false);
                File fl = null;

                try {
                    fl = new File( getExternalCacheDir().getAbsolutePath() + File.separator + "test.png");
                    try {
                        if ( !fl.exists() )
                            fl.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( fl ));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fl = null;
                }

                final String imagePath = fl != null ? fl.getAbsolutePath() : "";

                if (!TextUtils.isEmpty(imagePath)) {
                    if (Utils.networkAvailable(AppBuilder.this)) {
                        showProgressDialog();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final boolean status;
                                try {
                                    status = Utils.sendClaim(imagePath);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (status)
                                                Toast.makeText(AppBuilder.this, R.string.complain_ok, Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(AppBuilder.this, R.string.complain_false, Toast.LENGTH_SHORT).show();
                                            hideProgressDialog();
                                        }
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AppBuilder.this, R.string.complain_false, Toast.LENGTH_SHORT).show();
                                            hideProgressDialog();
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else
                        Toast.makeText(AppBuilder.this, R.string.complain_no_internet, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(AppBuilder.this, R.string.complain_false, Toast.LENGTH_SHORT).show();
            }
        });

        actualWidgetList.add(0, new Widget() {{
            setAddToSidebar(true);
        }});
        actualWidgetList.add(0, flagContent);
        actualWidgetList.add(0, shareApp);
        actualWidgetList.add(0, toFavorites);
        actualWidgetList.add(0, toMasterapp);
    }

    @Override
    public void shareViaFacebook() {
        if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
            Intent bridge = new Intent(AppBuilder.this, SharingActivity.class);
            bridge.putExtra("type", "facebook");

            String appUrl = appConfig.getAppName();
            appUrl = appUrl.replaceAll("\\s", "-");
            appUrl = appUrl + "/" + appConfig.getmAppId();

            String shareString = getString(R.string.sharing_email_found) + " " + appConfig.getAppName() + " http://ibuildapp.com/market/" + appUrl;
            bridge.putExtra("msg", shareString);

            startActivityForResult(bridge, SHARING_FACEBOOK);
        } else
            Authorization.authorize(AppBuilder.this, AUTHORIZATION_FACEBOOK, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
    }

    @Override
    public void shareViaTwitter() {
        if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_TWITTER)) {
            Intent bridge = new Intent(AppBuilder.this, SharingActivity.class);
            bridge.putExtra("type", "twitter");

            String appUrl = appConfig.getAppName();
            appUrl = appUrl.replaceAll("\\s", "-");
            appUrl = appUrl + "/" + appConfig.getmAppId();

            String shareString = getString(R.string.sharing_email_found) + " " + appConfig.getAppName() + " http://ibuildapp.com/market/" + appUrl;
            bridge.putExtra("msg", shareString);

            startActivityForResult(bridge, SHARING_TWITTER);
        } else
            Authorization.authorize(AppBuilder.this, AUTHORIZATION_TWITTER, Authorization.AUTHORIZATION_TYPE_TWITTER);
    }

    @Override
    public void shareViaEmail() {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        String appUrl = appConfig.getAppName();
        appUrl = appUrl.replaceAll("\\s", "-");
        appUrl = appUrl + "/" + appConfig.getmAppId();

        String shareString = getString(R.string.sharing_email_found) + " " + appConfig.getAppName() + " http://ibuildapp.com/market/" + appUrl;
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "iBuildapp Marketplace");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareString);

        startActivityForResult(Intent.createChooser(emailIntent, ""), SHARING_EMAIL);
    }

    @Override
    public void shareViaSms() {
        startActivityForResult(new Intent(AppBuilder.this, ContactChooser.class), SHARING_SMS);
    }

}