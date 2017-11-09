package com.ibuildapp.masterapp;

import android.content.*;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.appbuilder.core.rs.BlurBuilder;
import com.appbuilder.core.xmlconfiguration.AppConfigManager;
import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.core.xmlconfiguration.AppConfigureParser;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.google.gson.Gson;
import com.ibuildapp.masterapp.DAO.TemplatesDAO;
import com.ibuildapp.masterapp.adapter.CategoryAdapter;
import com.ibuildapp.masterapp.api.ServerApi;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.*;
import com.ibuildapp.masterapp.utils.FlurryLogger;
import com.ibuildapp.masterapp.utils.Logger;
import com.ibuildapp.masterapp.utils.Statics;
import com.ibuildapp.masterapp.utils.Utils;
import com.ibuildapp.masterapp.view.EditTextHandleBackPressed;
import com.ibuildapp.masterapp.view.SideBarComponent;
import com.ibuildapp.romanblack.WebPlugin.WebPlugin;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import static com.appbuilder.core.xmlconfiguration.AppConfigManager.ConfigManagerSettings.CONFIG_SOURCE.FROM_BUILDIN;

public class MainActivity extends BaseActivity implements Observer {

    // constants
    private final String TAG = "com.ibuildapp.masterapp.MainActivity";
    private final int GRID_ROW_HEIGHT = 56; //DP
    private final int GRID_ROW_SPACING = 1; //DP
    private final int GRID_NUM_COLUMNS = 2; //DP
    private final float GRID_BITMAP_ASPECT_RATIO = 1.23f;

    private final int AUTHORIZATION_FACEBOOK = 1001;
    private final int AUTHORIZATION_TWITTER = 1002;

    private final int SHARING_FACEBOOK = 1004;
    private final int SHARING_TWITTER = 1005;
    private final int SHARING_EMAIL = 1006;
    private final int SHARING_SMS = 1007;

    // UI
    private SideBarComponent rootScrollerHolder;
    private ImageView searchBtn;
    private TextView searchCancel;
    private LinearLayout searchHolder;
    private EditTextHandleBackPressed searchEdit;
    private LinearLayout hamburgerBtn;
    private GridView categoryGrid;
    private LinearLayout sharingHolder;
    private TextView inviteButton;

    // backend
    private Logger logger;
    private CategoryAdapter adapter;
    private LayoutInflater inflater;
    private int currentCategoryId;
    private List<CategoryEntity> categoryList = new ArrayList<CategoryEntity>();
    private BroadcastReceiver favouritesReceiver;
    private float density;
    private int screenWidth;
    private int screenHeight;
    private int appIdToRate;
    private boolean rateAction;
    private SmsBody body;

    private Callback<AppsId> searchCallback;

    private Callback<AppsId> sortedCallback;
    private Callback<StatusOnly> rateAppCallback;
    private Callback<FeaturedResponse> firstTenAppsCallback;
    private RelativeLayout relativeHolder;
    private ImageView blured;
    private final static String CAHCE_CONFIG_FILE = "cache.config";

//    private Callback<TemplateResponse> templateCallback;
//    private Callback<PrefixResponse> prefixCallback;
//    private Callback<CategoryListResponse> categoryCallback;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideTopBar();
        initializeBackend();
        initializeUI();

