package com.druglane;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import database.Repository;
import network.VolleySingleton;


public class SettingsActivity extends AppCompatActivity {

    private LinearLayout details, subscription, verification, reply_history, seller_settings, logout, account_type;
    private Repository repository;
    private TextView account_type_text, subscription_text, verification_text, verification_drawable, seller_header;
    private String verified;
    private View.OnClickListener retryVerification, learnVerification;
    public static String location;
    public static String country, subscription_end;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        details = findViewById(R.id.settings_account);
//        subscription = findViewById(R.id.settings_subscription);
        verification = findViewById(R.id.settings_verification);
        logout = findViewById(R.id.settings_logout);
        seller_settings= findViewById(R.id.settings_seller_settings);
        reply_history = findViewById(R.id.settings_reply_history);
        repository = new Repository(this.getApplication());
        account_type_text = findViewById(R.id.settings_account_type_text);
        account_type = findViewById(R.id.settings_account_type);
//        subscription_text = findViewById(R.id.subscription_text);
        verification_text = findViewById(R.id.verification_text);
        verification_drawable = findViewById(R.id.verification_drawable);
        seller_header = findViewById(R.id.seller_header);
        verification_text.setText("Checking verification status....");

        checkVerification();


        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, AccountActivity.class);
                startActivity(i);
            }
        });

        SharedPreferences sharedPref = SettingsActivity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String subscription_end = sharedPref.getString(getString(R.string.subscription_key), "");
        final String user_type  = sharedPref.getString(getString(R.string.type_key), "buyer");
        location = sharedPref.getString(getString(R.string.location_key), "N/A");
        country = sharedPref.getString(getString(R.string.country_key), "N/A");

        account_type_text.setText("You are currently a "+user_type+". Tap to change");
//        if(Main2Activity.isPast(subscription_end)){
//            //disable all the seller stuff
//            subscription_text.setText("Your subscription as a seller has expired. Tap here to fix this");
//            subscription_text.setTextColor(Color.MAGENTA);
//            subscription_text.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    openWebURL("https://druglanechat.firebaseapp.com/subscribe/" + Main2Activity._id);
//
//                }
//            });
//        }
//        else{
//            if(user_type.equals("seller")){
//                subscription_text.setText("Your subscription is active. It expires on "+subscription_end
//                    .substring(0, 10)
//                );
//            }
//
//        }
        if(user_type.equals("buyer")){
            seller_settings.setVisibility(View.GONE);
            reply_history.setVisibility(View.GONE);
            verification.setVisibility(View.GONE);
//            subscription.setVisibility(View.GONE);
            seller_header.setVisibility(View.VISIBLE);
        }

         retryVerification = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkVerification();
            }
        };

       learnVerification = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingsActivity.this);
                builder1.setMessage("Verification helps buyers to identify authentic sellers. When you are verified, a badge is " +
                        "displayed to buyers every time you reply a search. We normally do verification once you register as " +
                        "a seller. For more information, go to our website https://thedruglane.com/verification")
                        .setCancelable(false)

                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;

                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();
            }
        };

//        subscription.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(SettingsActivity.this, AccountActivity.class);
//                startActivity(i);
//            }
//        });

        verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingsActivity.this);
                builder1.setMessage("Verification helps buyers to identify authentic sellers. When you are verified, a badge is " +
                        "displayed to buyers every time you reply a search. We normally do verification once you register as " +
                        "a seller. For more information, go to our website https://thedruglane.com/verification")
                        .setCancelable(false)

                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;

                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();

            }
        });

        account_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String new_account = user_type.equals("buyer") ? "seller" : "buyer";
                AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingsActivity.this);
                builder1.setMessage("You are currently a "+user_type+". Would you like to change it to a "+new_account
                +" account?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(SettingsActivity.this, AccountActivity.class);
                                i.putExtra("change_type", new_account);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;

                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();
            }
        });

        reply_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(SettingsActivity.this, AccountActivity.class);
