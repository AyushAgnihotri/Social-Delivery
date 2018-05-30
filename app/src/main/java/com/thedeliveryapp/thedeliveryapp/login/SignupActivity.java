package com.thedeliveryapp.thedeliveryapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.thedeliveryapp.thedeliveryapp.R;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;

public class SignupActivity extends AppCompatActivity {



    private EditText inputName, inputMobile, inputAltMobile, inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputName = (EditText) findViewById(R.id.name);
        inputMobile = (EditText) findViewById(R.id.mobile);
        inputAltMobile = (EditText) findViewById(R.id.alt_mobile);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                final String name = inputName.getText().toString().trim();
                final String mobile = inputMobile.getText().toString().trim();
                final String alt_mobile = inputAltMobile.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), "Enter your Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(getApplicationContext(), "Enter your Mobile No.!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((mobile.length() != 10) || (alt_mobile.length() != 10)) {
                    Toast.makeText(getApplicationContext(), "Enter 10-digit Mobile No.!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(alt_mobile)) {
                    Toast.makeText(getApplicationContext(), "Enter your Alternate Mobile No.!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                //create com.thedeliveryapp.thedeliveryapp.user.user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Successfully Registered.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignupActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the com.thedeliveryapp.thedeliveryapp.user.user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in com.thedeliveryapp.thedeliveryapp.user.user can be handled in the listener.
                                if(!task.isSuccessful()) {
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                    switch (errorCode) {

                                        case "ERROR_INVALID_EMAIL":
                                            Toast.makeText(SignupActivity.this, "The email address is badly formatted.", Toast.LENGTH_SHORT).show();
                                            break;

                                        case "ERROR_EMAIL_ALREADY_IN_USE":
                                            Toast.makeText(SignupActivity.this, "The email address is already in use by another account.", Toast.LENGTH_SHORT).show();
                                            break;

                                        case "ERROR_WEAK_PASSWORD":
                                            Toast.makeText(SignupActivity.this, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                } else {
                                    update_userdetails_database(name, mobile, alt_mobile, email);

                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }


    //Method to upload details in database;
    void update_userdetails_database(String name, String Mobile, String Alt_Mobile, String Email) {
        UserDetails Details = new UserDetails(name, Mobile, Alt_Mobile, Email);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId=user.getUid();

        root.child("deliveryApp").child("users").child(userId).setValue(Details);
    }

}