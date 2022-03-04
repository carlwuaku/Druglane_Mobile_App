package com.druglane;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
//
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.Map;

import network.VolleySingleton;

/**
 * Created by Carl on 11/25/2018.
 */

public class FirebaseIdService
        extends
        FirebaseInstanceIdService
{
    public static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Registration Token: = " + token);

        SharedPreferences sharedPref = FirebaseIdService.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.device_id_key), token);
        editor.apply();


//        sendRegistrationToServer();
    }


}
