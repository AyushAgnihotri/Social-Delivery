package com.thedeliveryapp.thedeliveryapp.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.CheckConnectivityMain;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;
import com.thedeliveryapp.thedeliveryapp.order_form.EditOrderForm;
import com.thedeliveryapp.thedeliveryapp.order_form.OrderForm;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

public class UserOrderDetailActivity extends AppCompatActivity  implements ConnectivityReceiver.ConnectivityReceiverListener{

    private TextView category, description, orderId, min_range, max_range, userName, userPhoneNumber, userLocationName,
            userLocationLocation, expiryTime_Date, expiryTime_Time, final_item_price, deliveryCharge, final_total, status, otp_h, otp, mop;
    private String date, time, deliverer_details, userID;
    Button acceptedBy;
    private DatabaseReference root, forUserData;
    private UserDetails userDetails = new UserDetails();

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
        checkConnection();

        category = findViewById(R.id.category);
        description = findViewById(R.id.description);
        orderId = findViewById(R.id.orderId);
        min_range = findViewById(R.id.price_range_min);
        max_range = findViewById(R.id.price_range_max);
        userName = findViewById(R.id.userName);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);
        userLocationName = findViewById(R.id.userLocationName);
        userLocationLocation = findViewById(R.id.userLocationLocation);
        expiryTime_Date = findViewById(R.id.expiryTime_Date);
        expiryTime_Time = findViewById(R.id.expiryTime_Time);
        final_item_price = findViewById(R.id.final_item_price);
        deliveryCharge = findViewById(R.id.deliveryCharge);
        final_total = findViewById(R.id.final_total);
        acceptedBy = findViewById(R.id.btn_accepted_by);
        status = findViewById(R.id.status);
        otp_h = findViewById(R.id.otp_h);
        otp = findViewById(R.id.otp);
        mop = findViewById(R.id.mop);

        FloatingActionButton fab = findViewById(R.id.fab);

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

        otp_h.setVisibility(View.GONE);
        otp.setVisibility(View.GONE);

        if (myOrder.status.equals("PENDING")) {
            fab.setVisibility(View.VISIBLE);
        }

        if (myOrder.status.equals("PENDING") || myOrder.status.equals("CANCELLED") || myOrder.status.equals("EXPIRED")) {
            acceptedBy.setEnabled(false);
            acceptedBy.setVisibility(View.GONE);
        } else if (myOrder.status.equals("FINISHED")) {
            acceptedBy.setText("Delivered By");
        }

        if (!myOrder.otp.equals("")) {
            otp_h.setVisibility(View.VISIBLE);
            otp.setVisibility(View.VISIBLE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                } else {
                    Intent intent = new Intent(UserOrderDetailActivity.this, EditOrderForm.class);
                    intent.putExtra("MyOrder",(Parcelable) myOrder);
                    startActivity(intent);
                    finish();
                }
            }
        });

        deliverer_details = "Name: \t\t\t" + myOrder.acceptedBy.name + "\nMobile: \t\t\t" + myOrder.acceptedBy.mobile;
        if (!myOrder.acceptedBy.alt_mobile.equals("")) {
            deliverer_details += "\nAlt. Mobile: \t\t\t" + myOrder.acceptedBy.alt_mobile;
        }
        deliverer_details += "\nE-mail: \t\t\t" + myOrder.acceptedBy.email;
        acceptedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(UserOrderDetailActivity.this)
                        .setTitle("Deliverer Details")
                        .setMessage(deliverer_details)
                        .setPositiveButton(getString(R.string.dialog_ok), null)
                        .show();
            }
        });

        category.setText(myOrder.category);
        description.setText(myOrder.description);
        status.setText((myOrder.status));
        orderId.setText(myOrder.orderId + "");
        min_range.setText(myOrder.min_range + "");
        max_range.setText(myOrder.max_range + "");
        fetchUserDetails();
        userLocationName.setText(myOrder.userLocation.Name);
        userLocationLocation.setText(myOrder.userLocation.Location);
        deliveryCharge.setText((myOrder.deliveryCharge+""));
        otp.setText(myOrder.otp);
        mop.setText(myOrder.mode_of_payment);

        if (myOrder.final_price == -1) {
            final_item_price.setText("- - - - -");
            final_total.setText("- - - - -");
        } else {
            final_item_price.setText(myOrder.final_price+"");
            final_total.setText((myOrder.deliveryCharge + myOrder.final_price)+"");
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
            if ((Integer.toString(myOrder.expiryTime.hour)).length() == 1) {
                time = "0" + myOrder.expiryTime.hour;
            } else {
                time = myOrder.expiryTime.hour+"";
            }
            time += ":";
            if ((Integer.toString(myOrder.expiryTime.minute)).length() == 1) {
                time += "0" + (Integer.toString(myOrder.expiryTime.minute));
            } else {
                time += myOrder.expiryTime.minute+"";
            }

            if (myOrder.expiryTime.hour < 12) {
                time += " AM";
            } else {
                time += " PM";
            }
        }
        expiryTime_Time.setText(time);

    }

    void fetchUserDetails() {
        root = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        forUserData = root.child("deliveryApp").child("users").child(userID);
        forUserData.keepSynced(true);
        forUserData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDetails = dataSnapshot.getValue(UserDetails.class);
                userName.setText(userDetails.name);
                userPhoneNumber.setText(userDetails.Mobile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {

            //navigateUpTo(new Intent(this, ItemListActivity.class));
            //return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if(!isConnected)
            showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }


    @Override
    protected void onResume() {
        super.onResume();
        CheckConnectivityMain.getInstance().setConnectivityListener(UserOrderDetailActivity.this);
    }

}

