package com.ibuildapp.masterapp.api;

import com.ibuildapp.masterapp.model.*;
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
     * Получение списка категорий
     * @return
     */
    @GET("/masterapp/category_list")
    void getCategoryList(Callback<CategoryListResponse> cb);

    /**
     * Получение списка отсортированных приложений для категории
     * @return
     */
    @Headers("Content-Encoding: gzip")
    @GET("/masterapp/sorted_app_list")
    void getSortedAppList(@Query("category_id") int categoryId, Callback<AppsId> cb);

    /**
     * Получение списка популярных приложений
     * @return
     */
    @GET("/masterapp/featured_apps_list")
    void getFeaturedList(Callback<FeaturedResponse> cb);

    /**
     * Запрос поиска блокирующий
     * @return
     */
    @GET("/masterapp/find")
    AppsId searchQuerySync(@Query("category_id") int cagetoryId,
                       @Query("search") String search);

    /**
     * Запрос поиска асинхронный
     * @return
     */
    @GET("/masterapp/find")
    void searchQueryAsync(@Query("category_id") int cagetoryId,
                           @Query("search") String search,
                           Callback<AppsId> cb);

    /**
     * Запрос на получение списка шаблонов для категорий
     * @return
     */
    @GET("/masterapp/category_template")
    void getTemplates( Callback<TemplateResponse> cb );

    /**
     * Получение списка приложений
     * @return
     */
    @FormUrlEncoded
    @POST("/masterapp/app_list")
    FeaturedResponse getAppList(@Field("appid") String appidList);

    /**
     * Получение списка приложений асинхронно
     * @return
     */
    @FormUrlEncoded
    @POST("/masterapp/app_list")
    void getAppListAsync(@Field("appid") String appidList, Callback<FeaturedResponse> cb);

    /**
     * Получение списка приложений
     * @return
     */
    @Multipart
    @POST("/masterapp/add_app")
    AddAppResponse addApp( @Part("title") String title,
                           @Part("logo") TypedFile  photo );

    /**
     * АСИНХРОННЫЙ Запрос на увеличение рейтинга у приложения
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
     * СИНХРОННЫЙ Запрос на увеличение рейтинга у приложения
     * @return
     */
    @FormUrlEncoded
    @POST("/masterapp/rate_app")
    StatusOnly rateAppSync( @Field("appid") int appid,
                            @Field("uuid") String uuid,
                            @Field("rate") int rate,
                            @Field("random") int random);

    /**
     * Запрос на получение подходящего префикса для картинок
     * @return
     */
    @GET("/masterapp/get_splash_prefix")
    void getSplashPrefix( @Query("platform") String platform,
                          @Query("screen_width") int screen_width,
                          @Query("screen_height") int screen_height,
                          Callback<PrefixResponse> cb );

    /**
     * Запрос шаринга списка телефонов
     * Выполняется синхронно
     * @return
     */
    @POST("/user.phone.php")
    SmsSharingResponse smsSharing( @Body SmsBody body );

}
