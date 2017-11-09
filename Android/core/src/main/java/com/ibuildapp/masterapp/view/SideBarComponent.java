package com.ibuildapp.masterapp.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.ibuildapp.masterapp.utils.Utils;

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
    private final int UPDATE_BLUR = 200002;

    private final int LEFT_SIDE_HOLDER = 1001;
    private final int RIGHT_SIDE_HOLDER = 1002;
    private final int CONTENT_SIDE_HOLDER = 1003;

    private final int LEFT_SIDE_BLUR_IMAGE = 1004;
    private final int RIGHT_SIDE_BLUR_IMAGE = 1005;

    // ui
    private FrameLayout leftHolder;
    private FrameLayout rightHolder;
    private ImageView leftHolderImageBlur;
    private ImageView rightHolderImageBlur;
    private Bitmap leftHolderBitmap;
    private Bitmap rightHolderBitmap;

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
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_POSITION: {
                    AnimationProtocol packet = (AnimationProtocol) msg.obj;
//                    Log.e(TAG, "xLeft = " + packet.xLeftMargin + " xRight = " + packet.xRightMargin +
//                            " leftShowen = " + packet.leftShowen+ " rightShowen = " + packet.rightShowen);

                    FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();

                    if ( packet.xLeftMargin != Integer.MAX_VALUE )
                        params.setMargins(packet.xLeftMargin, 0, 0, 0);
                    contentHolder.setLayoutParams(params);

                    // поддержка blur
                    if ( packet.xLeftMargin > 0 ) // left side opened
                    {
                        if ( leftHolderImageBlur != null )
                        {
                            float percent = (Math.abs(packet.xLeftMargin) * 100)/leftSideSize;
                            int alpha = (int) ((255 * percent)/100);
                            leftHolderImageBlur.setAlpha(255-alpha);
                        }
                    } else if ( packet.xLeftMargin < 0 )
                    {
                        if ( rightHolderImageBlur != null )
                        {
                            float percent = (Math.abs(packet.xLeftMargin) * 100)/rightSideSize;
                            int alpha = (int) ((255 * percent)/100);
                            rightHolderImageBlur.setAlpha(255-alpha);
                        }
                    }

                    isLeftShowen = packet.leftShowen;
                    isRightShowen= packet.rightShowen;
                }
                break;

                case UPDATE_BLUR :
                {
                    AnimationProtocol packet = (AnimationProtocol) msg.obj;
                    // поддержка blur
                    if ( packet.xLeftMargin > 0 ) // left side opened
                    {
                        if ( leftHolderImageBlur != null )
                        {
                            float percent = (Math.abs(packet.xLeftMargin) * 100)/leftSideSize;
                            int alpha = (int) ((255 * percent)/100);
                            leftHolderImageBlur.setAlpha(255-alpha);
                        }
                    } else if ( packet.xLeftMargin < 0 )
                    {
                        if ( rightHolderImageBlur != null )
                        {
                            float percent = ( Math.abs(packet.xLeftMargin) * 100 )/rightSideSize;
                            int alpha = (int) ((255 * percent)/100);
                            rightHolderImageBlur.setAlpha(255-alpha);
                        }
                    }
                } break;
            }
        }
    };

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
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.LEFT;
        leftHolder.setLayoutParams(params);
        addView(leftHolder);

        rightHolder = new FrameLayout(getContext());
        rightHolder.setId(RIGHT_SIDE_HOLDER);
        rightHolder.setBackgroundColor(Color.parseColor("#00ff00"));
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        addView(rightHolder,params);

        contentHolder = new LinearLayout(getContext());
        contentHolder.setId(CONTENT_SIDE_HOLDER);
        contentHolder.setBackgroundColor(Color.parseColor("#0000ff"));
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity= Gravity.LEFT;
        addView(contentHolder, params);

        // after view has been drawen set width and height in pixels - need, cause we will regulate content holder over margins in framelayout
        // после того как view отрисуется нужно задать ей размер в пикселях - иначе не будет работать scroll, т.к. мы регулируем положение contentHolder через margins в frameLayout
        contentHolder.post( new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                params.width = contentHolder.getWidth();
                params.height = contentHolder.getHeight();
                contentHolder.setLayoutParams( params);
            }
        });

        detector = new GestureDetector(getContext(), new MyGestureListener());
        scroller = new Scroller(getContext(), new DecelerateInterpolator(0.5f));

    }

    public boolean isLeftShowen() {
        return isLeftShowen;
    }

    public boolean isRightShowen() {
        return isRightShowen;
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
        leftHolder.post(new Runnable() {
            @Override
            public void run() {
                View v = leftHolder.findViewById(LEFT_SIDE_BLUR_IMAGE);
                if (v != null) {
                    leftHolder.removeView(leftHolderImageBlur);
                    if (leftHolderBitmap != null)
                        leftHolderBitmap.recycle();
                }

                Bitmap tmp = Utils.loadBitmapFromView(leftHolder);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    leftHolderBitmap = Utils.fastblur(tmp, 1);
                } else{
                    leftHolderBitmap = Utils.fastblur(tmp, 20);
                }
                tmp.recycle();

                leftHolderImageBlur = new ImageView(getContext());
                leftHolderImageBlur.setId(LEFT_SIDE_BLUR_IMAGE);
                leftHolderImageBlur.setImageBitmap(leftHolderBitmap);
                leftHolder.addView(leftHolderImageBlur, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        });
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
        rightHolder.post(new Runnable() {
            @Override
            public void run() {
                View v = rightHolder.findViewById(RIGHT_SIDE_BLUR_IMAGE);
                if (v != null) {
                    rightHolder.removeView(rightHolderImageBlur);
                    if (rightHolderBitmap != null)
                        rightHolderBitmap.recycle();
                }

                Bitmap tmp = Utils.loadBitmapFromView(rightHolder);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    rightHolderBitmap = Utils.fastblur(tmp, 1);
                } else{
                    rightHolderBitmap = Utils.fastblur(tmp, 20);
                }
                    tmp.recycle();



                rightHolderImageBlur = new ImageView(getContext());
                rightHolderImageBlur.setId(RIGHT_SIDE_BLUR_IMAGE);
                rightHolderImageBlur.setImageBitmap(rightHolderBitmap);
                rightHolder.addView(rightHolderImageBlur, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Log.e(TAG, "onTouchEvent");
        if ( detector.onTouchEvent(event) ) return true;
        //Log.e(TAG, "onTouchEvent__");

        // check for overscroll ( проверка на необходимость доскролить)
        if ((event.getPointerCount() == 1) && ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP))
        {
            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
            if ( params.leftMargin == 0 || params.leftMargin == leftSideSize || params.leftMargin == -rightSideSize )
                return super.onTouchEvent(event);

            if ( params.leftMargin > 0 )  // need to overscroll left side ( доскроливание левого сайдбара )
            {
                if ( params.leftMargin > leftSideSize/2 ) // overscroll right ( доскроллить вправо )
                {
                    //Log.e(TAG, "onTouchEvent1");
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
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

                            // control message with final coordinat point (контрольное сообщение с конечной позицией)
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(true, isRightShowen, leftSideSize, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();

                } else // overscroll left ( доскроллить влево )
                {
                    //Log.e(TAG, "onTouchEvent2");
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
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

                            // control message with final coordinat point (контрольное сообщение с конечной позицией)
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(false, isRightShowen, 0, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            } else if ( params.leftMargin < 0 ) // доскроливание правого сайдбара
            {
                if ( Math.abs(params.leftMargin) < rightSideSize/2 ) // overscroll right ( доскроллить вправо )
                {
                    //Log.e(TAG, "onTouchEvent3");
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
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

                            // control message with final coordinat point (контрольное сообщение с конечной позицией)
                            Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, 0, Integer.MAX_VALUE));
                            handler.sendMessage(msg);
                        }
                    }).start();

                } else if ( Math.abs(params.leftMargin) > rightSideSize/2 ) // overscroll left ( доскроллить влево )
                {
                    //Log.e(TAG, "onTouchEvent4");
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
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

                            // control message with final coordinat point (контрольное сообщение с конечной позицией)
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
            if ( !isLeftShowen && !isRightShowen ) // nothing showed ( ничего не показано - оба сайдбара закрыты )
            {
                // check direction
                if ( e2.getRawX() > e1.getRawX() ) // -->
                {
                    if ( e1.getRawX() > (SENSETIVE_AREA_SIZE * density) )
                        return false;
                } else if ( e2.getRawX() < e1.getRawX() ) // <--
                {
                    if ( e1.getRawX() < ( screenWidth -  (SENSETIVE_AREA_SIZE * density)) )
                        return false;
                }
            } else if ( isLeftShowen )
            {
                if ( e1.getRawX() < ( screenWidth -  (SENSETIVE_AREA_SIZE * density)) )
                    return false;
            } else if ( isRightShowen )
            {
                if ( e1.getRawX() > ( rightSideSize ) )
                    return false;
            }

            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
            if ( params.leftMargin > 0 ) // left pannel is visible
            {
                leftHolder.setVisibility(VISIBLE);
                rightHolder.setVisibility(GONE);
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    try {
                        Drawable leftDrawable = leftChildView.getBackground();
                        setBackground(leftDrawable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ( params.leftMargin < 0 )
            {
                leftHolder.setVisibility(GONE);
                rightHolder.setVisibility(VISIBLE);

                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    try {
                        Drawable rightDrawable = rightChildView.getBackground();
                        setBackground(rightDrawable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // enable/disable scroll (проверяем есть ли sidebar с той стороны, куда тянем)
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
                // check for overscroll - проверка на перескроливаение границы левого\правого бара
                int newMargin = params.leftMargin + (int)(-distanceX);
                //Log.e(TAG, "Marg = " + newMargin );


//                if ( newMargin > 0  )
//                {
//                    if ( newMargin > leftSideSize )
//                    {
//                        Log.e(TAG, "1");
//                        Message msg = handler.obtainMessage( UPDATE_BLUR, new AnimationProtocol(true, isRightShowen, leftSideSize, Integer.MAX_VALUE ) );
//                        handler.sendMessage(msg);
//                        return false;
//                    }
//
//                } else if ( newMargin < 0  )
//                {
//                    if ( Math.abs(newMargin) > rightSideSize )
//                    {
//                        Log.e(TAG, "2");
//                        Message msg = handler.obtainMessage( UPDATE_BLUR, new AnimationProtocol(isLeftShowen, true, rightSideSize, Integer.MAX_VALUE ) );
//                        handler.sendMessage(msg);
//                        return false;
//                    }
//
//                }


//                if ( e2.getRawX() > e1.getRawX() ) // -->
//                {
//                    Log.e(TAG, "--> Marg = " + newMargin );
//                    if ( isRightShowen )
//                    {
//                        if ( Math.abs( newMargin ) > 0 )
//                        {
////                            Message msg = handler.obtainMessage( UPDATE_BLUR, new AnimationProtocol(false, false, 0, Integer.MAX_VALUE ) );
////                            handler.sendMessage(msg);
//                            return true;
//                        }
//                    } else if ( !isLeftShowen )
//                    {
//                        if ( Math.abs( newMargin ) > leftSideSize )
//                        {
////                            Message msg = handler.obtainMessage( UPDATE_BLUR, new AnimationProtocol(true, false, leftSideSize, Integer.MAX_VALUE ) );
////                            handler.sendMessage(msg);
//                            return true;
//                        }
//                    }
//                } else if ( e2.getRawX() < e1.getRawX() ) // <--
//                {
//                    Log.e(TAG, "<-- Marg = " + newMargin);
//                    if ( !isRightShowen )
//                    {
//                        if ( Math.abs( newMargin ) > rightSideSize )
//                        {
////                            Message msg = handler.obtainMessage( UPDATE_BLUR, new AnimationProtocol(false, true, -rightSideSize, Integer.MAX_VALUE ) );
////                            handler.sendMessage(msg);
//                            return true;
//                        }
//                    } else
//                    {
//                        if ( Math.abs( newMargin ) < 0 )
//                        {
////                            Message msg = handler.obtainMessage( UPDATE_BLUR, new AnimationProtocol(false, false, 0, Integer.MAX_VALUE ) );
////                            handler.sendMessage(msg);
//                            return true;
//                        }
//                    }
//                }

                //Log.e(TAG, "Margin = " + newMargin);
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

            Log.e(TAG, "onFling");
            // check direction
            if ( e2.getRawX() > e1.getRawX() ) // fling to right -->
            {
                //Log.e( TAG, "-->" );

                if ( !isLeftShowen && !isRightShowen)
                {
                    if ( leftHolder.getChildCount() == 0 )
                        return super.onFling(e1, e2, velocityX, velocityY);

                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
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
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
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
            } else if ( e2.getRawX() < e1.getRawX() ) // fling to left <--
            {
                //Log.e(TAG, "<--");

                if ( isLeftShowen )
                {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
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

                    return true;
                } else if ( !isLeftShowen && !isRightShowen )
                {
                    if ( rightHolder.getChildCount() == 0 )
                        return super.onFling(e1, e2, velocityX, velocityY);

                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout.LayoutParams params = (LayoutParams) contentHolder.getLayoutParams();
                            scroller.abortAnimation();
                            scroller.startScroll(0, 0, rightSideSize+params.leftMargin, 0, OVERSCROLLER_ANIMATION_DURATION);
                            int oldMargin = params.leftMargin;
                            while ( scroller.computeScrollOffset() )
                            {
                                Message msg = handler.obtainMessage(UPDATE_POSITION, new AnimationProtocol(isLeftShowen, false, oldMargin - scroller.getCurrX(), Integer.MAX_VALUE));
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

                    return true;
                }
            }

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

    public void makeScreenshot()
    {

        contentHolder.setDrawingCacheEnabled(true);
        contentHolder.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(contentHolder.getDrawingCache());
        contentHolder.setDrawingCacheEnabled(false);


        //Bitmap btm = loadBitmapFromView(rootScroller, screenWidth, screenHeight);
        try {
            File fl = new File("/storage/emulated/0/test.png");
            try {
                if ( !fl.exists() )
                    fl.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( fl ));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
