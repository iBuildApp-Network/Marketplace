package com.appbuilder.core;


/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 15.07.14
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationEntity {
    public int appid;
    public int categoryid;
    public String token;
    public String title;
    public String description;
    public String pictureUrl;
    public String picturePath;
    public boolean favourited;
    public boolean active;
    public String background;

    public ApplicationEntity() {
        appid = -1;
        categoryid = -1;
        token="";
        title="";
        description="";
        pictureUrl="";
        picturePath="";
        favourited = false;
        active = false;
        //background = "#cccccc";
    }

    public ApplicationEntity(int appid, int categoryid, String token, String title, String description, String pictureUrl, String picturePath) {
        this.appid = appid;
        this.categoryid = categoryid;
        this.token = token;
        this.title = title;
        this.description = description;
        this.pictureUrl = pictureUrl;
        this.picturePath = picturePath;
    }

    @Override
    public String toString() {
        return "ApplicationEntity{" +
                "appid=" + appid +
                ", categoryid=" + categoryid +
                ", token='" + token + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", picturePath='" + picturePath + '\'' +
                ", favourited=" + favourited +
                ", active=" + active +
                '}';
    }
}

