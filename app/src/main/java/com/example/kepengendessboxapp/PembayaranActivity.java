// PembayaranActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PembayaranActivity extends AppCompatActivity {

    private TextView txtMetode, txtVaNumber, txtTotalBayar, txtBatasWaktu, txtInstruksi;
    private Button btnSalin, btnOk;
    private CheckBox cbSudahBayar;
    // Dropdown views
    private LinearLayout layoutInstruksi;
    private LinearLayout layoutJudulInstruksi;
    private ImageView imgArrow;

    // Nomor VA untuk masing-masing bank
    private final String VA_BCA = "3901085717959668";
    private final String VA_MANDIRI = "89508085717959668";
    private final String VA_BRI = "88810085717959668";
    private final String VA_BNI = "8810085717959668";

    private CountDownTimer countDownTimer;
    private String pesananId;
    private long savedDeadline;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembayaran);

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance();

        // Inisialisasi View
        initViews();

        // Ambil data dari Intent
        String metodePembayaran = getIntent().getStringExtra("metode");
        long totalBayar = getIntent().getLongExtra("total", 0);
        pesananId = getIntent().getStringExtra("pesanan_id");

        if (pesananId == null || pesananId.isEmpty()) {
            Toast.makeText(this, "ID pesanan tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tampilkan data
        setupPaymentDetails(metodePembayaran, totalBayar);

        // Setup tombol
        setupClickListeners();

        // Setup dropdown
        setupExpandableInstructions();

        //  Muat deadline dari Firestore
        loadDeadline();
    }

    private void initViews() {
        txtMetode = findViewById(R.id.txtMetode);
        txtVaNumber = findViewById(R.id.txtVaNumber);
        txtTotalBayar = findViewById(R.id.txtTotalBayar);
        txtBatasWaktu = findViewById(R.id.txtBatasWaktu);
        txtInstruksi = findViewById(R.id.txtInstruksi);

        // Dropdown
        layoutInstruksi = findViewById(R.id.layoutInstruksi);
        layoutJudulInstruksi = findViewById(R.id.layoutJudulInstruksi);
        imgArrow = findViewById(R.id.imgArrow);

        // Tombol
        btnSalin = findViewById(R.id.btnSalin);
        btnOk = findViewById(R.id.btnOK);

        //  Inisialisasi CheckBox
        cbSudahBayar = findViewById(R.id.cbSudahBayar);
    }

    private void setupPaymentDetails(String metode, long total) {
        txtMetode.setText(metode);
        txtTotalBayar.setText("Rp " + rp(total));

        // Tentukan nomor VA dan instruksi
        String vaNumber = "";
        String instruksi = "";

        if (metode.equals("BCA Virtual Account")) {
            vaNumber = VA_BCA;
            instruksi = "1. Masuk ke aplikasi mobile m-BCA\n" +
                    "2. Pilih menu M-TRANSFER > BCA VIRTUAL ACCOUNT\n" +
                    "3. Masukkan " + vaNumber + " sebagai rekening tujuan\n" +
                    "4. Masukkan jumlah yang harus dibayar\n" +
                    "5. Masukkan PIN m-BCA Anda\n" +
                    "6. Ikuti instruksi untuk menyelesaikan transaksi";

        } else if (metode.equals("Mandiri Virtual Account")) {
            vaNumber = VA_MANDIRI;
            instruksi = "1. Login ke aplikasi Livin' by Mandiri\n" +
                    "2. Pilih menu Pembayaran > Buat Pembayaran Baru > Multi Payment\n" +
                    "3. Pilih rekening sumber anda pada Rekening Sumber. Kemudian, pilih Danatopup sebagai Penyedia Jasa, masukkan " + vaNumber + " ke dalam kolom No VA dan tekan TAMBAH SEBAGAI NOMOR BARU dan tekan LANJUT\n" +
                    "4. Konfirmasi transaksi dengan memasukkan PIN Livin' by Mandiri anda";

        } else if (metode.equals("BRI Virtual Account")) {
            vaNumber = VA_BRI;
            instruksi = "1. Login BRIMO terlebih dahulu\n" +
                    "2. Pilih Menu Dompet Digital atau E-Wallet\n" +
                    "3. Jika nomor ponsel belum ada di Daftar Tersimpan, maka kamu bisa pilih Tambah Daftar Baru\n" +
                    "4. Pilih Jenis Wallet, pilih DANA, selanjutnya masukkan nomor Virtual Account yang akan di transfer " + vaNumber + " sebagai nomor VA\n" +
                    "5. Aplikasi BRIMO akan mengeluarkan informasi nama pengguna DANA dan nomor ponsel. Pastikan data tersebut sudah benar.\n" +
                    "6. Selanjutnya masukkan nominal tagihan dan pilih sumber dana. Jika sudah sesuai, pilih Konfirmasi\n" +
                    "7. Kemudia aplikasi BRIMO akan menampilkan halaman konfirmasi data dan detail transaksi. Pastikan semua data yang ditampilkan sudah benar. Jika sudah benar, pilih Konfirmasi.\n" +
                    "8. Masukkan PIN BRIMO. Pastikan PIN BRIMO yang dimasukkan telah sesuai\n" +
                    "9. Transaksi berhasil\n";

        } else if (metode.equals("BNI Virtual Account")) {
            vaNumber = VA_BNI;
            instruksi = "1. Login ke aplikasi BNI Mobile Banking\n" +
                    "2. Pilih TRANSFER > VIRTUAL ACCOUNT BILLING\n" +
                    "3. Pilih tab Input Baru. Kemudian, masukkan " + vaNumber + "\n" +
                    "4. Masukkan jumlah yang harus di bayar. Kemudian, tekan Lanjut\n" +
                    "5. Konfirmasi transaksi anda dengan memasukkan password mobile banking anda\n";
        }

        txtVaNumber.setText(vaNumber);
        txtInstruksi.setText(instruksi);
    }

    private void setupExpandableInstructions() {
        layoutJudulInstruksi.setOnClickListener(v -> {
            if (txtInstruksi.getVisibility() == View.GONE) {
                txtInstruksi.setVisibility(View.VISIBLE);
                imgArrow.setImageResource(R.drawable.baseline_expand_more_24); // panah bawah
            } else {
                txtInstruksi.setVisibility(View.GONE);
                imgArrow.setImageResource(R.drawable.baseline_chevron_right_24); // panah kanan
            }
        });
    }

    //  Muat deadline dari Firestore
    private void loadDeadline() {
        firestore.collection("pesanan").document(pesananId)
                .get()
                .addOnSuccessListener(this::startCountdownTimer)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal muat waktu", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    //  Mulai hitung mundur dari sisa waktu
    private void startCountdownTimer(DocumentSnapshot doc) {
        savedDeadline = doc.getLong("deadline");
        long timeRemaining = savedDeadline - System.currentTimeMillis();

        if (timeRemaining <= 0) {
            txtBatasWaktu.setText("Waktu pembayaran habis");
            btnSalin.setEnabled(false);
            txtInstruksi.setText("Pembayaran tidak dapat diproses karena waktu telah habis.");
            imgArrow.setImageResource(R.drawable.baseline_chevron_right_24);
            Toast.makeText(this, "Waktu pembayaran habis", Toast.LENGTH_SHORT).show();
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
                btnSalin.setEnabled(false);
                txtInstruksi.setText("Pembayaran tidak dapat diproses karena waktu telah habis.");
                imgArrow.setImageResource(R.drawable.baseline_chevron_right_24);
                Toast.makeText(PembayaranActivity.this, "Waktu pembayaran habis", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void setupClickListeners() {
        btnSalin.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Nomor Virtual Account", txtVaNumber.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Nomor VA disalin!", Toast.LENGTH_SHORT).show();
        });

        btnOk.setOnClickListener(v -> {
            //  Tentukan status berdasarkan CheckBox
            String status = cbSudahBayar.isChecked() ? "Menunggu Konfirmasi" : "Menunggu Pembayaran";

            // Update status di Firestore
            firestore.collection("pesanan").document(pesananId)
                    .update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Status: " + status, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal update status", Toast.LENGTH_SHORT).show();
                    });

            // Pindah ke Riwayat
            Intent intent = new Intent(PembayaranActivity.this, RiwayatPesananActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Tutup Pembayaran
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    //  Format Rupiah konsisten dengan CheckoutActivity
    public String rp(long amount) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        return format.format(amount).replace("Rp", "").trim();
    }
}