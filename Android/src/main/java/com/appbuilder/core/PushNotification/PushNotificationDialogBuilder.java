package com.appbuilder.core.PushNotification;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appbuilder.core.R;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 14.03.14
 * Time: 18:14
 * To change this template use File | Settings | File Templates.
 */
public class PushNotificationDialogBuilder extends AlertDialog.Builder {
    private Context context;
    private View.OnClickListener innerListener;
    private ImageView closeBtn;
    private View customView;

    public PushNotificationDialogBuilder(Context context) {
        super(context);
        this.context = context;
    }

    public AlertDialog.Builder setViewFromNotification(AppPushNotificationMessage messageStruct, View.OnClickListener onClick) {
        customView = LayoutInflater.from(context).inflate(R.layout.core_pushnotification_dialog, null);
        LinearLayout temp = (LinearLayout) customView.findViewById(R.id.notification_temp_layout);
        LinearLayout tempBottom = (LinearLayout) customView.findViewById(R.id.notification_temp_layout_bottom);
        ImageView notificationImg = (ImageView) customView.findViewById(R.id.notification_img);
        if (TextUtils.isEmpty(messageStruct.imagePath)) {
            temp.setVisibility(View.VISIBLE);
            tempBottom.setVisibility(View.VISIBLE);
            notificationImg.setVisibility(View.GONE);
        } else {
            // декодируем картинку и вставляем в холдер
            temp.setVisibility(View.GONE);
            tempBottom.setVisibility(View.GONE);
            notificationImg.setVisibility(View.VISIBLE);
            notificationImg.setImageBitmap(BitmapFactory.decodeFile(messageStruct.imagePath));
        }

        innerListener = onClick;
        closeBtn = (ImageView) customView.findViewById(R.id.notification_close);

        TextView message = (TextView) customView.findViewById(R.id.notification_text);
        message.setText(messageStruct.descriptionText);
        return super.setView(customView);
    }

    @Override
    public AlertDialog create() {
        final AlertDialog temp = super.create();
        temp.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        if (closeBtn != null)
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    temp.dismiss();
                    innerListener.onClick(view);


                }
            });
        return temp;
    }
}
