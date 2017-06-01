package com.ruutdrop.ruutdropapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.ruutdrop.ruutdropapp.R;
import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//import com.googlecode.tesseract.android.TessBaseAPI;


/**
 * Created by Aman on 09-10-2016.
 */
public class LetsgoActivity extends AppCompatActivity {
    private static final String TAG = LetsgoActivity.class.getSimpleName();
    static final int PHOTO_REQUEST_CODE = 1;
//    private TessBaseAPI tessBaseApi;
    private int count = 0;
    TextView textView;
    Uri outputFileUri;
    private static final String lang = "eng";
    String result = "empty";
    private Uri resultUri;
    private ImageView capturedimage;
    private RelativeLayout manualaddresslayout, photonotapprovedlayout, photonotapprovedlayout1, imagenotcapturedlayout;
    private LinearLayout scanlayout;
    private static final int REQUEST_WRITE_PERMISSION = 786;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    private static final String TESSDATA = "tessdata";
    private String regularfontpath;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String response;
    private Button retakephotobutton,photonotapprovedTitle;
    private EditText manualname, addressline1, addressline2, city, zipcode;
    private String IMGS_PATH;
    private TextView markbox;
    private TessBaseAPI tessBaseApi;
    private String contactNumber = "9500834273";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
//        regularfontpath = "SanFranscisco/SanFranciscoText-Regular.otf";
//        Typeface regular = Typeface.createFromAsset(getAssets(), regularfontpath);

        capturedimage = (ImageView) findViewById(R.id.capturedimage);
        Button retakephoto = (Button) findViewById(R.id.retakephoto);
        Button retakephoto1 = (Button) findViewById(R.id.retakephoto1);
        Button addaddressmanually = (Button) findViewById(R.id.addaddressmanually);
        Button cancelledallboxesscanned =  (Button)findViewById(R.id.cancelledallboxesscanned);
        manualaddresslayout = (RelativeLayout) findViewById(R.id.manualaddresslayout);
        scanlayout = (LinearLayout) findViewById(R.id.scanlayout);
        photonotapprovedlayout = (RelativeLayout) findViewById(R.id.photonotapprovedlayout);
        photonotapprovedlayout1 = (RelativeLayout) findViewById(R.id.photonotapprovedlayout1);
        imagenotcapturedlayout = (RelativeLayout) findViewById(R.id.imagenotcapturedlayout);
        retakephotobutton = (Button) findViewById(R.id.retakephotobutton);
        Button nextBox = (Button) findViewById(R.id.nextBox);
        Button allboxesscanned = (Button) findViewById(R.id.allboxesscanned);
        markbox = (TextView) findViewById(R.id.markbox);

