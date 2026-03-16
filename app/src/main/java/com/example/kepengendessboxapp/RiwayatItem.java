// RiwayatItem.java berhasil
package com.example.kepengendessboxapp;

public class RiwayatItem {
    private String namaPesanan;
    private int jumlah;
    private int totalPesanan;
    private int imageResId;
    private String email;
    private String tanggalPesanan;

    public RiwayatItem(String namaPesanan, int jumlah, int totalPesanan, int imageResId, String email, String tanggalPesanan) {
        this.namaPesanan = namaPesanan;
        this.jumlah = jumlah;
        this.totalPesanan = totalPesanan;
        this.imageResId = imageResId;
        this.email = email;
        this.tanggalPesanan = tanggalPesanan;
    }

    // Getter
    public String getNamaPesanan() { return namaPesanan; }
    public int getJumlah() { return jumlah; }
    public int getTotalPesanan() { return totalPesanan; }
    public int getImageResId() { return imageResId; }
    public String getEmail() { return email; }
    public String getTanggalPesanan() { return tanggalPesanan; }
}