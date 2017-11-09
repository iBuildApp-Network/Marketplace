package com.ibuildapp.masterapp.adapter;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * callback for image download event
 */
public interface OnImageDoneListener {

    /**
     * callback when image downloadaed
     *
     * @param imageHolder         -
     * @param image               - image bitmap
     * @param downloadedImagePath - downloaded image path
     */
    public void onImageLoaded(int uid, ImageView imageHolder, String name, Bitmap image, String downloadedImagePath);
}
