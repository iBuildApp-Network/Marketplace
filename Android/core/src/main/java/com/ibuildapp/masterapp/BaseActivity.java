package com.ibuildapp.masterapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseActivity extends Activity {
    private LinearLayout editBtn;
    private LinearLayout backBtn;
    private LinearLayout content;
    private RelativeLayout topbarHolder;
    private TextView title;
    private ProgressDialog progressDialog;

    private LayoutInflater inflater;
    protected float density;
    protected int screenWidth;
    protected int screenHeight;
    protected InputMethodManager imm;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.setContentView(R.layout.masterapp_base_layout);


        density = getResources().getDisplayMetrics().density;
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inflater = LayoutInflater.from(BaseActivity.this);

        initializeUI();
    }

    @Override
    public void setContentView(int layoutResID) {
        content.removeAllViews();
        content.addView(inflater.inflate(layoutResID, null), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void initializeUI() {
        editBtn = (LinearLayout) findViewById(R.id.masterapp_topbar_edit_btn);
        backBtn = (LinearLayout) findViewById(R.id.masterapp_topbar_back_btn);
        content = (LinearLayout) findViewById(R.id.masterapp_content);
        title = (TextView) findViewById(R.id.masterapp_topbar_title);
        topbarHolder = (RelativeLayout) findViewById(R.id.masterapp_topbar_holder);
    }

    public void hideTopBar()
    {
        topbarHolder.setVisibility(View.GONE);

    }

    // настройка названия формы
    public void setTopbarTitle( String titleString )
    {
        if ( !TextUtils.isEmpty(titleString) )
            title.setText(titleString);
    }

    // настройка правой кнопки
    public void setEditBtnView( View view ) throws NullPointerException
    {
        if (view != null)
        {
            editBtn.setVisibility(View.VISIBLE);
            editBtn.removeAllViews();
            editBtn.addView(view);
        }
        else
            throw new NullPointerException("View is null");
    }

    public void setEditBtnText( String text )
    {
        TextView textView = new TextView(this);
        textView.setTextColor(getResources().getColor(R.color.white));
        editBtn.setVisibility(View.VISIBLE);
        editBtn.removeAllViews();
        editBtn.addView(textView);

        if ( text != null )
            textView.setText(text);
    }

    public void hideEditBtn( )
    {
        editBtn.setVisibility( View.GONE );
    }

    public void showEditBtn( )
    {
        editBtn.setVisibility( View.VISIBLE );
    }

    public void setEditBtnClickListener( View.OnClickListener listener )
    {
        if ( listener != null )
            editBtn.setOnClickListener( listener );
    }

    public void setBackBtnView( View view ) throws NullPointerException
    {
        if (view != null)
        {
            backBtn.setVisibility(View.VISIBLE);
            backBtn.removeAllViews();
            backBtn.addView(view);
        }
        else
            throw new NullPointerException("View is null");
    }

    public void setBackBtnText( String text )
    {
        TextView textView = new TextView(this);
        textView.setTextColor(getResources().getColor(R.color.white));
        backBtn.setVisibility(View.VISIBLE);
        backBtn.removeAllViews();
        backBtn.addView(textView);

        if ( text != null )
            textView.setText(text);
    }

    public void setBackBtnArrow( String text )
    {
        View v = inflater.inflate(R.layout.masterapp_arrow_back_layout,null);
        TextView textView = (TextView) v.findViewById(R.id.textView);
        textView.setTextColor(getResources().getColor(R.color.white));
        backBtn.setVisibility(View.VISIBLE);
        backBtn.removeAllViews();
        backBtn.addView(v);

        if ( text != null )
            textView.setText(text);
    }

    public void hideBackBtn( )
    {
        backBtn.setVisibility(View.GONE);
    }

    public void setBackBtnClickListener( View.OnClickListener listener )
    {
        if ( listener != null )
            backBtn.setOnClickListener( listener );
    }

    /**
     * Базовая функция которая покажет нам прогресс диалог
     */
    public void showProgressDialog( String msg ) {
        progressDialog = ProgressDialog.show(this, "", msg, false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
