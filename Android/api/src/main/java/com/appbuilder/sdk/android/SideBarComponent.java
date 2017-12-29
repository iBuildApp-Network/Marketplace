package com.appbuilder.sdk.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 06.10.14
 * Time: 12:35
 * To change this template use File | Settings | File Templates.
 */
public class SideBarComponent extends FrameLayout {
    // constant
    private final int SENSETIVE_AREA_SIZE = 80;
    private final int SCROLLER_ANIMATION_DURATION = 250;
    private final int OVERSCROLLER_ANIMATION_DURATION = 100;
    private final float DEFAULT_SIDEHOLDER_WIDTH_PERSENT = 0.8f;
    private String TAG = "com.example.SideBarScrollTest.view.SideBarComponent";
    private final int UPDATE_POSITION = 200001;

    private final int LEFT_SIDE_HOLDER = 1001;
    private final int RIGHT_SIDE_HOLDER = 1002;
    private final int CONTENT_SIDE_HOLDER = 1003;

    private final int LEFT_SIDE_BLUR_IMAGE = 1004;
    private final int RIGHT_SIDE_BLUR_IMAGE = 1005;

    // ui
    private FrameLayout leftHolder;
    private FrameLayout rightHolder;

    private View leftChildView;
    private View rightChildView;
    private LinearLayout contentHolder;

    // backedn
    private Scroller scroller;
    private float density;
    private int screenWidth;
    private int screenHeight;
    private boolean isLeftShowen = false;
    private boolean isRightShowen = false;
    private GestureDetector detector;
    private int leftSideSize;
    private int rightSideSize;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_POSITION: {
                    AnimationProtocol packet = (AnimationProtocol) msg.obj;
//                    Log.e(TAG, "xLeft = " + packet.xLeftMargin + " xRight = " + packet.xRightMargin +
//                            " leftShowen = " + packet.leftShowen+ " rightShowen = " + packet.rightShowen);

                    LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();

                    if ( packet.xLeftMargin != Integer.MAX_VALUE )
                        params.setMargins(packet.xLeftMargin, 0, 0, 0);
                    contentHolder.setLayoutParams(params);

                    isLeftShowen = packet.leftShowen;
                    isRightShowen= packet.rightShowen;
                }
                break;
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
        params.width = getMeasuredWidth();
        contentHolder.setLayoutParams(params);
    }

    public void refreshSize()
    {
        final LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
        Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, isRightShowen, params.leftMargin, Integer.MAX_VALUE));
        handler.sendMessage(msg);
    }

    public SideBarComponent(Context context) {
        super(context);
        init();
    }

    public SideBarComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SideBarComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        density = getResources().getDisplayMetrics().density;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        leftHolder = new FrameLayout(getContext());
        leftHolder.setId(LEFT_SIDE_HOLDER);
        leftHolder.setBackgroundColor(Color.RED);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.LEFT;
        leftHolder.setLayoutParams(params);
        addView(leftHolder);

        rightHolder = new FrameLayout(getContext());
        rightHolder.setId(RIGHT_SIDE_HOLDER);
        rightHolder.setBackgroundColor(Color.parseColor("#00ff00"));
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        addView(rightHolder,params);

        contentHolder = new LinearLayout(getContext());
        contentHolder.setBackgroundColor(Color.BLACK);
        contentHolder.setId(CONTENT_SIDE_HOLDER);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity= Gravity.LEFT;
        addView(contentHolder, params);

        // after view has been drawen set width and height in pixels - need, cause we will regulate content holder over margins in framelayout
        // после того как view отрисуется нужно задать ей размер в пикселях - иначе не будет работать scroll, т.к. мы регулируем положение contentHolder через margins в frameLayout
//        contentHolder.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
//                params.width = contentHolder.getWidth();
//                params.height = contentHolder.getHeight();
//                contentHolder.setLayoutParams(params);
//            }
//        }, 100);

        detector = new GestureDetector(getContext(), new MyGestureListener());
        scroller = new Scroller(getContext(), new DecelerateInterpolator(0.5f));

    }

