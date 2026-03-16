// MenuDetailActivity.java berhasil
package com.example.kepengendessboxapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.text.NumberFormat;

public class MenuDetailActivity extends AppCompatActivity {
    private TextView txtNamaMenuDetail, txtHargaMenuDetail, txtDeskripsiMenuDetail;
    private EditText edtJumlah;
    private Button btnTambah;
    private ImageButton btnBack; // Tombol back
    private FirebaseUser user;
    private String userId;
    private ArrayList<CartItem> cartItemList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_detail);

        // Inisialisasi View
        ImageView imgMenuDetail = findViewById(R.id.imgMenuDetail);
        txtNamaMenuDetail = findViewById(R.id.txtNamaMenuDetail);
        txtDeskripsiMenuDetail = findViewById(R.id.txtDeskripsiMenuDetail);
        txtHargaMenuDetail = findViewById(R.id.txtHargaMenuDetail);
        edtJumlah = findViewById(R.id.edt_jumlah);
        btnTambah = findViewById(R.id.btnTambah);
        btnBack = findViewById(R.id.btnBack); // Ambil tombol back

        // Inisialisasi Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User tidak login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = user.getUid();

        // Inisialisasi cart & progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menambahkan ke keranjang...");
        progressDialog.setCancelable(false);

        loadCartItems();

        // Ambil data dari Intent
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("IMAGE_URL");
        String namaMakanan = intent.getStringExtra("NAMA");
        String deskripsiMakanan = intent.getStringExtra("DESKRIPSI");
        String hargaMakanan = intent.getStringExtra("HARGA");

        if (imageUrl == null || namaMakanan == null || hargaMakanan == null) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tampilkan data
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(imgMenuDetail);

        txtNamaMenuDetail.setText(namaMakanan);
        txtDeskripsiMenuDetail.setText(deskripsiMakanan);

        int harga;
        try {
            harga = Integer.parseInt(hargaMakanan);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtHargaMenuDetail.setText(formatRupiah(harga));

        // Setup tombol tambah
        btnTambah.setOnClickListener(v -> addToCart(imageUrl, harga));

        // Setup tombol back dengan animasi
        btnBack.setOnClickListener(v -> {
            finish(); // Tutup MenuDetailActivity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void addToCart(String imageUrl, int harga) {
        String jumlahStr = edtJumlah.getText().toString().trim();

        if (jumlahStr.isEmpty()) {
            edtJumlah.setError("Jumlah harus diisi");
            edtJumlah.requestFocus();
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) {
                edtJumlah.setError("Minimal 1");
                edtJumlah.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            edtJumlah.setError("Masukkan angka valid");
            edtJumlah.requestFocus();
            return;
        }

        String namaMenu = txtNamaMenuDetail.getText().toString();
        String email = user.getEmail();

        //  Hapus: imageResourceId (int)
        //  Simpan: imageUrl (String) → simpan di CartItem
        CartItem newItem = new CartItem(email, namaMenu, harga, jumlah, imageUrl);

        boolean found = false;
        for (int i = 0; i < cartItemList.size(); i++) {
            CartItem existingItem = cartItemList.get(i);
            if (existingItem.getNamaMenu().equals(newItem.getNamaMenu()) &&
                    existingItem.getHarga() == newItem.getHarga() &&
                    existingItem.getImageUrl().equals(newItem.getImageUrl())) { // ✅ Bandingkan URL
                existingItem.setJumlah(existingItem.getJumlah() + newItem.getJumlah());
                cartItemList.set(i, existingItem);
                found = true;
                break;
            }
        }

        if (!found) {
            cartItemList.add(newItem);
        }

        saveCartItems();
        progressDialog.show();

        new android.os.Handler().postDelayed(() -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MenuDetailActivity.this, CartActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 800);
    }

    public static String formatRupiah(int amount) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        return format.format(amount).replace("Rp", "Rp ");
    }

    private void loadCartItems() {
        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString("cart items_" + userId, null);
        Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
        cartItemList = gson.fromJson(json, type);

        if (cartItemList == null) {
            cartItemList = new ArrayList<>();
        }
    }

    private void saveCartItems() {
        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartItemList);
        editor.putString("cart items_" + userId, json);
        editor.apply();
    }

    //  Animasi saat back dengan tombol fisik
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}