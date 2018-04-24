package com.iss247.awsdemo.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.iss247.awsdemo.GlideApp;
import com.iss247.awsdemo.R;
import com.iss247.awsdemo.models.NewsDO;
import com.iss247.awsdemo.services.PushListenerService;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class ActDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static PinpointManager pinpointManager;
    public final String TAG = ActDashboard.class.getSimpleName();
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
    private int notifyID = 1;
    private int transferID;
    private TextView mTextSessionDetails;
    private TextView mTextDeviceToken;
    private TextView mTextDynamoData;
    private TextView mTextPlayVideo;
    private ImageView mImageViewS3;
    private ImageView mImageViewUploadToS3;
    private TransferUtility transferUtility;
    private Notification.Builder notificationBuilder;
    private Notification notification;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dashboard);
        intializeView();
        addAWSSignInStateListener();
        configurePinPoint();
        getAWSDeviceToken();
        loadDynamoDBData();
    }

    private void loadDynamoDBData() {
        // Instantiate a AmazonDynamoDBMapperClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        new Thread(() -> {


//            NewsDO newsItem = dynamoDBMapper.load(
//                    NewsDO.class,
//                    IdentityManager.getDefaultIdentityManager().getCachedUserID(),
//                    "Article1");

            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

            PaginatedScanList<NewsDO> paginatedq = dynamoDBMapper.scan(NewsDO.class, scanExpression);
            ArrayList<String> newsItemList = new ArrayList<>();

            for (NewsDO q : paginatedq) {
                newsItemList.add(q.getArticleId());
            }

            ListView listView = findViewById(R.id.list);

            String[] values = newsItemList.toArray(new String[newsItemList.size()]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.activity_listview, values);

            runOnUiThread(() -> listView.setAdapter(adapter));

            // Item read
            Log.d("News Item:", newsItemList.toString());

        }).start();

    }

    private void getAWSDeviceToken() {
        new Thread(() -> {
            try {
                final String deviceToken =
                        InstanceID.getInstance(ActDashboard.this).getToken(
                                "209550004762",
                                GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                Log.e("NotError device token ", deviceToken);
                runOnUiThread(() -> {
                    Log.e(TAG, " device token " + deviceToken);
                    mTextDeviceToken.setText(String.format(getString(R.string.device_token), deviceToken));
                });
                pinpointManager.getNotificationClient()
                        .registerDeviceToken(deviceToken);
            } catch (Exception e) {
                e.printStackTrace();
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

        mTextSessionDetails.setText(getString(R.string.session_started));


        AnalyticsEvent event =
                pinpointManager.getAnalyticsClient().createEvent("UserLoggedInEvent")
                        .withAttribute("screen", "dashboard");

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mTextSessionDetails = findViewById(R.id.txtSessionDetail);
        mTextDeviceToken = findViewById(R.id.txtDeviceToken);
        mTextDynamoData = findViewById(R.id.txtDynamoData);
        mTextPlayVideo = findViewById(R.id.txtViewOpenS3);

        mTextPlayVideo.setOnClickListener(this);

        mTextSessionDetails.setText(getString(R.string.session_yet_to_start));
        mTextDeviceToken.setText(getString(R.string.token_not_present));
        mTextDynamoData.setText("Dynamo DB data will be displayed here ... ");

        Button buttonDownloadImage = findViewById(R.id.btnDownloadImage);
        Button buttonUploadImage = findViewById(R.id.btnUploadImage);
        Button buttonDownloadVideo = findViewById(R.id.btnDownloadVideo);
        Button buttonUploadData = findViewById(R.id.btnUploadData);

        buttonDownloadImage.setOnClickListener(this);
        buttonDownloadVideo.setOnClickListener(this);
        buttonUploadImage.setOnClickListener(this);
        buttonUploadData.setOnClickListener(this);

        mImageViewS3 = findViewById(R.id.imgViewS3);
        mImageViewUploadToS3 = findViewById(R.id.imgViewUploadS3);

    }

    private void downloadWithTransferUtility() {

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Image from S3");
        progress.setMessage("Downloading image please wait...");
        progress.setIndeterminate(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        TransferObserver transferObserver =
                transferUtility.download(
                        "public/video1.mp4",
                        new File(getExternalFilesDir(null) + "/files/video1.mp4"));

        // Attach a listener to the observer to get state update and progress notifications
        transferObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload
                    progress.setProgress(100);
                    progress.dismiss();
                    setImage();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
                progress.setProgress(percentDone);
                Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        transferID = transferObserver.getId();

        Log.d(TAG, "Bytes Transferrred: " + transferObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + transferObserver.getBytesTotal());
    }

    private void setImage() {
        File file = new File(getExternalFilesDir(null) + "/files/mario.jpg");
        GlideApp
                .with(ActDashboard.this)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageViewS3);

        GlideApp
                .with(ActDashboard.this)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageViewUploadToS3);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            IdentityManager.getDefaultIdentityManager().signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (transferUtility != null && transferID != 0) {
            transferUtility.pause(transferID);
        }
        // unregister notification receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (transferUtility != null && transferID != 0) {
            transferUtility.resume(transferID);
        }
        // register notification receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                new IntentFilter(PushListenerService.ACTION_PUSH_NOTIFICATION));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDownloadImage:
                downloadWithTransferUtility();
                break;
            case R.id.btnUploadImage:
                uploadWithTransferUtility();
                break;
            case R.id.btnDownloadVideo:
                downloadVideoWithTransferUtility();
                break;
            case R.id.txtViewOpenS3:
                playVideo();
                break;
            case R.id.btnUploadData:
                addNewArticle();
                break;
            default:
                break;
        }
    }

    private void playVideo() {
        File file = new File(getExternalFilesDir(null) + "/files/samplevideo.mp4");
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(file.getAbsolutePath()));
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "video/*");
            startActivity(intent);
        } else {
            CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
            Snackbar.make(coordinatorLayout, "Please download video file to play it", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void downloadVideoWithTransferUtility() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Video from S3");
        progress.setMessage("Downloading video please wait...");
        progress.setIndeterminate(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        TransferObserver transferObserver =
                transferUtility.download(
                        "public/samplevideo.mp4",
                        new File(getExternalFilesDir(null) + "/files/samplevideo.mp4"));

        // Attach a listener to the observer to get state update and progress notifications
        transferObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload
                    progress.setProgress(100);
                    progress.dismiss();
                    playVideo();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
                progress.setProgress(percentDone);
                Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        transferID = transferObserver.getId();

        Log.d(TAG, "Bytes Transferrred: " + transferObserver.getBytesTransferred());
    }

    private void addNewArticle() {
        // Instantiate a AmazonDynamoDBMapperClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();


        final NewsDO newsItem = new NewsDO();

        newsItem.setUserId(IdentityManager.getDefaultIdentityManager().getCachedUserID());

        newsItem.setArticleId("Article" + new Random().nextInt());
        newsItem.setContent("This is the article content");
        newsItem.setTitle("Modi is good or bad?");

        new Thread(() -> {
            dynamoDBMapper.save(newsItem);
            // Item saved
            loadDynamoDBData();
        }).start();
    }

    private void uploadWithTransferUtility() {

        TransferUtility uploadTransferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        File file = new File(getExternalFilesDir(null) + "/files/mario.jpg");
        if (file.exists()) {
            TransferObserver uploadObserver =
                    uploadTransferUtility.upload(
                            "uploads/sample_upload.jpg", file);

            // Attach a listener to the observer to get state update and progress notifications
            uploadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        updateProgress(100);
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;
                    updateProgress(percentDone);
                    Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                            + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    // Handle errors
                }

            });

            Log.d(TAG, "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
            Log.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());

            showNotification();
        }
    }

    private void updateProgress(int percentDone) {
        if (notificationBuilder != null) {
            notificationBuilder.setProgress(100, percentDone, false);

            if (percentDone == 100) {
                notification = notificationBuilder
                        .setOngoing(false)
                        .setContentText("File uploaded successfully")
                        .build();
            } else {
                notification = notificationBuilder
                        .setOngoing(true)
                        .build();
            }

            notificationManager.notify(notifyID, notification);
        }
    }

    private void showNotification() {

        String channelId = "my_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification and set the notification channel.
            notificationBuilder = new Notification.Builder(ActDashboard.this, channelId)
                    .setOngoing(true)
                    .setContentTitle("Upload to S3")
                    .setSmallIcon(R.drawable.ic_adb_black_24dp)
                    .setProgress(100, 0, false)
                    .setContentText("Uploading image to S3, please wait...");
        } else {
            notificationBuilder = new Notification.Builder(ActDashboard.this)
                    .setOngoing(true)
                    .setContentTitle("Upload to S3")
                    .setSmallIcon(R.drawable.ic_adb_black_24dp)
                    .setProgress(100, 0, false)
                    .setContentText("Uploading image to S3, please wait...");
        }

        notification = notificationBuilder.build();

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Test Channel";
            // Sets an ID for the notification, so it can be updated.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        // Issue the notification.
        if (notificationManager != null) {
            notificationManager.notify(notifyID, notification);
        }
    }
}
