// MenuPagerAdapter.java berhasil
package com.example.kepengendessboxapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class MenuPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList;

    public MenuPagerAdapter(@NonNull FragmentActivity fa, List<Fragment> fragments) {
        super(fa);
        this.fragmentList = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}