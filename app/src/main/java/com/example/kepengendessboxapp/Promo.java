// Promo.java berhasil
package com.example.kepengendessboxapp;

import com.google.firebase.firestore.PropertyName;

public class Promo implements AdminProdukAdapter.Produk {

    @PropertyName("nama")
    private String namaPromo;

    @PropertyName("harga")
    private int hargaPromo;

    @PropertyName("imageUrl")
    private String imageUrl;

    @PropertyName("deskripsi")
    private String deskripsiPromo;

    //  Tambahkan field rating
    @PropertyName("totalRating")
    private double totalRating = 0.0;

    @PropertyName("jumlahUlasan")
    private int jumlahUlasan = 0;

    private boolean isBawaan = true;
    private String documentId;

    //  Konstruktor kosong (untuk Firestore)
    public Promo() {
        // Diperlukan agar Firestore bisa membuat objek
    }

    //  Constructor untuk produk bawaan
    public Promo(String namaPromo, int hargaPromo, String imageUrl, String deskripsiPromo) {
        this.namaPromo = namaPromo;
        this.hargaPromo = hargaPromo;
        this.imageUrl = imageUrl;
        this.deskripsiPromo = deskripsiPromo;
        this.totalRating = 0.0;
        this.jumlahUlasan = 0;
        this.isBawaan = true;
        this.documentId = null;
    }

    //  Constructor untuk produk dari Firestore
    public Promo(String namaPromo, int hargaPromo, String imageUrl, String deskripsiPromo, String documentId) {
        this.namaPromo = namaPromo;
        this.hargaPromo = hargaPromo;
        this.imageUrl = imageUrl;
        this.deskripsiPromo = deskripsiPromo;
        this.totalRating = 0.0;
        this.jumlahUlasan = 0;
        this.isBawaan = false;
        this.documentId = documentId;
    }

    //  Getter & Setter
    public boolean isBawaan() { return isBawaan; }
    public void setIsBawaan(boolean bawaan) { isBawaan = bawaan; }
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    //  Implement interface Produk
    @Override
    public String getNama() {
        return namaPromo;
    }

    @Override
    public int getHarga() {
        return hargaPromo;
    }

    @Override
    public String getDeskripsi() {
        return deskripsiPromo;
    }

    // Getter & Setter lainnya
    public String getNamaPromo() {
        return namaPromo;
    }

    public void setNamaPromo(String namaPromo) {
        this.namaPromo = namaPromo;
    }

    public int getHargaPromo() {
        return hargaPromo;
    }

    public void setHargaPromo(int hargaPromo) {
        this.hargaPromo = hargaPromo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDeskripsiPromo() {
        return deskripsiPromo;
    }

    public void setDeskripsiPromo(String deskripsiPromo) {
        this.deskripsiPromo = deskripsiPromo;
    }

    public double getTotalRating() { return totalRating; }

    public void setTotalRating(double totalRating) { this.totalRating = totalRating; }

    public int getJumlahUlasan() { return jumlahUlasan; }

    public void setJumlahUlasan(int jumlahUlasan) { this.jumlahUlasan = jumlahUlasan; }
}