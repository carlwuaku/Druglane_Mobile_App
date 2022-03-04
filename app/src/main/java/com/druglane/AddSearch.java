package com.druglane;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import backgroundServices.AddSearchService;
import id.zelory.compressor.Compressor;
import viewmodels.MessageViewModel;

public class AddSearch extends AppCompatActivity {

    public EditText messagetxt ;
    public EditText quantity_field ;
    public Button send ;
    private MessageViewModel vm;
    private String _id;
    private String user_name;
    private String user_type;
    private String device_id;
    private String location;
    private String country;
    private String attachment = "";
    private TextView btnChoose;
    private TextView btnTakePic, cancelPic;
    private ImageView imageView;
    private String current_message = "";
    private Uri filePath;
    private String client_key = "";
    private String quantity = "0";
    private String location_filter = "my_location";
    String mCurrentPhotoPath;
    private String price_filter = "all";
//    private File actualImage = null;
//
    private Bitmap selectedBitmap = null;
    private Uri selectedUri = null;
//    the bytes we upload
    private byte[] mUploadBytes;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;


    private final int PICK_IMAGE_REQUEST = 71;
    static final int REQUEST_TAKE_PHOTO = 11;
    private static final int SERVICE_JOB_ID = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_search);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        _id = sharedPref.getString(getString(R.string.user_id_key), null);
        user_name = sharedPref.getString(getString(R.string.name_key), null);
        user_type = sharedPref.getString(getString(R.string.type_key), null);
        device_id = sharedPref.getString(getString(R.string.device_id_key), null);
        location = sharedPref.getString(getString(R.string.location_key), null);
        country = sharedPref.getString(getString(R.string.country_key), null);
//        verifyPermissions();

        vm = ViewModelProviders.of(this).get(MessageViewModel.class);
        btnChoose =  findViewById(R.id.btnChoose);
        btnTakePic =  findViewById(R.id.take_picture);
        imageView = (ImageView) findViewById(R.id.imgView);
        cancelPic = findViewById(R.id.cancelImage);
        cancelPic.setVisibility(View.GONE);
        messagetxt = (EditText) findViewById(R.id.message) ;
        quantity_field = (EditText) findViewById(R.id.quantity_field) ;
        send = (Button) findViewById(R.id.send);

        RadioButton my_location_btn = findViewById(R.id.my_location_radio);
        RadioButton my_country_btn = findViewById(R.id.my_country_radio);
        my_country_btn.setText(getString(R.string.my_country) +" ("+country+")");
        my_location_btn.setText(getString(R.string.my_location) +" ("+location+")");

        //check if somem data was sent
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String param = extras.getString("param");
            messagetxt.setText(param);
        }
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(AddSearch.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                    chooseImage();
                }
                else{

//            ask for permission
                    Log.d("permissinos", "read Permissions not granged");
                    ActivityCompat.requestPermissions(AddSearch.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            002);
                }

            }
        });
        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                launchCamera();&& Manifest.permission.WRITE_EXTERNAL_STORAGE == PackageManager.PERMISSION_GRANTED

                if (ContextCompat.checkSelfPermission(AddSearch.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(AddSearch.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED ) {
                    Log.d("permissinos", "Permissions  granged");
                    dispatchTakePictureIntent();
                }
                else{
//            ask for permission
                    Log.d("permissinos", "Permissions not granged");
                    ActivityCompat.requestPermissions(AddSearch.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            001);
                }
            }
        });

        cancelPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageDrawable(null);
                filePath = null;
                cancelPic.setVisibility(View.GONE);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

//emit the message
//                this will contain the string of the filePath uri. if null, leave it like that. else, update it
                send();

                //                if(selectedUri == null && selectedBitmap == null){
////                    this method contains the savetext method call. it skips the upload if the file is empty
//                    uploadImage();
//                }
//
//                else{
//                    new BackgroundImageResize(selectedBitmap).execute(selectedUri);
//
//                }



            }
        });
