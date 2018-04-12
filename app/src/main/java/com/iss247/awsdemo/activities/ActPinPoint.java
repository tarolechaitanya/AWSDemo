package com.iss247.awsdemo.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.iss247.awsdemo.R;

/**
 * Created by chaitanya-iss247 on 12/4/18.
 */

public class ActPinPoint extends AppCompatActivity {

    private PinpointManager pinpointManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pin_point);

        TextView mTxtStartSession = findViewById(R.id.txtStartSession);
        TextView mTxtStopSession = findViewById(R.id.txtStopSession);

        PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                getApplicationContext(),
                AWSMobileClient.getInstance().getCredentialsProvider(),
                AWSMobileClient.getInstance().getConfiguration());

        pinpointManager = new PinpointManager(pinpointConfig);
        // Start a session with Pinpoint
        pinpointManager.getSessionClient().startSession();

        mTxtStartSession.setText("Session start event sent to AWS");
        mTxtStopSession.setText("Session stop event will be sent to AWS after app is closed");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pinpointManager != null) {
            // Stop the session and submit the default app started event
            pinpointManager.getSessionClient().stopSession();
            pinpointManager.getAnalyticsClient().submitEvents();
        }
    }
}
