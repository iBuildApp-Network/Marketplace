package com.appbuilder.core.LoginScreen;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.appbuilder.core.LoginScreen.service.LoginScreenService;
import com.appbuilder.core.LoginScreen.service.LoginSettings;
import com.appbuilder.core.LoginScreen.service.LoginSettingsService;
import com.appbuilder.core.LoginScreen.service.OnDone;
import com.appbuilder.core.R;
import com.appbuilder.core.Statics;
import com.appbuilder.sdk.android.BarDesigner;
import com.appbuilder.sdk.android.LoginScreen;

public class LoginWithEmailActivity extends Activity implements View.OnClickListener {
    private BarDesigner navBarDesign = null;

    private Button loginButton;
    private TextView recoveryPasswordButton;
    private EditText passwordText;
    private EditText usernameText;
    private LoginScreen loginScreen;
    private CheckBox checkBox;
    private ProgressDialog progressDialog;

    private static final String SETTINGS_NAME = "LOGIN_SETTINGS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_with_email_layout);

        loginScreen = (LoginScreen) getIntent().getSerializableExtra("loginScreen");
        navBarDesign = (BarDesigner) getIntent().getSerializableExtra("navBarDesign");

        TextView pageTitle = (TextView) findViewById(R.id.textView2);

        pageTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, navBarDesign.titleDesign.fontSize);
        pageTitle.setTextColor(navBarDesign.titleDesign.textColor);

        passwordText = (EditText) findViewById(R.id.login_screen_login_password);
        usernameText = (EditText) findViewById(R.id.login_screen_login_username);
        loginButton = (Button) findViewById(R.id.login_screen_login_button);
        recoveryPasswordButton = (TextView) findViewById(R.id.forget_your_pass_button);

        usernameText.setOnClickListener(this);
        passwordText.setOnClickListener(this);
        recoveryPasswordButton.setOnClickListener(this);

        passwordText.addTextChangedListener(getWatcher());
        usernameText.addTextChangedListener(getWatcher());

        loginButton.setOnClickListener(this);
        loginButton.setEnabled(validate());

        checkBox = (CheckBox) findViewById(R.id.login_screen_remember_me_check_box);

        LoginSettings settings = LoginSettingsService.loadSettings(getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE));
        usernameText.setText(settings.getUsername());
        passwordText.setText(settings.getPassword());

        if (validate()) {
            doLogin();
        }
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
                loginButton.setEnabled(validate());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private boolean validate() {
        Boolean result;

        try {
            result = (usernameText.getText().length() > 1) && (passwordText.getText().length() > 4);
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_screen_login_button) {
            doLogin();
        } else if (view.getId() == R.id.forget_your_pass_button) {
            Intent intent = new Intent(this, RecoveryPasswordActivity.class);
            intent.putExtra("loginScreen", loginScreen);
            intent.putExtra("navBarDesign", navBarDesign);
            startActivity(intent);
        }
    }

    private void doLogin() {
        final String password = passwordText.getText().toString();
        final String username = usernameText.getText().toString();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
        } else {
            Toast.makeText(this, R.string.login_no_internet, Toast.LENGTH_LONG).show();

            return;
        }

        startAction();
        LoginScreenService.doLogin(loginScreen.getLoginEndpoint(), username, password, "email",
                loginScreen.getAppId(), new OnDone() {
            @Override
            public void onDone(final int result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopAction();
                        if (result == 200) {
                            if (checkBox.isChecked()) {
                                LoginSettingsService.saveSettings(getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE),
                                        new LoginSettings(username, password));
                            } else {
                                try {
                                    LoginSettingsService.deleteSettings(getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE));
                                } catch (Exception ex) {
                                }
                            }

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("username", username);
                            resultIntent.putExtra("password", password);
                            setResult(RESULT_OK, resultIntent);

                            finish();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginWithEmailActivity.this);
                            builder.setTitle(getString(R.string.wrong_login_information));
                            builder.setMessage(getString(R.string.check_login_info));
                            builder.setPositiveButton("OK", null);
                            builder.show();
                        }

                    }
                });
            }
        });
    }


    public void startAction() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.login_progress));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        com.appbuilder.sdk.android.Statics.closeMain = true;

        super.onBackPressed();// stub
    }
}
