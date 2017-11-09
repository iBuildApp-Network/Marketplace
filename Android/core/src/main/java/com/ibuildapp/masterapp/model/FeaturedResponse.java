package com.ibuildapp.masterapp.model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 22.07.14
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class FeaturedResponse {
    private List<ApplicationEntity> apps;

    public List<ApplicationEntity> getAppsList() {
        return apps;
    }

    public void AppsList(List<ApplicationEntity> categoryList) {
        this.apps = categoryList;
    }
}
