// CartItem.java Berhasil
package com.example.kepengendessboxapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

// CartItem.java
public class CartItem implements Parcelable {

    private String email;
    private String namaMenu;
    private int harga;
    private int jumlah;
    private String imageUrl;

    public CartItem(String email, String namaMenu, int harga, int jumlah, String imageUrl) {
        this.email = email;
        this.namaMenu = namaMenu;
        this.harga = harga;
        this.jumlah = jumlah;
        this.imageUrl = imageUrl;
    }

    protected CartItem(Parcel in) {
        email = in.readString();
        namaMenu = in.readString();
        harga = in.readInt();
        jumlah = in.readInt();
        imageUrl = in.readString();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    // Getter
    public String getEmail() { return email; }
    public String getNamaMenu() { return namaMenu; }
    public int getHarga() { return harga; }
    public int getJumlah() { return jumlah; }
    public String getImageUrl() { return imageUrl; }

    public void setJumlah(int jumlah) {
        if (jumlah > 0) {
            this.jumlah = jumlah;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(namaMenu);
        dest.writeInt(harga);
        dest.writeInt(jumlah);
        dest.writeString(imageUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CartItem cartItem = (CartItem) o;
        return harga == cartItem.harga &&
                jumlah == cartItem.jumlah &&
                Objects.equals(email, cartItem.email) &&
                Objects.equals(namaMenu, cartItem.namaMenu) &&
                Objects.equals(imageUrl, cartItem.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, namaMenu, harga, jumlah, imageUrl);
    }
}
