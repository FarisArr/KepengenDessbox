// LaporanPenjualanActivity.java berhasil
package com.example.kepengendessboxapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LaporanPenjualanActivity extends AppCompatActivity {

    private TextView txtTotalPendapatan, txtTotalPesanan;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_penjualan);

        // Inisialisasi View
        initViews();

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance();

        // Muat data laporan
        loadSalesReport();
    }

    private void initViews() {
        txtTotalPendapatan = findViewById(R.id.txtTotalPendapatan);
        txtTotalPesanan = findViewById(R.id.txtTotalPesanan);

        //  Validasi view tidak null
        if (txtTotalPendapatan == null) {
            Log.e("Laporan", "txtTotalPendapatan tidak ditemukan di layout!");
            Toast.makeText(this, "Error: UI tidak lengkap", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (txtTotalPesanan == null) {
            Log.e("Laporan", "txtTotalPesanan tidak ditemukan di layout!");
            Toast.makeText(this, "Error: UI tidak lengkap", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void loadSalesReport() {
        firestore.collection("pesanan")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Gagal: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        processSalesData(querySnapshot);
                    }
                });
    }

    private void processSalesData(QuerySnapshot queryDocumentSnapshots) {
        long totalPendapatan = 0;
        int totalPesanan = 0;

        Log.d("Laporan", "Jumlah dokumen: " + queryDocumentSnapshots.size());

        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
            String id = doc.getId();
            String metode = doc.getString("metodePembayaran");
            String status = doc.getString("status");
            Long totalHarga = doc.getLong("totalHarga");

            Log.d("Laporan", "ID: " + id);
            Log.d("Laporan", "Metode: '" + metode + "'");
            Log.d("Laporan", "Status: '" + status + "'");
            Log.d("Laporan", "Total Harga: " + totalHarga);

            if (metode == null || status == null || totalHarga == null) {
                Log.d("Laporan", "⚠️ Dilewati: field null");
                continue;
            }

            //  1. COD: hanya hitung jika "Pesanan Selesai"
            if ("COD (Bayar di Tempat)".equals(metode)) {
                if ("Pesanan Selesai".equals(status)) {
                    totalPendapatan += totalHarga;
                    totalPesanan++;
                    Log.d("Laporan", "✅ Tambahkan COD: " + totalHarga);
                }
            }
            //  2. Transfer: hitung jika status sudah lunas (Diproses, Dikirim, Selesai)
            else if (metode.contains("Virtual Account")) {
                if ("Pesanan Diproses".equals(status) ||
                        "Pesanan Dikirim".equals(status) ||
                        "Pesanan Selesai".equals(status)) {
                    totalPendapatan += totalHarga;
                    totalPesanan++;
                    Log.d("Laporan", "✅ Tambahkan Transfer: " + totalHarga + " (Status: " + status + ")");
                }
            }
        }

        Log.d("Laporan", "📊 FINAL - Total Pendapatan: " + totalPendapatan);
        Log.d("Laporan", "📊 FINAL - Total Pesanan: " + totalPesanan);

        // Update UI
        if (txtTotalPendapatan != null) {
            txtTotalPendapatan.setText("Rp " + rp(totalPendapatan));
        } else {
            Log.e("Laporan", "txtTotalPendapatan is null");
        }

        if (txtTotalPesanan != null) {
            txtTotalPesanan.setText(totalPesanan + " pesanan");
        } else {
            Log.e("Laporan", "txtTotalPesanan is null");
        }
    }

    // Format Rupiah
    public String rp(long amount) {
        return java.text.NumberFormat.getInstance().format(amount).replace(",", ".");
    }
}