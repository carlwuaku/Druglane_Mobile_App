package com.druglane;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import network.VolleySingleton;

public class Activate extends AppCompatActivity {
    private EditText codeText;
    private ProgressBar mProgressView;
    private TextView feedback, terms_link;
    private String code;
    private Button submit;
    private TextView resend_code, logout;
    String _id;
    public static final String EXTRA_REPLY = "com.druglane.android.activate.REPLY";

    String name;

    @Override
    protected void onResume() {
        super.onResume();

        checkLogin();
    }

    @Override
    protected void onRestart() {
        super.onRestart();


        checkLogin();
    }

    public void checkLogin(){
        final SharedPreferences sharedPref = Activate.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

       _id = sharedPref.getString(getString(R.string.user_id_key), null);
        String activated = sharedPref.getString(getString(R.string.activated_key), "Inactive");


        if(_id != null){
            //if logged in and activated, move to the main page
            if(activated.equals("Active")){
                Intent i = new Intent(this, Main2Activity.class);
                startActivity(i);
            }

        }
        else{
            //else login
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        codeText = findViewById(R.id.activation_code);
        mProgressView = findViewById(R.id.activation_progress);
        feedback = findViewById(R.id.feedback);
        submit = findViewById(R.id.send_activation_button);
        resend_code = findViewById(R.id.resend_code);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });
        logout = findViewById(R.id.logout);

        resend_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendCode();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    public void sendCode(){
        feedback.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
        code = codeText.getText().toString();

        if(code.isEmpty()){
            Toast.makeText(Activate.this, "Please enter a code", Toast.LENGTH_LONG).show();

            return;
        }
        //        the type


        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = Uri.parse(base_url+"client/api_activate_with_code").toString();


        // Access the RequestQueue through your singleton class.
        try{
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST,uri,  new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);
                            SharedPreferences sharedPref = Activate.this.getSharedPreferences(
                                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPref.edit();
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                //get the status
                                String status = jsonObject.getString("status");
                                String message = jsonObject.getString("message");
                                System.out.println("response status"+status);
                                switch (status){
                                    case "1":
                                        editor.putString(getString(R.string.activated_key), "Active");
                                        editor.apply();
                                        Main2Activity.activated = "Active";
                                        feedback.setVisibility(View.GONE);
                                        Toast.makeText(Activate.this, "Activation was successful", Toast.LENGTH_LONG).show();
                                        Intent replyIntent = new Intent(Activate.this, Main2Activity.class);
                                        startActivity(replyIntent);
//                                        setResult(RESULT_OK, replyIntent);
//                                        finish();

                                        break;
                                    default:
                                        feedback.setVisibility(View.VISIBLE);
                                        feedback.setText(message);
                                        Toast.makeText(Activate.this, message, Toast.LENGTH_LONG).show();

                                }
                                mProgressView.setVisibility(View.GONE);
//                                Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("user_data").getString("id"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                mProgressView.setVisibility(View.GONE);
                                feedback.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                                Toast.makeText(Activate.this, "Error", Toast.LENGTH_LONG).show();

                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressView.setVisibility(View.GONE);
                            error.printStackTrace();
                            Toast.makeText(Activate.this, "Unable to send code. Please check your connection", Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("code", code);
//                    params.put("password", password);
//                    params.put("email", Main2Activity.email);
                    params.put("_id", _id);

                    System.out.println(code +" "+_id);
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void resendCode( ){

        final SharedPreferences sharedPref = Activate.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);


        final String phone = sharedPref.getString(getString(R.string.phone_key), "N/A");
        if(phone.equals("N/A")){
            Toast.makeText(this, "Incorrect phone number", Toast.LENGTH_LONG).show();
            return;
        }

        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = base_url+"client/resendActivationCode";
        // Access the RequestQueue through your singleton class.
        mProgressView.setVisibility(View.VISIBLE);

        try{
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST,uri,  new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                mProgressView.setVisibility(View.GONE);

                                //get the status
                                String status = jsonObject.getString("status");
                                Log.d("resendActivationCode", status);
                                switch (status){
                                    case "1":
                                        Toast.makeText(Activate.this,
                                                "Activation code sent via sms to your phone. Please wait for it.",
                                                Toast.LENGTH_LONG).show();
                                        break;
                                    default:
                                        Toast.makeText(Activate.this,
                                                "Error resending your code. Please try again",
                                                Toast.LENGTH_LONG).show();

                                }
                            } catch (JSONException e) {
                                mProgressView.setVisibility(View.GONE);
                                e.printStackTrace();
                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressView.setVisibility(View.GONE);
                            error.printStackTrace();
//                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("phone", phone);
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){

            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public void logout(){
        //clear preferences and go to login screen
        SharedPreferences sharedPref = Activate.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.user_id_key), null);
        editor.putString(getString(R.string.name_key), null);
        editor.putString(getString(R.string.type_key), null);
        editor.putString(getString(R.string.phone_key), null);
        editor.putString(getString(R.string.email_key), null);
        editor.putString(getString(R.string.location_key), null);
        editor.putString(getString(R.string.country_key), null);
//        editor.putString(getString(R.string.verified), null);
        editor.apply();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Exit the app?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        moveTaskToBack(true);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
