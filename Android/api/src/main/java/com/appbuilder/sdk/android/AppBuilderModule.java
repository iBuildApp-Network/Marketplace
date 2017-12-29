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
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.flurry.android.FlurryAgent;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class AppBuilderModule extends Activity implements AppBuilderInterface {

    private static final String TAG = "com.ibuildapp.core.AppBuilderModule";
    private boolean flurryStarted = false;

    private long millis;

    private String flurryId = "";

    private AppAdvData advData = null;
    private AppAdvView adView = null;
    private Serializable session = null;
    protected Bundle state = null;

    private Widget widget = null;

    protected HashMap<Object, HashMap<Object, Object>> nativeFeatures = new HashMap<Object, HashMap<Object, Object>>();

    static public enum NATIVE_FEATURES {
        SMS, EMAIL, ADD_CONTACT, ADD_EVENT,
        LOCAL_NOTIFICATION
    }

    ;

    final private int MENU_ITEM_SMS_CLICK = 1;
    final private int MENU_ITEM_EMAIL_CLICK = 2;
    final private int MENU_ITEM_ADD_CONTACT_CLICK = 3;
    final private int MENU_ITEM_ADD_EVENT_CLICK = 4;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MENU_ITEM_SMS_CLICK: {
                    sendSMS();
                    //Toast.makeText(AppBuilderModule.this, "SMS Clicked" , Toast.LENGTH_LONG).show();
                }
                break;
                case MENU_ITEM_EMAIL_CLICK: {
                    sendEmail();
                    //Toast.makeText(AppBuilderModule.this, "Email Clicked" , Toast.LENGTH_LONG).show();
                }
                break;
                case MENU_ITEM_ADD_CONTACT_CLICK: {
                    //Toast.makeText(AppBuilderModule.this, "Add Contact Clicked" , Toast.LENGTH_LONG).show();
                    addContact();
                }
                break;
                case MENU_ITEM_ADD_EVENT_CLICK: {
                    //Toast.makeText(AppBuilderModule.this, "Add Event Clicked" , Toast.LENGTH_LONG).show();
                    addEvent();
                }
                break;
            }
        }
    };

    /* Activity methods */
    @Override
    final public void onCreate(Bundle savedInstanceState) {
        try {//ErrorLogging

            super.onCreate(savedInstanceState);
            state = savedInstanceState;
            try {
                session = savedInstanceState.getSerializable("session");
            } catch (Exception e) {
            }

            readConfiguration();
            try {
                Intent currentIntent = getIntent();
                Bundle store = currentIntent.getExtras();
                advData = (AppAdvData) store.getSerializable("Advertisement");
                boolean fs = store.getBoolean("firstStart");

                if (advData.getAdvType().length() > 0) {
                    adView = new AppAdvView(this, advData, fs);
                }

                flurryId = store.getString("flurry_id");

                widget = (Widget) store.getSerializable("Widget");
                super.setTitle(widget.getTitle());

                // for test purpose
                //advData.setAdvType("gAd");
                //advData.setAdvContent("a1506e9d0a47840");


            } catch (Exception e) {
            }

        } catch (Exception e) {
            finish();
        }

        try {

            create();

        } catch (Exception e) {
        }
    }

    @Override
    final public void setTitle(CharSequence title) {
        // super.setTitle(title);
    }

    @Override
    final public void setTitle(int titleId) {
        //   super.setTitle(titleId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {//ErrorLogging

            if (nativeFeatures.isEmpty()) {
                return false;
            }

            menu.clear();
            for (Entry<Object, HashMap<Object, Object>> entry : nativeFeatures.entrySet()) {
                Object feature = entry.getKey();
                if (feature.equals(AppBuilderModule.NATIVE_FEATURES.SMS)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Send SMS");
                    menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_SMS_CLICK);
                            return true;
                        }
                    });
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.EMAIL)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Send Email");
                    menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_EMAIL_CLICK);
                            return true;
                        }
                    });
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_CONTACT)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Add Contact");
                    menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_ADD_CONTACT_CLICK);
                            return true;
                        }
                    });
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_EVENT)) {
                    MenuItem menuItem = menu.add("");
                    menuItem.setTitle("Add Event");
                    menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            handler.sendEmptyMessage(MENU_ITEM_ADD_EVENT_CLICK);
                            return true;
                        }
                    });
                }
            }
            return super.onPrepareOptionsMenu(menu);

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    final public void onStart() {
//        try {
//            String pluginName = Statics.mapPluginConversation.get(widget.getPluginName());
//            if (pluginName == null) {
//                pluginName = widget.getPluginName();
//            }
//            LogDebug(String.format("Start Module %s", pluginName));
//            Long stub = new Long((long)0);
//            EasyTracker.getInstance(this).send(
//                    MapBuilder.createEvent("LifeCycle", "Start Module", pluginName, stub).build());
//        } catch (Exception ex) {
//            LogError(ex);
//        }

        try {
            millis = System.currentTimeMillis();

            Map<String, String> map = new HashMap<String, String>();
            map.put("action", "start");
            FlurryAgent.logEvent(this.getClass().getSimpleName(), map);
        } catch (Exception ex) {
            Log.d("", "");
        }

        start();
        super.onStart();
    }

    private void LogError(Exception ex) {
        Log.e(TAG, "", ex);
    }

    private void LogDebug(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    final public void onRestart() {
        restart();
        super.onRestart();
    }

    @Override
    final public void onResume() {
        resume();
        super.onResume();
    }

    @Override
    final public void onPause() {
        pause();
        super.onPause();
    }

    @Override
    final public void onStop() {
        try {
            int seconds = (int) (System.currentTimeMillis() - millis) / 1000;
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", "stop");
            map.put("usage interval", "" + seconds);
            FlurryAgent.logEvent(this.getClass().getSimpleName(), map);
        } catch (Exception ex) {
            Log.d("", "");
        }
//        // Google Analytics
//        try {
//            String pluginName = Statics.mapPluginConversation.get(widget.getPluginName());
//            if (pluginName == null) {
//                pluginName = widget.getPluginName();
//            }
//            LogDebug(String.format("stop plugin  %s", pluginName));
//            Long seconds = (System.currentTimeMillis() - millis) / 1000;
//            EasyTracker.getInstance(this).send(
//                    MapBuilder.createEvent("LifeCycle", "Stop Module", pluginName, seconds).build());
//        } catch (Exception ex) {
//            LogError(ex);
//        }

        stop();
        super.onPause();
    }

    @Override
    final public void onDestroy() {
        writeConfiguration();

        destroy();
        super.onDestroy();
    }

    @Override
    final public void setContentView(int layoutResID) {
        try {//ErrorLogging

            LinearLayout rootLayout = new LinearLayout(this);
            //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
            //rootLayout.setLayoutParams(lp);
            rootLayout.setOrientation(LinearLayout.VERTICAL);

            try {
                if (adView != null) {
                    if (adView.getParent() != null) {
                        ViewGroup view = (ViewGroup) adView.getParent();
                        view.removeAllViews();
                    }
                    rootLayout.addView(adView);
                    adView.loadAdMob();
                }
            } catch (Exception e) {
                Log.w("", "");
            }

            View userLayout = LayoutInflater.from(this).inflate(layoutResID, null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            userLayout.setLayoutParams(params);

            rootLayout.addView(userLayout);

            super.setContentView(rootLayout);

        } catch (Exception e) {
        }
    }

    @Override
    final public void setContentView(View view) {
        try {

            LinearLayout rootLayout = new LinearLayout(this);
            rootLayout.setOrientation(LinearLayout.VERTICAL);

            if (adView != null) {
                rootLayout.addView(adView);
                adView.loadAdMob();
            }

            rootLayout.addView(view);
            super.setContentView(rootLayout);

        } catch (Exception e) {
        }
    }
    public void setTokenBanner(){
      /*  TextView countToken = new TextView(this);
        countToken.setText("Тест");
        LinearLayout adTokenLayout = new LinearLayout(this);
        LinearLayout.LayoutParams paramstoken = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20);
        adTokenLayout.setLayoutParams(paramstoken);
        adTokenLayout.setOrientation(LinearLayout.VERTICAL);
        adTokenLayout.addView(countToken);
        rootLayout.addView(adTokenLayout);*/
    }

    @Override
    final public void setContentView(View view, ViewGroup.LayoutParams params) {
        try {//ErrorLogging

            LinearLayout rootLayout = new LinearLayout(this);
            rootLayout.setOrientation(LinearLayout.VERTICAL);

            if (adView != null) {
                rootLayout.addView(adView);
                adView.loadAdMob();
            }

            rootLayout.addView(view, params);
            super.setContentView(rootLayout);

        } catch (Exception e) {
        }
    }

    @Override
    final public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("session", session);
    }

    @Override
    final public void startActivity(Intent intent) {
        try {//ErrorLogging

            boolean isExtends = true;
            if (intent.getAction() == null) {
                try {
                    /*Bundle store = new Bundle();
                    store.putSerializable("Advertisement", advData);
                    intent.putExtras(store);*/
                    intent.putExtra("Advertisement", advData);
                } catch (Exception e) {
                    Log.d("IBUILDAPP", e.toString());
                }

                ComponentName cn = intent.resolveActivity(getPackageManager());
                String className = cn.getClassName();

                Class class1 = null;
                try {
                    class1 = Class.forName(className);
                } catch (ClassNotFoundException cNFEx) {
                    Log.e("", "");
                }

                isExtends = false;

                try {
                    Class class2 = class1.getSuperclass();

                    while (true) {

                        if (class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModule")
                                || class1.getName().equals("com.ibuildapp.romanblack.CallPlugin.CallPlugin")
                                || class2.getName().equals("com.google.android.maps.MapActivity")
                                || class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModuleMain")
                                || class1.getName().equals("com.google.android.gms.ads.AdActivity")
                                || class1.getName().equals("com.google.android.youtube.PlayerActivity")
                                || class1.getName().equals("com.ibuildapp.romanblack.TableReservationPlugin.TableReservationPersonPicker")
                                || class1.getName().equals("com.ibuildapp.romanblack.CameraPlugin.chooser.ChooserActivity")
                                || class1.getName().equals("com.paypal.android.MEP.PayPalActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.LoginActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentMethodActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentConfirmActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentCompletedActivity")
                                ) {
                            isExtends = true;
                            break;
                        } else if (class2.getName().equals("java.lang.Object")) {
                            break;
                        }

                        class2 = class2.getSuperclass();

                    }
                } catch (NullPointerException nPEx) {
                }
            }

            if (isExtends) {
                super.startActivity(intent);
            } else {
                Toast.makeText(AppBuilderModule.this, "Your Activity should extend AppBuilderModule", Toast.LENGTH_LONG).show();
                return;
            }

            //super.startActivity(intent);

        } catch (Exception e) {
        }
    }

    @Override
    final public void startActivityForResult(Intent intent, int requestCode) {
        try {//ErrorLogging

            boolean isExtends = true;
            if (intent.getAction() == null) {
                try {
                    /*Bundle store = new Bundle();
                    store.putSerializable("Advertisement", advData);
                    intent.putExtras(store);*/
                    intent.putExtra("Advertisement", advData);
                } catch (Exception e) {
                    Log.d("IBUILDAPP", e.toString());
                }

                ComponentName cn = intent.resolveActivity(getPackageManager());
                String className = cn.getClassName();

                Class class1 = null;
                try {
                    class1 = Class.forName(className);
                } catch (ClassNotFoundException cNFEx) {
                    Log.e("", "");
                }

                isExtends = false;

                try {
                    Class class2 = class1.getSuperclass();

                    while (true) {

                        if (class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModule")
                                || class2.getName().equals("com.appbuilder.sdk.android.AppBuilderModuleMain")
                                || class1.getName().equals("com.ibuildapp.romanblack.CallPlugin.CallPlugin")
                                || class2.getName().equals("com.google.android.maps.MapActivity")
                                || class1.getName().equals("com.google.android.gms.ads.AdActivity")
                                || class1.getName().equals("com.google.android.youtube.PlayerActivity")
                                || class1.getName().equals("com.ibuildapp.romanblack.TableReservationPlugin.TableReservationPersonPicker")
                                || class1.getName().equals("com.ibuildapp.romanblack.CameraPlugin.chooser.ChooserActivity")
                                || class1.getName().equals("com.paypal.android.MEP.PayPalActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.LoginActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentMethodActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentConfirmActivity")
                                || class1.getName().equals("com.paypal.android.sdk.payments.PaymentCompletedActivity")
                                ) {
                            isExtends = true;
                            break;
                        } else if (class2.getName().equals("java.lang.Object")) {
                            break;
                        }

                        class2 = class2.getSuperclass();

                    }
                } catch (NullPointerException nPEx) {
                }
            }

            if (isExtends) {
                super.startActivityForResult(intent, requestCode);
            } else {
                Toast.makeText(AppBuilderModule.this, "Your Activity should extend AppBuilderModule", Toast.LENGTH_LONG).show();
                return;
            }

            //super.startActivity(intent);

        } catch (Exception e) {
        }
    }

    final public void setSession(Serializable session) {
        this.session = session;
    }

    final public Serializable getSession() {
        return session;
    }

    /* native features */
    final public void addNativeFeature(AppBuilderModule.NATIVE_FEATURES feature, Object parameter, Object value) {
        try {//ErrorLogging

            if (!nativeFeatures.containsKey(feature)) {
                HashMap<Object, Object> params = new HashMap<Object, Object>();
                if (feature.equals(AppBuilderModule.NATIVE_FEATURES.SMS)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.SMS.TEXT, hm.get("text"));
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.EMAIL)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.EMAIL.ADDRESS, null);
                    params.put(AppNativeFeature.EMAIL.SUBJECT, hm.get("subject"));
                    params.put(AppNativeFeature.EMAIL.TEXT, hm.get("text"));
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_CONTACT)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.CONTACT.NAME, hm.get("contactName"));
                    params.put(AppNativeFeature.CONTACT.PHONE, hm.get("contactNumber"));
                    params.put(AppNativeFeature.CONTACT.EMAIL, hm.get("contactEmail"));
                    params.put(AppNativeFeature.CONTACT.WEBSITE, hm.get("contactSite"));
                } else if (feature.equals(AppBuilderModule.NATIVE_FEATURES.ADD_EVENT)) {
                    HashMap<String, String> hm = (HashMap<String, String>) value;
                    params.put(AppNativeFeature.EVENT.TITLE, hm.get("title"));
                    params.put(AppNativeFeature.EVENT.BEGIN, hm.get("begin"));
                    params.put(AppNativeFeature.EVENT.END, hm.get("end"));
                    params.put(AppNativeFeature.EVENT.FREQUENCY, hm.get("frequency"));
                }
                nativeFeatures.put(feature, params);
            }

            if (nativeFeatures.containsKey(feature)) {
                if (nativeFeatures.get(feature).containsKey(parameter)) {
                    nativeFeatures.get(feature).put(parameter, value);
                }
            }

        } catch (Exception e) {
        }
    }

    final public void removeNativeFeature(AppBuilderModule.NATIVE_FEATURES feature) {
        if (nativeFeatures.containsKey(feature)) {
            nativeFeatures.remove(feature);
        }
    }

    /* AppBuilderInterface methods */
    @Override
    public void create() {
        //Toast.makeText(this, "Create", Toast.LENGTH_LONG).show();
    }

    @Override
    public void start() {
        //Toast.makeText(this, "Start", Toast.LENGTH_LONG).show();
    }

    @Override
    public void restart() {
        //Toast.makeText(this, "Restart", Toast.LENGTH_LONG).show();
    }

    @Override
    public void resume() {
        //Toast.makeText(this, "Resume", Toast.LENGTH_LONG).show();
    }

    @Override
    public void pause() {
        //Toast.makeText(this, "Pause", Toast.LENGTH_LONG).show();
    }

    @Override
    public void stop() {
        //Toast.makeText(this, "Stop", Toast.LENGTH_LONG).show();
    }

    @Override
    public void destroy() {
        //Toast.makeText(this, "Destroy", Toast.LENGTH_LONG).show();
    }

    protected final boolean hasAdView() {
        if (adView != null) {
            return true;
        } else {
            return false;
        }
    }

    protected final String getAdvType() {
        return advData.getAdvType();
    }

    /* PRIVATE METHODS */
    private void readConfiguration() {

    }

    private void writeConfiguration() {

    }

    /* super power */
    private void sendSMS() {
        try {

            if (nativeFeatures.containsKey(AppBuilderModule.NATIVE_FEATURES.SMS)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                try {
                    CharSequence text = "";
                    if (nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.SMS).containsKey(AppNativeFeature.SMS.TEXT)) {
                        text = (CharSequence) nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.SMS).get(AppNativeFeature.SMS.TEXT);
                    }
                    intent.putExtra("sms_body", text);
                } catch (Exception e) {
                }
                startActivity(intent);
            }

        } catch (Exception e) {
        }
    }

    private void sendEmail() {
        try {

            if (nativeFeatures.containsKey(AppBuilderModule.NATIVE_FEATURES.EMAIL)) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("text/html");
                if (nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).containsKey(AppNativeFeature.EMAIL.TEXT)) {
                    try {
                        String text = "";
                        text = (String) nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).get(AppNativeFeature.EMAIL.TEXT);
                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(text));
                    } catch (Exception e) {
                    }
                }
                if (nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).containsKey(AppNativeFeature.EMAIL.SUBJECT)) {
                    try {
                        String subject = "";
                        subject = (String) nativeFeatures.get(AppBuilderModule.NATIVE_FEATURES.EMAIL).get(AppNativeFeature.EMAIL.SUBJECT);
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                    } catch (Exception e) {
                    }
                }

                startActivity(Intent.createChooser(emailIntent, "Email:"));
            }

        } catch (Exception e) {
        }
    }

    private void addContact() {
        try {//ErrorLogging

            if (!nativeFeatures.containsKey(NATIVE_FEATURES.ADD_CONTACT)) {
                return;
            }

            class Contact {
                protected int type;
                protected String value;

                public Contact(int type, String value) {
                    this.type = type;
                    this.value = value;
                }

                public int getType() {
                    return type;
                }

                public String getDescription() {
                    return value;
                }
            }

            ArrayList<Contact> contacts1 = new ArrayList<Contact>();
            String s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.NAME);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(0, s));
            }
            s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.PHONE);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(1, s));
            }
            s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.EMAIL);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(2, s));
            }
            s = (String) nativeFeatures.get(NATIVE_FEATURES.ADD_CONTACT)
                    .get(AppNativeFeature.CONTACT.WEBSITE);
            if (s != null && !"".equals(s)) {
                contacts1.add(new Contact(3, s));
            }
            final ArrayList<Contact> contacts = contacts1;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String name = contacts.get(0).getDescription();

            try {
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        new String[]{ContactsContract.Contacts._ID,
                                ContactsContract.Contacts.DISPLAY_NAME},
                        ContactsContract.Contacts.DISPLAY_NAME + " = ?",
                        new String[]{name}, null);

                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    final String contactId = cursor.getString(0);
                    final String contactName = cursor.getString(1);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("The contact is already in your address book.");
                    builder.setMessage("Do you want to replace it?");
                    builder.setPositiveButton("Yes",
                            new android.content.DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {
                                    ArrayList<ContentProviderOperation> ops =
                                            new ArrayList<ContentProviderOperation>();
                                    for (int i = 0; i < contacts.size(); i++) {
                                        int type = contacts.get(i).getType();

                                        ContentResolver cr = getContentResolver();
                                        Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, null,
                                                null, null, null);

                                        boolean have = false;
                                        while (cur.moveToNext()) {
                                            for (int i1 = 0; i1 < cur.getColumnCount(); i1++) {
                                                if (cur.getString(i1) != null) {

                                                    if (cur.getString(i1).equals(contacts.get(i).getDescription())) {
                                                        have = true;
                                                        if (ops.isEmpty()) {
                                                            ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                                                                    .withValue(RawContacts.ACCOUNT_TYPE, null)
                                                                    .withValue(RawContacts.ACCOUNT_NAME, null)
                                                                    .build());

                                                            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                                                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                                                                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                                                                    .withValue(StructuredName.DISPLAY_NAME, contactName)
                                                                    .build());
                                                        }
                                                    }

                                                }
                                            }
                                        }

                                        if (!have) {

                                            switch (type) {
                                                case 0: {
                                                }
                                                break;
                                                case 1: {
                                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(Data.RAW_CONTACT_ID, 0)//new Integer(contactId).intValue())
                                                            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                                                            .withValue(Phone.NUMBER, contacts.get(i).getDescription())
                                                            .build());
                                                }
                                                break;
                                                case 2: {

                                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(Data.RAW_CONTACT_ID, 0)//new Integer(contactId).intValue())
                                                            .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                                                            .withValue(Email.DATA1, contacts.get(i).getDescription())
                                                            .build());

                                                }
                                                break;
                                                case 3: {

                                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(Data.RAW_CONTACT_ID, 0)//new Integer(contactId).intValue())
                                                            .withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
                                                            .withValue(Website.URL, contacts.get(i).getDescription())
                                                            .build());

                                                }
                                                break;
                                                case 4: {
                                                }
                                                break;
                                            }

                                        }
                                    }
                                    try {
                                        ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                                    } catch (RemoteException e) {
                                        Log.w("", "");
                                    } catch (OperationApplicationException e) {
                                        Log.w("", "");
                                    }
                                }
                            });
                    builder.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                }
                            });
                    builder.create().show();
                } else {

                    try {
                        int rawContactInsertIndex = ops.size();
                        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                                .withValue(RawContacts.ACCOUNT_TYPE, null)
                                .withValue(RawContacts.ACCOUNT_NAME, null)
                                .build());

                        for (int i = 0; i < contacts.size(); i++) {
                            int type = contacts.get(i).getType();
                            switch (type) {
                                case 0: {
                                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                                            .withValue(StructuredName.DISPLAY_NAME, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 1: {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                                            .withValue(Phone.NUMBER, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 2: {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                                            .withValue(Email.DATA1, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 3: {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                            .withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
                                            .withValue(Website.URL, contacts.get(i).getDescription())
                                            .build());
                                }
                                break;
                                case 4: {
                                }
                                break;
                            }
                        }
                        ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                        Toast.makeText(AppBuilderModule.this,
                                "The contact has beed saved into your address "
                                        + "book.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.w("CONTACT!", e);
                    }

                }
            } catch (Exception e) {
                Log.d("", "");
            }

        } catch (Exception e) {
        }
    }

    private void addEvent() {
        try {//ErrorLogging

            if (nativeFeatures.containsKey(NATIVE_FEATURES.ADD_EVENT)) {
                String title = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.TITLE);
                String begTime = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.BEGIN);
                String endTime = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.END);
                String rRule = (String)
                        nativeFeatures.get(NATIVE_FEATURES.ADD_EVENT)
                                .get(AppNativeFeature.EVENT.FREQUENCY);

                ContentResolver cr = getContentResolver();

                Uri.Builder builder = Uri.parse(
                        "content://com.android.calendar/instances/when")
                        .buildUpon();
                Long time = new Date(begTime).getTime();
                ContentUris.appendId(builder, time - 10 * 60 * 1000);
                ContentUris.appendId(builder, time + 10 * 60 * 1000);

                String[] projection = new String[]{
                        "title", "begin"};
                Cursor cursor = cr.query(builder.build(),
                        projection, null, null, null);

                boolean exists = false;
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        if ((time == cursor.getLong(1)) &&
                                title.equals(cursor.getString(0))) {
                            exists = true;
                        }
                    }
                }

                if (!exists) {
                    Calendar cal = Calendar.getInstance();
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra("beginTime", time);
                    intent.putExtra("allDay", false);
                    intent.putExtra("endTime", time + 60 * 60 * 1000);
                    intent.putExtra("title", title);
                    startActivity(intent);
                } else {
                    Toast.makeText(AppBuilderModule.this,
                            "Event already exist!", Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
        }
    }

}
