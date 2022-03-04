package viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import database.Repository;
import models.Message;
import models.Reply;

/**
 * Created by Carl on 11/15/2018.
 */

public class ReplyViewModel extends AndroidViewModel{
    private Repository mRepository;

    private LiveData<List<Reply>> mAllWords;

    public ReplyViewModel (Application application) {
        super(application);
        mRepository = new Repository(application);
        mAllWords = mRepository.getAllReplies();
    }

    public LiveData<List<Reply>> getAllReplies() { return mAllWords; }

    public LiveData<List<Reply>> getMessageReplies(String key) { return mRepository.getMessageReplies(key);}

    public void insert(Reply word) { mRepository.insertReply(word); }


}
