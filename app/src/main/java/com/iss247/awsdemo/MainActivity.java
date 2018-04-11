package com.iss247.awsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                SignInUI signInUI = (SignInUI) AWSMobileClient.getInstance().getClient(MainActivity.this, SignInUI.class);
                signInUI.login(MainActivity.this, NextActivity.class).execute();
            }
        }).execute();
    }
}
