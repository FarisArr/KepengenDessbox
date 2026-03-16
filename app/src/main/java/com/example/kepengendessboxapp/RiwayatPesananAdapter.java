// RiwayatPesananAdapter.java berhasil
package com.example.kepengendessboxapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RiwayatPesananAdapter extends RecyclerView.Adapter<RiwayatPesananAdapter.ViewHolder> {

    private ArrayList<RiwayatItem> riwayatItemList;
    private Context context;

    public RiwayatPesananAdapter(ArrayList<RiwayatItem> riwayatItemList, Context context) {
        this.riwayatItemList = riwayatItemList != null ? riwayatItemList : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_riwayat_pesanan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RiwayatItem item = riwayatItemList.get(position);

        // Jumlah item
        holder.txtNamaPesanan.setText("x" + item.getJumlah() + " Item");

        // Tanggal (dengan validasi)
        String tanggal = item.getTanggalPesanan();
        if (tanggal != null && !tanggal.isEmpty()) {
            holder.txtTanggal.setText(tanggal);
        } else {
            holder.txtTanggal.setText("Tanggal tidak tersedia");
        }

        // Klik item → DetailPesananActivity
        holder.llItemRiwayat.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailPesananActivity.class);
            intent.putExtra("NAMA_PESANAN", item.getNamaPesanan());
            intent.putExtra("JUMLAH", item.getJumlah());
            intent.putExtra("TOTAL", item.getTotalPesanan());
            intent.putExtra("TANGGAL", item.getTanggalPesanan());
            intent.putExtra("EMAIL", item.getEmail());
            intent.putExtra("IMAGE_RES_ID", item.getImageResId());

            context.startActivity(intent);
            ((android.app.Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public int getItemCount() {
        return riwayatItemList.size();
    }

    //  Metode untuk update data
    public void updateList(ArrayList<RiwayatItem> newList) {
        this.riwayatItemList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    //  Metode untuk tambah item
    public void addItem(RiwayatItem item) {
        riwayatItemList.add(item);
        notifyItemInserted(riwayatItemList.size() - 1);
    }

    //  Metode untuk hapus semua item
    public void clear() {
        int size = riwayatItemList.size();
        riwayatItemList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llItemRiwayat;
        TextView txtNamaPesanan;
        TextView txtTanggal;
        ImageView imgArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llItemRiwayat = itemView.findViewById(R.id.llItemRiwayat);
            txtNamaPesanan = itemView.findViewById(R.id.txtNamaPesanan);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            imgArrow = itemView.findViewById(R.id.imgArrow);
        }
    }
}