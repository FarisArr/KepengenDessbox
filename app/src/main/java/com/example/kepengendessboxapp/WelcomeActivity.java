// WelcomeActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    //  Email admin
    private static final String ADMIN_EMAIL = "kepengendessbox@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();

        // Setup listener untuk cek login
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                navigateBasedOnRole(user);
            }
        };
    }

    private void navigateBasedOnRole(FirebaseUser user) {
        String email = user.getEmail();

        Intent intent;
        if (email != null && ADMIN_EMAIL.equals(email)) {
            intent = new Intent(WelcomeActivity.this, AdminActivity.class);
        } else {
            intent = new Intent(WelcomeActivity.this, MenuActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Tutup WelcomeActivity
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    public void toLoginWel(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void toRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}