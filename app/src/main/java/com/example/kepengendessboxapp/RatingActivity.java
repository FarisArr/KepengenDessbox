// RatingActivity.java berhasil
package com.example.kepengendessboxapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {

    private ImageView[] stars = new ImageView[5];
    private EditText edtReview;
    private Button btnSubmit;
    private int rating = 0;
    private String pesananId;
    private DocumentSnapshot pesananDoc;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        firestore = FirebaseFirestore.getInstance();
        pesananId = getIntent().getStringExtra("pesanan_id");

        if (pesananId == null) {
            Toast.makeText(this, "ID pesanan tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 Ambil data pesanan dari Firestore
        firestore.collection("pesanan").document(pesananId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        pesananDoc = doc;
                        initViews();
                        setupRatingBar();
                        setupSubmit();
                    } else {
                        Toast.makeText(this, "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat pesanan", Toast.LENGTH_SHORT).show();
                    finish();
                });

        initViews();
        setupRatingBar();
        setupSubmit();
    }

    private void initViews() {
        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);
        edtReview = findViewById(R.id.edtReview);
        btnSubmit = findViewById(R.id.btnSubmitRating);
    }

    private void setupRatingBar() {
        for (int i = 0; i < 5; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> {
                rating = index + 1;
                updateStars();
            });
        }
    }

    private void updateStars() {
        for (int i = 0; i < 5; i++) {
            stars[i].setImageResource(i < rating ? R.drawable.star_filled : R.drawable.star_outline);
        }
    }

    private void setupSubmit() {
        btnSubmit.setOnClickListener(v -> {
            if (rating == 0) {
                Toast.makeText(this, "Pilih rating terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String review = edtReview.getText().toString().trim();

            //  1. Simpan ke pesanan (untuk user)
            Map<String, Object> data = new HashMap<>();
            data.put("rated", true);
            data.put("rating", rating);
            data.put("review", review.isEmpty() ? "Tidak ada ulasan" : review);

            firestore.collection("pesanan")
                    .document(pesananId)
                    .update(data)
                    .addOnSuccessListener(aVoid -> {
                        //  2. Simpan ke ulasan_produk untuk setiap produk dalam pesanan
                        List<Map<String, Object>> pesananList = (List<Map<String, Object>>) pesananDoc.get("pesanan");
                        String namaUser = pesananDoc.getString("NamaLengkap"); // Ambil nama user

                        if (pesananList != null) {
                            for (Map<String, Object> item : pesananList) {
                                String namaProduk = (String) item.get("namaPesanan"); // Harus sama dengan getNama()

                                Map<String, Object> ulasan = new HashMap<>();
                                ulasan.put("namaProduk", namaProduk);
                                ulasan.put("rating", rating);
                                ulasan.put("review", review);
                                ulasan.put("namaUser", namaUser != null ? namaUser : "User");
                                ulasan.put("timestamp", System.currentTimeMillis());

                                // Simpan ke ulasan_produk
                                firestore.collection("ulasan_produk")
                                        .add(ulasan)
                                        .addOnSuccessListener(aVoid2 -> {
                                            // Bisa ditambahkan log jika perlu
                                            hitungDanUpdateRating(namaProduk);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle error (opsional)
                                        });
                            }
                        }

                        Toast.makeText(this, "Terima kasih atas ulasannya!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal kirim ulasan", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void hitungDanUpdateRating(String namaProduk) {
        firestore.collection("ulasan_produk")
                .whereEqualTo("namaProduk", namaProduk)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null) return;

                    double total = 0;
                    int count = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Long rating = doc.getLong("rating");
                        if (rating != null) {
                            total += rating;
                            count++;
                        }
                    }

                    if (count > 0) {
                        double avg = total / count;

//                        // Update ke semua kategori
//                        updateRatingDiKoleksi("promo_tambahan", namaProduk, avg, count);
//                        updateRatingDiKoleksi("dessert_tambahan", namaProduk, avg, count);
//                        updateRatingDiKoleksi("minuman_tambahan", namaProduk, avg, count);
                    }
                });
    }

//    private void updateRatingDiKoleksi(String koleksi, String namaProduk, double avg, int count) {
//        firestore.collection(koleksi)
//                .whereEqualTo("nama", namaProduk)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                        firestore.collection(koleksi)
//                                .document(doc.getId())
//                                .update("totalRating", avg, "jumlahUlasan", count);
//                    }
//                });
//    }
}