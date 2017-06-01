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

import com.ruutdrop.ruutdropapp.R;
import com.ruutdrop.ruutdropapp.helpers.PostIntrepreter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * created by Aman on 04/01/17.
 */

public class ChangePasswordActivity extends AppCompatActivity {
    String email;
    EditText password, confirmPassword;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);
        email = getIntent().getStringExtra("email");
        password = (EditText)findViewById(R.id.password);
        confirmPassword = (EditText)findViewById(R.id.confirmpassword);
        Button changePassword = (Button)findViewById(R.id.changePassword);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(password.getText().toString().length() >0 && confirmPassword.getText().toString().length() >0 ) {
                    if (password.getText().toString().equalsIgnoreCase(confirmPassword.getText().toString())) {
                        new ChangePassword(password.getText().toString()).execute();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Passwords do not match!", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(ChangePasswordActivity.this, "Don't leave any field blank!!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public class ChangePassword extends AsyncTask<Void, Void, Void> {
        private String response;
        private String password;
        ProgressDialog asyncDialog = new ProgressDialog(ChangePasswordActivity.this);


        ChangePassword(String password) {
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
            postParams.put("password",password);
            postParams.put("action", "reset");

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
                    Toast.makeText(ChangePasswordActivity.this,"Password reset successfully!! Please Login to continue",Toast.LENGTH_SHORT).show();
                    Intent loginActivity = new Intent(ChangePasswordActivity.this,SigninActivity.class);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(loginActivity);

                }
                else{
                    Toast.makeText(ChangePasswordActivity.this,"Something went wrong! Please Try again",Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(ChangePasswordActivity.this,"Something went wrong! Please Try again",Toast.LENGTH_SHORT).show();
            }

        }

    }
}
