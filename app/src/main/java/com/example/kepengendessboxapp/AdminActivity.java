//AdminActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class AdminActivity extends AppCompatActivity {

    // Deklarasi Button
    private Button btnKelolaMenu, btnRiwayatPesanan, btnLihatUlasan, btnLaporanPenjualan, btnLogoutAdmin;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Menggunakan layout yang sudah Anda buat

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi View
        initView();

        // Setup onClickListener untuk semua tombol
        setupClickListeners();

        //  Subscribe ke topik admin
        FirebaseMessaging.getInstance().subscribeToTopic("admin")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Berlangganan notifikasi admin", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal berlangganan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initView() {
        btnKelolaMenu = findViewById(R.id.btnKelolaMenu);
        btnRiwayatPesanan = findViewById(R.id.btnRiwayatPesanan);
        btnLihatUlasan = findViewById(R.id.btnLihatUlasan);
        btnLaporanPenjualan = findViewById(R.id.btnLaporanPenjualan);
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin);
    }

    private void setupClickListeners() {
        //  Kelola Menu
        btnKelolaMenu.setOnClickListener(v -> navigateTo(KelolaMenuActivity.class));

        //  Riwayat Pesanan
        btnRiwayatPesanan.setOnClickListener(v -> {
            Intent intent = new Intent(this, RiwayatPesananActivity.class);
            intent.putExtra("isAdmin", true); // Beri tahu ini admin
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        //  Lihat Ulasan
        btnLihatUlasan.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, UlasanAdminActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        //  Laporan Penjualan
        btnLaporanPenjualan.setOnClickListener(v -> navigateTo(LaporanPenjualanActivity.class));

        //  Logout Admin
        btnLogoutAdmin.setOnClickListener(v -> logoutAdmin());
    }

    //  Navigasi dengan animasi transisi
    private void navigateTo(Class<?> targetClass) {
        Intent intent = new Intent(AdminActivity.this, targetClass);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    //  Logout Admin
    private void logoutAdmin() {
        mAuth.signOut();
        Toast.makeText(this, "Berhasil logout sebagai admin", Toast.LENGTH_SHORT).show();

        // Kembali ke Welcome Activity
        Intent intent = new Intent(AdminActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}