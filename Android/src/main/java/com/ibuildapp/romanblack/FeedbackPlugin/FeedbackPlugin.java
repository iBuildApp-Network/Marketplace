package com.ibuildapp.romanblack.FeedbackPlugin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
//import com.appbuilder.core.R;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Widget;
import java.util.ArrayList;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Roman Black
 */
public class FeedbackPlugin extends AppBuilderModuleMain{
    
    private static final int INITIALIZATION_FAILED = 0;
    private static final int SEND = 1;
    private static final int SHOW_PROGRESS_DIALOG = 2;
    private static final int HIDE_PROGRESS_DIALOG = 3;
    private static final int CLOSE_ALL = 4;
    private static final int SHOW_DIALOG = 5;
    
    private String appid = null;
    private String endpoint = null;
    
    private EditText sendEditText = null;
    private ProgressDialog progressDialog = null;

    private Widget widget = null;
    
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case INITIALIZATION_FAILED:{
                    Toast.makeText(FeedbackPlugin.this, getResources().getIdentifier("alert_cannot_init", "string", getPackageName()),//R.string.alert_cannot_init,
                            Toast.LENGTH_LONG).show();
                    finish();
                }break;
                case SEND:{
                    send();
                }break;
                case SHOW_PROGRESS_DIALOG:{
                    if(progressDialog != null){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    
                    progressDialog = ProgressDialog.show(FeedbackPlugin.this, "", 
                            getResources().getString(getResources().getIdentifier("load", "string", getPackageName())));//R.string.load));
                }break;
                case HIDE_PROGRESS_DIALOG:{
                    if(progressDialog != null){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }break;
                case CLOSE_ALL:{
                    finish();
                }break;
                case SHOW_DIALOG:{
                    showDialog();
                }break;
            }
            
            super.handleMessage(msg); //To change body of generated methods, choose Tools | Templates.
        }
        
    };
    
    @Override
    public void create(){
        setContentView(/*R.layout.feedback_main*/getResources().getIdentifier("feedback_main", "layout", getPackageName()));
        
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
        }else{
            Toast.makeText(this, getResources().getIdentifier("need_internet_connection", "string", getPackageName())//R.string.need_internet_connection
                    , Toast.LENGTH_LONG).show();
            finish();
        }
        
        Intent currentIntent = getIntent();
        Bundle store = currentIntent.getExtras();
        widget = (Widget) store.getSerializable("Widget");
        if (widget == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }
        if (widget.getTitle().length() > 0) {
            setTitle(widget.getTitle());
        }
        
        try {
            if (widget.getPluginXmlData().length() == 0) {
                if (currentIntent.getStringExtra("WidgetFile").length() == 0) {
                    handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 3000);
                    return;
                }
            }
        } catch (Exception e) {
            handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 3000);
            return;
        }
        
        if (widget.getTitle() != null && widget.getTitle().length() != 0){
            setTopBarTitle(widget.getTitle());
        } else{
            setTopBarTitle("");
        }
        
        boolean showSideBar = ((Boolean) getIntent().getExtras().getSerializable("showSideBar")).booleanValue();
        if (!showSideBar) {
            setTopBarLeftButtonText(getResources().getString(/*R.string.common_home_upper*/
                    getResources().getIdentifier("common_home_upper", "string", getPackageName())), true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        
        setTopBarRightButtonText(getResources().getString(/*R.string.common_send_upper*/
                getResources().getIdentifier("common_send_upper", "string", getPackageName())), false, new View.OnClickListener() {

            public void onClick(View arg0) {
                handler.sendEmptyMessage(SEND);
            }
        });
        
        sendEditText = (EditText)findViewById(/*R.id.feedback_editfield*/
                getResources().getIdentifier("feedback_editfield", "id", getPackageName()));
        
        appid = currentIntent.getStringExtra("appid");
        
        if(appid == null){
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }
        
        //appid = "1524";//"RReePPLLaaCCee";
        
        if(appid.length() == 0){
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }
        
        endpoint = new Parser().parse(widget.getPluginXmlData());
        
        if(endpoint == null){
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }
        
        if(endpoint.length() == 0){
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }
    }
    
    private void send(){
        if(sendEditText.getText().length() == 0){
            Toast.makeText(this, getResources().getIdentifier("feedback_type_message", "string", getPackageName()), Toast.LENGTH_LONG).show();
            return;
        }
        
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
        }else{
            Toast.makeText(this, /*R.string.need_internet_connection*/
                    getResources().getIdentifier("need_internet_connection", "string", getPackageName()), Toast.LENGTH_LONG).show();
            return;
        }
        
        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
        
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(sendEditText.getWindowToken(), 0);
        
        new Thread(){

            @Override
            public void run() {
                HttpParams params = new BasicHttpParams();
                params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, 
                        HttpVersion.HTTP_1_1);
                HttpClient httpClient = new DefaultHttpClient(params);

                try{
                    StringBuilder sb = new StringBuilder();
                    sb.append(endpoint);
                    sb.append("/");

                    HttpPost httpPost = new HttpPost(sb.toString());
                    
                    ArrayList<NameValuePair> httpParams = new ArrayList<NameValuePair>();
                    
                    NameValuePair param1 = new BasicNameValuePair("app_id", appid);
                    httpParams.add(param1);
                    
                    NameValuePair param2 = new BasicNameValuePair("message", sendEditText.getText().toString());
                    httpParams.add(param2);
                    
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(httpParams, "utf-8");

                    httpPost.setEntity(entity);

                    String resp = httpClient.execute(httpPost, new BasicResponseHandler());
                    
                    com.appbuilder.sdk.android.Statics.closeMain = true;
                    
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    
                    handler.sendEmptyMessage(SHOW_DIALOG);
                    //handler.sendEmptyMessage(CLOSE_ALL);
                
                    //android.os.Process.killProcess(android.os.Process.myPid());
                }catch(Exception ex){
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    
                    Log.d("", "");
                }
            }
            
        }.start();
    }
    
    private class Parser extends DefaultHandler{
        
        private boolean endpointStarted = false;
        private StringBuilder sb = new StringBuilder();
        
        public String parse(String xml){
            try{
                Xml.parse(xml, this);
                
                return sb.toString().trim();
            }catch(Exception ex){
            }
            
            return "";
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(localName.equalsIgnoreCase("endpoint")){
                endpointStarted = true;
            }
            
            super.startElement(uri, localName, qName, attributes); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(localName.equalsIgnoreCase("endpoint")){
                endpointStarted = false;
            }
            
            super.endElement(uri, localName, qName); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(endpointStarted){
                sb.append(ch, start, length);
            }
            
            super.characters(ch, start, length); //To change body of generated methods, choose Tools | Templates.
        }
      
    }
    
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle(getString(getResources().getIdentifier("feedback_alert_send_ok", "string", getPackageName())));
        builder.setMessage(getString(getResources().getIdentifier("feedback_alert_send_ok", "string", getPackageName())));
        builder.setPositiveButton(getString(getResources().getIdentifier("ok", "string", getPackageName())),
            new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                    
                }
            }
        );
        builder.create().show();
    }
    
}
