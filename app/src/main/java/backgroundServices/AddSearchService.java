package backgroundServices;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.druglane.Main2Activity;
import com.druglane.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dao.MessageDao;
import database.Repository;
import database.RoomDatabase;
import id.zelory.compressor.Compressor;
import models.Message;
import network.VolleySingleton;

public class AddSearchService extends JobIntentService {

    private String current_message = "";
    private String filename = "";
    private Uri filePath;
    private String client_key = "";
    String mCurrentPhotoPath;
    //    private File actualImage = null;
//
    private Bitmap selectedBitmap = null;
    private Uri selectedUri = null;
    //    the bytes we upload
    private byte[] mUploadBytes = null;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
//a reference to the current message being worked on
    private Message currentSavedMessage;
    private Repository mRepository;
    private String quantity;
    private String location_filter;
    private String price_filter;


    private String _id;
    private String user_name;
    private String user_type;
    private String device_id;
    private String location;
    private String country;

    private MessageDao messageDao;
    File  compressedImageFile;

    public AddSearchService() {
        super();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        RoomDatabase db = RoomDatabase.getDatabase(this);
        messageDao = db.messageDao();
//        receive messages and insert them into db with status: pending;
//        if there is an image, upload it, and update the message with the filename.
//        then emit it and send the notification

        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        _id = sharedPref.getString(getString(R.string.user_id_key), null);
        user_name = sharedPref.getString(getString(R.string.name_key), null);
        user_type = sharedPref.getString(getString(R.string.type_key), null);
        device_id = sharedPref.getString(getString(R.string.device_id_key), null);
        location = sharedPref.getString(getString(R.string.location_key), null);
        country = sharedPref.getString(getString(R.string.country_key), null);
//1. save to db with status pending
        Bundle extras = intent.getExtras();
        current_message = extras.getString("message");
        quantity = extras.getString("quantity");
        location_filter = extras.getString("location_filter");
        price_filter = extras.getString("price_filter");

        if(extras.getString("uri") != null){
            selectedUri = Uri.parse(extras.getString("uri"));
        }

        Log.d("service save text", "saving "+current_message);
        saveText(current_message);
//        2. if there is no attachment, emit message and send notification, then update status.
//        else upload the
        if(selectedUri == null){
            if(Main2Activity.isInternetAvailable()){
                emit();
//                new BackgroundImageResize().execute(selectedUri);
            }


        }

        else{
//            this calls the upload method when compression is done.
//            the upload method calls emit when upload is done and updates the object.filename
//            if no internet, just save the filename and leave


            if(Main2Activity.isInternetAvailable()){
                uploadImage();
//                new BackgroundImageResize().execute(selectedUri);
            }
            else{

                currentSavedMessage.setFile_uri(selectedUri.toString());
                
            }


        }


    }



    public void emit(){
        JSONObject data = new JSONObject();
        try {
            data.put("message", current_message);
            data.put("sender_name", user_name);
            data.put("filename", filename);
            data.put("user", _id);
            data.put("device_id", device_id);
            data.put("client_key", client_key);
            data.put("location_filter", location_filter);
            data.put("quantity", quantity);
            data.put("country", country);
            data.put("location", location);
            data.put("price_filter", price_filter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        check the connection
        if(Main2Activity.socket.connected()){
            Main2Activity.socket.emit("new_message", data);
            currentSavedMessage.setStatus("sent");
            sendNotification();
        }


//        messageDao.update(currentSavedMessage);
        new updateAsyncTask(messageDao).execute(currentSavedMessage.getClient_key(), filename);

    }


    private void uploadImage() {
            try{
                final StorageReference ref = storageReference.child("livechatimages/"+ UUID.randomUUID().toString());

                UploadTask uploadTask =  ref.putFile(selectedUri);
//            UploadTask uploadTask = ref.putBytes(mUploadBytes);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        System.out.println("complete \n ");
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            filename = downloadUri.toString();
                            currentSavedMessage.setFilename(filename);
                            System.out.println("saving text \n " + filename);
                            emit();

//                        Toast.makeText(AddSearch.this, "Uploaded"+attachment, Toast.LENGTH_LONG).show();
                        } else {
                            // Handle failures
                            // ...dont upload it. let them know
                            currentSavedMessage.setFilename(filename);
                            System.out.println("saving text \n " + filename);
                            emit();
                        }
                    }
                });
            }
            catch (Exception exc){
                exc.printStackTrace();
            }




//            ref.putFile(filePath)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                            Uri url = taskSnapshot.get
//                            attachment = ref.getDownloadUrl().toString();
//
//                            progressDialog.dismiss();
//                            Toast.makeText(AddSearch.this, "Uploaded"+attachment, Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
//                            Toast.makeText(AddSearch.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
//                                    .getTotalByteCount());
//                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
//                        }
//                    });


    }

    public void sendNotification(){

        //do the network thingy
        final VolleySingleton network = VolleySingleton.getInstance(this);
        String base_url = network.base_url;
        String uri = base_url+"send_notifications";
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
                                Log.d("send_notifi service", status);

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
                    params.put("filename", filename);
                    params.put("user", _id);
                    params.put("message", current_message);
                    params.put("sender_name", user_name);
                    params.put("client_key", client_key);
                    params.put("location_filter", location_filter);
                    params.put("quantity", quantity);
                    params.put("country", country);
                    params.put("location", location);
                    params.put("price_filter", price_filter);
                    params.put("device_id", device_id);
                    return params;
                }


            };

            network.getRequestQueue().add(stringRequest);



        }catch (Exception e){

            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //    compress image
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        public BackgroundImageResize(){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }



        @Override
        protected byte[] doInBackground(Uri... uris) {
            Log.d("image comppression", "do in background started");

//        if a file was sellected

                try{
                    selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uris[0]);
                }
                catch (IOException e){
                    e.printStackTrace();
                }

    /**
    TODO:     only compress if the file is more than 100kb
     */
            byte[] bytes = null;
            Log.d("compressing image", "size before "+  selectedBitmap.getByteCount());
            bytes = getBytesFromBitmap(selectedBitmap, 50);
            Log.d("compressing image", "size after "+  bytes.length/1024);
            mUploadBytes = bytes;
            Log.d("compressing image muplo", "size after "+  bytes.length/1024);


            return bytes;

        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);

            Log.d( "service compress","done compressing image "+mUploadBytes.length/1024);
            uploadImage();
        }
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }


    private void saveText(String message){
        current_message = message;
//        save it with file name of empty. if there is a file, it will be updated once upload finishes
        long seconds = System.currentTimeMillis();
        client_key = String.valueOf(seconds);


            Message m = new Message(current_message,_id,"","");
            m.setLast_reply_time(String.valueOf(seconds));
            m.setClient_key(client_key);
            m.setQuantity(quantity);
            m.setPrice_filter(price_filter);
            messageDao.insert(m);
//            m.setId(Integer.parseInt(String.valueOf(id)));
//            set the current message
            currentSavedMessage  = m;
    }

    public static class updateAsyncTask extends AsyncTask<String, Void, Void> {

        private MessageDao mAsyncTaskDao;

        updateAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        //sen
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.updateSent(params[0], params[1]);
            return null;
        }
    }


}
