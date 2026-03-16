// ProdukBawaan.java berhasil
package com.example.kepengendessboxapp;

import java.util.ArrayList;

public class ProdukBawaan {

    public static ArrayList<Promo> getPromoBawaan() {
        ArrayList<Promo> list = new ArrayList<>();
        Promo p1 = new Promo(
                "Paket Promo 1",
                32000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.promo1,
                "Paket hemat isi dessert + 1 minuman, cocok untuk berbagi bersama teman."
        );
        p1.setTotalRating(0.0);
        p1.setJumlahUlasan(0);

        Promo p2 = new Promo(
                "Paket Promo 2",
                27000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.promo2,
                "Paket hemat isi dessert + 1 minuman, cocok untuk berbagi bersama teman."
        );
        p2.setTotalRating(0.0);
        p2.setJumlahUlasan(0);

        list.add(p1);
        list.add(p2);
        return list;
    }

    public static ArrayList<Dessert> getDessertBawaan() {
        ArrayList<Dessert> list = new ArrayList<>();

        Dessert d1 = new Dessert(
                "Dessert Box Chocolate",
                30000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.chocolate,
                "Dessert box Chocolate adalah pilihan yang sempurna untuk memuaskan pencinta cokelat."
        );
        d1.setTotalRating(0.0);
        d1.setJumlahUlasan(0);

        Dessert d2 = new Dessert(
                "Dessert Box Regal",
                30000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.regal,
                "Dessert box Regal adalah penggabungan kreatif dari kelezatan biskuit Regal dan cream yang lembut."
        );
        d2.setTotalRating(0.0);
        d2.setJumlahUlasan(0);

        Dessert d3 = new Dessert(
                "Dessert Box Milo",
                28000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.milo,
                "Dessert box Milo adalah kombinasi antara rasa Milo yang terkenal dengan kualitas premium dan presentasi yang menawan."
        );
        d3.setTotalRating(0.0);
        d3.setJumlahUlasan(0);

        Dessert d4 = new Dessert(
                "Dessert Box Red Velvet",
                28000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.red_velvet,
                "Dessert box Red Velvet adalah perpaduan manis dari berbagai macam kudapan yang mengusung tema red velvet."
        );
        d4.setTotalRating(0.0);
        d4.setJumlahUlasan(0);

        Dessert d5 = new Dessert(
                "Dessert Box Keju",
                25000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.keju,
                "Dessert box Keju adalah kombinasi yang menggabungkan kelezatan berbagai macam kudapan manis yang menggunakan keju sebagai bahan utama."
        );
        d5.setTotalRating(0.0);
        d5.setJumlahUlasan(0);

        list.add(d1);
        list.add(d2);
        list.add(d3);
        list.add(d4);
        list.add(d5);
        return list;
    }

    public static ArrayList<Minuman> getMinumanBawaan() {
        ArrayList<Minuman> list = new ArrayList<>();

        Minuman m1 = new Minuman(
                "Cappucino",
                5000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.cappucino,
                "Cappuccino adalah harmoni sempurna antara espresso yang kuat, susu yang kental, dan busa yang lembut."
        );
        m1.setTotalRating(0.0);
        m1.setJumlahUlasan(0);

        Minuman m2 = new Minuman(
                "Taro",
                5000,
                "android.resource://com.example.kepengendessboxapp/" + R.drawable.taro,
                "Minuman Taro adalah perpaduan yang menyegarkan antara kekentalan susu, manisnya bubuk taro, dan kelezatan yang memukau."
        );
        m2.setTotalRating(0.0);
        m2.setJumlahUlasan(0);

        list.add(m1);
        list.add(m2);
        return list;
    }
}