//        verifyPermissions();
    }

    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

    private void send(){
        current_message = messagetxt.getText().toString();
        if(!current_message.isEmpty()){
            String file_path = null;
            if(filePath != null){
                file_path = filePath.toString();
            }
            quantity = quantity_field.getText().toString();
            Intent mServiceIntent = new Intent();
            mServiceIntent.putExtra("message", messagetxt.getText().toString());
            mServiceIntent.putExtra("uri", file_path);
            mServiceIntent.putExtra("quantity", quantity);
//                    do the location filter
            RadioGroup arr_group = findViewById(R.id.location_filter);
            int selectedId = arr_group.getCheckedRadioButtonId();
//                    use the id to do the location thingy
            switch (selectedId){
                case R.id.my_location_radio:
                    location_filter = "my_location";
                    break;
                case R.id.my_country_radio:
                    location_filter = "my_country";
                    break;
                case R.id.all_sellers_radio:
                    location_filter = "sellers";

            }

            RadioGroup price_group = findViewById(R.id.price_filter);
            int selectedPrice = price_group.getCheckedRadioButtonId();
//                    use the id to do the location thingy
            switch (selectedPrice){
                case R.id.retail_prices_radio:
                    price_filter = "retail";
                    break;
                case R.id.wholesale_prices_radio:
                    price_filter = "wholesale";

            }
//                    RadioButton arr_selected =  findViewById(selectedId);
//                    location_filter = arr_selected.getText().toString();
            mServiceIntent.putExtra("location_filter", location_filter);
            mServiceIntent.putExtra("price_filter", price_filter);

            messagetxt.setText("");
            quantity_field.setText("");
            if(Main2Activity.socket.connected()) {
                Toast.makeText(AddSearch.this, "Sending message...", Toast.LENGTH_SHORT).show();
                AddSearchService.enqueueWork(AddSearch.this, AddSearchService.class, SERVICE_JOB_ID, mServiceIntent);

            }
            else{
                Toast.makeText(AddSearch.this, "Connection lost. Try again later", Toast.LENGTH_LONG).show();

            }

            imageView.setImageDrawable(null);
            filePath = null;
            cancelPic.setVisibility(View.GONE);

            //go back
            finish();
        }
        else{
            Toast.makeText(AddSearch.this, "Please type a product name or description", Toast.LENGTH_SHORT).show();

        }
    }


    private void chooseImage() {
//        verifyPermissions();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

//    private void saveText(){
//        long seconds = System.currentTimeMillis();
//        client_key = String.valueOf(seconds);
//        JSONObject data = new JSONObject();
//        try {
//            data.put("message", current_message);
//            data.put("sender_name", user_name);
//            data.put("filename", attachment);
//            data.put("user", _id);
//            data.put("device_id", device_id);
//            data.put("client_key", client_key);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        if(!current_message.isEmpty()){
//            Message m = new Message(current_message,Main2Activity._id,attachment,"");
//            m.setLast_reply_time(String.valueOf(seconds));
//            m.setClient_key(client_key);
//            vm.insert(m);
//            Toast.makeText(AddSearch.this, "Added successfully", Toast.LENGTH_LONG).show();
//
//            Main2Activity.socket.emit("new_message", data);
//            sendNotification();
//            messagetxt.setText("");
//            NavUtils.navigateUpFromSameTask(this);
//
//        }
//        else{
//             Toast.makeText(AddSearch.this, "Please type something", Toast.LENGTH_SHORT).show();
//
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        System.out.println("request code" + requestCode);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();

//            actualImage = new File(filePath.getPath());
//            compress();
            selectedUri = filePath;
            selectedBitmap = null;
//            File f  = null;
            System.out.println("filepath "+data.getDataString());
            try {
//                f = new Compressor(this).compressToFile(new File(data.getDataString()));
//                filePath = Uri.fromFile(f);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                selectedBitmap = bitmap;
                imageView.setImageBitmap(scaleToFitHeight(bitmap, 300));
                cancelPic.setVisibility(View.VISIBLE);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){

//            THIS IS MORE GENERIC
//            Bitmap b ;
//            b = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(b);
//            selectedBitmap = b;
//            selectedUri = null;

//FROM BEFORE. THIS WONT SAVE THE PIC TO THE GALLERY
//            filePath = data.getData();
            File f  = null;
            System.out.println("filepath "+mCurrentPhotoPath);

            try {
                f = new Compressor(this).compressToFile(new File(mCurrentPhotoPath));
                filePath = Uri.fromFile(f);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                selectedBitmap = bitmap;
                selectedUri =  null;
                imageView.setImageBitmap(scaleToFitHeight(bitmap, 450));
                cancelPic.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

//    public void launchCamera() {
////        verifyPermissions();
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//        }
//    }

//    private void verifyPermissions(){
//        System.out.println("checking permissions");
//        String[] permissions = {
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA
//        };
//
//        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED
//            && ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[2]) == PackageManager.PERMISSION_GRANTED){
////    btnTakePic.setVisibility(View.VISIBLE);
////            btnChoose.setVisibility(View.VISIBLE);
//        }
//        else{
//
//            ActivityCompat.requestPermissions(AddSearch.this, permissions, 1);
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 001: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchTakePictureIntent();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case 002: {
                // READ PERMISSION If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    chooseImage();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_menu, menu);
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


            case R.id.action_send_search:
                send();
        }

        return super.onOptionsItemSelected(item);
    }

//    private void uploadImage() {
//        current_message = messagetxt.getText().toString();
//        if(current_message.trim().isEmpty()){
//            Toast.makeText(AddSearch.this, "Please type something", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if(mUploadBytes != null)
//        {
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();
//
//            final StorageReference ref = storageReference.child("livechatimages/"+ UUID.randomUUID().toString());
//
////            UploadTask uploadTask = ref.putFile(filePath);
//            UploadTask uploadTask = ref.putBytes(mUploadBytes);
//            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//
//                    // Continue with the task to get the download URL
//                    return ref.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    progressDialog.dismiss();
//                    System.out.println("complete \n ");
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult();
//                        attachment = downloadUri.toString();
//                        System.out.println("saving text \n " + attachment);
//
//                        saveText();
//
////                        Toast.makeText(AddSearch.this, "Uploaded"+attachment, Toast.LENGTH_LONG).show();
//                    } else {
//                        // Handle failures
//                        // ...
//                    }
//                }
//            });
//
////            ref.putFile(filePath)
////                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                        @Override
////                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//////                            Uri url = taskSnapshot.get
////                            attachment = ref.getDownloadUrl().toString();
////
////                            progressDialog.dismiss();
////                            Toast.makeText(AddSearch.this, "Uploaded"+attachment, Toast.LENGTH_SHORT).show();
////                        }
////                    })
////                    .addOnFailureListener(new OnFailureListener() {
////                        @Override
////                        public void onFailure(@NonNull Exception e) {
////                            progressDialog.dismiss();
////                            Toast.makeText(AddSearch.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
////                        }
////                    })
////                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
////                        @Override
////                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
////                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
////                                    .getTotalByteCount());
////                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
////                        }
////                    });
//        }
//        else{
//            System.out.println("no file. uploading \n ");
//            saveText();
//        }
//
//    }
//
//    public void sendNotification(){
//
//        //do the network thingy
//        final VolleySingleton network = VolleySingleton.getInstance(this);
//        String base_url = network.base_url;
//        String uri = base_url+"send_notifications";
////"send_notifications?filename="+filename+"&message="+msg+"&sender_name="+this.user.name+"&user="+this.user._id
//
//        // Access the RequestQueue through your singleton class.
//        try{
//            StringRequest stringRequest = new StringRequest
//                    (Request.Method.POST,uri,  new Response.Listener<String>() {
//
//                        @Override
//                        public void onResponse(String response) {
//                            try {
//
//                                JSONObject jsonObject = new JSONObject(response);
//
//                                //get the status
//                                String status = jsonObject.getString("status");
//                                Log.d("send_notification", status);
//
////                                Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("user_data").getString("id"), Toast.LENGTH_LONG).show();
//                            } catch (JSONException e) {
//
//                                e.printStackTrace();
//                            }
////                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG).show();
//                        }
//
//
//                    }, new Response.ErrorListener() {
//
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            // TODO: Handle error
//                            Log.e("error", error.toString());
////                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
//                        }
//                    }){
//                @Override
//                protected Map<String, String> getParams() {
//
//                    // Creating Map String Params.
//                    Map<String, String> params = new HashMap<String, String>();
//
//                    // Adding All values to Params.
//                    params.put("filename", attachment);
//                    params.put("user", _id);
//                    params.put("message", current_message);
//                    params.put("sender_name", user_name);
//                    params.put("client_key", client_key);
//                    return params;
//                }
//
//
//            };
//
//            network.getRequestQueue().add(stringRequest);
//
//
//
//        }catch (Exception e){
//
//            e.printStackTrace();
////            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }

//    compress image
//    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]>{
//        Bitmap bgBitmap;
//
//        public BackgroundImageResize(Bitmap b){
//            if(b != null){
//                bgBitmap = b;
//            }
//        }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        Toast.makeText(AddSearch.this, "compressing image", Toast.LENGTH_SHORT).show();
//    }
//
//
//
//    @Override
//    protected byte[] doInBackground(Uri... uris) {
//        Log.d("image comppression", "do in background started");
//
////        if a file was sellected
//         if(selectedBitmap == null){
//             try{
//                 selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uris[0]);
//             }
//             catch (IOException e){
//                 e.printStackTrace();
//             }
//         }
//
//         byte[] bytes = null;
//         Log.d("compressing image", "size before "+  selectedBitmap.getByteCount());
//         bytes = getBytesFromBitmap(selectedBitmap, 80);
//        Log.d("compressing image", "size after "+  bytes.length/1024);
//        mUploadBytes = bytes;
//        Log.d("compressing image muplo", "size after "+  bytes.length/1024);
//
//
//        return bytes;
//
//        }
//
//    @Override
//    protected void onPostExecute(byte[] bytes) {
//        super.onPostExecute(bytes);
//
//        Toast.makeText(AddSearch.this, "done compressing image "+mUploadBytes.length/1024, Toast.LENGTH_SHORT).show();
////        uploadImage();
//    }
//}
//
//    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
//        return stream.toByteArray();
//    }
//
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "DL_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        getExternalStoragePublicDirectory stores the pic in the gallery
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {

            // Permission is  granted
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                    Toast.makeText(AddSearch.this, "Error opening camera", Toast.LENGTH_SHORT).show();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.druglane.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }



    }

//    public void compress(){
//
//
//
//
//        try {
//           File compressedImage = new Compressor(this)
//                    .setMaxWidth(640)
//                    .setMaxHeight(480)
//                    .setQuality(75)
//                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
//                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
//                    .compressToFile(actualImage);
//
//
//
//
//        } catch (Exception e) {
//            Log.e("compress error", e.getMessage());
//            e.printStackTrace();
//        }
//
//
//    }
}
