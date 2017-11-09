package com.appbuilder.core.LoginScreen;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.appbuilder.core.LoginScreen.service.LoginScreenService;
import com.appbuilder.core.LoginScreen.service.LoginSettings;
import com.appbuilder.core.LoginScreen.service.LoginSettingsService;
import com.appbuilder.core.LoginScreen.service.OnDone;
import com.appbuilder.core.R;
import com.appbuilder.sdk.android.LoginScreen;

/**
 * Created by Artem on 11.02.14.
 */
public class RegisterEmailAccountActivity extends Activity implements View.OnClickListener {
    EditText usernameText;
    EditText passwordText;
    Button signupButton;
    LoginScreen loginScreen;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_email_account_layout);

        loginScreen = (LoginScreen) getIntent().getSerializableExtra("loginScreen");

        usernameText = (EditText) findViewById(R.id.login_screen_signup_username);
        passwordText = (EditText) findViewById(R.id.login_screen_signup_password);

        signupButton = (Button) findViewById(R.id.login_screen_signup_button);
        signupButton.setOnClickListener(this);

        usernameText.addTextChangedListener(getWatcher());
        passwordText.addTextChangedListener(getWatcher());

        signupButton.setEnabled(validate());

    }

    private TextWatcher getWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                signupButton.setEnabled(validate());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }


    public boolean validate() {
        Boolean result;

        try {
            result = (usernameText.getText().length() > 0) && (passwordText.getText().length() > 0);
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_screen_login_button) {
            String password = passwordText.getText().toString();
            String username = usernameText.getText().toString();

            LoginScreenService.doCreateAccount(loginScreen.getSignupEndpoint(), username, password,
                    loginScreen.getAppId(), new OnDone() {
                @Override
                public void onDone(final int result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result == 200) {
                                Toast.makeText(RegisterEmailAccountActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(RegisterEmailAccountActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        }
    }
}