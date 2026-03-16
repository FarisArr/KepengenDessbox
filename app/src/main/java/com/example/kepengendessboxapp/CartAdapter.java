// CartAdapter.java Berhasil
package com.example.kepengendessboxapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private ArrayList<CartItem> cartItemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onAddQuantity(int position);
        void onSubtractQuantity(int position);
        void onRemoveItem(int position);
    }

    public CartAdapter(ArrayList<CartItem> cartItemList, OnItemClickListener listener) {
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_records, parent, false);
        return new CartViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem currentItem = cartItemList.get(position);

        //  Ganti dari setImageResource() → Glide untuk tampilkan gambar dari URL
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getImageUrl()) // String: bisa dari drawable URI atau Firebase Storage
                .placeholder(R.drawable.placeholder) // Gambar sementara saat loading
                .error(R.drawable.placeholder) // Gambar jika error
                .into(holder.imgItemPesanan);

        // Tetap tampilkan data lainnya
        holder.txtPesanan.setText(currentItem.getNamaMenu());
        holder.txtHarga.setText(rp(currentItem.getHarga()));
        holder.txtJmlPesanan.setText(String.valueOf(currentItem.getJumlah()));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    private String rp(int amount) {
        Locale locale = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setMaximumFractionDigits(0);
        return format.format(amount).replace("Rp", "Rp ").replace(",", ".");
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgItemPesanan;
        public TextView txtPesanan;
        public TextView txtHarga;
        public TextView txtJmlPesanan;
        public Button btnKurang;
        public Button btnTambah;
        public Button btnHapus;

        public CartViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imgItemPesanan = itemView.findViewById(R.id.imgItemPesanan);
            txtPesanan = itemView.findViewById(R.id.txt_pesanan);
            txtHarga = itemView.findViewById(R.id.txt_harga);
            txtJmlPesanan = itemView.findViewById(R.id.txt_jml_pesanan);
            btnKurang = itemView.findViewById(R.id.btn_kurang);
            btnTambah = itemView.findViewById(R.id.btn_tambah);
            btnHapus = itemView.findViewById(R.id.btn_hapus);

            btnKurang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSubtractQuantity(position);
                        }
                    }
                }
            });

            btnTambah.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onAddQuantity(position);
                        }
                    }
                }
            });

            btnHapus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onRemoveItem(position);
                        }
                    }
                }
            });
        }
    }
}