        photonotapprovedTitle = (Button) findViewById(R.id.photonotapproved1);
        try {
            contactNumber = getIntent().getStringExtra("contactNumber");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        manualname = (EditText) findViewById(R.id.manualname);
        addressline1 = (EditText) findViewById(R.id.manualaddressline1);
        addressline2 = (EditText) findViewById(R.id.manualaddressline2);
        city = (EditText) findViewById(R.id.manualcity);
        zipcode = (EditText) findViewById(R.id.manualzipcode);

        ImageView callRuutdrop = (ImageView) findViewById(R.id.callRuutdrop);
        callRuutdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(LetsgoActivity.this);
                builder.setTitle("Support")
                        .setMessage("Are you sure you want to call Ruutdrop?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                Intent intent = new Intent(Intent.ACTION_CALL);

                                intent.setData(Uri.parse("tel:"+contactNumber));
                                if (ActivityCompat.checkSelfPermission(LetsgoActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    ActivityCompat.requestPermissions(LetsgoActivity.this,new String[]{Manifest.permission.CALL_PHONE},59);
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
        Button manualsubmitaddress = (Button) findViewById(R.id.manualsubmitaddress);
        manualsubmitaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressline1.getText().toString().equalsIgnoreCase("")  || city.getText().toString().equalsIgnoreCase("") || zipcode.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(LetsgoActivity.this, "* marked Fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    result = addressline1.getText().toString() + " " + addressline2.getText().toString() + " " + city.getText().toString() + " " + zipcode.getText().toString();
                    new SendTypedAddress().execute();
                }
            }
        });

        nextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity();
            }
        });

        cancelledallboxesscanned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(LetsgoActivity.this);
                builder.setTitle("Please Confirm!")
                        .setMessage("Are you sure you have scanned all the boxes?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                new ConfirmBoxesScanning().execute();
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
        allboxesscanned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                LetsgoActivity.this.finish();
                final AlertDialog.Builder builder = new AlertDialog.Builder(LetsgoActivity.this);
                builder.setTitle("Please Confirm!")
                        .setMessage("Are you sure you have scanned all the boxes?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                new ConfirmBoxesScanning().execute();
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

        startCameraActivity();

//        retakephoto.setTypeface(regular);
//        addaddressmanually.setTypeface(regular);

        addaddressmanually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photonotapprovedTitle.setText("INVALID ADDRESS");
                photonotapprovedlayout1.setVisibility(View.GONE);
                photonotapprovedlayout.setVisibility(View.GONE);
                manualaddresslayout.setVisibility(View.VISIBLE);
            }
        });

        Button addaddressmanually1 = (Button) findViewById(R.id.addaddressmanually1);
        addaddressmanually1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photonotapprovedTitle.setText("INVALID ADDRESS");
                photonotapprovedlayout1.setVisibility(View.GONE);
                photonotapprovedlayout.setVisibility(View.GONE);
                imagenotcapturedlayout.setVisibility(View.GONE);
                scanlayout.setVisibility(View.GONE);
                manualaddresslayout.setVisibility(View.VISIBLE);

            }
        });
        retakephoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photonotapprovedTitle.setText("PHOTO NOT APPROVED");
                photonotapprovedlayout1.setVisibility(View.GONE);
                photonotapprovedlayout.setVisibility(View.GONE);

                startCameraActivity();
            }
        });
        retakephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photonotapprovedTitle.setText("PHOTO NOT APPROVED");
                photonotapprovedlayout.setVisibility(View.GONE);
//                retakephoto.setVisibility(View.GONE);
                photonotapprovedlayout1.setVisibility(View.GONE);

                startCameraActivity();
