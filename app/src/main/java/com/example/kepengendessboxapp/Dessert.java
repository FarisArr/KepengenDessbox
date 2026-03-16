// Dessert.java berhasil
package com.example.kepengendessboxapp;

import com.google.firebase.firestore.PropertyName;

public class Dessert implements AdminProdukAdapter.Produk {

    @PropertyName("nama")
    private String namaDessert;

    @PropertyName("harga")
    private int hargaDessert;

    @PropertyName("imageUrl")
    private String imageUrl;

    @PropertyName("deskripsi")
    private String deskripsiDessert;

    // 🔹 Tambahkan field rating
    @PropertyName("totalRating")
    private double totalRating = 0.0;

    @PropertyName("jumlahUlasan")
    private int jumlahUlasan = 0;

    private boolean isBawaan = true;
    private String documentId;

    //  Konstruktor kosong (untuk Firestore)
    public Dessert() {
        // Diperlukan agar Firestore bisa membuat objek
    }

    //  Constructor untuk produk bawaan
    public Dessert(String namaDessert, int hargaDessert, String imageUrl, String deskripsiDessert) {
        this.namaDessert = namaDessert;
        this.hargaDessert = hargaDessert;
        this.imageUrl = imageUrl;
        this.deskripsiDessert = deskripsiDessert;
        this.totalRating = 0.0;
        this.jumlahUlasan = 0;
        this.isBawaan = true;
        this.documentId = null;
    }

    //  Constructor untuk produk dari Firestore
    public Dessert(String namaDessert, int hargaDessert, String imageUrl, String deskripsiDessert, String documentId) {
        this.namaDessert = namaDessert;
        this.hargaDessert = hargaDessert;
        this.imageUrl = imageUrl;
        this.deskripsiDessert = deskripsiDessert;
        this.totalRating = 0.0;
        this.jumlahUlasan = 0;
        this.isBawaan = false;
        this.documentId = documentId;
    }

    //  Getter & Setter
    public boolean isBawaan() {
        return isBawaan;
    }

    public void setBawaan(boolean bawaan) {
        isBawaan = bawaan;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    //  Implement interface Produk
    @Override
    public String getNama() {
        return namaDessert;
    }

    @Override
    public int getHarga() {
        return hargaDessert;
    }

    @Override
    public String getDeskripsi() {
        return deskripsiDessert;
    }

    // Getter & Setter lainnya
    public String getNamaDessert() {
        return namaDessert;
    }

    public void setNamaDessert(String namaDessert) {
        this.namaDessert = namaDessert;
    }

    public int getHargaDessert() {
        return hargaDessert;
    }

    public void setHargaDessert(int hargaDessert) {
        this.hargaDessert = hargaDessert;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDeskripsiDessert() {
        return deskripsiDessert;
    }

    public void setDeskripsiDessert(String deskripsiDessert) {
        this.deskripsiDessert = deskripsiDessert;
    }

    public double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
    }

    public int getJumlahUlasan() {
        return jumlahUlasan;
    }

    public void setJumlahUlasan(int jumlahUlasan) {
        this.jumlahUlasan = jumlahUlasan;
    }
}