// Minuman.java berhasil
package com.example.kepengendessboxapp;

import com.google.firebase.firestore.PropertyName;

public class Minuman implements AdminProdukAdapter.Produk {

    @PropertyName("nama")
    private String namaMinuman;

    @PropertyName("harga")
    private int hargaMinuman;

    @PropertyName("imageUrl")
    private String imageUrl;

    @PropertyName("deskripsi")
    private String deskripsiMinuman;

    // 🔹 Tambahkan field rating
    @PropertyName("totalRating")
    private double totalRating = 0.0;

    @PropertyName("jumlahUlasan")
    private int jumlahUlasan = 0;

    private boolean isBawaan = true;
    private String documentId;

    //  Konstruktor kosong (untuk Firestore)
    public Minuman() {
        // Diperlukan agar Firestore bisa membuat objek
    }

    //  Constructor untuk produk bawaan
    public Minuman(String namaMinuman, int hargaMinuman, String imageUrl, String deskripsiMinuman) {
        this.namaMinuman = namaMinuman;
        this.hargaMinuman = hargaMinuman;
        this.imageUrl = imageUrl;
        this.deskripsiMinuman = deskripsiMinuman;
        this.totalRating = 0.0;
        this.jumlahUlasan = 0;
        this.isBawaan = true;
        this.documentId = null;
    }

    //  Constructor untuk produk dari Firestore
    public Minuman(String namaMinuman, int hargaMinuman, String imageUrl, String deskripsiMinuman, String documentId) {
        this.namaMinuman = namaMinuman;
        this.hargaMinuman = hargaMinuman;
        this.imageUrl = imageUrl;
        this.deskripsiMinuman = deskripsiMinuman;
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
        return namaMinuman;
    }

    @Override
    public int getHarga() {
        return hargaMinuman;
    }

    @Override
    public String getDeskripsi() {
        return deskripsiMinuman;
    }

    // Getter & Setter lainnya
    public String getNamaMinuman() {
        return namaMinuman;
    }

    public void setNamaMinuman(String namaMinuman) {
        this.namaMinuman = namaMinuman;
    }

    public int getHargaMinuman() {
        return hargaMinuman;
    }

    public void setHargaMinuman(int hargaMinuman) {
        this.hargaMinuman = hargaMinuman;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDeskripsiMinuman() {
        return deskripsiMinuman;
    }

    public void setDeskripsiMinuman(String deskripsiMinuman) {
        this.deskripsiMinuman = deskripsiMinuman;
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