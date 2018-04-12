package com.iss247.awsdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.iss247.awsdemo.R;

/**
 * Created by chaitanya-iss247 on 12/4/18.
 */

public class ActHome extends AppCompatActivity implements View.OnClickListener {

    private CardView mCardViewPinPoint;
    private CardView mCardViewPush;
    private CardView mCardViewDynamoDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home);
        mCardViewPinPoint = findViewById(R.id.cardViewPinPoint);
        mCardViewPush = findViewById(R.id.cardViewPush);
        mCardViewDynamoDB = findViewById(R.id.cardViewDynamo);

        mCardViewPinPoint.setOnClickListener(this);
        mCardViewPush.setOnClickListener(this);
        mCardViewDynamoDB.setOnClickListener(this);

        intializeAWSMobileClient();
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
