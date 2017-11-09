package com.ibuildapp.masterapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ibuildapp.masterapp.R;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.ApplicationEntity;
import com.ibuildapp.masterapp.utils.Statics;
import com.ibuildapp.masterapp.view.AlphaImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class PagingAdapter extends BaseImageAdapter {
    // const
    private final String TAG = "com.ibuildapp.masterapp.adapter.PagingAdapter";
    private final int PAGES_TO_CLEAR = 4;
    private final float ASPECT_RATIO = 1.25f;
    private final int TEXT_HOLDER_SIZE = 30;

    private List<ApplicationEntity> content;
    private Bitmap placeHolder;
    private LayoutInflater inflater;
    private OnEndReached listener;
    private float density;
    private int itemsOnScreen;
    private int imageW;
    private int imageH;


    public PagingAdapter(Context context, AbsListView uiView, List<ApplicationEntity> contentVar, int holderSize, int itemsOnPage, int itemsOnScreen) {
        super(context,uiView);
        this.content = contentVar;
        this.itemsOnScreen = itemsOnScreen;
        this.inflater = LayoutInflater.from(context);
        density = context.getResources().getDisplayMetrics().density;
        this.placeHolder = BitmapFactory.decodeResource( context.getResources(), R.drawable.no_image );

        // вычислим размеры для картинки элемента
        int itemOnPageSize = holderSize/itemsOnPage;
        //imageW = (int) (itemOnPageSize - 2*density* Statics.FEATURED_IMAGE_MARGINS); //px
        imageW = (int) (itemOnPageSize); //px
        imageH = (int) (imageW * ASPECT_RATIO); // px

        setOnLoadedListener(new onLoadedListener() {
            @Override
            public void onImageLoaded(int uid, String downloadedImagePath) {
                // обновление картинки для приложения
                SqlAdapter.updateApplicationImagePath(uid, downloadedImagePath);
                Log.e("","");
            }
        });
    }

    public interface OnEndReached {
        void onEndReached();
    }

    public void setListener(OnEndReached listener) {
        this.listener = listener;
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
    public View getView( int i, View view, ViewGroup viewGroup ) {
        if ( i == ( getCount() -1 ) ) // достигли последнего элемента
        {
            if ( listener != null )
                listener.onEndReached();
        }

        //Log.e(TAG, "Position. " + Integer.toString(i));

        // ************** процедура чистки картинок **************

        // верхние от текущей позиции
        if ( i != 0 )
        {
            int abovePosition = i - (itemsOnScreen * PAGES_TO_CLEAR);
            if ( abovePosition > 0 )
            {
                for ( int index = 0; index < abovePosition; index++ )
                {
                    int key = content.get(index).appid;
                    if (imageMap.containsKey(key))
                    {
                        imageMap.remove(key);
                        //Log.e(TAG, "ABOVE. Removed position " + Integer.toString(index) + " key = "  +Integer.toString(key));
                    }
                }
            }

            // нижние от текущей позиции
            int belowPosition = (i + (itemsOnScreen * PAGES_TO_CLEAR));
            if ( belowPosition < getCount() )
            {
                for ( int index = belowPosition; index < getCount(); index++ )
                {
                    int key = content.get(index).appid;
                    if (imageMap.containsKey(key))
                    {
                        imageMap.remove(key);
//                        Log.e(TAG, "BELOW. Removed position " + Integer.toString(index) + " key = "  +Integer.toString(key)
//                            + " Belowposition = "+ Integer.toString(belowPosition)
//                            + " geCount() = " + Integer.toString(getCount())
//                            + " getView position = " + Integer.toString(i));
                    }
                }
            }
        }
        // ************** ************************* **************


        TextView text;
        AlphaImageView image;
        LinearLayout trickLayout = null;
        FrameLayout textHolder = null;
        FrameLayout imageHolder = null;
        View v = view;
        LinearLayout border2 = null;
        if ( v == null )
        {
            v = inflater.inflate(R.layout.masterapp_applist_item, null);
            text = (TextView) v.findViewById(R.id.app_text);
            image = (AlphaImageView) v.findViewById(R.id.app_image);
            trickLayout = (LinearLayout) v.findViewById(R.id.trick_layout);
            textHolder = (FrameLayout) v.findViewById(R.id.text_holder);
            imageHolder = (FrameLayout) v.findViewById(R.id.image_holder);
            border2 = (LinearLayout) v.findViewById(R.id.border_layout_2);
            v.setTag( new ViewHolderApp(text, image, trickLayout, textHolder, imageHolder, border2));
        } else
        {
            ViewHolderApp holder = (ViewHolderApp) v.getTag();
            text = holder.name;
            image = holder.image;
            trickLayout = holder.trickLayout;
            textHolder = holder.textHolder;
            imageHolder = holder.imageHolder;
            border2 = holder.border2;
        }

        text.setText(content.get(i).title);
        text.setSingleLine(true);


        // set view holder params
        AbsListView.LayoutParams holderParams = (AbsListView.LayoutParams) v.getLayoutParams();
        if ( holderParams != null )
        {
            holderParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            holderParams.height = imageH + (int) ((TEXT_HOLDER_SIZE * density));
        } else
        {
            holderParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imageH + (int) ((TEXT_HOLDER_SIZE * density)));
        }
        v.setLayoutParams(holderParams);

        // set textholder params
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textHolder.getLayoutParams();
        if ( params == null )
        {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) ((TEXT_HOLDER_SIZE * density)));
        } else
        {
            params.width = imageW;
            params.height = (int) ((TEXT_HOLDER_SIZE * density));
        }
        textHolder.setLayoutParams(params);

        setImage(image, i);

        try {
            image.setBackgroundColor(Color.parseColor(content.get(i).background));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }

    /**
     * Fucntion sets image to holder if image exists otherwise creates task to download it
     *
     * @param imageHolder - link to imageview
     * @param id          - bitmap id
     */
    private void setImage(ImageView imageHolder, int id) {
        imageHolder.setTag(content.get(id).appid);
        Bitmap btm = imageMap.get(content.get(id).appid);
        if (btm == null || btm.getHeight() == 1) {
            GradientDrawable gd = new GradientDrawable();
            //Log.e(TAG, "color = " + content.get(id).background);

            // добавляем прозрачность к цвету
            int color = 0;
            try {
                color = Color.parseColor(content.get(id).background);
            } catch (Exception e) {
                Log.e( TAG, "Item name = " + content.get(id).title + "ERROR");
                color = Color.parseColor("#B5B5B5");
            }
            color = color & Color.parseColor("#00ffffff"); // обнуляем прозрачность
            color = color | Color.parseColor("#aa000000"); // выставляем прозрачность
            gd.setColor(color);
            imageHolder.setImageDrawable(gd);

            //Log.d(TAG, "Start thread = " + new SimpleDateFormat("mm:ss.SSS").format(new Date())  + " Name = " + content.get(id).title );
            addTask(imageHolder, content.get(id).appid, content.get(id).title, "", content.get(id).picturePath, content.get(id).pictureUrl, -1, -1, -1, -1, Bitmap.Config.RGB_565);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }

    private static class ViewHolderApp
    {
        public TextView name;
        public AlphaImageView image;
        public LinearLayout trickLayout;
        public FrameLayout textHolder;
        public FrameLayout imageHolder;
        public LinearLayout border2;

        private ViewHolderApp(TextView name, AlphaImageView image, LinearLayout trickLayout, FrameLayout textHolder, FrameLayout imageHolder, LinearLayout border2) {
            this.name = name;
            this.image = image;
            this.trickLayout = trickLayout;
            this.textHolder = textHolder;
            this.imageHolder = imageHolder;
            this.border2 = border2;
        }
    }
}
