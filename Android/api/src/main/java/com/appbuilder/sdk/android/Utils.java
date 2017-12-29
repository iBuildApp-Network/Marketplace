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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class Utils {

    public static Bitmap CreateSquareColorBitmap(String colorString) {
        return Utils.CreateSquareColorBitmap(Color.parseColor(colorString));
    }

    public static Bitmap CreateSquareColorBitmap(int color) {
        int[] colors = new int[100];
        for (int i = 0; i < 100; i++) {
            colors[i] = color;
        }
        Bitmap bmp = Bitmap.createBitmap(colors, 10, 10, Bitmap.Config.ARGB_8888);
        return bmp;
    }

    public static Bitmap BmpResizeDisplay(Bitmap src, Activity ctx) {
        Display display = ctx.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrix = new DisplayMetrics();
        display.getMetrics(metrix);

        return Utils.BmpResize(src, metrix.widthPixels, metrix.heightPixels);
    }

    public static Bitmap BmpResize(Bitmap src, int newWidth, int newHeight) {
        int width = src.getWidth();
        int height = src.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    public static float PixelsToDpi(float pixels) {
        return pixels;
    }

    public static float DpiToPixels(float dpi) {
        return dpi;
    }

    public static String AppFolder(Activity ctx) {
        ApplicationInfo info = ctx.getApplicationInfo();
        PackageManager pm = ctx.getPackageManager();
        CharSequence c = pm.getApplicationLabel(info);
        String appLabel = c.toString();
        String appFolder = Environment.getExternalStorageDirectory() + "/" + "AppBuilder"/*appLabel*/ /*+ "/" */;
        File file = new File(appFolder);
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (SecurityException e) {
                appFolder = null;
            }
        }
        return appFolder;
    }


    public static String readXmlFromFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        return stringBuilder.toString();
    }
    public static boolean streamToFile(InputStream in, String outFilePath) {
        boolean result = true;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(outFilePath));
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
        } catch (Exception e) {
            result = false;
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {

            }
        }
        return result;
    }

    public static String PluginFolder(Activity ctx) {
        String appFolder = Utils.AppFolder(ctx);
        if (appFolder == null) {
            return null;
        }
        String pluginFolder = appFolder + "/plugins/";
        File file = new File(pluginFolder);
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (SecurityException e) {
                pluginFolder = null;
            }
        }
        return pluginFolder;
    }

    public static String CheckTmpDirectory(Activity ctx) {
        String appFolder = Utils.AppFolder(ctx);
        String tmpFolder = appFolder + "/tmp";
        File file = new File(tmpFolder);
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (SecurityException e) {
                e.printStackTrace();
                return null;
            }
        }
        return tmpFolder;
    }

    public static String newTempFilePath(Activity ctx) {
        String tmpFolder = Utils.CheckTmpDirectory(ctx);
        if (tmpFolder == null) {
            return null;
        }
        UUID id = UUID.randomUUID();
        return tmpFolder + "/" + String.valueOf(id);
    }

    public static boolean sdcardAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static void exitProcess() {
        Process.killProcess(Process.myPid());
    }

    public static boolean networkAvailable(Activity ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static boolean usedMobileNetwork(Activity ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null)
            return false;
        return (ni.getType() != ConnectivityManager.TYPE_MOBILE);
    }

    public static String fromBase64(String data) {
        byte[] base64;
        try {
            base64 = Base64.decode(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new String(base64);
    }

    public static String toBase64(String data) {
        String result;
        try {
            result = Base64.encodeBytes(data.getBytes());
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public static String removeSpec(String source) {
        String temp = source;
        while (checkForSpec(temp)) {
            if (temp.contains("&amp;")) {
                int start = temp.indexOf("&amp;");
                char[] buffer_start = new char[start];
                temp.getChars(0, start, buffer_start, 0);
                int end = start + 5;
                char[] buffer_end = new char[temp.length() - end];
                temp.getChars(end, temp.length(), buffer_end, 0);
                temp = "";
                temp = new String(buffer_start) + "&" + new String(buffer_end);
            } else if (temp.contains("&apos;")) {
                int start = temp.indexOf("&apos;");
                char[] buffer_start = new char[start];
                temp.getChars(0, start, buffer_start, 0);
                int end = start + 6;
                char[] buffer_end = new char[temp.length() - end];
                temp.getChars(end, temp.length(), buffer_end, 0);
                temp = new String(buffer_start) + "\'" + new String(buffer_end);
            } else if (temp.contains("&quot;")) {
                int start = temp.indexOf("&quot;");
                char[] buffer_start = new char[start];
                temp.getChars(0, start, buffer_start, 0);
                int end = start + 6;
                char[] buffer_end = new char[temp.length() - end];
                temp.getChars(end, temp.length(), buffer_end, 0);
                temp = new String(buffer_start) + "\"" + new String(buffer_end);
            } else if (temp.contains("&lt;")) {
                int start = temp.indexOf("&lt;");
                char[] buffer_start = new char[start];
                temp.getChars(0, start, buffer_start, 0);
                int end = start + 4;
                char[] buffer_end = new char[temp.length() - end];
                temp.getChars(end, temp.length(), buffer_end, 0);
                temp = new String(buffer_start) + "<" + new String(buffer_end);
            } else if (temp.contains("&qt;")) {
                int start = temp.indexOf("&qt;");
                char[] buffer_start = new char[start];
                temp.getChars(0, start, buffer_start, 0);
                int end = start + 4;
                char[] buffer_end = new char[temp.length() - end];
                temp.getChars(end, temp.length(), buffer_end, 0);
                temp = new String(buffer_start) + ">" + new String(buffer_end);
            } else if (temp.contains("&#8217;")) {
                temp = temp.replace("&#8217;", "'");
            } else if (temp.contains("&#8220;")) {
                temp = temp.replace("&#8220;", "\"");
            } else if (temp.contains("&#8221;")) {
                temp = temp.replace("&#8221;", "\"");
            } else if (temp.contains("&#124;")) {
                temp = temp.replace("&#124;", "|");
            } else if (temp.contains("&#8211;")) {
                temp = temp.replace("&#8211;", "-");
            } else if (temp.contains("\u0010")) {
                temp = temp.replace("\u0010", "");
            }
        }

        return temp;
    }

    public static boolean checkForSpec(String source) {
        if (source.contains("&amp;"))
            return true;
        else if (source.contains("&apos;"))
            return true;
        else if (source.contains("&quot;"))
            return true;
        else if (source.contains("&lt;"))
            return true;
        else if (source.contains("&gt;"))
            return true;
        else if (source.contains("&#8217;"))
            return true;
        else if (source.contains("&#8220;"))
            return true;
        else if (source.contains("&#8221;"))
            return true;
        else if (source.contains("&#124;"))
            return true;
        else if (source.contains("&#8211;"))
            return true;
        else if (source.contains("\u0010"))
            return true;
        else
            return false;
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

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    public static String toHex(byte[] data) {
        char[] chars = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            chars[i * 2] = HEX_DIGITS[(data[i] >> 4) & 0xf];
            chars[i * 2 + 1] = HEX_DIGITS[data[i] & 0xf];
        }
        return new String(chars);
    }

    public static String md5ForFile(String path) {
        MessageDigest messageDigest = null;
        String md5 = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            InputStream inputStream = new FileInputStream(path);
            new DigestInputStream(inputStream, messageDigest);
            while((inputStream.read()) != -1);
            byte[] digest = messageDigest.digest();
            md5 = new BigInteger(1, digest).toString(16);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return md5;
    }

    public static boolean isChemeDark(int backColor) {
        int r = (backColor >> 16) & 0xFF;
        int g = (backColor >> 8) & 0xFF;
        int b = (backColor >> 0) & 0xFF;

        double Y = (0.299 * r + 0.587 * g + 0.114 * b);
        if (Y > 127) {
            return true;
        } else {
            return false;
        }
    }


    public static JSONObject inputStreamToJSONObject(InputStream is) {
        BufferedReader streamReader = null;
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        JSONObject jsonObject = null;

        try {
            streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            jsonObject = new JSONObject(responseStrBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static String inputStreamToString(InputStream in) {
        InputStreamReader is = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = null;
        try {
            read = br.readLine();
            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public static byte[] loadImageFromUrl(String imageUrl) {
        byte[] bytes = null;

        try {
            URL url = new URL(imageUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            bytes = stream.toByteArray();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static byte[] loadImageFromResource(Integer resource, Activity ctx) {
        byte[] bytes;
        // Get image from resources
        Drawable d = ctx.getResources().getDrawable(resource); // the drawable (Captain Obvious, to the rescue!!!)
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        bytes = stream.toByteArray();
        return bytes;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        //
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String fileId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileId, options);
    }

    /**
     * Создание drawable из URL
     *
     * @param url - ссылка на изображение
     * @return Drawable - изображение
     * @throws IOException
     */
    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }

    /**
     * Скачивает файл по URL и сохраняет его по указанному пути + md5(url)
     * т.е. path +/+ md5(url)
     *
     * @param path - папка в которую нужно сохранить файлик
     * @param url  - ссыль на файл который нужно скачать
     * @return - абсолютный путь к скаченному файлу
     */
    public static String downloadFile(String url, String path) {
        int BYTE_ARRAY_SIZE = 1024;

        // downloading cover image and saving it into file
        try {
            URL imageUrl = new URL(URLDecoder.decode(url));
            URLConnection conn = imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File resFile = new File(path + File.separator + md5(url));
            if (!resFile.exists())
                resFile.createNewFile();
            else {
                resFile.delete();
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
            Log.e("API - downloadFile() - SocketTimeoutException", "An error has occurred downloading the image: " + url);
            return null;
        } catch (IllegalArgumentException e) {
            Log.e("API - downloadFile() - IllegalArgumentException", "An error has occurred downloading the image: " + url);
            return null;
        } catch (Exception e) {
            Log.e("API - downloadFile() - Exception", "An error has occurred downloading the image: " + url);
            return null;
        }
    }

    public static boolean sendClaim(String imagePath) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        HttpClient httpClient = new DefaultHttpClient(params);

        try {
            HttpPost httpPost = new HttpPost("http://ibuildapp.com/endpoint/masterapp.php");//("http://ibuilder.solovathost.com/endpoint/masterapp.php");//

            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntity.addPart("app_id", new StringBody(com.appbuilder.sdk.android.Statics.appId, Charset.forName("UTF-8")));//new StringBody("2697", Charset.forName("UTF-8")));//
            //multipartEntity.addPart("app_id", new StringBody("748", Charset.forName("UTF-8")));
            multipartEntity.addPart("action", new StringBody("rep_forbidden_content", Charset.forName("UTF-8")));

            if (!TextUtils.isEmpty(imagePath)) {
                multipartEntity.addPart("screenshot", new FileBody(new File(imagePath)));
            }

            httpPost.setEntity(multipartEntity);

            //httpClient.execute(httpPost, null);

            HttpResponse response = httpClient.execute(httpPost);

            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static void sendSms(Activity context, String message) throws ActivityNotFoundException {
        Uri uri = Uri.parse("sms:");

        Intent intent = new Intent();
        intent.setData(uri);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra("sms_body", message);
        intent.putExtra("address", "");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_SENDTO);
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
            if(defaultSmsPackageName != null) {
                intent.setPackage(defaultSmsPackageName);
            }
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setType("vnd.android-dir/mms-sms");
        }

        context.startActivity(intent);
    }

}