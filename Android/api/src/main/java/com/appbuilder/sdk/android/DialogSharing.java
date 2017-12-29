/****************************************************************************
 * *
 * Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
 * *
 * This file is part of iBuildApp.                                          *
 * *
 * This Source Code Form is subject to the terms of the iBuildApp License.  *
 * You can obtain one at http://ibuildapp.com/license/                      *
 * *
 ****************************************************************************/
package com.appbuilder.sdk.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doubledeath on 12.08.2015.
 */
public class DialogSharing extends Dialog {

    public static class Configuration {

        private final boolean isEmpty;

        private final Item.OnClickListener facebookSharingClickListener;
        private final Item.OnClickListener twitterSharingClickListener;
        private final Item.OnClickListener emailSharingClickListener;
        private final Item.OnClickListener smsSharingClickListener;
        private final Item.OnClickListener savePictureSharingClickListener;
        private final List<Item> customItems;

        private Configuration(Builder builder) {
            facebookSharingClickListener = builder.facebookSharingClickListener;
            twitterSharingClickListener = builder.twitterSharingClickListener;
            emailSharingClickListener = builder.emailSharingClickListener;
            smsSharingClickListener = builder.smsSharingClickListener;
            savePictureSharingClickListener = builder.savePictureSharingClickListener;
            customItems = builder.customItems;
            isEmpty = builder.isEmpty;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public static class Builder {

            private boolean isEmpty;

            private Item.OnClickListener facebookSharingClickListener;
            private Item.OnClickListener twitterSharingClickListener;
            private Item.OnClickListener emailSharingClickListener;
            private Item.OnClickListener smsSharingClickListener;
            private Item.OnClickListener savePictureSharingClickListener;
            private List<Item> customItems;

            public Builder() {
                isEmpty = true;

                customItems = new ArrayList<>();
            }

            public Builder setFacebookSharingClickListener(Item.OnClickListener facebookSharingClickListener) {
                this.facebookSharingClickListener = facebookSharingClickListener;
                isEmpty = false;

                return this;
            }

            public Builder setTwitterSharingClickListener(Item.OnClickListener twitterSharingClickListener) {
                this.twitterSharingClickListener = twitterSharingClickListener;
                isEmpty = false;

                return this;
            }

            public Builder setEmailSharingClickListener(Item.OnClickListener emailSharingClickListener) {
                this.emailSharingClickListener = emailSharingClickListener;
                isEmpty = false;

                return this;
            }

            public Builder setSmsSharingClickListener(Item.OnClickListener smsSharingClickListener) {
                this.smsSharingClickListener = smsSharingClickListener;
                isEmpty = false;

                return this;
            }

            public Builder setSavePictureSharingClickListener(Item.OnClickListener savePictureSharingClickListener) {
                this.savePictureSharingClickListener = savePictureSharingClickListener;
                isEmpty = false;

                return this;
            }

            public Builder addCustomListener(int titleResource, int iconResource, boolean top, Item.OnClickListener onClickListener) {
                customItems.add(new Item(titleResource, iconResource, top, onClickListener));
                isEmpty = false;

                return this;
            }

            public Configuration build() {
                return new Configuration(this);
            }

        }

    }

    enum ViewHolder {

        ;

        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();

            if (viewHolder == null) {
                viewHolder = new SparseArray<>();
                view.setTag(viewHolder);
            }

            View childView = viewHolder.get(id);

            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }

