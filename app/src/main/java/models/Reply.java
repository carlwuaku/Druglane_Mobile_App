package models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Carl on 11/15/2018.
 */
@Entity
public class Reply {

    private String buyer;
    private String reply_message;
    private String seller;
    private  String seller_name;
    private String seller_phone;
    private String seller_email;
    private String seller_location;
    private  String date;
    private String search_param;
    private String read;
    private String physical_location;

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    //    whether the seller is verified
    @NonNull
    private String verified;
    @NonNull
    private String client_key;

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public    String getSearch_param() {
        return search_param;
    }

    public void setSearch_param(String search_param) {
        this.search_param = search_param;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Reply(String buyer, String reply_message, String search_param, String seller, String seller_name,
                 String seller_phone, String seller_email, String seller_location, String date,
                 String read) {
        this.buyer = buyer;
        this.reply_message = reply_message;
        this.seller = seller;
        this.seller_name = seller_name;
        this.seller_phone = seller_phone;
        this.seller_email = seller_email;
        this.seller_location = seller_location;
        this.date = date;
        this.search_param = search_param;
        this.read = read;
        this.client_key = "";
        this.verified = "no";
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:m:ss");
        String formattedDate = df.format(c);
        System.out.println(formattedDate);
        this.date = formattedDate;
    }

    public String getBuyer() {

        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getReply_message() {
        return reply_message;
    }

    public void setReply_message(String reply_message) {
        this.reply_message = reply_message;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getSeller_name() {
        return seller_name;
    }

    public void setSeller_name(String seller_name) {
        this.seller_name = seller_name;
    }

    public String getSeller_phone() {
        return seller_phone;
    }

    public void setSeller_phone(String seller_phone) {
        this.seller_phone = seller_phone;
    }

    public String getSeller_email() {
        return seller_email;
    }

    public void setSeller_email(String seller_email) {
        this.seller_email = seller_email;
    }

    public String getSeller_location() {
        return seller_location;
    }

    public void setSeller_location(String seller_location) {
        this.seller_location = seller_location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClient_key() {
        return client_key;
    }

    public void setClient_key(String client_key) {
        this.client_key = client_key;
    }

    public String getPhysical_location() {
        return physical_location;
    }

    public void setPhysical_location(String physical_location) {
        this.physical_location = physical_location;
    }
}
