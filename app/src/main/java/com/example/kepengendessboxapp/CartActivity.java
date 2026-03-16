// CartActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItemList;
    private TextView txtJumlahItem;
    private TextView txtTotal;
    private Button btnCheckout;
    private ImageButton btnBack; // Tombol back
    private FirebaseUser user;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Inisialisasi View
        recyclerView = findViewById(R.id.recycler_view_cart);
        txtJumlahItem = findViewById(R.id.txtJumlahItem); //
        txtTotal = findViewById(R.id.txtTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack); // Ambil tombol back

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = user.getUid();

        loadCartItems();

        cartAdapter = new CartAdapter(cartItemList, new CartAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {}

            @Override
            public void onAddQuantity(int position) {
                CartItem item = cartItemList.get(position);
                item.setJumlah(item.getJumlah() + 1);
                cartAdapter.notifyItemChanged(position);
                saveCartItems();
                updateTotal(); // Update setiap ada perubahan
            }

            @Override
            public void onSubtractQuantity(int position) {
                CartItem item = cartItemList.get(position);
                if (item.getJumlah() > 1) {
                    item.setJumlah(item.getJumlah() - 1);
                    cartAdapter.notifyItemChanged(position);
                    saveCartItems();
                    updateTotal();
                }
            }

            @Override
            public void onRemoveItem(int position) {
                cartItemList.remove(position);
                cartAdapter.notifyItemRemoved(position);
                saveCartItems();
                updateTotal();
            }
        });

        recyclerView.setAdapter(cartAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateTotal(); //  Update saat pertama kali buka

        btnCheckout.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            Gson gson = new Gson();
            String json = gson.toJson(cartItemList);
            intent.putExtra("cart_items", json);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        //  Tombol back dengan animasi
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void loadCartItems() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("cart items_" + userId, null);
        Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
        cartItemList = gson.fromJson(json, type);

        if (cartItemList == null) {
            cartItemList = new ArrayList<>();
        }
    }

    private void saveCartItems() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartItemList);
        editor.putString("cart items_" + userId, json);
        editor.apply();
    }

    //  Update total harga dan jumlah item
    private void updateTotal() {
        int totalJumlah = 0;
        int totalHarga = 0;

        for (CartItem item : cartItemList) {
            totalJumlah += item.getJumlah();
            totalHarga += item.getHarga() * item.getJumlah();
        }

        txtJumlahItem.setText(totalJumlah + " Item");
        txtTotal.setText("Rp. " + rp(totalHarga));
    }

    // Format Rupiah
    public String rp(int amount) {
        Locale locale = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setMaximumFractionDigits(0);
        String formattedAmount = format.format(amount);
        return formattedAmount.replace("Rp", "").trim();
    }

    //  Animasi saat back dengan tombol fisik
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}