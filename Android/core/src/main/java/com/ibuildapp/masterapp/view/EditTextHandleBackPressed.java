package com.ibuildapp.masterapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 24.10.14
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
public class EditTextHandleBackPressed extends EditText {
    private OnBackPressed backPressed;

    public void setBackPressedInterface( OnBackPressed backPressed )
    {
        this.backPressed = backPressed;
    }

    public EditTextHandleBackPressed(Context context) {
        super(context);
    }

    public EditTextHandleBackPressed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextHandleBackPressed(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ( backPressed != null )
            {
                backPressed.onBackPressed();
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public interface OnBackPressed {
        void onBackPressed();
    }
}
