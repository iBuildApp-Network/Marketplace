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
package com.appbuilder.sdk.android.animations;


import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.appbuilder.sdk.android.animations.SimpleAnimationListener;

public abstract class AnimUtils {
    public static Animation createGoneRightAnimation(final View v, int duration, int displayWidth){
        int []screenSize = new int[2];
        v.getLocationOnScreen(screenSize);
        TranslateAnimation animation = new TranslateAnimation(0, displayWidth-screenSize[0],0,0);
        animation.setDuration(duration);
        animation.setAnimationListener(new SimpleAnimationListener(){
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
            }
        });
        return animation;
    }

    public static Animation createGoneLeftAnimation(final View v, int duration){
        int []screenSize = new int[2];
        v.getLocationOnScreen(screenSize);
        int diff = -screenSize[0]-v.getWidth();
        TranslateAnimation animation = new TranslateAnimation(0, diff,0,0);
        animation.setDuration(duration);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }
        });
        return animation;
    }

    public static  Animation createAlphaGoneAnimation(final View v, int duration){
        AlphaAnimation animation = new AlphaAnimation(1f, 0f);
        animation.setDuration(duration);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
            }
        });
        return animation;
    }

    public static  Animation createBottomShowAnimation(final View v, int duration){
        int []screenSize = new int[2];
        v.getLocationOnScreen(screenSize);
        int diff = screenSize[1]+v.getWidth();
        TranslateAnimation animation = new TranslateAnimation(0, 0,diff,0);
        animation.setDuration(duration);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
        });
        return animation;
    }

    public static  Animation createBottomGoneAnimation(final View v, int duration){
        int []screenSize = new int[2];
        v.getLocationOnScreen(screenSize);
        int diff = screenSize[1]+v.getWidth();
        TranslateAnimation animation = new TranslateAnimation(0, 0,0,diff);
        animation.setDuration(duration);
        return animation;
    }

    public static Animation createShowRightAnimation(final View v, int duration, int displayWidth){
        int []screenSize = new int[2];
        v.getLocationOnScreen(screenSize);
        TranslateAnimation animation = new TranslateAnimation(displayWidth-screenSize[0],0,0,0);
        animation.setDuration(duration);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
        });
        return animation;
    }

    public static Animation createShowLeftAnimation(final View v, int duration){
        int []screenSize = new int[2];
        v.getLocationOnScreen(screenSize);
        int diff = -screenSize[0]-v.getWidth();
        TranslateAnimation animation = new TranslateAnimation(diff,0,0,0);
        animation.setDuration(duration);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
        });
        return animation;
    }

    public static Animation createShowAlphaAnimation(final View v, int duration){
        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(duration);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
        });
        return animation;
    }
}
