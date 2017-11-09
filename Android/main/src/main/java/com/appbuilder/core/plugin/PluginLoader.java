package com.appbuilder.core.plugin;
import android.app.Activity;

public interface PluginLoader {
	
	public int LoadPluginFromActivity(Activity activity, String pluginName, String pluginPackage, String pluginURL, String pluginHash);
	
}
