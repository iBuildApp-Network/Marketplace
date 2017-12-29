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
package com.appbuilder.sdk.android;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 28.10.13
 * Time: 18:33
 * Класс-дизайнер описываем визуальное представление для баров (верхнего и нижнего)
 */
public class BarDesigner implements Serializable {
    public int color;                   // цвет подложки
    public TitleDesign titleDesign;     // дизайн заголовка
    public TitleDesign itemDesign;      // дизайн элемента в баре

    public TitleDesign rightButtonDesign;       // дизайн правой кнопки для bottom bar
    public TitleDesign leftButtonDesign;        // дизайн левой кнопки для bottom bar

    public BarDesigner() {
        this.titleDesign = new TitleDesign();
        this.itemDesign = new TitleDesign();
        this.rightButtonDesign = new TitleDesign();
        this.leftButtonDesign = new TitleDesign();
    }

    /**
     * класс дизайнер заголовка
     */
    public class TitleDesign implements Serializable {
        public int textColor;
        public int selectedColor;
        public String textAlignment="";
        public int numberOfLines;
        public int fontSize;

        public String fontFamily="";
        public String fontWeight="";

    }

}

