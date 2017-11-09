package com.appbuilder.core;


        import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 22.07.14
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class CategoryListResponse {
    private List<CategoryEntity> categories;

    public List<CategoryEntity> getCategoryList() {
        return categories;
    }

    public void setCategoryList(List<CategoryEntity> categoryList) {
        this.categories = categoryList;
    }
}

