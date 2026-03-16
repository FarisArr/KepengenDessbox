// DessertAdapter.java berhasil
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

public class DessertAdapter extends RecyclerView.Adapter<DessertAdapter.ViewHolder> {

    private ArrayList<Dessert> listDessert;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Dessert dessert);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public DessertAdapter(ArrayList<Dessert> listDessert) {
        this.listDessert = listDessert;
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
        Dessert dessert = listDessert.get(position);
        holder.txtNamaDessert.setText(dessert.getNama());
        holder.txtHargaDessert.setText(formatRupiah(dessert.getHarga()));


        // Gunakan Glide untuk tampilkan gambar dari URL (Firebase Storage atau drawable)
        Glide.with(holder.itemView.getContext())
                .load(dessert.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imgDessert);


        //  Gabungkan rating dan jumlah ulasan dalam satu string
        String ratingStr = String.format("%.1f", dessert.getTotalRating());
        String jumlahUlasanStr = " (" + dessert.getJumlahUlasan() + " Ulasan)";
        holder.txtRating.setText(ratingStr + jumlahUlasanStr);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && dessert != null) {
                listener.onItemClick(dessert);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listDessert.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNamaDessert, txtHargaDessert, txtRating;
        public ImageView imgDessert;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaDessert = itemView.findViewById(R.id.txtNamaItem);
            txtHargaDessert = itemView.findViewById(R.id.txtHargaItem);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgDessert = itemView.findViewById(R.id.imgItem);
        }
    }

    // Update list untuk filter
    public void updateList(ArrayList<Dessert> filteredList) {
        listDessert.clear();
        listDessert.addAll(filteredList);
        notifyDataSetChanged();
    }
}