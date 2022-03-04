package dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import models.Message;
import models.Reply;

/**
 * Created by Carl on 11/15/2018.
 */

@Dao
public interface ReplyDao {
    @Insert
    void insert(Reply reply);

    @Insert
    void insert(List<Reply> reply);

    @Query("DELETE FROM reply")
    void deleteAll();

    @Query("SELECT * from reply ORDER BY id DESC")
    LiveData<List<Reply>> getAll();

    @Query("SELECT * from reply where reply_message like  :param ")
    LiveData<List<Reply>> search(String param);

    @Query("select count(id) from reply")
    int getCount();

    @Query("select count(id) from reply where search_param like :param")
    int getReplyCount(String param);

    @Query("SELECT * from reply where client_key =  :key ")
    LiveData<List<Reply>> getMessageReplies(String key);

    @Query("update reply set reply_message = :new_reply where client_key = :client_key and seller = :seller ")
    void updateReply(String client_key, String seller, String new_reply);
}
