package com.appbuilder.core.PushNotification;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.appbuilder.core.AppBuilder;
import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.core.PushNotification.services.PushNotificationService;
import com.appbuilder.core.R;
import com.appbuilder.core.Statics;
import com.appbuilder.core.tools.Tools;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.flurry.android.FlurryAgent;
import com.google.android.c2dm.C2DMBaseReceiver;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class AppPushNotificationReceiver extends /*BroadcastReceiver*/C2DMBaseReceiver{
    public static final String ACTION = "com.appbuilder.core.PUSH_MESSAGE";
    
    private static final String TAG = "PUSHNS_receiver";
    private String pushRegistrationUrl = "http://" + 
                                          AppBuilder.DOMEN +
                                          "/pushns.registration.php?project=" +
                                          AppBuilder.APP_ID + 
                                          "&platform=android";
    //"http://ibuildapp.com/pushns.registration.php?project=834612&platform=android";

    /**
     * The C2DMReceiver class must create a no-arg constructor and pass the
     * sender id to be used for registration.
     *
     * @param senderId
     */
    public AppPushNotificationReceiver(String senderId) {
        super(senderId);
    }

    public AppPushNotificationReceiver() {
        super("");
    }

    /*@Override
    public void onReceive(Context context, Intent intent) {
        PushNotificationService.writeToLog("receiver onreceive");
        
        Log.e(TAG, "broadcast recieved");
        
        onMessage(context, intent);
    }*/

    @Override
    public void onRegistered(Context context, String registrationId) {
        Log.d(TAG, "Registration ID arrived: Fantastic!!!");
        Log.d(TAG, registrationId);

		/* here send registration code to server */
        String UID = md5(android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
        pushRegistrationUrl += "&device=" + UID;
        pushRegistrationUrl += "&token=" + registrationId;

        //Log.d(TAG, pushRegistrationUrl);
        try {
            URL url = new URL(pushRegistrationUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            StringBuilder sb = new StringBuilder();
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
            in.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void onUnregistered(Context context) {
        Log.w(TAG, "onUnregistered");
    }

    // *****************************************************************************************************************
    // вносишь изменения здесь, посмотри в appbuildermodulemain!!!!
    // *****************************************************************************************************************
    @Override
    protected void onMessage(Context context, Intent receivedIntent) {
        messageReceived(context, receivedIntent);
        /*Log.e(TAG, "onMessage");
        Log.e(TAG, "App status = " + getAppStatus(context));

        Log.e(TAG, "start *******************");
        Bundle bundle = receivedIntent.getExtras();
        Set<String> keys = bundle.keySet();
        for (String s : keys) {
            String value = bundle.getString(s);
            Log.e(TAG, "Key = " + s + " Val = " + value + "\n");
        }
        Log.e(TAG, "******************* end");

        // событие Flury, что пришел пуш для разных типов
        String type = receivedIntent.getStringExtra("type");
        Log.e(TAG, "Type = " + type);
        int typeInt = Integer.parseInt(type);
        switch (typeInt) {

            case 1: // обычный пуш сделанный вручную
            {
                Log.e(TAG, "TYPE = PushNS");
                FlurryAgent.logEvent("PushNS");

                // *************************************************************************************************************
                // обработка стандартного случая обработки пуша
                int order = -1;
                if (!TextUtils.isEmpty(receivedIntent.getStringExtra("order"))) {
                    try {
                        order = Integer.parseInt(receivedIntent.getStringExtra("order"));
                    } catch (Exception e) {
                        order = -1;
                    }
                }

                // введен только message - показываем только пуш в статус баре + в базу НЕ записываем
                if (!TextUtils.isEmpty(receivedIntent.getStringExtra("message")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("title")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("descr")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("image")) && order == -1) {

                    Log.e(TAG, "push with status bar message only");

                    // формируем соощение в статус-бар
                    notificationBarTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    notificationTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    notificationText = receivedIntent.getStringExtra("message");
                    try {
                        Intent intent = new Intent(context, AppBuilder.class);
                        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
                        notification.defaults = Notification.DEFAULT_SOUND;
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        notification.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);

                        // в качестве id используем произвольное число, т.к. в базу это сообщение не ложится
                        mManager.notify((int) 1, notification);

                    } catch (Exception e) {
                    }
                    return;
                }

                // in case when status-bar message exists and other fields are empty
                AppPushNotificationMessage tempMessge = null;
                if (!TextUtils.isEmpty(receivedIntent.getStringExtra("message")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("title")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("descr"))) {

                    tempMessge = new AppPushNotificationMessage(receivedIntent.getStringExtra("package"),
                            "",
                            receivedIntent.getStringExtra("message"),
                            receivedIntent.getStringExtra("message"),
                            receivedIntent.getStringExtra("image"),
                            new Date(),
                            order);

                } else {
                    tempMessge = new AppPushNotificationMessage(receivedIntent.getStringExtra("package"),
                            receivedIntent.getStringExtra("title"),
                            receivedIntent.getStringExtra("message"),
                            receivedIntent.getStringExtra("descr"),
                            receivedIntent.getStringExtra("image"),
                            new Date(),
                            order);
                }
                messageObject = new AppPushNotificationMessage(
                        tempMessge.packageName,
                        tempMessge.titleText,
                        tempMessge.statusBarText,
                        tempMessge.descriptionText,
                        tempMessge.imgUrl,
                        new Date(), order);

                Log.e(TAG, " Normal push = " + messageObject.uid
                        + " messageObject.statusBar = " + messageObject.statusBarText
                        + " messageObject.description = " + messageObject.descriptionText
                        + " messageObject.title = " + messageObject.titleText
                        + " messageObject.imgUrl = " + messageObject.imgUrl
                        + " messageObject.path = " + messageObject.imagePath
                        + " messageObject.widgetOrder = " + messageObject.widgetOrder);

                // добавляем пуш в базу
                AppPushNotificationDB.init(context);
                long id = AppPushNotificationDB.insertNotification(messageObject);
                messageObject.uid = id;
                Log.e(TAG, " Push in DB. UID = " + messageObject.uid);

                // дальнейшая обработка - картинка и вывод сообщения в статус-бар
                new CustomThread(messageObject, context).start();
            }
            break;

            case 2: // пуш автообновления
            {
                Log.e(TAG, " TYPE = PushNS ConfigurationUpdate");
                FlurryAgent.logEvent("PushNS ConfigurationUpdate");

                if (getAppStatus(context).compareToIgnoreCase(context.getString(R.string.core_pushns_status_running)) == 0) // приложение запущено -> заносми сообщение в базу и делаем диалоговое окно
                {
                    // формируем сообщение
                    messageObject = new AppPushNotificationMessage(
                            "",
                            "",
                            "",
                            context.getString(R.string.core_pushns_content_update_msg),
                            "",
                            new Date(), -1);

                    // добавляем пуш в базу
                    AppPushNotificationDB.init(context);
                    long id = AppPushNotificationDB.insertNotification(messageObject);
                    messageObject.uid = id;
                    Log.e(TAG, " Push in DB. UID = " + messageObject.uid);

                    // ******************************************
                    // рассылваем широковещательное сообщение
                    Intent messageIntent = new Intent(Statics.BROADCAST_UID);
                    context.sendBroadcast(messageIntent);

                    // формируем соощение в статус-бар
                    notificationBarTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    notificationTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    notificationText = receivedIntent.getStringExtra("message");
                    try {
                        Intent intent = new Intent(context, AppBuilder.class);
                        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
                        notification.defaults = Notification.DEFAULT_SOUND;
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        notification.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);

                        // в качестве id используем произвольное число, т.к. в базу это сообщение не ложится
                        mManager.notify((int) messageObject.uid, notification);

                    } catch (Exception e) {
                    }
                } else // приложение выгружено из памяти -> генерим сообщение в статус бар
                {
                    // формируем соощение в статус-бар
                    notificationBarTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    notificationTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    notificationText = receivedIntent.getStringExtra("message");
                    try {
                        Intent intent = new Intent(context, AppBuilder.class);
                        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
                        notification.defaults = Notification.DEFAULT_SOUND;
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        notification.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);

                        // в качестве id используем произвольное число, т.к. в базу это сообщение не ложится
                        mManager.notify((int) -1, notification);

                    } catch (Exception e) {
                    }
                }

            }
            break;

            case 3: // пуш для виджетов audio, video, photogallery
            {
                Log.e(TAG, "TYPE = PushNS Widget");
                FlurryAgent.logEvent("PushNS Widget");

                // *************************************************************************************************************
                // если это мессадж для подсистемы сообщений для галереи, аудио, видео, ТО в базу его не пишем - стразу рассылка
                String packageName = receivedIntent.getStringExtra("package");
                if (packageName != null) {
                    if (packageName.length() > 0) {
                        Intent it = new Intent(packageName + ".PUSH");
                        it.putExtras(receivedIntent.getExtras());
                        context.sendBroadcast(it);
                        return;
                    }
                }
            }
            break;
        }*/

    }

    @Override
    public void onError(Context context, String errorId) {
        Log.w(TAG, "onError: " + errorId);
    }
    
    static void messageReceived(Context context, Intent receivedIntent){
        Log.e(TAG, "onMessage");
        Log.e(TAG, "App status = " + getAppStatus(context));

        Log.e(TAG, "start *******************");
        Bundle bundle = receivedIntent.getExtras();
        Set<String> keys = bundle.keySet();
        for (String s : keys) {
            String value = bundle.getString(s);
            //Log.e(TAG, "Key = " + s + " Val = " + value + "\n");
        }
        Log.e(TAG, "******************* end");

        // событие Flury, что пришел пуш для разных типов
        String type = receivedIntent.getStringExtra("type");
        Log.e(TAG, "Type = " + type);
        int typeInt = Integer.parseInt(type);
        switch (typeInt) {

            case 1: // обычный пуш сделанный вручную
            {
                Log.e(TAG, "TYPE = PushNS");
                FlurryAgent.logEvent("PushNS");

                // *************************************************************************************************************
                // обработка стандартного случая обработки пуша
                int order = -1;
                if (!TextUtils.isEmpty(receivedIntent.getStringExtra("order"))) {
                    try {
                        order = Integer.parseInt(receivedIntent.getStringExtra("order"));
                    } catch (Exception e) {
                        order = -1;
                    }
                }

                // введен только message - показываем только пуш в статус баре + в базу НЕ записываем
                if (!TextUtils.isEmpty(receivedIntent.getStringExtra("message")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("title")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("descr")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("image")) && order == -1) {

                    Log.e(TAG, "push with status bar message only");

                    // формируем соощение в статус-бар
                    String notificationBarTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    String notificationTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    String notificationText = receivedIntent.getStringExtra("message");
                    try {
                        Intent intent = new Intent(context, AppBuilder.class);
                        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
                        notification.defaults = Notification.DEFAULT_SOUND;
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                //        notification.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);

                        // в качестве id используем произвольное число, т.к. в базу это сообщение не ложится
                        mManager.notify((int) 1, notification);

                    } catch (Exception e) {
                    }
                    return;
                }

                // in case when status-bar message exists and other fields are empty
                AppPushNotificationMessage tempMessge = null;
                if (!TextUtils.isEmpty(receivedIntent.getStringExtra("message")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("title")) &&
                        TextUtils.isEmpty(receivedIntent.getStringExtra("descr"))) {

                    tempMessge = new AppPushNotificationMessage(receivedIntent.getStringExtra("package"),
                            "",
                            receivedIntent.getStringExtra("message"),
                            receivedIntent.getStringExtra("message"),
                            receivedIntent.getStringExtra("image"),
                            new Date(),
                            order);

                } else {
                    tempMessge = new AppPushNotificationMessage(receivedIntent.getStringExtra("package"),
                            receivedIntent.getStringExtra("title"),
                            receivedIntent.getStringExtra("message"),
                            receivedIntent.getStringExtra("descr"),
                            receivedIntent.getStringExtra("image"),
                            new Date(),
                            order);
                }
                AppPushNotificationMessage messageObject = new AppPushNotificationMessage(
                        tempMessge.packageName,
                        tempMessge.titleText,
                        tempMessge.statusBarText,
                        tempMessge.descriptionText,
                        tempMessge.imgUrl,
                        new Date(), order);

                /*Log.e(TAG, " Normal push = " + messageObject.uid
                        + " messageObject.statusBar = " + messageObject.statusBarText
                        + " messageObject.description = " + messageObject.descriptionText
                        + " messageObject.title = " + messageObject.titleText
                        + " messageObject.imgUrl = " + messageObject.imgUrl
                        + " messageObject.path = " + messageObject.imagePath
                        + " messageObject.widgetOrder = " + messageObject.widgetOrder);*/

                // добавляем пуш в базу
                AppPushNotificationDB.init(context);
                long id = AppPushNotificationDB.insertNotification(messageObject);
                messageObject.uid = id;
                Log.e(TAG, " Push in DB. UID = " + messageObject.uid);

                // дальнейшая обработка - картинка и вывод сообщения в статус-бар
                new CustomThread(messageObject, context).start();
            }
            break;

            case 2: // пуш автообновления
            {
                Log.e(TAG, " TYPE = PushNS ConfigurationUpdate");
                FlurryAgent.logEvent("PushNS ConfigurationUpdate");

                if (getAppStatus(context).compareToIgnoreCase(context.getString(R.string.core_pushns_status_running)) == 0) // приложение запущено -> заносми сообщение в базу и делаем диалоговое окно
                {
                    // формируем сообщение
                   AppPushNotificationMessage messageObject = new AppPushNotificationMessage(
                            "",
                            "",
                            "",
                            context.getString(R.string.core_pushns_content_update_msg),
                            "",
                            new Date(), -1);

                    // добавляем пуш в базу
                    AppPushNotificationDB.init(context);
                    long id = AppPushNotificationDB.insertNotification(messageObject);
                    messageObject.uid = id;
                    Log.e(TAG, " Push in DB. UID = " + messageObject.uid);

                    // ******************************************
                    // рассылваем широковещательное сообщение
                    Intent messageIntent = new Intent(Statics.BROADCAST_UID);
                    context.sendBroadcast(messageIntent);

                    // формируем соощение в статус-бар
                    String notificationBarTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    String notificationTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    String notificationText = receivedIntent.getStringExtra("message");
                    try {
                        Intent intent = new Intent(context, AppBuilder.class);
                        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
                        notification.defaults = Notification.DEFAULT_SOUND;
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    //    notification.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);

                        // в качестве id используем произвольное число, т.к. в базу это сообщение не ложится
                        mManager.notify((int) messageObject.uid, notification);

                    } catch (Exception e) {
                    }
                } else // приложение выгружено из памяти -> генерим сообщение в статус бар
                {
                    // формируем соощение в статус-бар
                    String notificationBarTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    String notificationTitle = context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.core_notification);
                    String notificationText = receivedIntent.getStringExtra("message");
                    try {
                        Intent intent = new Intent(context, AppBuilder.class);
                        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
                        notification.defaults = Notification.DEFAULT_SOUND;
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                 //       notification.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);

                        // в качестве id используем произвольное число, т.к. в базу это сообщение не ложится
                        mManager.notify((int) -1, notification);

                    } catch (Exception e) {
                    }
                }

            }
            break;

            case 3: // пуш для виджетов audio, video, photogallery
            {
                Log.e(TAG, "TYPE = PushNS Widget");
                FlurryAgent.logEvent("PushNS Widget");

                // *************************************************************************************************************
                // если это мессадж для подсистемы сообщений для галереи, аудио, видео, ТО в базу его не пишем - стразу рассылка
                String packageName = receivedIntent.getStringExtra("package");
                if (packageName != null) {
                    if (packageName.length() > 0) {
                        Intent it = new Intent(packageName + ".PUSH");
                        it.putExtras(receivedIntent.getExtras());
                        context.sendBroadcast(it);
                        return;
                    }
                }
            }
            break;
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
            Log.w("WebPlugin CREATE MD5", e);
        }

        return null;
    }

    private static class CustomThread extends Thread {
        private AppPushNotificationMessage pushMessage;
        
        private Context currentContext;

        private CustomThread(AppPushNotificationMessage pushMessage, Context ctx) {
            this.pushMessage = pushMessage;
            
            this.currentContext = ctx;
        }

        @Override
        public void run() {
            super.run();
            if (!TextUtils.isEmpty(pushMessage.imgUrl)) {
                // Докачка картинки для сообщения
                Log.e(TAG, "DOWNLOAD START");
                String cachePath = Environment.getExternalStorageDirectory() + "/AppBuilder/" + currentContext.getPackageName();
                String resPath = Utils.downloadFile(pushMessage.imgUrl, cachePath);
                if (TextUtils.isEmpty(resPath))
                    resPath = "";
                AppPushNotificationDB.updateNotificationImage(pushMessage.uid, resPath);
                Log.e(TAG, " DOWNLOAD after update. pushMessage.uid = " + pushMessage.uid
                        + " pushMessage.title = " + pushMessage.titleText
                        + " pushMessage.statusBar = " + pushMessage.statusBarText
                        + " pushMessage.description = " + pushMessage.descriptionText
                        + " pushMessage.imgUrl = " + pushMessage.imgUrl
                        + " pushMessage.path = " + resPath);
            }

            // отладка...
            List<AppPushNotificationMessage> mse = AppPushNotificationDB.selectAllNotifications();
            for (AppPushNotificationMessage s : mse) {
                Log.e(TAG, s.toString());
                Log.e(TAG, "\n");
            }

            // ******************************************
            // обновляем xml для приложение

            // TODO вот туточки надо будет обновлять все картинки для только что скаченной конфигурации
            String xmlUrl = AppPushNotificationDB.getXmlUrl();
            Log.e(TAG, "XMLURL = " + xmlUrl);
            AppConfigure config = Tools.updateAppConfig(currentContext, xmlUrl);
            if (config != null) {
                Log.e(TAG, "update configuration - success");

                // проверяем есть ли у нас класс для этого виджета
                Widget widget = config.getWidgetWithOrder(pushMessage.widgetOrder);
                if (widget != null) {
                    try {
                        Class.forName(widget.getPluginPackage() + "." + widget.getPluginName());
                        AppPushNotificationDB.updateNotificationPackageStatus(pushMessage.uid, true);
                        //Log.e(TAG, "Package = " + widget.getPluginPackage() + "." + widget.getPluginName() + "EXIST");
                    } catch (ClassNotFoundException e) {
                        AppPushNotificationDB.updateNotificationPackageStatus(pushMessage.uid, false);
                        //Log.e(TAG, "Package = " + widget.getPluginPackage() + "." + widget.getPluginName() + "NOT EXIST");
                    }
                } else {
                    AppPushNotificationDB.updateNotificationPackageStatus(pushMessage.uid, false);
                    Log.e(TAG, "NO Such widget");
                }

            } else
                Log.e(TAG, "update configuration - error");


            // ******************************************
            // рассылваем широковещательное сообщение
            Intent messageIntent = new Intent(Statics.BROADCAST_UID);
            currentContext.sendBroadcast(messageIntent);

            // формируем соощение в статус-бар
            String notificationBarTitle = currentContext.getResources().getString(R.string.app_name) + " " + currentContext.getResources().getString(R.string.core_notification);
            String notificationTitle = currentContext.getResources().getString(R.string.app_name) + " " + currentContext.getResources().getString(R.string.core_notification);
            String notificationText = pushMessage.statusBarText;

            try {
                Intent intent = new Intent(currentContext, AppBuilder.class);
                NotificationManager mManager = (NotificationManager) currentContext.getSystemService(Context.NOTIFICATION_SERVICE);

                Notification notification = new Notification(R.drawable.icon_notification, notificationBarTitle, System.currentTimeMillis());
                notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
                notification.defaults = Notification.DEFAULT_SOUND;
                PendingIntent pendingIntent = PendingIntent.getActivity(currentContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
              //  notification.setLatestEventInfo(currentContext, notificationTitle, notificationText, pendingIntent);

                // в качестве id для нотификации используем id уведомления из базы, чтобы потом его можно было удалить
                mManager.notify((int) pushMessage.uid, notification);
            } catch (Exception e) {
            }

        }
    }

    public static String getAppStatus(Context currentContext) {
        SharedPreferences preferences = currentContext.getSharedPreferences(currentContext.getString(R.string.core_pushns_status), Context.MODE_WORLD_WRITEABLE);
        return preferences.getString(currentContext.getPackageName(), currentContext.getString(R.string.core_pushns_status_closed));
    }
}
