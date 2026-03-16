// TambahProdukActivity.java berhasil
package com.example.kepengendessboxapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TambahProdukActivity extends AppCompatActivity {

    private EditText edtNama, edtHarga, edtDeskripsi;
    private RadioGroup rgKategori;
    private ImageView imgPreview;
    private Button btnPilihGambar, btnSimpan;
    private Uri imageUri = null;
    private final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_produk);

        // Inisialisasi
        initViews();
        db = FirebaseFirestore.getInstance();
        httpClient = new OkHttpClient();

        // Pilih gambar dari galeri
        btnPilihGambar.setOnClickListener(v -> pilihGambar());

        // Simpan produk
        btnSimpan.setOnClickListener(v -> simpanProduk());

    }

    private void initViews() {
        edtNama = findViewById(R.id.edtNamaProduk);
        edtHarga = findViewById(R.id.edtHargaProduk);
        edtDeskripsi = findViewById(R.id.edtDeskripsiProduk);
        rgKategori = findViewById(R.id.rgKategori);
        imgPreview = findViewById(R.id.imgPreview);
        btnPilihGambar = findViewById(R.id.btnPilihGambar);
        btnSimpan = findViewById(R.id.btnSimpanProduk);
    }

    private void pilihGambar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgPreview.setImageURI(imageUri);
            Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal memilih gambar", Toast.LENGTH_SHORT).show();
        }
    }

    // TambahProdukActivity.java
    private void simpanProduk() {
        String nama = edtNama.getText().toString().trim();
        String hargaStr = edtHarga.getText().toString().trim();
        String deskripsi = edtDeskripsi.getText().toString().trim();

        int selectedId = rgKategori.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String kategori = rb != null ? rb.getText().toString() : "";

        //  Validasi semua field
        if (nama.isEmpty() || hargaStr.isEmpty() || deskripsi.isEmpty() || kategori.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Semua field harus diisi, termasuk gambar", Toast.LENGTH_SHORT).show();
            return;
        }

        int harga;
        try {
            harga = Integer.parseInt(hargaStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Harga harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tentukan koleksi Firestore berdasarkan kategori
        String koleksi;
        switch (kategori) {
            case "Paket Promo":
                koleksi = "promo_tambahan";
                break;
            case "Dessert Box":
                koleksi = "dessert_tambahan";
                break;
            case "Minuman":
                koleksi = "minuman_tambahan";
                break;
            default:
                Toast.makeText(this, "Kategori tidak valid", Toast.LENGTH_SHORT).show();
                return;
        }

        //  Pastikan db tidak null
        if (db == null) {
            Toast.makeText(this, "Gagal: Firestore tidak terhubung", Toast.LENGTH_LONG).show();
            return;
        }

        //  Upload gambar ke ImgBB
        uploadImageToImgBB(imageUri, imageUrl -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                //  Simpan data ke Firestore
                Map<String, Object> produk = new HashMap<>();
                produk.put("nama", nama);
                produk.put("harga", harga);
                produk.put("deskripsi", deskripsi);
                produk.put("kategori", kategori);
                produk.put("imageUrl", imageUrl);

                //  Tambahkan produk ke Firestore
                db.collection(koleksi)
                        .add(produk)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                            finish(); // Kembali ke KelolaMenuActivity
                        })
                        .addOnFailureListener(e -> {
                            //  Tampilkan error lebih detail
                            Toast.makeText(this, "Gagal menyimpan ke Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        });
            } else {
                Toast.makeText(this, "Gagal upload gambar ke ImgBB", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadImageToImgBB(Uri imageUri, OnUploadCompleteListener listener) {
        try {
            //  Baca InputStream dan konversi ke byte[]
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            byte[] imageBytes = baos.toByteArray();
            inputStream.close();
            baos.close();

            //  Buat RequestBody dari byte[]
            RequestBody imageBody = RequestBody.create(imageBytes, MediaType.parse("image/*"));

            //  Buat Multipart
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", getString(R.string.imgbb_api_key))
                    .addFormDataPart("image", UUID.randomUUID().toString() + ".jpg", imageBody)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(TambahProdukActivity.this, "Upload gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    listener.onComplete(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String url = json.getJSONObject("data").getString("url");
                            runOnUiThread(() -> Toast.makeText(TambahProdukActivity.this, "Gambar berhasil di-upload", Toast.LENGTH_SHORT).show());
                            listener.onComplete(url);
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(TambahProdukActivity.this, "Gagal parse JSON", Toast.LENGTH_LONG).show());
                            listener.onComplete(null);
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(TambahProdukActivity.this, "Upload gagal: " + response.message(), Toast.LENGTH_LONG).show());
                        listener.onComplete(null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            listener.onComplete(null);
        }
    }

    public interface OnUploadCompleteListener {
        void onComplete(String imageUrl);
    }
}