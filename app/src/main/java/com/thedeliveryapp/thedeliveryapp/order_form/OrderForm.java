package com.thedeliveryapp.thedeliveryapp.order_form;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.CheckConnectivityMain;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.login.ResetPasswordActivity;
import com.thedeliveryapp.thedeliveryapp.user.UserViewActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.AcceptedBy;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryDate;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryTime;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;
import com.thedeliveryapp.thedeliveryapp.user.order.UserLocation;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OrderForm extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    TextView category ;
    Button date_picker, time_picker, user_location;
    Calendar calendar ;
    EditText description, min_int_range, max_int_range, delivery_charge;

    private DatabaseReference root, deliveryApp, wallet_ref;
    private String userId, TAG, otp;
    private int OrderNumber, order_id, balance;
    int flag;
    UserLocation userLocation = null;
    ExpiryTime expiryTime = null;
    ExpiryDate expiryDate = null;
    AcceptedBy acceptedBy = null;
    OrderData order;

    int PLACE_PICKER_REQUEST =1;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_form);

        checkConnection();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("New Order");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        acceptedBy = new AcceptedBy("-", "-", "-", "-", "-");
        otp = "";

        category = findViewById(R.id.btn_category);
        date_picker = findViewById(R.id.btn_date_picker);
        time_picker = findViewById(R.id.btn_time_picker);
        calendar = Calendar.getInstance();
        description = findViewById(R.id.description_of_order);
        min_int_range = findViewById(R.id.min_int);
        max_int_range = findViewById(R.id.max_int);
        user_location = findViewById(R.id.user_location);
        delivery_charge = findViewById(R.id.delivery_charge);


        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> mcategories = new ArrayList<String>();
                mcategories.add("Food");
                mcategories.add("Medicine");
                mcategories.add("Household");
                mcategories.add("Electronics");
                mcategories.add("Toiletries");
                mcategories.add("Books");
                mcategories.add("Clothing");
                mcategories.add("Shoes");
                mcategories.add("Sports");
                mcategories.add("Games");
                mcategories.add("Others");
                //Create sequence of items
                final CharSequence[] Categories = mcategories.toArray(new String[mcategories.size()]);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(OrderForm.this);
                dialogBuilder.setTitle("Choose Category");
                dialogBuilder.setItems(Categories, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedText = Categories[item].toString();  //Selected item in listview
                        category.setText(selectedText);
                    }
                });
                //Create alert dialog object via builder
                AlertDialog alertDialogObject = dialogBuilder.create();
                //Show the dialog
                alertDialogObject.show();
            }
        });

        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(OrderForm.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        expiryDate = new ExpiryDate(year,monthOfYear,dayOfMonth);
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
                        date_picker.setText(date);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(OrderForm.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        expiryTime = new ExpiryTime(i,i1);
                        calendar.set(Calendar.HOUR_OF_DAY, i);
                        calendar.set(Calendar.MINUTE, i1);
                        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());
                        time_picker.setText(time);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });


        user_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(OrderForm.this), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                   // Log.e(TAG, e.getStackTrace().toString());
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
               Place place = PlacePicker.getPlace(OrderForm.this,data);
               userLocation = new UserLocation(place.getName().toString(),place.getAddress().toString(),place.getPhoneNumber().toString());
               user_location.setText(userLocation.Location);
               //String toastMsg = String.format("Place: %s", place.getName());
               //Toast.makeText(OrderForm.this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public static int getImageId(String category) {
        if(category.equals("None"))
            return R.drawable.ic_action_movie;
        else if(category.equals("Food") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Medicine") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Household") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Electronics") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Toiletries") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Books") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Clothing") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Shoes") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Sports") )
            return R.drawable.ic_action_movie;
        else if(category.equals("Games") )
            return R.drawable.ic_action_movie;
        else
            return R.drawable.ic_action_movie;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final String order_description = description.getText().toString();
        final String order_category = category.getText().toString();
        final String order_min_range = min_int_range.getText().toString();
        final String order_max_range = max_int_range.getText().toString();
        final String order_delivery_charge = delivery_charge.getText().toString();
        //noinspection SimplifiableIfStatement

        if (id == R.id.action_save) {
            flag = 0;
            //Default text for date_picker = "ExpiryDate"
            //Default text for time_picker = "ExpiryTime"
            if(userLocation == null || order_description.equals("") || order_category.equals("None") || order_min_range.equals("") || order_max_range.equals("")|| order_delivery_charge.equals("")) {
                new AlertDialog.Builder(OrderForm.this)
                        .setMessage(getString(R.string.dialog_save))
                        .setPositiveButton(getString(R.string.dialog_ok), null)
                        .show();
                return true;
            }
            if (Integer.parseInt(order_min_range) > Integer.parseInt(order_max_range)) {
                Toast.makeText(getApplicationContext(), "Min value cannot be more than Max value!", Toast.LENGTH_SHORT).show();
                return true;
            }


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            userId = user.getUid();

            root = FirebaseDatabase.getInstance().getReference();

            /*
            TAG = "hello";
            wallet_ref = root.child("deliveryApp").child("users").child(userId).child("wallet");
            wallet_ref.keepSynced(true);
            wallet_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer wal_bal = dataSnapshot.getValue(Integer.class);
                    balance = wal_bal;

                    if (Integer.parseInt(order_max_range) > balance) {
                        Toast.makeText(getApplicationContext(), "Insufficient balance in your wallet to place this order!\nPlease put some money in your wallet", Toast.LENGTH_LONG).show();
                        flag = 1;
                        Log.d(TAG, "flag value1 " + flag);
                    } else {
                        flag = 2;
                        Log.d(TAG, "flag value2 " + flag);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Log.d(TAG, "flag value3 " + flag);
            try {
                if (flag == 0) {
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
                System.out.println("interrupted.");
            }

            Log.d(TAG, "flag value4 " + flag);
            if (flag == 1) {
                return true;
            }
            */

            deliveryApp = root.child("deliveryApp");
            deliveryApp.keepSynced(true);

            deliveryApp.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild("totalOrders")) {
                        root.child("deliveryApp").child("totalOrders").setValue(1);
                        OrderNumber = 1;
                        order_id = OrderNumber;
                        order = new OrderData(order_category,order_description , order_id,Integer.parseInt(order_max_range), Integer.parseInt(order_min_range),userLocation,expiryDate,expiryTime,"PENDING",Integer.parseInt(order_delivery_charge), acceptedBy,userId, otp);
                        root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(OrderNumber)).setValue(order);
                    }
                    else {
                        OrderNumber = dataSnapshot.child("totalOrders").getValue(Integer.class);
                        OrderNumber++;
                        order_id = OrderNumber;
                        order = new OrderData(order_category,order_description , order_id,Integer.parseInt(order_max_range), Integer.parseInt(order_min_range),userLocation,expiryDate,expiryTime,"PENDING",Integer.parseInt(order_delivery_charge), acceptedBy,userId, otp);
                        root.child("deliveryApp").child("totalOrders").setValue(OrderNumber);
                        root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(OrderNumber)).setValue(order);
                    }
                    UserViewActivity.adapter.insert(0, order);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


          finish();

        }

        else if (id==android.R.id.home) {

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
        CheckConnectivityMain.getInstance().setConnectivityListener(OrderForm.this);
    }
}
