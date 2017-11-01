package com.appbuilder.core.GPSNotification;

import java.io.Serializable;

public class GPSItem implements Serializable {
	private static final long serialVersionUID = 1L;
	static public enum Show {SINGLE, PLURAL};
	
	private double longitude = 0;
	private double latitude = 0;
	private int radius = 0;
	private int distance = 0;
	private String title = "";
	private String description = "";
	private Show showType = Show.SINGLE;
	
	public GPSItem(){
	}

	public void setLongitude(double value){
		longitude = value;
	}
	public double getLongitude(){
		return longitude;
	}

	public void setLatitude(double value){
		latitude = value;
	}
	public double getLatitude(){
		return latitude;
	}

	public void setRadius(int value){
		radius = value;
	}
	public int getRadius(){
		return radius;
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
	
	public void setCountOfView(Show show){
		showType = show;
	}
	public Show getCountOfView(){
		return showType;
	}
	
	public void setDistance(int value){
		distance = value;
	}
	public int getDistance(){
		return distance;
	}
	
	public boolean isInRadius(){
		return (distance < radius)? true: false;
	}
}
