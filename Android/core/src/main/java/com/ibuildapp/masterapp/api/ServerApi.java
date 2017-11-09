package com.ibuildapp.masterapp.api;

import android.text.TextUtils;
import android.util.Log;
import com.ibuildapp.masterapp.model.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 22.07.14
 * Time: 11:58
 * To change this template use File | Settings | File Templates.
 */
public class ServerApi {

    // constants
    public static final int STATUS_OK = 0;
    private final String SERVICE_URL = "http://ibuildapp.com/";
    //private final String SERVICE_URL = "http://ibuilder.solovathost.com";
    private static ServerApi ourInstance = new ServerApi();

    private final String TAG = "com.smartsoft.kaard.api.ServerApi";
    private ServerAPIInterface mService;


    public static ServerApi getInstance() {
        return ourInstance;
    }

    private ServerApi() {
        try
        {
            RestAdapter mRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(SERVICE_URL)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();

            mService= mRestAdapter.create(ServerAPIInterface.class);
        }catch (Exception e)
        {
            Log.e(TAG,"");
        }

    }

    public void getCategoryList(Callback<CategoryListResponse> cb)
    {
        try {
            mService.getCategoryList(cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void getSortedAppList(int categoryId, Callback<AppsId> cb)
    {
        try {
            mService.getSortedAppList(categoryId,cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void getFeaturedList(Callback<FeaturedResponse> cb)
    {
        try {
            mService.getFeaturedList(cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public FeaturedResponse getAppsList( List<String> appidList )
    {
        try {
            // приобразуем список appid в json массив
            JSONArray jsArray = new JSONArray(appidList);
            if ( jsArray.toString().compareToIgnoreCase("[\"\"]") == 0)
                return null;

            return mService.getAppList(jsArray.toString());
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public void getAppsListAsync( List<String> appidList, Callback<FeaturedResponse> cb )
    {
        try {
            // приобразуем список appid в json массив
            JSONArray jsArray = new JSONArray(appidList);
            if ( jsArray.toString().compareToIgnoreCase("[\"\"]") == 0)
                return;

            mService.getAppListAsync(jsArray.toString(), cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return;
        }
    }

    public AppsId searchSync(int category, String search)
    {
        try {
            return mService.searchQuerySync(category, search);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public void searchAsync(int category, String search, Callback<AppsId> cb)
    {
        try {
            mService.searchQueryAsync(category, search, cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void getTemplates( Callback<TemplateResponse> cb )
    {
        try {
            mService.getTemplates(cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return;
        }
    }

    public void rateAppAsync( int appid, String uuid, boolean rate, Callback<StatusOnly> cb )
    {
        try {
            int rand = new Random().nextInt(65535);
            mService.rateAppAsync(appid, uuid, (rate)?1:0,rand, cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return;
        }
    }

    public StatusOnly rateAppSync( int appid, String uuid, boolean rate )
    {
        try {
            int rand = new Random().nextInt(65535);
            return mService.rateAppSync(appid, uuid, (rate)?1:0,rand);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }


    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // это рабочий запрос!!!!
    public AddAppResponse addApp( String title, TypedFile photo )
    {
        try {
            return mService.addApp(title, photo);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public String addAppCustom( CategoryTemplate paramList, File logoFile )
    {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(SERVICE_URL + "/masterapp/add_app");
        MultipartEntity multipartEntity = new MultipartEntity();

        try {
            for ( TemplateField s: paramList.template )
            {
                if ( !TextUtils.isEmpty(s.value) )
                    multipartEntity.addPart( s.name, new StringBody(s.value, Charset.forName("UTF-8")) );
            }

            if ( logoFile != null  )
            {
                multipartEntity.addPart("logo", new FileBody(logoFile));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        post.setEntity(multipartEntity);

        HttpResponse response = null;
        String result = null;
        try {
            String strResponseSaveGoal =
                    client.execute(post,  new BasicResponseHandler());

            //response = client.execute(post);
            return strResponseSaveGoal;
        } catch (IOException e) {
        }
        return  null;
    }

    public static String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String body = "";
        String content = "";
        while ((body = rd.readLine()) != null) {
            content += body + "\n";
        }
        return content.trim();
    }

    public void getSplashPrefix( String platform, int screen_width, int screen_height, Callback<PrefixResponse> cb  )
    {
        try {
            mService.getSplashPrefix(platform, screen_width, screen_height, cb);
        } catch (RetrofitError e) {
            Log.e(TAG, e.getMessage(), e);
            return;
        }
    }

    public SmsSharingResponse smsSharing( SmsBody body )
    {
        return mService.smsSharing(body);
    }
}
