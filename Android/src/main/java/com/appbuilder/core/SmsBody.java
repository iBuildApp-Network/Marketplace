package com.appbuilder.core;


        import java.io.Serializable;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 03.12.14
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
public class SmsBody implements Serializable {
    public String action;
    public String project_id;
    public Map<String, PhoneNamePare> phones;

    public SmsBody() {
        this.action = "send";
        this.project_id = "masterapp";
        this.phones = new HashMap<String, PhoneNamePare>();
    }

    public static class PhoneNamePare
    {
        public String phone;
        public String name;

        public PhoneNamePare(String phone, String name) {
            this.phone = phone;
            this.name = name;
        }
    }
}

