package com.druglane;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import network.VolleySingleton;

public class SellerSettings extends AppCompatActivity {

    private RadioButton only_retail, only_wholesale, all;
    private Button save_btn;
    private ProgressBar mProgressView;
    String current_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_settings);

        final SharedPreferences sharedPref = SellerSettings.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        current_type = sharedPref.getString(getString(R.string.price_filter), "all");

        only_retail = findViewById(R.id.only_retail);
        only_wholesale = findViewById(R.id.only_wholesale);
        all = findViewById(R.id.all);

        switch (current_type){
            case "retail":
                only_retail.setChecked(true);
                break;
            case "wholesale":
                only_wholesale.setChecked(true);
                break;
            default:
                all.setChecked(true);
        }

        mProgressView = findViewById(R.id.settings_loading);
        save_btn = findViewById(R.id.update_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });
    }

    public void update(){
        final String type;
        //        the type
        RadioGroup arr_group = findViewById(R.id.seller_settings_price_filter);
        int selectedId = arr_group.getCheckedRadioButtonId();
//                    use the id to do the location thingy
        switch (selectedId){
            case R.id.only_retail:
                type = "retail";
                break;
            case R.id.only_wholesale:
                type = "wholesale";
                break;
                default:
                    type = "all";

        }

        mProgressView.setVisibility(View.VISIBLE);
        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = Uri.parse(base_url+"api_updateInfo").toString();


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
                                        Toast.makeText(SellerSettings.this, "Details updated successfully", Toast.LENGTH_LONG).show();
                                        SharedPreferences sharedPref = SellerSettings.this.getSharedPreferences(
                                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString(getString(R.string.price_filter), type);

                                        editor.apply();
                                        Intent i = new Intent(SellerSettings.this, Main2Activity.class);
                                        startActivity(i);
                                        break;
                                    default:
                                        Toast.makeText(SellerSettings.this, "Unable to update. Please check your connection", Toast.LENGTH_LONG).show();

                                }
                                mProgressView.setVisibility(View.GONE);
//                                Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("user_data").getString("id"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                mProgressView.setVisibility(View.GONE);
                                e.printStackTrace();
                                Toast.makeText(SellerSettings.this, "Unable to update. Please check your connection", Toast.LENGTH_LONG).show();

                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            mProgressView.setVisibility(View.GONE);
                            Toast.makeText(SellerSettings.this, "Unable to update. Please check your connection", Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("name", Main2Activity.user_name);
                    params.put("country", Main2Activity.country);
                    params.put("email", Main2Activity.email);
                    params.put("phone", Main2Activity.phone);
                    params.put("location", Main2Activity.location);
                    params.put("_id", Main2Activity._id);
                    params.put("type", Main2Activity.user_type);
                    params.put("selling_price_filter", type);
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
