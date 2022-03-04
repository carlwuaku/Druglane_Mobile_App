package models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Carl on 11/15/2018.
 */

@Entity
public class Message {
    @NonNull
    private String message;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull

    private String user;
    @NonNull
    private String filename;
    @NonNull
    private  String sender_name;

    @NonNull
    private  String has_unread;

    @NonNull
    private  String blocked;

    @NonNull
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(@NonNull String quantity) {
        this.quantity = quantity;
    }

    @NonNull
    private  String quantity;

    @NonNull
    public String getHas_unread() {
        return has_unread;
    }

    public void setHas_unread(@NonNull String has_unread) {
        this.has_unread = has_unread;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private int num_unread;

    @NonNull
    public int getTotal_replies() {
        return total_replies;
    }

    public void setTotal_replies(@NonNull int total_replies) {
        this.total_replies = total_replies;
    }

    @NonNull
    private int total_replies;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Ignore

    private boolean selected;

    @NonNull
    public String getLast_reply() {
        return last_reply;
    }

    public void setLast_reply( String last_reply) {
        this.last_reply = last_reply;
    }

    @NonNull
    private String last_reply;


    @NonNull
    public String getLast_reply_time() {
        return last_reply_time;
    }

    public void setLast_reply_time(@NonNull String last_reply_time) {
        this.last_reply_time = last_reply_time;
    }

    @NonNull
    private String last_reply_time;

    public int getNum_unread() {
        return num_unread;
    }

    public void setNum_unread(int num_unread) {
        this.num_unread = num_unread;
    }

    @NonNull
    public String getClient_key() {
        return client_key;
    }

    public void setClient_key(@NonNull String client_key) {
        this.client_key = client_key;
    }

    @NonNull
    private String client_key;

    @NonNull
    private String price_filter;

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    @NonNull
    private String status;


//    in some cases, a file may be selected, but no internet to upload to firebase. we save it here and
//    then compress it when its ready to be uploaded
    @NonNull
    private String file_uri;
    @NonNull
    private String device_id;

    @NonNull
    private String date;



    //    @NonNull
//    private String has_unread;

//    public String getHas_unread() {
//        return has_unread;
//    }
//
//    public void setHas_unread(String has_unread) {
//        this.has_unread = has_unread;
//    }

    @Ignore
    public Message() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:m:ss");
        String formattedDate = df.format(c);
        System.out.println(formattedDate);
        this.date = formattedDate;
    }

    public Message(String message, String user, String filename, String sender_name) {
        this.message = message;
        this.user = user;
        this.filename = filename;
        this.sender_name = sender_name;
        this.num_unread = 0;
        this.last_reply = "";
        this.last_reply_time = "";
        this.selected = false;
        this.has_unread = "no";
        this.total_replies = 0;
        this.status = "pending";
        this.quantity = "N/A";
        this.file_uri = "";
        this.blocked = "";
        this.device_id = "";
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:m:ss");
        String formattedDate = df.format(c);
        System.out.println(formattedDate);
        this.date = formattedDate;
//        this.has_unread = has_unread;

    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    @NonNull
    public String getFile_uri() {
        return file_uri;
    }

    public void setFile_uri(@NonNull String file_uri) {
        this.file_uri = file_uri;
    }

    @NonNull
    public String getBlocked() {
        return blocked;
    }

    public void setBlocked(@NonNull String blocked) {
        this.blocked = blocked;
    }

    @NonNull
    public String getPrice_filter() {
        return price_filter;
    }

    public void setPrice_filter(@NonNull String price_filter) {
        this.price_filter = price_filter;
    }

    @NonNull
    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(@NonNull String device_id) {
        this.device_id = device_id;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }
}
