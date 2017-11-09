package com.ibuildapp.masterapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 28.07.14
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class CategoryTemplate implements Serializable{
    public int categoryid;
    public List<TemplateField> template;
}
