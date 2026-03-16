// UlasanAdminActivity.java
package com.example.kepengendessboxapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class UlasanAdminActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UlasanAdapter adapter;
    private ArrayList<Ulasan> listUlasan;
    private FirebaseFirestore firestore;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ulasan_admin);

        recyclerView = findViewById(R.id.recyclerViewUlasan);
        txtEmpty = findViewById(R.id.txtEmpty);
        firestore = FirebaseFirestore.getInstance();
        listUlasan = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UlasanAdapter(listUlasan);
        recyclerView.setAdapter(adapter);

        loadUlasan();
    }

    private void loadUlasan() {
        firestore.collection("ulasan_produk")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) return;

                    listUlasan.clear();

                    if (querySnapshot.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    txtEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Ulasan ulasan = doc.toObject(Ulasan.class);
                        if (ulasan != null) {
                            //  Ambil data tambahan: totalRating & jumlahUlasan
                            loadProductStats(ulasan, () -> {
                                listUlasan.add(ulasan);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                });
    }

    //  Interface untuk callback
    interface OnStatsLoadedListener {
        void onLoaded();
    }

    //  Hitung total rating dan jumlah ulasan dari ulasan_produk
    private void loadProductStats(Ulasan ulasan, OnStatsLoadedListener listener) {
        String namaProduk = ulasan.getNamaProduk();

        firestore.collection("ulasan_produk")
                .whereEqualTo("namaProduk", namaProduk)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        ulasan.setTotalRating(0.0);
                        ulasan.setJumlahUlasan(0);
                        listener.onLoaded();
                        return;
                    }

                    double total = 0;
                    int count = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long rating = doc.getLong("rating");
                        if (rating != null) {
                            total += rating;
                            count++;
                        }
                    }

                    double avg = count > 0 ? total / count : 0.0;
                    ulasan.setTotalRating(avg);
                    ulasan.setJumlahUlasan(count);

                    listener.onLoaded();
                })
                .addOnFailureListener(e -> {
                    ulasan.setTotalRating(0.0);
                    ulasan.setJumlahUlasan(0);
                    listener.onLoaded();
                });
    }
}