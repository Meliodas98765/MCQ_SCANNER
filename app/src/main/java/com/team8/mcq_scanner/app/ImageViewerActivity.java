package com.team8.mcq_scanner.app;

import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_IMGPATH;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_USERNAME;
import static com.team8.mcq_scanner.app.managers.Constants.IMG_DEFAULTS;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.team8.mcq_scanner.app.managers.Utills;

public class ImageViewerActivity extends AppCompatActivity {

    int placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.activity_image_fullscreen);
        final String imgPath = extras.getString(EXTRA_IMGPATH);
        final Uri imageUri = Uri.parse(imgPath);
        final String username = extras.getString(EXTRA_USERNAME, "");

        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final PhotoView imageViewZoom = findViewById(R.id.imgPath);
        final TextView txtMyName = findViewById(R.id.txtMyName);

        if (!Utills.isEmpty(username)) {
            if (Utills.isEmpty(username)) {
                txtMyName.setVisibility(View.GONE);
            } else {
                txtMyName.setVisibility(View.VISIBLE);
                txtMyName.setText(username);
            }
        }

        if (!Utills.isEmpty(username) && imgPath.equalsIgnoreCase(IMG_DEFAULTS)) {
            placeholder = R.drawable.ic_baseline_image_24;
        } else {
            placeholder = R.drawable.ic_baseline_image_24;
        }

        try {
            if (imgPath.equals(IMG_DEFAULTS)) {
                Glide.with(this).load(placeholder).into(imageViewZoom);
            } else {
                Glide.with(this).load(imageUri).into(imageViewZoom);
            }
        } catch (Exception ignored) {
        }

    }

}
