package com.appbuilder.core.GPSNotification;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.appbuilder.core.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GPSLocationMap extends MapActivity implements LocationListener {

    private LocationManager locationManager = null;
    private double srcLatitude = -999;
    private double srcLongitude = -999;
    private String srcTitle = "";
    private String srcDescription = "";
    private double dstLatitude = -999;
    private double dstLongitude = -999;
    private String dstTitle = "";
    private String dstDescription = "";

    final private int LOCATION_LISTENER_START = 0;
    final private int LOCATION_LISTENER_STOP = 1;
    final private int LOCATION_LISTENER_ERROR = 2;
    final private int LOCATION_MAP_SHOW = 3;
    final private int LOADING_ABORTED = 4;
    final private int SHOW_PROGRESS = 5;
    final private int HIDE_PROGRESS = 6;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case LOADING_ABORTED: {
                    closeActivity();
                }
                break;
                case LOCATION_LISTENER_START: {
                    startLocationListener();
                }
                break;
                case LOCATION_LISTENER_STOP: {
                    stopLocationListener();
                }
                break;
                case LOCATION_LISTENER_ERROR: {
                    //Toast.makeText(this, "Cannot initialize app, please check internet connection. Turn on wi-fi or mobile internet.", Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 5000);
                }
                break;
                case LOCATION_MAP_SHOW: {
                    showLocationMap();
                }
                break;
                case SHOW_PROGRESS: {
                    showProgress();
                }
                break;
                case HIDE_PROGRESS: {
                    hideProgress();
                }
                break;
            }
        }
    };

    private ProgressDialog progressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_map_main);

        progressDialog = ProgressDialog.show(this, null, getString(R.string.load), true);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                handler.sendEmptyMessage(LOADING_ABORTED);
            }
        });

        GPSItem gpsItem = null;
        try {
            gpsItem = (GPSItem) getIntent().getSerializableExtra("gpsNotificationData");
        } catch (Exception e) {
        }

        if (gpsItem != null) {
            dstLatitude = gpsItem.getLatitude();
            dstLongitude = gpsItem.getLongitude();
            dstTitle = gpsItem.getTitle();
            dstDescription = gpsItem.getDescription();
        }

        srcLatitude = getIntent().getDoubleExtra("srcLatitude", 0);
        srcLongitude = getIntent().getDoubleExtra("srcLongitude", 0);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        handler.sendEmptyMessage(LOCATION_MAP_SHOW);
        handler.sendEmptyMessageDelayed(LOCATION_LISTENER_STOP, 3 * 60 * 1000);
        progressDialog.dismiss();
    }

    @Override
    public void onPause() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        super.onDestroy();
    }

    /* LocationListener Methods */
    @Override
    public void onLocationChanged(Location location) {
        StringBuilder address = new StringBuilder();
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(((int) (location.getLatitude() * 1e6)) / 1e6, ((int) (location.getLongitude() * 1e6)) / 1e6, 1);
            if (addresses.size() > 0) {
                for (int index = 0; index < addresses.get(0).getMaxAddressLineIndex(); index++) {
                    address.append(" ");
                    address.append(addresses.get(0).getAddressLine(index));
                    address.append(" ");
                }
            }
        } catch (Exception e) {
        }

        srcLatitude = location.getLatitude();
        srcLongitude = location.getLongitude();
        srcDescription = address.toString();

        handler.sendEmptyMessage(LOCATION_LISTENER_STOP);
        handler.sendEmptyMessage(LOCATION_MAP_SHOW);
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /* PRIVATE METHODS */
    private void startLocationListener() {
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 60 * 1000, 0, this);
            }
        } else {
            handler.sendEmptyMessage(LOCATION_LISTENER_ERROR);
        }
    }

    private void stopLocationListener() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void showLocationMap() {
        //MapView mapView = (MapView) findViewById(R.id.gps_map_view);
        
        /*
        srcLatitude = 55.758032;
        srcLongitude = 37.618332;
        dstLatitude = 55.789701;
        dstLongitude = 37.372513;
        */

        WebView mapView = (WebView) findViewById(R.id.gps_map_view);

        mapView.getSettings().setJavaScriptEnabled(true);
        mapView.getSettings().setGeolocationEnabled(true);
        //mapView.getSettings().setPluginsEnabled(true);
        mapView.getSettings().setAllowFileAccess(true);
        mapView.getSettings().setAppCacheEnabled(true);
        mapView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mapView.getSettings().setBuiltInZoomControls(true);
        mapView.clearHistory();
        mapView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        mapView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //super.onPageStarted(view, url, favicon);

                if (progressDialog != null) {
                    if (!progressDialog.isShowing()) {
                        handler.sendEmptyMessage(SHOW_PROGRESS);
                    }
                } else {
                    handler.sendEmptyMessage(SHOW_PROGRESS);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                handler.sendEmptyMessage(HIDE_PROGRESS);
                //super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("maps.google")) {
                    return false;
                } else {
                    return true;
                }
            }

        });

        String routeURL;

        StringBuilder sb = new StringBuilder();
        sb.append("http://maps.google.com/maps?saddr=");
        sb.append(srcLatitude); ///1E6);
        sb.append(",");
        sb.append(srcLongitude);///1E6);
        sb.append("&daddr=");
        sb.append(dstLatitude);///1E6);
        sb.append(",");
        sb.append(dstLongitude);///1E6);
        sb.append("&ll=");
        sb.append((srcLatitude/*/1E6*/ + dstLatitude/*/1E6*/) / 2);
        sb.append(",");
        sb.append((srcLongitude/*/1E6*/ + dstLongitude/*/1E6*/) / 2);
        sb.append("&z=");

        int z;
        int gr;
        if (Math.abs(srcLatitude - dstLatitude) >
                Math.abs(srcLongitude - dstLongitude)) {
            gr = Math.abs((int) (srcLatitude - dstLatitude));
        } else {
            gr = Math.abs((int) (srcLongitude - dstLongitude));
        }
        if (gr > (120 * 1E6)) {
            z = 1;
        } else if (gr > (60 * 1E6)) {
            z = 2;
        } else if (gr > (30 * 1E6)) {
            z = 3;
        } else if (gr > (15 * 1E6)) {
            z = 4;
        } else if (gr > (8 * 1E6)) {
            z = 5;
        } else if (gr > (4 * 1E6)) {
            z = 6;
        } else if (gr > (2 * 1E6)) {
            z = 7;
        } else if (gr > (1 * 1E6)) {
            z = 8;
        } else if (gr > (0.5 * 1E6)) {
            z = 9;
        } else {
            z = 10;
        }

        sb.append(z);

        routeURL = sb.toString();

        mapView.loadUrl(routeURL);
        
        /*if(srcLatitude != -999 && srcLongitude != -999){
            GeoPoint srcGeoPoint = new GeoPoint((int)(srcLatitude * 1E6), (int)(srcLongitude * 1E6));
            GeoPoint dstGeoPoint = new GeoPoint((int)(dstLatitude * 1E6), (int)(dstLongitude * 1E6));
            int color = Color.argb(127, 204, 51, 255);
            DrawPath(srcGeoPoint, dstGeoPoint, color, mapView);
        }else{
            GeoPoint dstGeoPoint = new GeoPoint((int)(dstLatitude * 1E6), (int)(dstLongitude * 1E6));
            int color = Color.argb(127, 204, 51, 255);
            DrawPath(null, dstGeoPoint, color, mapView);
        }*/
    }

    private void DrawPath(GeoPoint srcGeoPoint, GeoPoint dstGeoPoint, int color, MapView mapView) {

        Bitmap pinIconRed = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_red);
        Bitmap pinIconBlue = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_blue);
        Bitmap shadowIcon = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_shadow);

        GPSLocationMapOverlay mapOverlay = new GPSLocationMapOverlay();
        ArrayList<GeoPoint> route = new ArrayList<GeoPoint>();

        if (srcGeoPoint != null) {
            StringBuilder urlString = new StringBuilder();
            urlString.append("http://maps.google.com/maps?f=d&hl=en");
            urlString.append("&saddr=");//from
            urlString.append(Double.toString((double) srcGeoPoint.getLatitudeE6() / 1.0E6));
            urlString.append(",");
            urlString.append(Double.toString((double) srcGeoPoint.getLongitudeE6() / 1.0E6));
            urlString.append("&daddr=");//to
            urlString.append(Double.toString((double) dstGeoPoint.getLatitudeE6() / 1.0E6));
            urlString.append(",");
            urlString.append(Double.toString((double) dstGeoPoint.getLongitudeE6() / 1.0E6));
            urlString.append("&ie=UTF8&0&om=0&output=kml");

            try {
                URL url = new URL(urlString.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.connect();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(urlConnection.getInputStream());

                if (doc.getElementsByTagName("GeometryCollection").getLength() > 0) {
                    String path = doc.getElementsByTagName("GeometryCollection").item(0).getFirstChild().getFirstChild().getFirstChild().getNodeValue();

                    String[] pairs = path.split(" ");
                    String[] coords = pairs[0].split(","); // lngLat[0]=longitude lngLat[1]=latitude lngLat[2]=height

                    route.add(new GeoPoint((int) (Double.parseDouble(coords[1]) * 1E6), (int) (Double.parseDouble(coords[0]) * 1E6)));
                    for (int i = 1; i < pairs.length; i++) {
                        coords = pairs[i].split(","); // watch out! For GeoPoint, first:latitude, second:longitude
                        route.add(new GeoPoint((int) (Double.parseDouble(coords[1]) * 1E6), (int) (Double.parseDouble(coords[0]) * 1E6)));
                    }
                }
            } catch (Exception e) {
            }
        }

        if (route.size() > 1)
            mapOverlay.setRoute(route, Color.argb(127, 204, 51, 255));

        if (srcGeoPoint != null) {
            GPSLocationMapItem srcMapItem = new GPSLocationMapItem();
            srcMapItem.setGeoPoint(srcGeoPoint);
            srcMapItem.setTitle(srcTitle);
            srcMapItem.setDescription(srcDescription);
            srcMapItem.setIcon(pinIconBlue);
            srcMapItem.setIconShadow(shadowIcon);
            mapOverlay.addPoint(srcMapItem);
        }

        GPSLocationMapItem dstMapItem = new GPSLocationMapItem();
        dstMapItem.setGeoPoint(dstGeoPoint);
        dstMapItem.setTitle(dstTitle);
        dstMapItem.setDescription(dstDescription);
        dstMapItem.setIcon(pinIconRed);
        dstMapItem.setIconShadow(shadowIcon);
        mapOverlay.addPoint(dstMapItem);

        mapView.getOverlays().clear();
        mapView.getOverlays().add(mapOverlay);

        if (srcGeoPoint == null) {
            mapView.getController().setZoom(12);
            mapView.getController().animateTo(dstGeoPoint);
        } else {
            mapView.getController().zoomToSpan((srcGeoPoint.getLatitudeE6() - dstGeoPoint.getLatitudeE6()), (srcGeoPoint.getLongitudeE6() - dstGeoPoint.getLongitudeE6()));
            //GeoPoint middleGeoPoint = new GeoPoint((srcGeoPoint.getLatitudeE6() + dstGeoPoint.getLatitudeE6())/2, (srcGeoPoint.getLongitudeE6() + dstGeoPoint.getLongitudeE6())/2);
            mapView.getController().animateTo(srcGeoPoint);
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void showProgress() {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(this, null, getString(R.string.load), true);
            }
        }
        /*progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new OnCancelListener(){
            @Override
            public void  onCancel(DialogInterface dialog){
                handler.sendEmptyMessage(LOADING_ABORTED);
            } 
        });*/
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void closeActivity() {
        if (progressDialog != null)
            progressDialog.dismiss();
        finish();
    }
}
