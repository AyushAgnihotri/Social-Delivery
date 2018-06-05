package com.thedeliveryapp.thedeliveryapp.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.thedeliveryapp.thedeliveryapp.R;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.CheckConnectivityMain;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;

import static com.thedeliveryapp.thedeliveryapp.login.LoginActivity.mGoogleApiClient;

public class OtherSignup extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private EditText inputName, inputMobile, inputAltMobile, inputEmail;
    private Button btnSignIn, btnSaveDetails;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private String google_name, google_email;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_signup);

        checkConnection();

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSaveDetails = (Button) findViewById(R.id.save_details);
        inputName = (EditText) findViewById(R.id.name);
        inputMobile = (EditText) findViewById(R.id.mobile);
        inputAltMobile = (EditText) findViewById(R.id.alt_mobile);
        inputEmail = (EditText) findViewById(R.id.email);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        google_name = getIntent().getStringExtra("username");
        google_email = getIntent().getStringExtra("email");

        inputName.setText(google_name);
        inputEmail.setText(google_email);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                auth.signOut();
                Intent intent = new Intent(OtherSignup.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSaveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String mobile = inputMobile.getText().toString().trim();
                final String alt_mobile = inputAltMobile.getText().toString().trim();

                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(getApplicationContext(), "Enter your Mobile No.!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mobile.length() != 10) {
                    Toast.makeText(getApplicationContext(), "Enter 10-digit Mobile No.!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ((alt_mobile.length() != 0) && (alt_mobile.length() != 10)) {
                    Toast.makeText(getApplicationContext(), "Enter 10-digit Alt. Mobile No.!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                Toast.makeText(OtherSignup.this, "Details Saved Successfully", Toast.LENGTH_SHORT).show();
                update_userdetails_database(google_name, mobile, alt_mobile, google_email);
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(OtherSignup.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        CheckConnectivityMain.getInstance().setConnectivityListener(OtherSignup.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        auth.signOut();
        Intent intent = new Intent(OtherSignup.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //Method to upload details in database;
    void update_userdetails_database(String name, String Mobile, String Alt_Mobile, String Email) {
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String playerId = status.getSubscriptionStatus().getUserId();
        UserDetails Details = new UserDetails(name, Mobile, Alt_Mobile, Email, 5000,playerId);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId=user.getUid();

        root.child("deliveryApp").child("users").child(userId).setValue(Details);
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

}