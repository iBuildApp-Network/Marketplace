package com.ibuildapp.masterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 21.07.14
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class DealsActivity extends BaseActivity {
    // UI
    private LinearLayout root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeBackend();

        initializeUI();


    }

    private void initializeBackend()
    {
        //TODO
    }

    private void initializeUI()
    {
        setContentView(R.layout.masterapp_deals_layout);
        setTopbarTitle(getString(R.string.deals));

        setBackBtnArrow(getString(R.string.back));
        setBackBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        root = (LinearLayout) findViewById(R.id.deals_root);
    }

}