//                onBackPressed();
            }
        });

        retakephotobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity();
            }
        });
    }

    /**
     * to get high resolution image from camera
     */
    private void startCameraActivity() {

        checkPermissions();
        try {
            try {

                IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/imgs";
                prepareDirectory(IMGS_PATH);

                String img_path = IMGS_PATH + "/ocr.jpg";

                outputFileUri = Uri.fromFile(new File(img_path));
                imagenotcapturedlayout.setVisibility(View.GONE);
                photonotapprovedlayout1.setVisibility(View.GONE);
                photonotapprovedlayout.setVisibility(View.GONE);
                scanlayout.setVisibility(View.GONE);
                manualaddresslayout.setVisibility(View.GONE);
                final Intent takePictureIntent = new Intent(LetsgoActivity.this, CameraActivity.class);
                takePictureIntent.putExtra("path", img_path);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        //making photo
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            prepareTesseract();
            InputStream image_stream = null;
            try {
                image_stream = getContentResolver().openInputStream(outputFileUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
//            Bitmap bitmap = BitmapFactory.decodeFile(outputFileUri.toString(), options);
            Bitmap bitmap = BitmapFactory.decodeStream(image_stream,null,options);
//            int w = bitmap.getWidth();
//            int h = bitmap.getHeight();
            ExifInterface ei = null;
            try {
                ei = new ExifInterface(outputFileUri.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = 0;
            if (ei != null) {
                orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
            }

            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    capturedimage.setImageBitmap(bitmap);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    capturedimage.setImageBitmap(bitmap);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    capturedimage.setImageBitmap(bitmap);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    capturedimage.setImageBitmap(bitmap);
                default:
                    capturedimage.setImageBitmap(bitmap);
                    break;
            }


            new Scan(bitmap).execute();
        } else {
            imagenotcapturedlayout.setVisibility(View.VISIBLE);
            photonotapprovedlayout1.setVisibility(View.GONE);
            photonotapprovedlayout.setVisibility(View.GONE);
            scanlayout.setVisibility(View.GONE);
            manualaddresslayout.setVisibility(View.GONE);
        }
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }
    /**
     * Prepare directory on external storage
     *
     * @param path
     * @throws Exception
     */
    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }


    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    /**
     * Copy tessdata files (located on assets/tessdata) to destination directory
     *
     * @param path - name of directory with .traineddata files
     */
    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }


    /**
     * don't run this code in main thread - it stops UI thread. Create AsyncTask instead.
     * http://developer.android.com/intl/ru/reference/android/os/AsyncTask.html
     *
     * @param bitmap
     */
    private void startOCR(Bitmap bitmap) {
        try {


            result = extractText(bitmap);
            Log.d("String", result);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(DATA_PATH, lang);

//       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,.");

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);

        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;

//        return "";
    }

    public class SendManualAddress extends AsyncTask<Void,Void,Void>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LetsgoActivity.this);
            dialog.setMessage("Please Wait....");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("address", result);
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            Log.d("text",result);
            String url = "http://34.207.184.123/API/match_case.php";
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
                addressline1.setText("");
                addressline2.setText("");
                city.setText("");
                zipcode.setText("");
                if (error.equalsIgnoreCase("false")) {
                    String textbox = jObject.optString("tag");
                    photonotapprovedlayout.setVisibility(View.GONE);
                    photonotapprovedlayout1.setVisibility(View.GONE);
                    scanlayout.setVisibility(View.VISIBLE);
                    manualaddresslayout.setVisibility(View.GONE);
                    imagenotcapturedlayout.setVisibility(View.GONE);
                    markbox.setText(textbox);

                }
                else {
                    if(jObject.optString("message").contains("already"))
                    {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(LetsgoActivity.this);
                        builder.setMessage("This address has been scanned already. Do you want to add another package to the same address?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        new InsertPackage().execute();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                        photonotapprovedlayout1.setVisibility(View.GONE);
                                        photonotapprovedlayout.setVisibility(View.GONE);
                                        startCameraActivity();
                                    }
                                });
                        final AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else {
                        count++;
                        if (count <= 2) {
                            scanlayout.setVisibility(View.GONE);
                            photonotapprovedlayout.setVisibility(View.VISIBLE);
                            photonotapprovedlayout1.setVisibility(View.GONE);
                            manualaddresslayout.setVisibility(View.GONE);
                            imagenotcapturedlayout.setVisibility(View.GONE);
                        } else {
                            scanlayout.setVisibility(View.GONE);
                            manualaddresslayout.setVisibility(View.GONE);
                            photonotapprovedlayout.setVisibility(View.GONE);
                            photonotapprovedlayout1.setVisibility(View.VISIBLE);
                            imagenotcapturedlayout.setVisibility(View.GONE);
                            count = 0;
                        }
                    }

                }
            } catch (JSONException e) {
                photonotapprovedlayout.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

        }

    }

    public class ConfirmBoxesScanning extends AsyncTask<Void,Void,Void>
    {
        private String response;

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LetsgoActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage("Please Wait....");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            String url = "http://34.207.184.123/API/ConfirmBoxesScanning.php";

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
                    new UpdateWarehouseTime("finish_scanning_time").execute();
                    Intent boxes = new Intent(LetsgoActivity.this,BoxesComputingActivity.class);
                    boxes.putExtra("contactNumber",contactNumber);
                    startActivity(boxes);
                }
                else{

                    final AlertDialog.Builder builder = new AlertDialog.Builder(LetsgoActivity.this);
                    builder.setTitle("Failure!")
                            .setMessage("You haven't scanned any boxes yet! Please start scanning.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    startCameraActivity();
                                }
                            });

                    final AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public class SendTypedAddress extends AsyncTask<Void,Void,Void>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LetsgoActivity.this);
            dialog.setMessage("Please Wait....");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("address", result);
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            Log.d("text",result);
            String url = "http://34.207.184.123/API/match_case.php";
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
                addressline1.setText("");
                addressline2.setText("");
                city.setText("");
                zipcode.setText("");
                if (error.equalsIgnoreCase("false")) {
                    String textbox = jObject.optString("tag");
                    photonotapprovedlayout.setVisibility(View.GONE);
                    photonotapprovedlayout1.setVisibility(View.GONE);
                    scanlayout.setVisibility(View.VISIBLE);
                    manualaddresslayout.setVisibility(View.GONE);
                    imagenotcapturedlayout.setVisibility(View.GONE);
                    markbox.setText(textbox);

                }
                else {
                    if(jObject.optString("message").contains("already"))
                    {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(LetsgoActivity.this);
                        builder.setMessage("This address has been scanned already,Do you want to add another package to the same address?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        new InsertPackage().execute();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                        photonotapprovedlayout1.setVisibility(View.GONE);
                                        photonotapprovedlayout.setVisibility(View.GONE);
                                        startCameraActivity();
                                    }
                                });
                        final AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else {
                            scanlayout.setVisibility(View.GONE);
                            manualaddresslayout.setVisibility(View.GONE);
                            photonotapprovedlayout.setVisibility(View.GONE);
                            photonotapprovedlayout1.setVisibility(View.VISIBLE);
                            imagenotcapturedlayout.setVisibility(View.GONE);
                    }

                }
            } catch (JSONException e) {
                photonotapprovedlayout.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

        }

    }
    public class Scan extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private Bitmap bitmap;
        private Scan(Bitmap bitmap)
        {
            this.bitmap = bitmap;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LetsgoActivity.this);
            dialog.setMessage("Please Wait....");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            startOCR(bitmap);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            try {
                new SendManualAddress().execute();
            } catch (Exception e) {
                photonotapprovedlayout.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

        }
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public class InsertPackage extends AsyncTask<Void,Void,Void>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LetsgoActivity.this);
            dialog.setMessage("Please Wait....");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("address", result);
            postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
            Log.d("text",result);
            String url = "http://34.207.184.123/API/InsertPackage.php";
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
                    String textbox = jObject.optString("tag");
                    photonotapprovedlayout.setVisibility(View.GONE);
                    photonotapprovedlayout1.setVisibility(View.GONE);
                    scanlayout.setVisibility(View.VISIBLE);
                    manualaddresslayout.setVisibility(View.GONE);
                    imagenotcapturedlayout.setVisibility(View.GONE);
                    markbox.setText(textbox);

                }
                else {
                    if(jObject.optString("message").equalsIgnoreCase("already_exists"))
                    {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(LetsgoActivity.this);
                        builder.setTitle("This address has been scanned already!")
                                .setMessage("Do you want to add another package to the same address?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        new InsertPackage().execute();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        photonotapprovedlayout1.setVisibility(View.GONE);
                                        photonotapprovedlayout.setVisibility(View.GONE);
                                        startCameraActivity();
                                    }
                                });
                        final AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else {
                        count++;
                        if (count <= 2) {
                            scanlayout.setVisibility(View.GONE);
                            photonotapprovedlayout.setVisibility(View.VISIBLE);
                            photonotapprovedlayout1.setVisibility(View.GONE);
                            manualaddresslayout.setVisibility(View.GONE);
                            imagenotcapturedlayout.setVisibility(View.GONE);
                        } else {
                            scanlayout.setVisibility(View.GONE);
                            manualaddresslayout.setVisibility(View.GONE);
                            photonotapprovedlayout.setVisibility(View.GONE);
                            photonotapprovedlayout1.setVisibility(View.VISIBLE);
                            imagenotcapturedlayout.setVisibility(View.GONE);
                            count = 0;
                        }
                    }

                }
            } catch (JSONException e) {
                photonotapprovedlayout.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onBackPressed() {

    }
}