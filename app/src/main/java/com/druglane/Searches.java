package com.druglane;


import android.app.AlertDialog;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import adapters.MessageAdapter;
import adapters.SearchesAdapter;
import models.Message;
import network.VolleySingleton;
import viewmodels.MessageViewModel;
import viewmodels.ReplyViewModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class Searches extends Fragment implements
        ActionMode.Callback {

    public RecyclerView myRecylerView ;
    public List<Message> MessageList ;
    public SearchesAdapter adapter;
    //    public EditText messagetxt ;
//    public Button send ;
    private MessageViewModel vm = null;

    public static ActionMode actionMode;
    private boolean isMultiSelect = false;

    //items from selection
    private List<Message> selectedIds = new ArrayList<>();
    private List<Message> selectedItems = new ArrayList<>();
    private SearchView searchView;
    TextView no_items;
    LinearLayout not_seller;
    TextView not_subscribed, instruction;
    Button subscribe_link, go_to_account;
    RecyclerView recyclerView;
    LinearLayout checking_subscription, not_subscribed_layout, subscribed_layout;
    ImageView img;
    private ProgressBar mProgressView;
    private String physical_location;

    public int unread_count = 0;

    public Searches() {
        // Required empty public constructor
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
//            check the subscription

//            when page is navigated to, clear all unread
            if(vm != null){
                vm.resetHasUnread();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_searches, container, false);

        vm = ViewModelProviders.of(this).get(MessageViewModel.class);
        no_items = rootview.findViewById(R.id.no_items_2);
        not_seller = rootview.findViewById(R.id.not_seller);
        not_subscribed =  rootview.findViewById(R.id.not_subscribed);
        subscribe_link = rootview.findViewById(R.id.subscribe_link);
        checking_subscription =  rootview.findViewById(R.id.checking_subscription);
        subscribed_layout =  rootview.findViewById(R.id.subscribed_layout);
        not_subscribed_layout =  rootview.findViewById(R.id.not_subscribed_layout);
        img  = rootview.findViewById(R.id.seller_image);
        go_to_account = rootview.findViewById(R.id.go_to_account);
        subscribe_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebURL("https://druglanechat.firebaseapp.com/subscribe/" + Main2Activity._id);
            }
        });

         recyclerView = rootview.findViewById(R.id.searchlist);
         instruction = rootview.findViewById(R.id.reply_instruction);
         adapter = new SearchesAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        checking_subscription.setVisibility(View.GONE);
        not_subscribed_layout.setVisibility(View.GONE);
        subscribed_layout.setVisibility(View.GONE);

        final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String type = sharedPref.getString(getString(R.string.type_key), "buyer");
        physical_location = sharedPref.getString(getString(R.string.physical_location), "N/A");

        if(!type.equals("seller")){
            not_seller.setVisibility(View.VISIBLE);
        }
        //user is a seller
        else{

            not_seller.setVisibility(View.GONE);
            //check if the subscription is active
            //no subscription was found or the subscription ended, check if it was renewed
            draw();
//            if(Main2Activity.subscription_end == "" ){
//            img.setImageResource(R.drawable.warning);
//            System.out.println("subscription not ok");
//                checkSubscription();
//            }
//            else{
//                //check if the date is not past
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                try {
//                    Date date = format.parse(Main2Activity.subscription_end);
//                    System.out.println(date);
//                    if(new Date().after(date)){
//                        //subscription has expired
//                        System.out.println("subscription date is not fine");
//                        not_subscribed_layout.setVisibility(View.VISIBLE);
//                        checkSubscription();
//                    }
//                    else{
//                        System.out.println("subscription date is fine");
//                        //subscription was found. go on
//                        draw();
//                    }
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//            }







        }

//        click go to account button
        go_to_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AccountActivity.class);
                startActivity(i);
            }
        });





        //touch listener
        adapter.setOnItemClickListener(new SearchesAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

//                if (!isMultiSelect){
//                    selectedIds = new ArrayList<>();
//                    isMultiSelect = true;
//
//                    if (actionMode == null){
//                        actionMode = getActivity().startActionMode(Searches.this); //show ActionMode.
//                    }
//                }
//
//                multiSelect(position);
//
                if(isMultiSelect){
                    multiSelect(position);
                }
                else {
                    Message m = adapter.getWord(position);
                    replySingle(m);
                }

            }

            @Override
            public void onItemLongClick(int position, View v) {
                if (!isMultiSelect){
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;

                    if (actionMode == null){
                        actionMode = getActivity().startActionMode(Searches.this); //show ActionMode.
                    }
                }

                multiSelect(position);
            }
        });
