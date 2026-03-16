// MenuActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private TextView txtNama;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private RecyclerView recPromo, recDessert, recMinuman;
    private PromoAdapter promoAdapter;
    private DessertAdapter dessertAdapter;
    private MinumanAdapter minumanAdapter;

    private ArrayList<Promo> listPromo, filteredPromo;
    private ArrayList<Dessert> listDessert, filteredDessert;
    private ArrayList<Minuman> listMinuman, filteredMinuman;

    private TextInputEditText searchEditText;

    // Tambahkan ListenerRegistration
    private ListenerRegistration promoTambahanListener, diubahPromoListener;
    private ListenerRegistration dessertTambahanListener, diubahDessertListener;
    private ListenerRegistration minumanTambahanListener, diubahMinumanListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_menu);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (user == null) {
            startActivity(new Intent(this, WelcomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        // Inisialisasi View
        txtNama = findViewById(R.id.txtNama);
        searchEditText = findViewById(R.id.searchEditText);
        recPromo = findViewById(R.id.rec_promo);
        recDessert = findViewById(R.id.rec_dessert);
        recMinuman = findViewById(R.id.rec_minuman);

        // Ambil Nama Lengkap dari Firestore
        loadUserDisplayName();

        //  Inisialisasi list
        listPromo = new ArrayList<>();
        listDessert = new ArrayList<>();
        listMinuman = new ArrayList<>();

        // Setup filtered list
        filteredPromo = new ArrayList<>();
        filteredDessert = new ArrayList<>();
        filteredMinuman = new ArrayList<>();

        // Setup Adapter
        setupAdapters();

        // Setup layout manager
        recPromo.setLayoutManager(new LinearLayoutManager(this));
        recDessert.setLayoutManager(new LinearLayoutManager(this));
        recMinuman.setLayoutManager(new LinearLayoutManager(this));

        // Handle pencarian
        setupSearch();

        // Setup FAB Navigation
        initFab();

        //  loadAll...()
        loadAllPromo();
        loadAllDessert();
        loadAllMinuman();
    }

    /**
     * Ambil Nama Lengkap dari Firestore
     */
    private void loadUserDisplayName() {
        String userId = user.getUid();
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String namaLengkap = documentSnapshot.getString("NamaLengkap");
                        if (namaLengkap != null && !namaLengkap.isEmpty()) {
                            txtNama.setText("Hi, " + namaLengkap);
                        } else {
                            String email = user.getEmail();
                            String namaDepan = email.split("@")[0];
                            txtNama.setText("Hi, " + namaDepan);
                        }
                    } else {
                        String email = user.getEmail();
                        String namaDepan = email.split("@")[0];
                        txtNama.setText("Hi, " + namaDepan);
                    }
                })
                .addOnFailureListener(e -> {
                    String email = user.getEmail();
                    String namaDepan = email.split("@")[0];
                    txtNama.setText("Hi, " + namaDepan);
                    Toast.makeText(this, "Gagal memuat nama", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupAdapters() {
        promoAdapter = new PromoAdapter(filteredPromo);
        dessertAdapter = new DessertAdapter(filteredDessert);
        minumanAdapter = new MinumanAdapter(filteredMinuman);

        recPromo.setAdapter(promoAdapter);
        recDessert.setAdapter(dessertAdapter);
        recMinuman.setAdapter(minumanAdapter);

        // Listener untuk klik item
        promoAdapter.setOnItemClickListener(promo -> openMenuDetail(
                promo.getNamaPromo(),
                String.valueOf(promo.getHargaPromo()),
                promo.getImageUrl(),
                promo.getDeskripsiPromo()
        ));

        dessertAdapter.setOnItemClickListener(dessert -> openMenuDetail(
                dessert.getNamaDessert(),
                String.valueOf(dessert.getHargaDessert()),
                dessert.getImageUrl(),
                dessert.getDeskripsiDessert()
        ));

        minumanAdapter.setOnItemClickListener(minuman -> openMenuDetail(
                minuman.getNamaMinuman(),
                String.valueOf(minuman.getHargaMinuman()),
                minuman.getImageUrl(),
                minuman.getDeskripsiMinuman()
        ));
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenu(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Biarkan fokus tetap aktif
            }
        });
    }

    private void filterMenu(String query) {
        if (query.isEmpty()) {
            filteredPromo.clear();
            filteredPromo.addAll(listPromo);

            filteredDessert.clear();
            filteredDessert.addAll(listDessert);

            filteredMinuman.clear();
            filteredMinuman.addAll(listMinuman);
        } else {
            String lowerCaseQuery = query.toLowerCase();

            filterList(listPromo, filteredPromo, lowerCaseQuery, Promo::getNamaPromo);
            filterList(listDessert, filteredDessert, lowerCaseQuery, Dessert::getNamaDessert);
            filterList(listMinuman, filteredMinuman, lowerCaseQuery, Minuman::getNamaMinuman);
        }

        promoAdapter.notifyDataSetChanged();
        dessertAdapter.notifyDataSetChanged();
        minumanAdapter.notifyDataSetChanged();
    }

    private <T> void filterList(ArrayList<T> source, ArrayList<T> target, String query, java.util.function.Function<T, String> getName) {
        target.clear();
        for (T item : source) {
            if (getName.apply(item).toLowerCase().contains(query)) {
                target.add(item);
            }
        }
    }

    private void loadAllPromo() {
        if (promoTambahanListener != null) promoTambahanListener.remove();
        if (diubahPromoListener != null) diubahPromoListener.remove();

        listPromo.clear();
        ArrayList<Promo> bawaan = ProdukBawaan.getPromoBawaan();
        listPromo.addAll(ProdukBawaan.getPromoBawaan());

        //  Hitung rating untuk produk bawaan
        for (Promo p : bawaan) {
            loadUlasanForProduk(p.getNama(), p);
        }

        promoTambahanListener = firestore.collection("promo_tambahan")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) return;

                    listPromo.clear();
                    listPromo.addAll(ProdukBawaan.getPromoBawaan());

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Promo p = doc.toObject(Promo.class);
                            if (p != null && !listPromo.contains(p)) {
                                p.setDocumentId(doc.getId());
                                listPromo.add(p);
                                loadUlasanForProduk(p.getNama(), p);
                            }
                        }
                    }

                    if (diubahPromoListener != null) diubahPromoListener.remove();

                    diubahPromoListener = firestore.collection("diubah")
                            .whereEqualTo("kategori", "promo")
                            .addSnapshotListener((diubahSnapshot, error2) -> {
                                if (error2 != null || diubahSnapshot == null) return;

                                for (DocumentSnapshot doc : diubahSnapshot.getDocuments()) {
                                    String aksi = doc.getString("aksi");
                                    String nama = doc.getString("nama");
                                    String namaBaru = doc.getString("namaBaru");
                                    Long hargaBaru = doc.getLong("harga");
                                    String deskripsiBaru = doc.getString("deskripsi");
                                    String imageUrlBaru = doc.getString("imageUrl"); //

                                    if ("hapus".equals(aksi)) {
                                        listPromo.removeIf(p -> p.getNama().equals(nama));
                                    } else if ("edit".equals(aksi)) {
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
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                // Hitung ulang rating setelah edit
                                for (AdminProdukAdapter.Produk p : listPromo) {
                                    if (p instanceof Promo) {
                                        loadUlasanForProduk(p.getNama(), (Promo) p);
                                    }
                                }
                                filteredPromo.clear();
                                filteredPromo.addAll(listPromo);
                                promoAdapter.notifyDataSetChanged();
                            });
                });
    }

    private void loadAllDessert() {
        if (dessertTambahanListener != null) dessertTambahanListener.remove();
        if (diubahDessertListener != null) diubahDessertListener.remove();

        listDessert.clear();
        ArrayList<Dessert> bawaan = ProdukBawaan.getDessertBawaan();
        listDessert.addAll(bawaan);

        //  Hitung rating untuk produk bawaan
        for (Dessert d : bawaan) {
            loadUlasanForProduk(d.getNama(), d);
        }

        dessertTambahanListener = firestore.collection("dessert_tambahan")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) return;

                    listDessert.clear();
                    listDessert.addAll(ProdukBawaan.getDessertBawaan());

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Dessert d = doc.toObject(Dessert.class);
                            if (d != null && !listDessert.contains(d)) {
                                d.setDocumentId(doc.getId());
                                listDessert.add(d);
                                // Hitung rating untuk produk tambahan
                                loadUlasanForProduk(d.getNama(), d);
                            }
                        }
                    }

                    if (diubahDessertListener != null) diubahDessertListener.remove();

                    diubahDessertListener = firestore.collection("diubah")
                            .whereEqualTo("kategori", "dessert")
                            .addSnapshotListener((diubahSnapshot, error2) -> {
                                if (error2 != null || diubahSnapshot == null) return;

                                for (DocumentSnapshot doc : diubahSnapshot.getDocuments()) {
                                    String aksi = doc.getString("aksi");
                                    String nama = doc.getString("nama");
                                    String namaBaru = doc.getString("namaBaru");
                                    Long hargaBaru = doc.getLong("harga");
                                    String deskripsiBaru = doc.getString("deskripsi");
                                    String imageUrlBaru = doc.getString("imageUrl");

                                    if ("hapus".equals(aksi)) {
                                        listDessert.removeIf(p -> p.getNama().equals(nama));
                                    } else if ("edit".equals(aksi)) {
                                        for (int i = 0; i < listDessert.size(); i++) {
                                            AdminProdukAdapter.Produk p = listDessert.get(i);
                                            if (p.getNama().equals(nama)) {
                                                if (p instanceof Dessert) {
                                                    Dessert old = (Dessert) p;
                                                    listDessert.set(i, new Dessert(
                                                            namaBaru,
                                                            hargaBaru != null ? hargaBaru.intValue() : p.getHarga(),
                                                            imageUrlBaru != null ? imageUrlBaru : old.getImageUrl(),
                                                            deskripsiBaru != null ? deskripsiBaru : p.getDeskripsi(),
                                                            old.getDocumentId()
                                                    ));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                // Hitung ulang rating setelah edit
                                for (AdminProdukAdapter.Produk d : listDessert) {
                                    if (d instanceof Dessert) {
                                        loadUlasanForProduk(d.getNama(), (Dessert) d);
                                    }
                                }
                                filteredDessert.clear();
                                filteredDessert.addAll(listDessert);
                                dessertAdapter.notifyDataSetChanged();
                            });
                });
    }

    private void loadAllMinuman() {
        if (minumanTambahanListener != null) minumanTambahanListener.remove();
        if (diubahMinumanListener != null) diubahMinumanListener.remove();

        listMinuman.clear();
        ArrayList<Minuman> bawaan = ProdukBawaan.getMinumanBawaan();
        listMinuman.addAll(bawaan);

        //  Hitung rating untuk produk bawaan
        for (Minuman m : bawaan) {
            loadUlasanForProduk(m.getNama(), m);
        }

        minumanTambahanListener = firestore.collection("minuman_tambahan")
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
                                //  Hitung rating untuk produk tambahan
                                loadUlasanForProduk(m.getNama(), m);
                            }
                        }
                    }

                    if (diubahMinumanListener != null) diubahMinumanListener.remove();

                    diubahMinumanListener = firestore.collection("diubah")
                            .whereEqualTo("kategori", "minuman")
                            .addSnapshotListener((diubahSnapshot, error2) -> {
                                if (error2 != null || diubahSnapshot == null) return;

                                for (DocumentSnapshot doc : diubahSnapshot.getDocuments()) {
                                    String aksi = doc.getString("aksi");
                                    String nama = doc.getString("nama");
                                    String namaBaru = doc.getString("namaBaru");
                                    Long hargaBaru = doc.getLong("harga");
                                    String deskripsiBaru = doc.getString("deskripsi");
                                    String imageUrlBaru = doc.getString("imageUrl");

                                    if ("hapus".equals(aksi)) {
                                        listMinuman.removeIf(p -> p.getNama().equals(nama));
                                    } else if ("edit".equals(aksi)) {
                                        for (int i = 0; i < listMinuman.size(); i++) {
                                            AdminProdukAdapter.Produk p = listMinuman.get(i);
                                            if (p.getNama().equals(nama)) {
                                                if (p instanceof Minuman) {
                                                    Minuman old = (Minuman) p;
                                                    listMinuman.set(i, new Minuman(
                                                            namaBaru,
                                                            hargaBaru != null ? hargaBaru.intValue() : p.getHarga(),
                                                            imageUrlBaru != null ? imageUrlBaru : old.getImageUrl(),
                                                            deskripsiBaru != null ? deskripsiBaru : p.getDeskripsi(),
                                                            old.getDocumentId()
                                                    ));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                //  Hitung ulang rating setelah edit
                                for (AdminProdukAdapter.Produk m : listMinuman) {
                                    if (m instanceof Minuman) {
                                        loadUlasanForProduk(m.getNama(), (Minuman) m);
                                    }
                                }
                                filteredMinuman.clear();
                                filteredMinuman.addAll(listMinuman);
                                minumanAdapter.notifyDataSetChanged();
                            });
                });
    }

    private void loadUlasanForProduk(String namaProduk, Promo promo) {
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
                        promo.setTotalRating(avg);
                        promo.setJumlahUlasan(count);
                        //  JANGAN simpan ke Firestore
                    }

                    // Refresh UI
                    filteredPromo.clear();
                    filteredPromo.addAll(listPromo);
                    promoAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
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
                        // JANGAN simpan ke Firestore
                    }

                    // Refresh UI
                    filteredDessert.clear();
                    filteredDessert.addAll(listDessert);
                    dessertAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
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
                        //  JANGAN simpan ke Firestore
                    }

                    // Refresh UI
                    filteredMinuman.clear();
                    filteredMinuman.addAll(listMinuman);
                    minumanAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }


    private void initFab() {
        findViewById(R.id.fabCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.fabRiwayat).setOnClickListener(v -> {
            startActivity(new Intent(this, RiwayatPesananActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.fabAbout).setOnClickListener(v -> {
            startActivity(new Intent(this, About.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // WhatsApp
        findViewById(R.id.fabWhatsapp).setOnClickListener(v -> {
            String message = "Hi, Selamat datang di Kepengen Dessbox";
            String url = "https://wa.me/+6285717959668?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.fabProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void openMenuDetail(String nama, String harga, String imageUrl, String deskripsi) {
        Intent intent = new Intent(this, MenuDetailActivity.class);
        intent.putExtra("NAMA", nama);
        intent.putExtra("HARGA", harga);
        intent.putExtra("IMAGE_URL", imageUrl);
        intent.putExtra("DESKRIPSI", deskripsi);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateTo(Class<?> targetClass) {
        Intent intent = new Intent(this, targetClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (targetClass == WelcomeActivity.class) finish();
    }

    // Format Rupiah
    public static String rp(int amount) {
        Locale localeID = new Locale("in", "ID");
        java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        return format.format(amount).replace("Rp", "").trim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  Muat ulang semua data produk
        loadAllPromo();
        loadAllDessert();
        loadAllMinuman();
        //  Refresh search jika ada query
        if (searchEditText != null) {
            String query = searchEditText.getText().toString();
            filterMenu(query);
        }

        //  MUAT ULANG NAMA USER SETELAH EDIT PROFIL
        loadUserDisplayName();
    }
}