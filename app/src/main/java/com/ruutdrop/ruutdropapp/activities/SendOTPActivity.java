package com.ruutdrop.ruutdropapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;
import com.ruutdrop.ruutdropapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * created by Aman on 04/01/17.
 */

public class SendOTPActivity extends AppCompatActivity {

    EditText emailTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendotp);
        Button sendotp = (Button)findViewById(R.id.sendotp);
        emailTxt = (EditText)findViewById(R.id.emailid);
        sendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!emailTxt.getText().toString().equalsIgnoreCase("")) {
                    new SendOtp(emailTxt.getText().toString()).execute();
                }
            }
        });
    }
    public class SendOtp extends AsyncTask<Void, Void, Void> {
        private String response;
        private String email;
        ProgressDialog asyncDialog = new ProgressDialog(SendOTPActivity.this);


        SendOtp(String email) {
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            asyncDialog.setMessage("Please Wait while Loading....");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("email", email);
            postParams.put("action", "initiate");

            String url = "http://34.207.184.123/API/ForgotPassword.php";
            response = PostIntrepreter.performPostCall(url, postParams);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("user", response);
            asyncDialog.dismiss();
            try {
                JSONObject jObject = new JSONObject(response);
                if(jObject.optString("error").equalsIgnoreCase("false"))
                {
                    String otp = jObject.optString("OTP");
                    Intent verifyOtp = new Intent(SendOTPActivity.this,VerifyOTPActivity.class);
                    verifyOtp.putExtra("otp",otp);
                    verifyOtp.putExtra("email",email);
                    startActivity(verifyOtp);

                }

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

    }
}