// MinumanAdapter.java berhasil
package com.example.kepengendessboxapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;
import java.text.NumberFormat;

public class MinumanAdapter extends RecyclerView.Adapter<MinumanAdapter.ViewHolder> {

    private ArrayList<Minuman> listMinuman;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Minuman minuman);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MinumanAdapter(ArrayList<Minuman> listMinuman) {
        this.listMinuman = listMinuman;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    // Format harga ke Rupiah
    public static String formatRupiah(int amount) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        return format.format(amount).replace("Rp", "Rp ");
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Minuman minuman = listMinuman.get(position);
        holder.txtNamaMinuman.setText(minuman.getNama());
        holder.txtHargaMinuman.setText(formatRupiah(minuman.getHarga()));

        // Gunakan Glide untuk tampilkan gambar dari URL (Firebase Storage atau drawable)
        Glide.with(holder.itemView.getContext())
                .load(minuman.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imgMinuman);


        //  Gabungkan rating dan jumlah ulasan dalam satu string
        String ratingStr = String.format("%.1f", minuman.getTotalRating());
        String jumlahUlasanStr = " (" + minuman.getJumlahUlasan() + " Ulasan)";
        holder.txtRating.setText(ratingStr + jumlahUlasanStr);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && minuman != null) {
                listener.onItemClick(minuman);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listMinuman.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNamaMinuman, txtHargaMinuman, txtRating;
        public ImageView imgMinuman;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaMinuman = itemView.findViewById(R.id.txtNamaItem);
            txtHargaMinuman = itemView.findViewById(R.id.txtHargaItem);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgMinuman = itemView.findViewById(R.id.imgItem);
        }
    }

    // Update list untuk filter
    public void updateList(ArrayList<Minuman> filteredList) {
        listMinuman.clear();
        listMinuman.addAll(filteredList);
        notifyDataSetChanged();
    }
}