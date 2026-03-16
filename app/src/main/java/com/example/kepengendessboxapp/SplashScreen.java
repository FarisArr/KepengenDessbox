// SplashScreen.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();

        // Durasi splash screen
        int SPLASH_TIME = 1500;

        new Handler().postDelayed(() -> {
            FirebaseUser user = mAuth.getCurrentUser();

            Intent intent;
            if (user != null) {
                String email = user.getEmail();

                // Ganti dengan email admin Anda
                if ("kepengendessbox@gmail.com".equals(email)) {
                    intent = new Intent(SplashScreen.this, AdminActivity.class);
                } else {
                    intent = new Intent(SplashScreen.this, MenuActivity.class);
                }
            } else {
                intent = new Intent(SplashScreen.this, WelcomeActivity.class);
            }

            startActivity(intent);
            finish(); // Tutup splash agar tidak bisa kembali
        }, SPLASH_TIME);
    }
}