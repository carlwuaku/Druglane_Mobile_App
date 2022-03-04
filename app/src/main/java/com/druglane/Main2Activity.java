package com.druglane;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dao.MessageDao;
import dao.ReplyDao;
import database.Repository;
import database.RoomDatabase;
import models.Message;
import models.Reply;
import network.VolleySingleton;
import viewmodels.MessageViewModel;
import viewmodels.ReplyViewModel;


public class Main2Activity extends AppCompatActivity {
    public static final int LOGIN_REQUEST_CODE = 1;
    public static final int ACTIVATE_REQUEST_CODE = 2;
    public static Socket socket;
    public static String _id;
    public static String user_name;
    public static String user_type;
    public static String phone;
    public static String email;
    public static String location;
    public static String country, subscription_end;
    public static String activated = "Inactive";

//    public static String verified;
    public static String price_filter = "all";
    private static String device_id;
    private ReplyViewModel rvm;
    private MessageViewModel vm;
//    FloatingActionButton fab;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TabItem buyer_tab, seller_tab;
    public static TabLayout tabLayout;
    BroadcastReceiver br = new MyBroadcastReceiver();
    public static String new_reply_channel_id = "ID1";
    public static String new_search_channel_id = "ID2";
    public static String new_subscription_channel_id = "ID2";
    private MessageDao messageDao;
    private ReplyDao replyDao;
    private Repository repository;
    public static boolean subscribed = false;
    public static String[] countriesList = {

    };
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */






    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int current_index;

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
        final SharedPreferences sharedPref = Main2Activity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        _id = sharedPref.getString(getString(R.string.user_id_key), null);

