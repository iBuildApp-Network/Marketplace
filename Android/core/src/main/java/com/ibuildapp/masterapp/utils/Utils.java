package com.ibuildapp.masterapp.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 17.07.14
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    /**
     * Функция проверяет наличие интернет соединения
     *
     * @param context - контекст приложения
     * @return -1 - соединение отсутствует
     *         1 - WI-FI соединение
     *         2- мобильный интернет
     */
    public static int checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            try {
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobile.isConnected() && !wifi.isConnectedOrConnecting()) {
                    return 2;
                }
            } catch (Exception ex) {
            }
            return 1;
        }
        return -1;
    }

    /**
     * копируем базу данных из папки assets в рабочую директорию приложения
     */
    public static boolean checkDb( )
    {
        File dest = new File(Statics.databasePath);
        if (!dest.exists())
            return false;
        else
            return true;
    }

    /**
     * копируем базу данных из папки assets в рабочую директорию приложения
     */
    public static boolean copyDbToWorkDirectory( Context context )
    {
        // копируем файл БД из assets в папку программы
        AssetManager manager = context.getAssets();
        InputStream stream = null;
        try {
            stream = manager.open(Statics.DB_FILE);
        } catch (IOException e) {
            Log.e("", "No such DB in assets");
            return false;
        }


//        File destDir = new File("/data/data/com.ibuildapp.masterapp/databases/");
//        if (!destDir.exists())
//            destDir.mkdirs();

        File dest;
        dest = new File( Statics.databasePath );
        if ( !dest.exists() )
        {
            try {
                OutputStream os = new FileOutputStream(dest);
                int length;
                byte[] buffer = new byte[1024];
                while ((length = stream.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                return true;
            } catch (Exception e) {
                if ( dest.exists())
                    dest.delete();
                e.printStackTrace();
                return false;
            }
        } else
            return true;
    }

    /**
     * Opens Bitmap from file
     *
     * @param fileName - file path
     * @return
     */
    public static Bitmap proccessBitmap(String fileName, int edgeLimit, int widthLimit, int heightLimit, int roundK, Bitmap.Config config ) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap resBitmap = null;

        InputStream fileStream = null;
        try{
            fileStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            return null;
        }

        // ужимаем картинку кратно 2
        int scale = 1;
        if ( edgeLimit > 0 )
        {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream( fileStream , null, opts);

            int width = opts.outWidth;
            int height = opts.outHeight;

            while (true) {
                if (width / 2 <= edgeLimit || height / 2 <= edgeLimit) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }
        }

        opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.inPreferredConfig = config;

        // декодируемс кортинку с нужным scale. Несколько раз если понадобится
        try {
            System.gc();
            resBitmap = BitmapFactory.decodeFile(fileName, opts);
        } catch (Exception ex) {
        } catch (OutOfMemoryError e) {
        }

        if ( resBitmap == null )
        {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if ( resBitmap == null )
        {
            try {
                System.gc();
                resBitmap = BitmapFactory.decodeFile(fileName, opts);
            } catch (Exception ex) {
            } catch (OutOfMemoryError ex) {
            }
        }

        if ( resBitmap == null )
            return null;

        // todo добавить обрезание картинки


        // обрезаем картинку под нужный размер
        if ( widthLimit > 0 && heightLimit > 0 )
            resBitmap = ThumbnailUtils.extractThumbnail(resBitmap, widthLimit, heightLimit, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        // скругление краев
        if ( roundK > 0 )
        {
            Bitmap output = Bitmap.createBitmap(resBitmap.getWidth(), resBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, resBitmap.getWidth(), resBitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundK, roundK, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(resBitmap, rect, rect, paint);

            resBitmap.recycle();
            return output;
        }

        return resBitmap;
    }

    /**
     * Opens Bitmap from file
     *
     * @param fileName - file path
     * @return
     */
    public static Bitmap proccessBitmap(byte[] ar, int edgeLimit, int widthLimit, int heightLimit, int roundK, Bitmap.Config config ) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap resBitmap = null;

        // ужимаем картинку кратно 2
        int scale = 1;
        if ( edgeLimit > 0 )
        {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(ar, 0, ar.length, opts);

            int width = opts.outWidth;
            int height = opts.outHeight;

            while (true) {
                if (width / 2 <= edgeLimit || height / 2 <= edgeLimit) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }
        }

        opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.inPreferredConfig = config;

        // декодируемс кортинку с нужным scale. Несколько раз если понадобится
        try {
            System.gc();
            resBitmap = BitmapFactory.decodeByteArray(ar, 0, ar.length, opts);
        } catch (Exception ex) {
        } catch (OutOfMemoryError e) {
        }

        if ( resBitmap == null )
        {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if ( resBitmap == null )
        {
            try {
                System.gc();
                resBitmap = BitmapFactory.decodeByteArray(ar, 0, ar.length, opts);
            } catch (Exception ex) {
            } catch (OutOfMemoryError ex) {
            }
        }

        if ( resBitmap == null )
            return null;

        // обрезаем картинку под нужный размер
        if ( widthLimit > 0 && heightLimit > 0 )
            resBitmap = ThumbnailUtils.extractThumbnail(resBitmap, widthLimit, heightLimit, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        // скругление краев
        if ( roundK > 0 )
        {
            Bitmap output = Bitmap.createBitmap(resBitmap.getWidth(), resBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, resBitmap.getWidth(), resBitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundK, roundK, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(resBitmap, rect, rect, paint);

            resBitmap.recycle();
            return output;
        }

        return resBitmap;
    }

    /**
     * download file url and save it
     *
     * @param url
     */
    public static byte[] downloadFileAsBytes(String url) {
        int BYTE_ARRAY_SIZE = 2048;
        try {
            URL imageUrl = new URL(URLDecoder.decode(url));
            URLConnection conn = imageUrl.openConnection();

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buf = new byte[BYTE_ARRAY_SIZE];
            int count = 0;
            while((count = bis.read(buf)) > 0) {
                baos.write(buf,0,count);
            }

            bis.close();
            baos.close();

            return baos.toByteArray();
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * download file url and save it
     *
     * @param url
     */
    public static String downloadFile(String url) {
        int BYTE_ARRAY_SIZE = 2048;
//        int CONNECTION_TIMEOUT = 60000;
//        int READ_TIMEOUT = 60000;

        // downloading cover image and saving it into file
        try {
            URL imageUrl = new URL(URLDecoder.decode(url));
            URLConnection conn = imageUrl.openConnection();
//            conn.setConnectTimeout(CONNECTION_TIMEOUT);
//            conn.setReadTimeout(READ_TIMEOUT);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            File resFile = new File(Statics.cachePath + File.separator + Utils.md5(url));
            if (!resFile.exists()) {
                resFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(resFile);
            int current = 0;
            byte[] buf = new byte[BYTE_ARRAY_SIZE];
            Arrays.fill(buf, (byte) 0);
            while ((current = bis.read(buf, 0, BYTE_ARRAY_SIZE)) != -1) {
                fos.write(buf, 0, current);
                Arrays.fill(buf, (byte) 0);
            }

            bis.close();
            fos.flush();
            fos.close();
            Log.d("", "");
            return resFile.getAbsolutePath();
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Opens Bitmap from stream
     *
     * @param stream - input stream
     * @return
     */
    public static Bitmap proccessBitmap(InputStream stream, Bitmap.Config config) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = config;
        Bitmap bitmap = null;
        try {
            // decode image with appropriate options
            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(stream, null, opts);
            } catch (Exception ex) {
                Log.d("", "");
            } catch (OutOfMemoryError e) {
                Log.d("", "");
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeStream(stream, null, opts);
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError ex) {
                    Log.e("decodeImageFile", "OutOfMemoryError");
                }
            }
        } catch (Exception e) {
            Log.d("", "");
            return null;
        }

        return bitmap;
    }


    public static String md5(String in) {
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
        } catch (NoSuchAlgorithmException nSAEx) {
        }

        return null;
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public static boolean isInteger(String value) {
        if(value.isEmpty())
            return false;

        for(int i = 0; i < value.length(); i++) {
            if(i == 0 && value.charAt(i) == '-') {
                if(value.length() == 1)
                    return false;
                else
                    continue;
            }

            if(Character.digit(value.charAt(i), 10) < 0)
                return false;
        }

        return true;
    }

}