//        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                viewReplies(position, view);
//            }
//
//            @Override
//            public void onItemLongClick(View view, int position) {
//
//            }
//        }));




        return  rootview;
    }

    private  void draw(){
        subscribed_layout.setVisibility(View.VISIBLE);
        vm.getAllSearches().observe(getActivity(), new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable final List<Message> words) {

                // Update the cached copy of the words in the adapter.
                if(words.size() > 0){
                    no_items.setVisibility(View.GONE);
                    img.setVisibility(View.GONE);
                    instruction.setVisibility(View.VISIBLE);
                    if(searchView != null){
                        searchView.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    img.setVisibility(View.VISIBLE);
                    img.setImageResource(R.drawable.box);
                    no_items.setVisibility(View.VISIBLE);
                    if(searchView != null){
                        searchView.setVisibility(View.GONE);
                    }
                    instruction.setVisibility(View.GONE);



                }
                adapter.setWords(words);

                unread_count = 0;
                for(int i = 0; i < words.size(); i++){
                    if(Objects.equals(words.get(i).getHas_unread(), "yes" )){
                        unread_count++;
                    }
                }
                CharSequence title = "Selling";
                if(unread_count > 0){
                    title = "Selling ("+ String.valueOf(unread_count)+")" ;

                }


                Main2Activity.tabLayout.getTabAt(1).setText(title);
            }
        });
    }

    private void multiSelect(int position) {
        Message data = adapter.getWord(position);
        if (data != null){
            if (actionMode != null) {
                if (selectedIds.contains(data)) {
                    selectedIds.remove(data);
                    data.setSelected(false);
                }
                else {
                    selectedIds.add(data);
                    data.setSelected(true);
                }
                if (selectedIds.size() > 0){
                    actionMode.setTitle(String.valueOf(selectedIds.size())); //show selected item count on action mode.
                }
                else{
                    actionMode.setTitle(""); //remove item count from action mode.
                    actionMode.finish(); //hide action mode.
                }
                adapter.setSelectedIds(selectedIds);

            }
        }
    }

    public void sendReply( String reply){



        if(!reply.isEmpty() ){

            for(int i = 0; i < adapter.getSelectedItems().size(); i++){
                Message message = adapter.getSelectedItems().get(i);

                JSONObject data = new JSONObject();
                try {
                    data.put("reply_message", reply);
                    data.put("buyer", message.getUser());
                    data.put("search_param", message.getMessage());
                    data.put("seller", Main2Activity._id);
                    data.put("seller_name", Main2Activity.user_name);
                    data.put("seller_phone", Main2Activity.phone);
                    data.put("seller_email", Main2Activity.email);
                    data.put("seller_location", Main2Activity.location);
                    data.put("verified", "");
                    data.put("client_key", message.getClient_key());
                    data.put("physical_location", physical_location);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(Main2Activity.socket.connected()){
                    Main2Activity.socket.emit("message_reply", data);
                    sendNotification(message, reply);
                    vm.delete(message);
                }
                else{
                    Toast.makeText(getActivity(), "Cannot connect to server. Try again later", Toast.LENGTH_SHORT).show();
                }

            }



        }
        else{
            Toast.makeText(getActivity(), "A reply is required", Toast.LENGTH_SHORT).show();
        }
        if(actionMode != null){
            actionMode.finish();
        }

    }

//    reply a single item
    public void replySingle(final Message message){
        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(getActivity());
        inputAlert.setTitle(message.getMessage());
                inputAlert.setMessage("Enter your price");
        final EditText userInput = new EditText(getActivity());
        userInput.setShowSoftInputOnFocus(true);
        inputAlert.setView(userInput);
        inputAlert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInputValue = userInput.getText().toString();
                List<Message> items = new ArrayList<>();
                items.add(message);
                adapter.setSelectedIds(items);

                sendReply( userInputValue);
//                        vm.delete(m);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
////        //noinspection SimplifiableIfStatement
////        if (id == R.id.action_settings) {
////            return true;
////        }
//
//        switch (id){
//
//            case R.id.action_seller_settings:
//                Intent i = new Intent(this.getActivity(), SellerSettings.class);
//                startActivity(i);
//                break;
//
//            case R.id.action_reply_history:
//                //go reply history in browser
//                openWebURL("https://thedruglane.com/reply_history/" + Main2Activity._id);
//                break;
//
//            case R.id.delete_all:
//
//                android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(getActivity());
//                builder1.setMessage("Delete all in the current list?")
//                        .setCancelable(false)
//                        .setPositiveButton("Sure!", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                startDelete(adapter.getFilteredItems());
//                                //as all selected items have been deleted, set the selected to empty
//                                setActionMode(null);
//                                isMultiSelect = false;
//                                selectedIds = new ArrayList<>();
//                                adapter.setSelectedIds(new ArrayList<Message>());
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                                return;
//
//                            }
//                        });
//                android.support.v7.app.AlertDialog alert1 = builder1.create();
//                alert1.show();
//
//
//                return  true;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.options_menu, menu);
//        menu.findItem(R.id.block_all).setVisible(false);
//        // Associate searchable configuration with the SearchView
//       /* SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        searchView = (SearchView) menu.findItem(R.id.action_search)
//                .getActionView();
//        searchView.setSearchableInfo(searchManager
//                .getSearchableInfo(getActivity().getComponentName()));
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//
//        // listening to search query text change
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // filter recycler view when query submitted
//                adapter.getFilter().filter(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                // filter recycler view when text is changed
//                adapter.getFilter().filter(query);
//                return false;
//            }
//        });*/
//        if(!Main2Activity.user_type.equals("seller")) {
//            menu.findItem(R.id.delete_all).setVisible(false);
//            menu.findItem(R.id.action_seller_settings).setVisible(false);
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_reply, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.action_delete_searches:
                android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder1.setMessage("Delete selected?")
                        .setCancelable(false)
                        .setPositiveButton("Sure!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startDelete(adapter.getSelectedItems());
                                //as all selected items have been deleted, set the selected to empty
                                setActionMode(null);
                                isMultiSelect = false;
                                selectedIds = new ArrayList<>();
                                adapter.setSelectedIds(new ArrayList<Message>());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;

                            }
                        });
                android.support.v7.app.AlertDialog alert1 = builder1.create();
                alert1.show();
                return true;

            case R.id.action_select_all_searches:
                for(int i = 0; i < adapter.getFilteredItems().size(); i++){
                    Message p = adapter.getFilteredItems().get(i);
                    if(!selectedIds.contains(p)){
                        selectedIds.add(p);
                        p.setSelected(true);
                    }
                    actionMode.setTitle(String.valueOf(selectedIds.size()));
                    adapter.setSelectedIds(selectedIds);
                }
                break;

            case R.id.action_reply_searches:
                /**
                 * TODO
                 */
                //handle connection status here

                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(getActivity());
                inputAlert.setTitle("Reply selected messages with your price");
