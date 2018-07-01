package com.thedeliveryapp.thedeliveryapp.deliverer;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.CheckConnectivityMain;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.login.SignupActivity;
import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;
import com.thedeliveryapp.thedeliveryapp.user.UserOrderDetailActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DelivererOrderDetailActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    private LinearLayout userName_h;
    private TextView category, description, orderId, min_range, max_range, userName, userPhoneNumber, userLocationName,
            userLocationLocation, expiryTime_Date, expiryTime_Time, final_item_price, deliveryCharge, final_total, status, mop;
    private String date, time, userId;
    private Button btn_accept, btn_show_path, btn_mark_delivered, btn_complete_order;
    private DatabaseReference root, ref1, ref2, ref3, wallet_ref, deliverer, forUserData;
    private UserDetails deliverer_data;
    public OrderData myOrder;
    private int balance;
    private GoogleMap googleMap;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private UserDetails userDetails = new UserDetails();

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
        root = FirebaseDatabase.getInstance().getReference();
        category = findViewById(R.id.category);
        description = findViewById(R.id.description);
        orderId = findViewById(R.id.orderId);
        min_range = findViewById(R.id.price_range_min);
        max_range = findViewById(R.id.price_range_max);
        userName_h = (LinearLayout) findViewById(R.id.userName_h);
        userName = findViewById(R.id.userName);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);
        userLocationName = findViewById(R.id.userLocationName);
        userLocationLocation = findViewById(R.id.userLocationLocation);
        expiryTime_Date = findViewById(R.id.expiryTime_Date);
        expiryTime_Time = findViewById(R.id.expiryTime_Time);
        final_item_price = findViewById(R.id.final_item_price);
        deliveryCharge = findViewById(R.id.deliveryCharge);
        final_total = findViewById(R.id.final_total);
        status = findViewById(R.id.status);
        btn_accept = (Button) findViewById(R.id.btn_accept);
        btn_show_path = (Button) findViewById(R.id.btn_show_path);
        btn_complete_order = (Button) findViewById(R.id.btn_complete_order);
        mop = findViewById(R.id.mop);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        myOrder = intent.getParcelableExtra("MyOrder");
        CollapsingToolbarLayout appBarLayout = findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(myOrder.category);
        }

        if (myOrder.status.equals("FINISHED")) {
            btn_accept.setEnabled(false);
            btn_accept.setVisibility(View.GONE);
            btn_show_path.setEnabled(false);
            btn_show_path.setVisibility(View.GONE);
            btn_complete_order.setEnabled(false);
            btn_complete_order.setVisibility(View.GONE);
        } else if (myOrder.status.equals("ACTIVE")) {
            btn_accept.setText("Reject");
        } else {
            btn_complete_order.setEnabled(false);
            btn_complete_order.setVisibility(View.GONE);
        }

        if (myOrder.status.equals("PENDING")) {
            userName_h.setVisibility(View.GONE);
        }


        category.setText(myOrder.category);
        description.setText(myOrder.description);
        status.setText(myOrder.status);
        orderId.setText(myOrder.orderId + "");
        min_range.setText(myOrder.min_range + "");
        max_range.setText(myOrder.max_range + "");
        fetchUserDetails();
        userLocationName.setText(myOrder.userLocation.Name);
        userLocationLocation.setText(myOrder.userLocation.Location);
        deliveryCharge.setText((myOrder.deliveryCharge+""));
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
                        userId = user.getUid();

                        ref1 = root.child("deliveryApp").child("orders").child(myOrder.userId).child(Integer.toString(myOrder.orderId));
                        ref1.keepSynced(true);
                        ref2 = ref1.child("acceptedBy");
                        ref2.keepSynced(true);

                        if (myOrder.status.equals("PENDING")) {
                            deliverer = root.child("deliveryApp").child("users").child(userId);
                            deliverer.keepSynced(true);

                            deliverer.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    deliverer_data = dataSnapshot.getValue(UserDetails.class);
                                    ref2.child("name").setValue(deliverer_data.name);
                                    ref2.child("email").setValue(deliverer_data.Email);
                                    ref2.child("mobile").setValue(deliverer_data.Mobile);
                                    ref2.child("alt_mobile").setValue(deliverer_data.Alt_Mobile);
                                    ref2.child("delivererID").setValue(userId);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            ref1.child("status").setValue("ACTIVE");
                            btn_accept.setText("Reject");

                            myOrder.status = "ACTIVE";
                            status.setText((myOrder.status));
                            setUpAcceptNotif(myOrder);

                            /*
                            // Deducts max_int money from orderer's wallet when order accepted
                            wallet_ref = root.child("deliveryApp").child("users").child(myOrder.userId).child("wallet");
                            wallet_ref.keepSynced(true);
                            wallet_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Integer wal_bal = dataSnapshot.getValue(Integer.class);
                                    balance = wal_bal;
                                    wallet_ref.setValue(balance-myOrder.max_range);
                                    setUpAcceptNotif(myOrder);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            */

                            btn_complete_order.setEnabled(true);
                            btn_complete_order.setVisibility(View.VISIBLE);
                            userName_h.setVisibility(View.VISIBLE);


                        } else if (myOrder.status.equals("ACTIVE")) {

                            ref1.child("status").setValue("PENDING");

                            btn_accept.setText("Accept");
                            myOrder.status = "PENDING";
                            status.setText((myOrder.status));

                            ref2.child("name").setValue("-");
                            ref2.child("email").setValue("-");
                            ref2.child("mobile").setValue("-");
                            ref2.child("alt_mobile").setValue("-");
                            ref2.child("delivererID").setValue("-");

                            ref1.child("otp").setValue("");
                            ref1.child("final_price").setValue(-1);

                            btn_complete_order.setEnabled(false);
                            btn_complete_order.setVisibility(View.GONE);
                            userName_h.setVisibility(View.GONE);
                            setUpRejectNotif(myOrder);
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

        btn_show_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO implement show path
                String data=myOrder.userLocation.Name;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+data));
                intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });


        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        btn_complete_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                } else {
                    Intent intent = new Intent(DelivererOrderDetailActivity.this, CompleteOrder.class);
                    intent.putExtra("MyOrder",(Parcelable) myOrder);
                    startActivity(intent);
                }
            }
        });

    }

    void fetchUserDetails() {
        forUserData = root.child("deliveryApp").child("users").child(myOrder.userId);
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

    public void setUpAcceptNotif(final OrderData order) {
        String userId = order.userId;
        root.child("deliveryApp").child("users").child(userId).child("playerId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String player_id = dataSnapshot.getValue(String.class);
                //TOAST
                try {
                    JSONObject notificationContent = new JSONObject("{'contents': {'en': '"+ order.description +"'}," +
                            "'include_player_ids': ['" + player_id + "'], " +
                            "'headings': {'en': 'woah ! your order just got accepted'} " +
                            "}");
                    JSONObject order = new JSONObject();
                    order.put("userId",myOrder.userId);
                    order.put("orderId",myOrder.orderId);
                    notificationContent.putOpt("data",order);
                    OneSignal.postNotification(notificationContent, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public void setUpRejectNotif(final OrderData order) {
        String userId = order.userId;
        root.child("deliveryApp").child("users").child(userId).child("playerId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String player_id = dataSnapshot.getValue(String.class);
                //TOAST
                try {
                    JSONObject notificationContent = new JSONObject("{'contents': {'en': '" + order.description + "'}," +
                            "'include_player_ids': ['" + player_id + "'], " +
                            "'headings': {'en': 'oops ! your order got rejected , order id : "+order.orderId+"'} " +
                            "}");
                    JSONObject order = new JSONObject();
                    order.put("userId", myOrder.userId);
                    order.put("orderId", myOrder.orderId);
                    notificationContent.putOpt("data", order);
                    OneSignal.postNotification(notificationContent, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
        CheckConnectivityMain.getInstance().setConnectivityListener(DelivererOrderDetailActivity.this);
    }


}
