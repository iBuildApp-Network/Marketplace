package com.appbuilder.core.GPSNotification;

import android.graphics.*;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import java.util.ArrayList;

public class GPSLocationMapOverlay extends Overlay {
	private ArrayList<GeoPoint> route = new ArrayList<GeoPoint>();
	private int routeColor = Color.argb(127, 204, 51, 255);
	private ArrayList<GPSLocationMapItem> points = new ArrayList<GPSLocationMapItem>();
	
	private Paint innerPaint, borderPaint;
	
	public GPSLocationMapOverlay(){}
	
	public void setRoute(ArrayList<GeoPoint> route, int color){
		this.route = route;
		routeColor = color;
	}

	public void addPoint(GPSLocationMapItem point){
		points.add(point);
	}

	@Override
	public boolean onTap(GeoPoint tapPoint, final MapView mapView)  {
    	RectF hitTestRecr = new RectF();
		
    	Point point = new Point();
		for(int i = 0; i < points.size(); i ++){
    		mapView.getProjection().toPixels(points.get(i).getGeoPoint(), point);
    		hitTestRecr.set(-points.get(i).getIcon().getWidth(), -points.get(i).getIcon().getHeight(), points.get(i).getIcon().getWidth(), 0);
    		hitTestRecr.offset(point.x, point.y);
    		mapView.getProjection().toPixels(tapPoint, point);
    		if (hitTestRecr.contains(point.x, point.y)) {
    			if(points.get(i).getState() == GPSLocationMapItem.states.SHOW){
    				points.get(i).setState(GPSLocationMapItem.states.HIDE);
    			}else{
    				points.get(i).setState(GPSLocationMapItem.states.SHOW);
    			}
    			break;
    		}
		}
		mapView.invalidate();
		
		return true;
	}


	@Override
	public boolean draw	(Canvas canvas, MapView mapView, boolean shadow, long when){
		Projection projection = mapView.getProjection();

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		
		/* draw route */
		paint.setStrokeWidth(3);
		paint.setAlpha(120);
		paint.setColor(routeColor);
		for(int i = 0; i < route.size() - 1; i ++){
			Point point1 = new Point();
			projection.toPixels(route.get(i), point1);
			Point point2 = new Point();
			projection.toPixels(route.get(i + 1), point2);
			canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paint);
		}

		for(int i = 0; i < points.size(); i ++){
		
	        Point point = new Point();
	        projection.toPixels(points.get(i).getGeoPoint(), point);

	        if(points.get(i).getIconShadow() != null){
	        	canvas.drawBitmap(points.get(i).getIconShadow(), point.x, point.y - points.get(i).getIconShadow().getHeight(), null);
	        }
	        if(points.get(i).getIcon() != null){
				canvas.drawBitmap(points.get(i).getIcon(), point.x - points.get(i).getIcon().getWidth()/2, point.y - points.get(i).getIcon().getHeight(), null);
	        }
	        if((points.get(i).getTitle().length() > 0 || points.get(i).getDescription().length() > 0) && points.get(i).getState() == GPSLocationMapItem.states.SHOW){
				int offsetX = 10; int offsetY = 10;

				TextPaint textPaint = new TextPaint();
				textPaint.setARGB(255, 255, 255, 255);
				textPaint.setTextSize(14);
				textPaint.setAntiAlias(true);

				Rect rect = new Rect();
	        	
				int boxWidth = 0; int boxHeight = 0;
		
				ArrayList<String> titleLines = new ArrayList<String>();
				ArrayList<String> textLines = new ArrayList<String>();
				
				int titleLineHeight = 0;
				int titleLineWidth = 0;
				int subtitleLineHeight = 0;
				int subtitleLineWidth;

	        	String title = points.get(i).getTitle();
				int lineLength = title.length(); 
				if(lineLength > 0){
					int maxLines = 1;
					if(lineLength > 25){
						maxLines = 2;
	    			}
					titleLines = splitString(title, maxLines);

					textPaint.setTextSize(16);
					textPaint.getTextBounds(titleLines.get(0), 0, titleLines.get(0).length(), rect);

					if(maxLines > 1)
						boxWidth = rect.width();
					boxHeight = (rect.height() * titleLines.size()) + (2 * titleLines.size());
					
					titleLineWidth = rect.width();
					titleLineHeight = rect.height() + 2; 
				}
				
				
	        	String text = points.get(i).getDescription();
				lineLength = text.length(); 
				if(lineLength > 0){
					int maxLines = 1;
					if(boxWidth == 0){
						if(lineLength < 20){
							maxLines = 1;
						}else if(lineLength > 20 &&  lineLength < 61){
							maxLines = 2;
		    			}else if(lineLength > 60 &&  lineLength < 120){
							maxLines = 3;
		    			}else if(lineLength > 121 &&  lineLength < 160){
							maxLines = 4;
		    			}else if(lineLength > 161 &&  lineLength < 200){
							maxLines = 5;
		    			}else{
							maxLines = 6;
						}
					}else{
						textPaint.setTextSize(12);
						textPaint.getTextBounds(text, 0, text.length(), rect);
						int strWidth = rect.width();
						
						if(strWidth > boxWidth){
							maxLines = strWidth / boxWidth;
							if(strWidth % boxWidth != 0)
								maxLines ++;
						}
					}
					textLines = splitString(text, maxLines);
					
					String maxLine = ""; lineLength = 0; 
					for(String line: textLines){
						if(lineLength < line.length()){
							lineLength = line.length();
							maxLine = line;
						}
					}
						
					textPaint.setTextSize(12);
					textPaint.getTextBounds(maxLine, 0, maxLine.length(), rect);
						
					boxHeight = boxHeight + (rect.height() + 2) * textLines.size(); 
					subtitleLineHeight = rect.height() + 2; 
					subtitleLineWidth = rect.width();
					
					boxWidth = (titleLineWidth > subtitleLineWidth)? titleLineWidth: subtitleLineWidth;
				}				
				
		    	//  Setup the info window with the right size & location
				int infoWindowWidth = boxWidth + offsetX * 2 + 18;
				int infoWindowHeight = boxHeight + offsetY * 2 + 10;
				RectF infoWindowRect = new RectF(0, 0, infoWindowWidth, infoWindowHeight);				
				int infoWindowOffsetX = point.x - infoWindowWidth/2;
				int infoWindowOffsetY = point.y - infoWindowHeight - 2;
				if(points.get(i).getIcon() != null)
					infoWindowOffsetY -= points.get(i).getIcon().getHeight();
				infoWindowRect.offset(infoWindowOffsetX,infoWindowOffsetY);

				canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());
				canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());