//                inputAlert.setMessage("Enter your price");
                final EditText userInput = new EditText(getActivity());
                userInput.setShowSoftInputOnFocus(true);
                inputAlert.setView(userInput);
                inputAlert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userInputValue = userInput.getText().toString();
                        sendReply( userInputValue);
//                        vm.delete(m);
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
                break;
            default:

        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode aMode) {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList<>();
        for(int i = 0; i < adapter.getFilteredItems().size(); i++){
            Message p = adapter.getFilteredItems().get(i);
            p.setSelected(false);
        }
        adapter.setSelectedIds(new ArrayList<Message>());
    }

    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }

    public void startDelete(List<Message> items){
        for(int i = 0; i < items.size(); i++){
            Message p = items.get(i);

            vm.delete(p);



        }
    }

    public void sendNotification(final Message message, final String reply){
        /**
         * data.put("reply_message", reply);
         *             data.put("buyer", message.getUser());
         *             data.put("search_param", message.getMessage());
         *             data.put("seller", Main2Activity._id);
         *             data.put("seller_name", Main2Activity.user_name);
         *             data.put("seller_phone", Main2Activity.phone);
         *             data.put("seller_email", Main2Activity.email);
         *             data.put("seller_location", Main2Activity.location);
         */

        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(getActivity());
        String base_url = network.base_url;
        String uri = base_url+"send_reply_notifications";
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
                                Log.d("send_notification", status);

//                                Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("user_data").getString("id"), Toast.LENGTH_LONG).show();
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
                    params.put("buyer", message.getUser());
                    params.put("reply_message", reply);
                    params.put("search_param", message.getMessage());
                    params.put("seller", Main2Activity._id);
                    params.put("seller_name", Main2Activity.user_name);
                    params.put("seller_phone", Main2Activity.phone);
                    params.put("seller_email", Main2Activity.email);
                    params.put("seller_location", Main2Activity.location);
                    params.put("verified", "");
                    params.put("filename", message.getFilename());
                    params.put("client_key", message.getClient_key());
                    params.put("physical_location", physical_location);
                    params.put("device_id", message.getDevice_id());
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){

            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void openWebURL( String inURL ) {
        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( inURL ) );

        startActivity( browse );
    }

    //CHECK IF USER HAS AN ACTIVE SUBSCRIPTION
    //check for an active subscription
