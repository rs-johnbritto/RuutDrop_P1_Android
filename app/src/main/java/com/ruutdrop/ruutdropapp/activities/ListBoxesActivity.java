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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruutdrop.ruutdropapp.fragments.BoxesFragment;
import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;
import com.ruutdrop.ruutdropapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * created by Aman on 11/12/16.
 */

public class ListBoxesActivity extends AppCompatActivity {private ViewPager viewPager;
    private BoxesPagerAdapter boxesPagerAdapter;
    private LinearLayout dotsLayout;
    private JSONArray packages;
    private TextView[] dots;
    private TextView title;
    private Button finishedLoading;
    int totalNumberOfPages = 0;
    ProgressDialog dialog;
    String contactNumber = "9500834273";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(ListBoxesActivity.this);
        dialog.setMessage("Please Wait..");

        setContentView(R.layout.activity_list_boxes);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        title = (TextView)findViewById(R.id.title);
        finishedLoading = (Button)findViewById(R.id.finishedLoading);
        new GetBoxesList().execute();

        try {
            contactNumber = getIntent().getStringExtra("contactNumber");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finishedLoading.setVisibility(View.VISIBLE);
        finishedLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(ListBoxesActivity.this);

                 builder.setTitle("Please confirm")
                        .setMessage("Are you sure the boxes are loaded?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                new UpdateWarehouseTime("finish_loading_time").execute();

                                Intent delivery = new Intent(ListBoxesActivity.this,DeliveryActivity.class);
                                getSharedPreferences("Prefs", Context.MODE_PRIVATE).edit().putBoolean("isRouting",true).apply();
                                delivery.putExtra("contactNumber",contactNumber);
                                startActivity(delivery);
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
        ImageView callRuutdrop = (ImageView) findViewById(R.id.callRuutdrop);
        callRuutdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ListBoxesActivity.this);
                builder.setTitle("Support")
                        .setMessage("Are you sure you want to call Ruutdrop?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                Intent intent = new Intent(Intent.ACTION_CALL);

                                intent.setData(Uri.parse("tel:"+contactNumber));
                                if (ActivityCompat.checkSelfPermission(ListBoxesActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    ActivityCompat.requestPermissions(ListBoxesActivity.this,new String[]{Manifest.permission.CALL_PHONE},59);
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
        ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
                int lastPageIndex = totalNumberOfPages;
                lastPageIndex -= 1;
                int startIndex = 0;
                if (position == lastPageIndex) {
                    finishedLoading.setVisibility(View.VISIBLE);
                    if(packages.length()%3 == 0)
                    {
                        startIndex = position*3;
                        //        {
                        startIndex += 2;

                    }
                    else if(packages.length()%3 == 2)
                    {
                        startIndex = position*3;

                        startIndex += 1;

                    }
                    else{
                        startIndex = position*3;

                    }
                } else {
                    startIndex = position*3;
                    //        {

                    startIndex += 2;

//                    finishedLoading.setVisibility(View.INVISIBLE);
                }
                int totalCount = packages.length();
                int currentCount = startIndex + 1;
                title.setText("Boxes List ("+currentCount+"/"+totalCount+")");
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        };

        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[totalNumberOfPages];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }
    private class BoxesPagerAdapter extends FragmentStatePagerAdapter {
        public BoxesPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {

            try {

                JSONArray subContents = new JSONArray();

                int startIndex = 0;
                int lastPageIndex = totalNumberOfPages;
                lastPageIndex -= 1;
                if(index == lastPageIndex)
                {
//                    self.finishedLoading.isHidden = false

                    if(packages.length()%3 == 0)
                    {
                        startIndex = index*3;
                        //        {
                        subContents.put(packages.optJSONObject(startIndex));
                        startIndex += 1;
                        subContents.put(packages.optJSONObject(startIndex));

                        startIndex += 1;
                        subContents.put(packages.optJSONObject(startIndex));
                    }
                    else if(packages.length()%3 == 2)
                    {
                        startIndex = index*3;
                        //        {
                        subContents.put(packages.optJSONObject(startIndex));
                        startIndex += 1;
                        subContents.put(packages.optJSONObject(startIndex));

                    }
                    else{
                        startIndex = index*3;
                        //        {
                        subContents.put(packages.optJSONObject(startIndex));
                    }
                                        }
                else if (index < lastPageIndex){
                    startIndex = index*3;
                    //        {
                    subContents.put(packages.optJSONObject(startIndex));
                    startIndex += 1;
                    subContents.put(packages.optJSONObject(startIndex));

                    startIndex += 1;
                    subContents.put(packages.optJSONObject(startIndex));
                }
                return BoxesFragment.newInstance(subContents,ListBoxesActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }



        @Override
        public int getCount() {
            return totalNumberOfPages;
        }
    }

class GetBoxesList extends AsyncTask<Void,Void,Void>
{

    private String response,tag;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("userid",getSharedPreferences("Prefs", Context.MODE_PRIVATE).getString("userid",""));
        String url = "http://34.207.184.123/API/getSortedBoxes.php";
        response = PostIntrepreter.performPostCall(url, postParams);

        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            dialog.dismiss();
            Log.d("text", response);
            JSONObject jObject = new JSONObject(response);
            String error = jObject.optString("error");
            if (error.equalsIgnoreCase("false")) {
                packages = jObject.optJSONArray("packages");
                if(packages.length()%3 ==0) {
                    totalNumberOfPages = packages.length()/3;
                }
                else{
                    totalNumberOfPages = packages.length()/3 + 1;
                }
                addBottomDots(0);
                BoxesPagerAdapter boxesPagerAdapter = new BoxesPagerAdapter(getSupportFragmentManager());
                viewPager.setAdapter(boxesPagerAdapter);

            }
        } catch (Exception e) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
    @Override
    public void onBackPressed() {

    }
}
