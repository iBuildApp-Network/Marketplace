package com.appbuilder.core;


//        import com.ibuildapp.masterapp.model.*;
        import retrofit.Callback;
        import retrofit.http.*;
        import retrofit.mime.TypedFile;

        import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 22.07.14
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public interface ServerAPIInterface {

    /**
     * @return
     */
    @GET("/masterapp/category_list")
    void getCategoryList(Callback<CategoryListResponse> cb);

    /**
     * @return
     */
    @Headers("Content-Encoding: gzip")
    @GET("/masterapp/sorted_app_list")
    void getSortedAppList(@Query("category_id") int categoryId, Callback<AppsId> cb);

    /**
     * @return
     */
    @GET("/masterapp/featured_apps_list")
    void getFeaturedList(Callback<FeaturedResponse> cb);

    /**
     * @return
     */
    @GET("/masterapp/find")
    AppsId searchQuerySync(@Query("category_id") int cagetoryId,
                           @Query("search") String search);

    /**
     * @return
     */
    @GET("/masterapp/find")
    void searchQueryAsync(@Query("category_id") int cagetoryId,
                          @Query("search") String search,
                          Callback<AppsId> cb);

    /**
     * @return
     */
    @GET("/masterapp/category_template")
    void getTemplates( Callback<TemplateResponse> cb );

    /**
     * @return
     */
    @FormUrlEncoded
    @POST("/masterapp/app_list")
    FeaturedResponse getAppList(@Field("appid") String appidList);

    /**
     * @return
     */
    @FormUrlEncoded
    @POST("/masterapp/app_list")
    void getAppListAsync(@Field("appid") String appidList, Callback<FeaturedResponse> cb);

    /**
     * @return
     */
    @Multipart
    @POST("/masterapp/add_app")
    AddAppResponse addApp( @Part("title") String title,
                           @Part("logo") TypedFile  photo );

    /**
     * @return
     */
    @FormUrlEncoded
    @POST("/masterapp/rate_app")
    void rateAppAsync(  @Field("appid") int appid,
                        @Field("uuid") String uuid,
                        @Field("rate") int rate,
                        @Field("random") int random,
                        Callback<StatusOnly> cb);

    /**
     * @return
     */
    @FormUrlEncoded
    @POST("/masterapp/rate_app")
    StatusOnly rateAppSync( @Field("appid") int appid,
                            @Field("uuid") String uuid,
                            @Field("rate") int rate,
                            @Field("random") int random);

    /**
     * @return
     */
    @GET("/masterapp/get_splash_prefix")
    void getSplashPrefix( @Query("platform") String platform,
                          @Query("screen_width") int screen_width,
                          @Query("screen_height") int screen_height,
                          Callback<PrefixResponse> cb );

    /**
     * @return
     */
    @POST("/user.phone.php")
    SmsSharingResponse smsSharing( @Body SmsBody body );

}

