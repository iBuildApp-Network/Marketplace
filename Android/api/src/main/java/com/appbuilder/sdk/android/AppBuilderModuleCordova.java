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

import static android.app.Activity.RESULT_OK;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appbuilder.sdk.android.AppBuilderModuleMain.SmoothInterpolator;
import static com.appbuilder.sdk.android.AppBuilderModuleMain.TAG;
import com.appbuilder.sdk.android.pushnotification.AppPushNotificationDB;
import com.appbuilder.sdk.android.pushnotification.AppPushNotificationDialogLayout;
import com.appbuilder.sdk.android.pushnotification.AppPushNotificationMessage;
import com.appbuilder.sdk.android.view.TopBarHamburger;
import com.appbuilder.statistics.StatisticsCollector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.cordova.CordovaActivity;

/* To-Do peredelat vse v pizdu*/
/**
 *
 * @author Roman Black
 */
public class AppBuilderModuleCordova extends CordovaActivity implements AppBuilderInterface, AppAdvView.OnAdClosedListener{

    final private int FIND_SURFACEOBJECTS = 5;

    final private int ARROW_WIDTH = 15;
    final private int ARROW_HEIGHT = 25;

    private final String WIDGET_HOLDER_BACKGROUND = "#3f434b";

    private final String WIDGET_TOPBAR_BACKGROUND = "#32363c";

//    private final String TOPBAR_SHOW_MENU_PARH = "/com/appbuilder/sdk/android/res/api_show_bar.png";
//    private final String TOPBAR_ARROW_PARH = "/com/appbuilder/sdk/android/res/api_topbar_arrow.png";

    private int screenWidth;
    private float density;
    private LayoutInflater layoutInflater = null;
    private View userLayout = null;
    private LinearLayout rootRoot;
    private LinearLayout adLayout;
    private FrameLayout rootFrameLayout;
    protected Bundle state = null;
    private Serializable session = null;
    private String className;
    private AppAdvData advData = null;
    private AppAdvView adView = null;
    private boolean firstStart;
    private String flurryId = "";
    private Widget widget = null;
    private ArrayList<Widget> widgets;
    protected BarDesigner navBarDesign;
    private BarDesigner tabBarDesign;
    protected BarDesigner bottomBarDesign;
    private boolean showSideBar = false;
    private String appId;
    private Intent resultData;
    private BroadcastReceiver broadcastReceiver;
    private boolean foreground;
    private NotificationManager mManager;
    private SwipeLinearLayout rootContainer;
    private LinearLayout userContainer;
    private LinearLayout menuContainer;
    private LinearLayout homeButton;
    private TranslateAnimation animShowMenu;
    private TranslateAnimation animHideMenu;
    private AlphaAnimation animHideImage;
    private AlphaAnimation animShowImage;
    private AlphaAnimation animShowDialog;
    private AlphaAnimation animHideDialog;
    private boolean isShown = false;
    private ArrayList<Widget> actualWidgetList = new ArrayList<Widget>();
    private ListView widgetList;
    private ArrayList<Bitmap> thumbnails = new ArrayList<Bitmap>();
    private LinearLayout topBar;
    private LinearLayout topBarLeftButton;
    private LinearLayout topBarRightButton;
    private TextView topBarTitle;
    private LinearLayout titleHolder;
    private LinearLayout dialogHolder;
    private ArrayList<View> surfaceObjects = new ArrayList<View>();
    private OnSwipeInterface swipeInterface;
    private boolean swipeBlock = false;
    private boolean isDialogShowen = false;
    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case FIND_SURFACEOBJECTS: {
                    getSufrafeObjects((View) message.obj);
                }
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            // *******************************************************************************
            // computing display width and height
            Display display = getWindowManager().getDefaultDisplay();
            screenWidth = display.getWidth();
            density = getResources().getDisplayMetrics().density;

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);

            layoutInflater = LayoutInflater.from(this);
            rootRoot = create_main_layout();
            state = savedInstanceState;
            try {
                session = savedInstanceState.getSerializable("session");
            } catch (Exception e) {
            }

            try {
                Intent currentIntent = getIntent();
                className = currentIntent.getComponent().getClassName();
                Bundle store = currentIntent.getExtras();
                advData = (AppAdvData) store.getSerializable("Advertisement");
                firstStart = store.getBoolean("firstStart");

                if (advData != null) {
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
                try{
                    showSideBar = (Boolean) store.getBoolean("showSideBar", false);
                }catch(Exception ex){
                }
                appId = store.getString("appid");
                Log.e(TAG + "- AppBuilderModuleMain", "Appid = " + appId);

                // ***************************************************************************************
                AppPushNotificationDB.init(this);

                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.e(TAG + "- AppBuilderModuleMain", "NOTIFICATON RECEIVED");
                        if (foreground)
                            createPushDialog();
                    }
                };
                IntentFilter intFilt = new IntentFilter(appId);
                registerReceiver(broadcastReceiver, intFilt);
                mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


