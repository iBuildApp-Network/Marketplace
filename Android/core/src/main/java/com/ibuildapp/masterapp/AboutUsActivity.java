package com.ibuildapp.masterapp;

import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 01.10.14
 * Time: 11:57
 * To change this template use File | Settings | File Templates.
 */
public class AboutUsActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // initializeBackend();

        initializeUI();
    }

    private void initializeUI()
    {
        // activity animation
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        setContentView(R.layout.masterapp_aboutus_layout);
        setTopbarTitle(getString(R.string.about_us));

        setBackBtnArrow(getString(R.string.back));
        setBackBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        int imageW = screenWidth/5;
        LinearLayout rowOneHolder = (LinearLayout) findViewById(R.id.row_one);
        for (int i = 0; i < rowOneHolder.getChildCount(); i++) {
            View v = rowOneHolder.getChildAt(i);
            if (v instanceof ImageView) {
                v.getLayoutParams().height = imageW;
            }
        }

        LinearLayout rowTwoHolder = (LinearLayout) findViewById(R.id.row_two);
        for (int i = 0; i < rowTwoHolder.getChildCount(); i++) {
            View v = rowTwoHolder.getChildAt(i);
            if (v instanceof ImageView) {
                v.getLayoutParams().height = imageW;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity animation
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

}
