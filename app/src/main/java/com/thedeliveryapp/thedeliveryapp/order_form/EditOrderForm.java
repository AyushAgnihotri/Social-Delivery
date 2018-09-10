package com.thedeliveryapp.thedeliveryapp.order_form;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.thedeliveryapp.thedeliveryapp.login.LoginActivity;
import com.thedeliveryapp.thedeliveryapp.user.UserOrderDetailActivity;
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

public class EditOrderForm extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private DatabaseReference root, ref1;
    private String userId, otp;
    private int OrderNumber, i, i1, year, monthOfYear, dayOfMonth, value;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    TextView category,delivery_charge,price, total_charge ;
    Button date_picker, time_picker, user_location;;
    Calendar calendar ;
    EditText description, min_int_range, max_int_range;
    OrderData updated_order, myOrder;
    UserLocation userLocation = null;
    ExpiryTime expiryTime = null;
    ExpiryDate expiryDate = null;
    private DatabaseReference deliveryApp;
    private int order_id, final_price = -1;
    int flag;
    AcceptedBy acceptedBy = null;
    OrderData order;

    int PLACE_PICKER_REQUEST = 1;
    public static final int REQUEST_LOCATION_PERMISSION = 10;
    public static final int  REQUEST_CHECK_SETTINGS = 20;

    private String date, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order_form);
        checkConnection();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("Edit Order");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        myOrder = intent.getParcelableExtra("MyOrder");

        description = findViewById(R.id.description_of_order);
        category = findViewById(R.id.btn_category);
        delivery_charge = findViewById(R.id.delivery_charge);
        min_int_range = findViewById(R.id.min_int);
        max_int_range = findViewById(R.id.max_int);
        date_picker = findViewById(R.id.btn_date_picker);
        time_picker = findViewById(R.id.btn_time_picker);
        user_location = findViewById(R.id.user_location);
        price = findViewById(R.id.max_price);
        total_charge = findViewById(R.id.total_amount);


        otp = "";

        calendar = Calendar.getInstance();
        OrderNumber = myOrder.orderId;

        description.setText(myOrder.description);
        category.setText(myOrder.category);
        min_int_range.setText(myOrder.min_range + "");
        max_int_range.setText(myOrder.max_range + "");
        max_int_range.setEnabled(false);

        userLocation = new UserLocation(myOrder.userLocation.Name, myOrder.userLocation.Location, myOrder.userLocation.PhoneNumber);

        year = myOrder.expiryDate.year;
        monthOfYear = myOrder.expiryDate.month;
        dayOfMonth = myOrder.expiryDate.day;

        expiryDate = new ExpiryDate(year,monthOfYear,dayOfMonth);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
        date_picker.setText(date);

        i = myOrder.expiryTime.hour;
        i1 = myOrder.expiryTime.minute;

        expiryTime = new ExpiryTime(i,i1);
        calendar.set(Calendar.HOUR_OF_DAY, i);
        calendar.set(Calendar.MINUTE, i1);
        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());
        time_picker.setText(time);

        user_location.setText(myOrder.userLocation.Location);

        /*
        userLocationName.setText(myOrder.userLocation.Name);
        userLocationLocation.setText(myOrder.userLocation.Location);
        */

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
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditOrderForm.this);
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditOrderForm.this, new DatePickerDialog.OnDateSetListener() {
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditOrderForm.this, new TimePickerDialog.OnTimeSetListener() {
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
                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                } else {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    try {
                        startActivityForResult(builder.build(EditOrderForm.this), PLACE_PICKER_REQUEST);
                    } catch (Exception e) {
                        // Log.e(TAG, e.getStackTrace().toString());
                    }
                }
            }
        });

        Button btn_proceed = findViewById(R.id.btn_proceed);