            return (T)childView;
        }

    }

    public static class Item {

        public interface OnClickListener {
            void onClick();
        }

        private static final int HEIGHT = 60;
        private static final int ICON_SIZE_WH = 40;
        private static final int ICON_MARGIN_LR = 10;
        private static final int TITLE_TEXT_SIZE = 20;

        private int titleResource = -1;
        private int iconResource = -1;
        private boolean alignTop;
        private OnClickListener onClickListener;

        private Item(int titleResource, int iconResource, OnClickListener onClickListener) {
            this.titleResource = titleResource;
            this.iconResource = iconResource;
            this.onClickListener = onClickListener;
        }

        private Item(int titleResource, int iconResource, boolean alignTop, OnClickListener onClickListener) {
            this.titleResource = titleResource;
            this.iconResource = iconResource;
            this.alignTop = alignTop;
            this.onClickListener = onClickListener;
        }

    }

    private static class Adapter extends BaseAdapter {

        private List<Item> items;
        private Activity context;
        private DialogInterface dialog;
        private float density;

        private Adapter(Activity context, List<Item> items, DialogInterface dialog) {
            this.items = items;
            this.context = context;
            this.dialog = dialog;
            density = context.getResources().getDisplayMetrics().density;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Item item = getItem(position);

            if(convertView == null)
                convertView = inflateItemView();

            ((ImageView)ViewHolder.get(convertView, R.id.icon)).setImageResource(item.iconResource);
            ((TextView)ViewHolder.get(convertView, R.id.title)).setText(item.titleResource);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    item.onClickListener.onClick();
                }
            });

            return convertView;
        }

        private View inflateItemView() {
            RelativeLayout layout = new RelativeLayout(context);
            layout.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, Float.valueOf(Item.HEIGHT * density).intValue()));
            layout.setBackgroundColor(context.getResources().getColor(android.R.color.white));

            ImageView icon = new ImageView(context);
            icon.setId(R.id.icon);

            final int iconSizeWH = Float.valueOf(Item.ICON_SIZE_WH * density).intValue();
            final int iconMarginLR = Float.valueOf(Item.ICON_MARGIN_LR * density).intValue();

            icon.setLayoutParams(new RelativeLayout.LayoutParams(iconSizeWH, iconSizeWH) {{
                addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                leftMargin = iconMarginLR;
                rightMargin = iconMarginLR;
            }});

            TextView title = new TextView(context);
            title.setId(R.id.title);
            title.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT) {{
                addRule(RelativeLayout.RIGHT_OF, R.id.icon);
            }});
            title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, Item.TITLE_TEXT_SIZE);
            title.setTextColor(Color.parseColor("#cc000000"));

            layout.addView(icon, 0);
            layout.addView(title, 1);

            return layout;
        }

    }

    private Adapter adapter;
    private ListView listView;

    DialogSharing(Activity context, Configuration configuration) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        listView = new ListView(context);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        listView.setDivider(new ColorDrawable(Color.parseColor("#33000000")));
        listView.setDividerHeight(1);

        setContentView(listView);
        makeDialog(context, configuration);
    }

    private void makeDialog(final Activity context, final Configuration configuration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Item> enabledItems = new ArrayList<>();

                if(configuration.facebookSharingClickListener != null)
                    enabledItems.add(new Item(R.string.share_facebook, R.drawable.share_facebook, configuration.facebookSharingClickListener));

                if(configuration.twitterSharingClickListener != null)
                    enabledItems.add(new Item(R.string.share_twitter, R.drawable.share_twitter, configuration.twitterSharingClickListener));

                if(configuration.emailSharingClickListener != null)
                    enabledItems.add(new Item(R.string.share_email, R.drawable.share_email, configuration.emailSharingClickListener));

                if(configuration.smsSharingClickListener != null)
                    enabledItems.add(new Item(R.string.share_message, R.drawable.share_message, configuration.smsSharingClickListener));

                if(configuration.savePictureSharingClickListener != null)
                    enabledItems.add(new Item(R.string.share_save_picture, R.drawable.share_save_picture, configuration.savePictureSharingClickListener));

                if(configuration.customItems.size() > 0) {
                    int i = 0;

                    for(Item item : configuration.customItems)
                        if(item.alignTop)
                            enabledItems.add(i++, item);
                        else
                            enabledItems.add(item);
                }

                adapter = new Adapter(context, enabledItems, DialogSharing.this);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

}
