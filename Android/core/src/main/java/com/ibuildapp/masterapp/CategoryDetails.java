package com.ibuildapp.masterapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.appbuilder.core.AppBuilder;
import com.flurry.android.FlurryAgent;
import com.ibuildapp.masterapp.adapter.PagingAdapter;
import com.ibuildapp.masterapp.api.ServerApi;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.ApplicationEntity;
import com.ibuildapp.masterapp.model.AppsId;
import com.ibuildapp.masterapp.model.CategoryEntity;
import com.ibuildapp.masterapp.model.FeaturedResponse;
import com.ibuildapp.masterapp.utils.FlurryLogger;
import com.ibuildapp.masterapp.utils.Statics;
import com.ibuildapp.masterapp.utils.Utils;
import com.ibuildapp.masterapp.view.EditTextHandleBackPressed;
import com.ibuildapp.masterapp.view.SearchView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 21.07.14
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public class CategoryDetails extends BaseActivity {

    // constants
    private final String TAG = "com.ibuildapp.masterapp.CategoryDetails";
    private final float FRICTION_COEFFICIENT = 9; // я даже ума не приложу в чем эта херня изменяется
    private final int HTTP_RESULT = 10011;
    private final int  UNABLE_CONNECT = 10012;
    private final int  LOAD_CONTENT = 10013;
    private final int  HIDE_PROGRESS = 10014;
    private final int  SHOW_NO_RESULT = 10015;
    private final int  UPDATE_COUNTER = 10016;
    private final int  SHOW_SEARCH_VIEW = 10017; //DP
    private final int  QUERY_APPS_COUNT = 36;
    private final int  GRID_ROW_SPACING = 5; //DP
    private final int  GRID_NUM_COLUMNS = 2; //DP


    // UI
    private GridView grid;
    private ProgressBar progressBar;
    private TextView noResult;

    private LinearLayout backBtn;
    private ImageView searchBtn;
    private TextView searchCancel;
    private LinearLayout searchHolder;
    private EditTextHandleBackPressed searchEdit;
    private TextView topbarTitle;

    // backend
    private boolean scrollToStart = false;
    private Animation showProgress;
    private Animation hideProgress;
    private boolean emptySearch = false;
    private boolean fromMainSearch = false;

    private InputMethodManager imm;
    private String searchStr;
    private List<String> searchAppidList = new ArrayList<String>();
    private PagingAdapter adapter;
    private boolean isLoading = false;
    private float density;
    private CategoryEntity category;
    private int categoryArrPosition = 0;
    private List<String> categoryAppsArr = new ArrayList<String>();
    private List<ApplicationEntity> appsContent = new ArrayList<ApplicationEntity>();
    private List<ApplicationEntity> adapterContent = new ArrayList<ApplicationEntity>();
    private int screenWidth;
    private int screenHeight;
    private ArrayList<String> appsToDownload = new ArrayList<String>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HTTP_RESULT: {
                    drawUI(true);
                    showProgress(false);
                    hideProgressDialog();
                    isLoading = false;
                }
                break;

                case SHOW_SEARCH_VIEW:
                {
                    searchBtn.setVisibility(View.GONE);
                    searchCancel.setVisibility(View.VISIBLE);
                    searchHolder.setVisibility(View.VISIBLE);
                    searchEdit.requestFocus();
                } break;

                case UPDATE_COUNTER :
                {
                    if ( msg.arg1 != -1 && msg.arg1 != 0)
                        setEditBtnText(Integer.toString(msg.arg1));
                    else
                        setEditBtnText("");
                }break;

                case UNABLE_CONNECT: {
                    drawUI(true);
                    showProgress(false);
                    hideProgressDialog();
                    isLoading = false;
                }
                break;

                case LOAD_CONTENT: {
                    showProgress(false);
                    hideProgressDialog();
                    loadContent();
                } break;

                case HIDE_PROGRESS: {
                    showProgress(false);
                    hideProgressDialog();
                    isLoading = false;
                } break;

                case SHOW_NO_RESULT: {
                    setEditBtnText("");
                    noResult.setVisibility(View.VISIBLE);
                    grid.setVisibility(View.GONE);
                    showProgress(false);
                    hideProgressDialog();
                }break;
            }
        }
    };
    private Date startCategorySession;
    private long appCounter=0;
    private Date startAppSession;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeBackend();

        initializeUI();

        loadContent();
    }

    private void drawUI( boolean showNoResult )
    {
        if ( appsContent.size() > 0 )
        {
            List<ApplicationEntity> tempSortedList = new ArrayList<ApplicationEntity>();
            for ( int i=0; i < appsContent.size(); i++ )
            {
                int catId = -1;
                if ( searchAppidList != null && searchAppidList.size() > 0 )
                    catId = Integer.parseInt(searchAppidList.get(i).trim());
                else
                {
                    if ( appsContent.size() > category.sorted_apps_list.size() )
                    {
                        Log.e( TAG, "WHAT THE FUCK ??? appsContent.size() = " + Integer.toString(appsContent.size())
                                + " category.sorted_apps_list.size() = " + Integer.toString(category.sorted_apps_list.size()));
                    }

                    catId = Integer.parseInt(category.sorted_apps_list.get(i).trim());
                }

                for ( ApplicationEntity s : appsContent )
                {
                    if (s.appid == catId)
                    {
                        tempSortedList.add(s);
                        break;
                    }
                }
            }

            adapterContent.clear();
            adapterContent.addAll(tempSortedList);
            Log.e(TAG, "adapterContent.size = "  + Integer.toString(adapterContent.size()));

            adapter.notifyDataSetChanged();
            noResult.setVisibility(View.GONE);
            grid.setVisibility(View.VISIBLE);

            if ( scrollToStart )
            {

                grid.postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        scrollToStart = false;
                        grid.smoothScrollToPosition(0);
                        Log.e(TAG, "scroll");
                    }
                },300);
            }
        } else
        {
            if ( showNoResult )
            {
                noResult.setVisibility(View.VISIBLE);
                grid.setVisibility(View.GONE);
            }
        }
    }

    private void initializeBackend()
    {
        appCounter = 0;
        startCategorySession = new Date();

        showProgress = AnimationUtils.loadAnimation(CategoryDetails.this, R.anim.show_progress_anim);
        hideProgress= AnimationUtils.loadAnimation(CategoryDetails.this, R.anim.hide_progress_anim);

        density = getResources().getDisplayMetrics().density;
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);

        Intent parent = getIntent();
        int catid = parent.getIntExtra("category_id",-1);
        searchStr = parent.getStringExtra("search");
        if (!TextUtils.isEmpty(searchStr))
        {
            handler.sendEmptyMessageDelayed(SHOW_SEARCH_VIEW, 200);
            fromMainSearch = true;
        }

        searchAppidList = (List<String>) parent.getSerializableExtra("appid");
        if ( searchAppidList == null )
            searchAppidList = new ArrayList<String>();

        if (!TextUtils.isEmpty(searchStr) && searchAppidList.size() == 0)
            emptySearch = true;
        else
            handler.sendMessage( handler.obtainMessage(UPDATE_COUNTER, searchAppidList.size(), -1));


        if ( catid != -1 )
            category = SqlAdapter.selectCategoryByID(catid);
    }

    private void initializeUI()
    {
        // activity animation
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        setContentView( R.layout.masterapp_category_details_layout);

        hideTopBar();
        backBtn  = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
            }
        });
        searchBtn = (ImageView) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBtn.setVisibility(View.GONE);
                searchCancel.setVisibility(View.VISIBLE);
                searchHolder.setVisibility(View.VISIBLE);
                searchEdit.requestFocus();
                showKeyboard();
            }
        });
        searchCancel = (TextView) findViewById(R.id.search_cancel_text);
        searchCancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBtn.setVisibility(View.VISIBLE);
                searchCancel.setVisibility(View.GONE);
                searchHolder.setVisibility(View.GONE);
                searchEdit.setText("");
                hideKeyboard();

                if ( !fromMainSearch )
                {
                    searchAppidList.clear();
                    categoryArrPosition = 0;
                    appsContent.clear();
                    loadContent();
                } else
                    handler.sendEmptyMessage(SHOW_NO_RESULT);

                handler.sendMessage( handler.obtainMessage(UPDATE_COUNTER, -1, -1));
            }
        });
        searchHolder = (LinearLayout) findViewById(R.id.search_edittext_holder);
        searchEdit = (EditTextHandleBackPressed) findViewById(R.id.search_edittext);
        if (!TextUtils.isEmpty(searchStr))
        {
            searchEdit.setText(searchStr);
            searchEdit.setSelection(searchStr.length());
        }
        searchEdit.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                emptySearch = false;

                if ( keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP )
                    return false;

                if (!TextUtils.isEmpty(textView.getText().toString()) )
                {
                    if ( Utils.checkNetwork(CategoryDetails.this) < 0)
                    {
                        Toast.makeText(CategoryDetails.this, getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
                    } else
                    {
                        showProgressDialog(getString(R.string.search));
                        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
                        new Thread( new Runnable() {
                            @Override
                            public void run() {
                                AppsId res = ServerApi.getInstance().searchSync( (category == null) ? -1:category.id, searchEdit.getText().toString());
                                if ( res != null )
                                {
                                    if( res.getApps().size() == 0  )
                                    {
                                        handler.sendEmptyMessage(SHOW_NO_RESULT);
                                    } else
                                    {
                                        scrollToStart = true;
                                        appsContent.clear();
                                        categoryArrPosition = 0;
                                        searchAppidList.clear();
                                        searchAppidList.addAll(res.getApps());
                                        handler.sendEmptyMessage(LOAD_CONTENT);
                                        handler.sendMessage( handler.obtainMessage(UPDATE_COUNTER, res.getApps().size(), -1));
                                    }
                                } else
                                {
                                    handler.sendEmptyMessage(UNABLE_CONNECT);
                                }
                            }
                        }).start();
                    }
                }

                return true;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.applist_progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            progressBar.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        noResult = (TextView) findViewById(R.id.applist_no_result);
        noResult.setVisibility(View.GONE);

        if ( category != null )
            ((TextView)findViewById(R.id.topbar_title)).setText(category.title);
        else
            ((TextView)findViewById(R.id.topbar_title)).setText(getString(R.string.search));

        setBackBtnArrow(getString(R.string.back));
        setBackBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        grid = (GridView) findViewById(R.id.applist_list);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            grid.setFriction(ViewConfiguration.getScrollFriction() * FRICTION_COEFFICIENT);
        }
        grid.setNumColumns(GRID_NUM_COLUMNS);
        grid.setVerticalScrollBarEnabled(false);
        grid.setSelector(R.drawable.listview_transperant_selector);
