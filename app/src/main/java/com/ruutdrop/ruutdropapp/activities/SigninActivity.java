package com.ruutdrop.ruutdropapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;
import com.ruutdrop.ruutdropapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Aman on 10-10-2016.
 */

public class SigninActivity extends AppCompatActivity {
    private EditText emailid,password;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        emailid = (EditText)findViewById(R.id.emailid);
        password = (EditText)findViewById(R.id.password);
        Button login = (Button) findViewById(R.id.login);
        Button forgotPassword = (Button) findViewById(R.id.forgotPassword);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendOtpIntent = new Intent(SigninActivity.this,SendOTPActivity.class);
                startActivity(sendOtpIntent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty(emailid)) {
                    Toast.makeText(SigninActivity.this, "Please enter the Email Address", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(password)) {
                    Toast.makeText(SigninActivity.this, "Please enter the Password", Toast.LENGTH_SHORT).show();
                } else if (!emailid.getText().toString().contains("@")) {
                    Toast.makeText(SigninActivity.this, "Please enter a Valid Email ID", Toast.LENGTH_SHORT).show();
                }
                else{
                    new signin(emailid.getText().toString(),password.getText().toString()).execute();
                    emailid.setText("");
                    password.setText("");
                }

            }
        });
            }
    boolean isEmpty(EditText et) {
        if (et.getText().toString().length() == 0)
            return true;
        else
            return false;
    }
    public class signin extends AsyncTask<Void, Void, Void> {
        private String response;
        private String email;
        private String password;
        ProgressDialog asyncDialog = new ProgressDialog(SigninActivity.this);


        public signin(String email, String password) {
            this.email = email;
            this.password = password;


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
            postParams.put("password", password);
            postParams.put("tag", "user");

            String url = "http://34.207.184.123/API/Login.php";
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
                String error = jObject.optString("error");
                if (error.equalsIgnoreCase("true")) {
                    Toast.makeText(SigninActivity.this, "Invalid Password", Toast.LENGTH_LONG).show();

                }
                else
                {
                    String userid = jObject.optString("id");

//                    final String accesstoken =jObject.optString("unique");
//                    String username =jObject.optString("name");
//                    clientManager=new GCMClientManager(SignInActivity.this,projectnumber);
//                    clientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
//                        @Override
//                        public void onSuccess(String registrationId, boolean isNewRegistration) {
//                            Log.d("reg token", registrationId);
////                            new SendRegistration(accesstoken, registrationId).execute();
//                        }
//
//                        @Override
//                        public void onFailure(String ex) {
//                            super.onFailure(ex);
//                            // If there is an error registering, don't just keep trying to register.
//                            // Require the user to click a button again, or perform
//                            // exponential back-off when retrying.
//                        }
//                    });

                    SharedPreferences sharedpreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                    sharedpreferences.edit().putString("EmailId",email).apply();
                    sharedpreferences.edit().putString("userid",userid).apply();

//                    sharedpreferences.edit().putString("unique",accesstoken).apply();
                    sharedpreferences.edit().putBoolean("isloggedin",true).apply();
//                    sharedpreferences.edit().putString("name",username).apply();
                    Intent signinactivity = new Intent(SigninActivity.this, SplashActivity.class);
                    signinactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    signinactivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    signinactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    SigninActivity.this.finish();
                    startActivity(signinactivity);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }
}
