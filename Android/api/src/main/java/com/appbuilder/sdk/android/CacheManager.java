/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appbuilder.sdk.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 *
 * @author mike
 */
public class CacheManager {
    
    public static String saveObject(Widget widget, Object object){
        String id = null;
        int i = 1;
        while(true){
            String cache = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder() + "/" + i;
            File cacheFile = new File(cache);
            if(cacheFile.exists()){
                i++;
            }else{
                id = "" + i;
                break;
            }
        }
        
        if(id == null){
            return null;
        }
        
        if(id.length() == 0){
            return null;
        }
        
        return saveObject(widget, object, id);
    }
    
    public static String saveObject(Widget widget, Object object, String id){
        return saveObject(widget, object, id, false);
    }
    
    public static String saveObject(Widget widget, Object object, String id, 
            boolean replace){
        String cachePath = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder();
        File cachePathFile = new File(cachePath);
        try{
            if(!cachePathFile.exists()){
                cachePathFile.mkdirs();
            }
        }catch(Exception e){
            return null;
        }
        
        String cache = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder() + "/" + id;
        File cacheFile = new File(cache);
        
        if(cacheFile == null){
            return null;
        }
        
        if(cacheFile.exists()){
            if(replace){
                try{
                    cacheFile.delete();
                    cacheFile.createNewFile();
                }catch(IOException iOEx){
                    return null;
                }
            }else{
                return null;
            }
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(cacheFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch(Exception e){
            return null;
        }
        
        return id;
    }
    
    public static Object getObject(Widget widget, String id){
        Object result = null;
        
        String cache = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder() + "/" + id;
        File cacheFile = new File(cache);
        
        if(cacheFile == null){
            return null;
        }
        
        if(!cacheFile.exists()){
            return null;
        }
        
        try{
            FileInputStream fis = new FileInputStream(cacheFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            result = ois.readObject();
            ois.close();
            fis.close();
        }catch(Exception iOEx){
            return null;
        }
        
        return result;
    }
    
    public static String saveBytes(Widget widget, byte[] bytes){
        String id = null;
        int i = 1;
        while(true){
            String cache = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder() + "/" + i;
            File cacheFile = new File(cache);
            if(cacheFile.exists()){
                i++;
            }else{
                id = "" + i;
                break;
            }
        }
        
        if(id == null){
            return null;
        }
        
        if(id.length() == 0){
            return null;
        }
        
        return saveBytes(widget, bytes, id);
    }
    
    public static String saveBytes(Widget widget, byte[] bytes, String id){
        return saveBytes(widget, bytes, id, false);
    }
    
    public static String saveBytes(Widget widget, byte[] bytes, String id,
            boolean replace){
        String cachePath = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder();
        File cachePathFile = new File(cachePath);
        try{
            if(!cachePathFile.exists()){
                cachePathFile.mkdirs();
            }
        }catch(Exception e){
            return null;
        }
        
        String cache = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder() + "/" + id;
        File cacheFile = new File(cache);
        
        if(cacheFile == null){
            return null;
        }
        
        if(cacheFile.exists()){
            if(replace){
                try{
                    cacheFile.delete();
                    cacheFile.createNewFile();
                }catch(IOException iOEx){
                    return null;
                }
            }else{
                return null;
            }
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(cacheFile);
            fos.write(bytes);
            fos.close();
        } catch(Exception e){
            return null;
        }
        
        return id;
    }
    
    public static byte[] getBytes(Widget widget, String id){
        byte[] result = null;
        
        String cache = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder() + "/" + id;
        File cacheFile = new File(cache);
        
        if(cacheFile == null){
            return null;
        }
        
        if(!cacheFile.exists()){
            return null;
        }
        
        try{
            FileInputStream fis = new FileInputStream(cacheFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int flag = fis.read();
            while(flag != -1){
                baos.write(flag);
                flag = fis.read();
            }
            result = baos.toByteArray();
            fis.close();
        }catch(Exception iOEx){
            return null;
        }
        
        return result;
    }
    
    public static String[] getCacheIds(Widget widget){
        String[] ids = null;
        
        String cachePath = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder();
        File cachePathFile = new File(cachePath);
        if(!cachePathFile.exists()){
            return new String[0];
        }
        
        ArrayList<String> strings = new ArrayList<String>();
        File[] files = cachePathFile.listFiles();
        for(int i = 0; i < files.length; i++){
            strings.add(files[i].getName());
        }
        
        ids = new String[strings.size()];
        for(int i = 0; i < strings.size(); i++){
            ids[i] = strings.get(i);
        }
        
        return ids;
    }
    
    public static void delete(Widget widget, String id){
        String cache = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder() + "/" + id;
        File cacheFile = new File(cache);
        
        if(cacheFile == null){
            return;
        }
        
        if(cacheFile.exists()){
            cacheFile.delete();
        }
    }
    
    public static void clean(Widget widget){
        String cachePath = widget.getCachePath() + "/" + widget.getPluginId()
                + "-" + widget.getOrder();
        File cachePathFile = new File(cachePath);
        
        if(cachePathFile == null){
            return;
        }
        
        if(!cachePathFile.exists()){
            return;
        }
        
        File[] files = cachePathFile.listFiles();
        for(int i = 0; i < files.length; i++){
            files[i].delete();
        }
    }
    
}
