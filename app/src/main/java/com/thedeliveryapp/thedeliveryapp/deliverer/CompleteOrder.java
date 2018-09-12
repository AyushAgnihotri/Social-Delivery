package com.thedeliveryapp.thedeliveryapp.deliverer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;

public class CompleteOrder extends AppCompatActivity {

    private OrderData myOrder;
    private EditText actual_price;
    private Button btn_send_otp;
    private String userId;
    private DatabaseReference root, ref1, ref2;

    int range = 9;
    int length = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);


        Intent intent = getIntent();
        myOrder = intent.getParcelableExtra("MyOrder");

        actual_price = findViewById(R.id.actual_price);
        btn_send_otp = findViewById(R.id.btn_send_otp);




        actual_price.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    btn_send_otp.setEnabled(true);
                    btn_send_otp.setBackground(getResources().getDrawable(R.drawable.button_round_main_green));
                } else {
                    btn_send_otp.setEnabled(false);
                    btn_send_otp.setBackground(getResources().getDrawable(R.drawable.button_round_main_gray));
                }
            }
        });

        btn_send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                } else {
                    String secret = generateSecureRandomNumber();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    userId = user.getUid();
                    root = FirebaseDatabase.getInstance().getReference();
                    ref1 = root.child("deliveryApp").child("orders").child(myOrder.userId).child(Integer.toString(myOrder.orderId)).child("otp");
                    ref1.keepSynced(true);
                    ref1.setValue(secret);
                    ref2 = root.child("deliveryApp").child("orders").child(myOrder.userId).child(Integer.toString(myOrder.orderId)).child("final_price");
                    ref2.keepSynced(true);
                    ref2.setValue(Integer.parseInt(actual_price.getText().toString()));
                    setUpOTPNotif(myOrder, secret);
                    Intent intent = new Intent(CompleteOrder.this, Otp_screen.class);
                    intent.putExtra("Final Price",(String) actual_price.getText().toString());
                    intent.putExtra("OTP",(String) secret);
                    intent.putExtra("MyOrder",(Parcelable) myOrder);
                    startActivity(intent);
                    finish();
                }
            }
        });

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

    private String generateSecureRandomNumber() {
        SecureRandom secureRandom = new SecureRandom();
        String s = "";
        for (int i = 0; i < length; i++) {
            int number = secureRandom.nextInt(range);
            if (number == 0 && i == 0) {
                i = -1;
                continue;
            }
            s = s + number;
        }
        return s;
    }

    public void setUpOTPNotif(final OrderData order, final String otp) {
        String userId = order.userId;
        root.child("deliveryApp").child("users").child(userId).child("playerId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String player_id = dataSnapshot.getValue(String.class);
                //TOAST
                try {
                    JSONObject notificationContent = new JSONObject("{'contents': {'en': '" + otp + "'}," +
                            "'include_player_ids': ['" + player_id + "'], " +
                            "'headings': {'en': 'Your OTP for Order Id : "+ order.orderId+"'} " +
                            "}");
                    JSONObject order = new JSONObject();
                    order.put("userId", myOrder.userId);
                    order.put("orderId", myOrder.orderId);
                    notificationContent.putOpt("data", order);
                    Log.i("JSONExample", "JSON parsed");
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
}
