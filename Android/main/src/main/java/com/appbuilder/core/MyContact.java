package com.appbuilder.sdk.android;

        import java.io.Serializable;
        import java.util.ArrayList;
        import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 01.12.14
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
public class MyContact implements Serializable{
    public String id;
    public String name;
    public List<String> phones = new ArrayList<String>();
    private boolean checked;

    public MyContact(String id, String name, List<String> phones, boolean checked) {
        this.id = id;
        this.name = name;
        this.phones = phones;
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public MyContact() {
    }
}

