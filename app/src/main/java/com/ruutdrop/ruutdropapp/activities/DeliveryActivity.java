package com.ruutdrop.ruutdropapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.VoiceCatalog;
import com.here.android.mpa.guidance.VoicePackage;
import com.here.android.mpa.mapping.LocalMesh;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapLocalModel;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapOverlayType;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;
import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;
import com.ruutdrop.ruutdropapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * created by Aman on 12/11/16.
 */
public class DeliveryActivity extends AppCompatActivity {


    private Map map = null;

    private boolean simulate = false;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    int delay = 2000; //milliseconds
    Handler h;
    private String provider;
    private Context mContext;
    boolean stop;
    private boolean firstUpdate = true;
    boolean fromVerify = false;
    int coords = 1;
    String isRadius = "yes";
    String contactNumber = "9500834273";
    private Button logOut;

    /**

     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private static final String TAG = DeliveryActivity.class.getSimpleName();

    private PositioningManager mgr;
    // map fragment embedded in this activity
    private MapFragment mapFragment = null;
    private String currentpackageid, currenttag, currentadress;
    private double currentlat, deliverylat, navlat;
    private double currentlang, deliverylng, navlng;

    ArrayList<String> packageids;
    private static final int MY_LOCATION_REQUEST_CODE = 0x45;
    private boolean paused;
    private NavigationManager navigationManager;
    private ProgressDialog dialog;
    RelativeLayout readyToDeliverLayout, allBoxesDeliveredLayout, deliveryConfirmedLayout;
    LinearLayout youHaveArrivedLayout;
    private List<MapObject> mapObjects;

    private TextView navDistance, navText, etaText, navRoadName, address;
    private RelativeLayout mainNavView;
    private ImageView navIcon;

    TextView tag;
    Button verifyDeliveryBtn, startDeliveryBtn, finishDayBtn, nextDeliveryBtn,youhavearrived;
    private Switch voiceSwitch;
    private Button skip;
    private Button ihavearrivedbtn,showAddress,mapCenter;


    // helper for the very first fix after startup (we want to jump to that position then)
    private boolean firstPositionSet = false;

    // custom position marker
    private MapCircle m_PositionMarker;
    private MapCircle m_PositionAccuracyIndicator;
    private MapLocalModel m_PositionMesh;

    // compass sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    // compass data
    private float mAzimuth;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    // listen for sensor updates
    private SensorEventListener sensorHandler = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            final float alpha = (float) 0.8;
            synchronized (this) {

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // Isolate the force of gravity with the low-pass filter. See Android documentation for details:
                    // http://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-accel
                    mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                    mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                    mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
                }

                if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic = sensorEvent.values.clone();
                }
            }

            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];

                if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                    float[] mOrientation = new float[3];
                    SensorManager.getOrientation(R, mOrientation); // mOrientation contains: azimuth, pitch and roll

                    mAzimuth = (float) Math.toDegrees(mOrientation[0]);
                    //float mPitch = (float) Math.toDegrees(mOrientation[1]);
                    //float mRoll = (float) Math.toDegrees(mOrientation[2]);
                    //float mInclination = (float) Math.toDegrees(SensorManager.getInclination(I));

                    if (mAzimuth < 0.0f) {
                        mAzimuth += 360.0f;
                    }

                    Log.v(TAG, "Rotate to " + mAzimuth);

                    // set yaw of our 3D position indicator to indicate compass direction
                    if (m_PositionMesh != null)
                        m_PositionMesh.setYaw(-mAzimuth);  // Think about animation and less updates here in production environments
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            Log.d(TAG, "Accuracy changed for " + sensor.getName() + " to " + i);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        checkPermissions();
        statusCheck();

        stop = false;
        mapObjects = new ArrayList<>();
        dialog = new ProgressDialog(DeliveryActivity.this);
        dialog.setMessage("Calculating Route....");
        readyToDeliverLayout = (RelativeLayout) findViewById(R.id.readytToDeliverLayout);
        allBoxesDeliveredLayout = (RelativeLayout) findViewById(R.id.allBoxesDeliveredLayout);
        youHaveArrivedLayout = (LinearLayout) findViewById(R.id.youHaveArrivedLayout);
        startDeliveryBtn = (Button) findViewById(R.id.startDelivery);
        verifyDeliveryBtn = (Button) findViewById(R.id.verifyDeliveryBtn);
        finishDayBtn = (Button) findViewById(R.id.finishDay);
        nextDeliveryBtn = (Button) findViewById(R.id.nextDeliveryBtn);
        tag = (TextView) findViewById(R.id.tag);
        address = (TextView) findViewById(R.id.address);
        deliveryConfirmedLayout = (RelativeLayout) findViewById(R.id.deliveryConfirmedLayout);
        skip = (Button) findViewById(R.id.skipBtn);

        youhavearrived = (Button)findViewById(R.id.youhavearrived);
        final LinearLayout totallinearLayout = (LinearLayout)findViewById(R.id.totallinearLayout);



        youhavearrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(totallinearLayout.getVisibility() == View.VISIBLE)
                {
                    totallinearLayout.setVisibility(View.GONE);
                }
                else{
                    totallinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        logOut = (Button)findViewById(R.id.logOut);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                SharedPreferences sharedpreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                                sharedpreferences.edit().putBoolean("isloggedin",false).apply();
                                Intent signinactivity = new Intent(DeliveryActivity.this, SplashActivity.class);
                                signinactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                signinactivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                signinactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                DeliveryActivity.this.finish();
                                startActivity(signinactivity);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();


            }
        });
        try {
            contactNumber = getIntent().getStringExtra("contactNumber");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        showAddress = (Button)findViewById(R.id.showAddress);
        mapCenter = (Button)findViewById(R.id.mapCenter);

        showAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
                builder.setTitle("Address");
                builder.setMessage(currentadress)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Skip Delivery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
                                builder.setTitle("Please Confirm!");
                                builder.setMessage("Are you sure you want to skip this delivery? ")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, final int id) {
                                                dialog.dismiss();
                                                SkipDelivery();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });

                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        showAddress.setVisibility(View.GONE);
        mapCenter.setVisibility(View.GONE);
        mapCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
                GeoCoordinate coordinate = new GeoCoordinate(currentlat,currentlang);
                map.setCenter(coordinate, Map.Animation.LINEAR, 22.0d,(float)mgr.getPosition().getHeading(),0);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW_NOZOOM);
                    }
                },1500);
            }
        });

        ImageView callRuutdrop = (ImageView) findViewById(R.id.callRuutdrop);
        callRuutdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
                builder.setTitle("Support")
                        .setMessage("Are you sure you want to call Ruutdrop?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                Intent intent = new Intent(Intent.ACTION_CALL);

                                intent.setData(Uri.parse("tel:"+contactNumber));
                                if (ActivityCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    ActivityCompat.requestPermissions(DeliveryActivity.this,new String[]{Manifest.permission.CALL_PHONE},59);
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
        navRoadName = (TextView)findViewById(R.id.navRoadName);

        ihavearrivedbtn = (Button)findViewById(R.id.ihavearrivedbtn);
        ihavearrivedbtn.setVisibility(View.GONE);
        ihavearrivedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
                builder.setTitle("Please Confirm!")
                        .setMessage("Are you sure you have arrived?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                ihavearrivedbtn.setVisibility(View.GONE);
                                try {
                                    stop = true;
                                    allBoxesDeliveredLayout.setVisibility(View.GONE);
                                    readyToDeliverLayout.setVisibility(View.GONE);
                                    youHaveArrivedLayout.setVisibility(View.VISIBLE);
                                    deliveryConfirmedLayout.setVisibility(View.GONE);
                                    navigationManager.stop();
//                                    map.setCenter(new GeoCoordinate(deliverylat,deliverylng), Map.Animation.LINEAR, 22.0d,(float)mgr.getPosition().getHeading(),map.getOrientation());
                                    tag.setText(currenttag);
                                    for(int i=0; i<packageids.size();i++)
                                        new UpdatePackageTime("delivered_time",packageids.get(i)).execute();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                final AlertDialog alert = builder.create();
                alert.show();

            }
        });

        voiceSwitch = (Switch)findViewById(R.id.voiceSwitch);

        voiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                try {
                    if (!isChecked) {
                        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
//                        VoiceCatalog voiceCatalog = VoiceCatalog.getInstance();
//                        navigationManager.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(0));
                    } else {
                        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }


            }
        });
        navDistance = (TextView)findViewById(R.id.navDistance);
        navText = (TextView)findViewById(R.id.navText);
        etaText = (TextView)findViewById(R.id.etaText);
        mainNavView = (RelativeLayout) findViewById(R.id.mainNavView);

        navIcon = (ImageView)findViewById(R.id.navIcon);
        logOut.setVisibility(View.VISIBLE);
        mainNavView.setVisibility(View.GONE);
        startDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetNextDelivery().execute();
            }
        });

        verifyDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromVerify = true;
                Intent verifyDelviery = new Intent(DeliveryActivity.this,VerifyDeliveryActivity.class);
                verifyDelviery.putExtra("currentpackageid",currentpackageid);
                verifyDelviery.putStringArrayListExtra("packageids",packageids);
                startActivity(verifyDelviery);

            }
        });


        finishDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateWarehouseTime("isDeliveryComplete").execute();

            }
        });


        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromVerify = false;
                deliveryConfirmedLayout.setVisibility(View.VISIBLE);
                youHaveArrivedLayout.setVisibility(View.GONE);
                allBoxesDeliveredLayout.setVisibility(View.GONE);
                readyToDeliverLayout.setVisibility(View.GONE);
                mainNavView.setVisibility(View.GONE);
                logOut.setVisibility(View.VISIBLE);
                ihavearrivedbtn.setVisibility(View.GONE);
                mapCenter.setVisibility(View.GONE);
                showAddress.setVisibility(View.GONE);
                for(int i=0; i<packageids.size();i++)
                    new UpdatePackageTime("isDelivered",packageids.get(i)).execute();

            }
        });

        nextDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new GetNextDelivery().execute();
            }
        });

    }

    private void SkipDelivery() {
        for(int i=0; i<packageids.size();i++)
            new SkipDelivery(packageids.get(i)).execute();
    }


    public class SkipDelivery extends AsyncTask<Void,Void,Void>
    {

        private final String packageid;
        private String response;

        public SkipDelivery(String packageid) {
            this.packageid = packageid;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("packageid",packageid);
            String url = "http://34.207.184.123/API/skipDelivery.php";

            response = PostIntrepreter.performPostCall(url, postParams);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                Log.d("text", response);

                JSONObject jObject = new JSONObject(response);
                if(jObject.optString("error").equalsIgnoreCase("false")) {
                    fromVerify = false;
                    navigationManager.stop();
                    deliveryConfirmedLayout.setVisibility(View.VISIBLE);
                    youHaveArrivedLayout.setVisibility(View.GONE);
                    allBoxesDeliveredLayout.setVisibility(View.GONE);
                    readyToDeliverLayout.setVisibility(View.GONE);
                    mainNavView.setVisibility(View.GONE);
                    logOut.setVisibility(View.VISIBLE);
                    ihavearrivedbtn.setVisibility(View.GONE);
                    mapCenter.setVisibility(View.GONE);
                    showAddress.setVisibility(View.GONE);
                }
                else
                {
                    Toast.makeText(DeliveryActivity.this,"Something went wrong! Please try again.",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(DeliveryActivity.this,"Something went wrong! Please Try again",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }

    public class GetNextDelivery extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        private String response;

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            String url = "http://34.207.184.123/API/getNextDelivery.php";
            response = PostIntrepreter.performPostCall(url, postParams);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            try {
                Double lat=0.0,lng=0.0;
                Log.d("text", response);
                JSONObject jObject = new JSONObject(response);
                try {
                    youHaveArrivedRadius = jObject.optDouble("youHaveArrived", 30);
                    iHaveArrivedRadius = jObject.optDouble("iHaveArrived", 30);
                    coords = jObject.optInt("coords");
                    isRadius = jObject.optString("isRadius");
                    contactNumber = jObject.optString("contactNumber");
                    getSharedPreferences("Prefs",Context.MODE_PRIVATE).edit().putString("contactNumber",contactNumber).apply();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(jObject.optString("error").equalsIgnoreCase("false")){

                    JSONArray packages = jObject.optJSONArray("nextPackage");
                    JSONObject nextpackage = packages.optJSONObject(0);
                    deliverylat = nextpackage.optDouble("latitude");
                    deliverylng = nextpackage.optDouble("longitude");
                    navlat = nextpackage.optDouble("navigationLatitude");
                    navlng = nextpackage.optDouble("navigationLongitude");
                    currentpackageid = nextpackage.optString("id");

                    String street  = nextpackage.optString("street");
                    String city = nextpackage.optString("city");
                    String state = nextpackage.optString("state");
                    String pincode = nextpackage.optString("pincode");

                    packageids = new ArrayList<>();
                    currenttag = "";
                    for(int i =0;i<packages.length();i++)
                    {
                        JSONObject singlepackage = packages.optJSONObject(i);
                        packageids.add(singlepackage.optString("id"));
                        if(i == 0)
                        {
                            currenttag = currenttag+singlepackage.optString("tag");
                        }
                        else{
                            currenttag = currenttag+",";
                            currenttag = currenttag+singlepackage.optString("tag");
                        }
                        new UpdatePackageTime("start_time",singlepackage.optString("id")).execute();
                        tag.setText(currenttag);

                    }

                    currentadress = street+"\n"+city+" "+state+"-"+pincode;
                    address.setText(currentadress);
                    getDirections();
                }
                else{
                    if(jObject.optString("reason").equalsIgnoreCase("incomplete"))
                    {

                        JSONArray packages = jObject.optJSONArray("nextPackage");
                        JSONObject nextpackage = packages.optJSONObject(0);
                        deliverylat = nextpackage.optDouble("latitude");
                        deliverylng = nextpackage.optDouble("longitude");
                        navlat = nextpackage.optDouble("navigationLatitude");
                        navlng = nextpackage.optDouble("navigationLongitude");
                        currentpackageid = nextpackage.optString("id");

                        String street  = nextpackage.optString("street");
                        String city = nextpackage.optString("city");
                        String state = nextpackage.optString("state");
                        String pincode = nextpackage.optString("pincode");

                        packageids = new ArrayList<>();
                        currenttag = "";
                        for(int i =0;i<packages.length();i++)
                        {
                            JSONObject singlepackage = packages.optJSONObject(i);
                            packageids.add(singlepackage.optString("id"));
                            if(i == 0)
                            {
                                currenttag = currenttag+singlepackage.optString("tag");
                            }
                            else{
                                currenttag = currenttag+",";
                                currenttag = currenttag+singlepackage.optString("tag");
                            }
                            new UpdatePackageTime("start_time",singlepackage.optString("id")).execute();
                        }
                        tag.setText(currenttag);
                        currentadress = street+"\n"+city+" "+state+"-"+pincode;
                        address.setText(currentadress);

                        allBoxesDeliveredLayout.setVisibility(View.GONE);
                        youHaveArrivedLayout.setVisibility(View.VISIBLE);
                        readyToDeliverLayout.setVisibility(View.GONE);
                    }
                    else{
                        allBoxesDeliveredLayout.setVisibility(View.VISIBLE);
                        readyToDeliverLayout.setVisibility(View.GONE);
                        youHaveArrivedLayout.setVisibility(View.GONE);
                        deliveryConfirmedLayout.setVisibility(View.GONE);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(DeliveryActivity.this,"Something went wrong! Please Try again",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void initialize() {
        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(
                R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)

                    // Set the zoom level to the average between min and max
                    map.setZoomLevel(17);
                    mapFragment.getPositionIndicator().setVisible(true);
                    mapFragment.getPositionIndicator().setAccuracyIndicatorVisible(true);
                    mgr = PositioningManager.getInstance();
                    navigationManager = NavigationManager.getInstance();
                    navigationManager.startTracking();


                    mgr.addListener(
                            new WeakReference<>(positionListener));
                    mgr.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR);
                    navigationManager.addPositionListener(
                            new WeakReference<>(positionlistener));
                    navigationManager.addManeuverEventListener(new WeakReference<>(newManeuverEvenListener));
                    navigationManager.addNewInstructionEventListener(new WeakReference<>(newInstructionEventListener));
                    navigationManager.addAudioFeedbackListener(new WeakReference<>(audioFeedbackListener));
                    navigationManager.addNavigationManagerEventListener(new WeakReference<>(navigationManagerEventListener));
                    navigationManager.addRerouteListener(new WeakReference<>(rerouteListener));
                    navigationManager.setMap(map);


                    try {
                        final VoiceCatalog voiceCatalog = VoiceCatalog.getInstance();
//                    voice
                        List<VoicePackage> voicePackages = VoiceCatalog.getInstance().getCatalogList();
                        long id = -1;
                        for (VoicePackage vpackage : voicePackages) {
                            System.out.println(vpackage.getMarcCode() + " :" + vpackage.getDownloadSize());
                            if (vpackage.getMarcCode().compareToIgnoreCase("eng") == 0) {
                                if (vpackage.isTts()) {
                                    id = vpackage.getId();
                                    break;
                                }
                            }
                        }
                        if (!voiceCatalog.isLocalVoiceSkin(id)) {
                            final long finalId = id;
                            voiceCatalog.downloadVoice(id, new VoiceCatalog.OnDownloadDoneListener() {

                                @Override
                                public void onDownloadDone(VoiceCatalog.Error error) {
                                    if (error == VoiceCatalog.Error.NONE) {
                                        System.out.println("voice download successful");
                                        navigationManager.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(finalId));
                                    } else {
                                        System.out.println("voice download failed " + error);
                                    }
                                }
                            });
                        } else {
                            navigationManager.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(id));
                        }
                        navigationManager.setDistanceUnit(NavigationManager.UnitSystem.IMPERIAL_US);

                        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                        voiceSwitch.setChecked(true);


                        // create a 3D mesh as position marker, since we can then use yaw to rotate
//                        createPositionMarkerMesh();

                        // create a custom position indicator dot
                        m_PositionMarker = new MapCircle();
                        m_PositionMarker.setFillColor(Color.argb(200, 0, 200, 0));
                        m_PositionMarker.setLineWidth(3);
                        m_PositionMarker.setLineColor(Color.BLACK);
                        m_PositionMarker.setRadius(3);

                        // create a custom accuracy indicator circle
                        m_PositionAccuracyIndicator = new MapCircle();
                        m_PositionAccuracyIndicator.setFillColor(Color.argb(70, 0, 200, 0)); // translucent
                        m_PositionAccuracyIndicator.setLineWidth(3);
                        m_PositionAccuracyIndicator.setLineColor(Color.BLACK);
                        m_PositionAccuracyIndicator.setRadius(20);
                        m_PositionAccuracyIndicator.setOverlayType(MapOverlayType.ROAD_OVERLAY); // put accuracy indicator behind buildings in render stack

                        // add this to the map. at this moment we still didn't define the position, we'll do this later on position updates
//                        map.addMapObject(m_PositionAccuracyIndicator);
//                        map.addMapObject(m_PositionMarker);




                        // set to last known position, if available
//                        GeoPosition lkp = PositioningManager.getInstance().getLastKnownPosition();
//                        if (lkp != null && lkp.isValid()) {
//                            map.setCenter(lkp.getCoordinate(), Map.Animation.NONE);
//
//                            // set custom position indicator
//                            m_PositionMarker.setCenter(lkp.getCoordinate());
//                            m_PositionAccuracyIndicator.setCenter(lkp.getCoordinate());
//                            m_PositionMesh.setAnchor(new GeoCoordinate(lkp.getCoordinate().getLatitude(), lkp.getCoordinate().getLongitude())); // ignoring altitude, since it would also set the imnage on this height
//                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment" + error.toString());
                }
            }
        });
    }

    private void createPositionMarkerMesh() {

        FloatBuffer buff = FloatBuffer.allocate(9);

        buff.put(0);
        buff.put(0.5f);
        buff.put(0);
        buff.put(0.2f);
        buff.put(-0.3f);
        buff.put(0);
        buff.put(-0.2f);
        buff.put(-0.3f);
        buff.put(0);

        IntBuffer vertIndicieBuffer = IntBuffer.allocate(3);
        vertIndicieBuffer.put(2);
        vertIndicieBuffer.put(1);
        vertIndicieBuffer.put(0);

        LocalMesh myMesh = new LocalMesh();
        myMesh.setVertices(buff);
        myMesh.setVertexIndices(vertIndicieBuffer);

        m_PositionMesh = new MapLocalModel();
        m_PositionMesh.setMesh(myMesh);
        m_PositionMesh.setScale(2.5f);
        m_PositionMesh.setDynamicScalingEnabled(true); // keep size when zooming

        map.addMapObject(m_PositionMesh); // add mesh to map. we set position later when we have the first reliable information
    }
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }
    NavigationManager.ManeuverEventListener newManeuverEvenListener = new NavigationManager.ManeuverEventListener() {
        @Override
        public void onManeuverEvent() {
            super.onManeuverEvent();
        }
    };

    private MapPolyline mapPolyline;
    private NavigationManager.NavigationManagerEventListener navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {
        @Override
        public void onRunningStateChanged() {
            super.onRunningStateChanged();
        }

        @Override
        public void onNavigationModeChanged() {
            super.onNavigationModeChanged();
        }

        @Override
        public void onEnded(NavigationManager.NavigationMode navigationMode) {
//            super.onEnded(navigationMode);
            try {
                stop = true;
                navigationManager.stop();
                allBoxesDeliveredLayout.setVisibility(View.GONE);
                readyToDeliverLayout.setVisibility(View.GONE);
                youHaveArrivedLayout.setVisibility(View.VISIBLE);
                deliveryConfirmedLayout.setVisibility(View.GONE);
                List<GeoCoordinate> testPoints = new ArrayList<GeoCoordinate>();
                testPoints.add(new GeoCoordinate(currentlat, currentlang, 10));
                testPoints.add(new GeoCoordinate(deliverylat, deliverylng, 10));
                GeoPolyline polyline = new GeoPolyline(testPoints);
                map.removeMapObject(mapPolyline);
                mapPolyline = new MapPolyline(polyline);
                mapPolyline.setLineWidth(15);
                mapObjects.add(mapPolyline);
                map.addMapObject(mapPolyline);
                map.setZoomLevel(23.0);
//                map.setCenter(new GeoCoordinate(deliverylat,deliverylng), Map.Animation.LINEAR, 22.0d,(float)mgr.getPosition().getHeading(),0);
                tag.setText(currenttag);
                for(int i=0; i<packageids.size();i++)
                    new UpdatePackageTime("delivered_time",packageids.get(i)).execute();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onMapUpdateModeChanged(NavigationManager.MapUpdateMode mapUpdateMode) {
            super.onMapUpdateModeChanged(mapUpdateMode);
        }

        @Override
        public void onRouteUpdated(Route route) {
            super.onRouteUpdated(route);
        }

        @Override
        public void onCountryInfo(String s, String s1) {
            super.onCountryInfo(s, s1);
        }
    };
    private NavigationManager.NewInstructionEventListener newInstructionEventListener = new NavigationManager.NewInstructionEventListener() {
        @Override
        public void onNewInstructionEvent() {
//            super.onNewInstructionEvent();
            try{
            Maneuver maneuver = navigationManager.getNextManeuver();
            if (maneuver != null) {
                if (maneuver.getAction() == Maneuver.Action.END) {
                    //notify the user that the route is complete
                }
                int dis = maneuver.getDistanceFromPreviousManeuver();
                dis = (dis / 100) * 100;
                double disinkm = dis / 1000;
                double disinmiles = disinkm/1.609344;
                System.out.println("disimiles before"+String.valueOf(disinmiles));

                disinmiles = round(disinmiles,2);
                System.out.println("disimiles after"+String.valueOf(disinmiles));

                if(disinmiles == 0.0)
                {
                    navDistance.setText("0.1 mi");
                }
                else{
                    if(disinmiles < 1334)
                        navDistance.setText(disinmiles+" mi");
                    else
                        navDistance.setText("0 mi");
                }

                navText.setText(ManeuverHelper.getNextManeuver(maneuver));
                navIcon.setImageResource(ManeuverHelper.getManeuverIcon(maneuver));
                navRoadName.setText(maneuver.getNextRoadName());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private NavigationManager.RerouteListener rerouteListener = new NavigationManager.RerouteListener() {
        @Override
        public void onRerouteBegin() {
            super.onRerouteBegin();
        }

        @Override
        public void onRerouteEnd(Route route) {
            super.onRerouteEnd(route);
            try{
                if(route != null) {
                    map.removeMapObject(mapRoute);
                    mapRoute = new MapRoute(route);
                    map.addMapObject(mapRoute);
                    mapObjects.add(mapRoute);
                    navigationManager.stop();
                    if (simulate) {
                        NavigationManager.Error error = navigationManager.simulate(route, 150);
                    } else {
                        NavigationManager.Error error = navigationManager.startNavigation(route);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onRerouteFailed() {
            super.onRerouteFailed();
        }
    };

    private NavigationManager.AudioFeedbackListener audioFeedbackListener = new NavigationManager.AudioFeedbackListener() {
        @Override
        public void onAudioStart() {
            super.onAudioStart();
        }

        @Override
        public void onAudioEnd() {
            super.onAudioEnd();
        }

        @Override
        public void onVibrationStart() {
            super.onVibrationStart();
        }

        @Override
        public void onVibrationEnd() {
            super.onVibrationEnd();
        }
    };

    private MapRoute mapRoute;
    private double youHaveArrivedRadius = 30;
    private double iHaveArrivedRadius = 150;
    private CoreRouter.Listener routerListener =
            new CoreRouter.Listener() {
                public void onCalculateRouteFinished(List<RouteResult> routeResults,
                                                     RoutingError errorCode) {
                    dialog.dismiss();
                    if (errorCode == RoutingError.NONE && routeResults.get(0).getRoute() != null) {
                        // create a map route object and place it on the map
                        allBoxesDeliveredLayout.setVisibility(View.GONE);
                        readyToDeliverLayout.setVisibility(View.GONE);
                        youHaveArrivedLayout.setVisibility(View.GONE);
                        deliveryConfirmedLayout.setVisibility(View.GONE);


                        mapRoute = new MapRoute(routeResults.get(0).getRoute());
                        mapObjects.add(mapRoute);
                        map.addMapObject(mapRoute);
                        // Get the bounding box containing the route and zoom in
//                        GeoBoundingBox gbb = routeResults.get(0).getRoute().getBoundingBox();

                        map.setMapScheme(Map.Scheme.CARNAV_DAY);
                        map.setZoomLevel(19);
                        map.setTilt(0);

                        h = new Handler();
                        navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW_NOZOOM);

                        logOut.setVisibility(View.GONE);
                        mainNavView.setVisibility(View.VISIBLE);
                        showAddress.setVisibility(View.VISIBLE);
                        mapCenter.setVisibility(View.VISIBLE);
                        showAddress.setVisibility(View.VISIBLE);
                        h.postDelayed(new Runnable(){
                            public void run(){
                                try {
                                    Date date = navigationManager.getEta(true, Route.TrafficPenaltyMode.OPTIMAL);
                                    SimpleDateFormat localDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                                    String time = localDateFormat.format(date);
                                    double dis = navigationManager.getNextManeuverDistance();
                                    dis = (dis / 100) * 100;
                                    System.out.println("dis " + String.valueOf(dis));
                                    double disinkm = dis / 1000;

                                    System.out.println("disinkm " + String.valueOf(disinkm));

                                    double disinmiles = disinkm / 1.609344;
                                    System.out.println("disimiles before" + String.valueOf(disinmiles));

                                    disinmiles = round(disinmiles, 2);
                                    System.out.println("disimiles after" + String.valueOf(disinmiles));

                                    if (disinmiles == 0.0) {
                                        navDistance.setText("0.1 mi");
                                    } else {
                                        if (disinmiles < 1334)
                                            navDistance.setText(disinmiles + " mi");
                                        else
                                            navDistance.setText("0 mi");
                                    }
                                    Maneuver maneuver = navigationManager.getNextManeuver();
                                    if (maneuver != null) {
                                        navText.setText(ManeuverHelper.getNextManeuver(maneuver));
                                        navIcon.setImageResource(ManeuverHelper.getManeuverIcon(maneuver));
                                        navRoadName.setText(maneuver.getNextRoadName());
                                        etaText.setText(time);
                                    }
//                                    Location locationA = new Location("point A");
//
//                                    locationA.setLatitude(currentlat);
//                                    locationA.setLongitude(currentlang);
//
//                                    Location locationB = new Location("point B");
//
//                                    locationB.setLatitude(deliverylat);
//                                    locationB.setLongitude(deliverylng);

                                    Location locationA = new Location("point A");

                                    locationA.setLatitude(currentlat);
                                    locationA.setLongitude(currentlang);

                                    Location locationB = new Location("point B");

                                    locationB.setLatitude(deliverylat);
                                    locationB.setLongitude(deliverylng);

                                    float distanceinmetres = locationA.distanceTo(locationB);
                                    float distance = navigationManager.getDestinationDistance();
                                    System.out.println("distance to destination"+distance);
                                    distance = (distance / 100) * 100;
                                    System.out.println("dis "+String.valueOf(distance));
                                    double distanceinkm = distance / 1000;

                                    System.out.println("distance in metres "+String.valueOf(distanceinmetres));

                                    System.out.println("iHaveArrivedRadius"+String.valueOf(iHaveArrivedRadius));
                                    System.out.println("youHaveArrivedRadius"+String.valueOf(youHaveArrivedRadius));


                                    if(!stop)
                                    {
                                        if(distanceinmetres < iHaveArrivedRadius) {
                                            ihavearrivedbtn.setVisibility(View.VISIBLE);
                                        }
                                        else {
                                            ihavearrivedbtn.setVisibility(View.GONE);
                                        }
                                    }
                                    if(distanceinmetres < youHaveArrivedRadius) {
                                        try {
                                            stop = true;
                                            allBoxesDeliveredLayout.setVisibility(View.GONE);
                                            readyToDeliverLayout.setVisibility(View.GONE);
                                            youHaveArrivedLayout.setVisibility(View.VISIBLE);
                                            deliveryConfirmedLayout.setVisibility(View.GONE);
                                            List<GeoCoordinate> testPoints = new ArrayList<GeoCoordinate>();
                                            testPoints.add(new GeoCoordinate(currentlat, currentlang, 10));
                                            testPoints.add(new GeoCoordinate(deliverylat, deliverylng, 10));
                                            GeoPolyline polyline = new GeoPolyline(testPoints);
                                            map.removeMapObject(mapPolyline);
                                            mapPolyline = new MapPolyline(polyline);
                                            mapPolyline.setLineWidth(20);
                                            mapObjects.add(mapPolyline);
                                            ihavearrivedbtn.setVisibility(View.GONE);


                                            if(!stop)
                                                map.addMapObject(mapPolyline);
                                            map.setZoomLevel(22.0);
//                                            map.setCenter(new GeoCoordinate(deliverylat,deliverylng), Map.Animation.LINEAR, 22.0d,(float)mgr.getPosition().getHeading(),0);
                                            tag.setText(currenttag);
                                            ihavearrivedbtn.setVisibility(View.GONE);
                                            navigationManager.stop();
                                            for(int i=0; i<packageids.size();i++)
                                                new UpdatePackageTime("delivered_time",packageids.get(i)).execute();
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
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
                        if(simulate) {
                            NavigationManager.Error error = navigationManager.simulate(routeResults.get(0).getRoute(),100);
                        }
                        else{
                            NavigationManager.Error error = navigationManager.startNavigation(routeResults.get(0).getRoute());
                        }


                    } else {
                        Toast.makeText(DeliveryActivity.this, "Problem with GPS! Try again later.", Toast.LENGTH_SHORT).show();
                        allBoxesDeliveredLayout.setVisibility(View.GONE);
                        readyToDeliverLayout.setVisibility(View.VISIBLE);
                        youHaveArrivedLayout.setVisibility(View.GONE);
                        deliveryConfirmedLayout.setVisibility(View.GONE);
                    }
                }

                public void onProgress(int percentage) {

                    System.out.println("Percentage" + percentage);
                }
            };

    // Functionality for taps of the "Get Directions" button
    public void getDirections() {
        // 1. clear previous results

        if(mapObjects!= null)
        map.removeMapObjects(mapObjects);
        dialog.show();

        // 2. Initialize CoreRouter
        CoreRouter coreRouter = new CoreRouter();
        // 3. Select routing options via RoutingMode
        RoutePlan routePlan = new RoutePlan();
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routeOptions.setStartDirection(RouteOptions.START_DIRECTION_ANY);
        routePlan.setRouteOptions(routeOptions);
        // 4. Select Waypoints for your routes
        // START: BC Place Stadium
        com.here.android.mpa.common.Image myImage =
                new com.here.android.mpa.common.Image();
        try {
            myImage.setImageResource(R.drawable.mapicon);
        } catch (IOException e) {
            finish();
        }

        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(currentlat, currentlang)));
//        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(47.386435, -122.047097)));
        try {
            if(coords == 1) {
                routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(deliverylat, deliverylng)));
            }
            else{
                routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(navlat, navlng)));
            }
        }
        catch (Exception e)
        {
            routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(deliverylat, deliverylng)));
            e.printStackTrace();
        }

        // END: Airport, YVR 47.386435, -122.047097
//        routePlan.addWaypoint(new GeoCoordinate(49.1947289, -123.1762924));
        // 5. Retrieve Routing information via CoreRouter.Listener
        coreRouter.calculateRoute(routePlan, routerListener);



        MapMarker pickupmarker = new MapMarker(new GeoCoordinate(deliverylat, deliverylng), myImage);
        ClusterLayer cl = new ClusterLayer();
        cl.addMarker(pickupmarker);
        map.addClusterLayer(cl);

        if(map.addMapObject(pickupmarker)){
            System.out.println("marker added");
            mapObjects.add(pickupmarker);
        }
        else
        {
            System.out.println("marker not added");
        }
    }


    private PositioningManager.OnPositionChangedListener positionListener = new
            PositioningManager.OnPositionChangedListener() {

                public void onPositionUpdated(PositioningManager.LocationMethod method,
                                              GeoPosition position, boolean isMapMatched) {
                    // set the center only when the app is in the foreground
                    // to reduce CPU consumption
                    if (!position.isValid())
                        return;

                    if (!firstPositionSet) {
                        map.setCenter(position.getCoordinate(), Map.Animation.BOW);
                        firstPositionSet = true;
                    }

                    try {
                        // get the new coordinate
                        GeoCoordinate pos = position.getCoordinate();

                        // set custom position indicator and accuracy indicator
                        m_PositionMarker.setCenter(pos);
                        m_PositionMesh.setAnchor(pos);
                        m_PositionAccuracyIndicator.setCenter(pos);
                        m_PositionAccuracyIndicator.setRadius(position.getLatitudeAccuracy());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (!paused) {

                        currentlang = position.getCoordinate().getLongitude();
                        currentlat = position.getCoordinate().getLatitude();
                        if(firstUpdate) {
                            firstUpdate = false;
                            map.setCenter(position.getCoordinate(),
                                    Map.Animation.LINEAR, 17.0d, (float) position.getHeading(), 0);
                        }

                    }
                }

                public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                                 PositioningManager.LocationStatus status) {
                    if (method == PositioningManager.LocationMethod.GPS_NETWORK_INDOOR) {
                        PositioningManager.getInstance().getPosition();
                    }
                }

//                boolean isExtrapolated =
//                        ((mgr.getRoadElement() != null) &&
//                                ((mgr.getRoadElement().getAttributes()
//                                        .contains(RoadElement.Attribute.TUNNEL))));
//                boolean hasGps = status == PositioningManager.LocationStatus.AVAILABLE;
            };

    private NavigationManager.PositionListener positionlistener
            = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition loc) {

            currentlang = loc.getCoordinate().getLongitude();
            currentlat = loc.getCoordinate().getLatitude();
//            map.setCenter(loc.getCoordinate(),
//                    Map.Animation.LINEAR, 17.0d, (float) loc.getHeading(), 0);
        }
    };
    public void onResume() {
        super.onResume();
        paused = false;
        if (mgr != null) {
            mgr.start(
                    PositioningManager.LocationMethod.GPS_NETWORK_INDOOR);
        }
        mSensorManager.registerListener(sensorHandler, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorHandler, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        if(fromVerify )
        {
            fromVerify = false;
            deliveryConfirmedLayout.setVisibility(View.VISIBLE);
            youHaveArrivedLayout.setVisibility(View.GONE);
            allBoxesDeliveredLayout.setVisibility(View.GONE);
            readyToDeliverLayout.setVisibility(View.GONE);
            mainNavView.setVisibility(View.GONE);
            logOut.setVisibility(View.VISIBLE);
            mapCenter.setVisibility(View.GONE);
            showAddress.setVisibility(View.GONE);

        }

    }

    // To pause positioning listener
    public void onPause() {
        if (mgr != null) {
            mgr.stop();
        }
        mSensorManager.unregisterListener(sensorHandler);

        super.onPause();
        paused = true;
    }

    // To remove the positioning listener
    public void onDestroy() {
        if (mgr != null) {
            // Cleanup
            mgr.removeListener(
                    positionListener);
        }
        map = null;
        super.onDestroy();
    }

    class UpdatePackageTime extends AsyncTask<Void,Void,Void>
    {

        private String response,tag,packageid;

        public UpdatePackageTime(String tag,String packageid) {
            this.tag = tag;
            this.packageid = packageid;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            postParams.put("tag",tag);
            postParams.put("packageid",packageid);
            String url = "http://34.207.184.123/API/UpdatePackage.php";

            response = PostIntrepreter.performPostCall(url, postParams);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                Log.d("text", response);

            } catch (Exception e) {
                Toast.makeText(DeliveryActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }

    }
    class UpdateWarehouseTime extends AsyncTask<Void,Void,Void>
    {

        private String response,tag;

        public UpdateWarehouseTime(String tag) {
            this.tag = tag;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            postParams.put("tag",tag);
            String url = "http://34.207.184.123/API/updatePickupTimeStamp.php";

            response = PostIntrepreter.performPostCall(url, postParams);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                Log.d("text", response);

                    Intent main = new Intent(DeliveryActivity.this,SplashActivity.class);
                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    DeliveryActivity.this.finish();
                    startActivity(main);
                    getSharedPreferences("Prefs",Context.MODE_PRIVATE).edit().putBoolean("isRouting",false).apply();
            } catch (Exception e) {
                Toast.makeText(DeliveryActivity.this,"Something went wrong! Please Try again",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }

    }
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    public void onBackPressed() {

    }
}