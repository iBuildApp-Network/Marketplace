package com.ibuildapp.masterapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import com.ibuildapp.masterapp.DAO.TemplatesDAO;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.CategoryEntity;
import com.ibuildapp.masterapp.model.TemplateResponse;
import com.ibuildapp.masterapp.utils.Statics;
import com.ibuildapp.masterapp.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 21.07.14
 * Time: 12:45
 * To change this template use File | Settings | File Templates.
 */
public class NewBusinessActivity extends BaseActivity implements View.OnClickListener {

    // constants
    private final int CHOOSE_CATEGORY = 10001;

    // UI
    private LinearLayout root;
    private RelativeLayout categoryHolder;
    private LinearLayout facebookHolder;
    private LinearLayout websiteHolder;

    private TextView categoryName;
    private TextView enterManually;
    private EditText facebookEdit;
    private EditText websiteEdit;

    // backend
    private CategoryEntity currentCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeBackend();

        initializeUI();
    }

    private void initializeBackend()
    {
        currentCategory = SqlAdapter.selectFirstCategory();
    }

    private void initializeUI()
    {
        setContentView(R.layout.masterapp_new_business_layout);
        setTopbarTitle(getString(R.string.add_new_business_title));

        setBackBtnArrow(getString(R.string.back));
        setBackBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setEditBtnText(getString(R.string.done));
        setEditBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.checkNetwork(NewBusinessActivity.this) < 0)
                {
                    Toast.makeText(NewBusinessActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(facebookEdit.getText().toString()))
                {
                    Toast.makeText(NewBusinessActivity.this, "Запрос на сервер на добавление нового приложения через FB", Toast.LENGTH_SHORT).show();

//                    String urlStr = new String(facebookEdit.getText().toString());
//                    if ( !urlStr.contains("http://") )
//                        urlStr = "http://" + urlStr;
//
//                    if ( Patterns.WEB_URL.matcher(urlStr).matches() )
//                    {
//                        // TODO
//
//                    } else
//                    {
//                        Toast.makeText(NewBusinessActivity.this, urlStr + " " + getString(R.string.incorrect_fb_page), Toast.LENGTH_SHORT).show();
//                        facebookHolder.setBackgroundResource(R.drawable.red_border);
//                    }
                } else
                {

                    String urlStr = new String(websiteEdit.getText().toString());
                    if ( !urlStr.contains("http://") )
                        urlStr = "http://" + urlStr;

                    if ( Patterns.WEB_URL.matcher(urlStr).matches() )
                    {
                        // TODO
                        Toast.makeText(NewBusinessActivity.this, "Запрос на сервер на добавление нового приложения через Site url", Toast.LENGTH_SHORT).show();
                    } else
                    {
                        websiteHolder.setBackgroundResource(R.drawable.red_border);
                        Toast.makeText(NewBusinessActivity.this, urlStr +" "+ getString(R.string.incorrect_website_url), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        hideEditBtn();

        root = (LinearLayout) findViewById(R.id.business_root);
        categoryHolder = (RelativeLayout) findViewById(R.id.business_change_category);
        facebookHolder = (LinearLayout) findViewById(R.id.business_fb_edit_holder);
        websiteHolder = (LinearLayout) findViewById(R.id.business_website_edit_holder);
        categoryName = (TextView) findViewById(R.id.business_category);
        enterManually  = (TextView) findViewById(R.id.business_manually);
        facebookEdit = (EditText) findViewById(R.id.business_fb_edit);
        facebookEdit.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if ( s.length() == 0 )
                {
                    hideEditBtn();
                    websiteEdit.setEnabled(true);
                }
                else
                {
                    showEditBtn();
                    websiteEdit.setEnabled(false);
                }
                facebookHolder.setBackgroundResource(R.drawable.grey_border);
            }
        });

        websiteEdit  = (EditText) findViewById(R.id.business_website_edit);
        websiteEdit.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if ( s.length() == 0 )
                {
                    hideEditBtn();
                    facebookEdit.setEnabled(true);
                }
                else
                {
                    showEditBtn();
                    facebookEdit.setEnabled(false);
                }
                websiteHolder.setBackgroundResource(R.drawable.grey_border);
            }
        });


        categoryHolder.setOnClickListener( this );
//        facebookHolder.setOnClickListener( this );
//        websiteHolder.setOnClickListener( this );
        enterManually.setOnClickListener( this );

        if (currentCategory != null)
            categoryName.setText(currentCategory.title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.business_change_category:
            {
                Intent bridge = new Intent(NewBusinessActivity.this, CategoryListActivity.class);
                startActivityForResult(bridge,CHOOSE_CATEGORY);
            } break;

            case R.id.business_fb_edit_holder:
            {
                facebookEdit.requestFocus();
            } break;

            case R.id.business_website_edit_holder:
            {
                websiteEdit.requestFocus();
            } break;

            case R.id.business_manually:
            {
                TemplateResponse templateList = new TemplatesDAO(Statics.cachePath).getTemplates();
                if ( templateList != null )
                {
                    Intent bridge = new Intent(NewBusinessActivity.this, NewBusinessManuallyActivity.class);
                    bridge.putExtra("category",currentCategory);
                    startActivity(bridge);
                } else
                    Toast.makeText(NewBusinessActivity.this, getString(R.string.no_templates), Toast.LENGTH_SHORT).show();

            } break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == CHOOSE_CATEGORY )
        {
            if ( resultCode == RESULT_OK )
            {
                currentCategory = (CategoryEntity) data.getSerializableExtra("category");
                if ( currentCategory != null )
                    categoryName.setText(currentCategory.title);
            }
        }
    }
}
