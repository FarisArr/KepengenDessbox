// EditProdukActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class EditProdukActivity extends AppCompatActivity {

    private EditText edtNamaProduk, edtHargaProduk, edtDeskripsiProduk;
    private Button btnSimpan, btnHapus, btnBatal, btnGantiGambar;
    private ImageView imgPreview;
    private FirebaseFirestore firestore;
    private OkHttpClient httpClient;

    // Data produk
    private String currentNama;
    private int currentHarga;
    private String currentDeskripsi;
    private String koleksi; // "dessert", "promo", atau "minuman"
    private String documentId;
    private boolean isBawaan; // true = bawaan, false = tambahan

    // Gambar
    private String currentImageUrl;
    private Uri newImageUri = null;
    private boolean imageChanged = false;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produk);

        // Inisialisasi Firebase dan HTTP Client
        firestore = FirebaseFirestore.getInstance();
        httpClient = new OkHttpClient();

        // Inisialisasi View
        initView();

        // Ambil data dari Intent
        getDataFromIntent();

        // Isi field dengan data produk
        setupForm();

        // Setup onClickListener
        setupClickListeners();
    }

    private void initView() {
        edtNamaProduk = findViewById(R.id.edtNamaProduk);
        edtHargaProduk = findViewById(R.id.edtHargaProduk);
        edtDeskripsiProduk = findViewById(R.id.edtDeskripsiProduk);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnHapus = findViewById(R.id.btnHapus);
        btnBatal = findViewById(R.id.btnBatal);
        btnGantiGambar = findViewById(R.id.btnGantiGambar);
        imgPreview = findViewById(R.id.imgPreview);
    }

    private void getDataFromIntent() {
        // Ambil data dari Intent
        currentNama = getIntent().getStringExtra("NAMA");
        currentHarga = getIntent().getIntExtra("HARGA", 0);
        currentDeskripsi = getIntent().getStringExtra("DESKRIPSI");
        koleksi = getIntent().getStringExtra("KOLEKSI");
        documentId = getIntent().getStringExtra("DOCUMENT_ID");
        currentImageUrl = getIntent().getStringExtra("IMAGE_URL");
        isBawaan = getIntent().getBooleanExtra("IS_BAWAAN", true); // Default: bawaan

        if (currentNama == null || koleksi == null) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupForm() {
        edtNamaProduk.setText(currentNama);
        edtHargaProduk.setText(String.valueOf(currentHarga));
        edtDeskripsiProduk.setText(currentDeskripsi);

        // Tampilkan gambar jika ada
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(imgPreview);
        }
    }

    private void setupClickListeners() {
        btnSimpan.setOnClickListener(v -> simpanPerubahan());
        btnHapus.setOnClickListener(v -> hapusProduk());
        btnBatal.setOnClickListener(v -> finish());
        btnGantiGambar.setOnClickListener(v -> pilihGambar());
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            newImageUri = data.getData();
            imgPreview.setImageURI(newImageUri);
            imageChanged = true;
        }
    }

    private void simpanPerubahan() {
        String namaBaru = edtNamaProduk.getText().toString().trim();
        String hargaStr = edtHargaProduk.getText().toString().trim();
        String deskripsiBaru = edtDeskripsiProduk.getText().toString().trim();

        if (namaBaru.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        int hargaBaru = Integer.parseInt(hargaStr);

        if (!isBawaan && documentId != null) {
            // Produk tambahan → update Firestore langsung
            Map<String, Object> update = new HashMap<>();
            update.put("nama", namaBaru);
            update.put("harga", hargaBaru);
            update.put("deskripsi", deskripsiBaru);

            if (imageChanged && newImageUri != null) {
                uploadImageToImgBB(newImageUri, imageUrl -> {
                    if (imageUrl != null) {
                        update.put("imageUrl", imageUrl);
                        updateToFirestore(update);
                    } else {
                        Toast.makeText(this, "Gagal upload gambar ke ImgBB", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                updateToFirestore(update);
            }
        } else {
            // Produk bawaan → simpan ke koleksi "diubah" dengan kunci unik
            Map<String, Object> edit = new HashMap<>();
            edit.put("nama", currentNama);
            edit.put("namaBaru", namaBaru);
            edit.put("harga", hargaBaru);
            edit.put("deskripsi", deskripsiBaru);
            edit.put("kategori", koleksi);
            edit.put("aksi", "edit");

            if (imageChanged && newImageUri != null) {
                uploadImageToImgBB(newImageUri, imageUrl -> {
                    if (imageUrl != null) {
                        edit.put("imageUrl", imageUrl);
                        saveToDiubah(edit);
                    } else {
                        saveToDiubah(edit);
                    }
                });
            } else {
                saveToDiubah(edit);
            }
        }
    }

    // Upload gambar ke ImgBB
    private void uploadImageToImgBB(Uri imageUri, OnUploadCompleteListener listener) {
        try {
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

            RequestBody imageBody = RequestBody.create(imageBytes, MediaType.parse("image/*"));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", getString(R.string.imgbb_api_key))
                    .addFormDataPart("image", UUID.randomUUID().toString() + ".jpg", imageBody)
                    .build();

            // Perbaiki URL: hapus spasi di akhir
            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(EditProdukActivity.this, "Upload gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    listener.onComplete(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String url = json.getJSONObject("data").getString("url");
                            runOnUiThread(() -> Toast.makeText(EditProdukActivity.this, "Gambar berhasil di-upload", Toast.LENGTH_SHORT).show());
                            listener.onComplete(url);
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(EditProdukActivity.this, "Gagal parse JSON", Toast.LENGTH_LONG).show());
                            listener.onComplete(null);
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(EditProdukActivity.this, "Upload gagal: " + response.message(), Toast.LENGTH_LONG).show());
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

    private void updateToFirestore(Map<String, Object> update) {
        firestore.collection(koleksi)
                .document(documentId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToDiubah(Map<String, Object> edit) {
        //  Gunakan kombinasi unik: nama + kategori
        String docId = "edit_" + koleksi + "_" + currentNama.replace(" ", "_");
        firestore.collection("diubah")
                .document(docId)
                .set(edit)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void hapusProduk() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Yakin ingin menghapus \"" + currentNama + "\"?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    if (!isBawaan && documentId != null) {
                        // Produk tambahan → hapus langsung
                        firestore.collection(koleksi)
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Produk bawaan → catat di "diubah"
                        String docId = "hapus_" + koleksi + "_" + currentNama.replace(" ", "_");
                        firestore.collection("diubah")
                                .document(docId)
                                .set(new HashMap<String, Object>() {{
                                    put("nama", currentNama);
                                    put("kategori", koleksi);
                                    put("aksi", "hapus");
                                }})
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal mencatat penghapusan", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public interface OnUploadCompleteListener {
        void onComplete(String imageUrl);
    }
}