/*
        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String order_description = description.getText().toString();
                final String order_category = category.getText().toString();
                final String order_delivery_charge = delivery_charge.getText().toString();
                final String order_min_range = min_int_range.getText().toString();
                final String order_max_range = max_int_range.getText().toString();


                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                }
                else {
                    if(order_description.equals("") || order_category.equals("None") || order_min_range.equals("") || order_max_range.equals("")) {
                        new AlertDialog.Builder(EditOrderForm.this)
                                .setMessage(getString(R.string.dialog_save))
                                .setPositiveButton(getString(R.string.dialog_ok), null)
                                .show();
                    }
                    else if (Integer.parseInt(order_min_range) > Integer.parseInt(order_max_range)) {
                        Toast.makeText(getApplicationContext(), "Min value cannot be more than Max value!", Toast.LENGTH_SHORT).show();
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    userId = user.getUid();
                    root = FirebaseDatabase.getInstance().getReference();
                    root.child("deliveryApp").child("orders").child(userId).child(OrderNumber+"").keepSynced(true);

                    root.child("deliveryApp").child("orders").child(userId).child(OrderNumber+"").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            OrderData current_order = dataSnapshot.getValue(OrderData.class);

                            if(current_order.status.equals("PENDING")) {
                                DeliveryChargeCalculater calc= new DeliveryChargeCalculater(Integer.parseInt(order_max_range));
                                delivery_charge.setText("₹"+Float.toString(calc.deliveryCharge));
                                price.setText("₹"+Float.toString(calc.max_price));
                                total_charge.setText("₹"+Float.toString(calc.total_price));





                            }
                            else {
                                Toast.makeText(EditOrderForm.this, "can't edit already accepted order", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

*/

        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String order_description = description.getText().toString();
                final String order_category = category.getText().toString();
                final String order_min_range = min_int_range.getText().toString();
                final String order_max_range = max_int_range.getText().toString();

                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                }
                else {

                    if(order_description.equals("") || order_category.equals("None") || order_min_range.equals("") || order_max_range.equals("")) {
                        new AlertDialog.Builder(EditOrderForm.this)
                                .setMessage(getString(R.string.dialog_save))
                                .setPositiveButton(getString(R.string.dialog_ok), null)
                                .show();
                    }
                    else if (Integer.parseInt(order_min_range) > Integer.parseInt(order_max_range)) {
                        Toast.makeText(getApplicationContext(), "Min value cannot be more than Max value!", Toast.LENGTH_SHORT).show();
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    userId = user.getUid();
                    root = FirebaseDatabase.getInstance().getReference();
                    root.child("deliveryApp").child("orders").child(userId).child(OrderNumber+"").keepSynced(true);

                    root.child("deliveryApp").child("orders").child(userId).child(OrderNumber+"").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            OrderData current_order = dataSnapshot.getValue(OrderData.class);

                            if(current_order.status.equals("PENDING")) {
                                DeliveryChargeCalculater calc= new DeliveryChargeCalculater(Integer.parseInt(order_max_range));
                            }
                            else {
                                Toast.makeText(EditOrderForm.this, "can't edit already accepted order", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                    flag=0;

                    //Default text for date_picker = "ExpiryDate"
                    //Default text for time_picker = "ExpiryTime"


                    deliveryApp = root.child("deliveryApp").child("orders").child(userId).child(OrderNumber+"");
                    deliveryApp.keepSynced(true);

                    deliveryApp.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderData current_order = dataSnapshot.getValue(OrderData.class);

                            if(current_order.status.equals("PENDING")) {
                                DeliveryChargeCalculater calc= new DeliveryChargeCalculater(Integer.parseInt(order_max_range));
                                updated_order = new OrderData(order_category, order_description, OrderNumber, Integer.parseInt(order_max_range), Integer.parseInt(order_min_range), userLocation, expiryDate, expiryTime, "PENDING",calc.deliveryCharge, myOrder.acceptedBy, userId, otp, myOrder.final_price);
                                root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(OrderNumber)).setValue(updated_order);
                                Intent intent = new Intent(EditOrderForm.this, UserOrderDetailActivity.class);
                                intent.putExtra("MyOrder", (Parcelable) updated_order);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(EditOrderForm.this, "can't edit already accepted order", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //Location location = locationResult.getLastLocation();
            }
        };

    }

    void requestLocationPermissions() {
        System.out.println("Inside getLatAndLong");
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permission lerha");
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            System.out.println("Permission pehle se hai");
            //Toast.makeText(DelivererViewActivity.this, "Location permission granted", Toast.LENGTH_SHORT).show();
            setGpsOn();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.out.println("Inside onRequestPermissionsResult");
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("onRequestPermissionsResult ki if condition ke andar");
                    setGpsOn();
                } else {
                    Toast.makeText(EditOrderForm.this, "Location permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(60* 1000);
        locationRequest.setFastestInterval(30*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    void setGpsOn() {
        System.out.println("Inside setGpsOn");
        LocationRequest mLocationRequest = getLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                System.out.println("task ke OnSuccess mai hoon");
                mFusedLocationClient.requestLocationUpdates
                        (getLocationRequest(), mLocationCallback,
                                null /* Looper */);
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("task ke onFailure mai hoon");
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(EditOrderForm.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(EditOrderForm.this,data);
                userLocation = new UserLocation(place.getName().toString(),place.getAddress().toString(),place.getPhoneNumber().toString());
                user_location.setText(userLocation.Location);
                //String toastMsg = String.format("Place: %s", place.getName());
                //Toast.makeText(EditOrderForm.this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            System.out.println("onActivityResult ke if mai hoon");
            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(), mLocationCallback,
                            null /* Looper */);
        }
    }




/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
      //  final String order_description = description.getText().toString();
       // final String order_category = category.getText().toString();
        //final String order_delivery_charge = delivery_charge.getText().toString();
        //final String order_min_range = min_int_range.getText().toString();
        //final String order_max_range = max_int_range.getText().toString();

        //noinspection SimplifiableIfStatement
        if (id==android.R.id.home) {
            Intent intent = new Intent(EditOrderForm.this, UserOrderDetailActivity.class);
            intent.putExtra("MyOrder",(Parcelable) myOrder);
            startActivity(intent);
            finish();

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
        mFusedLocationClient.requestLocationUpdates
                (getLocationRequest(), mLocationCallback,
                        null /* Looper */);
        CheckConnectivityMain.getInstance().setConnectivityListener(EditOrderForm.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

}
