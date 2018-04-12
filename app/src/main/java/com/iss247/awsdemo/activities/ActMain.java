package com.iss247.awsdemo.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.iss247.awsdemo.R;

//import com.amazonaws.mobile.auth.ui.SignInUI;

public class ActMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
//                SignInUI signInUI = (SignInUI) AWSMobileClient.getInstance().getClient(ActMain.this, SignInUI.class);
//                signInUI.login(ActMain.this, ActNext.class).execute();
            }
        }).execute();
    }
}
