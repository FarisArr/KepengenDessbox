// ProfileActivity.java Berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private TextView txtNamaLengkap, txtEmail, txtNoWa;
    private Button btnEditProfile, btnLogout;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi View
        txtNamaLengkap = findViewById(R.id.txtNamaLengkap);
        txtEmail = findViewById(R.id.txtEmail);
        txtNoWa = findViewById(R.id.txtNoWa);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load data profil
        loadProfileData();

        // Edit Profil
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("namaLengkap", txtNamaLengkap.getText().toString());
            intent.putExtra("noWa", txtNoWa.getText().toString());
            startActivity(intent);
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, WelcomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    private void loadProfileData() {
        if (user == null) return;

        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) {
                            txtNamaLengkap.setText(user.getNamaLengkap());
                            txtEmail.setText(user.getEmail());
                            txtNoWa.setText(user.getNoWhatsapp());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal muat profil", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData(); // Refresh data jika di-edit
    }
}