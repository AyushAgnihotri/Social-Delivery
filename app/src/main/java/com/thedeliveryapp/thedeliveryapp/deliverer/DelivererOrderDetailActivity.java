package com.thedeliveryapp.thedeliveryapp.deliverer;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.thedeliveryapp.thedeliveryapp.login.SignupActivity;
import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;
import com.thedeliveryapp.thedeliveryapp.user.UserOrderDetailActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

public class DelivererOrderDetailActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private TextView category, description, orderId, min_range, max_range, userLocationName,
            userLocationLocation, userLocationPhoneNumber, expiryTime_Date, expiryTime_Time, deliveryCharge, status;
    private String date, time, userId, wal_bal;
    private Button btn_accept, btn_show_path, btn_mark_delivered;
    private DatabaseReference root, allorders, ref1, ref2, wallet_ref, deliverer;
    private UserDetails deliverer_data;
    private int wallet;

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
        checkConnection();
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
        status = findViewById(R.id.status);
        btn_accept = (Button) findViewById(R.id.btn_accept);
        btn_show_path = (Button) findViewById(R.id.btn_show_path);
        btn_mark_delivered = (Button) findViewById(R.id.btn_mark_delivered);

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

        if (myOrder.status.equals("FINISHED")) {
            btn_accept.setEnabled(false);
            btn_accept.setVisibility(View.GONE);
            btn_show_path.setEnabled(false);
            btn_show_path.setVisibility(View.GONE);
            btn_mark_delivered.setEnabled(false);
            btn_mark_delivered.setVisibility(View.GONE);
        } else if (myOrder.status.equals("ACTIVE")) {
            btn_accept.setText("Reject");
        } else {
            btn_mark_delivered.setEnabled(false);
            btn_mark_delivered.setVisibility(View.GONE);
        }


        category.setText(myOrder.category);
        description.setText(myOrder.description);
        status.setText(myOrder.status);
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

        final AlertDialog alertDialog = new AlertDialog.Builder(DelivererOrderDetailActivity.this)
                .setCancelable(false)
                .setTitle("Are you sure?")
                .setPositiveButton("Yes",null)
                .setNegativeButton("No",null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button yesButton = (alertDialog).getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                Button noButton = (alertDialog).getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Now Background Class To Update Operator State
                        alertDialog.dismiss();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        userId = user.getUid();

                        root = FirebaseDatabase.getInstance().getReference();
                        allorders = root.child("deliveryApp").child("orders");
                        allorders.keepSynced(true);

                        if (myOrder.status.equals("PENDING")) {
                            deliverer = root.child("deliveryApp").child("users").child(userId);
                            deliverer.keepSynced(true);
                            deliverer.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    deliverer_data = dataSnapshot.getValue(UserDetails.class);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            allorders.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot userdata : dataSnapshot.getChildren()) {
                                        if (userdata.getKey().equals(userId)) {
                                            continue;
                                        }
                                        for (DataSnapshot orderdata : userdata.getChildren()) {
                                            final OrderData order = orderdata.getValue(OrderData.class);
                                            if (order.orderId == myOrder.orderId) {
                                                final String user = userdata.getKey();

                                                /*
                                                wallet_ref = root.child("deliveryApp").child("users").child(user).child("wallet");
                                                wallet_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Integer wallet_balance = dataSnapshot.getValue(Integer.class);
                                                        //Toast.makeText(DelivererOrderDetailActivity.this, wallet_balance, Toast.LENGTH_SHORT).show();
                                                        System.out.println("wallet_balance " + wallet_balance);
                                                        wallet = wallet_balance;
                                                        System.out.println("wallet1 " + wallet);
                                                        root.child("deliveryApp").child("users").child(user).child("wallet").setValue(wallet-order.max_range);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                                */

                                                ref1 = orderdata.getRef();
                                                ref1.child("status").setValue("ACTIVE");
                                                btn_accept.setText("Reject");
                                                myOrder.status = "ACTIVE";
                                                status.setText((myOrder.status));
                                                btn_mark_delivered.setEnabled(true);
                                                btn_mark_delivered.setVisibility(View.VISIBLE);

                                                ref2 = ref1.child("acceptedBy");
                                                ref2.child("name").setValue(deliverer_data.name);
                                                ref2.child("email").setValue(deliverer_data.Email);
                                                ref2.child("mobile").setValue(deliverer_data.Mobile);
                                                ref2.child("alt_mobile").setValue(deliverer_data.Alt_Mobile);
                                                ref2.child("delivererID").setValue(userId);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else if (myOrder.status.equals("ACTIVE")) {

                            allorders.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot userdata : dataSnapshot.getChildren()) {
                                        if (userdata.getKey().equals(userId)) {
                                            continue;
                                        }
                                        for (DataSnapshot orderdata : userdata.getChildren()) {
                                            OrderData order = orderdata.getValue(OrderData.class);
                                            if (order.orderId == myOrder.orderId) {
                                                ref1 = orderdata.getRef();
                                                ref1.child("status").setValue("PENDING");

                                                btn_accept.setText("Accept");
                                                myOrder.status = "PENDING";
                                                status.setText((myOrder.status));
                                                btn_mark_delivered.setEnabled(false);
                                                btn_mark_delivered.setVisibility(View.GONE);

                                                ref2 = ref1.child("acceptedBy");
                                                ref2.child("name").setValue("-");
                                                ref2.child("email").setValue("-");
                                                ref2.child("mobile").setValue("-");
                                                ref2.child("alt_mobile").setValue("-");
                                                ref2.child("delivererID").setValue("-");
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                });

                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        btn_mark_delivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_accept.setEnabled(false);
                btn_accept.setVisibility(View.GONE);
                btn_mark_delivered.setText("Delivered");
                btn_mark_delivered.setBackgroundColor(Color.GREEN);
                btn_mark_delivered.setEnabled(false);
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
        CheckConnectivityMain.getInstance().setConnectivityListener(DelivererOrderDetailActivity.this);
    }

}