//        grid.setHorizontalSpacing((int) (density * GRID_ROW_SPACING));
        //grid.setVerticalSpacing((int) (density * Statics.FEATURED_IMAGE_MARGINS));
        grid.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                appCounter++;
                startAppSession =  new Date();
                Log.e(TAG, "onItemClick");
                ApplicationEntity itemTemp = adapterContent.get(position);
                ApplicationEntity itemContent = SqlAdapter.selectApplicationById(itemTemp.appid);

                Log.e(TAG, "Item = " + itemContent.toString());
                Intent bridge = new Intent(CategoryDetails.this, AppBuilder.class);
                bridge.putExtra("appid",itemContent.appid);
                bridge.putExtra("token",itemContent.token);
                bridge.putExtra("splash",itemContent.picturePath);

                ApplicationEntity favApp = SqlAdapter.selectFavouriteAppById(itemContent.appid);
                if ( favApp != null && favApp.active )
                    bridge.putExtra("favourites",true);
                overridePendingTransition(R.anim.activity_open_scale_main, R.anim.activity_close_translate_main);
                startActivity(bridge);

                // flurry event
                FlurryLogger.increaseAppOpenCount();
            }
        });

        adapter = new PagingAdapter(CategoryDetails.this, grid, adapterContent, (int) (screenWidth-(2*6*density)), GRID_NUM_COLUMNS, QUERY_APPS_COUNT);
        adapter.setListener( new PagingAdapter.OnEndReached() {
            @Override
            public void onEndReached() {
                if (!isLoading )
                {
                    loadContent();
                }
            }
        });
        grid.setAdapter( adapter );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( startAppSession != null )
            FlurryLogger.appSessionTime( new Date(new Date().getTime() - startAppSession.getTime()) );
    }

    private void showProgress( boolean show )
    {
        if (show)
        {
            progressBar.clearAnimation();
            progressBar.startAnimation(showProgress);
        } else
        {
            progressBar.clearAnimation();
            progressBar.startAnimation(hideProgress);
        }
    }

    private void loadContent()
    {
        if ( emptySearch )
        {
            handler.sendEmptyMessage(SHOW_NO_RESULT);
            return;
        }

        isLoading = true;
        showProgress(true);
        //showProgressDialog(getString(R.string.loading));

        if ( searchAppidList != null && searchAppidList.size() != 0 )
        {
            //scrollToStart = true;
            categoryAppsArr.clear();
            Integer tempPosition = new Integer(categoryArrPosition);
            int limit = (tempPosition + QUERY_APPS_COUNT) <= searchAppidList.size() ?
                    (tempPosition + QUERY_APPS_COUNT) : searchAppidList.size();
            for ( int i = tempPosition; i < limit; i++ )
            {
                categoryAppsArr.add(searchAppidList.get(i));
                categoryArrPosition++;
            }
        } else
        {
            categoryAppsArr.clear();
            Integer tempPosition = new Integer(categoryArrPosition);
            int limit = (tempPosition + QUERY_APPS_COUNT) <= category.sorted_apps_list.size() ?
                    (tempPosition + QUERY_APPS_COUNT) : category.sorted_apps_list.size();
            for ( int i = tempPosition; i < limit; i++ )
            {
                categoryAppsArr.add(category.sorted_apps_list.get(i));
                categoryArrPosition++;
            }
        }

        Log.e(TAG, "categoryAppsArr = " + Arrays.toString(categoryAppsArr.toArray()));
        List<ApplicationEntity> appsList = SqlAdapter.selectApplicationsByIdArray(categoryAppsArr);
        if ( appsList.size() == categoryAppsArr.size() ) // все есть в базе -> можно отрисовывать
        {
            Log.e(TAG, "ALL in DB");
            appsContent.addAll(appsList);
            drawUI(true);
            //hideProgressDialog();
            showProgress(false);
            isLoading = false;
        } else
        {
            appsToDownload.clear();

            for ( String s : categoryAppsArr )
            {
                boolean isFound = false;
                for ( ApplicationEntity a : appsList )
                {
                    if ( s.compareToIgnoreCase(Integer.toString(a.appid)) ==0 )
                    {
                        isFound = true;
                        break;
                    }
                }

                if ( !isFound )
                    appsToDownload.add(s);
            }

            appsContent.addAll(appsList);
            drawUI(false);

            if ( appsToDownload.size() != 0 )
            {
                new Thread( new Runnable() {
                    @Override
                    public void run() {

                        FeaturedResponse response = ServerApi.getInstance().getAppsList( appsToDownload );
                        if ( response != null && response.getAppsList().size() > 0)
                        {
                            SqlAdapter.insertApplication(response.getAppsList());
                            List<ApplicationEntity> appsList = SqlAdapter.selectApplicationsByIdArray(appsToDownload);
                            appsContent.addAll(appsList);
                            handler.sendEmptyMessage(HTTP_RESULT);
                        } else
                            handler.sendEmptyMessage(HIDE_PROGRESS);
                    }
                }).start();
            }  else
            {
                isLoading = false;
                showProgress(false);
                //hideProgressDialog();
            }
        }
    }

    private void profiling ( String msg )
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        Log.e(TAG, formatter.format(new Date()) +" "+  msg );
    }

    @Override
    protected void onDestroy() {
        adapter.clearBitmaps();
        if ( category != null && !TextUtils.isEmpty(category.title) )
            FlurryLogger.categorySessionTime( category.title, new Date( new Date().getTime() - startCategorySession.getTime()), (int) appCounter);

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryLogger.onStartInit(CategoryDetails.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryLogger.onStopInit();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
    }

    private void showKeyboard() {
        imm.toggleSoftInputFromWindow(searchEdit.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }
}
