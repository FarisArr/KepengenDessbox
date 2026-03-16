// AdminProdukAdapter.java Berhasil
package com.example.kepengendessboxapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdminProdukAdapter extends RecyclerView.Adapter<AdminProdukAdapter.ViewHolder> {

    public interface Produk {
        String getNama();
        int getHarga();
        String getImageUrl();
        String getDeskripsi();
    }

    private ArrayList<Produk> listProduk;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEdit(Produk produk);
    }

    public interface OnDeleteClickListener {
        void onDelete(Produk produk);
    }

    public AdminProdukAdapter(ArrayList<Produk> listProduk,
                              OnEditClickListener editListener,
                              OnDeleteClickListener deleteListener) {
        this.listProduk = listProduk;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Produk produk = listProduk.get(position);
        holder.txtNama.setText(produk.getNama());
        holder.txtHarga.setText("Rp " + rp(produk.getHarga()));

        //  Gunakan Glide untuk load gambar dari imageUrl
        if (produk.getImageUrl() != null && !produk.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(produk.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProduk);
        } else {
            holder.imgProduk.setImageResource(R.drawable.placeholder);
        }

        holder.btnEdit.setOnClickListener(v -> editListener.onEdit(produk));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(produk));
    }

    @Override
    public int getItemCount() {
        return listProduk.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtHarga;
        ImageView imgProduk;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtHarga = itemView.findViewById(R.id.txtHarga);
            imgProduk = itemView.findViewById(R.id.imgProduk);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Format Rupiah
    public String rp(int amount) {
        java.util.Locale localeID = new java.util.Locale("in", "ID");
        java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        return format.format(amount).replace("Rp", "").trim();
    }
}