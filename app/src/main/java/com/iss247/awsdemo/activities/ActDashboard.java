package com.iss247.awsdemo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.iss247.awsdemo.R;
import com.iss247.awsdemo.services.PushListenerService;

public class ActDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public final String TAG = ActDashboard.class.getSimpleName();
    public static PinpointManager pinpointManager;
    private TextView mTextSessionDetails;
    private TextView mTextDeviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dashboard);
        intializeView();
        addAWSSignInStateListener();
        configurePinPoint();
        getAWSDeviceToken();
    }

    private void getAWSDeviceToken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String deviceToken =
                            InstanceID.getInstance(ActDashboard.this).getToken(
                                    "209550004762",
                                    GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                    Log.e("NotError device token ", deviceToken);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, " device token " + deviceToken);
                            mTextDeviceToken.setText("Device token : " + deviceToken);
                        }
                    });
                    pinpointManager.getNotificationClient()
                            .registerGCMDeviceToken(deviceToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void configurePinPoint() {
        PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                getApplicationContext(),
                AWSMobileClient.getInstance().getCredentialsProvider(),
                AWSMobileClient.getInstance().getConfiguration());

        pinpointManager = new PinpointManager(pinpointConfig);

        // Start a session with Pinpoint
        pinpointManager.getSessionClient().startSession();

        mTextSessionDetails.setText("Session started");


        AnalyticsEvent event =
                pinpointManager.getAnalyticsClient().createEvent("UserLoggedInEvent")
                        .withAttribute("screen", "dashboard");
//                        .withAttribute("DemoAttribute2", "DemoAttributeValue2")
//                        .withMetric("DemoMetric1", Math.random());

        pinpointManager.getAnalyticsClient().recordEvent(event);
    }

    private void addAWSSignInStateListener() {
        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            // Sign-in listener
            public void onUserSignedIn() {
                Log.d(TAG, "User Signed In");
            }

            // Sign-out listener
            @Override
            public void onUserSignedOut() {
                // Stop the session and submit the default app started event
                pinpointManager.getSessionClient().stopSession();
                pinpointManager.getAnalyticsClient().submitEvents();
                // return to the sign-in screen upon sign-out
                startActivity(new Intent(ActDashboard.this, ActAuthenticate.class));
                finish();
            }
        });
    }

    private void intializeView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mTextSessionDetails = findViewById(R.id.txtSessionDetail);
        mTextDeviceToken = findViewById(R.id.txtDeviceToken);

        mTextSessionDetails.setText("Session yet to start... please wait");
        mTextDeviceToken.setText("Device token not present... please wait");

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            IdentityManager.getDefaultIdentityManager().signOut();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received notification from local broadcast. Display it in a dialog.");


            Log.d(TAG, "Received notification from local broadcast. Display it in a dialog.");
            Bundle data = intent.getBundleExtra(PushListenerService.INTENT_SNS_NOTIFICATION_DATA);
            String message = PushListenerService.getMessage(data);

            new AlertDialog.Builder(ActDashboard.this)
                    .setTitle("Push notification")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    };

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
