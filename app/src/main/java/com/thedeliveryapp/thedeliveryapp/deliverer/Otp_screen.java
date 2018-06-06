package com.thedeliveryapp.thedeliveryapp.deliverer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

public class Otp_screen extends AppCompatActivity {

    private EditText f1, f2, f3, f4, f5;
    private Button btn_mark_delivered;
    private String otp;
    private DatabaseReference root, ref1, ref2, ref3;
    private OrderData myOrder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_screen);

        Intent intent = getIntent();
        otp = intent.getStringExtra("OTP");
        myOrder = intent.getParcelableExtra("MyOrder");

        f1 = (EditText) findViewById(R.id.f1);
        f2 = (EditText) findViewById(R.id.f2);
        f3 = (EditText) findViewById(R.id.f3);
        f4 = (EditText) findViewById(R.id.f4);
        f5 = (EditText) findViewById(R.id.f5);
        btn_mark_delivered = (Button) findViewById(R.id.btn_mark_delivered);

        f1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f2.requestFocus();
                }
            }
        });

        f2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f3.requestFocus();
                }
            }
        });

        f3.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f4.requestFocus();
                }
            }
        });

        f4.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f5.requestFocus();
                }
            }
        });

        btn_mark_delivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String secret = f1.getText().toString() + f2.getText().toString() + f3.getText().toString()
                        + f4.getText().toString() + f5.getText().toString();
                if (secret.equals(otp)) {
                    root = FirebaseDatabase.getInstance().getReference();
                    root.child("deliveryApp").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild("our_wallet")) {
                                root.child("deliveryApp").child("our_wallet").setValue(0);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    ref1 = root.child("deliveryApp").child("orders").child(myOrder.userId).child(Integer.toString(myOrder.orderId));
                    ref1.keepSynced(true);
                    ref1.child("status").setValue("FINISHED");

                    ref2 = ref1.child("acceptedBy").child("delivererID");
                    ref2.keepSynced(true);
                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String delivererID = dataSnapshot.getValue(String.class);
                            ref3 = root.child("deliveryApp").child("users").child(delivererID);
                            ref3.keepSynced(true);
                            ref3.child("wallet").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Integer wal_bal = dataSnapshot.getValue(Integer.class);
                                    int balance = wal_bal;
                                    ref3.child("wallet").setValue(balance + myOrder.max_range);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    Toast.makeText(Otp_screen.this, "Delivery Finished!!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(Otp_screen.this, DelivererViewActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(Otp_screen.this, "Wrong OTP! Enter correct OTP", Toast.LENGTH_LONG).show();
                }
            }
        });

    }                               

}
