// PesananModel.java berhasil
package com.example.kepengendessboxapp;

import java.util.List;
import java.util.Map;

public class PesananModel {
    private String NamaLengkap;
    private String Alamat;
    private String Email;
    private List<Map<String, Object>> pesanan;
    private long Ongkir;
    private long totalHarga;
    private String tanggal;

    public PesananModel() {}

    // Getter dan Setter
    public String getNamaLengkap() { return NamaLengkap; }
    public void setNamaLengkap(String namaLengkap) { NamaLengkap = namaLengkap; }

    public String getAlamat() { return Alamat; }
    public void setAlamat(String alamat) { Alamat = alamat; }

    public String getEmail() { return Email; }
    public void setEmail(String email) { Email = email; }

    public List<Map<String, Object>> getPesanan() { return pesanan; }
    public void setPesanan(List<Map<String, Object>> pesanan) { this.pesanan = pesanan; }

    public long getOngkir() { return Ongkir; }
    public void setOngkir(long ongkir) { Ongkir = ongkir; }

    public long getTotalHarga() { return totalHarga; }
    public void setTotalHarga(long totalHarga) { this.totalHarga = totalHarga; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
}