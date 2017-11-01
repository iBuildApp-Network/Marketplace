package com.appbuilder.core.plugin;
import com.appbuilder.core.plugin.PluginLoader;

public interface PluginInterface {

	public String getPluginMainActivityName();   
	public void SetApplicationPackage(String appPackage);
	public void SetPluginLoader(PluginLoader pluginLoader);

}
