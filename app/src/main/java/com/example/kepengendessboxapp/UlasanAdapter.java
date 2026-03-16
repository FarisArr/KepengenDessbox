// UlasanAdapter.java berhasil
package com.example.kepengendessboxapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UlasanAdapter extends RecyclerView.Adapter<UlasanAdapter.ViewHolder> {
    private ArrayList<Ulasan> listUlasan;

    public UlasanAdapter(ArrayList<Ulasan> listUlasan) {
        this.listUlasan = listUlasan;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ulasan_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ulasan ulasan = listUlasan.get(position);
        holder.txtNamaProduk.setText(ulasan.getNamaProduk());
        holder.txtReview.setText(ulasan.getReview());
        holder.txtNamaUser.setText("— " + ulasan.getNamaUser());
        holder.txtTimestamp.setText(ulasan.getFormattedTimestamp());
        holder.txtRatingUlasan.setText(String.valueOf(ulasan.getRating()));
    }

    @Override
    public int getItemCount() {
        return listUlasan.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNamaProduk, txtRatingUlasan, txtReview, txtNamaUser, txtTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaProduk = itemView.findViewById(R.id.txtNamaProduk);
            txtRatingUlasan = itemView.findViewById(R.id.txtRatingUlasan);
            txtReview = itemView.findViewById(R.id.txtReview);
            txtNamaUser = itemView.findViewById(R.id.txtNamaUser);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
        }
    }
}