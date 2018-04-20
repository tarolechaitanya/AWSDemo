package com.iss247.awsdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.regions.Regions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.iss247.awsdemo.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chaitanya-iss247 on 12/4/18.
 */

public class ActHome extends AppCompatActivity implements View.OnClickListener {

    private int RC_SIGN_IN = 109;
    private CardView mCardViewPinPoint;
    private CardView mCardViewPush;
    private CardView mCardViewDynamoDB;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton button = findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mCardViewPinPoint = findViewById(R.id.cardViewPinPoint);
        mCardViewPush = findViewById(R.id.cardViewPush);
        mCardViewDynamoDB = findViewById(R.id.cardViewDynamo);

        mCardViewPinPoint.setOnClickListener(this);
        mCardViewPush.setOnClickListener(this);
        mCardViewDynamoDB.setOnClickListener(this);

        intializeAWSMobileClient();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Log.w(" ActHome ", " authentication successful !!! ");
//            String token = account.getIdToken();
//            Log.w(" ActHome ", " id token " + token);

//            GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
//            AccountManager am = AccountManager.get(this);
//            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

//            String token = GoogleAuthUtil.getToken(getApplicationContext(), accounts[0].name,
//                    getString(R.string.server_client_id));

            String token = account.getIdToken();
            Map<String, String> logins = new HashMap<>();
            logins.put("accounts.google.com", token);


            CognitoCachingCredentialsProvider credentialsProvider =
                    new CognitoCachingCredentialsProvider(getApplicationContext(), "us-east-1:0d4f67bb-cf8c-4087-8297-e01739c1ed68", Regions.US_EAST_1);

            credentialsProvider.setLogins(logins);

//            credentialsProvider.refresh();


//            updateUI(account);
        } catch (Exception e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(" ActHome ", "signInResult:failed code=" + e);
//            updateUI(null);
        }
    }

    private void intializeAWSMobileClient() {
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("ActHome ", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cardViewPinPoint:
                startActivity(new Intent(ActHome.this, ActPinPoint.class));
                break;
            case R.id.cardViewPush:
                startActivity(new Intent(ActHome.this, ActPush.class));
                break;
            case R.id.cardViewDynamo:
                startActivity(new Intent(ActHome.this, ActDynamo.class));
                break;
            default:
                break;
        }
    }
}
