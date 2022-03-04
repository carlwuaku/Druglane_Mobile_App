package com.druglane;

import android.graphics.Bitmap;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ViewImage extends AppCompatActivity {
    public TextView title;
    public ImageView imageView;
    public ProgressBar progress;
    public ZoomageView zoomageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

//        title = findViewById(R.id.image_view_item);
        imageView = findViewById(R.id.imageView);
        progress = findViewById(R.id.image_progress);
        zoomageView  = findViewById(R.id.myZoomageView);

        Bundle extras = getIntent().getExtras();
        String message = extras.getString("message");

        getSupportActionBar().setTitle(message);

        String imageUrl = extras.getString("imageUrl");
//        title.setText(message);
        Picasso.get().load(imageUrl).into(zoomageView, new Callback() {
            @Override
            public void onSuccess() {
//                hide the progress bar
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                System.out.println("image error: "+e.getMessage());
                Toast.makeText(ViewImage.this, "Error. Invalid file attached",  Toast.LENGTH_SHORT);
                NavUtils.navigateUpFromSameTask(ViewImage.this);

            }
        });
    }

    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }
}
