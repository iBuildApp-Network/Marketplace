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
package com.appbuilder.sdk.android.pushnotification;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 13.03.14
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public class AppPushNotificationMessage {
    public long uid;                        // уникальный идентификатор сообщения в базе
    public String packageName;              // имя пакета, которому предназначается пуш
    public String statusBarText;            // текст уведомления
    public String descriptionText;          // текст уведомления
    public String titleText;                // заголовок уведомления
    public String imgUrl;                   // ссылка на картинку сообщения
    public Date notificationDate;           // время прихода сообщения
    public String imagePath;                // путь к картинке на диске
    public long androidNotificationId;      // номер уведомления в статус баре
    public int widgetOrder;                 // номер виджета который надо запустить
    public boolean isPackageExist;          // вкомпилен ли такой пакет в приложени???


    /**
     * конструктор по умолчанию с инициализацией значений дефолтными полями
     */
    public AppPushNotificationMessage() {
        this.uid = -1;
        this.packageName = "";
        this.statusBarText = "";
        this.descriptionText = "";
        this.imgUrl = "";
        Date startDate = new Date();
        startDate.setTime(-1);
        this.notificationDate = startDate;
        this.imagePath = "";
        this.androidNotificationId = -1;
        this.widgetOrder = -1;
        this.isPackageExist = false;
    }

    public AppPushNotificationMessage(String packageName, String title, String statusBarText, String descriptionText, String imgUrl, Date date, int widgetOrder) {
        this.titleText = title;
        this.packageName = packageName;
        this.statusBarText = statusBarText;
        this.descriptionText = descriptionText;
        this.imgUrl = imgUrl;
        this.notificationDate = date;
        this.widgetOrder = widgetOrder;
    }
}
