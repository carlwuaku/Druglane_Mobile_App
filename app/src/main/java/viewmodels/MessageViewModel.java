package viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import database.Repository;
import models.Message;

/**
 * Created by Carl on 11/15/2018.
 */

public class MessageViewModel extends AndroidViewModel{
    private Repository mRepository;

    private LiveData<List<Message>> mAllWords;

    public MessageViewModel (Application application) {
        super(application);
        mRepository = new Repository(application);
        mAllWords = mRepository.getAllMessages();
    }

    public LiveData<List<Message>> getAllMessages() { return mAllWords; }

    public LiveData<List<Message>> getAllSearches() { return mRepository.getSearches(); }

    public void insert(Message word) { mRepository.insert(word); }

    public int getMessageCount(String param){
        return mRepository.countMessageReplies(param);
    }

    public void resetHasUnread(){
        mRepository.resetHasUnread();
    }

    public void resetUnread(String key){ mRepository.resetUnreadCount(key); }
    public void setBlocked(String key){mRepository.setMessageBlocked(key);}

    public void delete(Message item){
        mRepository.deleteMessage(item);
    }
}
