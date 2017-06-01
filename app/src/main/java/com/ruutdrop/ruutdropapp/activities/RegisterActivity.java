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
import android.widget.Toast;

import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;
import com.ruutdrop.ruutdropapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Aman on 10-10-2016.
 */

public class RegisterActivity extends AppCompatActivity {
    private EditText firstname, lastname, emailid, password, address, homephone, workphone, driverlicenseplate, carlicenseplate,city,pincode;
    private Button register;
    private String regularfontpath;
    private static final String REGISTER_URL = "http://www.dazecorp.com:9081/Register/";
    private EditText state;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firstname = (EditText) findViewById(R.id.firstname);
        lastname = (EditText) findViewById(R.id.lastname);
        emailid = (EditText) findViewById(R.id.registeremailid);
        password = (EditText) findViewById(R.id.registerpassword);
        address = (EditText) findViewById(R.id.registeraddress);
        homephone = (EditText) findViewById(R.id.homephone);
        workphone = (EditText) findViewById(R.id.workphone);
        driverlicenseplate = (EditText) findViewById(R.id.driverlicenseplate);
        state = (EditText) findViewById(R.id.registerState);
        carlicenseplate = (EditText) findViewById(R.id.carlicenseplate);
        city = (EditText) findViewById(R.id.registercity);
        pincode = (EditText) findViewById(R.id.registerpincode);

        register = (Button) findViewById(R.id.register);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty(firstname)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the First name", Toast.LENGTH_SHORT).show();

                } else if (isEmpty(lastname)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Last Name", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(emailid)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Email ID", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Password", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(address)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Address", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(homephone)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Cell Phone Number", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(workphone)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Work Phone Number", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(driverlicenseplate)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Driver's License Plate Number", Toast.LENGTH_SHORT).show();
                } else if (isEmpty(carlicenseplate)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Car's License Plate Number", Toast.LENGTH_SHORT).show();
                }
                else if (isEmpty(city)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the City", Toast.LENGTH_SHORT).show();
                }
                else if (isEmpty(pincode)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the Pincode", Toast.LENGTH_SHORT).show();
                }

                else if (!emailid.getText().toString().contains("@")) {
                    Toast.makeText(RegisterActivity.this, "Please enter a Valid Email ID", Toast.LENGTH_SHORT).show();
                } else {
                    new userdetails(address.getText().toString(), carlicenseplate.getText().toString(), city.getText().toString(), driverlicenseplate.getText().toString(), emailid.getText().toString(),firstname.getText().toString(),homephone.getText().toString(),lastname.getText().toString(),password.getText().toString(),pincode.getText().toString(),workphone.getText().toString(),state.getText().toString()).execute();
                    firstname.setText("");
                    lastname.setText("");
                    emailid.setText("");
                    password.setText("");
                    address.setText("");
                    workphone.setText("");
                    homephone.setText("");
                    driverlicenseplate.setText("");
                    carlicenseplate.setText("");
                    city.setText("");
                    state.setText("");
                    pincode.setText("");
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

    public class userdetails extends AsyncTask<Void, Void, Void> {
        private String response;
        private String firstname;
        private String lastname;
        private String email;
        private String password;
        private String address;
        private String city,state;
        private String pincode;
        private String workphone;
        private String homephone;
        private String driverlicenseplate;
        private String carlicenseplate;
        ProgressDialog asyncDialog = new ProgressDialog(RegisterActivity.this);

        public userdetails(String address, String carlicenseplate, String city, String driverlicenseplate, String email, String firstname, String homephone, String lastname, String password, String pincode, String workphone,String state) {
            this.address = address;
            this.carlicenseplate = carlicenseplate;
            this.city = city;
            this.driverlicenseplate = driverlicenseplate;
            this.email = email;
            this.firstname = firstname;
            this.homephone = homephone;
            this.state = state;
            this.lastname = lastname;
            this.password = password;
            this.pincode = pincode;
            this.workphone = workphone;
        }

        @Override
        protected void onPreExecute() {
            asyncDialog.setMessage("Please Wait while registering....");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("firstname", firstname);
            postParams.put("lastname", lastname);
            postParams.put("email", email);
            postParams.put("password", password);
            postParams.put("address", address);
            postParams.put("city", city);
            postParams.put("pincode", pincode);
            postParams.put("workphone", workphone);
            postParams.put("homephone", homephone);
            postParams.put("driverlicenseplate", driverlicenseplate);
            postParams.put("carlicenseplate", carlicenseplate);
            postParams.put("tag", "user");
            postParams.put("usertype", "normal");
            String url = "http://34.207.184.123/API/Sign_Up.php";
            response = PostIntrepreter.performPostCall(url, postParams);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("ourpicks", response);
            asyncDialog.dismiss();
            try {
                JSONObject jObject = new JSONObject(response);
                String error = jObject.optString("error");
                if (error.equalsIgnoreCase("false")) {
                    Toast.makeText(RegisterActivity.this,"Signup Successfull",Toast.LENGTH_SHORT).show();
                    Intent intent2=new Intent(RegisterActivity.this,SigninActivity.class);
                    startActivity(intent2);
                }
                else {
                    Toast.makeText(RegisterActivity.this,"Email ID or Phone Number Already Registered. Please Enter Another Valid Email ID or Phone Number",Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }
    }
