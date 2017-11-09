package com.ibuildapp.masterapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.appbuilder.core.AppBuilder;
import com.ibuildapp.masterapp.adapter.PagingAdapter;
import com.ibuildapp.masterapp.api.ServerApi;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.ApplicationEntity;
import com.ibuildapp.masterapp.model.StatusOnly;
import com.ibuildapp.masterapp.utils.Statics;
import com.ibuildapp.masterapp.utils.Utils;
import com.ibuildapp.masterapp.view.EditTextHandleBackPressed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 21.07.14
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class FavouritesActivity extends BaseActivity {
    // constants
    private final String TAG = "com.ibuildapp.masterapp.FavouritesActivity";
    private final int GRID_ROW_SPACING = 5; //DP
    private final int GRID_NUM_COLUMNS = 2; //DP
    private final int ITEMS_ON_SCREEN_COUNT = 12;
    // UI
    private GridView grid;
    private TextView noResult;
    private LinearLayout backBtn;
    private ImageView searchBtn;
    private TextView searchCancel;
    private LinearLayout searchHolder;
    private EditTextHandleBackPressed searchEdit;
    //backend
    private List<ApplicationEntity> appsContent = new ArrayList<ApplicationEntity>();
    private PagingAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeBackend();

        initializeUI();

        favouritesServerRequest();
    }

    private void favouritesServerRequest() {
        if (Utils.checkNetwork(FavouritesActivity.this) > 0)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<ApplicationEntity> unrateList = SqlAdapter.selectFavouritesByActiveByFavourited(false, null);
                    for (ApplicationEntity s : unrateList) {
                        StatusOnly status = ServerApi.getInstance().rateAppSync(s.appid, Statics.uuid, false);
                        switch (status.getStatus()) {
                            case 0: {
                                SqlAdapter.deleteFavourite(s.appid);
                            }
                            break;

                            case 1: {
                                SqlAdapter.deleteFavourite(s.appid);
                            }
                            break;

                            case -1: {
                                Log.e(TAG, "No such app ");
                            }
                            break;
                        }
                    }

                    List<ApplicationEntity> rateList = SqlAdapter.selectFavouritesByActiveByFavourited(true, false);
                    for (ApplicationEntity s : rateList) {
                        StatusOnly status = ServerApi.getInstance().rateAppSync(s.appid, Statics.uuid, true);
                        if (status != null) {
                            switch (status.getStatus()) {
                                case 0: {
                                    SqlAdapter.updateFavourite(s.appid, true, true);
                                }
                                break;

                                case 1: {
                                    SqlAdapter.updateFavourite(s.appid, true, true);
                                }
                                break;

                                case -1: {
                                    Log.e(TAG, "No such app ");
                                }
                                break;
                            }
                        }
                    }
                }
            }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(searchEdit.getText().toString())) {
            List<ApplicationEntity> res = SqlAdapter.selectFavouriteLike(searchEdit.getText().toString());
            if (res.size() > 0) {
                grid.setVisibility(View.VISIBLE);
                noResult.setVisibility(View.GONE);
                appsContent.clear();
                appsContent.addAll(res);
                sortByName();
                adapter.notifyDataSetChanged();
            } else {
                grid.setVisibility(View.GONE);
                noResult.setVisibility(View.VISIBLE);
            }
        } else {
            List<ApplicationEntity> res = SqlAdapter.selectAllFavourites();
            if (res.size() > 0) {
                grid.setVisibility(View.VISIBLE);
                noResult.setVisibility(View.GONE);
                appsContent.clear();
                appsContent.addAll(res);
                sortByName();
                adapter.notifyDataSetChanged();
            } else {
                grid.setVisibility(View.GONE);
                noResult.setVisibility(View.VISIBLE);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void initializeBackend() {
        //TODO
    }

    private void initializeUI() {
        // activity animation
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        setContentView(R.layout.masterapp_favourites_layout);
        setTopbarTitle(getString(R.string.favourites));
        hideTopBar();

        backBtn = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(getButtonsCLick());
        searchBtn = (ImageView) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(getButtonsCLick());
        searchCancel = (TextView) findViewById(R.id.search_cancel_text);
        searchCancel.setOnClickListener(getButtonsCLick());
        searchHolder = (LinearLayout) findViewById(R.id.search_edittext_holder);
        searchEdit = (EditTextHandleBackPressed) findViewById(R.id.search_edittext);
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP)
                    return false;

                List<ApplicationEntity> res = SqlAdapter.selectFavouriteLike(textView.getText().toString());

//                Collections.sort(res, new Comparator<ApplicationEntity>() {
//                    @Override
//                    public int compare(ApplicationEntity first, ApplicationEntity second) {
//                        return first.timestamp > second.timestamp ? -1 : first.timestamp < second.timestamp ? 1 : 0;
//                    }
//                });

                if (res.size() > 0) {
                    grid.setVisibility(View.VISIBLE);
                    noResult.setVisibility(View.GONE);
                    appsContent.clear();
                    appsContent.addAll(res);
                    sortByName();
                    adapter.notifyDataSetChanged();

                    grid.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            grid.smoothScrollToPosition(0);
                        }
                    }, 300);
                } else {
                    grid.setVisibility(View.GONE);
                    noResult.setVisibility(View.VISIBLE);
                }

                return true;
            }
        });

        grid = (GridView) findViewById(R.id.favourites_list);
        grid.setNumColumns(GRID_NUM_COLUMNS);
        grid.setVerticalScrollBarEnabled(false);
        grid.setSelector(R.drawable.listview_transperant_selector);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ApplicationEntity itemTemp = appsContent.get(position);
                ApplicationEntity itemContent = SqlAdapter.selectApplicationById(itemTemp.appid);

                // TODO запуск ядра!!
                Intent bridge = new Intent(FavouritesActivity.this, AppBuilder.class);
                bridge.putExtra("appid", itemContent.appid);
                bridge.putExtra("token", itemContent.token);
                bridge.putExtra("splash", itemContent.picturePath);

                ApplicationEntity favApp = SqlAdapter.selectFavouriteAppById(itemContent.appid);
                if (favApp != null && favApp.active)
                    bridge.putExtra("favourites", true);

                startActivity(bridge);
            }
        });

        adapter = new PagingAdapter(FavouritesActivity.this, grid, appsContent, (int) (screenWidth-(2*6*density)), GRID_NUM_COLUMNS, ITEMS_ON_SCREEN_COUNT);
        grid.setAdapter(adapter);

        noResult = (TextView) findViewById(R.id.favourites_no_result);
        noResult.setVisibility(View.GONE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity animation
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
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

                        List<ApplicationEntity> res = SqlAdapter.selectAllFavourites();
                        if ( res.size() > 0 )
                        {
                            grid.setVisibility(View.VISIBLE);
                            noResult.setVisibility(View.GONE);
                            appsContent.clear();
                            appsContent.addAll(res);
                            sortByName();
                            adapter.notifyDataSetChanged();
                        } else
                        {
                            grid.setVisibility(View.GONE);
                            noResult.setVisibility(View.VISIBLE);
                        }
                    }
                    break;

                    case R.id.back_btn: {
                        finish();
                    }
                    break;
                }
            }
        };
    }

    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
    }

    private void showKeyboard() {
        imm.toggleSoftInputFromWindow(searchEdit.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    private void sortByName() {
        Collections.sort(appsContent, new Comparator<ApplicationEntity>() {
            @Override
            public int compare(ApplicationEntity first, ApplicationEntity second) {
                if(first.title == null)
                    return -1;

                return first.title.compareTo(second.title);
            }
        });
    }

}
