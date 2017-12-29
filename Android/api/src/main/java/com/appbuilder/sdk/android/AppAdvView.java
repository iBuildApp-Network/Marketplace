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
package com.appbuilder.sdk.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.millennialmedia.android.MMAdView;
import com.smaato.soma.BannerView;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.google.android.gms.internal.zzhl.runOnUiThread;


public class AppAdvView extends RelativeLayout {
    private static  final int HEIGHT_DP = 50;
    final private int CLOSE_VIEW = 0;
    final private int ADD_CLOSE_BUTTON = 1;

    private int smaatoCount = 0;
    public int countToken = 0;
    private Context context = null;
    private AdView adView = null;
    private AppAdvData advData = null;
    private boolean firstStart = false;
    private boolean isAdClosed;
    private float density;
    private int height;

    private OnAdClosedListener onAdClosedListener;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case ADD_CLOSE_BUTTON: {
                    addCloseButton();
                }
                break;
                case CLOSE_VIEW: {
                    closeView();
                }
                break;
            }
            ;
        }
    };

    public AppAdvView(Context context, AppAdvData advData) {
        super(context);
        this.context = context;
        this.advData = advData;
        density = getResources().getDisplayMetrics().density;
        initAdvData();
    }

    public AppAdvView(Context context, AppAdvData advData, boolean fs) {
        super(context);
        this.context = context;
        this.advData = advData;
        this.firstStart = fs;
        density = getResources().getDisplayMetrics().density;
        initAdvData();
    }

    public AppAdvView(Context context, AttributeSet attrs, AppAdvData advData) {
        super(context, attrs);
        this.context = context;
        this.advData = advData;
        density = getResources().getDisplayMetrics().density;
        initAdvData();
    }


    private void initAdvData() {
        if (advData == null)
            return;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            advData.setAdvState(AppAdvData.AD_HIDDEN);
            return;
        }
        if (!ni.isConnectedOrConnecting()) {
            advData.setAdvState(AppAdvData.AD_HIDDEN);
            return;
        }

        /* check saved configuration */
        String cachePath = context.getCacheDir().toString();
        File cache = new File(cachePath + "/" + md5("ibuildapp-" + advData.getAdvSessionUid()));
        if (cache.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
                AppAdvData tempAdvData = (AppAdvData) ois.readObject();
                ois.close();
                if (firstStart) {
                    advData.setAdvState(AppAdvData.AD_VISIBLE);
                    try {
                        cache.createNewFile();
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
                        oos.writeObject(advData);
                        oos.close();
                    } catch (Exception e) {
                        cache.delete();
                    }
                } else {
                    advData.setAdvState(tempAdvData.getAdvState());
                }
            } catch (Exception e) {
            }
        }

        //this.setVisibility(GONE);            
        //Toast.makeText(context, "State" + advData.getAdvState(), Toast.LENGTH_LONG).show();
        if (advData.getAdvState() == AppAdvData.AD_VISIBLE) {
            if (advData.getAdvType().equals("gAd")) {
                setAdMob(advData.getAdvContent());
            } else if (advData.getAdvType().equals("url")) {
                setBanner(advData.getAdvType(), advData.getAdvContent(), advData.getAdvRedirect());
            } else if (advData.getAdvType().equals("html")) {
                setBanner(advData.getAdvType(), advData.getAdvContent(), advData.getAdvRedirect());
            } else if (advData.getAdvType().equals("smaato")) {
                setSmaatoAd(advData.getAdvPublisherId(), advData.getAdvAdSpaceId());
            } else if (advData.getAdvType().equals("mlmedia")) {
                setMMediaAd(advData.getAdvApId());
            }
        }
    }

    public int getAdHeight() {
        return height;
    }

    private void setAdMob(String publishingId) {
        adView = new AdView(context);
        adView.setAdUnitId(publishingId);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e("BANNER", "BANNER LOADED");
                SharedPreferences  sharedPprefsToken = context.getSharedPreferences("tokenCount", Context.MODE_PRIVATE);
                countToken = sharedPprefsToken.getInt("tokenCount", 0);
                countToken = ++countToken;
                Log.e("TOKEN BALANCE", String.valueOf(countToken));
                SharedPreferences.Editor edToken = sharedPprefsToken.edit();
                edToken.putInt("tokenCount", countToken);
                edToken.commit();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppBuilderModuleMainAppCompat appCompat = new AppBuilderModuleMainAppCompat();
                        AppBuilderModuleMain appMain  = new AppBuilderModuleMain();
                        if (context instanceof AppBuilderModuleMainAppCompat) {
                           appCompat  = (AppBuilderModuleMainAppCompat) getContext();
                           appCompat.updateTokenViewFromAdd(countToken);
                        } else  if (context instanceof  AppBuilderModuleMain) {
                            appMain = (AppBuilderModuleMain) getContext();
                            appMain.updateTokenViewFromAdd(countToken);

                        } else {

                        }

                    }
                    });




            }
        });



        height = AdSize.SMART_BANNER.getHeightInPixels(context);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addView(adView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        closeBtn_v2();
    }

    void loadAdMob() {
        if(adView != null && advData != null && advData.getAdvType().equals("gAd"))
            adView.loadAd(new AdRequest.Builder().build());
    }

    private void closeBtn_v2() {
        ImageView closeBtn = new ImageView(context);
        closeBtn.setImageResource(R.drawable.adview_btn_close);
        closeBtn.setLayoutParams(new RelativeLayout.LayoutParams((int) (density * 25), (int) (density * 25)) {{
            addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        }});
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    handler.sendEmptyMessage(CLOSE_VIEW);
                } catch (Exception e) {
                }
            }
        });
        addView(closeBtn);
    }

    private void setBanner(String advType, String advContent, String advRedirect) {
        WebView webView = new WebView(context);
        height = (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, HEIGHT_DP,
                        context.getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        webView.setLayoutParams(lp);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; "
                + "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 "
                + "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 "
                + "Safari/6531.22.7");
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setUseWideViewPort(true);

        if (advType.equalsIgnoreCase("url")) {
            webView.loadUrl(advContent);
        } else {
            webView.loadDataWithBaseURL(null, advContent, "text/html", "utf-8", null);
        }

        if (advData.getAdvRedirect().length() > 0) {
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(advData.getAdvRedirect()));
                    context.startActivity(intent);
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
            webView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                }
            });
        } else {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Log.d("", "");
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("", "");
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.d("", "");
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains("smaato.net") && (smaatoCount == 0)) {
                        smaatoCount++;
                        return false;
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);

                    smaatoCount = 0;

                    return true;
                }
            });
        }

        addView(webView);

        closeBtn_v2();
    }

    private void setSmaatoAd(int publisherId, int adspaceId) {
        BannerView bannerView = new BannerView(context);

        height = (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, HEIGHT_DP,
                        context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams
                (LayoutParams.MATCH_PARENT, height);
        bannerView.setLayoutParams(llp);

        bannerView.getAdSettings().setPublisherId(publisherId);//(923863275);
        bannerView.getAdSettings().setAdspaceId(adspaceId);//(65766148);

        addView(bannerView);

        bannerView.asyncLoadNewBanner();

        closeBtn_v2();
    }

    private void setMMediaAd(String appId) {
        //MMAdView mMAdView = new MMAdView((Activity)context, goalId, 
        //      MMAdView.BANNER_AD_TOP, 45);
        //MMAdView.startConversionTrackerWithGoalId(context, goalId);
        MMAdView mMAdView = new MMAdView((Activity) context, appId,
                MMAdView.BANNER_AD_TOP, 45);
        mMAdView.setId(10000);

        //mMAdView.setApid(appId);

        height = (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, HEIGHT_DP,
                        context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams
                (LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mMAdView.setLayoutParams(llp);

        mMAdView.callForAd();

        addView(mMAdView);

        closeBtn_v2();
    }

    /* PRIVATE METHODS */
    private void addCloseButton() {
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        setLayoutParams(rlp);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(rlp);

        ImageView imageView = new ImageView(context);
        imageView.setId(1);
        String imgSrc = "iVBORw0KGgoAAAANSUhEUgAAACMAAAAjCAYAAAAe2bNZAAAABGdBTUEAAK/INwWK6QAAABl0RVh0" +
                "U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAR2SURBVHja7FdbbBRVGP7O7G53thcLurtc" +
                "SiMXL0SotlpqsKZqLAYKgRoVE4MxpjXhQWPQNKYm+oyJDwYfNAWixqgPajCKoiagtIVwKUK3REzD" +
                "3S2Udku73e5ut9ud439m/knXtU+zTeRhT/JlO2fO/PPNf77//06FlBK3ynDn+bzGMYoIxYRSQjn/" +
                "lvC8GmlCghAjjPOvup7ie0a+ZBQRD0EnlBHuIAQICwh+JqXz2hSTiBCGGDcJ0SyyhlMygj/Exy9d" +
                "SKgkLEVfVzX2dzyM86cXYuhKCTQNRqAyIZatjohN24/jgcdP07rLhKsEF5NSY0o41IxNZB5hMWEF" +
                "YSU62rbgp92roXs1+CsoP0ss2lFKyMg12piojFQ1XSpv6/jW4ys5S3fOEwaYUNIJGRXeS7iNidyN" +
                "yXgN2hpb0X8ygFV1wHzaqflBi8zUJHD9AjAxRq+k9177C3/ry2P+Xb/v9QUWHafn+wnEFGNaHlpR" +
                "OqE3Yhneb3kO544F4NUht70L2bCVVJK0CMTHTEKy4XnIza/TtURlrL9sZGfLs9IwlrPGVCyv2yEZ" +
                "nTMTxJnfqnH46xUqYXJHB1DdaK3KZIAj3wDTVDD1zwBPvmhOy9EIxCftCJ75eUm8+4fa0oYtYRZ0" +
                "1GlmvPw1fvzy6RoDkrZOQhz7keoiZa164gUY9z0Co+oxYN3L1tzoDYgTB4go1bxHIn3oqxquPBWr" +
                "yElmXNw/VB8pl31di4Utu4NfQtD2yLc+g/T6IJp3WNlQuhwdhHinmRRyylpLU+nergWcYZ9TMiKL" +
                "kI5I2CeQVQTd+8zsyPbPITyUQCFgxKPQ2puAi30z66jNaWNDOmdZxXJpeXZgYcw2W3EXCZgabJIa" +
                "bSIGQfoRq+r/s8zj1mS+dqACZLiVT06X+VOu6HUdzEo2vwps3g6RnIBUZFxuCL0UcuubKknA9x9b" +
                "+lZ74180qWJwrIyTzBj8cFK1eHlv3TA4iiQSWLeNsjEOERmAeO8liF1EbnyYSjoKufEVYIMl5rQS" +
                "8craIbYJ06eckLGzoswu4tnY2mOkrRoTNwfpTsLquB+9AXH5HMTZoxC73ybqZEMxarSDF00jEoK2" +
                "qKm1h/4c4Vgpt8PM2MY37Fq76WTiwafqikO/VuDod6SPaeql9MEXQjNP9B6G+GKnKexMbydctNGe" +
                "tU1hrFn/B/cYk4xTO9DZIJUd3EOdtnbqtfrWovCf5eaWGaysnO5kGNbDcmlVVPugcw9K5/XkaweS" +
                "LT/OKb5KQUNFHx7Zk3lofVglxlwhZqAujYx1ma5pHGAiIXbuEY417dS1BfcG1axu5yPEncqnMt37" +
                "6uSBvbUi1BmksvaYi4vL0sb9DTfEhpZTrkefPkFTlwhXCIO2Y+dzhMg2TPtM42fjDM5yuJrkg1T2" +
                "4SrCc8l8D1e2kNM5FWaKmr2mmMnmHjsVgQnGv46dYg4O5LY9eHjrvNktPodsimGTyGRLXczhfwc2" +
                "KS0HdhazkZml3uaUTN5Dwy00CmQKZApkCmQKZP6v8Y8AAwAM1bZSYou4NgAAAABJRU5ErkJggg==";
        try {
            imageView.setImageBitmap(BitmapFactory.decodeStream(new ByteArrayInputStream(
                    Base64.decode(imgSrc))));
        } catch (IOException iOEx) {
            Log.d("", "");
        }
        //imgSrc.getBytes())));
        //imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_close));
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    handler.sendEmptyMessage(CLOSE_VIEW);
                } catch (Exception e) {
                }
            }
        });
        linearLayout.addView(imageView);
        addView(linearLayout);
    }

    void closeView() {
        if(!isAdClosed) {
            try {
                isAdClosed = true;

                this.setVisibility(GONE);

                if (onAdClosedListener != null) {
                    onAdClosedListener.onAdClosed();
                }

                adView.destroy();

                advData.setAdvState(AppAdvData.AD_HIDDEN);
                String cachePath = context.getCacheDir().toString();
                File cache = new File(cachePath + "/" + md5("ibuildapp-" + advData.getAdvSessionUid()));
                try {
                    cache.createNewFile();
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
                    oos.writeObject(advData);
                    oos.close();
                } catch (Exception e) {
                    cache.delete();
                }
            } catch (Exception e) {
            }
        }
    }

    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }

        return null;
    }

    public void setOnAdClosedListener(OnAdClosedListener listener){
        onAdClosedListener = listener;
    }

    public interface OnAdClosedListener{

        public void onAdClosed();
    }

}
