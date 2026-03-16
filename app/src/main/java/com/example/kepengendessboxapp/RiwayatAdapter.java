// RiwayatAdapter.java berhasil
package com.example.kepengendessboxapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RiwayatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BULAN = 0;
    private static final int TYPE_PESANAN = 1;

    private List<Map<String, Object>> dataList;
    private Context context;

    public RiwayatAdapter(List<Map<String, Object>> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    public void updateList(List<Map<String, Object>> newDataList) {
        this.dataList.clear();
        this.dataList.addAll(newDataList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, Object> item = dataList.get(position);
        if (item.containsKey("type") && "bulan".equals(item.get("type"))) {
            return TYPE_BULAN;
        }
        return TYPE_PESANAN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_BULAN) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_riwayat_bulan, parent, false);
            return new BulanViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_riwayat_pesanan, parent, false);
            return new PesananViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, Object> item = dataList.get(position);
        if (holder instanceof BulanViewHolder) {
            ((BulanViewHolder) holder).bind(item);
        } else {
            ((PesananViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ViewHolder untuk Bulan
    static class BulanViewHolder extends RecyclerView.ViewHolder {
        TextView txtBulan, txtJumlahItem;

        BulanViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBulan = itemView.findViewById(R.id.txtBulan);
            txtJumlahItem = itemView.findViewById(R.id.txtJumlahItem);
        }

        void bind(Map<String, Object> item) {
            String bulan = (String) item.get("bulan");
            txtBulan.setText(bulan);

            Object jumlahObj = item.get("jumlahItem");
            if (jumlahObj instanceof Integer) {
                txtJumlahItem.setText(((Integer) jumlahObj) + " Pesanan");
            } else if (jumlahObj instanceof Long) {
                txtJumlahItem.setText(((Long) jumlahObj) + " Pesanan");
            } else {
                txtJumlahItem.setText("0 Pesanan");
            }
        }
    }

    // ViewHolder untuk Pesanan
    static class PesananViewHolder extends RecyclerView.ViewHolder {
        TextView txtNamaPesanan, txtTanggal;
        View llItemRiwayat;

        PesananViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaPesanan = itemView.findViewById(R.id.txtNamaPesanan);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            llItemRiwayat = itemView.findViewById(R.id.llItemRiwayat);
        }

        void bind(Map<String, Object> item) {
            // Tampilkan nama pesanan (sudah berisi "3 Item")
            String namaPesanan = (String) item.get("namaPesanan");
            if (namaPesanan != null) {
                txtNamaPesanan.setText(namaPesanan);
            } else {
                txtNamaPesanan.setText("1 Item");
            }

            // Format tanggal
            String tanggal = (String) item.get("tanggal");
            if (tanggal != null && !tanggal.isEmpty()) {
                txtTanggal.setText(formatTanggal(tanggal));
            } else {
                txtTanggal.setText("Tanggal tidak tersedia");
            }

            // Klik item → buka DetailPesananActivity
            llItemRiwayat.setOnClickListener(v -> {
                String pesananId = (String) item.get("pesanan_id");
                if (pesananId != null && !pesananId.isEmpty()) {
                    Intent intent = new Intent(v.getContext(), DetailPesananActivity.class);
                    intent.putExtra("pesanan_id", pesananId);
                    v.getContext().startActivity(intent);
                } else {
                    // Jika ID tidak valid
                    android.widget.Toast.makeText(v.getContext(), "ID pesanan tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Konversi objek ke int aman
        private int getInt(Object value) {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            }
            return 0;
        }

        // Format tanggal ke Indonesia
        private String formatTanggal(String dateStr) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH);
                SimpleDateFormat output = new SimpleDateFormat("dd MMMM yyyy | HH:mm", Locale.forLanguageTag("id-ID"));
                Date date = input.parse(dateStr);
                return output.format(date);
            } catch (Exception e) {
                return dateStr;
            }
        }
    }
}