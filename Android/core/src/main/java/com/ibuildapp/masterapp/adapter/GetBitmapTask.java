package com.ibuildapp.masterapp.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import com.ibuildapp.masterapp.utils.Logger;
import com.ibuildapp.masterapp.utils.Statics;
import com.ibuildapp.masterapp.utils.Utils;

import java.io.*;
import java.util.Date;

/**
 * Support thread for baseimageadapter
 * Each copy of such class checks image in assets, cache or download it
 * As a result it calls the callback function
 */
public class GetBitmapTask extends Thread {

    private final String TAG = GetBitmapTask.class.getCanonicalName();
    private boolean isInterrupted = false;
    private int uid;
    private ImageView id;
    private String url;
    private String cachePath;
    private String resPath;
    private String name;
    private OnImageDoneListener listener;
    private AssetManager assetMgr;
    private float density;

    private int edgeLimit;
    private int width;
    private int height;
    private int roundK;
    private Bitmap.Config config;

    /**
     * @param context
     * @param uid       - task uid
     * @param name      - taks name
     * @param id        - uid of image
     * @param resPath   - path of image in assets
     * @param cachePath - path of image in cache
     * @param url       - - path of image at http
     * @param width     - not use now
     * @param height    - not use now
     */
    public GetBitmapTask(Context context, int uid, String name, ImageView id, String resPath, String cachePath, String url, int edgeLimit, int width, int height, int roundK, Bitmap.Config config) {

        this.uid = uid;
        this.name = name;
        this.id = id;
        this.resPath = resPath;
        this.cachePath = cachePath;
        this.url = url;

        this.edgeLimit = edgeLimit;
        this.width = width;
        this.height = height;
        this.roundK = roundK;
        this.config = config;

        assetMgr = context.getAssets();
        density = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public synchronized void interrupt() {
        super.interrupt();
        isInterrupted = true;
    }

    @Override
    public void run() {
        super.run();

        final long currentMilis = new Date().getTime();

        // 1. check bitmap in assets
        if (!TextUtils.isEmpty(resPath)) {
            InputStream stream = null;
            try {
                stream = assetMgr.open(resPath);
                // todo - доделать поддержку закругления и обрезки для этой функции
                Bitmap btm = Utils.proccessBitmap(stream, Bitmap.Config.RGB_565);
                if (listener != null) {
                    String[] msg = new String[] {name, "resPath = " + resPath, Long.toString(new Date().getTime() - currentMilis)};
                    Logger.getInstance().logMsg(msg);
                    Log.e(TAG, "RESOURSES Time = " + (new Date().getTime() - currentMilis));
                    listener.onImageLoaded(uid, id, name, btm, null);
                    return;
                }
            } catch (IOException e) {
                stream = null;
            }
        }

        if (isInterrupted) {
            return;
        }

        // 2. check bitmap in cache
        if (!TextUtils.isEmpty(cachePath)) {
            File imageFile = new File(cachePath);
            if (imageFile.exists()) {
                Bitmap btm = Utils.proccessBitmap(cachePath, edgeLimit, width, height, roundK, config);
                if (listener != null) {
                    String[] msg = new String[] {name, "cachePath = " + cachePath, Long.toString(new Date().getTime() - currentMilis)};
                   /* try {
                        Logger.getInstance().logMsg(msg);
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }*/
                    Log.e(TAG, "CACHE time = " + (new Date().getTime() - currentMilis));
                    listener.onImageLoaded(uid, id, name, btm, cachePath);
                    return;
                }
            }
        }

        if (isInterrupted) {
            return;
        }

        // 3. download bitmap from www
        if (!TextUtils.isEmpty(url)) {

            byte[] resAr = Utils.downloadFileAsBytes(url);

            if ( resAr != null )
            {
                Log.d(TAG, "Downloaded time = " + (new Date().getTime() - currentMilis));

                // обрезаем верх и низ
                System.gc();

                try {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inPreferredConfig = config;
                    Bitmap btm = BitmapFactory.decodeByteArray(resAr, 0, resAr.length, opt);
                    final Bitmap resBtm = Bitmap.createBitmap(btm, 0, 12, btm.getWidth(), btm.getHeight()-45);
                    resAr = null;

                    // сначала отрисовываем картинку в listview
                    if (listener != null) {
                        Log.d(TAG, "HTTP Time = " + (new Date().getTime() - currentMilis));
                        listener.onImageLoaded(uid, id, name, resBtm, "");
                    }

                    btm.recycle();
                    btm = null;

                    // дергаем callback еще раз - для сохранения закешированной картинки в базе
                    new BitmapSaverThread( resBtm, url, listener ).start();
                } catch (Exception e) {

                }

            }

//            Log.d(TAG, "Before Download time = " + (new Date().getTime() - currentMilis));
//            String downloadedImg =
//                    Utils.downloadFile(url);
//
//            if (downloadedImg != null) {
//                Log.d(TAG, "Downloaded time = " + (new Date().getTime() - currentMilis));
//
//                // костыль обрезки bottombar и topbar
//                BitmapFactory.Options opt = new BitmapFactory.Options();
//                Bitmap btm = BitmapFactory.decodeFile( downloadedImg);
//                Bitmap resBtm = Bitmap.createBitmap(btm, 0, (int) (12),btm.getWidth(),btm.getHeight()-(int)(45));
//                try {
//                    resBtm.compress(Bitmap.CompressFormat.JPEG,100, new FileOutputStream(new File(downloadedImg)));
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                Log.d(TAG, "Kostil time = " + (new Date().getTime() - currentMilis));
//
//                btm = Utils.proccessBitmap(downloadedImg, edgeLimit, width, height, roundK, config);
//                if (btm == null) {
//                    Log.e(TAG, "btm = null");
//                }
//
//                if (listener != null) {
//                    String[] msg = new String[] {name, "url = " + url, Long.toString(new Date().getTime() - currentMilis)};
//                    try {
//                        Logger.getInstance().logMsg(msg);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    Log.d(TAG, "HTTP Time = " + (new Date().getTime() - currentMilis));
//                    listener.onImageLoaded(uid, id, name, btm, downloadedImg);
//                    return;
//                }
//            }  else
//                Log.e(TAG, "downloaded file = null");
        }

        if (listener != null) {
            Log.e(TAG, "NULL");
            listener.onImageLoaded(uid, id, name, null, null);
            return;
        }
    }

    public void setListener(OnImageDoneListener listener) {
        this.listener = listener;
    }

    private class BitmapSaverThread extends Thread {
        Bitmap btm;
        String url;
        OnImageDoneListener listener;

        private BitmapSaverThread( Bitmap btm, String url, OnImageDoneListener listener ) {
            this.btm = btm;
            this.url = url;
            this.listener = listener;
        }

        @Override
        public void run() {
            Log.d(TAG, "SAVED THREAD ");

            File outFile = null;
            try {
                outFile = new File(Statics.cachePath + File.separator + Utils.md5(url));
                btm.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(outFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (listener != null) {
                Log.d(TAG, "Bitmap Saved succesfully ");
                listener.onImageLoaded(uid, null, "", null, outFile.getAbsolutePath());
                return;
            }
        }
    }
}
