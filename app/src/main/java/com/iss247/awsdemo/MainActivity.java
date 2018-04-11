package com.iss247.awsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;

public class MainActivity extends AppCompatActivity {

    public static PinpointManager pinpointManager;

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
        //TODO uncomment below code
//        PinpointConfiguration pinpointConfiguration = new PinpointConfiguration(getApplicationContext(),
//                AWSMobileClient.getInstance().getCredentialsProvider(),
//                AWSMobileClient.getInstance().getConfiguration());
//
//        pinpointManager = new PinpointManager(pinpointConfiguration);
//
//        pinpointManager.getSessionClient().startSession();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop the session and submit the default app started event
        if (pinpointManager != null) {
            pinpointManager.getSessionClient().stopSession();
            pinpointManager.getAnalyticsClient().submitEvents();
        }
    }
}
