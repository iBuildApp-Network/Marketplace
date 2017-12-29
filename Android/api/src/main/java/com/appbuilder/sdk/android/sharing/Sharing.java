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
package com.appbuilder.sdk.android.sharing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Sharing {

    public enum SharingTypes {
        SHARING_TYPE_ANY,
        SHARING_TYPE_VKONTAKTE,
        SHARING_TYPE_LINKEDIN,
        SHARING_TYPE_FACEBOOK,
        SHARING_TYPE_TWITTER
    }

    /**
     * @param types   тип сервиса
     * @param message текст сообщения (используется везде)
     * @throws IllegalStateException выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    public static void share(SharingTypes types, String message) throws IllegalStateException {
        internalShare(types, null, message, null, null, null, null);
    }

    /**
     * @param types    тип сервиса
     * @param title    заголовок сообщения (используется в LinkedIn)
     * @param message  текст сообщения (используется везде)
     * @param imageUrl ссылка на картинку
     * @throws IllegalStateException  выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    public static void share(SharingTypes types, String title, String message, String imageUrl) throws IllegalStateException {
        internalShare(types, title, message, null, null, imageUrl, null);
    }

    /**
     * @param types    тип сервиса
     * @param message  текст сообщения (используется везде)
     * @param imageUrl ссылка на картинку
     * @throws IllegalStateException  выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    public static void share(SharingTypes types, String message, String imageUrl) throws IllegalStateException {
        internalShare(types, null, message, null, null, imageUrl, null);
    }

    /**
     * @param types         тип сервиса
     * @param message       текст сообщения (используется везде)
     * @param imageResource идентификатор ресурса (картинки)
     * @param ctx           контекст активити, используется для подгрузки ресурсов
     * @throws IllegalStateException  выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    public static void share(SharingTypes types, String message, Integer imageResource, Activity ctx) throws IllegalStateException {
        internalShare(types, null, message, null, imageResource, null, ctx);
    }

    /**
     * @param types          тип сервиса
     * @param title          заголовок сообщения (используется в LinkedIn)
     * @param message        текст сообщения (используется везде)
     * @param imageResource  идентификатор ресурса (картинки)
     * @param ctx            контекст активити, используется для подгрузки ресурсов
     * @throws IllegalStateException  выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    public static void share(SharingTypes types, String title, String message, Integer imageResource, Activity ctx) throws IllegalStateException {
        internalShare(types, title, message, null, imageResource, null, ctx);
    }

    /**
     * @param types        тип сервиса
     * @param title        заголовок сообщения (используется в LinkedIn)
     * @param message      текст сообщения (используется везде)
     * @param description  описание (используется в LinkedIn)
     * @param ctx          контекст активити, используется для подгрузки ресурсов
     * @throws IllegalStateException  выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    public static void share(SharingTypes types, String title, String message, String description, Activity ctx) throws IllegalStateException {
        internalShare(types, title, message, null, null, null, ctx);
    }

    /**
     * @param types        тип сервиса
     * @param title        заголовок сообщения (используется в LinkedIn)
     * @param message      текст сообщения (используется везде)
     * @param description  описание (используется в LinkedIn)
     * @param imageUrl     ссылка на картинку
     * @param ctx          контекст активити, используется для подгрузки ресурсов
     * @throws IllegalStateException  выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    public static void share(SharingTypes types, String title, String message, String description, String imageUrl, Activity ctx) throws IllegalStateException {
        internalShare(types, title, message, description, null, imageUrl, ctx);
    }

    /**
     * @param types          тип сервиса
     * @param title          заголовок сообщения (используется в LinkedIn)
     * @param message        текст сообщения (используется везде)
     * @param description    описание (используется в LinkedIn)
     * @param imageResource  идентификатор ресурса (картинки)
     * @param imageUrl       ссылка на картинку
     * @param ctx            контекст активити, используется для подгрузки ресурсов
     * @throws IllegalStateException  выкидывается если что-то пошло не так
     * @brief функция для расшаривания контента
     */
    private static void internalShare(SharingTypes types, String title, String message, String description, Integer imageResource, String imageUrl, Activity ctx) throws IllegalStateException {
        switch (types) {
            case SHARING_TYPE_ANY:
                break;
            case SHARING_TYPE_VKONTAKTE:
                VkontakteSharing.share(title, message, imageResource, imageUrl, ctx);
                break;
            case SHARING_TYPE_LINKEDIN:
                LinkedInSharing.share(title, message, imageResource, imageUrl, ctx, description);
                break;
            case SHARING_TYPE_FACEBOOK:
                break;
            case SHARING_TYPE_TWITTER:
                break;
        }
    }

}