// ***************************************************************************************
// 05.06.2013 Brazhnik
// onCreate modification
// ***************************************************************************************
                // creating root container
                rootContainer = new SwipeLinearLayout(this, showSideBar);
                rootContainer.setOrientation(LinearLayout.HORIZONTAL);

                // creating two child view: user's view and menu view
                menuContainer = new LinearLayout(this);
                menuContainer.setOrientation(LinearLayout.VERTICAL);
                menuContainer.setBackgroundColor(Color.parseColor(WIDGET_HOLDER_BACKGROUND));

                LinearLayout.LayoutParams par = null;
                if (showSideBar) {
                    LinearLayout menuTopBar = create_menu_topbar_layout();
                    par = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    menuTopBar.setVisibility(View.GONE);
//                    homeButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if (className.compareTo("com.appbuilder.core.AppBuilder") == 0) {
//                                // this is main activity -> just close sidebar
//                                rootContainer.clearAnimation();
//                                rootContainer.startAnimation(animHideMenu);
//                                isShown = false;
//                            } else {
//                                setResult(RESULT_CANCELED);
//                                finish();
//                            }
//                        }
//                    });

                    // set adapter to listview
                    widgetList = new ListView(this);
                    widgetList.setBackgroundColor(Color.parseColor(WIDGET_HOLDER_BACKGROUND));
                    widgetList.setCacheColorHint(Color.parseColor(WIDGET_HOLDER_BACKGROUND));
                    widgetList.setVerticalScrollBarEnabled(false);
                    widgetList.setDivider(null);
                    widgetList.setDividerHeight(0);

                    addMasterappActions();

                    int onlyMasterappItems = actualWidgetList.size();

                    for (int i = 0; i < widgets.size(); i++)
                        if (widgets.get(i).isAddToSidebar())
                            actualWidgetList.add(widgets.get(i));

                    if(onlyMasterappItems == actualWidgetList.size())
                        actualWidgetList.remove(onlyMasterappItems - 1);

                    //  decode bitmaps from sdcard
//                    for (int i = 0; i < actualWidgetList.size(); i++) {
//                        try {
//                            Bitmap tempBM = proccessBitmap(actualWidgetList.get(i).getFaviconFilePath());
//                            thumbnails.add(i, tempBM);
//                        } catch (Exception e) {
//                        }
//                    }

                    SidebarAdapter adapter = new SidebarAdapter(this, actualWidgetList, widget.getOrder());
                    widgetList.setAdapter(adapter);
                    widgetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                            final int order = actualWidgetList.get(i).getOrder();

                            if (order == widget.getOrder()) {
                                isShown = false;
                                rootContainer.clearAnimation();
                                rootContainer.startAnimation(animHideMenu);
                            } else if (order != -1) {
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

                                                com.appbuilder.sdk.android.Statics.sidebarClickListeners.get(order).onClick(view);
                                            }
                                        }
                                );
                                isShown = false;
                                rootContainer.clearAnimation();
                                rootContainer.startAnimation(animHideMenuStartActivity);
                            }

//                            Widget tempWidget = actualWidgetList.get(i);
//
//                            if(tempWidget.getCustomClickListener() != null)
//                                tempWidget.getCustomClickListener().onClick(view);
//                            else {
//                                Intent bridge = new Intent();
//                                bridge.putExtra("widget", tempWidget);
//                                setResult(RESULT_OK, bridge);
//                                finish();
//                            }
                        }
                    });
                    menuContainer.addView(menuTopBar, par);
                    menuContainer.addView(widgetList, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
                        topMargin = Float.valueOf(getResources().getDisplayMetrics().density * 15).intValue();
                    }});
                } else
                    par = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                //------------------------------------
                // create user container
                userContainer = new LinearLayout(this);
                userContainer.setOrientation(LinearLayout.VERTICAL);
                //userContainer.setBackgroundColor(Color.parseColor("#c0b9ff"));
                topBar = create_topbar_layout();
