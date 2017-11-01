package com.appbuilder.core.LoginScreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.appbuilder.core.LoginScreen.service.LoginScreenService;
import com.appbuilder.core.LoginScreen.service.OnDone;
import com.appbuilder.core.R;
import com.appbuilder.sdk.android.BarDesigner;
import com.appbuilder.sdk.android.LoginScreen;

public class RecoveryPasswordActivity extends Activity implements View.OnClickListener {

    final private int ARROW_WIDTH = 15;
    final private int ARROW_HEIGHT = 25;

    private ProgressDialog progressDialog = null;
    private ImageView backArrow = null;
    Button reset;
    TextView backText;
    EditText emailText;
    LoginScreen loginScreen;

    private BarDesigner navBarDesign = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.login_screen_recovery_password_activity);

        loginScreen = (LoginScreen) getIntent().getSerializableExtra("loginScreen");
        navBarDesign = (BarDesigner) getIntent().getSerializableExtra("navBarDesign");

        TextView pageTitle = (TextView) findViewById(R.id.textView2);

        pageTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, navBarDesign.titleDesign.fontSize);
        pageTitle.setTextColor(navBarDesign.titleDesign.textColor);

        float density = getResources().getDisplayMetrics().density;

        backArrow = (ImageView) findViewById(R.id.login_screen_restoration_back_arrow);
        backArrow.setImageResource(R.drawable.api_topbar_arrow);
        backArrow.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (density * ARROW_WIDTH), (int) (density * ARROW_HEIGHT)));
        backArrow.setColorFilter(navBarDesign.itemDesign.textColor);
        backText = (TextView) findViewById(R.id.login_screen_restoration_back_text);
        backText.setTextColor(navBarDesign.itemDesign.textColor);
        backText.setTextSize(TypedValue.COMPLEX_UNIT_SP, navBarDesign.itemDesign.fontSize);

        emailText = (EditText) findViewById(R.id.login_screen_recovery_email_text);
        reset = (Button) findViewById(R.id.login_screen_recovery_button);

        reset.setOnClickListener(this);
        findViewById(R.id.login_screen_restoration_back_button).setOnClickListener(this);
        //backText.setOnClickListener(this);

        emailText.addTextChangedListener(getWatcher());
        reset.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private TextWatcher getWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                reset.setEnabled(validate());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private boolean validate() {
        try {
            boolean result = emailText.getText().length() > 0;
            result &= android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString()).matches();

            Log.d("", "");
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_screen_recovery_button) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnectedOrConnecting()) {
            } else {
                Toast.makeText(this, R.string.reset_no_internet, Toast.LENGTH_LONG).show();

                return;
            }

            if (validate()) {
                startAction();
                LoginScreenService.doRecovery(loginScreen.getRecoveryPasswordEndpoint(), emailText.getText().toString(),
                        loginScreen.getAppId(), new OnDone() {
                    @Override
                    public void onDone(final int result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopAction();

                                if (result == 200) {
                                    Toast.makeText(RecoveryPasswordActivity.this, R.string.password_was_sent, Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RecoveryPasswordActivity.this);
                                    builder.setTitle(R.string.password_reset);
                                    builder.setMessage(R.string.email_not_found);
                                    builder.setPositiveButton("OK", null);
                                    builder.show();
                                }
                            }
                        });
                    }
                });
            }
        } else if (view.getId() == R.id.login_screen_restoration_back_button) {
            finish();
        }
    }

    public void startAction() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.reset_progress));
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //finish();
                    onBackPressed();
                }
            });
        }


        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void stopAction() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.hide();
            }
        }
    }
}
