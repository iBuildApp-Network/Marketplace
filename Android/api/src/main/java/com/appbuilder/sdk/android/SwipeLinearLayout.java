/****************************************************************************
 * *
 * Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
 * *
 * This file is part of iBuildApp.                                          *
 * *
 * This Source Code Form is subject to the terms of the iBuildApp License.  *
 * You can obtain one at http://ibuildapp.com/license/                      *
 * *
 ****************************************************************************/
package com.appbuilder.sdk.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 10.06.13
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class SwipeLinearLayout extends LinearLayout {

    private final int SWIPE_THRESHOLD;
    private static final int SWIPE_VELOCITY_THRESHOLD = 50;

    private GestureDetector gestureDetector;
    private OnSwipeInterface eventsHandler;
    private MotionEvent previous = null;
    private boolean showSidebar;

    public SwipeLinearLayout(Context context, boolean showSidebar) {
        super(context);

        this.showSidebar = showSidebar;

        if (showSidebar)
            this.gestureDetector = new GestureDetector(context, new GestureListener());

        SWIPE_THRESHOLD = Float.valueOf(context.getResources().getDisplayMetrics().widthPixels / 4f).intValue();
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            if (eventsHandler != null)
                eventsHandler.onTouchEvent(e.getX());

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;

            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            if (eventsHandler != null)
                                eventsHandler.onSwipeRight();
                            result = true;
                        } else {
                            if (eventsHandler != null)
                                eventsHandler.onSwipeLeft();
                            result = true;
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            if (eventsHandler != null)
                                eventsHandler.onSwipeBottom();
                            result = true;
                        } else {
                            if (eventsHandler != null)
                                eventsHandler.onSwipeTop();
                            result = true;
                        }
                    }
                }
                return result;
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (showSidebar) {
            int action = ev.getAction();

            // Always handle the case of the touch gesture being complete.
            if (action == MotionEvent.ACTION_CANCEL) {
                previous = MotionEvent.obtain(ev);

                return false;
            } else if (action == MotionEvent.ACTION_UP) {
                previous = MotionEvent.obtain(ev);

                return eventsHandler != null && eventsHandler.onTouchEvent(ev.getX());
            }

            // handle ACTION_MOVE events
            switch (action) {
                case (MotionEvent.ACTION_MOVE): {
                    if (previous.getAction() == MotionEvent.ACTION_MOVE) {
                        float diffY = ev.getY() - previous.getY();
                        float diffX = ev.getX() - previous.getX();
                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                                if (diffX > 0) {
                                    if (eventsHandler != null)
                                        eventsHandler.onSwipeRight();
                                    previous = MotionEvent.obtain(ev);

                                    return true;
                                } else {
                                    if (eventsHandler != null)
                                        eventsHandler.onSwipeLeft();
                                    previous = MotionEvent.obtain(ev);

                                    return true;
                                }
                            }
                        }
                    }
                }
                break;

            }
            previous = MotionEvent.obtain(ev);
        }

        return false; // разрешить дочерним VIEW обрабатывать это событие, т.е. не перехватывать!

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (showSidebar)
            gestureDetector.onTouchEvent(event);

        return true;
    }

    public void setOnSwipeEvents(OnSwipeInterface I) {
        this.eventsHandler = I;
    }

    public void disableSwipe() {
        showSidebar = false;
    }

    public void enableSwipe() {
        showSidebar = true;
    }

}
