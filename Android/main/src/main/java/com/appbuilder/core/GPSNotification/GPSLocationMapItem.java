package com.appbuilder.core.GPSNotification;

import com.google.android.maps.GeoPoint;

import android.graphics.Bitmap;

public class GPSLocationMapItem {

	static public enum states{SHOW, HIDE};
	
	private GeoPoint point = null;
	private String title = "";
	private String description = "";

	private Bitmap icon = null;
	private Bitmap shadow = null;
	
	private states state = states.SHOW;
	
	public GPSLocationMapItem(){
	}

	public void setGeoPoint(GeoPoint value){
		point = value;
	}
	public GeoPoint getGeoPoint(){
		return point;
	}

	public void setTitle(String value){
		title = value;
	}
	public String getTitle(){
		return title;
	}

	public void setDescription(String value){
		description = value;
	}
	public String getDescription(){
		return description;
	}
	
	public void setIcon(Bitmap value){
		icon = value;
	}
	public Bitmap getIcon(){
		return icon;
	}
	
	public void setIconShadow(Bitmap value){
		shadow = value;
	}
	public Bitmap getIconShadow(){
		return shadow;
	}
	
	public void setState(states value){
		state = value;
	}
	public states getState(){
		return state;
	}
}
