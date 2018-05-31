package com.thedeliveryapp.thedeliveryapp.user;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView category, description, imageId, min_range, max_range, userLocationName,
            userLocationLocation, userLocationPhoneNumber, expiryTime_Date, expiryTime_Time;
    private String date, time;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        category = findViewById(R.id.category);
        description = findViewById(R.id.description);
        imageId = findViewById(R.id.imageId);
        min_range = findViewById(R.id.price_range_min);

        max_range = findViewById(R.id.price_range_max);
        userLocationName = findViewById(R.id.userLocationName);
        userLocationLocation = findViewById(R.id.userLocationLocation);
        userLocationPhoneNumber = findViewById(R.id.userLocationPhoneNumber);
        expiryTime_Date = findViewById(R.id.expiryTime_Date);
        expiryTime_Time = findViewById(R.id.expiryTime_Time);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        OrderData myOrder = intent.getParcelableExtra("MyOrder");
        CollapsingToolbarLayout appBarLayout = findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(myOrder.category);
        }


        category.setText(myOrder.category);
        description.setText(myOrder.description);
        imageId.setText(myOrder.imageId + "");
        min_range.setText(myOrder.min_range + "");
        max_range.setText(myOrder.max_range + "");
        userLocationName.setText(myOrder.userLocation.Name);
        userLocationLocation.setText(myOrder.userLocation.Location);


        if (myOrder.userLocation.PhoneNumber.equals("")) {
            userLocationPhoneNumber.setText("-");
        } else {
            userLocationPhoneNumber.setText(myOrder.userLocation.PhoneNumber);
        }


        if (myOrder.expiryDate == null) {
            date = "-";
        } else {
            date = myOrder.expiryDate.day + "/" + myOrder.expiryDate.month + "/" + myOrder.expiryDate.year;
        }
        expiryTime_Date.setText(date);


        if (myOrder.expiryTime == null) {
            time = "-";
        } else {
            if (myOrder.expiryTime.hour < 12) {
                time = myOrder.expiryTime.hour + ":" + myOrder.expiryTime.minute + " AM";
            } else {
                time = myOrder.expiryTime.hour + ":" + myOrder.expiryTime.minute + " PM";
            }
        }
        expiryTime_Time.setText(time);

    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {

            //navigateUpTo(new Intent(this, ItemListActivity.class));
            //return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

