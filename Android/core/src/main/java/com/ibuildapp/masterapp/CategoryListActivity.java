package com.ibuildapp.masterapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ibuildapp.masterapp.adapter.CategoryPickerAdapter;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.CategoryEntity;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 21.07.14
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
public class CategoryListActivity extends Activity {

    private ListView list;
    private List<CategoryEntity> categoryList;

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryList = SqlAdapter.selectAllCategory();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.masterapp_category_list_layout);
        list = (ListView) findViewById(R.id.categorylist_list);
        list.setBackgroundColor(Color.WHITE);
        list.setSelector(R.drawable.listview_custom_background);
        list.setAdapter( new CategoryPickerAdapter(CategoryListActivity.this, categoryList));
        list.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent res = new Intent();
                res.putExtra("category", categoryList.get(i));
                setResult(RESULT_OK,res);
                finish();
            }
        });


    }
}
