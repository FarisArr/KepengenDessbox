// PromoFragment.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
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

public class PromoFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminProdukAdapter adapter;
    private ArrayList<AdminProdukAdapter.Produk> listPromo;
    private FirebaseFirestore firestore;

    // Tambahkan di bagian atas fragment
    private ListenerRegistration tambahanListener;
    private ListenerRegistration diubahListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_produk, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listPromo = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        adapter = new AdminProdukAdapter(listPromo,
                produk -> {
                    //  Edit produk
                    Intent intent = new Intent(getActivity(), EditProdukActivity.class);
                    intent.putExtra("EDIT_MODE", true);
                    intent.putExtra("NAMA", produk.getNama());
                    intent.putExtra("HARGA", produk.getHarga());
                    intent.putExtra("DESKRIPSI", produk.getDeskripsi());
                    intent.putExtra("KOLEKSI", "promo");

                    if (produk instanceof Promo) {
                        Promo p = (Promo) produk;
                        intent.putExtra("IMAGE_URL", p.getImageUrl());
                        intent.putExtra("DOCUMENT_ID", p.getDocumentId());
                        intent.putExtra("IS_BAWAAN", p.isBawaan());
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
                                String docId = "hapus_promo_" + produk.getNama().replace(" ", "_");
                                firestore.collection("diubah")
                                        .document(docId)
                                        .set(new HashMap<String, Object>() {{
                                            put("nama", produk.getNama());
                                            put("kategori", "promo");
                                            put("aksi", "hapus");
                                        }})
                                        .addOnSuccessListener(aVoid -> {
                                            listPromo.remove(produk);
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

    // PromoFragment.java
    private void loadUlasanForProduk(String namaProduk, Promo promo) {
        firestore.collection("ulasan_produk")
                .whereEqualTo("namaProduk", namaProduk)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null) return;

                    double total = 0;
                    int count = 0;
                    for (DocumentSnapshot doc : querySnapshot) {
                        Long rating = doc.getLong("rating");
                        if (rating != null) {
                            total += rating;
                            count++;
                        }
                    }

                    if (count > 0) {
                        double avg = total / count;
                        promo.setTotalRating(avg);
                        promo.setJumlahUlasan(count);

                        // Simpan ke Firestore agar tidak hitung ulang
                        if (!promo.isBawaan() && promo.getDocumentId() != null) {
                            firestore.collection("promo_tambahan")
                                    .document(promo.getDocumentId())
                                    .update("totalRating", avg, "jumlahUlasan", count);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void loadDataFromFirestore() {
        // 1. Reset & isi ulang dengan bawaan
        listPromo.clear();
        ArrayList<Promo> bawaan = ProdukBawaan.getPromoBawaan();
        listPromo.addAll(bawaan);

        //  Hitung rating untuk semua produk bawaan
        for (AdminProdukAdapter.Produk p : bawaan) {
            if (p instanceof Promo) {
                loadUlasanForProduk(p.getNama(), (Promo) p);
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
        tambahanListener = firestore.collection("promo_tambahan")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) return;

                    // Reset ulang: bawaan + tambahan
                    listPromo.clear();
                    listPromo.addAll(ProdukBawaan.getPromoBawaan());

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Promo p = doc.toObject(Promo.class);
                            if (p != null) {
                                p.setDocumentId(doc.getId());
                                if (!listPromo.contains(p)) {
                                    listPromo.add(p);
                                    //  Hitung rating untuk produk tambahan
                                    loadUlasanForProduk(p.getNama(), p);
                                }
                            }
                        }
                    }

                    // 4. Hentikan & pasang ulang listener "diubah"
                    if (diubahListener != null) {
                        diubahListener.remove();
                    }

                    diubahListener = firestore.collection("diubah")
                            .whereEqualTo("kategori", "promo")
                            .addSnapshotListener((diubahSnapshot, error2) -> {
                                if (error2 != null || diubahSnapshot == null) return;

                                for (DocumentSnapshot doc : diubahSnapshot.getDocuments()) {
                                    String aksi = doc.getString("aksi");
                                    String nama = doc.getString("nama");

                                    if ("hapus".equals(aksi)) {
                                        listPromo.removeIf(p -> p.getNama().equals(nama));
                                    } else if ("edit".equals(aksi)) {
                                        String namaBaru = doc.getString("namaBaru");
                                        Long hargaBaru = doc.getLong("harga");
                                        String deskripsiBaru = doc.getString("deskripsi");
                                        String imageUrlBaru = doc.getString("imageUrl");

                                        for (int i = 0; i < listPromo.size(); i++) {
                                            AdminProdukAdapter.Produk p = listPromo.get(i);
                                            if (p.getNama().equals(nama)) {
                                                if (p instanceof Promo) {
                                                    Promo old = (Promo) p;
                                                    listPromo.set(i, new Promo(
                                                            namaBaru,
                                                            hargaBaru != null ? hargaBaru.intValue() : p.getHarga(),
                                                            imageUrlBaru != null ? imageUrlBaru : old.getImageUrl(),
                                                            deskripsiBaru != null ? deskripsiBaru : p.getDeskripsi(),
                                                            old.getDocumentId()
                                                    ));
                                                } else {
                                                    listPromo.set(i, new Promo(
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
        //  Refresh semua produk: bawaan & tambahan
        for (AdminProdukAdapter.Produk p : listPromo) {
            if (p instanceof Promo) {
                Promo promo = (Promo) p;
                loadUlasanForProduk(promo.getNama(), promo);
            }
        }
    }
}