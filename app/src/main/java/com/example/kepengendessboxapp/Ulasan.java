// Ulasan.java Berhasil
package com.example.kepengendessboxapp;

import com.google.firebase.firestore.PropertyName;

public class Ulasan {
    @PropertyName("namaProduk")
    private String namaProduk;

    @PropertyName("rating")
    private Long rating;

    @PropertyName("review")
    private String review;

    @PropertyName("namaUser")
    private String namaUser;

    @PropertyName("timestamp")
    private Long timestamp;

    //  Field tambahan: totalRating & jumlahUlasan
    private double totalRating = 0.0;
    private int jumlahUlasan = 0;

    // Konstruktor kosong (untuk Firestore)
    public Ulasan() {}

    // Constructor dengan parameter
    public Ulasan(String namaProduk, Long rating, String review, String namaUser, Long timestamp) {
        this.namaProduk = namaProduk;
        this.rating = rating;
        this.review = review;
        this.namaUser = namaUser;
        this.timestamp = timestamp;
    }

    // Getter & Setter
    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }

    public Long getRating() { return rating; }
    public void setRating(Long rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public String getNamaUser() { return namaUser; }
    public void setNamaUser(String namaUser) { this.namaUser = namaUser; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    //  Getter & Setter untuk totalRating
    public double getTotalRating() { return totalRating; }
    public void setTotalRating(double totalRating) { this.totalRating = totalRating; }

    //  Getter & Setter untuk jumlahUlasan
    public int getJumlahUlasan() { return jumlahUlasan; }
    public void setJumlahUlasan(int jumlahUlasan) { this.jumlahUlasan = jumlahUlasan; }

    // Format timestamp
    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        return new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date(timestamp));
    }
}