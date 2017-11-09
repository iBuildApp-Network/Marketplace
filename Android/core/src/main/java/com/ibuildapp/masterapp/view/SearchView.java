package com.ibuildapp.masterapp.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ibuildapp.masterapp.R;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 17.07.14
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
public class SearchView extends LinearLayout implements TextWatcher {

    private EditText edit;
    private LinearLayout cancel;
    private LinearLayout root;
    private OnClickListener cancelClick;
    private TextWatcher watcher;

    public SearchView(Context context) {
        super(context);
        init();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setWatcher(TextWatcher watcher) {
        this.watcher = watcher;
    }

    public void setOnEditorActionListener( TextView.OnEditorActionListener listener ) throws NullPointerException
    {
        if ( listener == null )
            throw new NullPointerException("Listener is null!");

        edit.setOnEditorActionListener( listener );
    }

    public void setCancelClick(OnClickListener cancelClick) {
        this.cancelClick = cancelClick;
    }

    public void noBackground()
    {
        root.setBackgroundColor(Color.TRANSPARENT);
    }

    public void setSelection(int idx )
    {
        edit.setSelection(idx);
    }

    private void init()
    {
        setGravity(Gravity.CENTER_VERTICAL);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.masterapp_search_view, null);
        v.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        root = (LinearLayout) v.findViewById(R.id.search_root);

        edit = (EditText) v.findViewById(R.id.search);
        edit.addTextChangedListener( this );

        cancel = (LinearLayout) v.findViewById(R.id.cancel);
        cancel.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setText("");
                cancel.setVisibility(GONE);
                if ( cancelClick != null )
                    cancelClick.onClick(cancel);
            }
        });



        addView(v);
    }

    public EditText getEdit() {
        return edit;
    }

    public String getText()
    {
        return edit.getText().toString();
    }

    public void setText(String text)
    {
        edit.setText(text);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if ( watcher != null )
            watcher.beforeTextChanged(charSequence, i, i2, i3);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if ( watcher != null )
            watcher.onTextChanged(charSequence, i, i2, i3);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if ( editable.length() > 0 )
            cancel.setVisibility(VISIBLE);
        else
            cancel.setVisibility(GONE);

        if ( watcher != null )
            watcher.afterTextChanged(editable);
    }
}
