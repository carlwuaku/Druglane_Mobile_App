package com.druglane;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class AccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static String name;
    public static String phone;
    public static String email;
    public static String location;
    public static String country;
    public String[] countries;
    private static String _id;

    private EditText nameView;
    private EditText phoneview;
    private EditText emailView;
//    private EditText countryview;
    private EditText locationView;
    private Spinner countries_list;
    private Spinner regions_list;
    private RadioButton buyer_btn;
    private RadioButton seller_btn;
//    private String license_number;
    private String physical_location;
    private EditText physical_locationView;
//    private EditText license_numberView;
    private TextView phy_loc_label;
    private Button save_btn;
    private  String type;
    RadioGroup types;

    private ProgressBar mProgressView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        final SharedPreferences sharedPref = AccountActivity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        name = sharedPref.getString(getString(R.string.name_key), null);
        phone = sharedPref.getString(getString(R.string.phone_key), "N/A");
        email = sharedPref.getString(getString(R.string.email_key), "N/A");
        location = sharedPref.getString(getString(R.string.location_key), "N/A");
        country = sharedPref.getString(getString(R.string.country_key), "N/A");
        _id = sharedPref.getString(getString(R.string.user_id_key), null);
        type = sharedPref.getString(getString(R.string.type_key), "buyer");
        physical_location = sharedPref.getString(getString(R.string.physical_location), "N/A");
//        license_number = sharedPref.getString(getString(R.string.license_number), "N/A");

        nameView = findViewById(R.id.name_field);
        phoneview = findViewById(R.id.phone_field);
        emailView = findViewById(R.id.email_field);
//        countryview = findViewById(R.id.email_field);
        locationView = findViewById(R.id.location_alt_field);
        mProgressView = findViewById(R.id.account_loading);
        phy_loc_label = findViewById(R.id.acc_phy_loc_label);
//        lic_num_label = findViewById(R.id.acc_lic_num_label);
//        license_numberView = findViewById(R.id.acc_license_number);
        physical_locationView = findViewById(R.id.acc_physical_location);



        countries_list = (Spinner) findViewById(R.id.countries_field);
        regions_list = (Spinner) findViewById(R.id.region_select);
        types = findViewById(R.id.user_type);
        seller_btn = findViewById(R.id.seller);
        buyer_btn = findViewById(R.id.buyer);

//        Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        countries_list.setAdapter(adapter);
        countries_list.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> regions_adapter = ArrayAdapter.createFromResource(this,
                R.array.regions_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        regions_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        regions_list.setAdapter(regions_adapter);
        regions_list.setOnItemSelectedListener(this);





        nameView.setText(name);
        phoneview.setText(phone);
        emailView.setText(email);
        locationView.setText(location);
        if (country != null && !Objects.equals("N/A", country)) {
            int spinnerPosition = adapter.getPosition(country);
            if(spinnerPosition != -1){
                countries_list.setSelection(spinnerPosition);
            }
//            make region select disappear based on country
            if(!Objects.equals("Ghana", country)){
                regions_list.setVisibility(View.GONE);
                locationView.setVisibility(View.VISIBLE);
            }
            else{
                regions_list.setVisibility(View.VISIBLE);
                locationView.setVisibility(View.GONE);
            }

        }

        if (location != null && !Objects.equals("N/A", location) && Objects.equals("Ghana", country)) {
            int spinnerPosition = regions_adapter.getPosition(location);
            if(spinnerPosition != -1){
                regions_list.setSelection(spinnerPosition);
                locationView.setVisibility(View.GONE);
            }

        }


        //check if something was sent from the settings page
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String param = extras.getString("change_type");
            type = param;
            Toast.makeText(this, "Scroll to the bottom and tap the 'UPADATE' button to finish", Toast.LENGTH_LONG).show();
        }
        if(type.equals("buyer")){
            buyer_btn.setChecked(true);
            seller_btn.setChecked(false);

//            lic_num_label.setVisibility(View.GONE);
//            license_numberView.setVisibility(View.GONE);

            phy_loc_label.setVisibility(View.GONE);
            physical_locationView.setVisibility(View.GONE);
        }
        if(type.equals("seller")){
            seller_btn.setChecked(true);
            buyer_btn.setChecked(false);
//            lic_num_label.setVisibility(View.VISIBLE);
//            license_numberView.setVisibility(View.VISIBLE);

            phy_loc_label.setVisibility(View.VISIBLE);
            physical_locationView.setVisibility(View.VISIBLE);
        }

        physical_locationView.setText(physical_location);
