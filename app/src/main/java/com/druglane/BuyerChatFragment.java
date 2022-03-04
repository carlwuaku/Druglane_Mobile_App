package com.druglane;


import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
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

import adapters.MessageAdapter;
import adapters.RecyclerItemClickListener;
import models.Message;
import network.VolleySingleton;
import viewmodels.MessageViewModel;
import viewmodels.ReplyViewModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class BuyerChatFragment extends Fragment
        implements ActionMode.Callback
{



    public RecyclerView myRecylerView ;
    public List<Message> MessageList ;
    public MessageAdapter adapter;
//    public EditText messagetxt ;
//    public Button send ;
    private MessageViewModel vm;
    private ReplyViewModel rvm;

    public static ActionMode actionMode;
    private boolean isMultiSelect = false;

    //items from selection
    private List<Message> selectedIds = new ArrayList<>();
    private List<Message> selectedItems = new ArrayList<>();
    private SearchView searchView;
    TextView no_items;
    public int unread_count = 0;
//    we will reset this when the sound plays so that it doesnt play when the person navigates
    public int page_unread_count = 0;
    private ImageButton home_send_btn;
    private EditText home_input;

    public BuyerChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootview = inflater.inflate(R.layout.fragment_buyer_chat, container, false);

        vm = ViewModelProviders.of(this).get(MessageViewModel.class);
        rvm = ViewModelProviders.of(this).get(ReplyViewModel.class);
        page_unread_count = 0;




        no_items = rootview.findViewById(R.id.no_items);
        RecyclerView recyclerView = rootview.findViewById(R.id.messagelist);
         adapter = new MessageAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLongClickable(true);

        vm.getAllMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable final List<Message> words) {
                // Update the cached copy of the words in the adapter.
                if(words.size() > 0){
                    no_items.setVisibility(View.GONE);
                    if(searchView != null){
                        searchView.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    if(searchView != null){
                        searchView.setVisibility(View.GONE);
                    }
                    no_items.setVisibility(View.VISIBLE);
                }
                adapter.setWords(words);
//                set the title to have a count of the unread messages
//                System.out.println("looperx words size "+ words.size());
                unread_count = 0;
                for(int i = 0; i < words.size(); i++){
//                    System.out.println("looperx unread "+ words.get(i).getNum_unread());
                    if(words.get(i).getNum_unread() > 0){
                        unread_count++;

                    }
                }
                CharSequence title = "Buying" ;

                if(unread_count > 0){
                     title = "Buying ("+ String.valueOf(unread_count)+")" ;

                }



                Main2Activity.tabLayout.getTabAt(0).setText(title);
                home_send_btn = rootview.findViewById(R.id.home_send_btn);
                home_input = rootview.findViewById(R.id.home_input);

                home_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            startSearch();

                            return true;
                        }
                        return false;
                    }
                });

                home_send_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        startSearch();
                    }
                });
            }
        });

