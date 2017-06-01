package com.ruutdrop.ruutdropapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ruutdrop.ruutdropapp.R;

/**
 * created by Aman on 04/01/17.
 */

public class VerifyOTPActivity extends AppCompatActivity {

    private String otp,email;
    private EditText otpText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifyotp);

        otp = getIntent().getStringExtra("otp");
        email = getIntent().getStringExtra("email");

        otpText = (EditText)findViewById(R.id.otp);
        Button verifyOtp = (Button)findViewById(R.id.verifyotp);
        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(otpText.getText().toString().equalsIgnoreCase(otp))
                {
                    Intent changePassword = new Intent(VerifyOTPActivity.this,ChangePasswordActivity.class);
                    changePassword.putExtra("email",email);
                    startActivity(changePassword);
                }
                else{
                    Toast.makeText(VerifyOTPActivity.this,"Wrong OTP! Please try again.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {

    }
}
