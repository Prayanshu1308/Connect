package com.hncc.connect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageViewForProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageViewForProfile = findViewById(R.id.image_viewer_image_view);
        Intent intent = getIntent();
        String profileImageUrl = intent.getExtras().get("imageUrl").toString();
        Picasso.get().load(profileImageUrl).into(imageViewForProfile);
    }
}