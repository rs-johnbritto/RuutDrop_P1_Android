package com.ruutdrop.ruutdropapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ruutdrop.ruutdropapp.R;
import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by karthik on 12/03/17.
 */
public class BoxesComputingActivity extends AppCompatActivity{
    int delay = 2000; //milliseconds
    String contactNumber = "9500834273";
    boolean stop = false;
    private boolean hasResponseCome = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxes_computing);

        try {
            contactNumber = getIntent().getStringExtra("contactNumber");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        new SortBoxes().execute();



        ImageView callRuutdrop = (ImageView) findViewById(R.id.callRuutdrop);
        callRuutdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(BoxesComputingActivity.this);
                builder.setTitle("Support")
                        .setMessage("Are you sure you want to call Ruutdrop?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                Intent intent = new Intent(Intent.ACTION_CALL);

                                intent.setData(Uri.parse("tel:"+contactNumber));
                                if (ActivityCompat.checkSelfPermission(BoxesComputingActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    ActivityCompat.requestPermissions(BoxesComputingActivity.this,new String[]{Manifest.permission.CALL_PHONE},59);
                                    return;
                                }
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        });
        final Handler h = new Handler();

        h.postDelayed(new Runnable(){
            public void run(){
                try {

                    if(!hasResponseCome)
                    {
                        hasResponseCome = true;
                        new ConfirmSorting().execute();
                    }

                    if (!stop)
                        h.postDelayed(this, delay);
                }
                catch (Exception e)
                {
                            e.printStackTrace();
                }
            }

        }, delay);
    }

    @Override
    public void onBackPressed() {

    }


    class ConfirmSorting extends AsyncTask<Void,Void,Void>
    {
        private String response;


        @Override
        protected Void doInBackground(Void... voids) {

            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            String url = "http://34.207.184.123/API/ConfirmSorting.php";

            response = PostIntrepreter.performPostCall(url, postParams);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hasResponseCome = false;
            try {
                Log.d("text", response);
                JSONObject jObject = new JSONObject(response);
                String error = jObject.optString("error");
                if (error.equalsIgnoreCase("false")) {
                    stop = true;
                    Intent boxes = new Intent(BoxesComputingActivity.this,ListBoxesActivity.class);
                    boxes.putExtra("contactNumber",contactNumber);
                    startActivity(boxes);
                }
            } catch (Exception e) {
//                Toast.m
                e.printStackTrace();
            }

        }
    }

    class SortBoxes extends AsyncTask<Void,Void,Void>
    {

        private String response;

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(BoxesComputingActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage("Please Wait....");
//            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            String url = "http://34.207.184.123/API/sortBoxes.php";

            response = PostIntrepreter.performPostCall(url, postParams);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            try {
                Log.d("text", response);
                JSONObject jObject = new JSONObject(response);
                String error = jObject.optString("error");
                if (error.equalsIgnoreCase("false")) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
