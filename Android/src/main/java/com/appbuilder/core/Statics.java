/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appbuilder.core;

/**
 * @author SimpleIce
 */
public class Statics {
    public static OnPostListener innerInterface;

    public static void addActivityIntefrace(OnPostListener innerInterface) {
        Statics.innerInterface = innerInterface;
    }

    public static String BROADCAST_UID = "";
}