//                try {
//                    topBarLeftButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            showMenu();
//                        }
//                    });
//                } catch (NullPointerException nPEx) {
//                }
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

                dialogHolder = new LinearLayout(this);
                dialogHolder.setVisibility(View.INVISIBLE);
                FrameLayout.LayoutParams fparams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                fparams.gravity = Gravity.CENTER;
                dialogHolder.setLayoutParams(fparams);
                rootFrameLayout.addView(dialogHolder);


                // *******************************************************************************
                // preparing animation objects
                animShowMenu = new TranslateAnimation(0, -(Double.valueOf(screenWidth * 0.85).intValue()), 0, 0);
                animShowMenu.setInterpolator(new SmoothInterpolator());
                animShowMenu.setDuration(400);
                animShowMenu.setFillEnabled(true);  // остаться в конечном положении после анимации
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
                animHideMenu.setFillEnabled(true);  // остаться в конечном положении после анимации
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

                animShowImage = new AlphaAnimation(0, 1);
                animShowImage.setInterpolator(new LinearInterpolator());
                animShowImage.setDuration(600);
                //animShowImage.setFillEnabled(true);
                animShowImage.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });


                animHideImage = new AlphaAnimation(1, 0);
                animHideImage.setInterpolator(new LinearInterpolator());
                animHideImage.setDuration(600);
                animHideImage.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

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
                            isShown = true;
                            rootContainer.clearAnimation();
                            rootContainer.startAnimation(animShowMenu);
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if (showSideBar && isShown) {
                            isShown = false;
                            rootContainer.clearAnimation();
                            rootContainer.startAnimation(animHideMenu);
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
//                        if (isShown && x > Double.valueOf(screenWidth * 0.85).intValue() && x < screenWidth) {
//                            isShown = false;
//                            rootContainer.clearAnimation();
//                            rootContainer.startAnimation(animHideMenu);
//
//                            return true;
//                        }

                        return false;
                    }
                };
                //
                if (showSideBar)
                    rootContainer.setOnSwipeEvents(swipeInterface);
// ***************************************************************************************

                if (isShown) {
                    params = new FrameLayout.LayoutParams(Double.valueOf(screenWidth * 1.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.LEFT;
                    params.setMargins(-(Double.valueOf(screenWidth * 0.85).intValue()), 0, 0, 0);
                    rootContainer.setLayoutParams(params);
                } else {
                    params = new FrameLayout.LayoutParams(Double.valueOf(screenWidth * 1.85).intValue(), ViewGroup.LayoutParams.MATCH_PARENT);
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
                        adView.loadAdMob();
                    }
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            StatisticsCollector.newError(e, "AppBuilderModule.onCreate()");//ErrorLogging
            finish();
        }

        try {
            create();
        } catch (Exception e) {
            StatisticsCollector.newError(e, this.getClass().getName());//ErrorLogging
        }
    }

    private LinearLayout create_main_layout() {

        LinearLayout root = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        root.setLayoutParams(params);
        root.setOrientation(LinearLayout.VERTICAL);

        adLayout = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        adLayout.setLayoutParams(params);
        adLayout.setOrientation(LinearLayout.VERTICAL);

        rootFrameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams paramsFr = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        rootFrameLayout.setLayoutParams(paramsFr);

//        root.addView(adLayout);
        root.addView(rootFrameLayout);

        return root;
    }

    private void createPushDialog() {
        Log.e("createPushDialog - AppBuilderModuleMain", "createPushDialog");
        final AppPushNotificationMessage freshMessage = AppPushNotificationDB.getNotificationIfExist();
        if (freshMessage != null) {
            if (!isDialogShowen) {
                AppPushNotificationDialogLayout dialog = null;
                if (freshMessage.isPackageExist) {
                    dialog = new AppPushNotificationDialogLayout(
                            this,
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
                                this,
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
                                this,
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

    private LinearLayout create_menu_topbar_layout() {
        LinearLayout root = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(params);

        // root header layout
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(navBarDesign.color);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int) (density * 50));
        params.gravity = Gravity.CENTER_VERTICAL;
        header.setLayoutParams(params);
        header.setBackgroundColor(Color.parseColor(WIDGET_TOPBAR_BACKGROUND));

        // home button
        homeButton = new LinearLayout(this);
        homeButton.setOrientation(LinearLayout.HORIZONTAL);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (density * 42));
        params.setMargins((int) (density * 10), 0, 0, 0);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        homeButton.setLayoutParams(params);
        homeButton.setGravity(Gravity.CENTER_VERTICAL);
        TextView text = new TextView(this);
        text.setTextSize(16);
        text.setTextColor(Color.WHITE);
        text.setTypeface(null, Typeface.BOLD);
        text.setText("Home");
        homeButton.addView(text);

        header.addView(homeButton);
        root.addView(header);

        return root;
    }

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
            } catch (OutOfMemoryError e) {
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
                } catch (Exception ex) {
                } catch (OutOfMemoryError ex) {
                    Log.e(TAG, "OutOfMemoryError", ex);
                }
            }
        } catch (Exception e) {
            return null;
        }

        return bitmap;
    }

    private LinearLayout create_topbar_layout() {
        try{
            LinearLayout root = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setLayoutParams(params);

            // root header layout
            LinearLayout header = new LinearLayout(this);
            header.setPadding((int) (density * 10), (int) (density * 10), (int) (density * 10), (int) (density * 10));
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setBackgroundColor(navBarDesign.color);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int) (density * 50));
            params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            header.setLayoutParams(params);


            // left button
            topBarLeftButton = new LinearLayout(this);
            topBarLeftButton.setGravity(Gravity.LEFT);
            topBarLeftButton.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 5;//4;
            params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            topBarLeftButton.setLayoutParams(params);
            topBarLeftButton.setVisibility(View.VISIBLE);

            // default image inside left button
