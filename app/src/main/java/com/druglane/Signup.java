package com.druglane;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

public class Signup extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private EditText nameView;
    private EditText phoneview;
    private EditText emailView;
//    private EditText license_numberView;
    private EditText physical_locationView;
    private EditText locationView;

    private String name;
    private String phone;
    private String email;
    private String license_number, physical_location;
    private String password;
    private String confirm_password;
    public static String country;
    private String location;
    private TextView feedback, terms_link;
    private Button signInButton;
    private EditText passwordview;
    private EditText confirmView;
    private Spinner countries_list;
    private Spinner regions_list;

    private ProgressBar mProgressView;
    private RadioButton buyer_btn;
    private RadioButton seller_btn;
    private TextView phy_loc_label;
    RadioGroup types;
    TextView login_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        nameView = findViewById(R.id.name);
        phoneview = findViewById(R.id.phone);
        emailView = findViewById(R.id.email);
        passwordview= findViewById(R.id.password);
        confirmView = findViewById(R.id.password_confirm);
        feedback = findViewById(R.id.signup_feedback);
        mProgressView = findViewById(R.id.signup_loading);
        locationView = findViewById(R.id.location_alt_field);
        phy_loc_label = findViewById(R.id.phy_loc_label);
        login_link = findViewById(R.id.login_link);
//        lic_num_label = findViewById(R.id.lic_num_label);
//        license_numberView = findViewById(R.id.license_number);
        physical_locationView = findViewById(R.id.physical_location);
         signInButton = (Button) findViewById(R.id.signup_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attempt();
            }
        });

        countries_list = (Spinner) findViewById(R.id.countries_field);
        regions_list = (Spinner) findViewById(R.id.region_select);

//        Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        countries_list.setAdapter(adapter);
        countries_list.setOnItemSelectedListener(this);
        countries_list.setSelection(adapter.getPosition("Ghana"));

        ArrayAdapter<CharSequence> regions_adapter = ArrayAdapter.createFromResource(this,
                R.array.regions_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        regions_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        regions_list.setAdapter(regions_adapter);
        regions_list.setOnItemSelectedListener(this);

        types = findViewById(R.id.user_type);
        seller_btn = findViewById(R.id.seller);
        buyer_btn = findViewById(R.id.buyer);
        terms_link = findViewById(R.id.terms_link);
        terms_link.setMovementMethod(LinkMovementMethod.getInstance());

        seller_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                lic_num_label.setVisibility(View.VISIBLE);
//                license_numberView.setVisibility(View.VISIBLE);

                phy_loc_label.setVisibility(View.VISIBLE);
                physical_locationView.setVisibility(View.VISIBLE);
            }
        });

        buyer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                lic_num_label.setVisibility(View.GONE);
//                license_numberView.setVisibility(View.GONE);

                phy_loc_label.setVisibility(View.GONE);
                physical_locationView.setVisibility(View.GONE);
            }
        });

        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Signup.this, LoginActivity.class);
                startActivity(i);
            }
        });


    }


    private void attempt() {


        // Reset errors.
        nameView.setError(null);
        phoneview.setError(null);
        emailView.setError(null);
//        license_numberView.setError(null);
        physical_locationView.setError(null);


        // Store values at the time of the login attempt.
        name = nameView.getText().toString();
        password = passwordview.getText().toString();
        email = emailView.getText().toString();
//        license_number = license_numberView.getText().toString();
        physical_location = physical_locationView.getText().toString();
        phone = phoneview.getText().toString();
        //        the type
        String type = "buyer";
        RadioGroup arr_group = findViewById(R.id.user_type);
        int selectedId = arr_group.getCheckedRadioButtonId();
//                    use the id to do the location thingy
        switch (selectedId){
            case R.id.buyer:
                type = "buyer";
                break;
            case R.id.seller:
                type = "seller";

        }

        confirm_password = confirmView.getText().toString();
        country = countries_list.getSelectedItem().toString();
        if(Objects.equals("Ghana", country)){
            location = regions_list.getSelectedItem().toString();
        }
        else{
            location = locationView.getText().toString();
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordview.setError(getString(R.string.error_field_required));
            focusView = passwordview;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            focusView = nameView;
            cancel = true;
        }

//        if (TextUtils.isEmpty(license_number) && type.equals("seller")) {
//            license_numberView.setError(getString(R.string.error_field_required));
//            focusView = license_numberView;
//            cancel = true;
//        }
//
//        if (TextUtils.isEmpty(physical_location) && type.equals("seller")) {
//            physical_locationView.setError(getString(R.string.error_field_required));
//            focusView = physical_locationView;
//            cancel = true;
//        }

        if (TextUtils.isEmpty(phone)) {
            phoneview.setError(getString(R.string.error_field_required));
            focusView = phoneview;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirm_password)) {
            confirmView.setError(getString(R.string.error_field_required));
            focusView = confirmView;
            cancel = true;
        }
