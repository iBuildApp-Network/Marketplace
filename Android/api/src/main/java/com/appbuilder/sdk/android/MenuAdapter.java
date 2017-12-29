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
/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 07.06.13
 * Time: 11:13
 * To change this template use File | Settings | File Templates.
 */
package com.appbuilder.sdk.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 07.05.13
 * Time: 17:20
 * To change this template use File | Settings | File Templates.
 */
public class MenuAdapter extends BaseAdapter {

    private int IMAGE_ID = 10001;
    private int TITLE_ID = 10002;
    private int SUBTITLE_ID = 10003;
    private ArrayList<Widget> widgets;
    private ArrayList<Bitmap> thumbnails = new ArrayList<Bitmap>();
    private Context context;
    private LayoutInflater layoutInflater;
    // get UI links
    private ImageView image;
    private TextView title;
    private TextView subtitle;
    private float density;

    public MenuAdapter( Context context,
                 ArrayList<Widget> widgets, ArrayList<Bitmap> thumbnails )
    {
        this.context = context;
        this.widgets = widgets;
        this.thumbnails = thumbnails;
        layoutInflater = LayoutInflater.from(context);
        density = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public int getCount() {
        return widgets.size();
    }

    @Override
    public Object getItem(int i) {
        return widgets.get(i);
    }

    @Override
    public long getItemId(int i) {
        return widgets.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View row = view;
        if ( row == null )
        {
            row = customView();
        } else
        {
            // get UI links
            image = (ImageView)row.findViewById(IMAGE_ID);
            title = (TextView)row.findViewById(TITLE_ID);
            subtitle = (TextView)row.findViewById(SUBTITLE_ID);
        }

        // set content
        if ( widgets.get(i).getTitle() != null && widgets.get(i).getTitle().equals("") != true )
        {
            title.setText(widgets.get(i).getTitle());
        } else
            title.setVisibility(View.INVISIBLE);

        if ( widgets.get(i).getSubtitle() != null && widgets.get(i).getSubtitle().equals("") != true )
        {
            subtitle.setText(widgets.get(i).getSubtitle());
        } else
            subtitle.setVisibility(View.INVISIBLE);

//        Bitmap tempBM = null;
//        try
//        {
//            tempBM = thumbnails.get(i);
//        } catch ( Exception e)
//        {
//            tempBM = null;
//        }
//
//        if ( tempBM == null)
//        {
//            try
//            {
//                tempBM = proccessBitmap(widgets.get(i).getFaviconFilePath());
//                thumbnails.add(i,tempBM);
//            } catch (  Exception e)
//            {
//                Log.d("","");
//            }
//
//        }
        try {
            image.setImageBitmap(thumbnails.get(i));
        }     catch (  Exception e)
        {
            Log.d("","");
        }


        return row;
    }

    // *******************************************************************************
    // resize image according to width and height and round corners and save it to file
    // "cachePath + md5(url)" file
    public static Bitmap proccessBitmap ( String fileName )
    {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //opts.inSampleSize = 2;

        Bitmap bitmap = null;
        try {
            // decode image with appropriate options
            File tempFile = new File(fileName);
            try{
                System.gc();
                bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
            }catch(Exception ex){
                Log.d("", "");
            }catch(OutOfMemoryError e)
            {
                Log.d("","");
                System.gc();
                try
                {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
                } catch(Exception ex){
                    Log.d("","");
                }catch( OutOfMemoryError ex )
                {
                    Log.e("decodeImageFile", "OutOfMemoryError");
                }
            }
        } catch ( Exception e)
        {
            Log.d("","");
            return null;
        }

        return bitmap;
    }

    // **********************************************************************************
    // create custom adapter view
    private View customView ()
    {
        // creating root layout
        LinearLayout root = new LinearLayout(context);
        //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root.setOrientation(LinearLayout.HORIZONTAL);
        //root.setLayoutParams(params);

        // creating image holder with image
        LinearLayout imageHolder = new LinearLayout(context);
        LinearLayout.LayoutParams  params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageHolder.setOrientation(LinearLayout.HORIZONTAL);
        imageHolder.setLayoutParams(params);

        LinearLayout imageHolderInner = new LinearLayout(context);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins( (int)(density*6),(int)(density*6),(int)(density*6),(int)(density*6) );
        imageHolder.setOrientation(LinearLayout.HORIZONTAL);
        imageHolder.setLayoutParams(params);

        image  = new ImageView(context);
        image.setId(IMAGE_ID);
        params = new LinearLayout.LayoutParams((int)(density*60), (int)(density*60));
        image.setLayoutParams(params);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        imageHolderInner.addView(image);
        imageHolder.addView(imageHolderInner);

        // creating item description holder with text
        LinearLayout descriptionHolder = new LinearLayout(context);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins( (int)(density*6),0,0,0 );
        descriptionHolder.setOrientation(LinearLayout.VERTICAL);
        descriptionHolder.setLayoutParams(params);

        // module title
        RelativeLayout moduleTitleHolder = new RelativeLayout(context);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins( 0,(int)(density*4),0,0 );
        params.gravity = Gravity.RIGHT;
        moduleTitleHolder.setLayoutParams(params);

        title = new TextView(context);
        title.setId(TITLE_ID);
        RelativeLayout.LayoutParams paramsRel = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsRel.setMargins( 0,0,(int)(density*6),0 );
        title.setLayoutParams(paramsRel);
        //title.setTextSize((int)(density*12));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);

        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setSingleLine();

        moduleTitleHolder.addView(title);

        // module subtitle
        LinearLayout moduleSubTitleHolder = new LinearLayout(context);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins( 0,(int)(density*2),0,0 );
        moduleSubTitleHolder.setOrientation(LinearLayout.VERTICAL);
        moduleSubTitleHolder.setLayoutParams(params);

        subtitle = new TextView(context);
        subtitle.setId(SUBTITLE_ID);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins( 0,0,(int)(density*10),0 );
        //subtitle.setTextSize((int)(density*10));
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        subtitle.setMaxLines(2);
        subtitle.setLayoutParams(params);

        moduleSubTitleHolder.addView(subtitle);

        descriptionHolder.addView(moduleTitleHolder);
        descriptionHolder.addView(moduleSubTitleHolder);

        // combine image&descriptoin holder together
        root.addView(imageHolder);
        root.addView(descriptionHolder);

        return root;
    }
}