				int x = infoWindowOffsetX + offsetX;
				int y = infoWindowOffsetY + offsetY;

				textPaint.setTextSize(16);
				for(int j = 0; j < titleLines.size(); j ++){
					y = y + titleLineHeight;
					canvas.drawText(titleLines.get(j), x, y, textPaint);
				}
				if(titleLines.size() > 0){
					y = y + 5;
				}
				
				/* draw subtitle */
				textPaint.setTextSize(14);
				for(int j = 0; j < textLines.size(); j ++){
					y = y + subtitleLineHeight;
					canvas.drawText(textLines.get(j), x, y + 1, textPaint);
				}
	        }
		}
		
		return super.draw(canvas, mapView, shadow, when);
	}
   	
   	public Paint getInnerPaint() {
		if ( innerPaint == null) {
			innerPaint = new Paint();
			innerPaint.setARGB(196, 75, 75, 75); //gray
			innerPaint.setAntiAlias(true);
		}
		return innerPaint;
	}

	public Paint getBorderPaint() {
		if ( borderPaint == null) {
			borderPaint = new Paint();
			borderPaint.setARGB(225, 255, 255, 255);
			borderPaint.setAntiAlias(true);
			borderPaint.setStyle(Style.STROKE);
			borderPaint.setStrokeWidth(2);
		}
		return borderPaint;
	}

	public TextPaint getTextPaint(float fontSize) {
		TextPaint textPaint = new TextPaint();
		textPaint.setTextSize(fontSize);
		textPaint.setARGB(255, 255, 255, 255);
		textPaint.setAntiAlias(true);
		return textPaint;
	}
	
	private ArrayList<String> splitString(String s, int maxLines){
		ArrayList<String> lines = new ArrayList<String>();

		if(maxLines == 1){
			lines.add(s);
		}else{
			int lineLength = (int) s.length() / maxLines;
			try{
				for(int i = 0; i < maxLines; i ++){
					String s1 = s.substring(0, lineLength);
					String s2 = s.substring(lineLength);
					int lastWhitespace = s1.lastIndexOf(" ");
					int firstWhitespace = s2.indexOf(" ");

					if(firstWhitespace == -1)
						firstWhitespace = s2.length();
					
					if(s1.length() - lastWhitespace < firstWhitespace){
						lines.add(s1.substring(0, lastWhitespace));
						s = s1.substring(lastWhitespace + 1) + s2;
					}else{
						lines.add(s1 + s2.substring(0, firstWhitespace));
						s = (firstWhitespace == s2.length())? "": s2.substring(firstWhitespace + 1);
					}
					if(s.length() < lineLength){
						lines.add(s);
						s = "";
						break;
					}
				}
				if(s.length() > 0)
					lines.add(s);
			}catch (Exception e){}
		}
		
		return lines;
	}
	
}
