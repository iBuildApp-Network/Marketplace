package com.ibuildapp.masterapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Base image adapter class provides easy access to download images for adapter asynchronous
 */
public class BaseImagePagerAdapter extends PagerAdapter implements BaseImageAdapterInterface {

    // constants
    private final int UPDATE_IMAGES = 10003;
    private final int THREAD_POOL_SIZE = 20;
    protected Context context;
    protected ConcurrentHashMap<Integer, Bitmap> imageMap;
    protected ConcurrentHashMap<Integer, ImageView> imageViewMap;
    private ConcurrentLinkedQueue taskQueue;
    private QueueManager queueTask;
    private AbsListView uiView;
    private onLoadedListener onLoadedListener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_IMAGES: {
                    // todo пока оставляем так.
                    ImageView img;
                    try
                    {
                        img = imageViewMap.get(msg.arg1);
                        img.setImageBitmap(imageMap.get(msg.arg1));
                    } catch (Exception e)
                    {
                        notifyDataSetChanged();
                    }

                    //notifyDataSetChanged();
//                    Bitmap btm = imageMap.get(msg.arg1);
//
//                    View v = uiView.getChildAt(msg.arg1 - uiView.getFirstVisiblePosition());
//                    if ( v != null )
//                    {
//                        ImageView img = (ImageView) ((ViewGroup)v).findViewById(R.id.category_image);
//                        img.setImageBitmap(btm);
//                        Log.e("Baseadapter", "v != null");
//                    } else
//                    {
//                        notifyDataSetChanged();
//                        Log.e("Baseadapter", "v == null");
//                    }
                }
                break;
            }
        }
    };

    public void setOnLoadedListener(BaseImagePagerAdapter.onLoadedListener onLoadedListener) {
        this.onLoadedListener = onLoadedListener;
    }

    public BaseImagePagerAdapter(Context context) {
        this.context = context;
        this.imageMap = new ConcurrentHashMap<Integer, Bitmap>();
        this.imageViewMap = new ConcurrentHashMap<Integer, ImageView>();
        this.taskQueue = new ConcurrentLinkedQueue();
        queueTask = new QueueManager();
        queueTask.start();
    }

    @Override
    public void addTask(ImageView imageHolder, int uid, String DUBUG_PRODUCT_NAME, String resPath, String cachePath, String url, int edgeLimit, int width, int height, int roundK, Bitmap.Config config) throws IllegalArgumentException {
        // refresh imageview for this uid!
        imageViewMap.put(uid, imageHolder);

        if (!imageMap.containsKey(uid)) {
            taskQueue.add(new TaskItem(imageHolder, uid, DUBUG_PRODUCT_NAME, resPath, cachePath, url, width, height));
            Bitmap.Config conf = Bitmap.Config.valueOf("RGB_565");
            imageMap.put(uid, Bitmap.createBitmap(1, 1, conf));
        }
    }

    @Override
    public void stopAllTasks() {
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return false;
    }

    /**
     * TASK object
     */
    private class TaskItem {

        private ImageView imageView;
        private int uid;
        private String uri;
        private String resPath;
        private String cachePath;
        private String name;
        private int width;
        private int height;

        public TaskItem(ImageView imageView, int uid, String name, String resPath, String cachePath, String uri, int width, int height) {
            this.uid = uid;
            this.name = name;
            this.imageView = imageView;
            this.resPath = resPath;
            this.cachePath = cachePath;
            this.uri = uri;

            this.width = width;
            this.height = height;
        }
    }

    /**
     * This manager handle queue limits and creates new thread to download images
     */
    private class QueueManager extends Thread implements OnImageDoneListener {

        private ConcurrentHashMap<Integer, Thread> threadList;

        private QueueManager() {
            this.threadList = new ConcurrentHashMap<Integer, Thread>();
        }

        @Override
        public void run() {
            super.run();

            while (true) {
                if (threadList.size() < THREAD_POOL_SIZE) {
                    TaskItem taskItem = (TaskItem) taskQueue.poll();
                    if (taskItem != null) {
                        if (!threadList.contains(taskItem.uid)) {
                            GetBitmapTask task = new GetBitmapTask(context, taskItem.uid, taskItem.name, taskItem.imageView, taskItem.resPath, taskItem.cachePath, taskItem.uri, -1, taskItem.width, taskItem.height, -1, Bitmap.Config.RGB_565);
                            task.setListener(this);
                            threadList.put(taskItem.uid, task);
                            task.start();
                        }
                    }
                }

                // sleep для разгрузки процессора
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onImageLoaded(int uid, ImageView imageHolder, String name, Bitmap image, String downloadedImagePath) {
            threadList.remove(uid);

            // put image to storage and refresh adapter
            if (image != null) {
                imageMap.put(uid, image);
                Message msg = handler.obtainMessage(UPDATE_IMAGES, uid, -1);
                handler.sendMessage(msg);

                if (!TextUtils.isEmpty(downloadedImagePath)) {
                    if (onLoadedListener != null) {
                        onLoadedListener.onImageLoaded(uid, downloadedImagePath);
                    }
                }
            }
        }
    }

    public interface onLoadedListener {

        /**
         * Callback after image downloading
         *
         * @param uid                 - image uid
         * @param downloadedImagePath - downloaded image path
         */
        void onImageLoaded(int uid, String downloadedImagePath);
    }
}