//
        if (!Objects.equals(password, confirm_password)) {
            confirmView.setError(getString(R.string.error_mismatch));
            focusView = confirmView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //start the network activity
            signup();
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
        }
    }

    public void signup(){
        feedback.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.GONE);
        //        the type
        String type = "buyer";
        RadioGroup arr_group = findViewById(R.id.user_type);
        int selectedId = arr_group.getCheckedRadioButtonId();
//                    use the id to do the location thingy
        switch (selectedId){
            case R.id.buyer:
                type = "buyer";
                break;
            case R.id.seller:
                type = "seller";

        }

        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = Uri.parse(base_url+"client/api_register").toString();


        // Access the RequestQueue through your singleton class.
        try{
            final String finalType = type;
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST,uri,  new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                //get the status
                                String status = jsonObject.getString("status");
                                String message = jsonObject.getString("message");
                                System.out.println("response status"+status);
                                switch (status){
                                    case "1":
                                        Toast.makeText(Signup.this, "Registration was successful", Toast.LENGTH_LONG).show();
                                        SharedPreferences sharedPref = Signup.this.getSharedPreferences(
                                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        JSONObject user_data = jsonObject.getJSONObject("user_data");
                                        String id = user_data.getString("_id");
                                        String name = user_data.getString("name");
                                        String type = user_data.getString("type");
                                        String phone = user_data.getString("phone");
                                        String email = user_data.getString("email");
                                        String location = user_data.getString("location");
                                        String country  = user_data.getString("country");
                                        String active = user_data.getString("status");
//                                        String lic_num = user_data.getString("license_number");
                                        String phyloc = user_data.getString("physical_location");

                                        editor.putString(getString(R.string.user_id_key), id);
                                        editor.putString(getString(R.string.name_key), name);
                                        editor.putString(getString(R.string.type_key), type);
                                        editor.putString(getString(R.string.phone_key), phone);
                                        editor.putString(getString(R.string.email_key), email);
                                        editor.putString(getString(R.string.location_key), location);
                                        editor.putString(getString(R.string.country_key), country);
                                        editor.putString(getString(R.string.activated_key), active);
                                        editor.putString(getString(R.string.physical_location), phyloc);
//                                        editor.putString(getString(R.string.license_number), lic_num);

                                        editor.apply();
                                        //check if user was activated

                                        if (!active.equals("Active")) {
                                            Intent i = new Intent(Signup.this, Activate.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);
                                        }
                                        else{
                                            Intent i = new Intent(Signup.this, Main2Activity.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);
                                        }



                                        break;
                                    default:
                                        Toast.makeText(Signup.this, message, Toast.LENGTH_LONG).show();

                                }
                                mProgressView.setVisibility(View.GONE);
                                signInButton.setVisibility(View.VISIBLE);
//                                Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("user_data").getString("id"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                mProgressView.setVisibility(View.GONE);
                                signInButton.setVisibility(View.VISIBLE);
                                e.printStackTrace();
                                Toast.makeText(Signup.this, "Unable to register. Please check your connection", Toast.LENGTH_LONG).show();

                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            mProgressView.setVisibility(View.GONE);
                            signInButton.setVisibility(View.VISIBLE);
                            error.printStackTrace();
                            Toast.makeText(Signup.this, "Unable to register. Please check your connection", Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("name", name);
                    params.put("password", password);
                    params.put("email", email);
                    params.put("phone", phone);
                    params.put("location", location);
                    params.put("country", country);
                    params.put("type", finalType);
//                    params.put("license_number", license_number);
                    params.put("physical_location", physical_location);
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*
     params.put("name", name);
                    params.put("password", password);
                    params.put("email", email);
                    params.put("phone", phone);
                    params.put("location", location);
                    params.put("country", country);
     */

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        int id = adapterView.getId();
        String val = adapterView.getSelectedItem().toString();
        System.out.println("selected item "+val+" "+id);
        switch (id){


            case R.id.countries_field:
                if(Objects.equals("Ghana", val)){
                    regions_list.setVisibility(View.VISIBLE);
                    locationView.setVisibility(View.GONE);
                }
                else{
                    regions_list.setVisibility(View.GONE);
                    locationView.setVisibility(View.VISIBLE);
                }
                break;
            default:


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
