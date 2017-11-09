package com.ibuildapp.masterapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 15.07.14
 * Time: 13:41
 * To change this template use File | Settings | File Templates.
 */
public class AlphaImageView extends ImageView {

    public AlphaImageView(Context context) {
        super(context);
    }

    public AlphaImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageBitmapWithAlpha (Bitmap bm)
    {
        super.setImageBitmap(bm);
        Animation alpha = new AlphaAnimation(0.3f,1.0f);
        alpha.setDuration(500);
        startAnimation(alpha);
    }
}
