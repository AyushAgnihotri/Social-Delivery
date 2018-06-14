package com.thedeliveryapp.thedeliveryapp.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thedeliveryapp.thedeliveryapp.R;

public class VerifyEmailScreen extends AppCompatActivity {

    TextView email_to_verify;
    Button btn_resend_email, btn_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email_screen);

        email_to_verify = findViewById(R.id.email_to_verify);
        btn_resend_email = findViewById(R.id.btn_resend_email);
        btn_refresh = findViewById(R.id.btn_refresh);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email_to_verify.setText(user.getEmail());

        btn_resend_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_resend_email.setEnabled(false);

                FirebaseAuth.getInstance().getCurrentUser()
                        .sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                btn_resend_email.setEnabled(true);

                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Verification email sent to : " + FirebaseAuth.getInstance().getCurrentUser().getEmail() , Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to send verification email!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser()
                        .reload()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    startActivity(new Intent(VerifyEmailScreen.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Please complete email verification first!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}