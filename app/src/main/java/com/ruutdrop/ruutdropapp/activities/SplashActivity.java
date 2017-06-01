package com.ruutdrop.ruutdropapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ruutdrop.ruutdropapp.R;
import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Aman on 09-10-2016.
 */

public class SplashActivity extends AppCompatActivity {

    private double youHaveArrivedRadius = 30;
    private double iHaveArrivedRadius = 150;
    String isRadius = "yes";
    String contactNumber = "9500834273";
    int coords = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int SPLASH_DISPLAY_LENGTH = 1000;
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Boolean isLoggedIn = getSharedPreferences("Prefs", Context.MODE_PRIVATE).getBoolean("isloggedin",false);
                if(isLoggedIn){
                    new GetPickupLocation().execute();
                }
                else {
                    Intent mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
                    startActivity(mainIntent);
                    SplashActivity.this.finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
    public class GetPickupLocation extends AsyncTask<Void,Void,Void>
    {


        private String response;

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            Log.d("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            String url = "http://34.207.184.123/API/getNewPickup.php";
            response = PostIntrepreter.performPostCall(url, postParams);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                Log.d("get new", response);

                JSONObject jObject = new JSONObject(response);
                youHaveArrivedRadius = jObject.optDouble("youHaveArrived", 30);
                iHaveArrivedRadius = jObject.optDouble("iHaveArrived", 30);
                coords = jObject.optInt("coords");
                isRadius = jObject.optString("isRadius");
                contactNumber = jObject.optString("contactNumber");
                getSharedPreferences("Prefs",Context.MODE_PRIVATE).edit().putString("contactNumber",contactNumber).apply();

                if(jObject.optString("status").equalsIgnoreCase("start") || jObject.optString("status").equalsIgnoreCase("onroute")){
                    Intent home = new Intent(SplashActivity.this,MainActivity.class);
                    home.putExtra("status","start");
                    home.putExtra("contactNumber",contactNumber);
                    SplashActivity.this.finish();
                    startActivity(home);

                }
                else  if(jObject.optString("status").equalsIgnoreCase("startscan"))
                {
                    JSONArray pickuppoints = jObject.optJSONArray("pickups");
                    JSONObject object = pickuppoints.getJSONObject(0);

                    String pickupcontact = object.optString("contactNumber");
                    String street  = object.optString("address");
                    String city = object.optString("city");
                    String state = object.optString("state");
                    String pincode = object.optString("pincode");

                    String currentadress = street + "\n" + city + " " + state + "-" + pincode;

                    Intent home = new Intent(SplashActivity.this,MainActivity.class);
                    home.putExtra("status","startscan");
                    SplashActivity.this.finish();
                    home.putExtra("currentaddress",currentadress);
                    home.putExtra("contactNumber",contactNumber);
                    home.putExtra("pickupcontact",pickupcontact);
                    startActivity(home);
                }
                else if(jObject.optString("status").equalsIgnoreCase("resumescan"))
                {
                    Intent home = new Intent(SplashActivity.this,MainActivity.class);
                    home.putExtra("status","resumescan");
                    home.putExtra("contactNumber",contactNumber);
                    SplashActivity.this.finish();
                    startActivity(home);
                }
                else if(jObject.optString("status").equalsIgnoreCase("sortboxes")){
                    Intent boxes = new Intent(SplashActivity.this,BoxesComputingActivity.class);
                    boxes.putExtra("contactNumber",contactNumber);
                    SplashActivity.this.finish();
                    startActivity(boxes);
                }
                else if(jObject.optString("status").equalsIgnoreCase("showboxes")){
                    Intent boxes = new Intent(SplashActivity.this,ListBoxesActivity.class);
                    boxes.putExtra("contactNumber",contactNumber);
                    SplashActivity.this.finish();
                    startActivity(boxes);
                }
                else if(jObject.optString("status").equalsIgnoreCase("startdelivery")){
                    Intent boxes = new Intent(SplashActivity.this,DeliveryActivity.class);
                    SplashActivity.this.finish();
                    boxes.putExtra("contactNumber",contactNumber);
                    startActivity(boxes);
                }

            } catch (JSONException e) {
                Toast.makeText(SplashActivity.this,"Something went wrong!!",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }

    }
}
