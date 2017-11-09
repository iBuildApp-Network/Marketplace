package com.appbuilder.core;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.appbuilder.core.xmlconfiguration.AppConfigure;
import com.appbuilder.sdk.android.AppAdvData;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PluginManager {

    private static final String ERR_NO_ERROR = "ERR_NO_ERROR";
    private static final String ERR_FILE_NOT_EXISTS = "ERR_FILE_NOT_EXISTS";
    private static final String ERR_INIT_CLASS_LOADER = "ERR_INIT_CLASS_LOADER";
    private static final String ERR_SECURITY_IO = "ERR_SECURITY_IO";
    private static final String ERR_LOAD_BASE_CLASS = "ERR_LOAD_BASE_CLASS";
    private static final String ERR_INIT_BASE_CLASS = "ERR_INIT_BASE_CLASS";
    private static final String ERR_RESOLVE_METHOD = "ERR_RESOLVE_METHOD";
    private static final String ERR_PLUGIN_METHOD = "ERR_PLUGIN_METHOD";
    private static final String ERR_PLUGIN_EMBEDDED = "ERR_PLUGIN_EMBEDDED";
    private static final String TAG = "com.ibuildapp.PluginManager";
    private final int START_MODULE = 10001;

    private boolean firstStart = false;

    static private PluginManager mPluginManager = null;
    private Activity mCaller;
    private Activity mHolder;
    private String mErrString = ERR_NO_ERROR;

    private PluginManager() {

    }

    public synchronized String getErrorString() {
        return mErrString;
    }

    public static synchronized PluginManager getManager() {
        if (mPluginManager == null) {
            mPluginManager = new PluginManager();
        }
        return mPluginManager;
    }

    public static boolean classExists(String className) {
        boolean resolved = true;
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            resolved = false;
        }
        return resolved;
    }

    public void installPlugin(String pluginPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(pluginPath)), "application/vnd.android.package-archive");
        mCaller.startActivityForResult(intent, START_MODULE);
    }

    private void obtainPluginFromUrl(final String pluginPath,
                                     final Widget widget,
                                     final ArrayList<Widget> getmWidgets,
                                     final AppConfigure appConfig) throws RuntimeException {
        final DownloadHelper dh = new DownloadHelper(mCaller, widget.getPluginUrl(), pluginPath, null, false, true);
        dh.setStartedRunnable(new Runnable() {
            public void run() {
                Log.d("Download plugin", "Start");
            }
        });

        dh.setFailedRunnable(new Runnable() {
            public void run() {
                Log.d("Download plugin", "Failed: " + dh.getErrorString());
                throw new RuntimeException();
            }
        });

        dh.setSuccessRunnable(new Runnable() {
            public void run() {
                Log.d("Download plugin", "Success");
                PluginManager.getManager().loadLocalPluginFile(pluginPath, widget, getmWidgets, appConfig);
            }
        });

        dh.start();
    }

    public synchronized void setContext(Activity caller, Activity holder) {
        mCaller = caller;
        mHolder = holder;
    }

    /**
     * function which responsible for preparing module for start
     *
     * @param caller      - parent activity which
     * @param holder      - ??? don't use
     * @param widget      - describer of external module
     * @param appAdv      - object that holds data for ad showing
     * @param forceUpdate
     */
    public synchronized void loadPlugin(Activity caller,
                                        Activity holder,
                                        Widget widget,
                                        AppAdvData appAdv,
                                        boolean forceUpdate,
                                        ArrayList<Widget> getmWidgets,
                                        AppConfigure appConfig) throws RuntimeException {

        mCaller = caller;
        mHolder = holder;

        //String pluginPath = Utils.PluginFolder(caller) + widget.getPluginName() + ".apk";
        //File pluginFile = new File(pluginPath);
        //boolean needUpdate = false;
        /*if (forceUpdate) {
            needUpdate = true;
        } else {
///////////////////////		
            String thisPackage = mCaller.getPackageName();
            String pluginActivityClassName = thisPackage + ".embedded." + widget.getPluginName() + "." + widget.getPluginName();
//String pluginActivityClassName = "com.ibuildapp.romanblack.CalculatorPlugin.CalculatorPlugin";
            if (PluginManager.classExists(pluginActivityClassName)) {
                try {
                    loadEmbeddedPlugin(pluginActivityClassName, widget, appAdv, getmWidgets, appConfig);
                } catch (RuntimeException e) {
                    mErrString = ERR_PLUGIN_EMBEDDED;
                    throw new RuntimeException();
                }
                return;
            } else {*/
        try {
            loadEmbeddedPlugin(/*pluginActivityClassName*/null, widget, appAdv, getmWidgets, appConfig);
        } catch (RuntimeException e) {
            mErrString = ERR_PLUGIN_EMBEDDED;
            throw new RuntimeException();
        }
///////////////////////			
                /*if (!pluginFile.exists()) {
                    needUpdate = true;
                } else if (widget.getPluginHash() != null) {
                    String localHash = Hash.MD5(pluginFile);
                    if (!localHash.equals(widget.getPluginHash())) {
                        needUpdate = true;
                    }
                }
            }*/
        /*}*/
        //installPlugin(pluginPath);
        /*if (needUpdate) {
            try {
                obtainPluginFromUrl(pluginPath, widget, getmWidgets, appConfig);
            } catch (RuntimeException e) {
                throw new RuntimeException();
            }
        } else {
            try {
                loadLocalPluginFile(pluginPath, widget, getmWidgets, appConfig);
            } catch (Exception e) {
                if (e instanceof SecurityException) {
                    mErrString = ERR_SECURITY_IO;
                }
                throw new RuntimeException();
            }
        }*/
    }

    /**
     * direct start of new Activity through intent
     */
    public void loadEmbeddedPlugin(String pluginActivityClassName,
                                   Widget widget,
                                   AppAdvData appAdv,
                                   ArrayList<Widget> getmWidgets,
                                   AppConfigure appConfig) throws RuntimeException {
        Intent it = new Intent();
        try {
            // specifying parent activity and class to start
            // mCaller - parent activity
            it.setClass(mCaller, Class.forName(widget.getPluginPackage() + "." + widget.getPluginName()));
        } catch (Exception e) {
            LogError(e);
        }

        ArrayList<Widget> widgetsList = new ArrayList<Widget>();
        for (Widget w : getmWidgets) {
            Widget www = new Widget(w);
            www.setPluginXmlData("");
            widgetsList.add(www);
        }

        // Костыль для конских xml
        if (widget.getPluginXmlData().length() > 50000) {
            String filename = saveXmlToFile(widget.getPluginXmlData());
            it.putExtra("WidgetFile", filename);
            widget = new Widget(widget);
            widget.setPluginXmlData("");
        }

        it.putExtra("Widget", widget);
        it.putExtra("Widgets", widgetsList);
        it.putExtra("Advertisement", appAdv);
        it.putExtra("showSideBar", appConfig.isShowSidebar());
        it.putExtra("navBarDesign", appConfig.getNavBarDesign());
        it.putExtra("tabBarDesign", appConfig.getTabBarDesign());
        it.putExtra("bottomBarDesign", appConfig.getBottomBarDesign());
        it.putExtra("appid", appConfig.getmAppId());
        it.putExtra("firstStart", firstStart);
        it.putExtra("flurry_id", AppBuilder.FLURYY_ID);
        try {
            //StatisticsCollector.newAction(widget.getPluginId());
            mHolder.startActivityForResult(it, START_MODULE);

            // Google Analytics
            try {
                String pluginName = widget.getmPluginType();
                if (TextUtils.isEmpty(pluginName)) {
                    pluginName = widget.getPluginName();
                }
                com.appbuilder.sdk.android.Statics.analiticsHandler.sendIbuildAppEvent(pluginName, "Start Module");

                String userPluginTitle = widget.getTitle();
                if (TextUtils.isEmpty(userPluginTitle)) {
                    userPluginTitle = widget.getPluginName();
                }
                com.appbuilder.sdk.android.Statics.analiticsHandler.sendUserEvent("Start Module", userPluginTitle);

            } catch (Exception ex) {
                LogError(ex);
            }
        } catch (ActivityNotFoundException e) {
            Log.d("loadEmbeddedPlugin", "activity exception");
            throw new RuntimeException();
        } catch (Exception e) {
            LogError(e);
            throw new RuntimeException();
        }
    }

    private String saveXmlToFile(String pluginXmlData) {
        String xmlPath = Environment.getExternalStorageDirectory() + "/AppBuilder/"
                + com.appbuilder.sdk.android.Statics.cachePath + "/cache";
        File dirs = new File(xmlPath);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }
        xmlPath = xmlPath + "/galleryCacheXmlData.xml";

        try {
            File xmlFile = new File(xmlPath);
            if (!xmlFile.createNewFile()) {
                xmlFile.delete();
                xmlFile.createNewFile();
            }
            Writer writer = new FileWriter(xmlFile);
            writer.write(pluginXmlData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return xmlPath;
    }

    private void LogError(Exception ex) {
        Log.e(TAG, "", ex);
    }

    private void LogDebug(String message) {
        Log.d(TAG, message);
    }

    public void loadLocalPluginFile(String path, Widget widget, ArrayList<Widget> getmWidgets,
                                    AppConfigure appConfig)
            throws RuntimeException, SecurityException {
        if (mCaller == null) {
            throw new RuntimeException();
        }

        if (!(new File(path)).exists()) {
            Log.d("loadLocalPluginFile", "File not exists");
            mErrString = ERR_FILE_NOT_EXISTS;
            throw new RuntimeException();
        }

        DexClassLoader classLoader = new DexClassLoader(
                path, Utils.PluginFolder(mCaller), null, getClass().getClassLoader());
        if (classLoader == null) {
            Log.d("loadLocalPluginFile", "DexClassLoader == null");
            mErrString = ERR_INIT_CLASS_LOADER;
            throw new RuntimeException();
        }

        String pluginCoreClassName = widget.getPluginPackage() + ".PluginCore";
        Class<?> pluginClass;
        Log.d(pluginCoreClassName, "loadLocalPluginFile");
        try {
            pluginClass = classLoader.loadClass(pluginCoreClassName);
            Log.d("loadLocalPluginFile", "Loaded ?");
        } catch (Exception e) {
            mErrString = ERR_LOAD_BASE_CLASS;
            throw new RuntimeException();
        }
        Log.d("loadLocalPluginFile", "Plugin core class loaded");
        Object pluginCore = null;
        try {
            pluginCore = pluginClass.newInstance();
        } catch (IllegalAccessException e) {
            mErrString = ERR_INIT_BASE_CLASS;
            throw new RuntimeException();
        } catch (InstantiationException e) {
            mErrString = ERR_INIT_BASE_CLASS;
            throw new RuntimeException();
        } catch (SecurityException e) {
        }

        Log.d("loadLocalPluginFile", "Plugin object created");

        Method methodGetPluginMainActivityName;
        Method methodSetApplicationPackage;
        Method methodSetPluginLoader;

        try {
            assert pluginCore != null;
            methodGetPluginMainActivityName = pluginCore.getClass().getMethod("getPluginMainActivityName");
            methodSetApplicationPackage = pluginCore.getClass().getMethod("SetApplicationPackage", String.class);
            methodSetPluginLoader = pluginCore.getClass().getMethod("SetPluginLoader", /*PluginLoader.class */  Object.class);
        } catch (Exception e) {
            Log.d("loadLocalPluginFile", "method exception 1");
            mErrString = ERR_RESOLVE_METHOD;
            throw new RuntimeException();
        }
        if (methodGetPluginMainActivityName == null || methodSetApplicationPackage == null
                || methodSetPluginLoader == null) {
            Log.d("loadLocalPluginFile", "method exception 2");
            mErrString = ERR_RESOLVE_METHOD;
            throw new RuntimeException();
        }
        Log.d("loadLocalPluginFile", "Methods resolved");

        String pluginActivityClassName;
        try {
            pluginActivityClassName = (String) methodGetPluginMainActivityName.invoke(pluginCore);
        } catch (Exception e) {
            Log.d("loadLocalPluginFile", "method exception 3");
            mErrString = ERR_PLUGIN_METHOD;
            throw new RuntimeException();
        }
        Log.d(pluginActivityClassName, "Class");

        try {
            ApplicationInfo info = mCaller.getApplicationInfo();
            assert info != null;
            methodSetApplicationPackage.invoke(pluginCore, info.packageName);
        } catch (Exception e) {
            mErrString = ERR_PLUGIN_METHOD;
            throw new RuntimeException();
        }

        Intent it = new Intent();
        it.setClassName(widget.getPluginPackage(), pluginActivityClassName);
        it.setAction(Intent.ACTION_MAIN);
        it.setExtrasClassLoader(classLoader);
        it.addCategory(Intent.CATEGORY_LAUNCHER);
        it.putExtra("Widget", widget);
        it.putExtra("Widgets", getmWidgets);
        it.putExtra("WidgetData", widget.getPluginXmlData());
        it.putExtra("showSideBar", appConfig.isShowSidebar());
        try {
            mHolder.startActivityForResult(it, START_MODULE);
        } catch (ActivityNotFoundException e) {
            Log.d("loadLocalPluginFile", "activity exception 3");
            installPlugin(path);
        }
    }

    /**
     * @param firstStart the firstStart to set
     */
    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }

}
