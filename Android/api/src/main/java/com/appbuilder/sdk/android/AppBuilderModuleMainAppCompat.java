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
package com.appbuilder.sdk.android;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appbuilder.sdk.android.pushnotification.AppPushNotificationDB;
import com.appbuilder.sdk.android.pushnotification.AppPushNotificationDialogLayout;
import com.appbuilder.sdk.android.pushnotification.AppPushNotificationMessage;
import com.appbuilder.sdk.android.view.TopBarHamburger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

public class AppBuilderModuleMainAppCompat extends AppCompatActivity implements AppBuilderInterface, TopBarInterface,
        AppAdvView.OnAdClosedListener{

    private long millis;
    private String flurryId = "";
    private AppAdvData advData = null;
    private AppAdvView adView = null;
    private Serializable session = null;
    protected Bundle state = null;
    private Widget widget = null;
    private ArrayList<View> surfaceObjects = new ArrayList<View>();
    protected HashMap<Object, HashMap<Object, Object>> nativeFeatures = new HashMap<Object, HashMap<Object, Object>>();
    private BroadcastReceiver broadcastReceiver;
    private NotificationManager mManager;
    private LinearLayout dialogHolder;
    public TextView countTokenView;

    public enum NATIVE_FEATURES {SMS, EMAIL, ADD_CONTACT, ADD_EVENT, LOCAL_NOTIFICATION}

    static public String TAG = AppBuilderModuleMainAppCompat.class.getCanonicalName();

    final private int MENU_ITEM_SMS_CLICK = 1;
    final private int MENU_ITEM_EMAIL_CLICK = 2;
    final private int MENU_ITEM_ADD_CONTACT_CLICK = 3;
    final private int MENU_ITEM_ADD_EVENT_CLICK = 4;
    final private int FIND_SURFACEOBJECTS = 5;

    final private int ARROW_WIDTH = 15;
    final private int ARROW_HEIGHT = 25;


    final private String MENU_INDEX = "MenuIndex";
    final private String MENU_TOP_COORDINATE = "MenuCoordinate";
    private final String WIDGET_HOLDER_BACKGROUND = "#3f434b";

    private final String WIDGET_TOPBAR_BACKGROUND = "#32363c";

    private ArrayList<Widget> widgets;
    private ArrayList<Widget> actualWidgetList = new ArrayList<Widget>();
    protected BarDesigner navBarDesign;
    private BarDesigner tabBarDesign;
    protected BarDesigner bottomBarDesign;
    private ListView widgetList;
    private OnSwipeInterface swipeInterface;
    private FrameLayout rootFrameLayout;
    private SwipeLinearLayout rootContainer;
    private LinearLayout userContainer;
    private LinearLayout menuContainer;
    private LinearLayout adTokenLayout;
    private String appId;
    private String AppBuilder;
    LinearLayout header = null;
    private boolean foreground;
    private TranslateAnimation animShowMenu;
    private TranslateAnimation animHideMenu;
    private AlphaAnimation animShowDialog;
    private AlphaAnimation animHideDialog;
    private boolean isDialogShowen = false;


    private LinearLayout adLayout;
    private LinearLayout rootRoot;

    protected boolean showSideBar = false;
    private boolean firstStart;

    private int screenWidth;
    private LayoutInflater layoutInflater = null;

    //    private LinearLayout homeButton;
    protected boolean isShown = false;
    private View userLayout = null;

    private LinearLayout topBar;
    private LinearLayout topBarLeftButton;
    private LinearLayout topBarRightButton;
    private TextView topBarTitle;
    private LinearLayout titleHolder;
    private float density;
    private ArrayList<Bitmap> thumbnails = new ArrayList<>();
    private SharedPreferences sPref;
    private boolean swipeBlock = false;
    private LinearLayout favouritesHolder;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MENU_ITEM_SMS_CLICK: {
                    sendSMS();
                }
                break;
                case MENU_ITEM_EMAIL_CLICK: {
                    sendEmail();
                }
                break;
                case MENU_ITEM_ADD_CONTACT_CLICK: {
                    addContact();
                }
                break;
                case MENU_ITEM_ADD_EVENT_CLICK: {
                    addEvent();
                }
                break;
                case FIND_SURFACEOBJECTS: {
                    getSufrafeObjects((View) message.obj);
                }
                break;
            }
        }
    };

    /* Activity methods */
    @Override
    final public void onCreate(Bundle savedInstanceState) {
        try {//ErrorLogging
            // *******************************************************************************
            // computing display width and height
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics metrix = new DisplayMetrics();
            display.getMetrics(metrix);
            screenWidth = metrix.widthPixels;
            density = getResources().getDisplayMetrics().density;

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);

            layoutInflater = LayoutInflater.from(this);
            rootRoot = create_main_layout();
            state = savedInstanceState;
            if(savedInstanceState != null) {
                try {
                    session = savedInstanceState.getSerializable("session");
                } catch (Exception e) {
                    LogWarning(e);
                }
            }

            readConfiguration();
            try {
                Intent currentIntent = getIntent();
                Bundle store = currentIntent.getExtras();
                advData = (AppAdvData) store.getSerializable("Advertisement");
                firstStart = store.getBoolean("firstStart");

                if (advData != null && !Statics.isAdClosed) {
                    if (advData.getAdvType().length() > 0) {
                        adView = new AppAdvView(this, advData, firstStart);
                        adView.setOnAdClosedListener(this);
                    }
                }

                flurryId = store.getString("flurry_id");
                widget = (Widget) store.getSerializable("Widget");
                widgets = (ArrayList<Widget>) store.getSerializable("Widgets");
                navBarDesign = (BarDesigner) store.getSerializable("navBarDesign");
                tabBarDesign = (BarDesigner) store.getSerializable("tabBarDesign");
                bottomBarDesign = (BarDesigner) store.getSerializable("bottomBarDesign");
                AppBuilder = store.getString("AppBuilder");
                try{
                    showSideBar = store.getBoolean("showSideBar", false);
                }catch(Exception ex){
                    LogError(ex);
                }

                appId = store.getString("appid");
                Log.e(TAG + "- AppBuilderModuleMain", "Appid = " + appId);

                AppPushNotificationDB.init(AppBuilderModuleMainAppCompat.this);

                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (foreground)
                        {
                            Log.e(TAG + "- AppBuilderModuleMain", "NOTIFICATON RECEIVED - status FOREGROUND");
                            createPushDialog();
                        } else
                            Log.e(TAG + "- AppBuilderModuleMain", "NOTIFICATON RECEIVED - status BACKGROUND");
                    }
                };

                try {
                    IntentFilter intFilt = new IntentFilter(Statics.appId);
                    registerReceiver(broadcastReceiver, intFilt);
                    Log.e(TAG, "register broadcast successfully. Appid = " + appId);
                } catch (Exception e) {
                    Log.e(TAG, "register broadcast ERROR Appid = " + appId);
                }
                mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // creating root container
                rootContainer = new SwipeLinearLayout(this, showSideBar);
                rootContainer.setOrientation(LinearLayout.HORIZONTAL);
                rootContainer.disableSwipe();

                // creating two child view: user's view and menu view
                menuContainer = new LinearLayout(this);
                menuContainer.setOrientation(LinearLayout.VERTICAL);
                menuContainer.setBackgroundColor(Color.parseColor(WIDGET_HOLDER_BACKGROUND));

                LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (showSideBar) {

                    // set adapter to listview
                    widgetList = new ListView(this);
                    widgetList.setBackgroundColor(Color.parseColor(WIDGET_HOLDER_BACKGROUND));
                    widgetList.setCacheColorHint(Color.parseColor(WIDGET_HOLDER_BACKGROUND));
                    widgetList.setVerticalScrollBarEnabled(false);
                    widgetList.setDivider(null);
                    widgetList.setDividerHeight(0);

                    for (int i = 0; i < widgets.size(); i++)
                        if (widgets.get(i).isAddToSidebar())
                            actualWidgetList.add(widgets.get(i));

                    SidebarAdapter adapter = new SidebarAdapter(this, actualWidgetList, widget.getOrder());
                    widgetList.setAdapter(adapter);
                    widgetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                            final int order = actualWidgetList.get(i).getOrder();

                            if(order == widget.getOrder()) {
                                hideSidebar();
                            } else if(order != -1) {
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

                                                Statics.sidebarClickListeners.get(order).onClick(view);
                                            }
                                        }
                                );
                                applySidebarAnimation(animHideMenuStartActivity);
                                isShown = false;
                            }
                        }
                    });
                    menuContainer.addView(widgetList, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
                        topMargin = Float.valueOf(getResources().getDisplayMetrics().density * 15).intValue();
                    }});
                }

                //------------------------------------
                // create user container
                userContainer = new LinearLayout(this);
                userContainer.setOrientation(LinearLayout.VERTICAL);
                topBar = createTopbarLayout();
                userContainer.addView(adLayout, par);
                userContainer.addView(topBar, par);

                // add child view to root container
                par = new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                rootContainer.addView(userContainer, par);
                par = new LinearLayout.LayoutParams(Double.valueOf(screenWidth * 0.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
                rootContainer.addView(menuContainer, par);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (screenWidth + screenWidth * 0.85),
                        ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.LEFT;
                params.setMargins(0, 0, 0, 0);
                rootFrameLayout.addView(rootContainer, params);

                dialogHolder = new LinearLayout(AppBuilderModuleMainAppCompat.this);
                dialogHolder.setVisibility(View.INVISIBLE);
                FrameLayout.LayoutParams fparams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                fparams.gravity = Gravity.CENTER;
                dialogHolder.setLayoutParams(fparams);
                rootFrameLayout.addView(dialogHolder);

                if (Statics.fromMasterApp) {
                    favouritesHolder = new LinearLayout(AppBuilderModuleMainAppCompat.this);
                    View favLayout = generateFavouriteLayout();

                    fparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    fparams.setMargins(0, (int) (20*getResources().getDisplayMetrics().density + (50*getResources().getDisplayMetrics().density)) ,0,0);
                    fparams.gravity = Gravity.TOP | Gravity.LEFT;
                    favouritesHolder.setLayoutParams( fparams);
                    rootFrameLayout.addView(favouritesHolder);
                    favouritesHolder.addView(favLayout);
                }

                // *******************************************************************************
                // preparing animation objects
                animShowMenu = new TranslateAnimation(0, -(Double.valueOf(screenWidth * 0.85).intValue()), 0, 0);
                animShowMenu.setInterpolator(new SmoothInterpolator());
                animShowMenu.setDuration(400);
                animShowMenu.setFillEnabled(true);
                animShowMenu.setAnimationListener(
                        new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                // hide surface view objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.INVISIBLE);
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

                                // show surfaceview objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.VISIBLE);
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
                                // hide surfaceview objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.INVISIBLE);
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

                                // show surfaceview objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.VISIBLE);
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
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                // *******************************************************************************
                // set swipe handler
                swipeInterface = new OnSwipeInterface() {
                    @Override
                    public void onSwipeLeft() {
                        if (showSideBar && !isShown) {
                            showSidebar();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if (showSideBar && isShown) {
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
                if (showSideBar)
                    rootContainer.setOnSwipeEvents(swipeInterface);
            } catch (Exception e) {
                LogError(e);
            }

        } catch (Exception e) {
            LogError(e);
            finish();
        }

        try {
            create();
        } catch (Exception e) {
            LogError(e);
        }
    }

    private static void LogError(Exception e) {
        Log.e(TAG, "", e);
    }

    private static void LogWarning(Exception e) {
        Log.w(TAG, e);
    }


    @Override
    final public void setTitle(CharSequence title) {
    }

    @Override
    final public void setTitle(int titleId) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {

            if (nativeFeatures.isEmpty()) {
                return false;
            }

            menu.clear();
            for (Map.Entry<Object, HashMap<Object, Object>> entry : nativeFeatures.entrySet()) {
                Object feature = entry.getKey();
                if (feature.equals(AppBuilderModule.NATIVE_FEATURES.SMS)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Send SMS");
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_SMS_CLICK);
                            return true;
                        }
                    });
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.EMAIL)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Send Email");
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_EMAIL_CLICK);
                            return true;
                        }
                    });
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_CONTACT)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Add Contact");
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_ADD_CONTACT_CLICK);
                            return true;
                        }
                    });
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_EVENT)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Add Event");
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_ADD_EVENT_CLICK);
                            return true;
                        }
                    });
                }
            }
            return super.onPrepareOptionsMenu(menu);

        } catch (Exception e) {
            LogError(e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    final public void onStart() {

        try {
            millis = System.currentTimeMillis();

            Map<String, String> map = new HashMap<>();
            map.put("action", "start");
//            FlurryAgent.logEvent(this.getClass().getSimpleName(), map);
        } catch (Exception e) {
            LogError(e);
        }

        try {
            start();
        } catch (Exception e) {
            LogError(e);
        }
        super.onStart();
    }

    @Override
    final public void onRestart() {
        try {
            restart();
        } catch (Exception e) {
            LogError(e);
        }
        super.onRestart();
    }

    @Override
    final public void onResume() {
        try {
            foreground = true;

            // restore
            if (showSideBar) {
                sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
                widgetList.setSelectionFromTop(sPref.getInt(MENU_INDEX, 0), sPref.getInt(MENU_TOP_COORDINATE, 0));
            }
            resume();


        } catch (Exception e) {
            LogError(e);
        }

        super.onResume();
    }

    @Override
    final public void onPause() {
        try {
            // save position and coordinate
            if (showSideBar) {
                int index = widgetList.getFirstVisiblePosition();
                View v = widgetList.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();

                sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(MENU_INDEX, index);
                ed.putInt(MENU_TOP_COORDINATE, top);
                ed.commit();
            }

            pause();

        } catch (Exception e) {
            LogError(e);
        }
        super.onPause();
    }

    @Override
    final public void onStop() {
        foreground = false;
        try {
            int seconds = (int) (System.currentTimeMillis() - millis) / 1000;

            Map<String, String> map = new HashMap<>();
            map.put("action", "stop");
            map.put("usage interval", "" + seconds);
            // FlurryAgent.logEvent(this.getClass().getSimpleName(), map);
        } catch (Exception e) {
            LogError(e);
        }

        try {
            stop();
        } catch (Exception e) {
            LogError(e);
        }
        super.onPause();
    }

    private void LogDebug(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    final public void onDestroy() {
        try {
            writeConfiguration();
        } catch (Exception e) {
            LogError(e);
        }

        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);


        try {
            destroy();
        } catch (Exception e) {
            LogError(e);
        }
        super.onDestroy();
    }


    public void setTokenBanner(){
        countTokenView = new TextView(this);
        updateTokenViewFromAdd(getPrefsInfoToken());
        adTokenLayout = new LinearLayout(this);
        LinearLayout.LayoutParams paramstoken = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        adTokenLayout.setLayoutParams(paramstoken);
        adTokenLayout.setOrientation(LinearLayout.HORIZONTAL);
        adTokenLayout.addView(countTokenView);
        adLayout.addView(adTokenLayout);
    }



    public void updateTokenViewFromAdd(Integer summ){
        countTokenView.setText("Token balance: "+summ+" IBA");
    }


    public Integer getPrefsInfoToken(){
        SharedPreferences  sharedPprefsToken = getSharedPreferences("tokenCount", Context.MODE_PRIVATE);
        Integer cToken = sharedPprefsToken.getInt("tokenCount", 0);
        return  cToken;

    }
 /*
    private rx.Observable<Integer> updatetokenBalance() {
        return rx.Observable.create(
                new rx.Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        subscriber.onNext(getPrefsInfoToken());
                        subscriber.onCompleted();

                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void setToken(final TextView textView, Integer summToken) {
        updatetokenBalance()
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer summ) {
                        Log.e("TOKEN BALANCE RXJAVA", String.valueOf(summ));
                        countTokenView.setText("Token balance: "+summ+" IBA");
                    }
                });
    }*/


    @Override
    final public void setContentView(int layoutResID) {
        try {
            try {
                if (adView != null) {
                    if (adView.getParent() != null) {
                        ViewGroup view = (ViewGroup) adView.getParent();
                        view.removeAllViews();
                        adTokenLayout.removeAllViews();
                    }
                    adLayout.addView(adView);
                    setTokenBanner();
                     adView.loadAdMob();
                }
            } catch (Exception e) {
                LogError(e);
            }

            // clean parent view
            if (userLayout != null)
                userContainer.removeView(userLayout);

            // add child view
            userLayout = layoutInflater.inflate(layoutResID, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            userContainer.addView(userLayout, params);

            // try to find surfaceview objects
            Message msg = handler.obtainMessage(FIND_SURFACEOBJECTS, userContainer);
            handler.sendMessageDelayed(msg, 500);
            super.setContentView(rootRoot);
            Log.d("", "");

        } catch (Exception e) {
            LogError(e);
        }
    }

    @Override
    final public void setContentView(View view) {
        try {
            try {
                if (adView != null) {
                    if (adView.getParent() != null) {
                        ViewGroup viewGroup = (ViewGroup) adView.getParent();
                        viewGroup.removeAllViews();
                        adTokenLayout.removeAllViews();
                    }
                    adLayout.addView(adView);
                    setTokenBanner();
                    adView.loadAdMob();
                }
            } catch (Exception e) {
                LogError(e);
            }

            // clean parent view
            if (userLayout != null)
                userContainer.removeView(userLayout);

            // add child view
            userLayout = view;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                 (LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            userContainer.addView(userLayout, params);


            // try to find surfaceview objects
            Message msg = handler.obtainMessage(FIND_SURFACEOBJECTS, userContainer);
            handler.sendMessageDelayed(msg, 500);
            super.setContentView(rootRoot);
        } catch (Exception e) {
            LogError(e);
        }
    }

    @Override
    final public void setContentView(View view, ViewGroup.LayoutParams params) {
        try {//ErrorLogging
            if (adView != null) {
                adLayout.addView(adView);
                setTokenBanner();
                adView.loadAdMob();
            }
            // clean parent view
            if (userLayout != null)
                userContainer.removeView(userLayout);

            // add child view
            userContainer.addView(view, params);
            userLayout = view;

            super.setContentView(rootRoot);
        } catch (Exception e) {
            LogError(e);
        }
    }

    @Override
    final public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("session", session);
    }

    @Override
    final public void startActivity(final Intent intent) {
        try {//ErrorLogging

            boolean isExtends = true;

            ComponentName cn = intent.resolveActivity(getPackageManager());
            String className = cn.getClassName();

            boolean classNotFound = false;

            Class class1 = null;
            try {
                class1 = Class.forName(className);
            } catch (ClassNotFoundException cNFEx) {
                LogError(cNFEx);

                classNotFound = true;
            }

            if (intent.getAction() == null) {
                try {
                    if(!className.equals("com.google.android.finsky.activities.LaunchUrlHandlerActivity")
                            && !classNotFound){

                        intent.putExtra("Advertisement", advData);
                        intent.putExtra("Widget", widget);
                        intent.putExtra("Widgets", widgets);
                        intent.putExtra("showSideBar", showSideBar);
                        intent.putExtra("navBarDesign", navBarDesign);
                        intent.putExtra("tabBarDesign", tabBarDesign);
                        intent.putExtra("firstStart", firstStart);
                        intent.putExtra("bottomBarDesign", bottomBarDesign);
                        intent.putExtra("flurry_id", flurryId);
                        intent.putExtra("appid", appId);
                        intent.putExtra("AppBuilder", AppBuilder);
                    }

                } catch (Exception e) {
                    LogError(e);
                }

                isExtends = false;

                try {
                    Class class2 = class1.getSuperclass();

                    while (true) {

                        if (class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModule")
                                || class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModuleMain")
                                || class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat")
                                || class1.getName().equals("com.ibuildapp.romanblack.CallPlugin.CallPlugin")
                                || class1.getName().equals("com.ibuildapp.romanblack.VideoPlugin.PlayerYouTubeActivity")
                                || class2.getName().equals("com.google.android.maps.MapActivity")
                                || class1.getName().equals("com.google.android.gms.ads.AdActivity")
                                || class1.getName().equals("com.google.android.youtube.PlayerActivity")
                                || class1.getName().equals("com.ibuildapp.romanblack.TableReservationPlugin.TableReservationPersonPicker")
                                || class1.getName().equals("com.ibuildapp.romanblack.CameraPlugin.chooser.ChooserActivity")
                                || class1.getName().equals("com.paypal.android.MEP.PayPalActivity")
                                || class1.getName().equals("com.ibuildapp.PayPalAndroidUtil.FakePaymentActivity")
                                || class1.getName().equals("com.ibuildapp.ZopimChatPlugin.ZopimChat")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.LoginActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentMethodActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentConfirmActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentCompletedActivity")
                                || className.equals("com.google.android.finsky.activities.LaunchUrlHandlerActivity")) {
                            isExtends = true;
                            break;
                        } else if (class2.getName().equals("java.lang.Object")) {
                            break;
                        }

                        class2 = class2.getSuperclass();

                    }
                } catch (NullPointerException e) {
                    LogError(e);

                    if(className.equals("com.google.android.finsky.activities.LaunchUrlHandlerActivity") || classNotFound){
                        isExtends = true;
                    }
                }
            }

            if(isShown) {
                final boolean _isExtends = isExtends;
                Animation animHideMenuStartActivity = new TranslateAnimation(0, Double.valueOf(screenWidth * 0.85).intValue(), 0, 0);
                animHideMenuStartActivity.setInterpolator(new SmoothInterpolator());
                animHideMenuStartActivity.setDuration(400);
                animHideMenuStartActivity.setFillEnabled(true);
                animHideMenuStartActivity.setAnimationListener(
                        new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                                // hide surfaceview objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.INVISIBLE);
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

                                // show surfaceview objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.VISIBLE);

                                if (_isExtends)
                                    AppBuilderModuleMainAppCompat.super.startActivityForResult(intent, 0);
                                else
                                    Toast.makeText(AppBuilderModuleMainAppCompat.this, "Your Activity should extend AppBuilderModule", Toast.LENGTH_LONG).show();
                            }
                        }
                );

                applySidebarAnimation(animHideMenuStartActivity);
                isShown = false;
            } else {
                if (isExtends)
                    super.startActivityForResult(intent, 0);
                else
                    Toast.makeText(AppBuilderModuleMainAppCompat.this, "Your Activity should extend AppBuilderModule", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            LogError(e);
        }
    }

    public void animateRootContainer(){
        inverseSidebarState();
    }

    @Override
    final public void startActivityForResult(final Intent intent, final int requestCode) {
        try {//ErrorLogging

            boolean isExtends = true;

            ComponentName cn = intent.resolveActivity(getPackageManager());
            String className = cn.getClassName();

            boolean classNotFound = false;

            Class class1 = null;
            try {
                class1 = Class.forName(className);
            } catch (ClassNotFoundException cNFEx) {
                LogError(cNFEx);

                classNotFound = true;
            }

            if (intent.getAction() == null) {
                try {
                    if(!className.equals("com.google.android.finsky.activities.LaunchUrlHandlerActivity") &&
                            !classNotFound){ // ÐºÐ¾ÑÑ‚Ñ‹Ð»ÑŒ Ð´Ð»Ñ ÑÑ…ÐµÐ¼ market:// , intent:// Ð¸ Ð¿Ñ€Ð¾Ñ‡Ð¸Ñ… Ð¿Ð¾Ð´Ð¾Ð±Ð½Ñ‹Ñ… ÑÑ…ÐµÐ¼

                        intent.putExtra("Advertisement", advData);
                        intent.putExtra("Widget", widget);
                        intent.putExtra("Widgets", widgets);
                        intent.putExtra("showSideBar", showSideBar);
                        intent.putExtra("navBarDesign", navBarDesign);
                        intent.putExtra("tabBarDesign", tabBarDesign);
                        intent.putExtra("firstStart", firstStart);
                        intent.putExtra("flurry_id", flurryId);
                        intent.putExtra("bottomBarDesign", bottomBarDesign);
                        intent.putExtra("appid", appId);
                        intent.putExtra("AppBuilder", AppBuilder);
                    }
                } catch (Exception e) {
                    LogError(e);
                }

                isExtends = false;

                try {
                    Class class2 = class1.getSuperclass();

                    while (true) {

                        if (class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModule")
                                || class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModuleMain")
                                || class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat")
                                || class1.getName().equals("com.ibuildapp.romanblack.CallPlugin.CallPlugin")
                                || class1.getName().equals("com.ibuildapp.romanblack.VideoPlugin.PlayerYouTubeActivity")
                                || class2.getName().equals("com.google.android.maps.MapActivity")
                                || class1.getName().equals("com.google.android.gms.ads.AdActivity")
                                || class1.getName().equals("com.google.android.youtube.PlayerActivity")
                                || class1.getName().equals("com.ibuildapp.romanblack.TableReservationPlugin.TableReservationPersonPicker")
                                || class1.getName().equals("com.ibuildapp.romanblack.CameraPlugin.chooser.ChooserActivity")
                                || class1.getName().equals("com.paypal.android.MEP.PayPalActivity")
                                || class1.getName().equals("com.ibuildapp.ZopimChatPlugin.ZopimChat")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.LoginActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentMethodActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentConfirmActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentCompletedActivity")
                                || className.equals("com.google.android.finsky.activities.LaunchUrlHandlerActivity")) {
                            isExtends = true;
                            break;
                        } else if (class2.getName().equals("java.lang.Object")) {
                            break;
                        }

                        class2 = class2.getSuperclass();

                    }
                } catch (NullPointerException e) {
                    LogError(e);

                    if(className.equals("com.google.android.finsky.activities.LaunchUrlHandlerActivity") || classNotFound){
                        isExtends = true;
                    }
                }
            }

            if(isShown) {
                final boolean _isExtends = isExtends;
                Animation animHideMenuStartActivity = new TranslateAnimation(0, Double.valueOf(screenWidth * 0.85).intValue(), 0, 0);
                animHideMenuStartActivity.setInterpolator(new SmoothInterpolator());
                animHideMenuStartActivity.setDuration(400);
                animHideMenuStartActivity.setFillEnabled(true);
                animHideMenuStartActivity.setAnimationListener(
                        new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                                // hide surfaceview objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.INVISIBLE);
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

                                // show surfaceview objects
                                for (View view : surfaceObjects)
                                    view.setVisibility(View.VISIBLE);

                                if (_isExtends)
                                    AppBuilderModuleMainAppCompat.super.startActivityForResult(intent, requestCode);
                                else
                                    Toast.makeText(AppBuilderModuleMainAppCompat.this, "Your Activity should extend AppBuilderModule", Toast.LENGTH_LONG).show();
                            }
                        }
                );

                applySidebarAnimation(animHideMenuStartActivity);
                isShown = false;
            } else {
                if (isExtends)
                    super.startActivityForResult(intent, requestCode);
                else
                    Toast.makeText(AppBuilderModuleMainAppCompat.this, "Your Activity should extend AppBuilderModule", Toast.LENGTH_LONG).show();
            }



        } catch (Exception e) {
            LogError(e);
        }
    }

    final public void setSession(Serializable session) {
        this.session = session;
    }

    final public Serializable getSession() {
        return session;
    }

    /* native features */
    final public void addNativeFeature(AppBuilderModuleMainAppCompat.NATIVE_FEATURES feature, Object parameter, Object value) {
        try {//ErrorLogging

            if (!nativeFeatures.containsKey(feature)) {
                HashMap<Object, Object> params = new HashMap<Object, Object>();
                if (feature.equals(AppBuilderModule.NATIVE_FEATURES.SMS)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.SMS.TEXT, hm.get("text"));
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.EMAIL)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.EMAIL.ADDRESS, null);
                    params.put(AppNativeFeature.EMAIL.SUBJECT, hm.get("subject"));
                    params.put(AppNativeFeature.EMAIL.TEXT, hm.get("text"));
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_CONTACT)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.CONTACT.NAME, hm.get("contactName"));
                    params.put(AppNativeFeature.CONTACT.PHONE, hm.get("contactNumber"));
                    params.put(AppNativeFeature.CONTACT.EMAIL, hm.get("contactEmail"));
                    params.put(AppNativeFeature.CONTACT.WEBSITE, hm.get("contactSite"));
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_EVENT)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.EVENT.TITLE, hm.get("title"));
                    params.put(AppNativeFeature.EVENT.BEGIN, hm.get("begin"));
                    params.put(AppNativeFeature.EVENT.END, hm.get("end"));
                    params.put(AppNativeFeature.EVENT.FREQUENCY, hm.get("frequency"));
                }
                nativeFeatures.put(feature, params);
            }

            if (nativeFeatures.containsKey(feature)) {
                if (nativeFeatures.get(feature).containsKey(parameter)) {
                    nativeFeatures.get(feature).put(parameter, value);
                }
            }

        } catch (Exception e) {
            LogError(e);
        }
    }

    final public void removeNativeFeature(AppBuilderModule.NATIVE_FEATURES feature) {
        if (nativeFeatures.containsKey(feature)) {
            nativeFeatures.remove(feature);
        }
    }

    /* AppBuilderInterface methods */
    @Override
    public void create() {
    }

    @Override
    public void start() {
    }

    @Override
    public void restart() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void destroy() {
    }

    protected final int getAdHeight() {
        return adView.getAdHeight();
    }

    protected final boolean hasAdView() {
        if (adView != null) {
            return true;
        } else {
            return false;
        }
    }

    public void onAdClosed() {
        if(adView != null)
            adView.closeView();

        adView = null;
        Statics.isAdClosed = true;
    }

    protected final String getAdvType() {
        return advData.getAdvType();
    }

    /* PRIVATE METHODS */
    private void readConfiguration() {

    }

    private void writeConfiguration() {

    }

    /* super power */
    private void sendSMS() {
        try {

            if (nativeFeatures.containsKey(AppBuilderModule.NATIVE_FEATURES.SMS)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                try {
                    CharSequence text = "";
                    if (nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.SMS).containsKey(AppNativeFeature.SMS.TEXT)) {
                        text = (CharSequence) nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.SMS).get(AppNativeFeature.SMS.TEXT);
                    }
                    intent.putExtra("sms_body", text);
                } catch (Exception e) {
                    LogError(e);
                }
                startActivity(intent);
            }

        } catch (Exception e) {
            LogError(e);
        }
    }

    private void sendEmail() {
        try {

            if (nativeFeatures.containsKey(AppBuilderModule.NATIVE_FEATURES.EMAIL)) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/html");
                if (nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).containsKey(AppNativeFeature.EMAIL.TEXT)) {
                    try {
                        String text;
                        text = (String) nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).get(AppNativeFeature.EMAIL.TEXT);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(text));
                    } catch (Exception e) {
                        LogError(e);
                    }
                }
                if (nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).containsKey(AppNativeFeature.EMAIL.SUBJECT)) {
                    try {
                        String subject;
                        subject = (String) nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).get(AppNativeFeature.EMAIL.SUBJECT);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    } catch (Exception e) {
                        LogError(e);
                    }
                }

                startActivity(Intent.createChooser(emailIntent, "Email:"));
            }

        } catch (Exception e) {
            LogError(e);
        }
    }

    private void addContact() {
        try {//ErrorLogging

            if (!nativeFeatures.containsKey(NATIVE_FEATURES.ADD_CONTACT)) {
                return;
            }

            class Contact {
                protected int type;
                protected String value;

                public Contact(int type, String value) {
                    this.type = type;
                    this.value = value;
                }

                public int getType() {
                    return type;
                }

                public String getDescription() {
                    return value;
                }
            }

            ArrayList<Contact> contacts1 = new ArrayList<Contact>();
            String s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.NAME);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(0, s));
            }
            s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.PHONE);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(1, s));
            }
            s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.EMAIL);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(2, s));
            }
            s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.WEBSITE);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(3, s));
            }
            final ArrayList<Contact> contacts = contacts1;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String name = contacts.get(0).getDescription();

            try {
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        new String[]{ContactsContract.Contacts._ID,
                                ContactsContract.Contacts.DISPLAY_NAME},
                        ContactsContract.Contacts.DISPLAY_NAME + " = ?",
                        new String[]{name}, null);

                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    final String contactId = cursor.getString(0);
                    final String contactName = cursor.getString(1);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("The contact is already in your address book.");
                    builder.setMessage("Do you want to replace it?");
                    builder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {
                                    ArrayList<ContentProviderOperation> ops =
                                            new ArrayList<ContentProviderOperation>();
                                    for (int i = 0; i < contacts.size(); i++) {
                                        int type = contacts.get(i).getType();

                                        ContentResolver cr = getContentResolver();
                                        Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, null,
                                                null, null, null);

                                        boolean have = false;
                                        while (cur.moveToNext()) {
                                            for (int i1 = 0; i1 < cur.getColumnCount(); i1++) {
                                                if (cur.getString(i1) != null) {

                                                    if (cur.getString(i1).equals(contacts.get(i).getDescription())) {
                                                        have = true;
                                                        if (ops.isEmpty()) {
                                                            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                                                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                                                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                                                    .build());

                                                            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                                                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                                                                    .build());
                                                        }
                                                    }

                                                }
                                            }
                                        }

                                        if (!have) {

                                            switch (type) {
                                                case 0: {
                                                }
                                                break;
                                                case 1: {
                                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)//new Integer(contactId).intValue())
                                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contacts.get(i).getDescription())
                                                            .build());
                                                }
                                                break;
                                                case 2: {

                                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)//new Integer(contactId).intValue())
                                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                                                            .withValue(ContactsContract.CommonDataKinds.Email.DATA1, contacts.get(i).getDescription())
                                                            .build());

                                                }
                                                break;
                                                case 3: {

                                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)//new Integer(contactId).intValue())
                                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                                                            .withValue(ContactsContract.CommonDataKinds.Website.URL, contacts.get(i).getDescription())
                                                            .build());

                                                }
                                                break;
                                                case 4: {
                                                }
                                                break;
                                            }

                                        }
                                    }
                                    try {
                                        ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                                    } catch (RemoteException | OperationApplicationException e) {
                                        LogError(e);
                                    }
                                }
                            });
                    builder.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                }
                            });
                    builder.create().show();
                } else {

                    try {
                        int rawContactInsertIndex = ops.size();
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                .build());

                        for (int i = 0; i < contacts.size(); i++) {
                            int type = contacts.get(i).getType();
                            switch (type) {
                                case 0: {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 1: {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 2: {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Email.DATA1, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 3: {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Website.URL, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 4: {
                                }
                                break;
                            }
                        }
                        ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                        Toast.makeText(AppBuilderModuleMainAppCompat.this,
                                "The contact has beed saved into your address "
                                        + "book.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        LogError(e);
                    }

                }
            } catch (Exception e) {
                LogError(e);
            }

        } catch (Exception e) {
            LogError(e);
        }
    }

    private void addEvent() {
        try {//ErrorLogging

            if (nativeFeatures.containsKey(NATIVE_FEATURES.ADD_EVENT)) {
                String title = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.TITLE);
                String begTime = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.BEGIN);
                String endTime = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.END);
                String rRule = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.FREQUENCY);

                ContentResolver cr = getContentResolver();

                Uri.Builder builder = Uri.parse(
                        "content://com.android.calendar/instances/when")
                        .buildUpon();
                Long time = new Date(begTime).getTime();
                ContentUris.appendId(builder, time - 10 * 60 * 1000);
                ContentUris.appendId(builder, time + 10 * 60 * 1000);

                String[] projection = new String[]{
                        "title", "begin"};
                Cursor cursor = cr.query(builder.build(),
                        projection, null, null, null);

                boolean exists = false;
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        if ((time == cursor.getLong(1)) &&
                                title.equals(cursor.getString(0))) {
                            exists = true;
                        }
                    }
                }

                if (!exists) {
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra("beginTime", time);
                    intent.putExtra("allDay", false);
                    intent.putExtra("endTime", time + 60 * 60 * 1000);
                    intent.putExtra("title", title);
                    startActivity(intent);
                } else {
                    Toast.makeText(AppBuilderModuleMainAppCompat.this,
                            "Event already exist!", Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            LogError(e);
        }
    }

    private void getSufrafeObjects(View source) {
        if (source instanceof ViewGroup) {
            int childCount = ((ViewGroup) source).getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (((ViewGroup) source).getChildAt(i) instanceof ViewGroup) {
                    getSufrafeObjects(((ViewGroup) source).getChildAt(i));
                } else {
                    if (((ViewGroup) source).getChildAt(i) instanceof SurfaceView)
                        surfaceObjects.add(((ViewGroup) source).getChildAt(i));
                }
            }
        }
        if (source instanceof SurfaceHolder)
            surfaceObjects.add(source);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrix = new DisplayMetrics();
        display.getMetrics(metrix);
        screenWidth = metrix.widthPixels;

        animShowMenu = new TranslateAnimation(0, -(Double.valueOf(screenWidth * 0.85).intValue()), 0, 0);
        animShowMenu.setInterpolator(new SmoothInterpolator());
        animShowMenu.setDuration(400);
        animShowMenu.setFillEnabled(true);
        animShowMenu.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // hide surfaceview objects
                        for (View view : surfaceObjects)
                            view.setVisibility(View.INVISIBLE);
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

                        // show surfaceview objects
                        for (View view : surfaceObjects)
                            view.setVisibility(View.VISIBLE);
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
                        // hide surfaceview objects
                        for (View view : surfaceObjects)
                            view.setVisibility(View.INVISIBLE);
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

                        // show surfaceview objects
                        for (View view : surfaceObjects)
                            view.setVisibility(View.VISIBLE);
                    }
                }
        );

        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(Double.valueOf(screenWidth * 0.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
        menuContainer.setLayoutParams(par);

        par = new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        userContainer.setLayoutParams(par);

        if (isShown) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Double.valueOf(screenWidth * 1.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.LEFT;
            params.setMargins(-(Double.valueOf(screenWidth * 0.85).intValue()), 0, 0, 0);
            rootContainer.setLayoutParams(params);
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Double.valueOf(screenWidth * 1.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.LEFT;
            params.setMargins(0, 0, 0, 0);
            rootContainer.setLayoutParams(params);
        }

        if (advData != null && !Statics.isAdClosed) {
            if (advData.getAdvType().length() > 0) {
                adLayout.removeView(adView);
                adView = new AppAdvView(this, advData, firstStart);
                adView.setOnAdClosedListener(this);
                adLayout.addView(adView);
                setTokenBanner();
                adView.loadAdMob();
            }
        }
    }

    public class SmoothInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float v) {
            return (float) Math.pow(v - 1, 3) + 1;
        }
    }

    @Override
    public void drawTopBarRightButton(View view) {
        if (view != null) {
            try {
                topBarRightButton.removeAllViews();
                topBarRightButton.addView(view);
                topBarRightButton.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                LogError(e);
            }

        }
    }

    @Override
    public void drawTopBarLeftButton(View view) {
        if (view != null) {
            try {
                topBarLeftButton.removeAllViews();
                topBarLeftButton.addView(view);
                topBarLeftButton.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                LogError(e);
            }
        }
    }

    public void hideTopBarRightButton() {
        if(1 < actualWidgetList.size() && actualWidgetList.get(1).getOrder() < -1) {
            actualWidgetList.get(0).setHidden(true);
            actualWidgetList.get(1).setHidden(true);

            Adapter adapter = widgetList.getAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    public void showTopBarRightButton() {
        if(1 < actualWidgetList.size() && actualWidgetList.get(1).getOrder() < -1) {
            actualWidgetList.get(0).setHidden(false);
            actualWidgetList.get(1).setHidden(false);

            Adapter adapter = widgetList.getAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    public int setTopBarRightButton(View view, String title, View.OnClickListener listener){
        if (showSideBar) {
            Widget widget = new Widget();
            widget.setOrder(Statics.getSidebarNonWidgetClickListenerIndex());
            widget.setLabel(title);
            widget.setAddToSidebar(true);
            Statics.sidebarClickListeners.put(widget.getOrder(), listener);
            //separator, must be added before any non-widget sidebar button
            actualWidgetList.add(0, new Widget() {{
                setAddToSidebar(true);
            }});
            actualWidgetList.add(0, widget);
            return widget.getOrder();
        }
        else setTopBarRightVeiw(view, title, false, listener);
        return Integer.MIN_VALUE;
    }
    public void updateWidgetInActualList(int index, String newTitle){
        for (Widget widget:actualWidgetList)
            if (widget.getOrder()==index)
                widget.setLabel(newTitle);
        Adapter adapter = widgetList.getAdapter();
        if (adapter instanceof BaseAdapter)
            ((BaseAdapter)adapter).notifyDataSetChanged();
    }

    private void setTopBarRightVeiw(View view, String text, boolean showArrow, View.OnClickListener clickHandler) {
        try {
            //
            topBarRightButton.removeAllViews();

            LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(params);

            if(view != null) {
                LinearLayout.LayoutParams viewLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layout.addView(view, viewLP);
            } else {
                LinearLayout textButton = createTextButton(Gravity.RIGHT);
                TextView textView = (TextView) textButton.getChildAt(1);
                ImageView arrowView = (ImageView) textButton.getChildAt(0);
                textView.setTextColor(navBarDesign.itemDesign.textColor);
                arrowView.setVisibility(View.GONE);
                textView.setText(text);
                layout.addView(textButton);
            }

            if (clickHandler != null)
                topBarRightButton.setOnClickListener(clickHandler);
            topBarRightButton.addView(layout);
            topBarRightButton.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            LogError(e);
        }
    }

    @Override
    public void setTopBarRightButtonText(String text, boolean showArrow, View.OnClickListener clickHandler) {
        setTopBarRightButton(null, text, clickHandler);
    }

    public void setTopBarLeftButtonTextAndColor (String text, int color, boolean showArrow, View.OnClickListener clickHandler)
    {
        try {
            topBarLeftButton.removeAllViews();
            LinearLayout layout = createTextButton(Gravity.LEFT);
            TextView textView = (TextView) layout.getChildAt(1);
            ImageView arrowView = (ImageView) layout.getChildAt(0);

            textView.setTextColor(color);
            Drawable d = arrowView.getBackground();
            d.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            arrowView.setBackgroundDrawable(d);

            textView.setText(text);

            if (!showArrow)
                arrowView.setVisibility(View.GONE);

            if (clickHandler != null)
                topBarLeftButton.setOnClickListener(clickHandler);
            topBarLeftButton.addView(layout);
            topBarLeftButton.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            LogError(e);
        }
    }

    @Override
    public void setTopBarLeftButtonText(String text, boolean showArrow, View.OnClickListener clickHandler) {
        setTopBarLeftButtonTextAndColor(text, navBarDesign.itemDesign.textColor, showArrow, clickHandler);
    }

    @Override
    public void swipeBlock() {
    }

    @Override
    public void setTopBarRightButtonOnClickListener(View.OnClickListener l) {
        topBarRightButton.setOnClickListener(l);
    }

    @Override
    public void setTopBarLeftButtonOnClickListener(View.OnClickListener l) {
        try {
            topBarLeftButton.setOnClickListener(l);
        } catch (NullPointerException nPEx) {
            LogError(nPEx);
        }
    }
    public void setTopBarBackgroundColor(final int color) {
        int backColor;
        if (color == Color.WHITE)
            backColor = Color.parseColor("#33000000");
        else if (color == Color.BLACK)
            backColor = Color.parseColor("#80FFFFFF");
        else backColor = Color.parseColor("#33FFFFFF");

        header.setBackgroundDrawable(new LayerDrawable(new Drawable[]{
                new ColorDrawable(color),
                new ColorDrawable(backColor )
        }));
        header.setPadding((int) (density * 10), (int) (density * 10), (int) (density * 10), (int) (density * 10));
    }

    @Override
    public void setTopBarBackground(int id) {
        if (id > 0)
            topBar.setBackgroundResource(id);
    }

    @Override
    public void setTopBarBackground(Bitmap src) {
        if (src != null) {
            BitmapDrawable temp = new BitmapDrawable(src);
            topBar.setBackgroundDrawable(temp);
        }
    }

    @Override
    public void setTopBarTitle(String title) {
        if (title != null )
            try{
                topBarTitle.setText(title);
            }catch(Exception ex){
                LogError(ex);
            }
    }

    @Override
    public void setTopBarTitleColor(int color) {
        topBarTitle.setTypeface(null, Typeface.NORMAL);
        topBarTitle.setTextColor(color);
        if (topBarRightButton instanceof TopBarHamburger)
        {
            TopBarHamburger hamburger = (TopBarHamburger) topBarRightButton;
            hamburger.setBlack();
        }
    }

    public void setTopbarTitleTypeface(int normal) {
        topBarTitle.setTypeface(null, normal);
    }

    @Override
    public void hideTopBar() {
        try {
            topBar.setVisibility(View.GONE);
        } catch (NullPointerException nPEx) {
            LogError(nPEx);
        }
    }

    @Override
    public void invisibleTopBar() {
        try {
            topBar.setVisibility(View.INVISIBLE);
        } catch (NullPointerException nPEx) {
            LogError(nPEx);
        }
    }

    @Override
    public void visibleTopBar() {
        try {
            topBar.setVisibility(View.VISIBLE);
        } catch (NullPointerException nPEx) {
            LogError(nPEx);
        }
    }

    protected final View getTopBar(){
        return topBar;
    }

    public View popTopBar() {
        userContainer.removeView(topBar);
        return topBar;
    }

    public void returnTopBar() {
        ViewGroup parent = (ViewGroup)topBar.getParent();
        if(parent == userContainer)
            return;
        if(parent != null)
            parent.removeView(topBar);
        userContainer.removeView(topBar);
    }

    @Override
    public void drawTopBarTitleView(View view, int gravity) throws NullPointerException {
        if (view != null) {
            titleHolder.removeAllViews();
            if (gravity != -1 || gravity != 0)
                titleHolder.setGravity(gravity);
            else
                titleHolder.setGravity(Gravity.CENTER);
            titleHolder.addView(view);
        } else
            throw new NullPointerException("View object is null");
    }

    @Override
    public void designButton(TextView text, BarDesigner.TitleDesign designer) {
        text.setTextColor(designer.textColor);
        text.setTextSize(designer.fontSize);
        if (designer.fontWeight.compareTo("bold") == 0)
            text.setTypeface(null, Typeface.BOLD);
        else if (designer.fontWeight.compareTo("italic") == 0)
            text.setTypeface(null, Typeface.ITALIC);
        else if (designer.fontWeight.compareTo("") == 0 || designer.fontWeight.length() == 0)
            text.setTypeface(null, Typeface.NORMAL);

        if (designer.textAlignment.compareTo("left") == 0)
            text.setGravity(Gravity.LEFT);
        else if (designer.textAlignment.compareTo("center") == 0)
            text.setGravity(Gravity.CENTER_HORIZONTAL);
        else if (designer.textAlignment.compareTo("right") == 0)
            text.setGravity(Gravity.RIGHT);
    }

    @Override
    public void setTitleLineAmount(int maxLines) {
        if (maxLines > 0) {
            if (maxLines > 2)
                topBarTitle.setMaxLines(2);
            else
                topBarTitle.setMaxLines(maxLines);
        } else
            topBarTitle.setMaxLines(2);
    }

    @Override
    public void disableSwipe() {
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

    public void hideSidebar() {
        applySidebarAnimation(animHideMenu);
        isShown = false;
        rootContainer.disableSwipe();
    }

    private void inverseSidebarState() {
        if(showSideBar) {
            if(isShown) {
                hideSidebar();
            } else {
                showSidebar();
            }
        }
    }

    // ***********************************************************************************
    // create layouts
    private LinearLayout create_main_layout() {

        LinearLayout root = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.setLayoutParams(params);
        root.setOrientation(LinearLayout.VERTICAL);

        adLayout = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        adLayout.setLayoutParams(params);
        adLayout.setOrientation(LinearLayout.VERTICAL);

        rootFrameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams paramsFr = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootFrameLayout.setLayoutParams(paramsFr);

        root.addView(rootFrameLayout);

        return root;
    }

    private LinearLayout createTextButton(int position) {
        LinearLayout root = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (position == Gravity.LEFT)
            root.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        else
            root.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setLayoutParams(params);

        ImageView arrow = new ImageView(this);
        params = new LinearLayout.LayoutParams((int) (density * ARROW_WIDTH), (int) (density * ARROW_HEIGHT));
        arrow.setBackgroundResource(R.drawable.api_topbar_arrow);
        arrow.setLayoutParams(params);

        TextView text = new TextView(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (5 * density), 0, 0, 0);
        text.setLayoutParams(params);

        text.setSingleLine();
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, navBarDesign.itemDesign.fontSize);
        text.setTextColor(navBarDesign.itemDesign.textColor);

        if (navBarDesign.itemDesign.fontWeight.compareToIgnoreCase("bold") == 0)
            text.setTypeface(Typeface.DEFAULT_BOLD);

        root.addView(arrow);
        root.addView(text);

        return root;
    }

    private LinearLayout createTopbarLayout() {
        try{
            LinearLayout root = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setLayoutParams(params);

            // root header layout
            header = new LinearLayout(this);
            header.setPadding((int) (density * 10), (int) (density * 10), (int) (density * 10), (int) (density * 10));
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setBackgroundColor(navBarDesign.color); //Ð²Ð¾Ñ‚ Ð¾Ð½!Color.parseColor("#FFFFFF"));//
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * 50));
            params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            header.setLayoutParams(params);


            // left button
            topBarLeftButton = new LinearLayout(this);
            topBarLeftButton.setGravity(Gravity.LEFT);
            topBarLeftButton.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 5;//4;
            params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            topBarLeftButton.setLayoutParams(params);
            topBarLeftButton.setVisibility(View.VISIBLE);

            // module title
            titleHolder = new LinearLayout(this);
            titleHolder.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 4;//3;
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            titleHolder.setLayoutParams(params);


            topBarTitle = new TextView(this);
            topBarTitle.setLines(1);
            topBarTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, navBarDesign.titleDesign.fontSize);
            topBarTitle.setTextColor(navBarDesign.titleDesign.textColor);
            topBarTitle.setText("Title");
            topBarTitle.setSingleLine(true);
            topBarTitle.setEllipsize(TextUtils.TruncateAt.END);

            if (navBarDesign.titleDesign.textAlignment.compareTo("left") == 0)
                topBarTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            else if (navBarDesign.titleDesign.textAlignment.compareTo("center") == 0)
                topBarTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            else if (navBarDesign.titleDesign.textAlignment.compareTo("right") == 0)
                topBarTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            if (navBarDesign.titleDesign.fontWeight.compareToIgnoreCase("bold") == 0)
                topBarTitle.setTypeface(Typeface.DEFAULT_BOLD);

            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            topBarTitle.setLayoutParams(params);
            setTopbarTitleTypeface(Typeface.NORMAL);
            titleHolder.addView(topBarTitle);

            // right button
            if (showSideBar){
                topBarRightButton = new TopBarHamburger(this, navBarDesign);
                topBarRightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inverseSidebarState();
                    }
                });
                topBarRightButton.setVisibility(showSideBar ? View.VISIBLE : View.INVISIBLE);
            }
            else{
                topBarRightButton = new LinearLayout(this);
                topBarRightButton.setOrientation(LinearLayout.HORIZONTAL);
                topBarRightButton.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 5;//4;
                params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                topBarRightButton.setLayoutParams(params);
                topBarRightButton.setVisibility(View.INVISIBLE);
            }

            header.addView(topBarLeftButton);
            header.addView(titleHolder);
            header.addView(topBarRightButton);
            root.addView(header);

            return root;
        }catch(Exception ex){
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * 50));
            LinearLayout result = new LinearLayout(this);
            result.addView(new View(this), lp);
            return result;
        }
    }
    public static int MergeColors(int backgroundColor, int foregroundColor) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL   = 16;
        final byte GREEN_CHANNEL =  8;

        final double ap1 = (double)(backgroundColor >> ALPHA_CHANNEL & 0xff) / 255d;
        final double ap2 = (double)(foregroundColor >> ALPHA_CHANNEL & 0xff) / 255d;
        final double ap = ap2 + (ap1 * (1 - ap2));

        final double amount1 =(ap1 * (1 - ap2)) / ap;
        final double amount2 = amount1 / ap;

        int a = ((int)(ap * 255d)) & 0xff;

        int r = ((int)(((float)(backgroundColor >> RED_CHANNEL & 0xff )*amount1) +
                ((float)(foregroundColor >> RED_CHANNEL & 0xff )*amount2))) & 0xff;
        int g = ((int)(((float)(backgroundColor >> GREEN_CHANNEL & 0xff )*amount1) +
                ((float)(foregroundColor >> GREEN_CHANNEL & 0xff )*amount2))) & 0xff;
        int b = ((int)(((float)(backgroundColor & 0xff )*amount1) +
                ((float)(foregroundColor & 0xff )*amount2))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b;
    }

    protected void setTitleView(View view) {
        titleHolder.removeAllViews();
        titleHolder.addView(view);
    }

    // *******************************************************************************
    // resize image according to width and height and round corners and save it to file
    // "cachePath + md5(url)" file
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
                LogError(ex);
            } catch (OutOfMemoryError e) {
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
                } catch (Exception ex) {
                    LogError(ex);
                } catch (OutOfMemoryError ex) {
                    Log.e(TAG, "OutOfMemoryError", ex);
                }
            }
        } catch (Exception e) {
            LogError(e);
            return null;
        }

        return bitmap;
    }

    public void forceCloseModule(int order) {
        Intent result = new Intent();
        result.putExtra(Statics.FORCE_CLOSE_MODULE_FLAG, Statics.FORCE_CLOSE_MODULE);
        result.putExtra(Statics.FORCE_CLOSE_NEW_MODULE_ORDER, order);
        setResult(RESULT_OK, result);
        finish();
    }

    public void forceCloseApp() {
        Intent result = new Intent();
        result.putExtra(Statics.FORCE_CLOSE_APP_FLAG, Statics.FORCE_CLOSE_MODULE);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            String res = data.getStringExtra(Statics.FORCE_CLOSE_MODULE_FLAG);
            if (!TextUtils.isEmpty(res)) {
                if (res.compareToIgnoreCase(Statics.FORCE_CLOSE_MODULE) == 0) {
                    forceCloseModule(data.getIntExtra(Statics.FORCE_CLOSE_NEW_MODULE_ORDER, -1));
                }
            }

            String resCloseApp = data.getStringExtra(Statics.FORCE_CLOSE_APP_FLAG);
            if (!TextUtils.isEmpty(resCloseApp)) {
                forceCloseApp();
            }
        }

        if(Statics.isAdClosed)
            onAdClosed();
    }

    private void createPushDialog() {
        Log.e("createPushDialog - AppBuilderModuleMain", "createPushDialog");
        final AppPushNotificationMessage freshMessage = AppPushNotificationDB.getNotificationIfExist();
        if (freshMessage != null) {
            if (!isDialogShowen) {
                AppPushNotificationDialogLayout dialog;
                if (freshMessage.isPackageExist) {
                    dialog = new AppPushNotificationDialogLayout(
                            AppBuilderModuleMainAppCompat.this,
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
                                    forceCloseModule(freshMessage.widgetOrder);
                                }
                            }
                    );
                } else {
                    if (freshMessage.widgetOrder == -1)
                    {
                        dialog = new AppPushNotificationDialogLayout(
                                AppBuilderModuleMainAppCompat.this,
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
                                null);
                    } else {
                        dialog = new AppPushNotificationDialogLayout(
                                AppBuilderModuleMainAppCompat.this,
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

                dialogHolder.removeAllViews();
                dialogHolder.addView(dialog);
                dialogHolder.setVisibility(View.VISIBLE);
                isDialogShowen = true;
                dialogHolder.clearAnimation();
                dialogHolder.startAnimation(animShowDialog);


                AppPushNotificationDB.deleteItemFromRelations(freshMessage.uid);
                mManager.cancel((int) freshMessage.uid);
                Log.e("NOTIFICATION - AppBuilderModuleMain", "We have message. Text = " + freshMessage.descriptionText + "ImgPath = " + freshMessage.imagePath);
            }
        } else {
            Log.e("NOTIFICATION - AppBuilderModuleMain", " no message found");
        }
    }

    private LinearLayout generateFavouriteLayout()
    {
        LinearLayout root = new LinearLayout(AppBuilderModuleMainAppCompat.this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams( new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // generating back btn
        LinearLayout backHolder = new LinearLayout(AppBuilderModuleMainAppCompat.this);
        backHolder.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                forceCloseApp();
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (40*density), (int) (40*density));
        params.setMargins((int) (-2*density), 0 ,0,0);
        backHolder.setLayoutParams( params );
        backHolder.setGravity(Gravity.CENTER);

        GradientDrawable holderBack = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.parseColor("#4c000000"),
                Color.parseColor("#4c000000")});
        holderBack.setGradientType(GradientDrawable.RECTANGLE);
        holderBack.setCornerRadii(new float[]{0, 0, 5 * density, 5 * density, 5 * density, 5 * density, 0, 0});
        holderBack.setStroke((int) (density * 1), Color.parseColor("#4cffffff"));
        backHolder.setBackgroundDrawable(holderBack);

        ImageView backImage = new ImageView(AppBuilderModuleMainAppCompat.this);
        params = new LinearLayout.LayoutParams((int) (25*density), (int) (25*density));
        params.setMargins((int) (-3 * density), 0,0,0);
        backImage.setLayoutParams(params);

        backImage.setImageResource(R.drawable.back_from_app);
        backHolder.addView(backImage);
        root.addView(backHolder);


        //*******************************
        // generate fav button
        LinearLayout favHolder = new LinearLayout(AppBuilderModuleMainAppCompat.this);
        favHolder.setBackgroundDrawable(holderBack);
        params = new LinearLayout.LayoutParams((int) (40*density), (int) (40*density));
        params.setMargins((int) (-2 * density), (int) (15 * density), 0, 0);
        favHolder.setLayoutParams(params);
        favHolder.setGravity(Gravity.CENTER);
        root.addView(favHolder);

        CheckBox checkBox = new CheckBox(AppBuilderModuleMainAppCompat.this);
        checkBox.setLayoutParams( new LinearLayout.LayoutParams((int) (25 * density), (int) (25 * density)));
        checkBox.setChecked(Statics.favouritedMasterApp);
        checkBox.setButtonDrawable(new BitmapDrawable());

        StateListDrawable stateList = new StateListDrawable();

        stateList.addState(new int[]{android.R.attr.state_checked}, getResources().getDrawable(R.drawable.favourits_added));
        stateList.addState(new int[]{}, getResources().getDrawable(R.drawable.favourits_removed));

        checkBox.setBackgroundDrawable(stateList);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Statics.favouritedMasterApp = isChecked;
                Intent messageIntent = new Intent(Statics.FAVOURITES_BROADCAST);
                messageIntent.putExtra("appid", Statics.appId);
                messageIntent.putExtra("favourites", isChecked);
                sendBroadcast(messageIntent);
            }
        });

        favHolder.addView(checkBox);

        return root;
    }

    protected String readXmlFromFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            LogError(e);
            return null;
        } catch (IOException e) {
            LogError(e);
            return null;
        }

        return stringBuilder.toString();
    }

    public DialogSharing showDialogSharing(DialogSharing.Configuration configuration) {
        DialogSharing sharing = new DialogSharing(this, configuration);
        sharing.show();
        return sharing;
    }

    @Override
    public void finish() {
        Integer token = getPrefsInfoToken();
        updateTokenViewFromAdd(token);
        if(getClass().getCanonicalName().equals(widget.getPluginPackage() + "." + widget.getPluginName()) && !TextUtils.isEmpty(AppBuilder)) {
            try {
                Intent intent = new Intent();
                intent.setClassName(AppBuilder.substring(0, AppBuilder.lastIndexOf(".AppBuilder")), AppBuilder);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                super.startActivityForResult(intent, -1);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else
            super.finish();

    }

    @Override
    public void onBackPressed() {
        Integer token = getPrefsInfoToken();
        updateTokenViewFromAdd(token);
        if(showSideBar && isShown) {
            hideSidebar();
        } else
            super.onBackPressed();

    }

    public boolean isShowSideBar() {
        return showSideBar;
    }

}