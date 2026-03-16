//PromoAdapter.java berhasil
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

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {
    private ArrayList<Promo> listPromo;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Promo promo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PromoAdapter(ArrayList<Promo> listPromo) {
        this.listPromo = listPromo;
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
        Promo promo = listPromo.get(position);
        holder.txtNamaPromo.setText(promo.getNama());
        holder.txtHargaPromo.setText(formatRupiah(promo.getHarga()));

        // Gunakan Glide untuk tampilkan gambar dari URL (Firebase Storage atau drawable)
        Glide.with(holder.itemView.getContext())
                .load(promo.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imgPromo);

        //  Gabungkan rating dan jumlah ulasan dalam satu string
        String ratingStr = String.format("%.1f", promo.getTotalRating());
        String jumlahUlasanStr = " (" + promo.getJumlahUlasan() + " Ulasan)";
        holder.txtRating.setText(ratingStr + jumlahUlasanStr);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && promo != null) {
                listener.onItemClick(promo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPromo.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNamaPromo, txtHargaPromo, txtRating;
        public ImageView imgPromo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaPromo = itemView.findViewById(R.id.txtNamaItem);
            txtHargaPromo = itemView.findViewById(R.id.txtHargaItem);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgPromo = itemView.findViewById(R.id.imgItem);
        }
    }

    // Update list untuk filter
    public void updateList(ArrayList<Promo> filteredList) {
        listPromo.clear();
        listPromo.addAll(filteredList);
        notifyDataSetChanged();
    }
}