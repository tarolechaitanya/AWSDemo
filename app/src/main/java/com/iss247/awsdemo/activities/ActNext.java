package com.iss247.awsdemo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.iss247.awsdemo.R;
import com.iss247.awsdemo.services.PushListenerService;

/**
 * Created by chaitanya-iss247 on 11/4/18.
 */

public class ActNext extends AppCompatActivity {

    public static PinpointManager pinpointManager;
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ActNext ", "Received notification from local broadcast. Display it in a dialog.");

            Bundle data = intent.getBundleExtra(PushListenerService.INTENT_SNS_NOTIFICATION_DATA);
            String message = PushListenerService.getMessage(data);

            new AlertDialog.Builder(ActNext.this)
                    .setTitle("Push notification")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        if (pinpointManager == null) {
            PinpointConfiguration pinpointConfiguration = new PinpointConfiguration(getApplicationContext(),
                    AWSMobileClient.getInstance().getCredentialsProvider(),
                    AWSMobileClient.getInstance().getConfiguration());

            pinpointManager = new PinpointManager(pinpointConfiguration);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String deviceToken = InstanceID.getInstance(ActNext.this).getToken("209550004762", GoogleCloudMessaging.INSTANCE_ID_SCOPE);

                        Log.e("NotError deviceToken: ", deviceToken);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            pinpointManager.getSessionClient().startSession();
        }
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

    @Override
    protected void onPause() {
        super.onPause();

        // unregister notification receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register notification receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                new IntentFilter(PushListenerService.ACTION_PUSH_NOTIFICATION));
    }
}
