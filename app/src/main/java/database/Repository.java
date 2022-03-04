package database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.widget.Toast;

import com.druglane.Main2Activity;

import java.util.List;

import dao.MessageDao;
import dao.ReplyDao;
import models.Message;
import models.Reply;

/**
 * Created by Carl on 11/15/2018.
 */

public class Repository {

    private MessageDao messageDao;
    private LiveData<List<Message>> allMessages;

    private ReplyDao replyDao;
    private LiveData<List<Reply>> allReplies;

    public Repository(Application application) {
        RoomDatabase db = RoomDatabase.getDatabase(application);
        messageDao = db.messageDao();
        replyDao = db.replyDao();

        allMessages = messageDao.getMyMessages(Main2Activity._id);
        allReplies = replyDao.getAll();
    }

    public LiveData<List<Message>> getAllMessages(){
        return allMessages;
    }

    public LiveData<List<Message>> getSearches(){
        return messageDao.getOtherMessages(Main2Activity._id);
    }

    public void insert (Message message) {
        new insertAsyncTask(messageDao).execute(message);

    }

    public void update (Message message) {
        new updateAsyncTask(messageDao).execute(message);

    }

    public void resetUnreadCount(String key){
        new resetUnreadAsyncTask(messageDao).execute(key);
    }

    public void resetHasUnread(){
        new resetHasUnreadAsyncTask(messageDao).execute("");
    }

    public int countMessageReplies(String param){
        return replyDao.getReplyCount(param);
    }

    public void setMessageBlocked(String key){
        new setBlockedMessageAsyncTask(messageDao).execute(key);
    }

    public static class insertAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao mAsyncTaskDao;

        insertAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Message... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public static class updateAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao mAsyncTaskDao;

        updateAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Message... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }


    public LiveData<List<Reply>> getAllReplies(){
        return allReplies;
    }

    public LiveData<List<Reply>> getMessageReplies(String key){
        return replyDao.getMessageReplies(key);
    }

    public void insertReply (Reply reply) {
        new insertReplyAsyncTask(replyDao).execute(reply);
    }

    public void updateMessage(String param){
        new updateMessageAsyncTask(messageDao).execute(param);
    }

    public static class insertReplyAsyncTask extends AsyncTask<Reply, Void, Void> {

        public ReplyDao mAsyncTaskDao;

        public insertReplyAsyncTask(ReplyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        public Void doInBackground(final Reply... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public static class updateMessageAsyncTask extends AsyncTask<String, Void, Void> {

        public MessageDao mAsyncTaskDao;

        public updateMessageAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        public Void doInBackground(final String... params) {
            //param1 = param
            mAsyncTaskDao.incrementUnread(params[0]);
            long millis = System.currentTimeMillis();

//Divide millis by 1000 to get the number of seconds.
            long seconds = millis / 1000;
            mAsyncTaskDao.updateLastTime(params[0], String.valueOf(seconds));
            return null;
        }
    }

    public static class resetUnreadAsyncTask extends AsyncTask<String, Void, Void> {

        public MessageDao mAsyncTaskDao;

        public resetUnreadAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        public Void doInBackground(final String... params) {
            //param1 = param
            mAsyncTaskDao.resetUnread(params[0]);
            return null;
        }
    }

    public static class resetHasUnreadAsyncTask extends AsyncTask<String, Void, Void> {

        public MessageDao mAsyncTaskDao;

        public resetHasUnreadAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        public Void doInBackground(final String... params) {
            //param1 = param
            mAsyncTaskDao.resetHasUnread();
            return null;
        }
    }

    //delete one item
    private static class DeleteMessageAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao mAsyncTaskDao;

        DeleteMessageAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Message... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    public void deleteMessage(Message... items) {
        new DeleteMessageAsyncTask(messageDao).execute(items);
    }

    //delete all messages
    private static class DeleteAllMessagesAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao mAsyncTaskDao;

        DeleteAllMessagesAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Message... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    public void deleteAllMessages(Message... items) {
        new DeleteAllMessagesAsyncTask(messageDao).execute(items);
    }

    //delete all replies
    private static class DeleteAllRepliesAsyncTask extends AsyncTask<Reply, Void, Void> {

        private ReplyDao mAsyncTaskDao;

        DeleteAllRepliesAsyncTask(ReplyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Reply... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    public void deleteAllReplies(Reply... items) {
        new DeleteAllRepliesAsyncTask(replyDao).execute(items);
    }


    public static class setBlockedMessageAsyncTask extends AsyncTask<String, Void, Void> {

        public MessageDao mAsyncTaskDao;

        public setBlockedMessageAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        public Void doInBackground(final String... params) {
            //param1 = param
            mAsyncTaskDao.setBlocked(params[0]);
            return null;
        }
    }
}
