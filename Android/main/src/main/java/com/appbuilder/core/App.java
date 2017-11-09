package com.appbuilder.core;

import android.app.Application;
import android.util.Log;

/**
 * Created by Artem on 06.02.14.
 */
public class App extends Application {
    @Override
    public void onTerminate() {
        Log.d("XXX", "Terminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("XXX", "onLowMemory");
    }
}
