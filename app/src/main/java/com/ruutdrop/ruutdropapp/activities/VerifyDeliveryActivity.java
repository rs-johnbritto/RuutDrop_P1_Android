package com.ruutdrop.ruutdropapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;
import com.ruutdrop.ruutdropapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * created by Aman on 21/12/16.
 */

public class VerifyDeliveryActivity extends AppCompatActivity {
    private SignaturePad signature_pad;
    private ProgressDialog progressDialog;
    private ImageView verifyImage;
    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;
    private String currentpackageid;
    private String contactNumber = "9500834273";

    public static String accessKey = "AKIAJYD6TZZK2IR5M54A";
    public static String secretKey = "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";
    public static String bucket_name = "ruutdrop";
    AmazonS3Client s3Client;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_delivery);

        progressDialog = new ProgressDialog(VerifyDeliveryActivity.this);
        progressDialog.setMessage("Please Wait..");
        s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
        signature_pad = (SignaturePad)findViewById(R.id.signature_pad);
        Button confirmDeliveryBtn = (Button) findViewById(R.id.confirmDeliveryBtn);
        Button skipSignatureButton = (Button) findViewById(R.id.skipSignature);
        verifyImage = (ImageView)findViewById(R.id.packageImage);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);


        currentpackageid = getIntent().getStringExtra("currentpackageid");
        ArrayList<String> packageids = getIntent().getStringArrayListExtra("packageids");




        try {
            contactNumber = getIntent().getStringExtra("contactNumber");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (int i = 0; i< packageids.size(); i++){
            if(i==0)
            {
                currentpackageid = packageids.get(i);
            }
            else{
                currentpackageid = currentpackageid +","+ packageids.get(i);
            }
        }

        ImageView callRuutdrop = (ImageView) findViewById(R.id.callRuutdrop);
        callRuutdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(VerifyDeliveryActivity.this);
                builder.setTitle("Support")
                        .setMessage("Are you sure you want to call Ruutdrop?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                Intent intent = new Intent(Intent.ACTION_CALL);

                                intent.setData(Uri.parse("tel:"+contactNumber));
                                if (ActivityCompat.checkSelfPermission(VerifyDeliveryActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                    ActivityCompat.requestPermissions(VerifyDeliveryActivity.this,new String[]{Manifest.permission.CALL_PHONE},59);
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
        signature_pad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
            }

            @Override
            public void onClear() {

            }
        });
        confirmDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPackageAndSignatureImage();
            }
        });

        skipSignatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPackageImage();
            }
        });



    }

    private void sendPackageAndSignatureImage() {

        progressDialog.show();
        Bitmap bitmap = ((BitmapDrawable) verifyImage.getDrawable()).getBitmap();
        File packagefile = persistImage(bitmap,"packageimage");
        uploadImage(packagefile,"package_image");


        Bitmap signature = signature_pad.getSignatureBitmap();
        File signfile = persistImage(signature,"signimage");
        uploadImage(signfile,"signature_image");

        }


    private void sendPackageImage() {
        progressDialog.show();

        Bitmap bitmap = ((BitmapDrawable) verifyImage.getDrawable()).getBitmap();

        File packagefile = persistImage(bitmap,"packageimage");
        uploadImage(packagefile,"package_image");
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        verifyImage.setImageBitmap(bitmap);

                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }
                }

        }
    }

    private void uploadImage(final File file, final String key) {


        final String[] fileurl = new String[1];

        new AsyncTask<Void,Void,Void>()
        {

             String response;

            @Override
            protected Void doInBackground(Void... voids) {



                String filename  = String.valueOf(UUID.randomUUID()).concat(".jpg");
                fileurl[0] = "https://s3.amazonaws.com/ruutdrop/"+filename;
                PutObjectRequest por = new PutObjectRequest(bucket_name, filename, file);
                por.setGeneralProgressListener(new ProgressListener() {
                    @Override
                    public void progressChanged(ProgressEvent progressEvent) {
                        if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE)
                        {
                            Log.e("upload","complete");
                            HashMap<String, String> postParams = new HashMap<>();
                            postParams.put("ids",currentpackageid);
                            postParams.put("value",fileurl[0]);
                            postParams.put("key",key);
                            String url = "http://34.207.184.123/API/UpdatePackageImage.php";

                            response = PostIntrepreter.performPostCall(url, postParams);
                            Log.e("rspns",response);
                        }
                        else if (progressEvent.getEventCode() == ProgressEvent.PART_COMPLETED_EVENT_CODE)
                        {
                            Log.e("upload","part complete");
                        }
                        else if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE)
                        {
                            Log.e("upload","failed");
                        }
                        else if (progressEvent.getEventCode() == ProgressEvent.CANCELED_EVENT_CODE)
                        {
                            Log.e("upload","cancelled");
                        }
                        else if (progressEvent.getEventCode() == ProgressEvent.STARTED_EVENT_CODE)
                        {
                            Log.e("upload","started");
                        }
                    }
                });
                s3Client.putObject(por);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                VerifyDeliveryActivity.this.finish();
            }
        }.execute();



    }
    private static File persistImage(Bitmap bitmap, String name) {
        File filesDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File imageFile = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("exception", "Error writing bitmap", e);
        }
        return imageFile;
    }

}
