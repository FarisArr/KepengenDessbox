// RiwayatPesananActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RiwayatPesananActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private RiwayatAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_pesanan);

        // Inisialisasi Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // Hilangkan teks "Riwayat"

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User tidak login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cek apakah user adalah admin
        isAdmin = user.getEmail().equals("kepengendessbox@gmail.com");

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recycler_view_riwayat_pesanan);
        if (recyclerView == null) {
            Toast.makeText(this, "Error: RecyclerView tidak ditemukan di layout", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Muat data riwayat
        if (isAdmin) {
            loadAllOrders(); // Admin: lihat semua pesanan
        } else {
            loadUserOrders(user.getEmail()); // User: lihat pesanan sendiri
        }
    }

    /**
     * Muat semua pesanan dari semua user (untuk admin)
     */
    private void loadAllOrders() {
        firestore.collection("pesanan")
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Riwayat", "Gagal memuat data", error);
                        Toast.makeText(this, "Gagal memuat: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        processQuerySnapshot(value, "Semua User");
                    }
                });
    }

    /**
     * Muat pesanan berdasarkan email user
     */
    private void loadUserOrders(String email) {
        firestore.collection("pesanan")
                .whereEqualTo("Email", email)
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Riwayat", "Gagal memuat data", error);
                        Toast.makeText(this, "Gagal memuat: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        processQuerySnapshot(value, email);
                    }
                });
    }

    /**
     * Proses data pesanan dan kelompokkan per bulan
     */
    private void processQuerySnapshot(QuerySnapshot value, String userEmail) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        java.util.Map<String, List<QueryDocumentSnapshot>> groupedByMonth = new java.util.LinkedHashMap<>();

        for (QueryDocumentSnapshot doc : value) {
            String tanggal = doc.getString("tanggal");
            if (tanggal == null) continue;

            String bulan = extractBulanIndonesia(tanggal);
            groupedByMonth.putIfAbsent(bulan, new ArrayList<>());
            groupedByMonth.get(bulan).add(doc);
        }

        // Proses bulan dari terbaru ke terlama
        for (Map.Entry<String, List<QueryDocumentSnapshot>> entry : groupedByMonth.entrySet()) {
            String bulan = entry.getKey();
            List<QueryDocumentSnapshot> docs = entry.getValue();

            // Header Bulan
            Map<String, Object> bulanItem = new HashMap<>();
            bulanItem.put("type", "bulan");
            bulanItem.put("bulan", bulan);
            bulanItem.put("jumlahItem", docs.size());
            dataList.add(bulanItem);

            // Item Pesanan
            for (QueryDocumentSnapshot doc : docs) {
                List<Map<String, Object>> pesananList = (List<Map<String, Object>>) doc.get("pesanan");
                if (pesananList != null) {
                    int totalItem = 0;
                    for (Map<String, Object> item : pesananList) {
                        totalItem += getInt(item.get("jumlah"));
                    }

                    Map<String, Object> pesananItem = new HashMap<>();
                    pesananItem.put("type", "pesanan");
                    pesananItem.put("namaPesanan", totalItem + " Item");
                    pesananItem.put("totalPesanan", doc.getLong("totalHarga"));
                    pesananItem.put("email", doc.getString("Email"));
                    pesananItem.put("tanggal", formatTanggalIndonesia(doc.getString("tanggal")));
                    pesananItem.put("pesanan_id", doc.getId());
                    dataList.add(pesananItem);
                }
            }
        }

        if (dataList.isEmpty()) {
            Toast.makeText(this, isAdmin ? "Belum ada pesanan dari user" : "Tidak ada riwayat pesanan", Toast.LENGTH_SHORT).show();
        }

        if (adapter == null) {
            adapter = new RiwayatAdapter(dataList, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(dataList);
        }
    }

    private int getInt(Object value) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        return 0;
    }

    //  Format tanggal ke Indonesia: "14 Agustus 2025, 13:23 Siang"
    private String formatTanggalIndonesia(String isoDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = input.parse(isoDate);

            // Format tanggal
            SimpleDateFormat output = new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID"));
            String tanggal = output.format(date);

            // Format jam
            int hour = date.getHours();
            String period;
            if (hour >= 6 && hour < 12) {
                period = "Pagi";
            } else if (hour >= 12 && hour < 15) {
                period = "Siang";
            } else if (hour >= 15 && hour < 18) {
                period = "Sore";
            } else if (hour >= 18 && hour < 24) {
                period = "Malam";
            } else {
                period = "Dini Hari";
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            String waktu = timeFormat.format(date);

            return tanggal + ", " + waktu + " " + period;
        } catch (Exception e) {
            Log.e("Riwayat", "Gagal format tanggal: " + isoDate, e);
            return "Tanggal tidak valid";
        }
    }

    //  Ekstrak bulan dari tanggal ISO
    private String extractBulanIndonesia(String isoDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat output = new SimpleDateFormat("MMMM yyyy", new Locale("in", "ID"));
            Date date = input.parse(isoDate);
            return output.format(date);
        } catch (Exception e) {
            Log.e("Riwayat", "Gagal ekstrak bulan: " + isoDate, e);
            return "Unknown";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        boolean fromAdmin = getIntent().getBooleanExtra("isAdmin", false);

        if (fromAdmin) {
            // Kembali ke AdminActivity
            Intent intent = new Intent(this, AdminActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            // Kembali ke MenuActivity (untuk user)
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish(); // Tutup RiwayatPesananActivity
    }
}