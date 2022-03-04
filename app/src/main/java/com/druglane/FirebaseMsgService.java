package com.druglane;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import android.app.Application;
import com.google.firebase.messaging.FirebaseMessagingService;
//
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import dao.MessageDao;
import dao.ReplyDao;
import database.Repository;
import database.RoomDatabase;
import models.Message;
import models.Reply;
import viewmodels.ReplyViewModel;

/**
 * Created by Carl on 11/25/2018.
 */

public class FirebaseMsgService
        extends
        FirebaseMessagingService
{
    private static final String TAG = "FirebaseMsgService";

    private ReplyDao replyDao;
    private MessageDao messageDao;
    public FirebaseMsgService() {

//        mRepository = new Repository(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        RoomDatabase db = RoomDatabase.getDatabase(this);
        replyDao = db.replyDao();
        messageDao = db.messageDao();
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification Title: " +
                    remoteMessage.getNotification().getTitle());

            Log.d(TAG, "Notification Message: " +
                    remoteMessage.getNotification().getBody());
        }

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " +
                    remoteMessage.getData().get("type"));

//            Toast.makeText(this, "New message: "+remoteMessage.getData().get("reply_message"), Toast.LENGTH_SHORT);
            if(Objects.equals(remoteMessage.getData().get("type"), "new_reply")){
                try{
                Log.d(TAG, "client key " +
                        remoteMessage.getData().get("client_key"));
                final String search_param = remoteMessage.getData().get("search_param");
                String reply_message = remoteMessage.getData().get("reply_message");
                String seller_name = remoteMessage.getData().get("seller_name");
                String seller_phone = remoteMessage.getData().get("seller_phone");
                String seller_email = remoteMessage.getData().get("seller_email");
                String seller_location =remoteMessage.getData().get("seller_location");
                String seller_id = remoteMessage.getData().get("seller");
                String client_key = remoteMessage.getData().get("client_key");
                    String verified = remoteMessage.getData().get("verified");
                    String pl = remoteMessage.getData().get("physical_location");

                // make instance of reply and save to db
//                System.out.println("new reply loca "+pl);
                Reply r = new Reply("self",reply_message, search_param, seller_id,seller_name, seller_phone, seller_email,
                        seller_location, "", "no");
                r.setPhysical_location(pl);
                r.setClient_key(client_key);
                r.setVerified(verified);

//                mRepository.insertReply(r);
                replyDao.insert(r);
                //update the messages table with last reply
                String last_reply = reply_message+ " \n "+seller_name;
                messageDao.update(search_param, last_reply);
                //update last reply time so it moves to the top
                long millis = System.currentTimeMillis();

//Divide millis by 1000 to get the number of seconds.
                long seconds = millis / 1000;
                messageDao.updateLastTime(client_key, String.valueOf(seconds));
                messageDao.incrementUnread(client_key);
                    messageDao.incrementTotalReplies(client_key);
                    int unread_count = messageDao.getTotalUnread();

//                    notify
                    String title = unread_count > 1 ? "You have new replies" : "New reply to "+search_param;
                    String content = unread_count > 1 ? "You have "+unread_count +" new replies": reply_message;
                    //if app is in background, do notification
                    if(App.inForeground){
                        sendIntent("new_reply");
                    }
                    else{
                        new_reply_notification(title, content, client_key, search_param);
                    }



                }
                catch (Exception e){
                    //System.out.println("new reply error: "+e.getMessage());
                }

            }
            else if(Objects.equals(remoteMessage.getData().get("type"), "new_message")){
                String price_filter = remoteMessage.getData().get("price_filter");
                Log.d(TAG, "new search message " +
                        remoteMessage.getData().get("price_filter"));

//                    filter by price type
                if(!Main2Activity.price_filter.equals("all") && price_filter != null && !price_filter.equals("all")){
                    if(!price_filter.equals(Main2Activity.price_filter)){
                        return;
                    }
                }
                //dont check if there is an active subscription...
//                SharedPreferences sharedPref = FirebaseMsgService.this.getSharedPreferences(
//                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//                String subscription_end = sharedPref.getString(getString(R.string.subscription_key), "");
//                if(Main2Activity.isPast(subscription_end)){
//                    System.out.println("Subscription expired");
//                    return;
//                }
                try{
                String message = remoteMessage.getData().get("message");
                String user = remoteMessage.getData().get("user");
                String sender_name = remoteMessage.getData().get("sender_name");
                    String quantity = remoteMessage.getData().get("quantity");
                //System.out.println("new message client "+remoteMessage.getData().get("client_key"));
                String filename = remoteMessage.getData().get("filename") == null  ? "" : remoteMessage.getData().get("filename");
                if(!Objects.equals(Main2Activity._id, user)){
                    Message message1 = new Message(message, user, filename, sender_name);
                    message1.setClient_key(remoteMessage.getData().get("client_key"));
                    message1.setDevice_id(remoteMessage.getData().get("device_id"));
                    message1.setHas_unread("yes");
                    message1.setQuantity(quantity);
                    message1.setPrice_filter(price_filter);
                    //System.out.println("new messagex "+ message1.getFilename()+
//                            " "+message1.getSender_name()+
//                            " "+message1.getUser()+
//                            " "+message1.getMessage());

                        messageDao.insert(message1);

                    int unread_count = messageDao.getTotalNewSearches(Main2Activity._id);
//                    notify
                    String title = unread_count > 1 ?  unread_count +" new requests from buyers" : "New search from "+sender_name;
                    String content = unread_count > 1 ? "You have "+unread_count +" new requests": message;
                    if(App.inForeground) {
                        sendIntent("new_message");
                    }
                    else{
                        new_search_notification(title, content);

                    }


                }
                }
                catch (Exception e){
                    //System.out.println("new message error: "+e.getMessage());
                }

            }
            //if user is not the same person sending this request, do the delete
            else if(Objects.equals(remoteMessage.getData().get("type"), "messages_deleted")){
                String _ids = remoteMessage.getData().get("_ids");
                String user_id = remoteMessage.getData().get("user");
                Log.d(TAG, "new delete message " +
                        _ids);
                try {
                    if(!user_id.equals(Main2Activity._id)) {
                        messageDao.deleteByClientKey(_ids);
                    }
                    else{
                        System.out.println("delete message from user");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            else if(Objects.equals(remoteMessage.getData().get("type"), "reply_updated")){
                String key = remoteMessage.getData().get("client_key");

                String new_reply = remoteMessage.getData().get("new_reply");
                String seller = remoteMessage.getData().get("seller");
                Log.d(TAG, "new update reply message "
                        );
                long millis = System.currentTimeMillis();

//Divide millis by 1000 to get the number of seconds.
                long seconds = millis / 1000;
                try {
                    replyDao.updateReply(key, seller, new_reply);
                    messageDao.updateLastTime(key, String.valueOf(seconds));
                    messageDao.incrementUnread(key);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            else if(Objects.equals(remoteMessage.getData().get("type"), "new_subscription")){
                String date = remoteMessage.getData().get("end_date");
                //notify and save in preferences
                new_subscription_notification(date);
                sendIntent("new_subscription");
            }
        }
    }

    public void new_reply_notification(String title, String message, String client_key, String search_param){
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra("type", "new_reply_notification");
        intent.putExtra("client_key", client_key);
        intent.putExtra("message", search_param);
        intent.putExtra("channel", "2");
        PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Main2Activity.new_reply_channel_id)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent2)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }

    public void new_search_notification(String title, String message){
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra("type", "new_message_notification");
        intent.putExtra("channel", "2");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Main2Activity.new_search_channel_id)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(2, builder.build());
    }

    public void new_subscription_notification( String end_date){
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra("type", "new_subscription_notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Main2Activity.new_subscription_channel_id)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Subscription renewed")
                .setContentText("Subscription has been renewed. It expires on "+end_date)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(2, builder.build());
    }

    public  void sendIntent(String type){
        Intent intent = new Intent();
        intent.setAction("com.druglane.broadcast.NEW_MESSAGE");
        intent.putExtra("type",type);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
