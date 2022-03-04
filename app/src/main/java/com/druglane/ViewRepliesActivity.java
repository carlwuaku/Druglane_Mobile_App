package com.druglane;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import adapters.MessageAdapter;
import adapters.ViewRepliesAdapter;
import models.Message;
import models.Reply;
import viewmodels.MessageViewModel;
import viewmodels.ReplyViewModel;

public class ViewRepliesActivity extends AppCompatActivity {
    public RecyclerView myRecylerView ;
    public List<Reply> replies ;
    public ViewRepliesAdapter adapter;
//    public TextView title;

    private ReplyViewModel rvm;
    private MessageViewModel vm;
    public String message;
    public String client_key;
    public  String file_name = null;
    public TextView no_items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_replies);
//        android.app.ActionBar ab = getActionBar();

        Bundle extras = getIntent().getExtras();
         message = extras.getString("message");
         client_key = extras.getString("client_key");
         file_name = extras.getString("filename");
         System.out.println("filename "+file_name);
//        title = findViewById(R.id.reply_title);
//        title.setVisibility(View.GONE);
        getSupportActionBar().setTitle(message);
//        if(ab != null){
//            ab.setTitle(message);
//        }
//        else{
//            ab.setTitle("Replies");
//        }
        no_items = findViewById(R.id.no_replies_text);
        rvm = ViewModelProviders.of(this).get(ReplyViewModel.class);
        vm = ViewModelProviders.of(this).get(MessageViewModel.class);

        vm.resetUnread(client_key);

        RecyclerView recyclerView = findViewById(R.id.replieslist);
        adapter = new ViewRepliesAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        rvm.getMessageReplies(client_key).observe(this, new Observer<List<Reply>>() {
            @Override
            public void onChanged(@Nullable final List<Reply> words) {
                // Update the cached copy of the words in the adapter.
                if(words.size() < 1){
                    no_items.setVisibility(View.VISIBLE);
                }
                else{
                    no_items.setVisibility(View.GONE);
                }
                adapter.setWords(words);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_replies_menu, menu);
        if(file_name == null || file_name.equals("")){
            menu.findItem(R.id.view_attachment_menu).setVisible(false);
        }
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


            case R.id.view_attachment_menu:
                Intent i = new Intent(this, ViewImage.class);

                i.putExtra("message", message);
                i.putExtra("imageUrl", file_name);
                startActivity(i);
                return  true;

        }

        return super.onOptionsItemSelected(item);
    }

}
