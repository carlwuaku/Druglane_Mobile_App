package com.druglane;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Message;
import network.VolleySingleton;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    public static final String EXTRA_REPLY = "com.druglane.android.login.REPLY";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText usernameView;
    private EditText mPasswordView;
    private ProgressBar mProgressView;
    private View mLoginFormView;
    private TextView feedback;
    Button mEmailSignInButton;
    TextView about_link, terms_link, forgot_password_text;

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
        final SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String _id = sharedPref.getString(getString(R.string.user_id_key), null);
        String activated = sharedPref.getString(getString(R.string.activated_key), "Inactive");
        if(_id != null){
            //if logged in but not actiavted, move to activation page
            if(!activated.equals("Active")){
                Intent i = new Intent(this, Activate.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
            else {
                Intent i = new Intent(this, Main2Activity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String _id = sharedPref.getString(getString(R.string.user_id_key), null);
        String activated = sharedPref.getString(getString(R.string.activated_key), "Inactive");
        if(_id != null){
            if(!activated.equals("Active")){
                Intent i = new Intent(this, Activate.class);
                startActivity(i);
            }
            else{
                Intent i = new Intent(this, Main2Activity.class);
                startActivity(i);
            }
        }


        // Set up the login form.
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        feedback = findViewById(R.id.error_feedback);
        mProgressView.setVisibility(View.GONE);
        usernameView =  findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

         mEmailSignInButton =  findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        forgot_password_text = findViewById(R.id.forgot_password);

        TextView signuplink = (TextView) findViewById(R.id.signup_link);

        signuplink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
        terms_link = findViewById(R.id.terms_link);
        about_link = findViewById(R.id.about_link);

        terms_link.setMovementMethod(LinkMovementMethod.getInstance());
        about_link.setMovementMethod(LinkMovementMethod.getInstance());

        forgot_password_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });
    }



    private void signup(){
        Intent i = new Intent(this,Signup.class);
        startActivity(i);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



    private void attemptLogin() {


        // Reset errors.
        usernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = usernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
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
            login(email, password);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
        }
    }


    public void login(final String username, final String password){
        feedback.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
        mEmailSignInButton.setVisibility(View.GONE);
        //do the network thingy
        final JSONObject json = new JSONObject();
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = Uri.parse(base_url+"client/api_dologin").toString();

        try {
            json.put("email",username);
            json.put("password",password);
            //System.out.print(username);
            //System.out.print(password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Access the RequestQueue through your singleton class.
        try{
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST,uri,  new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject xjsonObject = new JSONObject(response);
                                SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences(
                                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                                SharedPreferences.Editor editor = sharedPref.edit();
                                //get the status
                                String status = xjsonObject.getString("status");
                                switch (status){
                                    case "1":
                                        try{
                                            JSONObject jsonObject = new JSONObject(xjsonObject.getJSONObject("user_data").toString());
                                            System.out.println(jsonObject);
                                            String id = jsonObject.getString("_id");
                                            String name = jsonObject.getString("name");
                                            String type = jsonObject.getString("type");
                                            String phone = jsonObject.getString("phone");
                                            String email = jsonObject.getString("email");
                                            String location = jsonObject.getString("location");
                                            String country  = jsonObject.getString("country");
                                            //may or may not be activated
                                            String activation_status = jsonObject.getString("status");
//                String verified = jsonObject.getString("verified");
                                            Main2Activity._id = jsonObject.getString("_id");
                                            Main2Activity.user_name = jsonObject.getString("name");
                                            Main2Activity.country = country;
                                            Main2Activity.phone = phone;
                                            Main2Activity.email = email;
                                            Main2Activity.location = location;
                                            Main2Activity.activated = activation_status;
                                            Main2Activity.user_type = type;
//                this.verified = verified;getString(R.string.type_key)

                                            editor.putString(getString(R.string.user_id_key), id);
                                            editor.putString(getString(R.string.name_key), name);
                                            editor.putString(getString(R.string.type_key), type);
                                            editor.putString(getString(R.string.phone_key), phone);
                                            editor.putString(getString(R.string.email_key), email);
                                            editor.putString(getString(R.string.location_key), location);
                                            editor.putString(getString(R.string.country_key), country);
                                            editor.putString(getString(R.string.activated_key), activation_status);

//                                            System.out.println("activation status "+activation_status);
//                editor.putString(getString(R.string.verified), verified);
                                            editor.apply();

                                            //if not active, send to the activation page
                                            if(!activation_status.equals("Active")){
//                                                System.out.println("activation status not active "+activation_status);
//                                                Intent i = new Intent(LoginActivity.this, Activate.class);
//                                                startActivity(i);

                                                //                                        update the token
//                                                replyIntent.putExtra(EXTRA_REPLY, "Active");
//                                                setResult(RESULT_OK, replyIntent);
//                                                finish();
                                                Intent activateIntent = new Intent(LoginActivity.this, Activate.class);
                                                startActivity(activateIntent);

                                            }
                                            else{

                                                Intent mainIntent = new Intent(LoginActivity.this, Main2Activity.class);
                                                startActivity(mainIntent);

//                                                Intent replyIntent = new Intent();
//                                        update the token
//                                                replyIntent.putExtra(EXTRA_REPLY, "Inactive");
//                                                setResult(RESULT_OK, replyIntent);
//                                                finish();


//                                                Intent m = new Intent(LoginActivity.this, Main2Activity.class);
//                                                startActivity(m);
                                            }


//                Toast.makeText(
//                        getApplicationContext(),
//                        "login done",
//                        Toast.LENGTH_LONG).show();
                                        }
                                        catch (Exception e){
                                            System.out.println("Error after login: \n "+e);
                                        }



                                        break;
                                    default:
                                        feedback.setVisibility(View.VISIBLE);

                                }
                                mProgressView.setVisibility(View.GONE);
                                mEmailSignInButton.setVisibility(View.VISIBLE);
//                                Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("user_data").getString("id"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                mProgressView.setVisibility(View.GONE);
                                mEmailSignInButton.setVisibility(View.VISIBLE);
                                e.printStackTrace();
                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            mProgressView.setVisibility(View.GONE);
                            mEmailSignInButton.setVisibility(View.VISIBLE);
//                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("email", username);
                    params.put("password", password);

                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){
            mProgressView.setVisibility(View.GONE);
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }




    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public void forgotPassword(){
        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(this);
        inputAlert.setTitle("Forgot password");
        inputAlert.setMessage("Enter your phone number");
        final EditText phone = new EditText(this);
        phone.setShowSoftInputOnFocus(true);
        inputAlert.setView(phone);
        inputAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInputValue = phone.getText().toString();
                sendPasswordRequest(userInputValue);
                dialog.dismiss();
            }
        });
        inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = inputAlert.create();
        alertDialog.show();
    }

    public void sendPasswordRequest( final String phone){


        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = base_url+"client/resendActivationCode";
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
                                Log.d("resendActivationCode", status);
                                switch (status){
                                    case "1":
                                        Toast.makeText(LoginActivity.this,
                                                "Password sent via sms to your phone. Please wait for it.",
                                                Toast.LENGTH_LONG).show();
                                        break;
                                        default:
                                            Toast.makeText(LoginActivity.this,
                                                    "Error resending your password. Please check to make sure the phone number is correct",
                                                    Toast.LENGTH_LONG).show();

                                }
                            } catch (JSONException e) {

                                e.printStackTrace();
                            }
//                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
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
                    params.put("type", "forgot_password");
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){

            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        //navigate to signup
        Intent i = new Intent(LoginActivity.this, Signup.class);
        startActivity(i);
    }
}

