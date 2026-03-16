// KelolaMenuActivity.java berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class KelolaMenuActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabTambah;
    private MenuPagerAdapter menuPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_menu); // Pastikan layout benar

        // Inisialisasi View
        initView();

        // Setup ViewPager2 dengan TabLayout
        setupViewPager();

        //  Validasi FAB tidak null
        if (fabTambah == null) {
            Toast.makeText(this, "FAB tidak ditemukan!", Toast.LENGTH_LONG).show();
            return;
        }

        //  Set onClickListener
        fabTambah.setOnClickListener(v -> {
            Intent intent = new Intent(KelolaMenuActivity.this, TambahProdukActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void initView() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabTambah = findViewById(R.id.fabTambah);
    }

    private void setupViewPager() {
        // Buat daftar fragment
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new PromoFragment());      // Tab 1
        fragments.add(new DessertFragment());    // Tab 2
        fragments.add(new MinumanFragment());    // Tab 3

        // Buat adapter
        menuPagerAdapter = new MenuPagerAdapter(this, fragments);
        viewPager.setAdapter(menuPagerAdapter);

        // Hubungkan TabLayout dengan ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Paket Promo");
                    break;
                case 1:
                    tab.setText("Dessert");
                    break;
                case 2:
                    tab.setText("Minuman");
                    break;
            }
        }).attach();
    }
}