//                startActivity(i);
                openWebURL("https://druglanechat.firebaseapp.com/reply_history/" + Main2Activity._id);

            }
        });

        seller_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, SellerSettings.class);
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingsActivity.this);
                builder1.setMessage("Logout? This will delete all the messages and replies you have")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                unsubscribe();
                                repository.deleteAllMessages();
                                repository.deleteAllReplies();
                                //unsubscribe from firebase
                                logout();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;

                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();
            }
        });
    }

    public void unsubscribe(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic("sellers")
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "unSubscribe ok";
                        if (!task.isSuccessful()) {
                            msg = "Message unsubscribe failed";
                        }
                        Log.d("sub result", msg);
//                        Toast.makeText(Main2Activity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        //unsubscribe to your location
        if(!Objects.equals("N/A", country)){
            //        subscribe to your location
            final String country_key = country.replaceAll(" ", "_");
            FirebaseMessaging.getInstance().unsubscribeFromTopic(country_key)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Subscribe ok to "+country_key;
                            if (!task.isSuccessful()) {
                                msg = "Message unsubscribe failed";
                            }
                            Log.d("country sub result", msg);
//                        Toast.makeText(Main2Activity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        if(!Objects.equals("N/A", location)){
            final String location_key = location.replaceAll(" ", "_");
            FirebaseMessaging.getInstance().unsubscribeFromTopic(location_key)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "unSubscribe ok to "+location_key;
                            if (!task.isSuccessful()) {
                                msg = "Message subscribe failed";
                            }
                            Log.d("location sub result", msg);
//                        Toast.makeText(Main2Activity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void logout(){
        //clear preferences and go to login screen
        SharedPreferences sharedPref = SettingsActivity.this.getSharedPreferences(
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

    public void openWebURL( String inURL ) {
        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( inURL ) );

        startActivity( browse );
    }

    public void checkVerification(){
        verification_text.setText("Checking verification status....");

        String _id = Main2Activity._id;
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = Uri.parse(base_url+"client/checkVerification?_id="+_id).toString();
        final Drawable warning = getResources().getDrawable( R.drawable.ic_warning_yellow_24dp );
        final Drawable check = getResources().getDrawable( R.drawable.ic_done_all_green_24dp );
        try{
            StringRequest stringRequest = new StringRequest
                    (Request.Method.GET,uri,  new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                //get the status
                                String status = jsonObject.getString("status");
                                System.out.println(jsonObject);
                                verification_text.setOnClickListener(learnVerification);
                                switch (status){
                                    case "1":
                                        System.out.println("verified ok");
                                        String data = jsonObject.getString("data");
                                        if(data.equals("yes")){
                                            verified = "yes";
                                            verification_text.setText("Your account is verified");
                                            verification_drawable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_green_24dp, 0, 0, 0);
//                                            verification_drawable.setCompoundDrawables(warning, null, null, null);
//                                            verification_drawable.setCompoundDrawables(check, null, null, null);

                                        }
                                        else{
                                            verified = "no";
                                            verification_text.setText("Your account has not been verified. Tap here to learn more");
                                            verification_text.setTextColor(Color.MAGENTA);
                                            verification_drawable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning_yellow_24dp, 0, 0, 0);

                                        }

                                        break;
                                    default:
                                        verified = "no";
                                        verification_text.setText("Your account has not been verified. Tap here to learn more");
                                        verification_text.setTextColor(Color.MAGENTA);
                                        verification_drawable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning_yellow_24dp, 0, 0, 0);

                                }
                            } catch (JSONException e) {

                                e.printStackTrace();
                                verified = "no";
                                verification_text.setText("Your account has not been verified. Tap here to learn more");
                                verification_text.setTextColor(Color.MAGENTA);
                                verification_text.setOnClickListener(learnVerification);
                                verification_drawable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning_yellow_24dp, 0, 0, 0);
                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            verification_text.setText("Unable to connect to server. Tap here to try again");
                            verification_text.setTextColor(Color.RED);
                            verification_text.setOnClickListener(retryVerification);
                            error.printStackTrace();
                            Toast.makeText(SettingsActivity.this, "Error connecting to server. Try again", Toast.LENGTH_LONG).show();
                        }
                    });


            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){
//            mProgressView.setVisibility(View.GONE);
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
