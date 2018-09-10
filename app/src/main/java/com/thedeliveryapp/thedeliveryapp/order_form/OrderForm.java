package com.thedeliveryapp.thedeliveryapp.order_form;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.view.View;
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
import android.content.Intent;

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
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.CheckConnectivityMain;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.deliverer.DelivererViewActivity;
import com.thedeliveryapp.thedeliveryapp.login.MainActivity;
import com.thedeliveryapp.thedeliveryapp.login.ResetPasswordActivity;
import com.thedeliveryapp.thedeliveryapp.paytm.Api;
import com.thedeliveryapp.thedeliveryapp.paytm.Checksum;
import com.thedeliveryapp.thedeliveryapp.paytm.Constants;
import com.thedeliveryapp.thedeliveryapp.paytm.Paytm;
import com.thedeliveryapp.thedeliveryapp.user.UserViewActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.AcceptedBy;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryDate;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryTime;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;
import com.thedeliveryapp.thedeliveryapp.user.order.UserLocation;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class OrderForm extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener, PaytmPaymentTransactionCallback {

    private BottomSheetBehavior mBottomSheetBehavior;
    TextView category,delivery_charge, price, total_charge ;
    Button date_picker, time_picker, user_location;
    Calendar calendar ;
    EditText description, min_int_range, max_int_range;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private ProgressBar progressBar;
    private DatabaseReference root, deliveryApp, ref1;
    private String userId, otp;
    private int OrderNumber, order_id, value, final_price = -1;
    int flag;
    UserLocation userLocation = null;
    ExpiryTime expiryTime = null;
    ExpiryDate expiryDate = null;
    AcceptedBy acceptedBy = null;
    OrderData order;

    int PLACE_PICKER_REQUEST = 1;

    public static final int REQUEST_LOCATION_PERMISSION = 10;
    public static final int  REQUEST_CHECK_SETTINGS = 20;

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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("New Order");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        acceptedBy = new AcceptedBy("-", "-", "-", "-", "-");
        otp = "";


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        category = findViewById(R.id.btn_category);
        date_picker = findViewById(R.id.btn_date_picker);
        time_picker = findViewById(R.id.btn_time_picker);
        calendar = Calendar.getInstance();
        description = findViewById(R.id.description_of_order);
        min_int_range = findViewById(R.id.min_int);
        max_int_range = findViewById(R.id.max_int);
        user_location = findViewById(R.id.user_location);
        delivery_charge = findViewById(R.id.delivery_charge);
        price = findViewById(R.id.max_price);
        total_charge = findViewById(R.id.total_amount);


        max_int_range.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String S = s.toString();
                if (!S.equals("")) {
                    value = Integer.parseInt(s.toString());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    userId = user.getUid();
                    root = FirebaseDatabase.getInstance().getReference();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

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
                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                } else {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    try {
                        startActivityForResult(builder.build(OrderForm.this), PLACE_PICKER_REQUEST);
                    } catch (Exception e) {
                        // Log.e(TAG, e.getStackTrace().toString());
                    }
                }
            }
        });

        View bottomSheet = findViewById(R.id.confirmation_dialog);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        Button btn_proceed = findViewById(R.id.btn_proceed);
        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setHideable(true);


        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String order_description = description.getText().toString();
                final String order_category = category.getText().toString();
                final String order_min_range = min_int_range.getText().toString();
                final String order_max_range = max_int_range.getText().toString();


                if(userLocation == null || order_description.equals("") || order_category.equals("None") || order_min_range.equals("") || order_max_range.equals("")) {
                    new AlertDialog.Builder(OrderForm.this)
                            .setMessage(getString(R.string.dialog_save))
                            .setPositiveButton(getString(R.string.dialog_ok), null)
                            .show();
                    return;
                }
                else if (Integer.parseInt(order_min_range) > Integer.parseInt(order_max_range)) {
                    Toast.makeText(getApplicationContext(), "Min value cannot be more than Max value!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                }
                else {
                    DeliveryChargeCalculater calc= new DeliveryChargeCalculater(Integer.parseInt(order_max_range));
                    delivery_charge.setText("₹"+Float.toString(calc.deliveryCharge));
                    price.setText("₹"+Float.toString(calc.max_price));
                    total_charge.setText("₹"+Float.toString(calc.total_price));
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });


        Button btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    generateCheckSum();
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
                    Toast.makeText(OrderForm.this, "Location permission Denied", Toast.LENGTH_LONG).show();
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
                        resolvable.startResolutionForResult(OrderForm.this,
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
               Place place = PlacePicker.getPlace(OrderForm.this,data);
               userLocation = new UserLocation(place.getName().toString(),place.getAddress().toString(),place.getPhoneNumber().toString());
               user_location.setText(userLocation.Location);
               //String toastMsg = String.format("Place: %s", place.getName());
               //Toast.makeText(OrderForm.this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            System.out.println("onActivityResult ke if mai hoon");
            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(), mLocationCallback,
                            null /* Looper */);
        }
    }


    @Override
    public boolean dispatchTouchEvent (MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                Rect outRect = new Rect() ;
                View bottomSheet=findViewById(R.id.confirmation_dialog);
                bottomSheet.getGlobalVisibleRect(outRect);
                if(!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /*
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }*/

/*
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
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
       // final String order_description = description.getText().toString();
        //final String order_category = category.getText().toString();
        //final String order_min_range = min_int_range.getText().toString();
        //final String order_max_range = max_int_range.getText().toString();
        //final String order_delivery_charge = delivery_charge.getText().toString();
        //noinspection SimplifiableIfStatement
        if (id==android.R.id.home) {

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
        CheckConnectivityMain.getInstance().setConnectivityListener(OrderForm.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    //PAYTM

    private void generateCheckSum() {
        final String order_max_range = max_int_range.getText().toString();
        DeliveryChargeCalculater calc= new DeliveryChargeCalculater(Integer.parseInt(order_max_range));

        //getting the tax amount first.
        String txnAmount = Float.toString(calc.total_price).trim();

        //creating a retrofit object.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //creating the retrofit api service
        Api apiService = retrofit.create(Api.class);

        //creating paytm object
        //containing all the values required
        final Paytm paytm = new Paytm(
                Constants.M_ID,
                Constants.CHANNEL_ID,
                txnAmount,
                Constants.WEBSITE,
                Constants.CALLBACK_URL,
                Constants.INDUSTRY_TYPE_ID
        );

        //creating a call object from the apiService
        Call<Checksum> call = apiService.getChecksum(
                paytm.getmId(),
                paytm.getOrderId(),
                paytm.getCustId(),
                paytm.getChannelId(),
                paytm.getTxnAmount(),
                paytm.getWebsite(),
                paytm.getCallBackUrl(),
                paytm.getIndustryTypeId()
        );

        //making the call to generate checksum
        call.enqueue(new Callback<Checksum>() {
            @Override
            public void onResponse(Call<Checksum> call, Response<Checksum> response) {

                //once we get the checksum we will initiailize the payment.
                //the method is taking the checksum we got and the paytm object as the parameter
                initializePaytmPayment(response.body().getChecksumHash(), paytm);
            }

            @Override
            public void onFailure(Call<Checksum> call, Throwable t) {

            }
        });
    }

    private void initializePaytmPayment(String checksumHash, Paytm paytm) {
        progressBar.setVisibility(View.GONE);
        //getting paytm service
        PaytmPGService Service = PaytmPGService.getStagingService();

        //use this when using for production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        //creating a hashmap and adding all the values required
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", Constants.M_ID);
        paramMap.put("ORDER_ID", paytm.getOrderId());
        paramMap.put("CUST_ID", paytm.getCustId());
        paramMap.put("CHANNEL_ID", paytm.getChannelId());
        paramMap.put("TXN_AMOUNT", paytm.getTxnAmount());
        paramMap.put("WEBSITE", paytm.getWebsite());
        paramMap.put("CALLBACK_URL", paytm.getCallBackUrl());
        paramMap.put("CHECKSUMHASH", checksumHash);
        paramMap.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());


        //creating a paytm order object using the hashmap
        PaytmOrder order = new PaytmOrder(paramMap);

        //intializing the paytm service
        Service.initialize(order, null);

        //finally starting the payment transaction
        Service.startPaymentTransaction(this, true, true, this);

    }

    //all these overriden method is to detect the payment result accordingly
    public void someUIErrorOccurred(String inErrorMessage) {

        Log.d("LOG", "UI Error Occur.");

        Toast.makeText(getApplicationContext(), " UI Error Occur. ", Toast.LENGTH_LONG).show();

    }

    @Override

    public void onTransactionResponse(Bundle inResponse) {

        Log.d("LOG", "Payment Transaction : " + inResponse);

        //Toast.makeText(getApplicationContext(), "Payment Transaction response "+inResponse.toString(), Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), "Payment Transaction Successful ", Toast.LENGTH_LONG).show();
        final String order_description = description.getText().toString();
        final String order_category = category.getText().toString();
        final String order_min_range = min_int_range.getText().toString();
        final String order_max_range = max_int_range.getText().toString();
        final DeliveryChargeCalculater calc= new DeliveryChargeCalculater(Integer.parseInt(order_max_range));
        flag = 0;
        //Default text for date_picker = "ExpiryDate"
        //Default text for time_picker = "ExpiryTime"


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        root = FirebaseDatabase.getInstance().getReference();

        deliveryApp = root.child("deliveryApp");
        deliveryApp.keepSynced(true);

        deliveryApp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("totalOrders")) {
                    root.child("deliveryApp").child("totalOrders").setValue(1);
                    OrderNumber = 1;
                    order_id = OrderNumber;
                    order = new OrderData(order_category, order_description, order_id, Integer.parseInt(order_max_range), Integer.parseInt(order_min_range), userLocation, expiryDate, expiryTime, "PENDING", calc.deliveryCharge, acceptedBy, userId, otp, final_price);
                    root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(OrderNumber)).setValue(order);
                } else {
                    OrderNumber = dataSnapshot.child("totalOrders").getValue(Integer.class);
                    OrderNumber++;
                    order_id = OrderNumber;
                    order = new OrderData(order_category, order_description, order_id, Integer.parseInt(order_max_range), Integer.parseInt(order_min_range), userLocation, expiryDate, expiryTime, "PENDING", calc.deliveryCharge, acceptedBy, userId, otp, final_price);
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
        Toast.makeText(getApplicationContext(), "Payment Transaction Successful ", Toast.LENGTH_LONG).show();
    }

    @Override

    public void networkNotAvailable() {

        Log.d("LOG", "UI Error Occur.");

        Toast.makeText(getApplicationContext(), " UI Error Occur. ", Toast.LENGTH_LONG).show();

    }

    @Override

    public void clientAuthenticationFailed(String inErrorMessage) {

        Log.d("LOG", "UI Error Occur.");

        Toast.makeText(getApplicationContext(), " Severside Error "+ inErrorMessage, Toast.LENGTH_LONG).show();

    }

    @Override

    public void onErrorLoadingWebPage(int iniErrorCode,

                                      String inErrorMessage, String inFailingUrl) {

    }

    @Override

    public void onBackPressedCancelTransaction() {

        // TODO Auto-generated method stub

    }

    @Override

    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

        Log.d("LOG", "Payment Transaction Failed " + inErrorMessage);

        Toast.makeText(getBaseContext(), "Payment Transaction Failed ", Toast.LENGTH_LONG).show();

    }


}
