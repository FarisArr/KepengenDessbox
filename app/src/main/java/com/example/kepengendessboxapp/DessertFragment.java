// DessertFragment.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;

public class DessertFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminProdukAdapter adapter;
    private ArrayList<AdminProdukAdapter.Produk> listDessert;
    private FirebaseFirestore firestore;

    // 🔹 Tambahkan ListenerRegistration
    private ListenerRegistration tambahanListener;
    private ListenerRegistration diubahListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_produk, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listDessert = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Inisialisasi adapter
        adapter = new AdminProdukAdapter(listDessert,
                produk -> {
                    //  Edit produk
                    Intent intent = new Intent(getActivity(), EditProdukActivity.class);
                    intent.putExtra("EDIT_MODE", true);
                    intent.putExtra("NAMA", produk.getNama());
                    intent.putExtra("HARGA", produk.getHarga());
                    intent.putExtra("DESKRIPSI", produk.getDeskripsi());
                    intent.putExtra("KOLEKSI", "dessert");

                    //  Cek instance dan ambil imageUrl
                    if (produk instanceof Dessert) {
                        Dessert d = (Dessert) produk;
                        intent.putExtra("IMAGE_URL", d.getImageUrl());
                        intent.putExtra("DOCUMENT_ID", d.getDocumentId());
                        intent.putExtra("IS_BAWAAN", d.isBawaan());
                    } else {
                        intent.putExtra("IMAGE_URL", (String) null);
                        intent.putExtra("DOCUMENT_ID", (String) null);
                    }

                    startActivity(intent);
                },
                produk -> {
                    //  Hapus produk
                    new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                            .setTitle("Hapus Produk")
                            .setMessage("Yakin ingin menghapus \"" + produk.getNama() + "\"?")
                            .setPositiveButton("Ya", (d, w) -> {
                                String docId = "hapus_dessert_" + produk.getNama().replace(" ", "_");
                                firestore.collection("diubah")
                                        .document(docId)
                                        .set(new HashMap<String, Object>() {{
                                            put("nama", produk.getNama());
                                            put("kategori", "dessert");
                                            put("aksi", "hapus");
                                        }})
                                        .addOnSuccessListener(aVoid -> {
                                            listDessert.remove(produk);
                                            adapter.notifyDataSetChanged();
                                            Toast.makeText(getActivity(), "Produk dihapus", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getActivity(), "Gagal mencatat penghapusan", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                });

        recyclerView.setAdapter(adapter);
        loadDataFromFirestore();

        return view;
    }

    private void loadUlasanForProduk(String namaProduk, Dessert dessert) {
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
                        dessert.setTotalRating(avg);
                        dessert.setJumlahUlasan(count);

                        //  Perbaiki: ganti dari "promo_tambahan" → "dessert_tambahan"
                        if (!dessert.isBawaan() && dessert.getDocumentId() != null) {
                            firestore.collection("dessert_tambahan")
                                    .document(dessert.getDocumentId())
                                    .update("totalRating", avg, "jumlahUlasan", count);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("DessertFragment", "Gagal ambil ulasan", e);
                });
    }

    private void loadDataFromFirestore() {
        // 1. Reset & isi ulang dengan bawaan
        listDessert.clear();
        ArrayList<Dessert> bawaan = ProdukBawaan.getDessertBawaan();
        listDessert.addAll(ProdukBawaan.getDessertBawaan());

        //  Hitung rating untuk semua produk bawaan
        for (AdminProdukAdapter.Produk p : bawaan) {
            if (p instanceof Dessert) {
                loadUlasanForProduk(p.getNama(), (Dessert) p);
            }
        }

        // 2. Hentikan listener lama jika ada
        if (tambahanListener != null) {
            tambahanListener.remove();
        }
        if (diubahListener != null) {
            diubahListener.remove();
        }

        // 3. Pasang listener produk tambahan
        tambahanListener = firestore.collection("dessert_tambahan")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) return;

                    // Reset ulang produk bawaan + tambahan
                    listDessert.clear();
                    listDessert.addAll(ProdukBawaan.getDessertBawaan());

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Dessert d = doc.toObject(Dessert.class);
                            if (d != null && !listDessert.contains(d)) {
                                d.setDocumentId(doc.getId());
                                listDessert.add(d);
                                // Hitung rating setelah produk ditambahkan
                                loadUlasanForProduk(d.getNama(), d);
                            }
                        }
                    }

                    // 4. Hentikan & pasang ulang listener "diubah"
                    if (diubahListener != null) {
                        diubahListener.remove();
                    }

                    diubahListener = firestore.collection("diubah")
                            .whereEqualTo("kategori", "dessert")
                            .addSnapshotListener((diubahSnapshot, error2) -> {
                                if (error2 != null || diubahSnapshot == null) return;

                                for (DocumentSnapshot doc : diubahSnapshot.getDocuments()) {
                                    String aksi = doc.getString("aksi");
                                    String nama = doc.getString("nama");

                                    if ("hapus".equals(aksi)) {
                                        listDessert.removeIf(p -> p.getNama().equals(nama));
                                    } else if ("edit".equals(aksi)) {
                                        String namaBaru = doc.getString("namaBaru");
                                        Long hargaBaru = doc.getLong("harga");
                                        String deskripsiBaru = doc.getString("deskripsi");
                                        String imageUrlBaru = doc.getString("imageUrl");

                                        for (int i = 0; i < listDessert.size(); i++) {
                                            AdminProdukAdapter.Produk p = listDessert.get(i);
                                            if (p.getNama().equals(nama)) {
                                                // Cek apakah p adalah Dessert
                                                if (p instanceof Dessert) {
                                                    Dessert old = (Dessert) p;
                                                    // Ganti objek lama dengan yang baru
                                                    listDessert.set(i, new Dessert(
                                                            namaBaru,
                                                            hargaBaru != null ? hargaBaru.intValue() : p.getHarga(),
                                                            imageUrlBaru != null ? imageUrlBaru : old.getImageUrl(),
                                                            deskripsiBaru != null ? deskripsiBaru : p.getDeskripsi(),
                                                            old.getDocumentId() // Pertahankan documentId
                                                    ));
                                                } else {
                                                    // Jika bukan Dessert (seharusnya tidak terjadi)
                                                    listDessert.set(i, new Dessert(
                                                            namaBaru,
                                                            hargaBaru != null ? hargaBaru.intValue() : p.getHarga(),
                                                            "android.resource://com.example.kepengendessboxapp/" + R.drawable.placeholder, // fallback
                                                            deskripsiBaru != null ? deskripsiBaru : p.getDeskripsi()
                                                    ));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            });
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listDessert != null && !listDessert.isEmpty()) {
            for (AdminProdukAdapter.Produk p : listDessert) {
                if (p instanceof Dessert) {
                    Dessert dessert = (Dessert) p;
                    loadUlasanForProduk(dessert.getNama(), dessert);
                }
            }
        }
    }
}