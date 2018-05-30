package com.thedeliveryapp.thedeliveryapp.order_form;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.user.ItemListActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;
import com.thedeliveryapp.thedeliveryapp.user.order.UserLocation;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OrderForm extends AppCompatActivity {

    TextView category ;
    Button date_picker;
    Button time_picker ;
    Calendar calendar ;
    EditText description ;
    EditText min_int_range ;
    EditText max_int_range ;
    Button user_location;
    private DatabaseReference root;
    private DatabaseReference deliveryApp;
    private String userId;
    private int OrderNumber;
    UserLocation userLocation;



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

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("New Order");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);





        category = findViewById(R.id.btn_category);
        date_picker = findViewById(R.id.btn_date_picker);
        time_picker = findViewById(R.id.btn_time_picker);
        calendar = Calendar.getInstance();
        description = findViewById(R.id.description_of_order);
        min_int_range = findViewById(R.id.min_int);
        max_int_range = findViewById(R.id.max_int);
        user_location = findViewById(R.id.user_location);

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
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(OrderForm.this, toastMsg, Toast.LENGTH_LONG).show();
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

    int getImageId(String category) {
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
        String order_description = description.getText().toString();
        String order_category = category.getText().toString();
        String order_min_range = min_int_range.getText().toString();
        String order_max_range = max_int_range.getText().toString();
        int order_image_id = getImageId(order_category);
        //noinspection SimplifiableIfStatement

        if (id == R.id.action_save) {
            //Default text for date_picker = "Date"
            //Default text for time_picker = "Time"
            if(order_description.equals("") || order_category.equals("None") || order_min_range.equals("") || order_max_range.equals("")) {
                new AlertDialog.Builder(OrderForm.this)
                        .setMessage(getString(R.string.dialog_save))
                        .setPositiveButton(getString(R.string.dialog_ok), null)
                        .show();
                return true;
            }


            final OrderData order= new OrderData(order_category,order_description ,order_image_id,Integer.parseInt(order_max_range), Integer.parseInt(order_min_range),userLocation);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            userId = user.getUid();


            root = FirebaseDatabase.getInstance().getReference();
            deliveryApp = root.child("deliveryApp");

            deliveryApp.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild("totalOrders")) {
                        root.child("deliveryApp").child("totalOrders").setValue(0);
                        OrderNumber = 0;
                        root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(OrderNumber)).setValue(order);

                    }
                    else {
                        OrderNumber = dataSnapshot.child("totalOrders").getValue(Integer.class);
                        OrderNumber++;
                        root.child("deliveryApp").child("totalOrders").setValue(OrderNumber);
                        root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(OrderNumber)).setValue(order);


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




          ItemListActivity.adapter.insert(0,
                    order);

          finish();

        }

        else if (id==android.R.id.home) {

        }

        return super.onOptionsItemSelected(item);
    }

}
