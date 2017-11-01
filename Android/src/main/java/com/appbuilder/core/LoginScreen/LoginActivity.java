package com.appbuilder.core.LoginScreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appbuilder.core.R;
import com.appbuilder.sdk.android.LoginScreen;

public class LoginActivity extends Activity implements View.OnClickListener {
    public LoginScreen ls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_screen_layout);

        LinearLayout loginTwitterButton = (LinearLayout) findViewById(R.id.login_screen_twitter_button);
        LinearLayout loginFacebookButton = (LinearLayout) findViewById(R.id.login_screen_facebook_button);
        LinearLayout loginEmailButton = (LinearLayout) findViewById(R.id.login_screen_email_button);

        TextView createEmailAccountButton = (TextView) findViewById(R.id.login_screen_create_new_account_button);
        LinearLayout createNewAccountLayout = (LinearLayout) findViewById(R.id.login_screen_create_new_account_layout);

        Intent intent = getIntent();
        ls = (LoginScreen) intent.getSerializableExtra("loginScreen");

        if (!ls.getAllowSignup()) createNewAccountLayout.setVisibility(View.GONE);
        if (!ls.getUseEmail()) loginEmailButton.setVisibility(View.GONE);
        if (ls.getUseFacebook()) loginFacebookButton.setVisibility(View.GONE);
        if (ls.getUseTwitter()) loginTwitterButton.setVisibility(View.GONE);

        loginEmailButton.setOnClickListener(this);
        loginTwitterButton.setOnClickListener(this);
        loginFacebookButton.setOnClickListener(this);
        createEmailAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_screen_twitter_button) {

        } else if (view.getId() == R.id.login_screen_facebook_button) {

        } else if (view.getId() == R.id.login_screen_email_button) {
            Intent intent = new Intent(this, LoginWithEmailActivity.class);
            intent.putExtra("loginScreen", ls);
            startActivity(intent);
        } else if (view.getId() == R.id.login_screen_create_new_account_button) {
            Intent intent = new Intent(this, RegisterEmailAccountActivity.class);
            intent.putExtra("loginScreen", ls);
            startActivity(intent);
        }
    }
}
