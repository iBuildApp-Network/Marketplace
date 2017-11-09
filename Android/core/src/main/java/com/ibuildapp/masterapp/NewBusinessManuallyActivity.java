package com.ibuildapp.masterapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ibuildapp.masterapp.DAO.TemplatesDAO;
import com.ibuildapp.masterapp.api.ServerApi;
import com.ibuildapp.masterapp.db.SqlAdapter;
import com.ibuildapp.masterapp.model.*;
import com.ibuildapp.masterapp.utils.Statics;
import com.ibuildapp.masterapp.utils.Utils;
import retrofit.mime.TypedFile;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 28.07.14
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
public class NewBusinessManuallyActivity extends BaseActivity {

    // constants
    private final int RESULT_LOAD_IMAGE = 10001;

    
    // backend
    private CategoryTemplate template;
    private CategoryEntity category;
    private float density;
    private File logoFile;

    // UI
    private LinearLayout contentHolder;
    private ImageView logoImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeBackend();

        initializeUI();

        drawTemplate();
    }

    private void initializeBackend()
    {
        density = getResources().getDisplayMetrics().density;

        category = (CategoryEntity) getIntent().getSerializableExtra("category");
        TemplateResponse templates = new TemplatesDAO(Statics.cachePath).getTemplates();
        for ( CategoryTemplate s : templates.templates )
        {
            if ( s.categoryid == category.id )
            {
                template = s;
                break;
            }
        }
    }

    private void initializeUI()
    {
        setContentView(R.layout.masterapp_new_business_manually_layout);
        setTopbarTitle(category.title);

        setBackBtnArrow(getString(R.string.back));
        setBackBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setEditBtnText(getString(R.string.done));
        setEditBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.checkNetwork(NewBusinessManuallyActivity.this) < 0)
                {
                    Toast.makeText(NewBusinessManuallyActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (validate())
                {
                    for ( int i=0; i < contentHolder.getChildCount(); i++ )
                    {
                        View childView = contentHolder.getChildAt(i);
                        TemplateField s = (TemplateField) childView.getTag();
                        if ( s != null )
                        {
                            s.value = ( (EditText)childView ).getText().toString();
                        }
                    }

                    // TODO
                    // String res = ServerApi.getInstance().addAppCustom(template, logoFile);

                    //AddAppResponse res = ServerApi.getInstance().addApp("title", new TypedFile("image/*", logoFile));
                    Toast.makeText(NewBusinessManuallyActivity.this, "Запрос на сервер на добавление нового приложения ", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(NewBusinessManuallyActivity.this, "Исправьте неверные поля", Toast.LENGTH_SHORT).show();

            }
        });

        contentHolder = (LinearLayout) findViewById(R.id.businessmanually_holder);
    }

    private void drawTemplate()
    {
        for ( TemplateField s : template.template )
        {
            LinearLayout.LayoutParams params;

            if ( s.type.compareToIgnoreCase("normal") == 0 )
            {
                EditText edit = new EditText(NewBusinessManuallyActivity.this);
                if ( s.lines.compareToIgnoreCase("single") == 0)
                {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.template_singleline_height));
                    edit.setSingleLine(true);
                }
                else
                {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.template_multiline_height));
                    edit.setLines(3);
                    edit.setGravity(Gravity.TOP);
                }

                edit.setLayoutParams(params);
                edit.setTextSize(14);
                edit.setHint(s.name);
                edit.setTextColor(getResources().getColor(R.color.text_black));
                edit.setHintTextColor(getResources().getColor(R.color.text_grey));
                edit.setBackgroundColor(Color.TRANSPARENT);
                edit.setPadding(
                        (int) getResources().getDimension(R.dimen.template_left_right_padding),
                        (s.lines.compareToIgnoreCase("single") == 0 ? 0 : (int) getResources().getDimension(R.dimen.template_left_right_padding)),
                        (int) getResources().getDimension(R.dimen.template_left_right_padding),
                        0);
                edit.setTag(s);
                edit.addTextChangedListener( new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        setEditBackNormal();
                    }
                });
                contentHolder.addView(edit);
            } else if ( s.type.compareToIgnoreCase("separator") == 0 )
            {
                LinearLayout separator = new LinearLayout(NewBusinessManuallyActivity.this);
                separator.setLayoutParams( new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.separato_height)) );
                separator.setBackgroundColor(getResources().getColor(R.color.border_grey));
                contentHolder.addView(separator);
            } else if ( s.type.compareToIgnoreCase("mail") == 0 )
            {
                EditText edit = new EditText(NewBusinessManuallyActivity.this);
                if ( s.lines.compareToIgnoreCase("single") == 0)
                {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDimension(R.dimen.template_singleline_height)));
                    edit.setSingleLine(true);
                }
                else
                {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (int) getResources().getDimension(R.dimen.template_multiline_height));
                    edit.setLines(3);
                }
                edit.setLayoutParams(params);
                edit.setTextSize(14);
                edit.setTag(s);
                edit.setHint(s.name);
                edit.setTextColor(getResources().getColor(R.color.text_black));
                edit.setHintTextColor(getResources().getColor(R.color.text_grey));
                edit.setBackgroundColor(Color.TRANSPARENT);
                edit.setPadding((int)getResources().getDimension(R.dimen.template_left_right_padding),0,(int)getResources().getDimension(R.dimen.template_left_right_padding),0);
                edit.addTextChangedListener( new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        setEditBackNormal();
                    }
                });
                contentHolder.addView(edit);
            }
        }

        // дорисовываем кнопочку addlogo
        TextView addLogo = new TextView(NewBusinessManuallyActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                (int)getResources().getDimension(R.dimen.template_left_right_padding),
                (int)getResources().getDimension(R.dimen.template_left_right_padding),
                (int)getResources().getDimension(R.dimen.template_left_right_padding),
                (int)getResources().getDimension(R.dimen.template_left_right_padding));
        addLogo.setLayoutParams(params);
        addLogo.setText(getString(R.string.add_logo));
        addLogo.setTextColor(getResources().getColor(R.color.blue));
        addLogo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo открыть галерею для загрузки изображения
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        contentHolder.addView(addLogo);

        logoImage = new ImageView(NewBusinessManuallyActivity.this);
        logoImage.setAdjustViewBounds(true);
        logoImage.setLayoutParams(params);
        contentHolder.addView(logoImage);
    }

    private boolean validate()
    {
        int invalidCount = 0;
        for ( int i=0; i < contentHolder.getChildCount(); i++ )
        {
            View v = contentHolder.getChildAt(i);
            if ( v instanceof EditText )
            {
                TemplateField templateF = (TemplateField) v.getTag();
                if ( templateF.type.compareToIgnoreCase("mail") == 0 )
                {
                    if ( !Patterns.EMAIL_ADDRESS.matcher(((EditText)v).getText().toString()).matches() )
                    {
                        invalidCount++;
                        ((EditText) v).setTextColor(getResources().getColor(R.color.border_red));
                    }
                }
            }
        }

        if ( invalidCount > 0 )
            return false;
        else
            return true;
    }

    private void setEditBackNormal()
    {
        for ( int i=0; i < contentHolder.getChildCount(); i++ )
        {
            View v = contentHolder.getChildAt(i);
            if ( v instanceof EditText )
            {
                ((EditText) v).setTextColor(getResources().getColor(R.color.text_black));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            logoFile = new File(cursor.getString(idx));
            Bitmap image = BitmapFactory.decodeFile(logoFile.getAbsolutePath());

            try {
                Matrix matrix = new Matrix();
                ExifInterface exifInterface = new ExifInterface(logoFile.getAbsolutePath());

                int rotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                switch (rotation) {
                    case ExifInterface.ORIENTATION_NORMAL: {
                    }
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_90: {
                        matrix.postRotate(90);
                        image = Bitmap.createBitmap(image, 0, 0,
                                image.getWidth(), image.getHeight(), matrix,
                                true);
                    }
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_180: {
                        matrix.postRotate(180);
                        image = Bitmap.createBitmap(image, 0, 0,
                                image.getWidth(), image.getHeight(), matrix,
                                true);
                    }
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_270: {
                        matrix.postRotate(270);
                        image = Bitmap.createBitmap(image, 0, 0,
                                image.getWidth(), image.getHeight(), matrix,
                                true);
                    }
                    break;
                }
            } catch (Exception e) {
                Log.d("", "");
            }


            logoImage.setImageBitmap(image);
            Log.e("","");
        }
    }
}