//    public void recomputeContentSize()
//    {
//        LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
//        params.width = contentHolder.getWidth();
//        params.height = contentHolder.getHeight();
//        contentHolder.setLayoutParams( params);
//    }

    public boolean isLeftShowen() {
        return isLeftShowen;
    }

    public boolean isRightShowen() {
        return true;//isRightShowen;
    }

    /**
     * Set the left bar view
     * @param leftView - view to add
     */
    public void setLeftSideView( View leftView ) throws NullPointerException
    {
        if ( leftView == null )
            throw new NullPointerException();

        leftChildView = leftView;

        if ( leftChildView.getLayoutParams() == null )
        {
            leftChildView.setLayoutParams( new ViewGroup.LayoutParams((int) (screenWidth * DEFAULT_SIDEHOLDER_WIDTH_PERSENT), ViewGroup.LayoutParams.MATCH_PARENT));
            leftSideSize = (int) (screenWidth * DEFAULT_SIDEHOLDER_WIDTH_PERSENT);
        } else
        {
            leftSideSize = leftChildView.getLayoutParams().width;
        }

        leftHolder.removeAllViews();
        leftHolder.addView(leftChildView);
    }

    /**
     * Set the left bar view
     * @param rightView - view to add
     */
    public void setRightSideView( View rightView ) throws NullPointerException
    {
        if ( rightView == null )
            throw new NullPointerException();

        rightChildView = rightView;

        if ( rightChildView.getLayoutParams() == null )
        {
            rightChildView.setLayoutParams( new ViewGroup.LayoutParams((int) (screenWidth * DEFAULT_SIDEHOLDER_WIDTH_PERSENT), ViewGroup.LayoutParams.MATCH_PARENT));
            rightSideSize = (int) (screenWidth * DEFAULT_SIDEHOLDER_WIDTH_PERSENT);
        } else
        {
            rightSideSize = rightChildView.getLayoutParams().width;
        }

        rightHolder.removeAllViews();
        rightHolder.addView(rightChildView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ( detector.onTouchEvent(event) ) return true;

        if ((event.getPointerCount() == 1) && ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP))
        {
            //Log.e(TAG, "onTouchEvent");

            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
            if ( params.leftMargin == 0 || params.leftMargin == leftSideSize || params.leftMargin == -rightSideSize )
                return super.onTouchEvent(event);

            if ( params.leftMargin > 0 )  // need to overscroll left side
            {
                if ( params.leftMargin > leftSideSize/2 ) // overscroll right
                {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                            scroller.abortAnimation();
                            scroller.startScroll(0, 0, leftSideSize-params.leftMargin, 0, OVERSCROLLER_ANIMATION_DURATION);
                            int oldMargin = params.leftMargin;
                            while ( scroller.computeScrollOffset() )
                            {
                                int x = scroller.getCurrX();
                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, oldMargin + x, Integer.MAX_VALUE));
                                handler.sendMessage(msg);

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // control message with final coordinat point
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(true, isRightShowen, leftSideSize, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();

                } else // overscroll left
                {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                            scroller.abortAnimation();
                            scroller.startScroll(0, 0, params.leftMargin, 0, OVERSCROLLER_ANIMATION_DURATION);
                            int oldMargin = params.leftMargin;
                            while ( scroller.computeScrollOffset() )
                            {
                                int x = scroller.getCurrX();
                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, oldMargin-x, Integer.MAX_VALUE));
                                handler.sendMessage(msg);

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // control message with final coordinat point
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, 0, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            } else if ( params.leftMargin < 0 )
            {
                if ( Math.abs(params.leftMargin) < rightSideSize/2 ) // overscroll right
                {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                            scroller.abortAnimation();
                            scroller.startScroll(0, 0, Math.abs(params.leftMargin), 0, OVERSCROLLER_ANIMATION_DURATION);
                            int oldMargin = params.leftMargin;
                            while ( scroller.computeScrollOffset() )
                            {
                                int x = scroller.getCurrX();
                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, oldMargin + x, Integer.MAX_VALUE));
                                handler.sendMessage(msg);

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // control message with final coordinat point
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, 0, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();

                } else if ( Math.abs(params.leftMargin) > rightSideSize/2 ) // overscroll left
                {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                            scroller.abortAnimation();
                            scroller.startScroll(0, 0, rightSideSize + params.leftMargin, 0, OVERSCROLLER_ANIMATION_DURATION);
                            int oldMargin = params.leftMargin;
                            while ( scroller.computeScrollOffset() )
                            {
                                int x = scroller.getCurrX();
                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, oldMargin - x, Integer.MAX_VALUE));
                                handler.sendMessage(msg);

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // control message with final coordinat point
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, true, -rightSideSize, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if ( detector.onTouchEvent(ev) ) return true;

        return super.onInterceptTouchEvent(ev);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
            //Log.e(TAG, " e1.getRawX() = " + e1.getRawX() + " e2.getRawX() = " + e2.getRawX() + " distanceX = " + distanceX);

            // check sensetive area size
            //Log.e(TAG, "e1.getRawX() = " + e1.getRawX());
            if ( !isLeftShowen && !isRightShowen ) // nothing showed
            {
                // check direction
                if ( e2.getRawX() > e1.getRawX() ) // -->
                {
                    if ( e1.getRawX() > (SENSETIVE_AREA_SIZE * density) )
                        return false;
                } else if ( e2.getRawX() < e1.getRawX() ) // <--
                {

//                    if ( e1.getRawX() < ( screenWidth -  (SENSETIVE_AREA_SIZE * density)) )
                        return false;
                }
            } else if ( isLeftShowen )
            {
                if ( e1.getRawX() < ( screenWidth -  (SENSETIVE_AREA_SIZE * density)) )
                    return false;
            } else if ( isRightShowen )
            {
                if ( e1.getRawX() > ( rightSideSize) )
                    return false;
            }

            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
            if ( params.leftMargin > 0 ) // left pannel is visible
            {
                leftHolder.setVisibility(VISIBLE);
                rightHolder.setVisibility(GONE);

                try {
                    Drawable leftDrawable = leftChildView.getBackground();
                    setBackgroundDrawable(leftDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ( params.leftMargin < 0 )
            {
                leftHolder.setVisibility(GONE);
                rightHolder.setVisibility(VISIBLE);

                try {
                    Drawable rightDrawable = rightChildView.getBackground();
                    setBackgroundDrawable(rightDrawable);
                    //setBackground( rightDrawable );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // enable/disable scroll
            if ( e2.getRawX() > e1.getRawX() ) // -->
            {
                if ( !isRightShowen )
                {
                    if ( leftHolder.getChildCount() == 0 )
                        return false;
                }
            } else if ( e2.getRawX() < e1.getRawX() ) // <--
            {
                if ( !isLeftShowen )
                {
                    if ( rightHolder.getChildCount() == 0 )
                        return false;
                }
            }

            // отлавливаем события ток по X
            if ( Math.abs(distanceX) > Math.abs(distanceY) )
            {
                // check for overscroll -
                int newMargin = params.leftMargin + (int)(-distanceX);
//                if ( e2.getRawX() > e1.getRawX() ) // -->
//                {
//                    //Log.e(TAG, "--> Marg = " + newMargin);
//                    if ( isLeftShowen )
//                    {
//                        if ( Math.abs( newMargin ) > leftSideSize )
//                            return false;
//                    }
//                } else if ( e2.getRawX() < e1.getRawX() ) // <--
//                {
//                    //Log.e(TAG, "<-- Marg = " + newMargin);
//                    if ( isRightShowen )
//                    {
//                        if ( Math.abs( newMargin ) > rightSideSize )
//                            return false;
//                    }
//                }

                params.setMargins(newMargin, 0, 0, 0);
                contentHolder.setLayoutParams(params);

//                Message msg = handler.obtainMessage( UPDATE_BLUR, new AnimationProtocol(isLeftShowen, isRightShowen, newMargin, Integer.MAX_VALUE ) );
//                handler.sendMessage(msg);

                return true;
            }

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.e(TAG, "Motioin1 = " + e1 + "Motioin2 = " + e2 + "velocityX = " + velocityX + "velocityY = " + velocityY );

            // check direction
            if ( e2.getRawX() > e1.getRawX() ) // fling to right -->
            {
                Log.e( TAG, "onFling() -->" );

                if ( !isLeftShowen && !isRightShowen )
                {
                    if ( leftHolder.getChildCount() == 0 )
                        return super.onFling(e1, e2, velocityX, velocityY);

                    leftHolder.setVisibility( VISIBLE );
                    rightHolder.setVisibility( GONE );

                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                            scroller.abortAnimation();
                            scroller.startScroll(0, 0, leftSideSize - params.leftMargin, 0, OVERSCROLLER_ANIMATION_DURATION);
                            int oldMargin = params.leftMargin;
                            while ( scroller.computeScrollOffset() )
                            {
                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, oldMargin+scroller.getCurrX(), Integer.MAX_VALUE));
                                handler.sendMessage(msg);

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // control message with final coordinat point
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(true, isRightShowen, leftSideSize, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();

                    return true;
                } else if ( isRightShowen )
                {
                    leftHolder.setVisibility( GONE );
                    rightHolder.setVisibility( VISIBLE );

                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                            scroller.abortAnimation();
                            scroller.startScroll(0, 0, Math.abs(params.leftMargin), 0, OVERSCROLLER_ANIMATION_DURATION);
                            int oldMargin = params.leftMargin;
                            while ( scroller.computeScrollOffset() )
                            {
                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, oldMargin + scroller.getCurrX(), Integer.MAX_VALUE));
                                handler.sendMessage(msg);

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // control message with final coordinat point
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, 0, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();

                    return true;
                }
            }
//            else if ( e2.getRawX() < e1.getRawX() ) // fling to left <--
//            {
//                Log.e(TAG, "onFling() <--");
//
//                if ( isLeftShowen )
//                {
//                    leftHolder.setVisibility( VISIBLE );
//                    rightHolder.setVisibility( GONE );
//
//                    new Thread( new Runnable() {
//                        @Override
//                        public void run() {
//                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
//                            scroller.abortAnimation();
//                            scroller.startScroll(0, 0, params.leftMargin, 0, OVERSCROLLER_ANIMATION_DURATION);
//                            int oldMargin = params.leftMargin;
//                            while ( scroller.computeScrollOffset() )
//                            {
//                                int x = scroller.getCurrX();
//                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, oldMargin-x, Integer.MAX_VALUE));
//                                handler.sendMessage(msg);
//
//                                try {
//                                    Thread.sleep(10);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            // control message with final coordinat point
//                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, 0, Integer.MAX_VALUE));
//                            handler.sendMessage(msg);
//                        }
//                    }).start();
//
//                    return true;
//                } else if ( !isLeftShowen && !isRightShowen )
//                {
//                    if ( rightHolder.getChildCount() == 0 )
//                        return super.onFling(e1, e2, velocityX, velocityY);
//
//                    leftHolder.setVisibility( GONE );
//                    rightHolder.setVisibility( VISIBLE );
//
//                    new Thread( new Runnable() {
//                        @Override
//                        public void run() {
//                            LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
//                            scroller.abortAnimation();
//                            scroller.startScroll(0, 0, rightSideSize+params.leftMargin, 0, OVERSCROLLER_ANIMATION_DURATION);
//                            int oldMargin = params.leftMargin;
//                            while ( scroller.computeScrollOffset() )
//                            {
//                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, oldMargin - scroller.getCurrX(), Integer.MAX_VALUE));
//                                handler.sendMessage(msg);
//
//                                try {
//                                    Thread.sleep(10);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            // control message with final coordinat point
//                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, true, -rightSideSize, Integer.MAX_VALUE));
//                            handler.sendMessage(msg);
//                        }
//                    }).start();
//
//                    return true;
//                }
//            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     *  Toggle left side if left side exists
     */
    public void toggleLeftSide()
    {
        leftHolder.setVisibility(VISIBLE);
        rightHolder.setVisibility(GONE);

        if ( !isLeftShowen )
        {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    scroller.abortAnimation();
                    scroller.startScroll(0, 0, leftSideSize, 0, SCROLLER_ANIMATION_DURATION);
                    while ( scroller.computeScrollOffset() )
                    {
                        int x = scroller.getCurrX();
                        Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, x, Integer.MAX_VALUE));
                        handler.sendMessage(msg);

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // control message with final coordinat point
                    Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(true, isRightShowen, leftSideSize, Integer.MAX_VALUE));
                    handler.sendMessage(msg);
                }
            }).start();
        } else
        {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    scroller.abortAnimation();
                    scroller.startScroll(0, 0, leftSideSize, 0, SCROLLER_ANIMATION_DURATION);
                    while ( scroller.computeScrollOffset() )
                    {
                        int x = scroller.getCurrX();
                        Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, leftSideSize - x, Integer.MAX_VALUE));
                        handler.sendMessage(msg);

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // control message with final coordinat point
                    Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, 0, Integer.MAX_VALUE));
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    /**
     *  Toggle right side if right side exists
     */
    public void toggleRightSide()
    {
        leftHolder.setVisibility( GONE );
        rightHolder.setVisibility( VISIBLE );

        if ( !isRightShowen )
        {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    scroller.abortAnimation();
                    scroller.startScroll(0, 0, rightSideSize, 0, SCROLLER_ANIMATION_DURATION);
                    while ( scroller.computeScrollOffset() )
                    {
                        int x = scroller.getCurrX();
                        Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, -x, Integer.MAX_VALUE));
                        handler.sendMessage(msg);

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // control message with final coordinat point
                    Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, true, -rightSideSize, Integer.MAX_VALUE));
                    handler.sendMessage(msg);
                }
            }).start();
        } else
        {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    scroller.abortAnimation();
                    scroller.startScroll(0, 0, rightSideSize, 0, SCROLLER_ANIMATION_DURATION);
                    while ( scroller.computeScrollOffset() )
                    {
                        int x = scroller.getCurrX();
                        Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, -rightSideSize + x, Integer.MAX_VALUE));
                        handler.sendMessage(msg);

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // control message with final coordinat point
                    Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, 0, Integer.MAX_VALUE));
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    @Override
    public void addView(View child) {
        if ( child.getId() == LEFT_SIDE_HOLDER || child.getId() == RIGHT_SIDE_HOLDER || child.getId() == CONTENT_SIDE_HOLDER )
        {
            super.addView(child);
            return;
        }

        if ( contentHolder != null )
            contentHolder.addView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if ( child.getId() == LEFT_SIDE_HOLDER || child.getId() == RIGHT_SIDE_HOLDER || child.getId() == CONTENT_SIDE_HOLDER )
        {
            super.addView(child, params);
            return;
        }

        if ( contentHolder != null )
            contentHolder.addView(child,params);
    }

    private class AnimationProtocol
    {
        public boolean leftShowen;
        public boolean rightShowen;
        public int xLeftMargin;
        public int xRightMargin;

        AnimationProtocol(boolean leftShowen, boolean rightShowen, int xLeftMargin, int xRightMargin) {
            this.leftShowen = leftShowen;
            this.rightShowen = rightShowen;
            this.xLeftMargin = xLeftMargin;
            this.xRightMargin = xRightMargin;
        }
    }

    public String makeScreenshot(String cachePath)
    {
        contentHolder.setDrawingCacheEnabled(true);
        contentHolder.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(contentHolder.getDrawingCache());
        contentHolder.setDrawingCacheEnabled(false);

        try {
            File fl = new File( cachePath + File.separator + "test.png");
            try {
                if ( !fl.exists() )
                    fl.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( fl ));
            return fl.getAbsolutePath();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public boolean isRightSideClosed() {
        return ((LayoutParams) contentHolder.getLayoutParams()).leftMargin == 0;
    }
}
