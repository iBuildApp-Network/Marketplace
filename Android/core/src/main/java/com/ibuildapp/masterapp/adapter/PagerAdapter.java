package com.ibuildapp.masterapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appbuilder.core.AppBuilder;
import com.ibuildapp.masterapp.R;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.ApplicationEntity;
import com.ibuildapp.masterapp.utils.Statics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 17.07.14
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
public class PagerAdapter extends BaseImageAdapter {
    // constants
    private final String TAG = "com.example.PagerMultiplePages.Adapter";

    // backend
    private float density;
    private float itemsOnPage;
    private int imageHW;
    private Context context;
    private List<ApplicationEntity> content;
    private int holderSize;
    private Map<Integer, Map<Integer, Bitmap>> btmMap = new HashMap<Integer, Map<Integer, Bitmap>>();
    private LayoutInflater inflater;
    private int itemOnPageSize;

    // UI
    private Bitmap placeHolder;

    public PagerAdapter(Context context, List<ApplicationEntity> inputContent, float itemsOnPage, int holderSize) throws IllegalArgumentException {
        super(context, null);

        if ( itemsOnPage <=0 )
            throw new IllegalArgumentException("itemsOnPage must be greater then 0");

        this.content = inputContent;
        this.itemsOnPage = itemsOnPage;
        this.context = context;
        this.holderSize = holderSize;
        this.placeHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
        density = context.getResources().getDisplayMetrics().density;
        inflater = LayoutInflater.from(context);

        // вычислим размеры для картинки элемента
        itemOnPageSize = (int) (holderSize/itemsOnPage);
        imageHW = (int) (itemOnPageSize - 2*density* Statics.FEATURED_IMAGE_MARGINS);

//        setOnLoadedListener(new onLoadedListener() {
//            @Override
//            public void onImageLoaded(int uid, String downloadedImagePath) {
//                // обновление картинки для featured приложения
//                SqlAdapter.updateFeaturedImageByApp(uid, downloadedImagePath);
//                Log.e("","");
//            }
//        });
    }

    @Override
    public Object getItem(int i) {
        return content.get(i);
    }

    @Override
    public long getItemId(int i) {
        return content.get(i).hashCode();
    }

    @Override
    public int getCount() {
        //return (int) Math.ceil(((float)content.size()/itemsOnPage));
        return content.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if ( view == null )
            v = inflater.inflate(R.layout.masterapp_featured_item, viewGroup, false);

        if ( v.getLayoutParams() == null )
        {
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(itemOnPageSize, (int) (itemOnPageSize + 14*density));
            v.setLayoutParams(params);
        }
        else
        {
            v.getLayoutParams().width = itemOnPageSize;
            v.getLayoutParams().height = (int) (itemOnPageSize + 14*density);
        }

        ApplicationEntity itemContent = content.get(i);
        v.setTag(itemContent);

        // text
        TextView text = (TextView) v.findViewById(R.id.textView);
        text.setText(itemContent.title);

        // image
        ImageView img = (ImageView) v.findViewById(R.id.imageView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageHW,imageHW);
        params.setMargins((int) (density*Statics.FEATURED_IMAGE_MARGINS),(int) (density*Statics.FEATURED_IMAGE_MARGINS),(int) (density*Statics.FEATURED_IMAGE_MARGINS),(int) (density*5));
        img.setLayoutParams(params);

//        Bitmap btm = imageMap.get(itemContent.appid);
//        if (btm == null || btm.getHeight() == 1) {
//            img.setImageBitmap(placeHolder);
//            addTask(img, itemContent.appid, itemContent.title, "", itemContent.picturePath, itemContent.pictureUrl, -1, -1);
//        } else {
//            img.setImageBitmap(btm);
//        }

        return v;
    }
}