        if(_id == null){
            Intent i = new Intent(this, Signup.class);
            startActivity(i);
        }
        else{

            //if logged in but not actiavted, move to activation page
            if(!activated.equals("Active")){
                Intent i = new Intent(this, Activate.class);
                startActivity(i);
            }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        final SharedPreferences sharedPref = Main2Activity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        _id = sharedPref.getString(getString(R.string.user_id_key), null);
        user_name = sharedPref.getString(getString(R.string.name_key), null);
        user_type = sharedPref.getString(getString(R.string.type_key), "buyer");
        device_id = sharedPref.getString(getString(R.string.device_id_key), "");
        phone = sharedPref.getString(getString(R.string.phone_key), "N/A");
        email = sharedPref.getString(getString(R.string.email_key), "N/A");
        location = sharedPref.getString(getString(R.string.location_key), "N/A");
        country = sharedPref.getString(getString(R.string.country_key), "N/A");
        price_filter = sharedPref.getString(getString(R.string.price_filter), "all");
        subscription_end = sharedPref.getString(getString(R.string.subscription_key), "");
        activated = sharedPref.getString(getString(R.string.activated_key), "Inactive");
//        verified = sharedPref.getString(getString(R.string.verified), "no");
        System.out.println("subscription end"+subscription_end);
        //    if not logged in, show login page
        if (_id == null) {
            Intent i = new Intent(this, Signup.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(i);
        } else {
            //if logged in but not actiavted, move to activation page
            if (!activated.equals("Active")) {
                Intent i = new Intent(this, Activate.class);
                startActivity(i);
            }
            sendRegistrationToServer();

            connectSocket();

//
            subscribeToTopic();

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        vm = ViewModelProviders.of(this).get(MessageViewModel.class);
        rvm = ViewModelProviders.of(this).get(ReplyViewModel.class);

        seller_tab = (TabItem) findViewById(R.id.seller_tab);
        buyer_tab = (TabItem) findViewById(R.id.buyer_tab);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //System.out.println("creating notification channel");
            CharSequence name = "New reply";
            String description = "Shows notifications for new replies";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(new_reply_channel_id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.cancelAll();

            CharSequence new_message_name = "New buyer request";
            String new_messasge_description = "Shows notifications for new requests from buyers";
            int new_messasge_importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel new_messasge_channel = new NotificationChannel(new_search_channel_id, new_message_name, new_messasge_importance);
            channel.setDescription(new_messasge_description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(new_messasge_channel);
            notificationManager.cancelAll();

            CharSequence new_subscription_name = "Subscription renewed";
            String new_subscription_description = "Shows notifications when subscription is renewed ";
            int new_subsctiption_importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel new_subscription_channel = new NotificationChannel(new_subscription_channel_id, new_subscription_name, new_subsctiption_importance);
            channel.setDescription(new_subscription_description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(new_subscription_channel);
            notificationManager.cancelAll();
        }


        BroadcastReceiver br = new MyBroadcastReceiver();
//        receive broadcast when new reply is received. fired by firebasemsgservice
        LocalBroadcastManager.getInstance(this).registerReceiver(br,
                new IntentFilter("com.druglane.broadcast.NEW_MESSAGE"));


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setVisibility(View.VISIBLE);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(Main2Activity.this, AddSearch.class);
//                startActivity(i);
//            }
//        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        current_index = 0;
                        if (Searches.actionMode != null) {
                            Searches.actionMode.finish();
                        }
//                        fab.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        current_index = 1;
                        if (BuyerChatFragment.actionMode != null) {
                            BuyerChatFragment.actionMode.finish();
                        }
//                        fab.setVisibility(View.GONE);

                        break;
                    default:
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        //in case the app was opened from a notification
        Bundle customData = getIntent().getExtras();
        if (customData != null) {
//            System.out.println("notification_type "+ customData.getString("type"));
//            if(Objects.equals(customData.getString("type"), "new_reply_notification")){
//
//                //navigate to the replies page
//                String client_key = customData.getString("client_key");
//                String message = customData.getString("message");
//                Intent replyIntent = new Intent(this, ViewRepliesActivity.class);
//
//
//                    replyIntent.putExtra("client_key", client_key);
//                replyIntent.putExtra("message", message);
//                    startActivity(replyIntent);
////                tabLayout.getTabAt(0).select();
//            }
//            System.out.println("customdata: "+customData.toString());
//
//            if(customData.getString("client_key") != null){
//                tabLayout.getTabAt(0).select();
//            }
//            else{
//                tabLayout.getTabAt(1).select();
//            }


        }
        RoomDatabase db = RoomDatabase.getDatabase(this);
        replyDao = db.replyDao();
        messageDao = db.messageDao();
        repository = new Repository(this.getApplication());


        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            String recipeId = appLinkData.getLastPathSegment();
            System.out.println(recipeId);
//            Uri appData = Uri.parse("content://com.recipe_app/recipe/").buildUpon()
//                    .appendPath(recipeId).build();
//            showRecipe(appData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        switch (id){


            case R.id.settings:
//                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//                builder1.setMessage("Logout? This will delete all the messages and replies you have")
//                        .setCancelable(false)
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                repository.deleteAllMessages();
//                                repository.deleteAllReplies();
//                                logout();
//                            }
//                        })
//                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                                return;
//
//                            }
//                        });
//                AlertDialog alert1 = builder1.create();
//                alert1.show();
                Intent i = new Intent(Main2Activity.this, SettingsActivity.class);
                startActivity(i);

                return  true;
            case R.id.help:
                openWebURL("https://thedruglane.com/help.html" );
                return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JSONObject jsonObject;
        SharedPreferences sharedPref = Main2Activity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        switch (requestCode){
            case LOGIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    System.out.println();
                    String user_data = data.getStringExtra(LoginActivity.EXTRA_REPLY);
                    //the string is the userdata in json.toString();
                    if(!user_data.equals("Active")){
                        Intent i = new Intent(this, Activate.class);
                        startActivityForResult(i, ACTIVATE_REQUEST_CODE);
                    }
                    else{
                        sendRegistrationToServer();

                        connectSocket();
                    }



//                    try{
//                        jsonObject = new JSONObject(user_data);
//                        System.out.println(jsonObject);
//                        String id = jsonObject.getString("_id");
//                        String name = jsonObject.getString("name");
//                        String type = jsonObject.getString("type");
//                        String phone = jsonObject.getString("phone");
//                        String email = jsonObject.getString("email");
//                        String location = jsonObject.getString("location");
//                        String country  = jsonObject.getString("country");
//                        //may or may not be activated
//                        String status = jsonObject.getString("status");
////                String verified = jsonObject.getString("verified");
//                        _id = jsonObject.getString("_id");
//                        user_name = jsonObject.getString("name");
//                        this.country = country;
//                        this.phone = phone;
//                        this.email = email;
//                        this.location = location;
//                        activated = status;
//                        user_type = type;
////                this.verified = verified;getString(R.string.type_key)
//
//                        editor.putString(getString(R.string.user_id_key), id);
//                        editor.putString(getString(R.string.name_key), name);
//                        editor.putString(getString(R.string.type_key), type);
//                        editor.putString(getString(R.string.phone_key), phone);
//                        editor.putString(getString(R.string.email_key), email);
//                        editor.putString(getString(R.string.location_key), location);
//                        editor.putString(getString(R.string.country_key), country);
//                        editor.putString(getString(R.string.activated_key), status);
//
//
////                editor.putString(getString(R.string.verified), verified);
//                        editor.apply();
//
//                        //if not active, send to the activation page
//
//
////                Toast.makeText(
////                        getApplicationContext(),
////                        "login done",
////                        Toast.LENGTH_LONG).show();
//                    }
//                    catch (Exception e){
//                        System.out.println("Error after login: \n "+e);
//                    }


                } else {
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivityForResult(i, LOGIN_REQUEST_CODE);
//            Toast.makeText(
//                    getApplicationContext(),
//                    "login failed",
//                    Toast.LENGTH_LONG).show();
                }
                break;
            case ACTIVATE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    //just set the variables to active
                    activated = "Active";
                    editor.putString(getString(R.string.activated_key), "Active");
                    editor.apply();
                }
                else {
                    Intent i = new Intent(this, Activate.class);
                    startActivityForResult(i, ACTIVATE_REQUEST_CODE);
//            Toast.makeText(
//                    getApplicationContext(),
//                    "login failed",
//                    Toast.LENGTH_LONG).show();
                }
                break;
                default:

        }

    }

    public void openWebURL( String inURL ) {
        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( inURL ) );

        startActivity( browse );
    }

    public void setTitle(String count){

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position){
                case 0:
                    BuyerChatFragment buyerChatFragment = new BuyerChatFragment();
                    return buyerChatFragment;
                case 1:
                    Searches searchFragment = new Searches();
                    return searchFragment;


                    default:
//                        fab.setVisibility(View.VISIBLE);
                        return PlaceholderFragment.newInstance(position + 1);
            }

        }



        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }



    public static void connectSocket(){
        try {



            socket = IO.socket("https://druglane.herokuapp.com");

            //create connection

            socket.connect();

// emit the event join along side with the nickname
            JSONObject user_data = new JSONObject();
            try {
                user_data.put("handle", user_name);
                user_data.put("type", user_type);
                user_data.put("_id", _id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("add_user",user_data);


        } catch (Exception e) {
            e.printStackTrace();

        }
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

    public void subscribeToTopic(){
        Log.d("country", country);
        Log.d("location", location);
        Log.d("subscribing to topic", "Subscribing to  topic");



        // [START subscribe_topics] if user is a buyer
        if(user_type.equals("seller")){
            FirebaseMessaging.getInstance().subscribeToTopic("sellers")
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Subscribe ok";
                            if (!task.isSuccessful()) {
                                msg = "Message subscribe failed";
                            }
                            Log.d("sub result", msg);
//                        Toast.makeText(Main2Activity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
//        subscribe to your location
            if(!Objects.equals("N/A", country)){
                //        subscribe to your location
                final String country_key = country.replaceAll(" ", "_");
                FirebaseMessaging.getInstance().subscribeToTopic(country_key)
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = "Subscribe ok to "+country_key;
                                if (!task.isSuccessful()) {
                                    msg = "Message subscribe failed";
                                }
                                Log.d("country sub result", msg);
//                        Toast.makeText(Main2Activity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            if(!Objects.equals("N/A", location)){
                final String location_key = location.replaceAll(" ", "_");
                FirebaseMessaging.getInstance().subscribeToTopic(location_key)
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = "Subscribe ok to "+location_key;
                                if (!task.isSuccessful()) {
                                    msg = "Message subscribe failed";
                                }
                                Log.d("location sub result", msg);
//                        Toast.makeText(Main2Activity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        }
        else{
            unsubscribe();
        }



        // [END subscribe_topics]
    }

    public void sendRegistrationToServer() {
        //if token is empty string, use the sharedpreferences
        final String token;
        SharedPreferences sharedPref = Main2Activity.this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        token = sharedPref.getString(getString(R.string.device_id_key), "");
        device_id = token;
        final String _id = sharedPref.getString(getString(R.string.user_id_key), null);
        //System.out.println("device id "+ token);
//        Toast.makeText(this, token, Toast.LENGTH_SHORT);
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = Uri.parse(base_url+"client/api_updateWebToken").toString();


        // Access the RequestQueue through your singleton class.
        try{
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST,uri,  new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {

                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("token", token);
                    params.put("type", "android");
                    params.put("_id", _id);
                    //System.out.println(params.toString());
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("token update error", e.getMessage());
        }
    }


    public void logout(){
        //clear preferences and go to login screen
        SharedPreferences sharedPref = Main2Activity.this.getSharedPreferences(
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

    public void viewAccount(){
        Intent i = new Intent(this, AccountActivity.class);
        startActivity(i);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println(intent.getStringExtra("type"));
            switch (intent.getStringExtra("type")){
                case "new_subscription":
                    String end_date = intent.getStringExtra("value");
                    Toast.makeText(Main2Activity.this, "Subscription renewed", Toast.LENGTH_LONG);
                    //update prefs
                    SharedPreferences sharedPref = Main2Activity.this.getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putString(getString(R.string.subscription_key), end_date);
//                editor.putString(getString(R.string.verified), verified);
                    editor.apply();
                    Main2Activity.subscription_end = end_date;
                    System.out.println("subscription udated. "+end_date);
                    break;
                    default:
                        //for new replies and messages if app is in foreground, make this sound. else do the notification

                        if(App.inForeground){
                            final MediaPlayer mediaPlayer = MediaPlayer.create(Main2Activity.this , R.raw.snap);
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.release();
                                }
                            });
                        }

            }


        }
    }

    public static boolean isInternetAvailable(){
        try{
            InetAddress ipAddress = InetAddress.getByName("google.com");
            return !ipAddress.equals("");
        }
        catch (UnknownHostException ue){
            return false;
        }
    }


    public static boolean isPast(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date now = new Date();
        if(dateStr.equals("")){
            return false;
        }
        boolean past = false;
        try {
            if (sdf.parse(dateStr.substring(0, 10)).before(now)) {
                System.out.println(dateStr + " is in the past.");
                past = true;
            } else {
                System.out.println(dateStr + " is in the future.");
                past = false;
            }
        } catch (ParseException e) {
            past = true;
            e.printStackTrace();
        }
        return past;
    }

    @Override
    public void onBackPressed() {
        //if on sellers, move back to buyers. if on buyers close app
        if(current_index == 1){
            tabLayout.getTabAt(0).select();
        }
        else{
            finish();
            moveTaskToBack(true);
        }
//        super.onBackPressed();
    }
}
