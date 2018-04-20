package com.iss247.awsdemo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.iss247.awsdemo.R;

//import com.google.android.gms.gcm.GoogleCloudMessaging;
//import com.google.android.gms.iid.InstanceID;

/**
 * Created by chaitanya-iss247 on 12/4/18.
 */

public class ActPush extends AppCompatActivity {

    public static final String LOG_TAG = ActPush.class.getSimpleName();

    public static PinpointManager pinpointManager;
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Received notification from local broadcast. Display it in a dialog.");


            Log.d(LOG_TAG, "Received notification from local broadcast. Display it in a dialog.");
//            Bundle data = intent.getBundleExtra(PushListenerService.INTENT_SNS_NOTIFICATION_DATA);
//            String message = PushListenerService.getMessage(data);

//            new AlertDialog.Builder(ActPush.this)
//                    .setTitle("Push notification")
//                    .setMessage(message)
//                    .setPositiveButton(android.R.string.ok, null)
//                    .show();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_push);

        final TextView txtDeviceToken = findViewById(R.id.txtDeviceToken);
        txtDeviceToken.setText("Device token not available, waiting for registeration...");

        if (pinpointManager == null) {
            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    getApplicationContext(),
                    AWSMobileClient.getInstance().getCredentialsProvider(),
                    AWSMobileClient.getInstance().getConfiguration());

            pinpointManager = new PinpointManager(pinpointConfig);

            new Thread(new Runnable() {
                @Override
                public void run() {
//                    try {
//                        final String deviceToken =
//                                InstanceID.getInstance(ActPush.this).getToken(
//                                        "209550004762",
//                                        GoogleCloudMessaging.INSTANCE_ID_SCOPE);
//                        Log.e("NotError device token ", deviceToken);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                txtDeviceToken.setText(" Device token : " + deviceToken);
//                            }
//                        });
//                        pinpointManager.getNotificationClient()
//                                .registerGCMDeviceToken(deviceToken);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }).start();
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
//        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
//                new IntentFilter(PushListenerService.ACTION_PUSH_NOTIFICATION));
    }


}

