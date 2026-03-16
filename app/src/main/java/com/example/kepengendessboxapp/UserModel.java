// UserModel.java
package com.example.kepengendessboxapp;

public class UserModel {
    private String NamaLengkap;
    private String NoWhatsapp;
    private String Email;

    public UserModel() {}

    public UserModel(String namaLengkap, String noWa, String email) {
        this.NamaLengkap = namaLengkap;
        this.NoWhatsapp = noWa;
        this.Email = email;
    }

    // Getter & Setter
    public String getNamaLengkap() { return NamaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.NamaLengkap = namaLengkap; }

    public String getNoWhatsapp() { return NoWhatsapp; }
    public void setNoWhatsapp(String noWa) { this.NoWhatsapp = noWa; }

    public String getEmail() { return Email; }
    public void setEmail(String email) { this.Email = email; }
}