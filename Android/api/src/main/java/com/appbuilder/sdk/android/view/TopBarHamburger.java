/**
 * *************************************************************************
 * *
 * Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
 * *
 * This file is part of iBuildApp.                                          *
 * *
 * This Source Code Form is subject to the terms of the iBuildApp License.  *
 * You can obtain one at http://ibuildapp.com/license/                      *
 * *
 * **************************************************************************
 */
package com.appbuilder.sdk.android.view;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appbuilder.sdk.android.BarDesigner;
import com.appbuilder.sdk.android.R;


public class TopBarHamburger extends LinearLayout{
    private ImageView hamburgerView;
    private BarDesigner design;
    private static float density = 0f;
    public TopBarHamburger(Context context, BarDesigner design) {
        super(context);
        this.design = design;

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        params.weight = 5;
        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;

        setLayoutParams(params);
        if (density == 0)
            density = getResources().getDisplayMetrics().density;

        hamburgerView = new ImageView(context);
        LayoutParams viewParams = new LayoutParams((int) (25*density), (int) (22*density));
        params.gravity = Gravity.CENTER;
        hamburgerView.setLayoutParams(viewParams);

       hamburgerView.setImageResource(R.drawable.hamburger_white);
        hamburgerView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(hamburgerView);
    }


    public void setBlack() {
        hamburgerView.setImageResource(R.drawable.hamburger_black);
    }
}
