package com.ibuildapp.masterapp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.ibuildapp.masterapp.R;
import com.ibuildapp.masterapp.model.CategoryEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 17.07.14
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class CategoryAdapter extends BaseImageAdapter {

    private LayoutInflater inflater;
    private List<CategoryEntity> content;
    private Bitmap placeHolder;
    private int rowH;
    private Map<String, Bitmap> bitmapMap = new HashMap<String, Bitmap>();


    public CategoryAdapter(Context context, AbsListView uiView, List<CategoryEntity> content, int rowH ) {
        super(context,uiView);

        inflater = LayoutInflater.from(context);
        this.content = content;
        this.placeHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
        this.rowH = rowH;
        Resources res = context.getResources();

        // подгружаем все картинки в память - да тяжело, а что делать?
        // возможно добавить ухудшение качества до RGB_565 если будет падать на мелких девайсах
        bitmapMap.put("beauty & fitness", BitmapFactory.decodeResource(res, R.drawable.category_beauty));
        bitmapMap.put("music & entertainment", BitmapFactory.decodeResource(res, R.drawable.category_music));
        bitmapMap.put("blogs & magazines", BitmapFactory.decodeResource(res, R.drawable.category_blogs));
        bitmapMap.put("automotive", BitmapFactory.decodeResource(res, R.drawable.category_automotive));
        bitmapMap.put("sports", BitmapFactory.decodeResource(res, R.drawable.category_sports));
        bitmapMap.put("schools & nonprofit", BitmapFactory.decodeResource(res, R.drawable.category_nonprofit));
        bitmapMap.put("shops", BitmapFactory.decodeResource(res, R.drawable.category_shops));
        bitmapMap.put("restaurants", BitmapFactory.decodeResource(res, R.drawable.category_restaurents));
        bitmapMap.put("professional services", BitmapFactory.decodeResource(res, R.drawable.category_services));
        bitmapMap.put("realestate", BitmapFactory.decodeResource(res, R.drawable.category_realestate));
        bitmapMap.put("law & finance", BitmapFactory.decodeResource(res, R.drawable.category_finance));
        bitmapMap.put("others", BitmapFactory.decodeResource(res, R.drawable.category_other));
    }

    @Override
    public int getCount() {
        return content.size();
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
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = view;
        if ( v == null )
            v = inflater.inflate(R.layout.masterapp_category_grid_item, null);

        if ( v.getLayoutParams() == null )
        {
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rowH);
            v.setLayoutParams(params);
        } else
        {
            v.getLayoutParams().height = rowH;
        }

        TextView categoryText = (TextView) v.findViewById(R.id.category_text);
        ImageView categoryImg = (ImageView) v.findViewById(R.id.category_image);
        categoryText.setText(content.get(i).title.toUpperCase());

        Bitmap btm = bitmapMap.get(content.get(i).title.toLowerCase());
        if ( btm != null)
            categoryImg.setImageBitmap(btm);

        return v;
    }

    @Override
    public void clearBitmaps() {
        Set<String> keys = bitmapMap.keySet();
        for ( String key : keys )
        {
            Bitmap btm = bitmapMap.get(key);
            if (btm != null)
                btm.recycle();
        }
    }
}