//        if (showSideBar) {
//            LinearLayout customLeftButtonHolder = new LinearLayout(this);
//            params = new LinearLayout.LayoutParams((int) (density * 38), (int) (density * 30));
//            customLeftButtonHolder.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//            customLeftButtonHolder.setOrientation(LinearLayout.HORIZONTAL);
//            customLeftButtonHolder.setLayoutParams(params);
//
//            ImageView image = new ImageView(this);
//            params = new LinearLayout.LayoutParams((int) (density * 25), (int) (density * 20));
//            image.setLayoutParams(params);
//            Drawable b_png = ContextCompat.getDrawable(this, R.drawable.api_show_bar);
//            b_png.setColorFilter(navBarDesign.itemDesign.textColor, PorterDuff.Mode.MULTIPLY);
//            image.setImageDrawable(b_png);
//            customLeftButtonHolder.addView(image);
//            topBarLeftButton.addView(customLeftButtonHolder);
//        }

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
            titleHolder.addView(topBarTitle);

            // right button
            if (showSideBar){
                topBarRightButton = new TopBarHamburger(this, navBarDesign);
                topBarRightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (showSideBar) {
                            if (isShown) {
                                isShown = false;
                                rootContainer.clearAnimation();
                                rootContainer.startAnimation(animHideMenu);
                            } else {
                                isShown = true;
                                rootContainer.clearAnimation();
                                rootContainer.startAnimation(animShowMenu);
                            }
                        }
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
        }catch(Exception ex) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * 50));
            LinearLayout result = new LinearLayout(this);
            result.addView(new View(this), lp);
            return result;
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
                    }
                    adLayout.addView(adView);
                    adView.loadAdMob();
                }
            } catch (Exception e) {
            }

            // clean parent view
            if (userLayout != null)
                userContainer.removeView(userLayout);

            // add child view
            userLayout = view;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.FILL_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            userContainer.addView(userLayout, params);


            // try to find surfaceview objects
            Message msg = handler.obtainMessage(FIND_SURFACEOBJECTS, userContainer);
            handler.sendMessageDelayed(msg, 500);
            super.setContentView(rootRoot);
        } catch (Exception e) {
            StatisticsCollector.newError(e, "AppBuilderModule.setContentView(View)");
            //LogError(e);
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

    public void showMenu() {
        if (showSideBar) {
            if (!isShown) {
                rootContainer.clearAnimation();
                rootContainer.startAnimation(animShowMenu);
                isShown = true;
            }
        }
    }

    //@Override
    public void setTopBarTitle(String title) {
        if (title != null && title.equals("") == false)
            try{
                topBarTitle.setText(title);
            }catch(Exception ex){
            }
    }

    //@Override
    public void setTopBarLeftButtonText(String text, boolean showArrow, View.OnClickListener clickHandler) {
        try {
            topBarLeftButton.removeAllViews();
            LinearLayout layout = createTextButton(Gravity.LEFT);
            TextView textView = (TextView) layout.getChildAt(1);
            ImageView arrowView = (ImageView) layout.getChildAt(0);


            textView.setTextColor(navBarDesign.itemDesign.textColor);
            Drawable d = arrowView.getBackground();
            d.setColorFilter(navBarDesign.itemDesign.textColor, PorterDuff.Mode.MULTIPLY);
            arrowView.setBackgroundDrawable(d);

            // text
            textView.setText(text);

            if (!showArrow)
                arrowView.setVisibility(View.GONE);

            if (clickHandler != null)
                topBarLeftButton.setOnClickListener(clickHandler);
            topBarLeftButton.addView(layout);
            topBarLeftButton.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            //LogError(e);
        }
    }

    private LinearLayout createTextButton(int position) {
        LinearLayout root = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        //root.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
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

        //text.setLines(1);
        text.setSingleLine();
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, navBarDesign.itemDesign.fontSize);
        text.setTextColor(navBarDesign.itemDesign.textColor);

//        if (navBarDesign.itemDesign.textAlignment.compareTo("left") == 0)
//            text.setGravity( Gravity.CENTER_VERTICAL |Gravity.LEFT );
//        else if (navBarDesign.itemDesign.textAlignment.compareTo("center") == 0)
//            text.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
//        else if (navBarDesign.itemDesign.textAlignment.compareTo("right") == 0)
//            text.setGravity( Gravity.CENTER_VERTICAL |Gravity.RIGHT );

        if (navBarDesign.itemDesign.fontWeight.compareToIgnoreCase("bold") == 0)
            text.setTypeface(Typeface.DEFAULT_BOLD);

        root.addView(arrow);
        root.addView(text);

        return root;
    }

    public void create() {
    }

    public void start() {
    }

    public void restart() {
    }

    public void resume() {
    }

    public void pause() {
    }

    public void stop() {
    }

    public void destroy() {
    }

    public void setSession(Serializable object) {
    }

    public Serializable getSession() {
        return null;
    }

    public void onAdClosed() {
        if(adView != null)
            adView.closeView();

        adView = null;
        Statics.isAdClosed = true;
    }

    protected class SmoothInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float v) {
            return (float) Math.pow(v - 1, 3) + 1;
        }
    }

    @Override
    public void finish() {
        if(getClass().getCanonicalName().equals(widget.getPluginPackage() + "." + widget.getPluginName())) {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.ibuildapp.masterapp", "com.appbuilder.core.AppBuilder");
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                if(resultData != null) {
                    intent.putExtras(resultData);
                    resultData = null;
                }

                super.startActivityForResult(intent, -1);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            if(resultData != null) {
                setResult(RESULT_OK, resultData);
                resultData = null;
            }

            super.finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(showSideBar && isShown) {
            isShown = false;
            rootContainer.clearAnimation();
            rootContainer.startAnimation(animHideMenu);
        } else
            super.onBackPressed();
    }

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (NullPointerException nPEx) {
        }
    }

    public void forceCloseModule(int order) {
        resultData = new Intent().putExtra(Statics.FORCE_CLOSE_MODULE_FLAG, Statics.FORCE_CLOSE_MODULE).putExtra(Statics.FORCE_CLOSE_NEW_MODULE_ORDER, order);
        finish();
    }

    public void forceCloseApp() {
        resultData = new Intent().putExtra(Statics.FORCE_CLOSE_APP_FLAG, Statics.FORCE_CLOSE_MODULE);
        finish();
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
                forceCloseApp();
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
                    if (Utils.networkAvailable(AppBuilderModuleCordova.this)) {
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
                                                Toast.makeText(AppBuilderModuleCordova.this, R.string.complain_ok, Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(AppBuilderModuleCordova.this, R.string.complain_false, Toast.LENGTH_SHORT).show();
                                            hideProgressDialog();
                                        }
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AppBuilderModuleCordova.this, R.string.complain_false, Toast.LENGTH_SHORT).show();
                                            hideProgressDialog();
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else
                        Toast.makeText(AppBuilderModuleCordova.this, R.string.complain_no_internet, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(AppBuilderModuleCordova.this, R.string.complain_false, Toast.LENGTH_SHORT).show();
            }
        });

        Widget separator = new Widget();
        separator.setAddToSidebar(true);

        actualWidgetList.add(0, separator);
        actualWidgetList.add(0, flagContent);
        actualWidgetList.add(0, toMasterapp);
    }

}
