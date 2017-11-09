package com.appbuilder.core.PushNotification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appbuilder.core.R;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 26.03.14
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */
public class AppPushNotificationDialogLayout extends FrameLayout {

    // constants
    private final int TEXT_MARGIN = 10;             //
    private final int CLOSE_BTN_SIZE = 20;          // размеры кнопки close
    private final double DIALOG_PERCENT = 0.9;      // ширина диалого в процентах от ширины экрана
    private final int CORNER_RADIUS = 10;           // радиуст скругления углов корневого Layout
    private final int ROOT_LAYOUT_PADDING = 18;     // отступы в корневом layout
    private final int TITLE_RIGHT_LEFT_MARGIN = 30; // отступы слева и справа у заголовка

    // data
    private String message;
    private String imgPath;
    private OnClickListener closeBtnEvent;
    private OnClickListener imageTapEvent;
    private Context context;
    private float density;
    private int screenWidth;
    private int screenHeight;
    private String titleText;

    public AppPushNotificationDialogLayout(Context context, String title, String message, String imgPath, OnClickListener closeBtnEvent, OnClickListener imageTapEvent) {
        super(context);
        this.message = message;
        this.imgPath = imgPath;
        this.titleText = title;
        this.closeBtnEvent = closeBtnEvent;
        this.imageTapEvent = imageTapEvent;
        this.context = context;
        this.density = context.getResources().getDisplayMetrics().density;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        initialize();
    }

    public AppPushNotificationDialogLayout(Context context, AttributeSet attrs, String title, String message, String imgPath, OnClickListener closeBtnEvent, OnClickListener imageTapEvent) {
        super(context, attrs);
        this.message = message;
        this.titleText = title;
        this.imgPath = imgPath;
        this.closeBtnEvent = closeBtnEvent;
        this.imageTapEvent = imageTapEvent;
        this.context = context;
        this.density = context.getResources().getDisplayMetrics().density;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        initialize();
    }

    private void initialize() {
        // *************************************************************************************************************
        // настройки корневого Layout
        // устанавливаеи ширину диалога screenWidth * 0.8
        setLayoutParams(new FrameLayout.LayoutParams((int) (screenWidth * DIALOG_PERCENT), ViewGroup.LayoutParams.WRAP_CONTENT));

        // цвет фона белый с закругленными краями
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#E5ffffff"), Color.parseColor("#E5ffffff")});
        float radius = CORNER_RADIUS * density;
        // rounded corners
        gd.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
        setBackgroundDrawable(gd);

        // вешаем обработчик
        if (imageTapEvent != null)
            setOnClickListener(imageTapEvent);

        // *************************************************************************************************************
        // content holder
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        int padding = (int) (ROOT_LAYOUT_PADDING * density);
        container.setPadding(padding, padding, padding, padding);

        // title
        TextView title = new TextView(context);
        title.setTextColor(Color.parseColor("#5B5B5B"));
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        linearParams.setMargins((int) (density * TITLE_RIGHT_LEFT_MARGIN), 0, (int) (density * TITLE_RIGHT_LEFT_MARGIN), (int) (density * TEXT_MARGIN));
        title.setLayoutParams(linearParams);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextSize(17);
        title.setTypeface(null, Typeface.BOLD);
        title.setMaxLines(2);
        if (TextUtils.isEmpty(titleText))
            title.setVisibility(GONE);
        else
            title.setText(titleText);

        //title.setBackgroundColor(Color.parseColor("#80ff0000"));


        // image
        ImageView image = new ImageView(context);
        image.setAdjustViewBounds(true);
        //image.setBackgroundColor(Color.parseColor("#8000ff00"));

        // text
        linearParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearParams.setMargins(0, (int) (10 * density), 0, 0);
        TextView text = new TextView(context);
        text.setTextColor(Color.BLACK);
        text.setLayoutParams(linearParams);
        text.setMaxLines(3);
        text.setText(message);
        //text.setBackgroundColor(Color.parseColor("#800000ff"));

        // temp bottom layout
        LinearLayout tempBottom = new LinearLayout(context);
        tempBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        //tempBottom.setBackgroundColor(Color.parseColor("#80ff00ff"));


        // image initialization
        if (TextUtils.isEmpty(imgPath)) {
            image.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) text.getLayoutParams()).setMargins(0, 0, 0, 0);
            ((LinearLayout.LayoutParams) title.getLayoutParams()).setMargins(0, 0, 0, (int) (5 * density));
        } else {
            // декодируем картинку и вставляем в холдер
            image.setVisibility(View.VISIBLE);
            Bitmap btm = BitmapFactory.decodeFile(imgPath);
            if (btm != null) {
                image.setImageBitmap(btm);

                // вычисляем размеры для картинки - она всегда будет квадратная
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) (screenWidth * DIALOG_PERCENT) - (2 * padding),
                        (int) (screenWidth * DIALOG_PERCENT) - (2 * padding));
                if (TextUtils.isEmpty(titleText))
                    params.setMargins(0, (int) (ROOT_LAYOUT_PADDING * density), 0, 0);

                image.setLayoutParams(params);

            } else {
                image.setVisibility(View.GONE);
                ((LinearLayout.LayoutParams) text.getLayoutParams()).setMargins(0, 0, 0, 0);
                ((LinearLayout.LayoutParams) title.getLayoutParams()).setMargins(0, 0, 0, (int) (5 * density));
            }
        }
        container.addView(title);
        container.addView(image);
        container.addView(text);
        container.addView(tempBottom);

        // *************************************************************************************************************
        // close btn
        LinearLayout closeBtnHolder = new LinearLayout(context); // холдер для кнопки закрыть. Ширина в 1.5 раз больше, для увеличение tap area
        if (closeBtnEvent != null)
            closeBtnHolder.setOnClickListener(closeBtnEvent);
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                (int) (density * 1.5 * CLOSE_BTN_SIZE),
                (int) (density * 1.5 * CLOSE_BTN_SIZE));
        frameParams.gravity = Gravity.RIGHT;
        padding = (int) (8 * density);
        frameParams.setMargins(0, padding, padding, 0);

        LinearLayout.LayoutParams linearP = new LinearLayout.LayoutParams(
                (int) (density * CLOSE_BTN_SIZE),
                (int) (density * CLOSE_BTN_SIZE));
        linearP.gravity = Gravity.RIGHT;
        linearP.setMargins(0, 0, -10, 0);


        ImageView closeBtn = new ImageView(context);
        closeBtn.setLayoutParams(linearP);
        closeBtn.setImageResource(R.drawable.push_dialog_btn_close);

        closeBtnHolder.addView(closeBtn, linearP);

        // Добавляем вьюшки на кастомный layout
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        addView(container, params);
        addView(closeBtnHolder, frameParams);
    }
}