//    public void checkSubscription(){
//        String _id = Main2Activity._id;
//        checking_subscription.setVisibility(View.VISIBLE);
//        final VolleySingleton network = VolleySingleton.getInstance(getActivity());
//        String base_url = network.base_url;
//        String uri = Uri.parse(base_url+"getActiveSubscription?_id="+_id).toString();
//        try{
//            StringRequest stringRequest = new StringRequest
//                    (Request.Method.GET,uri,  new Response.Listener<String>() {
//
//                        @Override
//                        public void onResponse(String response) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(response);
//                                System.out.println("subscription data: " +jsonObject);
//                                //get the status
//                                String status = jsonObject.getString("status");
//                                System.out.println(jsonObject);
//                                checking_subscription.setVisibility(View.GONE);
//                                switch (status){
//                                    case "1":
//                                        System.out.println("subscription status ok");
//                                        //if no data was returned, user doesn't have any subscription
//                                        JSONObject data = jsonObject.getJSONObject("data");
//                                        if(data == null){
//                                            //not subscribed
//                                            not_subscribed_layout.setVisibility(View.VISIBLE);
//
//                                        }
//                                        else{
//                                            not_subscribed_layout.setVisibility(View.GONE);
//                                            SharedPreferences sharedPref = Searches.this.getActivity().getSharedPreferences(
//                                                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//
//                                            SharedPreferences.Editor editor = sharedPref.edit();
//
//                                            editor.putString(getString(R.string.subscription_key), data.getString("end_date"));
////                editor.putString(getString(R.string.verified), verified);
//                                            editor.apply();
//                                            Main2Activity.subscription_end = data.getString("end_date");
//                                            System.out.println(Main2Activity.subscription_end);
//                                            //subscribed
//                                            draw();
//
//                                        }
//
//                                        break;
//                                    default:
//                                        not_subscribed_layout.setVisibility(View.VISIBLE);
//                                        System.out.println("status not ok");
//
//                                }
//                            } catch (JSONException e) {
//                                checking_subscription.setVisibility(View.GONE);
//                                not_subscribed_layout.setVisibility(View.VISIBLE);
//                                e.printStackTrace();
//                            }
////                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
//                        }
//
//
//                    }, new Response.ErrorListener() {
//
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            checking_subscription.setVisibility(View.GONE);
//                            not_subscribed_layout.setVisibility(View.VISIBLE);
//                            error.printStackTrace();
////                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//
//
//            network.getRequestQueue().add(stringRequest);
//
//
//
//        }catch (Exception e){
//            mProgressView.setVisibility(View.GONE);
////            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }


}