        onNewIntent(getIntent());
        ReferrerReceiver.getObservable().addObserver(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        onNewIntent(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        SharedPreferences sharedPref = getSharedPreferences("referrer", MODE_PRIVATE);
        String appId = sharedPref.getString("referrer", "notfound");

        if (appId.equals("notfound") && intent != null && intent.getData() != null)
            appId = intent.getData().getQueryParameter("app");

        if (Utils.isInteger(appId)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("referrer", "notfound");
            editor.commit();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("ibuildappmarket://?app=" + appId));
            startActivity(browserIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        blured.setVisibility(View.GONE);
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryLogger.onStartInit(MainActivity.this);
        FlurryLogger.appLaunchEvent();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        FlurryLogger.onStopInit();
        Log.e(TAG, "onStop");
        super.onStop();
    }

    private void initializeBackend() {

        // log testing
        try {
            Logger.init(Environment.getExternalStorageDirectory().getAbsolutePath(),"log.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        inflater = LayoutInflater.from(getApplicationContext());
        density = getResources().getDisplayMetrics().density;
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        Statics.uuid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Statics.cachePath = getExternalCacheDir().getAbsolutePath();
        Statics.language = Locale.getDefault().getLanguage();
        Statics.databasePath = getDatabasePath(Statics.DB_NAME).getPath();
        File parentDir = new File(Statics.databasePath).getParentFile();
        if ( !parentDir.exists() )
            parentDir.mkdirs();

        if ( !Utils.checkDb() )
            Utils.copyDbToWorkDirectory( MainActivity.this );

        SqlAdapter.init( MainActivity.this );

//        categoryCallback = new Callback<CategoryListResponse>() {
//            @Override
//            public void success(CategoryListResponse categoryListResponse, Response response) {
//                Log.e(TAG, "");
//                if (categoryListResponse != null) {
//                    SqlAdapter.clearTableCategory();
//                    SqlAdapter.insertCategory(categoryListResponse.getCategoryList());
//                }
//
//                hideProgressDialog();
//                initializeUI();
//            }
//
//            @Override
//            public void failure(RetrofitError retrofitError) {
//                hideProgressDialog();
//                initializeUI();
//            }
//        };

        firstTenAppsCallback = new Callback<FeaturedResponse>() {
            @Override
            public void success(FeaturedResponse featuredResponse, Response response) {
                hideProgressDialog();
                SqlAdapter.insertApplication(featuredResponse.getAppsList());

                Intent bridge = new Intent(MainActivity.this, CategoryDetails.class);
                bridge.putExtra("category_id", currentCategoryId);
                startActivity(bridge);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                hideProgressDialog();
                Log.e(TAG, "Cannot get first ten apps = " + retrofitError.getMessage());
            }
        };

        searchCallback = new Callback<AppsId>() {
            @Override
            public void success(AppsId appsId, Response response) {
                hideProgressDialog();

                if ( appsId != null ) {
                    Intent bridge = new Intent(MainActivity.this, CategoryDetails.class);
                    bridge.putExtra("search", searchEdit.getText().toString());
                    bridge.putExtra("appid", (Serializable) appsId.getApps());
                    startActivity(bridge);

                    // венмен поле поиска в исходное положение
                    searchBtn.setVisibility(View.VISIBLE);
                    searchEdit.setText("");
                    searchCancel.setVisibility(View.GONE);
                    searchHolder.setVisibility(View.GONE);
                    hideKeyboard();

                } else
                    Toast.makeText(MainActivity.this, getString(R.string.unable_connect), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(MainActivity.this, getString(R.string.unable_connect), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        };

//        templateCallback = new Callback<TemplateResponse>() {
//            @Override
//            public void success(TemplateResponse templateResponse, Response response) {
//                if (templateResponse != null && templateResponse.templates.size() != 0) {
//                    new TemplatesDAO(Statics.cachePath).setTemplates(templateResponse);
//                }
//                ServerApi.getInstance().getSplashPrefix(Statics.PLATFORM_NAME, screenWidth, screenHeight, prefixCallback);
//            }
//
//            @Override
//            public void failure(RetrofitError retrofitError) {
//                ServerApi.getInstance().getSplashPrefix(Statics.PLATFORM_NAME, screenWidth, screenHeight, prefixCallback);
//            }
//        };
//
//        prefixCallback = new Callback<PrefixResponse>() {
//            @Override
//            public void success(PrefixResponse prefixResponse, Response response) {
//                if ( prefixResponse != null )
//                    Statics.IMAGE_PREFIX = prefixResponse.prefix;
//
//                ServerApi.getInstance().getCategoryList(categoryCallback);
//            }
//
//            @Override
//            public void failure(RetrofitError retrofitError) {
//                ServerApi.getInstance().getCategoryList(categoryCallback);
//            }
//        };

        favouritesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String appid = intent.getStringExtra("appid");
                boolean addTo = intent.getBooleanExtra("favourites", false);
                Log.e(TAG, "Appid = " + appid + " addto = " + addTo);

                appIdToRate =Integer.parseInt(appid);
                rateAction = addTo;

                if (addTo) {
                    //
                    FlurryLogger.favouritesAdded();

                    ApplicationEntity app = SqlAdapter.selectApplicationById(Integer.parseInt(appid));
                    if (app != null) {
                        app.favourited = false;
                        app.active = true;
//                        app.timestamp = System.currentTimeMillis();
                        SqlAdapter.insertFavourites(app);
                        if ( Utils.checkNetwork( MainActivity.this ) > 0 )
                        {
                            ServerApi.getInstance().rateAppAsync(app.appid, Statics.uuid, true, rateAppCallback);
                        }
                    } else
                        Log.e(TAG, "Added error. No such app");
                } else {
                    SqlAdapter.updateFavourite(Integer.parseInt(appid), false, false);

                    if ( Utils.checkNetwork( MainActivity.this ) > 0 )
                    {
                        ServerApi.getInstance().rateAppAsync(Integer.parseInt(appid), Statics.uuid, false, rateAppCallback);
                    }
                }
            }
        };

        sortedCallback = new Callback<AppsId>() {
            @Override
            public void success(AppsId sortedAppListResponse, Response response) {
                if ( sortedAppListResponse != null ) {
                    SqlAdapter.updateCategorySortedAps(currentCategoryId, sortedAppListResponse.apps);

                    List<String> firstTenApps = new ArrayList<String>();
                    int count = sortedAppListResponse.getApps().size();
                    for (int i = 0; i < count; i++) {
                        firstTenApps.add(sortedAppListResponse.apps.get(i));
                    }

                    ServerApi.getInstance().getAppsListAsync( firstTenApps, firstTenAppsCallback );
                } else
                {
                    Toast.makeText(MainActivity.this, getString(R.string.unable_connect), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(MainActivity.this, getString(R.string.unable_connect), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        };

        rateAppCallback = new Callback<StatusOnly>() {
            @Override
            public void success(StatusOnly statusOnly, Response response) {
                if ( rateAction )
                {
                    switch ( statusOnly.getStatus() )
                    {
                        case 0:
                        {
                            SqlAdapter.updateFavourite(appIdToRate, true, true);
                        } break;

                        case 1:
                        {
                            SqlAdapter.updateFavourite(appIdToRate, true, true);
                        } break;

                        case -1:
                        {
                            Log.e(TAG, "No such app ");
                        } break;
                    }
                } else
                {
                    switch ( statusOnly.getStatus() )
                    {
                        case 0:
                        {
                            SqlAdapter.deleteFavourite(appIdToRate);
                        } break;

                        case 1:
                        {
                            SqlAdapter.deleteFavourite(appIdToRate);
                        } break;

                        case -1:
                        {
                            Log.e(TAG, "No such app ");
                        } break;
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "Connection error");
            }
        };


        IntentFilter intFilt = new IntentFilter(com.appbuilder.sdk.android.Statics.FAVOURITES_BROADCAST);
        registerReceiver(favouritesReceiver, intFilt);
    }

//    private void loadContent() {
//        if (Utils.checkNetwork(MainActivity.this) < 0) {
//            initializeUI();
//        } else {
//            showProgressDialog(getString(R.string.loading));
//
//            ServerApi.getInstance().getTemplates(templateCallback);
//        }
//    }

    private void initializeUI() {
        setContentView(R.layout.masterapp_main);

//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                copy();
//            }
//        });

        hideTopBar();
        relativeHolder = (RelativeLayout) findViewById(R.id.relative_holder);
        blured = (ImageView) findViewById(R.id.blured);
        rootScrollerHolder = (SideBarComponent) findViewById(R.id.root_scroller);
        ViewGroup leftView = (ViewGroup) inflater.inflate(R.layout.masterapp_sidebar_left_view, null);
        leftView.setLayoutParams(new ViewGroup.LayoutParams((int) (screenWidth * 0.7), ViewGroup.LayoutParams.MATCH_PARENT));

        inviteButton = (TextView) leftView.findViewById(R.id.sidebar_invite_btn);
        inviteButton.setOnClickListener(getButtonsCLick());

        sharingHolder = (LinearLayout) leftView.findViewById(R.id.sidebar_sharing_holder);
        ImageView sharingFacebook = (ImageView) leftView.findViewById(R.id.sidebar_facebook_sharing);
        sharingFacebook.setOnClickListener( getButtonsCLick());
        ImageView sharingTwitter= (ImageView) leftView.findViewById(R.id.sidebar_twitter_sharing);
        sharingTwitter.setOnClickListener( getButtonsCLick());
        ImageView sharingEmail= (ImageView) leftView.findViewById(R.id.sidebar_email_sharing);
        sharingEmail.setOnClickListener( getButtonsCLick() );
        ImageView sharingSms = (ImageView) leftView.findViewById(R.id.sidebar_sms_sharing);
        sharingSms.setOnClickListener( getButtonsCLick() );

        int sharingImageSize = (int) ((screenWidth * 0.7)
                - (2*15*density)
                - (3*8*density))
                /4;

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) sharingFacebook.getLayoutParams();
        params.width = sharingImageSize;
        params.height = sharingImageSize;

        params = (LinearLayout.LayoutParams) sharingTwitter.getLayoutParams();
        params.width = sharingImageSize;
        params.height = sharingImageSize;

        params = (LinearLayout.LayoutParams) sharingEmail.getLayoutParams();
        params.width = sharingImageSize;
        params.height = sharingImageSize;

        params = (LinearLayout.LayoutParams) sharingSms.getLayoutParams();
        params.width = sharingImageSize;
        params.height = sharingImageSize;

        Button sidebar_favourites_btn = (Button)leftView.findViewById(R.id.sidebar_favourites_btn);
        Button sidebar_about_btn = (Button)leftView.findViewById(R.id.sidebar_about_btn);
        Button sidebar_dapp_btn = (Button)leftView.findViewById(R.id.sidebar_dapp_btn);

        sidebar_favourites_btn.setOnClickListener(getButtonsCLick());
        sidebar_about_btn.setOnClickListener(getButtonsCLick());
        sidebar_dapp_btn.setOnClickListener(getButtonsCLick());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            sidebar_favourites_btn.setAllCaps(false);
            sidebar_about_btn.setAllCaps(false);
            sidebar_dapp_btn.setAllCaps(false);

        }

        rootScrollerHolder.setLeftSideView(leftView);

        searchBtn = (ImageView) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener( getButtonsCLick());
        searchCancel = (TextView) findViewById(R.id.search_cancel_text);
        searchCancel.setOnClickListener( getButtonsCLick());
        searchHolder = (LinearLayout) findViewById(R.id.search_edittext_holder);
        searchEdit = (EditTextHandleBackPressed) findViewById(R.id.search_edittext);
        hamburgerBtn = (LinearLayout) findViewById(R.id.hamburger_btn);
        hamburgerBtn.setOnClickListener(getButtonsCLick());

        categoryGrid = (GridView) findViewById(R.id.main_categories);
        categoryGrid.setSelector(R.drawable.grid_custom_background);
        categoryGrid.setVerticalScrollBarEnabled(false);
        categoryGrid.setNumColumns(GRID_NUM_COLUMNS);
        categoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // flurry event
                if ( Utils.checkNetwork(MainActivity.this) > 0 )
                    FlurryLogger.categoryOpened(categoryList.get(i).title);


                currentCategoryId = categoryList.get(i).id;
                List<String> sortedApps = SqlAdapter.getSortedAppsForCategoryByID(categoryList.get(i).id);
                if ( sortedApps.size() > 0  )
                {
                    Intent bridge = new Intent(MainActivity.this, CategoryDetails.class);
                    bridge.putExtra("category_id", categoryList.get(i).id);
                    startActivity(bridge);

                    if ( rootScrollerHolder.isLeftShowen() )
                        rootScrollerHolder.toggleLeftSide();
                } else
                {
                    if ( Utils.checkNetwork(MainActivity.this) < 0 )
                    {
                        Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //showProgressDialog(getString(R.string.loading));

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        // todo мутим blur
                        final Bitmap bluredImg = BlurBuilder.blur(relativeHolder);
                        blured.setImageBitmap(bluredImg);
                        blured.setVisibility(View.VISIBLE);

                        AnimationSet alpha = (AnimationSet) AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha_blur_onitem);
                        //alpha.setFillAfter(true);
                        //alpha.setFillEnabled(true);
                        alpha.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                blured.setAlpha(1.0f);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        blured.clearAnimation();
                        blured.startAnimation(alpha);
                    } else
                        showProgressDialog(getString(R.string.loading));

                    ServerApi.getInstance().getSortedAppList(categoryList.get(i).id, sortedCallback);
                }
            }
        });


        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP)
                    return false;

//                if (keyEvent != null && keyEvent.getAction() == KeyEvent.KEYCODE_BACK)
//                {
//                    searchBtn.setVisibility(View.VISIBLE);
//                    searchCancel.setVisibility(View.GONE);
//                    searchHolder.setVisibility(View.GONE);
//                    return false;
//                }


                if (!TextUtils.isEmpty(textView.getText().toString())) {
                    if (Utils.checkNetwork(MainActivity.this) < 0) {
                        Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    } else {
                        showProgressDialog(getString(R.string.search));
                        ServerApi.getInstance().searchAsync(-1, textView.getText().toString(), searchCallback);
                    }
                }

                Log.e("", "");
                return true;
            }
        });

        searchEdit.setBackPressedInterface( new EditTextHandleBackPressed.OnBackPressed() {
            @Override
            public void onBackPressed() {
                searchBtn.setVisibility(View.VISIBLE);
                searchCancel.setVisibility(View.GONE);
                searchHolder.setVisibility(View.GONE);
                searchEdit.setText("");
                hideKeyboard();
            }
        });

//        int searchBlockH = (int) (Statics.SEARCH_BLOCK_H * density);
//        //int featuredHeaderH = (int) (Statics.FEATURED_HEADER_BLOCK_H * density);
//
//        int featuderContentH = (int) ((screenWidth / Statics.FEATURED_ELEMENTS_COUNT) + 3*Statics.FEATURED_IMAGE_MARGINS*density  + 16 *density);
//        //int dealsBlockH = (int) (Statics.DEALS_BLOCK_H * density);
//
//        int categoryGridH = screenHeight - (searchBlockH +  featuderContentH);
//        int minGridH = (int) ((density * GRID_ROW_HEIGHT * 4) + (density * GRID_ROW_SPACING * 3));
//        ViewGroup.LayoutParams params = categoryGrid.getLayoutParams();
//        int rowH = 0;
//        if (categoryGridH > minGridH) {
//            rowH = categoryGridH / 4;
//            params.height = categoryGridH;
//            categoryGrid.setLayoutParams(params);
//        } else {
//            rowH = minGridH / 4;
//            params.height = minGridH;
//            categoryGrid.setLayoutParams(params);
//        }



        int rowH = (int) (screenWidth/(2*GRID_BITMAP_ASPECT_RATIO));
        categoryList = SqlAdapter.selectAllCategory();
        adapter = new CategoryAdapter(MainActivity.this, categoryGrid,categoryList, rowH);
        categoryGrid.setAdapter(adapter);
        //categoryGrid.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == AUTHORIZATION_FACEBOOK )
        {
            if ( resultCode == RESULT_OK )
            {
                if ( Utils.checkNetwork(MainActivity.this)  > 0 )
                {
                    shareFaceBook();
                    FlurryLogger.sharingFacebookAttempt();
                }
                else
                    Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

            }
        } else if ( requestCode == AUTHORIZATION_TWITTER )
        {
            if ( resultCode == RESULT_OK )
            {
                if ( Utils.checkNetwork(MainActivity.this)  > 0 )
                {
                    shareTwitter();
                    FlurryLogger.sharingTwitterAttempt();
                }
                else
                    Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        } else if ( requestCode == SHARING_FACEBOOK )
        {
            if ( resultCode == RESULT_OK)
            {
                Toast.makeText(MainActivity.this, getString(R.string.sharing_success), Toast.LENGTH_SHORT).show();
                FlurryLogger.sharingFacebookResult(0);
            } else if ( resultCode == RESULT_CANCELED )
            {
                FlurryLogger.sharingFacebookResult(-1);
                Toast.makeText(MainActivity.this, getString(R.string.sharing_error), Toast.LENGTH_SHORT).show();
            }
        } else if ( requestCode == SHARING_TWITTER )
        {
            if ( resultCode == RESULT_OK)
            {
                Toast.makeText(MainActivity.this, getString(R.string.sharing_success), Toast.LENGTH_SHORT).show();
                FlurryLogger.sharingTwitterResult(0);
            } else if ( resultCode == RESULT_CANCELED )
            {
                FlurryLogger.sharingTwitterResult(-1);
                int error_code;
                try {
                    error_code = Integer.valueOf(popSharingActivityResultData("error_code"));
                } catch(NumberFormatException exception) {
                    error_code = -1;
                }
                    switch ( error_code )
                    {
                        case 1:
                        {
                            Toast.makeText(MainActivity.this, getString(R.string.sharing_dublicated), Toast.LENGTH_SHORT).show();
                        } break;

                        case -1:
                        {
                            Toast.makeText(MainActivity.this, getString(R.string.sharing_error), Toast.LENGTH_SHORT).show();
                        } break;
                    }
            }
        } else if ( requestCode == SHARING_EMAIL )
        {
            if ( resultCode == RESULT_OK )
            {
                FlurryLogger.sharingEmalResult(0);
            } else if ( resultCode == RESULT_CANCELED )
            {
                FlurryLogger.sharingEmalResult(-1);
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

                showProgressDialog(getString(R.string.loading));
                new Thread( new Runnable() {
                    @Override
                    public void run() {
                        Runnable action = null;
                        try {
                            SmsSharingResponse response = ServerApi.getInstance().smsSharing(body);
                            FlurryLogger.sharingSmsResult(0);
                            action = new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, getString(R.string.sharing_sms_success), Toast.LENGTH_SHORT).show();
                                    hideProgressDialog();
                                }
                            };
                        } catch (RetrofitError e) {
                            FlurryLogger.sharingSmsResult(-1);
                            action = new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, getString(R.string.sharing_sms_error), Toast.LENGTH_SHORT).show();
                                    hideProgressDialog();
                                }
                            };
                        }
                        runOnUiThread( action );
                    }
                }).start();
            } else
                FlurryLogger.sharingSmsResult(-1);

        }
    }

    private View.OnClickListener getButtonsCLick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.search_btn: {
                        searchBtn.setVisibility(View.GONE);
                        searchCancel.setVisibility(View.VISIBLE);
                        searchHolder.setVisibility(View.VISIBLE);
                        searchEdit.requestFocus();
                        showKeyboard();
                    }
                    break;

                    case R.id.search_cancel_text: {
                        searchBtn.setVisibility(View.VISIBLE);
                        searchCancel.setVisibility(View.GONE);
                        searchHolder.setVisibility(View.GONE);
                        searchEdit.setText("");
                        hideKeyboard();
                    }
                    break;

                    case R.id.hamburger_btn: {
                        rootScrollerHolder.toggleLeftSide();
                    }
                    break;

                    case R.id.sidebar_favourites_btn: {
                        FlurryLogger.favouritesTrigger();

                        Intent bridge = new Intent(MainActivity.this, FavouritesActivity.class);
                        startActivity(bridge);
                        rootScrollerHolder.toggleLeftSide();

                    }
                    break;

                    case R.id.sidebar_about_btn: {
                        Intent bridge = new Intent(MainActivity.this, AboutUsActivity.class);
                        startActivity(bridge);
                        rootScrollerHolder.toggleLeftSide();
                    }
                    break;

                    case R.id.sidebar_dapp_btn: {
                        ArrayList<Widget> widgetsList = new ArrayList<Widget>();
                        AppConfigManager.ConfigManagerSettings settings = new AppConfigManager.ConfigManagerSettings(FROM_BUILDIN, getCacheDir().getPath(),"", 0 );
                        AppConfigManager appConfig = new AppConfigManager();
                        AppConfigure getConf = appConfig.getConfig(getApplicationContext(), settings);
                        for (Widget w :  getConf.getmWidgets()) {
                            Widget www = new Widget(w);
                         //   www.setPluginXmlData("");
                            widgetsList.add(www);
                        }
                        Widget web = widgetsList.get(13);

                        Intent bridge = new Intent(MainActivity.this, WebPlugin.class);
                        bridge.putExtra("Widget", web);
                        bridge.putExtra("navBarDesign", getConf.getNavBarDesign());
                        startActivity(bridge);
                        rootScrollerHolder.toggleLeftSide();
                    }
                    break;

                    case R.id.sidebar_invite_btn: {
                        if ( sharingHolder.getVisibility() == View.VISIBLE )
                        {
                            sharingHolder.setVisibility(View.GONE);
                            inviteButton.setTextColor(getResources().getColor(R.color.white));
                        } else
                        {
                            sharingHolder.setVisibility(View.VISIBLE);
                            inviteButton.setTextColor(getResources().getColor(R.color.blue));
                        }
                    }
                    break;

                    case R.id.sidebar_facebook_sharing:
                    {
                        if (Utils.checkNetwork(MainActivity.this) > 0)
                        {
                            if ( Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK) )
                            {
                                shareFaceBook();
                                FlurryLogger.sharingFacebookAttempt();
                            } else
                                Authorization.authorize(MainActivity.this, AUTHORIZATION_FACEBOOK, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                        } else
                            Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

                    } break;

                    case R.id.sidebar_twitter_sharing:
                    {
                        if ( Utils.checkNetwork(MainActivity.this)  >0 )
                        {
                            if ( Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_TWITTER) )
                            {
                                shareTwitter();
                                FlurryLogger.sharingTwitterAttempt();
                            } else
                                Authorization.authorize(MainActivity.this, AUTHORIZATION_TWITTER, Authorization.AUTHORIZATION_TYPE_TWITTER);
                        }else
                            Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();


                    } break;

                    case R.id.sidebar_email_sharing:
                    {
                        if ( Utils.checkNetwork(MainActivity.this)  > 0 )
                        {
                            shareEmail();
                            FlurryLogger.sharingEmailAttempt();
                        }
                        else
                            Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    } break;

                    case R.id.sidebar_sms_sharing :
                    {
                        FlurryLogger.sharingSmsAttempt();
                        startActivityForResult(new Intent(MainActivity.this, ContactChooser.class), SHARING_SMS);
                    } break;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        adapter.clearBitmaps();
        if (favouritesReceiver != null)
            unregisterReceiver(favouritesReceiver);

        Logger.getInstance().closeLogger();
        FlurryLogger.clearState();
        ReferrerReceiver.getObservable().deleteObserver(this);
    }

    private void hideKeyboard()
    {
        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
    }

    private void showKeyboard()
    {
        imm.toggleSoftInputFromWindow(searchEdit.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_open_scale_main, R.anim.activity_close_translate_main);
    }

    public void shareFaceBook( )
    {
        Intent bridge = new Intent(MainActivity.this, SharingActivity.class);
        bridge.putExtra("type", "facebook");
        startActivityForResult(bridge, SHARING_FACEBOOK);
    }

    public void shareTwitter( )
    {
        Intent bridge = new Intent(MainActivity.this, SharingActivity.class);
        bridge.putExtra("type", "twitter");
        startActivityForResult(bridge, SHARING_TWITTER);
    }

    private void shareEmail( )
    {
        Intent emailIntent =
                new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_sharing_subject));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.sharing_text)+ " " + Statics.SHARING_URL);
        startActivityForResult(Intent.createChooser(emailIntent, ""), SHARING_EMAIL);
    }

    private void copy()  {
        File src = new File("/data/data/com.ibuildapp.masterapp/databases/MasterApp.db");
        if (src.exists())
        {
            try {
                InputStream stream = new BufferedInputStream( new FileInputStream(src));
                File dest = new File("/mnt/sdcard/masterapp_default_db.db");

                OutputStream os = new FileOutputStream(dest);
                int length;
                byte[] buffer = new byte[1024];
                while ((length = stream.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                os.close();
                stream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else
        {
            Log.e(TAG, "");
        }
    }

    static String SharingActivityResultDataPreferenceName = MainActivity.class.getPackage().getName() + "_SharingActivity_result_data";

    private String popSharingActivityResultData(String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(SharingActivityResultDataPreferenceName, MODE_PRIVATE);
        String value = sharedPreferences.getString(name, "");
        sharedPreferences.edit().putString(name, "").apply();
        return value;
    }

}
