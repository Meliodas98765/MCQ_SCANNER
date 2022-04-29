package com.team8.mcq_scanner.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team8.mcq_scanner.app.managers.Utills;

public class OnBoarding extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private String currentUser;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utills.setWindow(getWindow());
        setContentView(R.layout.activity_on_boarding);
        String[] permission =
                {
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_SETTINGS
                };
        for (String permissions :
                permission) {
            if (ContextCompat.checkSelfPermission(this, permissions)
                    == PackageManager.PERMISSION_DENIED) {
                try {
                    ActivityCompat.requestPermissions(this, new String[] {permissions}, 3452);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseUser != null){
                    Intent intent = new Intent(OnBoarding.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(OnBoarding.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        }, 2000);

    }
}