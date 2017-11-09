package com.ibuildapp.masterapp.DAO;

import android.util.Log;
import com.ibuildapp.masterapp.model.TemplateResponse;

import java.io.*;

/**
 * Class describes Data Access Object for userprofile object
 */
public class TemplatesDAO {

    private final String LOG_TAG = "com.ibuildapp.romanblack.ShopingCartPlugin.data.DAO.UserProfileDAO";
    private final String FILE_NAME = "template.data";
    private String cachePath;

    public TemplatesDAO(String cachePath) {
        this.cachePath = cachePath;

        // prepare cache folder
        File cacheFolder = new File(cachePath);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
    }

    public TemplateResponse getTemplates() {
        // DEserialization
        File cache = new File(cachePath + File.separator + FILE_NAME);
        if (cache.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
                TemplateResponse object = (TemplateResponse) ois.readObject();
                ois.close();
                return object;
            } catch (Exception e) {
                Log.e(LOG_TAG, "", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public void setTemplates(TemplateResponse templates) {
        // serialization
        File cache = new File(cachePath + File.separator + FILE_NAME);
        if (cache.exists()) {
            cache.delete();
        }

        try {
            cache.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
            oos.writeObject(templates);
            oos.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            cache.delete();
        }

    }
}
