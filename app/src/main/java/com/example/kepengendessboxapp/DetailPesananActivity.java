// DetailPesananActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailPesananActivity extends AppCompatActivity {

    // Tombol Kembali
    private ImageView btnBack;

    // Variabel untuk menyimpan data penting
    private String metodePembayaranStr;
    private long totalHarga;
    private String pesananId;
    private boolean isAdmin = false;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DocumentSnapshot pesananDoc;

    private static final String TAG = "DetailPesanan";
    private TextView txtNamaPemesan, txtNomorTelepon, txtNomorPesanan, txtMetodePembayaran, txtWaktuPesanan, txtAlamatPengiriman;
    private TextView txtJumlahItem, txtHargaOngkir, txtTotal;
    private LinearLayout linearLayoutPesanan, linearBtnUser, linearBtnAdmin;

    // Tombol Untuk User
    private Button btnLanjutkanPembayaran, btnKonfirmasiPesanan, btnPesanLagi, btnBatalkanPesanan;

    // Tombol status untuk admin
    private Button btnDiproses, btnDikirim, btnSelesai, btnDibatalkan;

    // Status Pesanan
    private TextView txtStatusPesanan;

    // Batas Waktu Pembayaran
    private TextView txtBatasWaktu;
    private CountDownTimer countDownTimer;
    private long savedDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesanan);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User tidak login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //  Cek role admin
        isAdmin = user.getEmail() != null && user.getEmail().equals("kepengendessbox@gmail.com");

        // Inisialisasi View
        initViews();

        // Cek apakah view null
        if (linearLayoutPesanan == null) {
            Toast.makeText(this, "Error: linear_pesanan tidak ditemukan", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Ambil pesanan_id dari Intent
        pesananId = getIntent().getStringExtra("pesanan_id");
        if (pesananId == null || pesananId.isEmpty()) {
            Toast.makeText(this, "ID pesanan tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup tombol back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                onBackPressed();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance();

        // Muat data pesanan (real-time)
        loadPesananData(pesananId);

        // Setup UI berdasarkan role
        setupRoleBasedUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtNamaPemesan = findViewById(R.id.txtNamaPemesan);
        txtNomorTelepon = findViewById(R.id.txtNomorTelepon);
        txtNomorPesanan = findViewById(R.id.txtNomorPesanan);
        txtWaktuPesanan = findViewById(R.id.txtWaktuPesanan);
        txtBatasWaktu = findViewById(R.id.txtBatasWaktu);
        txtMetodePembayaran = findViewById(R.id.txtMetodePembayaran);
        txtAlamatPengiriman = findViewById(R.id.txtAlamatPengiriman);
        txtStatusPesanan = findViewById(R.id.txtStatusPesanan);
        txtJumlahItem = findViewById(R.id.txtJumlahItem);
        txtHargaOngkir = findViewById(R.id.txtHargaOngkir);
        txtTotal = findViewById(R.id.txtTotal);
        linearLayoutPesanan = findViewById(R.id.linear_pesanan);

        // Tombol Untuk User
        linearBtnUser = findViewById(R.id.linearBtnUser);
        btnLanjutkanPembayaran = findViewById(R.id.btnLanjutkanPembayaran);
        btnKonfirmasiPesanan = findViewById(R.id.btnKonfirmasiPesanan);
        btnPesanLagi = findViewById(R.id.btnPesanLagi);
        btnBatalkanPesanan = findViewById(R.id.btnBatalkanPesanan);

        // Tombol Untuk Admin
        linearBtnAdmin = findViewById(R.id.linearBtnAdmin);
        btnDiproses = findViewById(R.id.btnDiproses);
        btnDikirim = findViewById(R.id.btnDikirim);
        btnSelesai = findViewById(R.id.btnSelesai);
        btnDibatalkan = findViewById(R.id.btnDibatalkan);

        firestore = FirebaseFirestore.getInstance();
    }

    private void setupRoleBasedUI() {
        if (isAdmin) {
            // Tampilkan tombol admin
            btnLanjutkanPembayaran.setVisibility(View.GONE);
            showAdminButtons();
            setupAdminButtonListeners();
            btnKonfirmasiPesanan.setVisibility(View.GONE);
        } else {
            // Sembunyikan tombol admin
            hideAdminButtons();
            btnLanjutkanPembayaran.setVisibility(View.GONE);
            setupKonfirmasiPesanan(); // Tampilkan tombol untuk user
        }
    }

    private void showAdminButtons() {
        if (findViewById(R.id.linearBtnAdmin) != null) {
            findViewById(R.id.linearBtnAdmin).setVisibility(View.VISIBLE);
        }
        if (btnDiproses != null) btnDiproses.setVisibility(View.VISIBLE);
        if (btnDikirim != null) btnDikirim.setVisibility(View.VISIBLE);
        if (btnSelesai != null) btnSelesai.setVisibility(View.VISIBLE);
        if (btnDibatalkan != null) btnDibatalkan.setVisibility(View.VISIBLE);
    }

    private void hideAdminButtons() {
        if (findViewById(R.id.linearBtnAdmin) != null) {
            findViewById(R.id.linearBtnAdmin).setVisibility(View.GONE);
        }
        if (btnDiproses != null) btnDiproses.setVisibility(View.GONE);
        if (btnDikirim != null) btnDikirim.setVisibility(View.GONE);
        if (btnSelesai != null) btnSelesai.setVisibility(View.GONE);
        if (btnDibatalkan != null) btnDibatalkan.setVisibility(View.GONE);
    }

    private void setupAdminButtonListeners() {
        if (btnDiproses != null) btnDiproses.setOnClickListener(v -> updateStatus("Diproses"));
        if (btnDikirim != null) btnDikirim.setOnClickListener(v -> updateStatus("Dikirim"));
        if (btnSelesai != null) btnSelesai.setOnClickListener(v -> updateStatus("Selesai"));
        if (btnDibatalkan != null) btnDibatalkan.setOnClickListener(v -> updateStatus("Dibatalkan"));
    }

    private void updateStatus(String status) {
        if (pesananId == null || pesananId.isEmpty()) return;

        //  Ambil metode pembayaran dari pesananDoc
        String metodePembayaran = pesananDoc.getString("metodePembayaran");
        String finalStatus;

        if ("COD (Bayar di Tempat)".equals(metodePembayaran)) {
            if ("Selesai".equals(status)) {
                finalStatus = "Pesanan Selesai";
            } else {
                finalStatus = "Pesanan " + status;
            }
        } else {
            if ("Diproses".equals(status)) {
                finalStatus = "Pesanan Diproses";
            } else {
                finalStatus = "Pesanan " + status;
            }
        }

        // Ubah status menjadi "Pesanan [Status]"
        String formattedStatus = "Pesanan " + status;

        firestore.collection("pesanan").document(pesananId)
                .update("status", formattedStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status diperbarui: " + formattedStatus, Toast.LENGTH_SHORT).show();
                    //  Kirim notifikasi ke user
                    sendNotificationToUser(formattedStatus);

                    //  Jika admin klik, sembunyikan semua tombol aksi user
                    if (isAdmin) {
                        btnKonfirmasiPesanan.setVisibility(View.GONE);
                        btnBatalkanPesanan.setVisibility(View.GONE);
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal update status", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update status gagal", e);
                });
    }

    //  GUNAKAN addSnapshotListener UNTUK REAL-TIME UPDATE
    private void loadPesananData(String pesananId) {
        firestore.collection("pesanan").document(pesananId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Gagal memuat data", error);
                        Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        displayPesanan(doc);
                    } else {
                        Toast.makeText(this, "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displayPesanan(DocumentSnapshot doc) {
        // Simpan data penting
        metodePembayaranStr = doc.getString("metodePembayaran");
        totalHarga = doc.getLong("totalHarga") != null ? doc.getLong("totalHarga") : 0;

        //  Ambil NamaLengkap dari Firestore
        String namaLengkap = doc.getString("NamaLengkap");
        if (txtNamaPemesan != null && namaLengkap != null && !namaLengkap.isEmpty()) {
            txtNamaPemesan.setText(namaLengkap);
        } else {
            txtNamaPemesan.setText("Pelanggan");
        }

        // Ambil email untuk cari Nama Lengkap dan No. WhatsApp dari Firestore
        String email = doc.getString("Email");
        if (email != null && !email.isEmpty()) {
            loadUserDataFromFirestore(email);
        } else {
            txtNamaPemesan.setText("Pelanggan");
            txtNomorTelepon.setText("-");
        }

        txtNomorPesanan.setText(generateRandomOrderNumber());

        if (txtWaktuPesanan != null && doc.getString("tanggal") != null) {
            txtWaktuPesanan.setText(formatTanggal(doc.getString("tanggal")));
        }

        // Alamat Pengiriman
        if (txtAlamatPengiriman != null) {
            String alamat = doc.getString("Alamat");
            if (alamat != null && !alamat.isEmpty()) {
                txtAlamatPengiriman.setText(alamat);
            } else {
                txtAlamatPengiriman.setText("Alamat tidak tersedia");
            }
        }

        // Metode Pembayaran
        if (txtMetodePembayaran != null && metodePembayaranStr != null) {
            txtMetodePembayaran.setText(metodePembayaranStr);
        }

        // Status Pesanan
        String status = doc.getString("status");
        TextView txtStatusPesanan = findViewById(R.id.txtStatusPesanan);
        if (txtStatusPesanan != null) {
            if (status != null) {
                txtStatusPesanan.setText(status);

                // Warna berdasarkan status
                if (status.startsWith("Pesanan Diterima") || status.startsWith("Pesanan Diproses") || status.startsWith("Pesanan Dikirim")) {
                    txtStatusPesanan.setTextColor(getResources().getColor(R.color.brown));
                } else if (status.equals("Pesanan Selesai")) {
                    txtStatusPesanan.setTextColor(getResources().getColor(R.color.green));
                } else if (status.equals("Pesanan Dibatalkan")) {
                    txtStatusPesanan.setTextColor(getResources().getColor(R.color.red));
                } else {
                    txtStatusPesanan.setTextColor(getResources().getColor(R.color.black));
                }
            } else {
                txtStatusPesanan.setText("Belum diproses");
            }
        }

        // Ongkir & total
        long ongkir = getInt(doc.get("Ongkir"));
        long totalHarga = getInt(doc.get("totalHarga"));
        if (txtHargaOngkir != null) txtHargaOngkir.setText("Rp " + rp(ongkir));
        if (txtTotal != null) txtTotal.setText("Rp " + rp(totalHarga));

        // Daftar Pesanan
        List<Map<String, Object>> pesananList = (List<Map<String, Object>>) doc.get("pesanan");
        if (pesananList != null && linearLayoutPesanan != null) {
            linearLayoutPesanan.removeAllViews();
            for (Map<String, Object> item : pesananList) {
                addItemToLayout(item);
            }
        }

        // Setup tombol "Lanjutkan Pembayaran" hanya untuk user & jika bukan COD
        if (!isAdmin && metodePembayaranStr != null && !metodePembayaranStr.equals("COD (Bayar di Tempat)")) {
            if ("Menunggu Pembayaran".equals(status)) {
                btnLanjutkanPembayaran.setVisibility(View.VISIBLE);
                btnLanjutkanPembayaran.setOnClickListener(v -> {
                    Intent intent = new Intent(DetailPesananActivity.this, PembayaranActivity.class);
                    intent.putExtra("metode", metodePembayaranStr);
                    intent.putExtra("total", totalHarga);
                    intent.putExtra("pesanan_id", pesananId);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            } else {
                btnLanjutkanPembayaran.setVisibility(View.GONE);
            }
        } else {
            btnLanjutkanPembayaran.setVisibility(View.GONE);
        }

        //  AMBIL DEADLINE DARI FIRESTORE
        savedDeadline = doc.getLong("deadline") != null ? doc.getLong("deadline") : 0;
        boolean waktuHabis = System.currentTimeMillis() > savedDeadline;

        //  RESET SEMUA TOMBOL
        btnLanjutkanPembayaran.setVisibility(View.GONE);
        btnKonfirmasiPesanan.setVisibility(View.GONE);
        btnBatalkanPesanan.setVisibility(View.GONE);
        btnPesanLagi.setVisibility(View.GONE);

        //  LOGIKA ADMIN
        if (isAdmin) {
            if ("Waktu Pembayaran Habis".equals(status)) {
                //  Sembunyikan semua tombol admin
                hideAdminButtons();
            } else {
                //  Tampilkan tombol admin jika status bukan "Waktu Pembayaran Habis"
                showAdminButtons();
                setupAdminButtonListeners();
            }
        }

        //  LOGIKA USER
        if (!isAdmin) {
            if ("COD (Bayar di Tempat)".equals(metodePembayaranStr)) {
                // COD: tampilkan Konfirmasi & Batalkan jika status "Menunggu Konfirmasi"
                if ("Menunggu Konfirmasi".equals(status)) {
                    btnKonfirmasiPesanan.setVisibility(View.VISIBLE);
                    btnBatalkanPesanan.setVisibility(View.VISIBLE);
                }
            } else {
                // Transfer
                if ("Menunggu Pembayaran".equals(status)) {
                    if (waktuHabis) {
                        //  Waktu habis → hanya tampilkan "Pesan Lagi"
                        firestore.collection("pesanan")
                                .document(pesananId)
                                .update("status", "Waktu Pembayaran Habis")
                                .addOnSuccessListener(aVoid -> {
                                    if (txtStatusPesanan != null) {
                                        txtStatusPesanan.setText("Waktu Pembayaran Habis");
                                        txtStatusPesanan.setTextColor(getResources().getColor(R.color.red));
                                    }
                                });
                        btnPesanLagi.setVisibility(View.VISIBLE);
                    } else {
                        //  Masih bisa bayar → tampilkan semua tombol
                        btnLanjutkanPembayaran.setVisibility(View.VISIBLE);
                        btnKonfirmasiPesanan.setVisibility(View.VISIBLE);
                        btnBatalkanPesanan.setVisibility(View.VISIBLE);
                    }
                } else if ("Waktu Pembayaran Habis".equals(status)) {
                    // Jika status sudah "Waktu Pembayaran Habis"
                    btnPesanLagi.setVisibility(View.VISIBLE);
                }
            }
        }

        //  SETUP LISTENER TOMBOL
        btnPesanLagi.setOnClickListener(v -> {
            Intent intent = new Intent(DetailPesananActivity.this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnBatalkanPesanan.setOnClickListener(v -> updateStatus("Dibatalkan"));

        // --- LOGIKA RATING: HANYA UNTUK USER DAN "Pesanan Selesai" ---
        Boolean rated = doc.getBoolean("rated");
        if (rated != null && rated) {
            //  Hanya tampilkan pesan terima kasih jika BUKAN admin
            if (!isAdmin) {
                btnKonfirmasiPesanan.setVisibility(View.VISIBLE);
                btnKonfirmasiPesanan.setText("Terima kasih atas ulasannya! 🌟");
                btnKonfirmasiPesanan.setEnabled(false);
                btnKonfirmasiPesanan.setBackgroundColor(getResources().getColor(R.color.gray));
            } else {
                //  Admin: sembunyikan
                btnKonfirmasiPesanan.setVisibility(View.GONE);
            }
        }
        else if (!isAdmin && "Pesanan Selesai".equals(status)) {
            //  User belum rating → tampilkan "Beri Ulasan"
            btnKonfirmasiPesanan.setVisibility(View.VISIBLE);
            btnKonfirmasiPesanan.setText("Beri Ulasan");
            btnKonfirmasiPesanan.setEnabled(true);
            btnKonfirmasiPesanan.setBackgroundResource(R.drawable.btn_bg_design);
            btnKonfirmasiPesanan.setOnClickListener(v -> {
                Intent intent = new Intent(DetailPesananActivity.this, RatingActivity.class);
                intent.putExtra("pesanan_id", pesananId);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
        else {
            //  Untuk kondisi lain, ikuti logika default
            if (isAdmin ||
                    (status != null && (
                            status.startsWith("Pesanan Diterima") ||
                                    status.startsWith("Pesanan Diproses") ||
                                    status.startsWith("Pesanan Dikirim") ||
                                    status.startsWith("Pesanan Selesai") ||
                                    status.startsWith("Pesanan Dibatalkan") ||
                                    "Waktu Pembayaran Habis".equals(status)))) {
                btnKonfirmasiPesanan.setVisibility(View.GONE);
                btnBatalkanPesanan.setVisibility(View.GONE);
            } else {
                btnKonfirmasiPesanan.setVisibility(View.VISIBLE);
                if (!isAdmin) {
                    setupBatalkanPesanan();
                }
            }
        }

        // TAMPILKAN HITUNG MUNDUR HANYA JIKA STATUS "Menunggu Pembayaran" DAN WAKTU BELUM HABIS
        if ("Menunggu Pembayaran".equals(status) && savedDeadline > 0 && !waktuHabis) {
            startCountdownTimer();
            txtBatasWaktu.setVisibility(View.VISIBLE);
        } else {
            txtBatasWaktu.setVisibility(View.GONE);
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        }

        // Simpan dokumen terakhir
        this.pesananDoc = doc;
    }


    // Metode untuk menghasilkan nomor pesanan acak 12 digit
    private String generateRandomOrderNumber() {
        long currentTime = System.currentTimeMillis();
        String orderNumber = String.valueOf(currentTime);
        while (orderNumber.length() < 12) {
            orderNumber = "0" + orderNumber;
        }
        return orderNumber.substring(orderNumber.length() - 12);
    }

    private void getUserPhoneByEmail(String email, OnPhoneResultListener listener) {
        firestore.collection("users")
                .whereEqualTo("Email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String phone = querySnapshot.getDocuments().get(0).getString("NoWhatsapp");
                        if (phone != null && phone.startsWith("0")) {
                            phone = "+62" + phone.substring(1); // Format ke +62
                        }
                        listener.onResult(phone);
                    } else {
                        listener.onResult(null);
                    }
                })
                .addOnFailureListener(e -> listener.onResult(null));
    }

    private interface OnPhoneResultListener {
        void onResult(String phone);
    }

    private void addItemToLayout(Map<String, Object> item) {
        try {
            View itemView = getLayoutInflater().inflate(R.layout.item_detail_pesanan, linearLayoutPesanan, false);

            ImageView imgMenu = itemView.findViewById(R.id.imgProduk);
            TextView txtNamaMenu = itemView.findViewById(R.id.txtNamaMenu);
            TextView txtJumlah = itemView.findViewById(R.id.txtJumlah);
            TextView txtHarga = itemView.findViewById(R.id.txtHarga);

            txtNamaMenu.setText((String) item.get("namaPesanan"));
            txtJumlah.setText("x" + getInt(item.get("jumlah")));
            txtHarga.setText("Rp " + rp(getInt(item.get("totalPesanan"))));

            //  Ganti dari imageResId → imageUrl
            String imageUrl = (String) item.get("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imgMenu);
            } else {
                imgMenu.setImageResource(R.drawable.placeholder);
            }

            linearLayoutPesanan.addView(itemView);
        } catch (Exception e) {
            Log.e(TAG, "Gagal tambah item", e);
        }
    }

    private void startCountdownTimer() {
        long timeRemaining = savedDeadline - System.currentTimeMillis();

        if (timeRemaining <= 0) {
            //  JANGAN ubah status
            txtBatasWaktu.setText("Waktu pembayaran habis");
            btnLanjutkanPembayaran.setVisibility(View.GONE);
            btnKonfirmasiPesanan.setVisibility(View.GONE);
            Toast.makeText(this, "Waktu pembayaran habis", Toast.LENGTH_LONG).show();
            return;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                txtBatasWaktu.setText(String.format("Bayar dalam: %d menit %d detik", minutes, seconds));
            }

            @Override
            public void onFinish() {
                txtBatasWaktu.setText("Waktu pembayaran habis");
                btnLanjutkanPembayaran.setVisibility(View.GONE);
                btnKonfirmasiPesanan.setVisibility(View.GONE);
                Toast.makeText(DetailPesananActivity.this, "Waktu pembayaran habis", Toast.LENGTH_LONG).show();
            }
        }.start();
    }


    private int getInt(Object value) {
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Integer) return (Integer) value;
        return 0;
    }

    private String rp(long amount) {
        Locale localeID = new Locale("in", "ID");
        java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        return format.format(amount).replace("Rp", "").trim();
    }

    private String formatTanggal(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat output = new SimpleDateFormat("dd MMMM yyyy | HH:mm", new Locale("in", "ID"));
            Date date = input.parse(dateStr);
            return output.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Format tanggal gagal", e);
            return dateStr;
        }
    }

    /**
     * Ambil Nama Lengkap dan No. WhatsApp dari Firestore berdasarkan Email
     */
    private void loadUserDataFromFirestore(String email) {
        if (email == null || txtNamaPemesan == null || txtNomorTelepon == null) return;

        firestore.collection("users")
                .whereEqualTo("Email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String namaLengkap = userDoc.getString("NamaLengkap");
                        String noWa = userDoc.getString("NoWhatsapp");

                        if (namaLengkap != null && !namaLengkap.isEmpty()) {
                            txtNamaPemesan.setText(namaLengkap);
                        }

                        if (noWa != null) {
                            // Format 08xx → +628xx
                            if (noWa.startsWith("0")) {
                                noWa = "+62" + noWa.substring(1);
                            }
                            txtNomorTelepon.setText(noWa);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal muat data user", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Setup tombol "Konfirmasi Pesanan" untuk user
     * User bisa konfirmasi meskipun belum bayar, untuk memberi tahu admin
     */
    private void setupKonfirmasiPesanan() {
        btnKonfirmasiPesanan.setOnClickListener(v -> {
            firestore.collection("pesanan").document(pesananId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String status = doc.getString("status");

                            //  Izinkan konfirmasi jika status masih dalam tahap awal
                            if ("Menunggu Konfirmasi".equals(status) ||
                                    "Menunggu Pembayaran".equals(status) ||
                                    "Pesanan Diproses".equals(status) ||
                                    "DITERIMA".equals(status)) {

                                // Tampilkan pesan sesuai status
                                if ("Menunggu Pembayaran".equals(status)) {
                                    Toast.makeText(this, "Anda belum bayar. Notifikasi dikirim sebagai pemberitahuan awal.", Toast.LENGTH_LONG).show();
                                }

                                // Kirim notifikasi ke admin dengan status terkini
                                sendWhatsAppToAdmin(status);

                            } else if ("DIPROSES".equals(status) ||
                                    "DIKIRIM".equals(status) ||
                                    "SELESAI".equals(status)) {
                                //  Pesanan sudah diproses
                                Toast.makeText(this, "Pesanan sedang diproses atau sudah selesai.", Toast.LENGTH_SHORT).show();
                            } else if ("DIBATALKAN".equals(status)) {
                                //  Pesanan dibatalkan
                                Toast.makeText(this, "Pesanan telah dibatalkan.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Data pesanan tidak ditemukan.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal memuat status pesanan.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void sendWhatsAppToAdmin(String status) {
        StringBuilder pesananList = new StringBuilder();
        List<Map<String, Object>> pesananListRaw = (List<Map<String, Object>>) pesananDoc.get("pesanan");
        if (pesananListRaw != null) {
            for (Map<String, Object> item : pesananListRaw) {
                pesananList.append("• ").append(item.get("namaPesanan"))
                        .append(" x").append(getInt(item.get("jumlah")))
                        .append(" = Rp ").append(rp(getInt(item.get("totalPesanan")))).append("\n");
            }
        }

        String message = "🔔 *KONFIRMASI PESANAN*\n\n" +
                "📌 *Nama Pemesan*: " + txtNamaPemesan.getText().toString() + "\n" +
                "📞 *Nomor Telepon*: " + txtNomorTelepon.getText().toString() + "\n" +
                "📅 *Waktu Pemesanan*: " + txtWaktuPesanan.getText().toString() + "\n" +
                "📦 *Metode Pembayaran*: " + txtMetodePembayaran.getText().toString() + "\n" +
                "🏠 *Alamat Pengiriman*: " + txtAlamatPengiriman.getText().toString() + "\n" +
                "📊 *Status Pesanan*: *" + status + "*\n\n" +
                "📋 *Daftar Pesanan*:\n" + pesananList.toString() + "\n" +
                "🚚 *Ongkos Pengiriman*: " + txtHargaOngkir.getText().toString() + "\n" +
                "💰 *Total*: " + txtTotal.getText().toString() + "\n\n" +
                "Silakan proses pesanan di aplikasi admin.";

        String url = "https://wa.me/+6285717959668?text=" + Uri.encode(message);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setPackage("com.whatsapp");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }

    /**
     * Setup tombol "Batalkan Pesanan" untuk user
     * Langsung update status & buka WhatsApp ke admin (sama seperti "Konfirmasi Pesanan")
     */
    private void setupBatalkanPesanan() {
        btnBatalkanPesanan.setVisibility(View.VISIBLE);
        btnBatalkanPesanan.setOnClickListener(v -> {
            // Buat AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Batalkan Pesanan?")
                    .setMessage("Apakah Anda yakin ingin membatalkan pesanan ini?\n\n" +
                            "Aksi ini tidak bisa dibatalkan. Pesanan akan langsung dibatalkan " +
                            "dan admin akan mendapat notifikasi.")
                    .setPositiveButton("Ya, Batalkan", (dialog, which) -> {
                        String status = "Pesanan Dibatalkan oleh User";

                        // Update status ke Firestore
                        firestore.collection("pesanan").document(pesananId)
                                .update("status", status)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Pesanan dibatalkan", Toast.LENGTH_SHORT).show();
                                    btnBatalkanPesanan.setVisibility(View.GONE);
                                    btnKonfirmasiPesanan.setVisibility(View.GONE);
                                    btnLanjutkanPembayaran.setVisibility(View.GONE);

                                    //  Langsung buka WhatsApp ke admin
                                    sendWhatsAppToAdminForCancellation(status);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal membatalkan pesanan", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Batal", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(false);

            // Buat dialog
            AlertDialog dialog = builder.create();

            // Atur warna tombol "Ya, Batalkan" menjadi merah
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(R.color.red));
            });

            // Tampilkan dialog
            dialog.show();
        });
    }

    /**
     * Kirim notifikasi ke admin saat user membatalkan pesanan
     * Harus 100% sama seperti sendWhatsAppToAdmin()
     */
    private void sendWhatsAppToAdminForCancellation(String status) {
        StringBuilder pesananList = new StringBuilder();
        List<Map<String, Object>> pesananListRaw = (List<Map<String, Object>>) pesananDoc.get("pesanan");
        if (pesananListRaw != null) {
            for (Map<String, Object> item : pesananListRaw) {
                pesananList.append("• ").append(item.get("namaPesanan"))
                        .append(" x").append(getInt(item.get("jumlah")))
                        .append(" = Rp ").append(rp(getInt(item.get("totalPesanan")))).append("\n");
            }
        }

        String message = "⚠️ *PEMBATALAN PESANAN OLEH USER*\n\n" +
                "📌 *Nama Pemesan*: " + txtNamaPemesan.getText().toString() + "\n" +
                "📞 *Nomor Telepon*: " + txtNomorTelepon.getText().toString() + "\n" +
                "📅 *Waktu Pemesanan*: " + txtWaktuPesanan.getText().toString() + "\n" +
                "📦 *Metode Pembayaran*: " + txtMetodePembayaran.getText().toString() + "\n" +
                "🏠 *Alamat Pengiriman*: " + txtAlamatPengiriman.getText().toString() + "\n" +
                "📊 *Status Pesanan*: *" + status + "*\n\n" +
                "📋 *Daftar Pesanan*:\n" + pesananList.toString() + "\n" +
                "🚚 *Ongkos Pengiriman*: " + txtHargaOngkir.getText().toString() + "\n" +
                "💰 *Total*: " + txtTotal.getText().toString() + "\n\n" +
                "User telah membatalkan pesanan secara mandiri.\n" +
                "Silakan periksa di aplikasi admin.";

        //  HAPUS SPASI BERLEBIH
        String url = "https://wa.me/+6285717959668?text=" + Uri.encode(message);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setPackage("com.whatsapp");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        try {
            getApplicationContext().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal buka WhatsApp: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     *  Kirim notifikasi WhatsApp ke user saat status pesanan berubah
     */
    private void sendNotificationToUser(String status) {
        firestore.collection("pesanan").document(pesananId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String email = doc.getString("Email");
                        if (email != null) {
                            firestore.collection("users")
                                    .whereEqualTo("Email", email)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            String phone = querySnapshot.getDocuments().get(0).getString("NoWhatsapp");
                                            if (phone != null && !phone.isEmpty()) {
                                                if (phone.startsWith("0")) {
                                                    phone = "+62" + phone.substring(1);
                                                }

                                                String message = "📦 *NOTIFIKASI PESANAN*\n\n" +
                                                        "Halo pelanggan,\n" +
                                                        "Pesanan Anda telah diupdate ke status: *" + status + "*.\n\n" +
                                                        "Terima kasih telah berbelanja di Kepengen Dessbox! 🧁";

                                                String url = "https://wa.me/" + phone + "?text=" + Uri.encode(message);
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(url));

                                                //  Buka WhatsApp di task terpisah
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pesananId != null) {
            loadPesananData(pesananId); // Muat ulang pesanan & user data
        }
    }
}