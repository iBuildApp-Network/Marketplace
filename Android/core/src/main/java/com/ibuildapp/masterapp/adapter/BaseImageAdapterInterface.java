package com.ibuildapp.masterapp.adapter;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface BaseImageAdapterInterface {

    /**
     * @param imageHolder - image holder
     * @param resPath     - resource assets name
     * @param cachePath   - image cache path
     * @param url         - image url
     * @param edgeLimit   - image edge limitataion
     * @param width       - image preview width
     * @param height      - image preview height
     * @param roundK      - round edges koeff
     * @param config      - bitmap configurations ( ALPHA_8, RGB_565, ARGB_4444, ARGB_8888 )
     */
    public void addTask(ImageView imageHolder,
                        int uid,
                        String DUBUG_PRODUCT_NAME,
                        String resPath,
                        String cachePath,
                        String url,
                        int edgeLimit,
                        int width,
                        int height,
                        int roundK,
                        Bitmap.Config config);

    /**
     * not in use now
     */
    public void stopAllTasks();
}