//        recyclerView.addOnItemTouchListener(
//                new RecyclerItemClickListener(getActivity(),
//                        recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
////                        viewReplies(position, v);
//                        Intent replyIntent = new Intent(getActivity(), ViewRepliesActivity.class);
//                        if(adapter != null){
//                            if(isMultiSelect){
//                                multiSelect(position);
//                            }
//                            else {
//                                Message m =
//                                        adapter.getWord(position);
//                                replyIntent.putExtra("message", m.getMessage());
//                                startActivity(replyIntent);
//                            }
//                        }
//                        else{
//                            Toast.makeText(getActivity(), "null object", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onItemLongClick(View view, int position) {
//                        if (!isMultiSelect){
//                            selectedIds = new ArrayList<>();
//                            isMultiSelect = true;
//
//                            if (actionMode == null){
//                                actionMode = getActivity().startActionMode(BuyerChatFragment.this); //show ActionMode.
//                            }
//                        }
//
//                        multiSelect(position);
//                    }
//                }));


        //touch listener
        adapter.setOnItemClickListener(new MessageAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
//                viewReplies(position, v);

                if(adapter != null){
                    if(isMultiSelect){
                        multiSelect(position);
                    }
                    else {
                        Intent replyIntent = new Intent(getActivity(), ViewRepliesActivity.class);

                        Message m =
                                adapter.getWord(position);
                        replyIntent.putExtra("message", m.getMessage());
                        replyIntent.putExtra("client_key", m.getClient_key());
                        replyIntent.putExtra("filename", m.getFilename());
                        startActivity(replyIntent);
                    }
                }
                else{
                    Toast.makeText(getActivity(), "null object", Toast.LENGTH_SHORT).show();
                }
            }



            @Override
            public void onItemLongClick(int position, View v) {
//                System.out.println("item long clicked");
                if (!isMultiSelect){
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;

                    if (actionMode == null){
                        actionMode = getActivity().startActionMode(BuyerChatFragment.this); //show ActionMode.
                    }
                }

                multiSelect(position);
            }
        });







        return  rootview;
    }

    public void startSearch(){
        String input = home_input.getText().toString();
        if(input.isEmpty()){
            Toast.makeText(getActivity(), "Type a name or description", Toast.LENGTH_SHORT).show();

        }
        else{
            Intent i = new Intent(getActivity(), AddSearch.class);
            i.putExtra("param", input);
            home_input.setText("");
            startActivity(i);
        }
    }



    private void multiSelect(int position) {
        Message data = adapter.getWord(position);

        if (data != null){
//            System.out.println("multiselect data not null "+data);
            if (actionMode != null) {
//                System.out.println("multiselect action mode not null ");
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
            else{
//                System.out.println("multiselect action mode is null ");
            }
        }
        else{
//            System.out.println("multiselect data is  null ");
        }
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
//
//            case R.id.delete_all:
//
//                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
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
//                AlertDialog alert1 = builder1.create();
//                alert1.show();
//
//
//                return  true;
//
//            case R.id.block_all:
//
//                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
//                builder2.setMessage("Block all in the current list from receiving replies?")
//                        .setCancelable(false)
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                startBlocking(adapter.getFilteredItems());
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
//                AlertDialog alert2 = builder2.create();
//                alert2.show();
//
//
//                return  true;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.options_menu, menu);
//        menu.findItem(R.id.action_seller_settings).setVisible(false);
//        menu.findItem(R.id.action_reply_history).setVisible(false);
//        // Associate searchable configuration with the SearchView
//        /*SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
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
//       super.onCreateOptionsMenu(menu, inflater);
//    }

    public void viewReplies(int position, View v){
        Intent replyIntent = new Intent(getContext(), ViewRepliesActivity.class);
        if(adapter != null){
            Message m =
                    adapter.getWord(position);
            replyIntent.putExtra("message", m.getMessage());
            startActivity(replyIntent);
        }
        else{
            Toast.makeText(getContext(), "null object", Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_select, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode aMode, MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.action_delete:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setMessage("Delete selected?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startDelete(adapter.getSelectedItems());
                                //as all selected items have been deleted, set the selected to empty
                                actionMode.finish();
//                                actionMode = null;
//                                isMultiSelect = false;
//                                selectedIds = new ArrayList<>();
//                                adapter.setSelectedIds(new ArrayList<Message>());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;

                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();
                return true;

            case R.id.block:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                builder2.setMessage("Block selected from receiving any more replies?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startBlocking(adapter.getSelectedItems());
                                //as all selected items have been deleted, set the selected to empty
                                actionMode.finish();
//                                actionMode = null;
//                                isMultiSelect = false;
//                                selectedIds = new ArrayList<>();
//                                adapter.setSelectedIds(new ArrayList<Message>());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;

                            }
                        });
                AlertDialog alert2 = builder2.create();
                alert2.show();
                return true;

            case R.id.action_select_all:
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
                default:

        }


        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionxMode) {
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
        ArrayList<String> _ids = new ArrayList<String>();
        for(int i = 0; i < items.size(); i++){
            Message p = items.get(i);

                vm.delete(p);
                _ids.add(p.getClient_key());
        }
//        send to server
        String keys = _ids.toString().replace("[", "")
                .replace("]", "");
        emitDeleteToSocket(keys);
        sendDeleteNotification(keys);
        deleteFromDb(keys);

    }

//in this case the items are not deleted from the db
    public void startBlocking(List<Message> items){
        ArrayList<String> _ids = new ArrayList<String>();
        for(int i = 0; i < items.size(); i++){
            Message p = items.get(i);
            vm.setBlocked(p.getClient_key());
            _ids.add(p.getClient_key());
        }
//        send to server
        String keys = _ids.toString().replace("[", "")
                .replace("]", "");
        emitDeleteToSocket(keys);
        sendDeleteNotification(keys);
        blockFromDb(keys);

    }

    public void sendDeleteNotification(final String _ids){

        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this.getActivity());
        String base_url = network.base_url;
        String uri = base_url+"send_delete_notifications";
//"send_notifications?filename="+filename+"&message="+msg+"&sender_name="+this.user.name+"&user="+this.user._id

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
                                Log.d("send delete service", status);

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
                            Log.e("error", error.toString());
//                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("_ids", _ids);
                    params.put("user", Main2Activity._id);
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){

            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteFromDb(final String _ids){

        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this.getActivity());
        String base_url = network.base_url;
        String uri = base_url+"message/deleteMessages";
//"send_notifications?filename="+filename+"&message="+msg+"&sender_name="+this.user.name+"&user="+this.user._id

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
                                Log.d("send delete service", status);

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
                            Log.e("error", error.toString());
//                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("_ids", _ids);
                    params.put("user", Main2Activity._id);

                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){

            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void blockFromDb(final String _ids){

        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this.getActivity());
        String base_url = network.base_url;
        String uri = base_url+"message/blockReplies";
//"send_notifications?filename="+filename+"&message="+msg+"&sender_name="+this.user.name+"&user="+this.user._id

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
                                Log.d("send block service", status);

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
                            Log.e("error", error.toString());
//                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    params.put("_ids", _ids);
                    params.put("user", Main2Activity._id);

                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){

            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void emitDeleteToSocket(String _ids){
        JSONObject data = new JSONObject();
        try {
            data.put("user", Main2Activity._id);
            data.put("_ids", _ids);

            if(Main2Activity.socket.connected()){
                Main2Activity.socket.emit("messages_deleted",  data);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
