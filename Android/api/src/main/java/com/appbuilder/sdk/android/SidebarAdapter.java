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

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SidebarAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Widget> widgets;
    private int currentWidgetOrder;
    private SidebarSharing sidebarSharing;

    private float density;

    public SidebarAdapter(Context context, @NonNull ArrayList<Widget> widgets, int currentWidgetOrder) {
        this.context = context;
        this.widgets = widgets;
        this.currentWidgetOrder = currentWidgetOrder;

        density = context.getResources().getDisplayMetrics().density;
    }

    public SidebarAdapter(Context context, @NonNull ArrayList<Widget> widgets, int currentWidgetOrder, SidebarSharing sidebarSharing) {
        this.context = context;
        this.widgets = widgets;
        this.currentWidgetOrder = currentWidgetOrder;
        this.sidebarSharing = sidebarSharing;

        density = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public int getCount() {
        return widgets.size();
    }

    @Override
    public Widget getItem(int position) {
        return widgets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getOrder() == -1 ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Widget widget = getItem(position);
        int viewType = getItemViewType(position);

        if(viewType == 1) {
            if (convertView == null)
                convertView = createSeparator();
        } else {
            if (convertView == null)
                convertView = createViewItem();

            convertView.setBackgroundColor(widget.getOrder() == currentWidgetOrder ? Color.parseColor("#33ffffff") : context.getResources().getColor(android.R.color.transparent));

            TextView label = (TextView)convertView.findViewById(R.id.sidebar_item_label);
            label.setText(widget.getLabel());

            ImageView icon = (ImageView)convertView.findViewById(R.id.sidebar_item_icon);

            if(widget.getIconResourceId() != 0) {
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(widget.getIconResourceId());
            } else
                icon.setVisibility(View.GONE);

            if(widget.isDrawSharing())
                ((FrameLayout)convertView.findViewById(R.id.sidebar_item_bottom_view)).addView(createBottomSharingView());
            else
                ((FrameLayout)convertView.findViewById(R.id.sidebar_item_bottom_view)).removeAllViews();
        }

        convertView.setVisibility(widget.isHidden() ? View.GONE : View.VISIBLE);

        return convertView;
    }

    private View createViewItem() {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        item.setFocusable(false);
        item.setFocusableInTouchMode(false);

        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Float.valueOf(60 * density).intValue()));
        top.setPadding(Float.valueOf(15 * density).intValue(), 0, Float.valueOf(15 * density).intValue(), 0);
        top.setFocusable(false);
        top.setFocusableInTouchMode(false);

        ImageView icon = new ImageView(context);
        icon.setId(R.id.sidebar_item_icon);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Float.valueOf(25 * density).intValue(), Float.valueOf(25 * density).intValue()) {{
            gravity = Gravity.CENTER_VERTICAL;
            rightMargin = Float.valueOf(10 * density).intValue();
        }});
        icon.setFocusable(false);
        icon.setFocusableInTouchMode(false);

        TextView label = new TextView(context);
        label.setId(R.id.sidebar_item_label);
        label.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setTextColor(context.getResources().getColor(android.R.color.white));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        label.setMaxLines(1);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setFocusable(false);
        label.setFocusableInTouchMode(false);

        top.addView(icon);
        top.addView(label);

        FrameLayout bottom = new FrameLayout(context);
        bottom.setId(R.id.sidebar_item_bottom_view);
        bottom.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Float.valueOf(60 * density).intValue()));
        bottom.setVisibility(View.GONE);
        bottom.setFocusable(false);
        bottom.setFocusableInTouchMode(false);

        item.addView(top);
        item.addView(bottom);

        return item;
    }

    private View createSeparator() {
        View separator = new View(context);
        separator.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1));
        separator.setBackgroundColor(Color.parseColor("#4dffffff"));

        RelativeLayout separatorHolder = new RelativeLayout(context);
        separatorHolder.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 1));
        separatorHolder.setPadding(Float.valueOf(15 * density).intValue(), 0, Float.valueOf(15 * density).intValue(), 0);
        separatorHolder.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        separatorHolder.addView(separator);

        return separatorHolder;
    }

    private View createBottomSharingView() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setPadding(Float.valueOf(15 * density).intValue(), 0, Float.valueOf(15 * density).intValue(), 0);

        int sharingImageSize = (int) ((context.getResources().getDisplayMetrics().widthPixels * 0.7) // 70% размера экрана - размер sidebar
                - (2*15*density) // паддинги холдера sidebar
                - (3*8*density)) // отступы кнопок друг от друга
                /4;              // колличество кнопок

        ImageView facebook = new ImageView(context);
        facebook.setLayoutParams(new LinearLayout.LayoutParams(sharingImageSize, sharingImageSize) {{
            gravity = Gravity.CENTER_VERTICAL;
            rightMargin = Float.valueOf(8 * density).intValue();
        }});
        facebook.setImageResource(R.drawable.sharing_facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sidebarSharing != null)
                    sidebarSharing.shareViaFacebook();
            }
        });

        ImageView twitter = new ImageView(context);
        twitter.setLayoutParams(new LinearLayout.LayoutParams(sharingImageSize, sharingImageSize) {{
            gravity = Gravity.CENTER_VERTICAL;
            rightMargin = Float.valueOf(8 * density).intValue();
        }});
        twitter.setImageResource(R.drawable.sharing_twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sidebarSharing != null)
                    sidebarSharing.shareViaTwitter();
            }
        });

        ImageView email = new ImageView(context);
        email.setLayoutParams(new LinearLayout.LayoutParams(sharingImageSize, sharingImageSize) {{
            gravity = Gravity.CENTER_VERTICAL;
            rightMargin = Float.valueOf(8 * density).intValue();
        }});
        email.setImageResource(R.drawable.sharing_email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sidebarSharing != null)
                    sidebarSharing.shareViaEmail();
            }
        });

        ImageView sms = new ImageView(context);
        sms.setLayoutParams(new LinearLayout.LayoutParams(sharingImageSize, sharingImageSize) {{
            gravity = Gravity.CENTER_VERTICAL;
        }});
        sms.setImageResource(R.drawable.sharing_sms);
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sidebarSharing != null)
                    sidebarSharing.shareViaSms();
            }
        });

        layout.addView(facebook);
        layout.addView(twitter);
        layout.addView(email);
        layout.addView(sms);

        return layout;
    }

}