//        license_numberView.setText(license_number);

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



        Button signInButton = (Button) findViewById(R.id.update_btn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attempt();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

         int id = adapterView.getId();
         String val = adapterView.getSelectedItem().toString();
//        System.out.println("selected item "+val+" "+id);
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

    private void attempt() {


        // Reset errors.
        nameView.setError(null);
        phoneview.setError(null);
        emailView.setError(null);
        locationView.setError(null);
        locationView.setError(null);
//        license_numberView.setError(null);
        physical_locationView.setError(null);


        // Store values at the time of the login attempt.
        name = nameView.getText().toString();
        email = emailView.getText().toString();
        phone = phoneview.getText().toString();
        country = countries_list.getSelectedItem().toString();
//        license_number = license_numberView.getText().toString();
        physical_location = physical_locationView.getText().toString();
//        the type
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

        if(Objects.equals("Ghana", country)){
            location = regions_list.getSelectedItem().toString();
        }
        else{
            location = locationView.getText().toString();
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(location)) {
            Toast.makeText(AccountActivity.this, "The location is required", Toast.LENGTH_SHORT).show();
            cancel = true;
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(AccountActivity.this, "The country is required", Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            focusView = nameView;
            cancel = true;
        }

//        if (TextUtils.isEmpty(email)) {
//            emailView.setError(getString(R.string.error_field_required));
//            focusView = emailView;
//            cancel = true;
//        }

        if (TextUtils.isEmpty(phone)) {
            phoneview.setError(getString(R.string.error_field_required));
            focusView = phoneview;
            cancel = true;
        }

//        if (TextUtils.isEmpty(license_number) && type.equals("seller")) {
//            license_numberView.setError(getString(R.string.error_field_required));
//            focusView = license_numberView;
//            cancel = true;
//        }

//        if (TextUtils.isEmpty(physical_location) && type.equals("seller")) {
//            physical_locationView.setError(getString(R.string.error_field_required));
//            focusView = physical_locationView;
//            cancel = true;
//        }

        if (TextUtils.isEmpty(type)) {
            Toast.makeText(AccountActivity.this, "The type is required", Toast.LENGTH_SHORT).show();
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
            update();
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
        }
    }

    public void update(){

        mProgressView.setVisibility(View.VISIBLE);
        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = Uri.parse(base_url+"client/api_updateInfo").toString();


        // Access the RequestQueue through your singleton class.
        try{
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST,uri,  new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                //get the status
                                String status = jsonObject.getString("status");
//                                System.out.println("response status"+status);
                                switch (status){
                                    case "1":
                                        Toast.makeText(AccountActivity.this, "Details updated successfully", Toast.LENGTH_LONG).show();
                                        SharedPreferences sharedPref = AccountActivity.this.getSharedPreferences(
                                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString(getString(R.string.name_key), name);
                                        editor.putString(getString(R.string.phone_key), phone);
                                        editor.putString(getString(R.string.email_key), email);
                                        editor.putString(getString(R.string.location_key), location);
                                        editor.putString(getString(R.string.country_key), country);
                                        editor.putString(getString(R.string.type_key), type);
//                                        editor.putString(getString(R.string.license_number), license_number);
                                        editor.putString(getString(R.string.physical_location), physical_location);
                                        editor.apply();
                                        Intent i = new Intent(AccountActivity.this, Main2Activity.class);
                                        i.putExtra("reassign_to_room", "yes");
                                        startActivity(i);
                                        break;
                                    default:
                                        Toast.makeText(AccountActivity.this, "Unable to update. Please check your connection", Toast.LENGTH_LONG).show();

                                }
                                mProgressView.setVisibility(View.GONE);
//                                Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("user_data").getString("id"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                mProgressView.setVisibility(View.GONE);
                                e.printStackTrace();
                                Toast.makeText(AccountActivity.this, "Unable to update. Please check your connection", Toast.LENGTH_LONG).show();

                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            mProgressView.setVisibility(View.GONE);
                            Toast.makeText(AccountActivity.this, "Unable to update. Please check your connection", Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("name", name);
                    params.put("country", country);
                    params.put("email", email);
                    params.put("phone", phone);
                    params.put("location", location);
                    params.put("_id", _id);
                    params.put("type", type);
                    params.put("selling_price_filter", Main2Activity.price_filter);

//                    params.put("license_number", license_number);
                    params.put("physical_location", physical_location);
//                    System.out.println(params.toString());
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(this, "An error occurred. Please try again", Toast.LENGTH_LONG).show();
        }
    }
}
