package com.thedeliveryapp.thedeliveryapp.deliverer;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.user.UserOrderDetailActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

public class DelivererOrderDetailActivity extends AppCompatActivity {

    private TextView category, description, orderId, min_range, max_range, userLocationName,
            userLocationLocation, userLocationPhoneNumber, expiryTime_Date, expiryTime_Time, deliveryCharge;
    private String date, time;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliverer_order_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        category = findViewById(R.id.category);
        description = findViewById(R.id.description);
        orderId = findViewById(R.id.orderId);
        min_range = findViewById(R.id.price_range_min);
        max_range = findViewById(R.id.price_range_max);
        userLocationName = findViewById(R.id.userLocationName);
        userLocationLocation = findViewById(R.id.userLocationLocation);
        userLocationPhoneNumber = findViewById(R.id.userLocationPhoneNumber);
        expiryTime_Date = findViewById(R.id.expiryTime_Date);
        expiryTime_Time = findViewById(R.id.expiryTime_Time);
        deliveryCharge = findViewById(R.id.delivery_charge);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        final OrderData myOrder = intent.getParcelableExtra("MyOrder");
        CollapsingToolbarLayout appBarLayout = findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(myOrder.category);
        }

        category.setText(myOrder.category);
        description.setText(myOrder.description);
        orderId.setText(myOrder.orderId + "");
        min_range.setText(myOrder.min_range + "");
        max_range.setText(myOrder.max_range + "");
        userLocationName.setText(myOrder.userLocation.Name);
        userLocationLocation.setText(myOrder.userLocation.Location);
        deliveryCharge.setText((myOrder.deliveryCharge+""));

        if (myOrder.userLocation.PhoneNumber.equals("")) {
            userLocationPhoneNumber.setText("-");
        } else {
            userLocationPhoneNumber.setText(myOrder.userLocation.PhoneNumber);
        }

        if (myOrder.expiryDate.day == 0) {
            date = "-";
        } else {
            date = myOrder.expiryDate.day + "/" + myOrder.expiryDate.month + "/" + myOrder.expiryDate.year;
        }
        expiryTime_Date.setText(date);


        if (myOrder.expiryTime.hour == -1) {
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
