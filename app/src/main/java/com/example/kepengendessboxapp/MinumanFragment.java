// MinumanFragment.java berhasil
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

public class MinumanFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminProdukAdapter adapter;
    private ArrayList<AdminProdukAdapter.Produk> listMinuman;
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

        listMinuman = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        adapter = new AdminProdukAdapter(listMinuman,
                produk -> {
                    //  Edit produk
                    Intent intent = new Intent(getActivity(), EditProdukActivity.class);
                    intent.putExtra("EDIT_MODE", true);
                    intent.putExtra("NAMA", produk.getNama());
                    intent.putExtra("HARGA", produk.getHarga());
                    intent.putExtra("DESKRIPSI", produk.getDeskripsi());
                    intent.putExtra("KOLEKSI", "minuman");

                    if (produk instanceof Minuman) {
                        Minuman m = (Minuman) produk;
                        intent.putExtra("IMAGE_URL", m.getImageUrl());
                        intent.putExtra("DOCUMENT_ID", m.getDocumentId());
                        intent.putExtra("IS_BAWAAN", m.isBawaan());
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
                                String docId = "hapus_minuman_" + produk.getNama().replace(" ", "_");
                                firestore.collection("diubah")
                                        .document(docId)
                                        .set(new HashMap<String, Object>() {{
                                            put("nama", produk.getNama());
                                            put("kategori", "minuman");
                                            put("aksi", "hapus");
                                        }})
                                        .addOnSuccessListener(aVoid -> {
                                            listMinuman.remove(produk);
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

    private void loadUlasanForProduk(String namaProduk, Minuman minuman) {
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
                        minuman.setTotalRating(avg);
                        minuman.setJumlahUlasan(count);

                        if (!minuman.isBawaan() && minuman.getDocumentId() != null) {
                            firestore.collection("minuman_tambahan")
                                    .document(minuman.getDocumentId())
                                    .update("totalRating", avg, "jumlahUlasan", count);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MinumanFragment", "Gagal ambil ulasan", e);
                });
    }

    private void loadDataFromFirestore() {
        // 1. Reset & isi ulang dengan bawaan
        listMinuman.clear();
        ArrayList<Minuman> bawaan = ProdukBawaan.getMinumanBawaan();
        listMinuman.addAll(ProdukBawaan.getMinumanBawaan());

        //  Hitung rating untuk semua produk bawaan
        for (AdminProdukAdapter.Produk p : bawaan) {
            if (p instanceof Minuman) {
                loadUlasanForProduk(p.getNama(), (Minuman) p);
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
        tambahanListener = firestore.collection("minuman_tambahan")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) return;

                    listMinuman.clear();
                    listMinuman.addAll(ProdukBawaan.getMinumanBawaan());

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Minuman m = doc.toObject(Minuman.class);
                            if (m != null && !listMinuman.contains(m)) {
                                m.setDocumentId(doc.getId());
                                listMinuman.add(m);
                                // Hitung rating setelah produk ditambahkan
                                loadUlasanForProduk(m.getNama(), m);
                            }
                        }
                    }

                    // 4. Hentikan & pasang ulang listener "diubah"
                    if (diubahListener != null) {
                        diubahListener.remove();
                    }

                    diubahListener = firestore.collection("diubah")
                            .whereEqualTo("kategori", "minuman")
                            .addSnapshotListener((diubahSnapshot, error2) -> {
                                if (error2 != null || diubahSnapshot == null) return;

                                for (DocumentSnapshot doc : diubahSnapshot.getDocuments()) {
                                    String aksi = doc.getString("aksi");
                                    String nama = doc.getString("nama");

                                    if ("hapus".equals(aksi)) {
                                        listMinuman.removeIf(p -> p.getNama().equals(nama));
                                    } else if ("edit".equals(aksi)) {
                                        String namaBaru = doc.getString("namaBaru");
                                        Long hargaBaru = doc.getLong("harga");
                                        String deskripsiBaru = doc.getString("deskripsi");
                                        String imageUrlBaru = doc.getString("imageUrl");

                                        for (int i = 0; i < listMinuman.size(); i++) {
                                            AdminProdukAdapter.Produk p = listMinuman.get(i);
                                            if (p.getNama().equals(nama)) {
                                                // Cek apakah produk adalah Minuman
                                                if (p instanceof Minuman) {
                                                    Minuman old = (Minuman) p;
                                                    // Ganti objek lama dengan yang baru
                                                    listMinuman.set(i, new Minuman(
                                                            namaBaru,
                                                            hargaBaru != null ? hargaBaru.intValue() : p.getHarga(),
                                                            imageUrlBaru != null ? imageUrlBaru : old.getImageUrl(),
                                                            deskripsiBaru != null ? deskripsiBaru : p.getDeskripsi(),
                                                            old.getDocumentId()
                                                    ));
                                                } else {
                                                    // Fallback jika bukan Minuman (seharusnya tidak terjadi)
                                                    listMinuman.set(i, new Minuman(
                                                            namaBaru,
                                                            hargaBaru != null ? hargaBaru.intValue() : p.getHarga(),
                                                            "android.resource://com.example.kepengendessboxapp/" + R.drawable.placeholder,
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
        if (listMinuman != null && !listMinuman.isEmpty()) {
            for (AdminProdukAdapter.Produk p : listMinuman) {
                if (p instanceof Minuman) {
                    Minuman minuman = (Minuman) p;
                    loadUlasanForProduk(minuman.getNama(), minuman);
                }
            }
        }
    }
}