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

import android.graphics.Bitmap;
import android.view.View;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 01.07.13
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public interface TopBarInterface {

    void drawTopBarRightButton( View view );
    void drawTopBarLeftButton( View view );
    void setTopBarRightButtonOnClickListener( View.OnClickListener l );
    void setTopBarLeftButtonOnClickListener( View.OnClickListener l );
    void setTopBarBackground( int id );
    void setTopBarBackground( Bitmap src );
    void setTopBarTitle( String title );
    void setTopBarTitleColor( int color );
    void hideTopBar(  );
    void invisibleTopBar();
    void visibleTopBar();
    void drawTopBarTitleView( View view, int gravity );
    void setTopBarRightButtonText(String text, boolean showArrow, View.OnClickListener clickHandler);
    void setTopBarLeftButtonText(String text, boolean showArrow, View.OnClickListener clickHandler);
    void swipeBlock();
    void designButton( TextView text, BarDesigner.TitleDesign designer );
    void setTitleLineAmount( int maxLines );
    void disableSwipe();
}
