package com.appbuilder.core;


        import java.io.Serializable;
        import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 15.07.14
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class CategoryEntity implements Serializable{
    public int id = -1;
    public String title;
    public String pictureUrl;
    public String picturePath;
    public int order = 0;
    public boolean enable = true;
    public List<String> sorted_apps_list;

    public CategoryEntity() {}

    public CategoryEntity(int id, String title, String pictureUrl, String picturePath, int order, boolean enable) {
        this.id = id;
        this.title = title;
        this.pictureUrl = pictureUrl;
        this.picturePath = picturePath;
        this.order = order;
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", picturePath='" + picturePath + '\'' +
                ", order=" + order +
                ", enable=" + enable +
                //", lastAppid=" + sorted_apps_list +
                '}';
    }
}
