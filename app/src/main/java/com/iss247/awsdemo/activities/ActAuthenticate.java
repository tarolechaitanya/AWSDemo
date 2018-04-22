package com.iss247.awsdemo.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

/*
 * Created by chaitanya on 21/4/18.
 */

public class ActAuthenticate extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intializeAWSMobileClient();
    }

    private void intializeAWSMobileClient() {
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("ActHome ", "AWSMobileClient is instantiated and you are connected to AWS!");

                AuthUIConfiguration config =
                        new AuthUIConfiguration.Builder()
                                .userPools(true)  // true? show the Email and Password UI
//                                .signInButton(FacebookButton.class) // Show Facebook button
//                                .signInButton(GoogleButton.class) // Show Google button
//                                .logoResId(R.drawable.mylogo) // Change the logo
                                .backgroundColor(Color.CYAN) // Change the backgroundColor
                                .isBackgroundColorFullScreen(false) // Full screen backgroundColor the backgroundColor full screenff
                                .fontFamily("sans-serif-light") // Apply sans-serif-light as the global font
                                .canCancel(true)
                                .build();

                SignInUI signinUI = (SignInUI) AWSMobileClient.getInstance().getClient(ActAuthenticate.this, SignInUI.class);
                signinUI.login(ActAuthenticate.this, ActDashboard.class).authUIConfiguration(config).execute();
            }
        }).execute();
    }
}
