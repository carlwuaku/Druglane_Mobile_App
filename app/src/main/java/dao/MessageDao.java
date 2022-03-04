package dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import models.Message;

/**
 * Created by Carl on 11/15/2018.
 */
@Dao
public interface MessageDao {
    @Insert
    void insert(Message message);

    @Insert
    void insert(List<Message> message);

    @Update
    void update(Message message);

    @Query("update message set status = 'sent', filename = :filename where client_key = :key")
    void updateSent(String key, String filename);

    @Query("DELETE FROM message")
    void deleteAll();

    @Query("DELETE FROM message where client_key in (:key)")
    void deleteByClientKey(String key);

    @Query("SELECT * from message ORDER BY last_reply_time DESC")
    LiveData<List<Message>> getAll();

    @Query("SELECT * from message where message like  :param ORDER BY last_reply_time DESC")
    LiveData<List<Message>> search(String param);

    @Query("SELECT * from message where user = :id ORDER BY last_reply_time DESC")
    LiveData<List<Message>> getMyMessages(String id);

    @Query("SELECT * from message where user != :id ORDER BY id DESC")
    LiveData<List<Message>> getOtherMessages(String id);

    @Query("select count(id) from message")
    int getCount();

    @Query("update message set last_reply = :param where message = :message")
    void update(String message, String param);

    @Query("update message set num_unread = num_unread + 1 where client_key = :key")
    void incrementUnread(String key);

    @Query("update message set blocked = 'yes'  where client_key = :key")
    void setBlocked(String key);

    @Query("update message set total_replies = total_replies + 1 where client_key = :key")
    void incrementTotalReplies(String key);

    @Query("update message set num_unread = 0 where client_key = :key")
    void resetUnread(String key);

    @Query("select sum(num_unread) from message")
    int getTotalUnread();

    @Query("select count(id) from message where has_unread = 'yes' and user != :id")
    int getTotalNewSearches(String id);

//    reset all new messages to read
    @Query("update message set has_unread = 'no' where user != :id")
    void resetNewSearches(String id);


    @Query("update message set last_reply_time = :time where client_key = :key")
    void updateLastTime(String key, String time);

    @Query("update message set has_unread = :unread where message = :message")
    void updateHasUnread(String message, String unread);

    @Delete
    void delete(Message item);

//    update all new searches to have unread set to no. no need to do individual messagess
    @Query("update message set has_unread = 'no'")
    void resetHasUnread();

    @Query("select * from message where id = :key or client_key = :key ORDER BY id ASC LIMIT 1")
    Message find(String key);

}
